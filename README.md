    Copyright 2018 ABSA Group Limited
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

# Cobrix - COBOL Data Source for Apache Spark

Pain free Spark/Cobol files integration.

Seamlessly query your COBOL/EBCDIC binary files as Spark Dataframes and streams.   

Add mainframe as a source to your data engineering strategy.


## Motivation

Among the motivations for this project, it is possible to highlight:

- Lack of expertise in the Cobol ecosystem, which makes it hard to integrate mainframes into data engineering strategies

- Lack of support from the open-source community to initiatives in this field

- The overwhelming majority (if not all) of tools to cope with this domain are proprietary

- Several institutions struggle daily to maintain their legacy mainframes, which prevents them from evolving to more modern approaches to data management

- Mainframe data can only take part in data science activities through very expensive investments


## Features

- Supports primitive types (although some are "Cobol compiler specific")

- Supports REDEFINES, OCCURS and DEPENDING ON fields (e.g. unchecked unions and variable-size arrays)

- Supports nested structures and arrays (including "flattened" nested names)

- Supports HDFS as well as local file systems

- The COBOL copybooks parser doesn't have a Spark dependency and can be reused for integrating into other data processing engines


## Usage

#### Coordinate for Maven POM dependency

```xml
<dependency>
      <groupId>za.co.absa.cobrix</groupId>
      <artifactId>spark-cobol</artifactId>
      <version>0.2.3</version>
</dependency>
```

### Reading Cobol binary files from HDFS/local and querying them 

1. Create a Spark ```SQLContext```

2. Start a ```sqlContext.read``` operation specifying ```za.co.absa.cobrix.spark.cobol.source``` as the format

3. Inform the path to the copybook describing the files through ```... .option("copybook", "path_to_copybook_file")```

4. Inform the path to the HDFS directory containing the files: ```... .load("path_to_directory_containing_the_binary_files")``` 

5. Inform the query you would like to run on the Cobol Dataframe

Below is an example whose full version can be found at ```za.co.absa.cobrix.spark.cobol.examples.SampleApp``` and ```za.co.absa.cobrix.spark.cobol.examples.CobolSparkExample```

```scala
    val config = new SparkConf().setAppName("CobolParser")        
    val sqlContext = new SQLContext(new SparkContext(config))

    val cobolDataframe = sqlContext
      .read
      .format("cobol")      
      .option("copybook", "path_to_copybook_file") // Use "file://somefile" to use the local file system and not HDFS
      .load("path_to_directory_containing_the_binary_files") // can point to both, local and HDFS
      
    cobolDataframe
    	.filter("MBSK861_LNGTH861 % 2 = 0") // filter the even values of the nested field 'MBSK861.LNGTH861'
    	.take(10)
    	.foreach(v => println(v))
```

Alternatively, you can use `.option("copybook_contents", contents)` to provide copybook contents directly. 

### Streaming Cobol binary files from a directory

1. Create a Spark ```StreamContext```

2. Import the binary files/stream conversion manager: ```za.co.absa.spark.cobol.source.streaming.CobolStreamer._```

3. Read the binary files contained in the path informed in the creation of the ```SparkSession``` as a stream: ```... streamingContext.cobolStream()```

4. Apply queries on the stream: ```... stream.filter("some_filter") ...```

5. Start the streaming job.

Below is an example whose full version can be found at ```za.co.absa.cobrix.spark.cobol.examples.StreamingExample```

```scala
    val spark = SparkSession
      .builder()
      .appName("CobolParser")
      .master("local[2]")
      .config("duration", 2)
      .config("copybook", "path_to_the_copybook")
      .config("path", "path_to_source_directory") // could be both, local or HDFS
      .getOrCreate()          
          
    val streamingContext = new StreamingContext(spark.sparkContext, Seconds(3))         
        
    import za.co.absa.spark.cobol.source.streaming.CobolStreamer._ // imports the Cobol streams manager
    
    val stream = streamingContext.cobolStream() // streams the binary files into the application    
    
	stream
		.filter(row => row.getAs[Integer]("MBSK862_PKEYK862_PAYM-NO-K862") % 2 == 0) // filters the even values of the nested field 'MBSK862_PKEYK862_PAYM-NO-K862'
		.print(10)		
    
    streamingContext.start()
    streamingContext.awaitTermination()
```

