    Copyright 2018 Barclays Africa Group Limited
    
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

- Mainframe data only can take part in data science activities through very expensive investments


## Features

- Supports primitive types (althouth some are "Cobol compiler specific")

- Supports REDEFINES, OCCURS and DEPENDING ON fields (e.g. unckecked unions and variable-size arrays)

- Supports nested structures and arrays (including "flattened" nested names)

- Supports HDFS as well as local file systems

- The COBOL copybooks parser don't have Spark dependency and can be reused for integrating into other data processing engines


## Usage

#### Coordinate for Maven POM dependancy

```xml
<dependency>
      <groupId>za.co.absa.cobrix</groupId>
      <artifactId>spark-cobol</artifactId>
      <version>0.1.4</version>
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
      .option("copybook", "path_to_copybook_file")
      .load("path_to_directory_containing_the_binary_files") // can point to both, local and HDFS
      
    cobolDataframe
    	.filter("MBSK861_LNGTH861 % 2 = 0") // filter the even values of the nested field 'MBSK861.LNGTH861'
    	.take(10)
    	.foreach(v => println(v))
```


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
	val configuration: org.apache.hadoop.conf.Configuration = ...
    val reader = new HDFSFlatReader(configuration, "path_to_copybook")    
    val sparkSchema = reader.getSparkSchema
    println(sparkSchema)
```  

If you want to check the layout of the copybook: 

```scala
	val configuration: org.apache.hadoop.conf.Configuration = ...
    val reader = new HDFSFlatReader(configuration, "path_to_copybook") 
    val cobolSchema = reader.getCobolSchema
    println(cobolSchema.generateRecordLayoutPositions())
```


## Performance

Tests were peformed on a synthetic dataset. The setup and results are as follows.

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