## Other Features

### Spark SQL schema extraction
This library also provides convenient methods to extract Spark SQL schemas and Cobol layouts from copybooks.  

If you want to extract a Spark SQL schema from a copybook: 

```scala
    import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenFlatReader
    
    val reader = new FixedLenFlatReader("...copybook_contents...")
    val sparkSchema = reader.getSparkSchema
    println(sparkSchema)
```

If you want to check the layout of the copybook: 

```scala
    import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenNestedReader
    
    val reader = new FixedLenNestedReader("...copybook_contents...")
    val cobolSchema = reader.getCobolSchema
    println(cobolSchema.generateRecordLayoutPositions())
```

### Varialble length records support

Cobrix supports variable length records is an experimental feature. If a record contains a field that contains the actual record size, this
field can be specified as a data source option and Cobrix will fetch records according to the value of the field.

Usage example:
```
.option("record_length_field", "LENGTH")
```

### Schema collapsing

Mainframe data often contain only one root GROUP. In such cases such a GROUP can be considered something similar to XML rowtag.
Cobrix allows to collapse the GROUP and expand it's records. To turn this on use the following option:

```
.option("schema_retention_policy", "collapse_root")
``` 

Let's loot at an example. Let's say we have a copybook that looks like this:
```
       01  RECORD.
           05  ID                        PIC S9(4)  COMP.
           05  COMPANY.
               10  SHORT-NAME            PIC X(10).
               10  COMPANY-ID-NUM        PIC 9(5) COMP-3.
```

Normally Spark schema for such a copybook will look like this:

```
root
 |-- RECORD: struct (nullable = true)
 |    |-- ID: integer (nullable = true)
 |    |-- COMPANY: struct (nullable = true)
 |    |    |-- SHORT_NAME: string (nullable = true)
 |    |    |-- COMPANY_ID_NUM: integer (nullable = true)
```

But when "schema_retention_policy" is set to "collapse_root" the root group will be collapsed and the schema will look
like this (note the RECORD field is no longer present):
```
root
 |-- ID: integer (nullable = true)
 |-- COMPANY: struct (nullable = true)
 |    |-- SHORT_NAME: string (nullable = true)
 |    |-- COMPANY_ID_NUM: integer (nullable = true)
```

You can experiment with this feature using built-in example in `za.co.absa.cobrix.spark.cobol.examples.CobolSparkExample`


### Record Id fields generation

For data that has record order dependency generation of "File_Id" and "Record_Id" fields is supported. File_Id is unique for each file when
a directory is specified as the source for data. Record_Id is a unique sequential record identifier within the file.

Turn this feature on use
```
.option("generate_record_id", true)
``` 

The following fields will be added to the top of the schema:
```
root
 |-- File_Id: integer (nullable = false)
 |-- Record_Id: long (nullable = false)
```

Currently this feature may inpact performance and scaling, please use it with caution.  

## Performance

Tests were performed on a synthetic dataset. The setup and results are as follows.

### Cluster setup

- Spark 2.2.0 

- Driver memory: 2GB

- Driver cores: 4

- Max results: 4096

- Executor memory: 4GB

- Executors: 30

- Deploy mode: client

### Test 1

- Dataset size: 20 GB

- Dataset spec: 1018250 files in a source directory summing up to 20GB

- Execution time: 25 minutes


### Test 2

- Dataset size: 21 GB

- Dataset spec: Single file of 21GB generated by concatenating a Cobol binary file to itself

- Execution time: 3 minutes

### Test 3

- Dataset size: 3.1 GB

- Dataset spec: 4 files of 780 MB

- Execution time: 4 minutes
