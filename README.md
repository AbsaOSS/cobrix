    Copyright 2018-2019 ABSA Group Limited
    
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

#### Linking

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.cobrix/spark-cobol/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.cobrix/spark-cobol)

Coordinates for Maven POM dependency

```xml
<dependency>
      <groupId>za.co.absa.cobrix</groupId>
      <artifactId>spark-cobol</artifactId>
      <version>0.3.2</version>
</dependency>
```

## Usage

## Quick start

This repository contains 2 standalone examples in `examples` directory:
* `spark-type-variety` is an example of a very simple mainframe file processing.
   It is a fixed record length raw data file with a corresponding copybook. The copybook 
   contains examples of various numeric data types Cobrix supports.
* `spark-cobol-app` is an example of a Spark Job for handling multisegment variable record
   length mainframe files.  

Both of these examples can be used as a template for a Spark job application. Refer to README.md
in each example folder for the detailed guide how to run the examples locally and on a cluster.

When running `mvn clean package` in `examples/spark-cobol-app` an uber jar will be created. It can be used to run
jobs via `spark-submit` or `spark-shell`. 

### Reading Cobol binary files from HDFS/local and querying them 

1. Create a Spark ```SQLContext```

2. Start a ```sqlContext.read``` operation specifying ```za.co.absa.cobrix.spark.cobol.source``` as the format

3. Inform the path to the copybook describing the files through ```... .option("copybook", "path_to_copybook_file")```. By default the copybook
   is expected to be in HDFS. You can specify that a copybook is located in the local file system by adding `file://` prefix. For example, you
   can specify a local file like this `.option("copybook", "file:///home/user/data/compybook.cpy")`. Alternatively, instead of providing a path
   to a copybook file you can provide the contents of the copybook itself by using `.option("copybook_contents", "...copybook contents...")`. 

4. Inform the path to the HDFS directory containing the files: ```... .load("path_to_directory_containing_the_binary_files")``` 

5. Inform the query you would like to run on the Cobol Dataframe

Below is an example whose full version can be found at ```za.co.absa.cobrix.spark.cobol.examples.SampleApp``` and ```za.co.absa.cobrix.spark.cobol.examples.CobolSparkExample```

```scala
val sparkBuilder = SparkSession.builder().appName("Example")
val spark = sparkBuilder
  .getOrCreate()

val cobolDataframe = spark
  .read
  .format("cobol")
  .option("copybook", "data/test1_copybook.cob")
  .load("data/test2_data")

cobolDataframe
    .filter("RECORD.ID % 2 = 0") // filter the even values of the nested field 'RECORD_LENGTH'
    .take(10)
    .foreach(v => println(v))
```

The full example is available [here](https://github.com/AbsaOSS/cobrix/blob/master/spark-cobol/src/main/scala/za/co/absa/cobrix/spark/cobol/examples/CobolSparkExample.scala)

In some scenarios Spark is unable to find "cobol" data source by it's short name. In that case you can use the full path to the source class instead: `.format("za.co.absa.cobrix.spark.cobol.source")`

Cobrix assumes input data is encoded in EBCDIC. You can load ASCII files as well by specifying the following option:
`.option("encoding", "ascii")`.

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
    .filter(row => row.getAs[Integer]("NUMERIC_FLD") % 2 == 0) // filters the even values of the nested field 'NUMERIC_FLD'
    .print(10)		

streamingContext.start()
streamingContext.awaitTermination()
```

### Using Cobrix from a Spark shell

To query mainframe files interactively using `spark-shell` you need to provide jar(s) containing Corbrix and it's dependencies.
This can be done either by downloading all the dependencies as separate jars or by creating an uber jar that contains all
of the dependencies.

#### Getting all Cobrix dependencies

Cobrix's `spark-cobol` data source depends on the COBOL parser that is a part of Cobrix itself and on `scodec` libraries
to decode various binary formats.

The jars that you need to get are:

* spark-cobol-0.3.2.jar
* cobol-parser-0.3.2.jar
* scodec-core_2.11-1.10.3.jar
* scodec-bits_2.11-1.1.4.jar

After that you can specify these jars in `spark-shell` command line. Here is an example:
```
$ spark-shell --master yarn --deploy-mode client --driver-cores 4 --driver-memory 4G --jars spark-cobol-0.3.0.jar,cobol-parser-0.3.0.jar,scodec-core_2.11-1.10.3.jar,scodec-bits_2.11-1.1.4.jar

Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
18/09/14 14:05:45 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Spark context available as 'sc' (master = yarn, app id = application_1535701365011_2721).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.2.1
      /_/

Using Scala version 2.11.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_171)
Type in expressions to have them evaluated.
Type :help for more information.

scala> val df = spark.read.format("cobol").option("copybook", "/data/example1/test3_copybook.cob").load("/data/example1/data")
df: org.apache.spark.sql.DataFrame = [TRANSDATA: struct<CURRENCY: string, SIGNATURE: string ... 4 more fields>]

scala> df.show(false)
+----------------------------------------------------+
|TRANSDATA                                           |
+----------------------------------------------------+
|[GBP,S9276511,Beierbauh.,0123330087,1,89341.00]     |
|[ZAR,S9276511,Etqutsa Inc.,0039003991,1,2634633.00] |
|[USD,S9276511,Beierbauh.,0038903321,0,75.71]        |
|[ZAR,S9276511,Beierbauh.,0123330087,0,215.39]       |
|[ZAR,S9276511,Test Bank,0092317899,1,643.94]        |
|[ZAR,S9276511,Xingzhoug,8822278911,1,998.03]        |
|[USD,S9276511,Beierbauh.,0123330087,1,848.88]       |
|[USD,S9276511,Beierbauh.,0123330087,0,664.11]       |
|[ZAR,S9276511,Beierbauh.,0123330087,1,55262.00]     |
+----------------------------------------------------+
only showing top 20 rows

scala>
``` 

#### Creating an uber jar

Gathering all dependencies manually maybe a tiresome task. A better approach would be to create a jar file that contains
all of the dependencies.

Creating an uber jar for Cobrix is very easy. Just go to a folder with one of the examples, say, `examples/spark-cobol-app`.
After that run `mvn package`. Then, copy `target/spark-cobol-app-0.0.1-SNAPSHOT.jar` to a clustrr and run:

```
$ spark-shell --jars spark-cobol-app-0.0.1-SNAPSHOT.jar
```

Our example pom projects are set up so an uber jar is built every time you build it using Maven.

## Other Features

### Spark SQL schema extraction
This library also provides convenient methods to extract Spark SQL schemas and Cobol layouts from copybooks.  

If you want to extract a Spark SQL schema from a copybook: 

```scala
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.schema.{CobolSchema, SchemaRetentionPolicy}

val parsedSchema = CopybookParser.parseTree(copyBookContents)
val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot)
val sparkSchema = cobolSchema.getSparkSchema.toString()
println(sparkSchema)
```

If you want to check the layout of the copybook: 

```scala
import za.co.absa.cobrix.cobol.parser.CopybookParser

val copyBook = CopybookParser.parseTree(copyBookContents)
println(copyBook.generateRecordLayoutPositions())
```

### Varialble length records support

Cobrix supports variable record length files. The only requirement is that such a file should contain a standard 4 byte
record header known as Record Descriptor Word (RDW). Such headers are created automatically when a variable record length
file is copied from a mainframe.

To load variable length record file the following option should be specified:
```
.option("is_record_sequence", "true")
```

The space used by the headers should not be mentioned in the copybook if this option is used. Please refer to the
'Record headers support' section below. 

### Schema collapsing

Mainframe data often contain only one root GROUP. In such cases such a GROUP can be considered something similar to XML rowtag.
Cobrix allows to collapse the GROUP and expand it's records. To turn this on use the following option:

```
.option("schema_retention_policy", "collapse_root")
```

Let's loot at an example. Let's say we have a copybook that looks like this:
```cobol
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

For data that has record order dependency generation of "File_Id" and "Record_Id" fields is supported. The values of the File_Id column will
be unique for each file when a directory is specified as the source for data. The values of the Record_Id column will be unique and sequential
record identifiers within the file.

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

### Locality optimization for variable-length records parsing

Variable-length records depend on headers to have their length calculated, which makes it hard to achieve parallelism while parsing.

Cobrix strives to overcome this drawback by performing a two-stages parsing. The first stage traverses the records retrieving their lengths
and offsets into structures called indexes. Then, the indexes are distributed across the cluster, which allows for parallel variable-length
records parsing.

However effective, this strategy may also suffer from excessive shuffling, since indexes may be sent to executors far from the actual data.

The latter issue is overcome by extracting the preferred locations for each index directly from HDFS, and then passing those locations to
Spark during the creation of the RDD that distributes the indexes.

When processing large collections, the overhead of collecting the locations is offset by the benefits of locality, thus, this feature is
enabled by default, but can be disabled by the configuration below:
```
.option("improve_locality", false)
```

### Workload optimization for variable-length records parsing

When dealing with variable-length records, Cobrix strives to maximize locality by identifying the preferred locations in the cluster to parse
each record, i.e. the nodes where the record resides.

This feature is implemented by querying HDFS about the locations of the blocks containing each record and instructing Spark to create the
partition for that record in one of those locations.

However, sometimes, new nodes can be added to the cluster after the Cobol file is stored, in which case those nodes would be ignored when
processing the file since they do not contain any record.

To overcome this issue, Cobrix also strives to re-balance the records among the new nodes at parsing time, as an attempt to maximize the
utilization of the cluster. This is done through identifying the busiest nodes and sharing part of their burden with the new ones.

Since this is not an issue present in most cluster configurations, this feature is disabled by default, and can be enabled from the
configuration below:
```
.option("optimize_allocation", true)
```

If however the option ```improve_locality``` is disabled, this option will also be disabled regardless of the value in ```optimize_allocation```.

### Record headers support

As you may already know a file in the mainframe world does not mean the same as in the PC world. On PCs we think of a file
as a stream of bytes that we can open, read/write and close. On mainframes a file can be a set of records that we can query.
Record is a blob of bytes, can have different size. Mainframe's 'filesystem' handles the mapping between logical records
and physical location of data.

> _Details are available at this [Wikipedia article](https://en.wikipedia.org/wiki/MVS) (look for MVS filesystem)._ 

So usually a file cannot simply be 'copied' from a mainframe. When files are transferred using tools like XCOM each
record is prepended with an additional *record header* or *RDW*. This header allows readers of a file in PC to restore the
'set of records' nature of the file.

Mainframe files coming from IMS and copied through specialized tools contain records (the payload) having schema of DBs
copybook warped with DB export tool headers wrapped with record headers. Like this:

RECORD_HEADERS ( TOOL_HEADERS ( PAYLOAD ) )

> _Similar to Internet's TCP protocol   IP_HEADERS ( TCP_HEADERS ( PAYLOAD ) )._

TOOL_HEADERS are application dependent. Often it contains the length of the payload. But this length is sometime
not very reliable. RECORD_HEADERS contain the record length (including TOOL_HEADERS length) and are proved to be reliable.

For fixed record length files record headers can be ignored since we already know the record length. But for variable
record length files and for multisegment files record headers can be considered the most reliable single point of truth
about record length.

You can instruct the reader to use 4 byte record headers to extract records from a mainframe file.

```
.option("is_record_sequence", "true")
```

This is very helpful for multisegment files when segments have different lengths. Since each segment has it's own
copybook it is very convenient to extract segments one by one by combining 'is_record_sequence' option with segment
filter option.

```
.option("segment_field", "SEG-ID")
.option("segment_filter", "1122334")
```

In this example it is expected that the copybook has a field with the name 'SEG-ID'. The data source will read all
segments, but will parse only ones that have `SEG-ID = "1122334"`.

If you want to parse multiple segments, set the option 'segment_filter' to a comma separated list of the segment values.
For example:
```
.option("segment_field", "SEG-ID")
.option("segment_filter", "1122334,1122335")
```
will only parse the records with `SEG-ID = "1122334" OR SEG-ID = "1122335"`

## <a id="ims"/>Reading hierarchical data sets

Let's imagine we have a multisegment file with 2 segments having parent-child relationships. Each segment has a different
record type. The root record/segment contains company info, an addreess and a taxpayer number. The child segment contains
a contact person for a company. Each company can have zero or more contact persons. So each root record can be followed by
zero or more child records.

To load such data in Spark the first thing you need to do is to create a copybook that contains all segment specific fields
in redefined groups. Here is the copybook for our example:

```cobol
        01  COMPANY-DETAILS.
            05  SEGMENT-ID        PIC X(5).
            05  COMPANY-ID        PIC X(10).
            05  STATIC-DETAILS.
               10  COMPANY-NAME      PIC X(15).
               10  ADDRESS           PIC X(25).
               10  TAXPAYER.
                  15  TAXPAYER-TYPE  PIC X(1).
                  15  TAXPAYER-STR   PIC X(8).
                  15  TAXPAYER-NUM  REDEFINES TAXPAYER-STR
                                     PIC 9(8) COMP.

            05  CONTACTS REDEFINES STATIC-DETAILS.
               10  PHONE-NUMBER      PIC X(17).
               10  CONTACT-PERSON    PIC X(28).
```

The 'SEGMENT-ID' and 'COMPANY-ID' fields are present in all of the segments. The 'STATIC-DETAILS' group is present only in
the root record. The 'CONTACTS' group is present only in child record. Notice that 'CONTACTS' redefine 'STATIC-DETAILS'.

Because the records have different lengths the 'is_record_sequence' option should be set to 'true'.

If you load this file as is you will get the schema and the data similar to this.

#### Spark App:
```scala
val df = spark
  .read
  .format("cobol")
  .option("copybook", "/path/to/thecopybook")
  .option("schema_retention_policy", "collapse_root")     // Collapses the root group returning it's field on the top level of the schema
  .option("is_record_sequence", "true")
  .load("examples/multisegment_data")
```

#### Schema
```
df.printSchema()
root
 |-- SEGMENT_ID: string (nullable = true)
 |-- COMPANY_ID: string (nullable = true)
 |-- STATIC_DETAILS: struct (nullable = true)
 |    |-- COMPANY_NAME: string (nullable = true)
 |    |-- ADDRESS: string (nullable = true)
 |    |-- TAXPAYER: struct (nullable = true)
 |    |    |-- TAXPAYER_TYPE: string (nullable = true)
 |    |    |-- TAXPAYER_STR: string (nullable = true)
 |    |    |-- TAXPAYER_NUM: integer (nullable = true)
 |-- CONTACTS: struct (nullable = true)
 |    |-- PHONE_NUMBER: string (nullable = true)
 |    |-- CONTACT_PERSON: string (nullable = true)
```

#### Data sample
```
df.show(10)
+----------+----------+--------------------+--------------------+
|SEGMENT_ID|COMPANY_ID|      STATIC_DETAILS|            CONTACTS|
+----------+----------+--------------------+--------------------+
|         C|9377942526|[Joan Q & Z,10 Sa...|[Joan Q & Z     1...|
|         P|9377942526|[+(277) 944 44 5,...|[+(277) 944 44 55...|
|         C|3483483977|[Robotrd Inc.,2 P...|[Robotrd Inc.   2...|
|         P|3483483977|[+(174) 970 97 5,...|[+(174) 970 97 54...|
|         P|3483483977|[+(848) 832 61 6,...|[+(848) 832 61 68...|
|         P|3483483977|[+(455) 184 13 3,...|[+(455) 184 13 39...|
|         C|7540764401|[Eqartion Inc.,87...|[Eqartion Inc.  8...|
|         C|4413124035|[Xingzhoug,74 Qin...|[Xingzhoug      7...|
|         C|9546291887|[ZjkLPj,5574, Tok...|[ZjkLPj         5...|
|         P|9546291887|[+(300) 252 33 1,...|[+(300) 252 33 17...|
+----------+----------+--------------------+--------------------+
```

As you can see Cobrix loaded *all* redefines for *every* record. Each record contains data from all of the segments. But only
one redefine is valid for every segment. So we need to split the data set into 2 datasets or tables. The distinguisher is
the 'SEGMENT_ID' field. All company details will go into one data sets (segment id = 'C' [company]) while contacts will go in
the second data set (segment id = 'P' [person]). While doing the split we can also collapse the groups so the table won't
contain nested structures. This can be helpful to simplify the analysis of the data.

While doing it you might notice that the taxpayer number field is actually a redefine. Depending on the 'TAXPAYER_TYPE'
either 'TAXPAYER_NUM' or 'TAXPAYER_STR' is used. We can resolve this in our Spark app as well. 

#### Getting the first segment
```scala
import spark.implicits._

val dfCompanies = df
  // Filtering the first segment by segment id
  .filter($"SEGMENT_ID"==="C")
  // Selecting fields that are only available in the first segment
  .select($"COMPANY_ID", $"STATIC_DETAILS.COMPANY_NAME", $"STATIC_DETAILS.ADDRESS",
  // Resolving the taxpayer redefine
    when($"STATIC_DETAILS.TAXPAYER.TAXPAYER_TYPE" === "A", $"STATIC_DETAILS.TAXPAYER.TAXPAYER_STR")
      .otherwise($"STATIC_DETAILS.TAXPAYER.TAXPAYER_NUM").cast(StringType).as("TAXPAYER"))
```

The resulting table looks like this:
```
dfCompanies.show(10, truncate = false)
+----------+-------------+-------------------------+--------+
|COMPANY_ID|COMPANY_NAME |ADDRESS                  |TAXPAYER|
+----------+-------------+-------------------------+--------+
|9377942526|Joan Q & Z   |10 Sandton, Johannesburg |92714306|
|3483483977|Robotrd Inc. |2 Park ave., Johannesburg|31195396|
|7540764401|Eqartion Inc.|871A Forest ave., Toronto|87432264|
|4413124035|Xingzhoug    |74 Qing ave., Beijing    |50803302|
|9546291887|ZjkLPj       |5574, Tokyo              |73538919|
|9168453994|Test Bank    |1 Garden str., London    |82573513|
|4225784815|ZjkLPj       |5574, Tokyo              |96136195|
|8463159728|Xingzhoug    |74 Qing ave., Beijing    |17785468|
|8180356010|Eqartion Inc.|871A Forest ave., Toronto|79054306|
|7107728116|Xingzhoug    |74 Qing ave., Beijing    |70899995|
+----------+-------------+-------------------------+--------+
```

This looks like a valid and clean table containing the list of companies. Now let's do the same for the second segment.

#### Getting the second segment
```scala
    val dfContacts = df
      // Filtering the second segment by segment id
      .filter($"SEGMENT_ID"==="P")
      // Selecting the fields only valid for the second segment
      .select($"COMPANY_ID", $"CONTACTS.CONTACT_PERSON", $"CONTACTS.PHONE_NUMBER")
```

The resulting data loons like this:
```
dfContacts.show(10, truncate = false)
+----------+--------------------+----------------+
|COMPANY_ID|CONTACT_PERSON      |PHONE_NUMBER    |
+----------+--------------------+----------------+
|9377942526|Janiece Newcombe    |+(277) 944 44 55|
|3483483977|Tyesha Debow        |+(174) 970 97 54|
|3483483977|Mindy Celestin      |+(848) 832 61 68|
|3483483977|Mabelle Winburn     |+(455) 184 13 39|
|9546291887|Carrie Celestin     |+(300) 252 33 17|
|9546291887|Edyth Deveau        |+(907) 101 70 64|
|9546291887|Jene Norgard        |+(694) 918 17 44|
|9168453994|Timika Bourke       |+(768) 691 44 85|
|9168453994|Lynell Riojas       |+(695) 918 33 16|
|4225784815|Jene Mackinnon      |+(540) 937 33 71|
+----------+--------------------+----------------+
```

This looks good as well. The table contains the list of contact persons for companies. This data set contains the
'COMPANY_ID' field which we can use later to join the tables. But often there are no such fields in data imported from
hierarchical databases. If that is the case Cobrix can help you craft such fields automatically. Use 'segment_field' to
specify a field that contain the segment id. Use 'segment_id_level0' to ask Cobrix to generate ids for the particular
segments. We can use 'segment_id_level1' to generate child ids as well. If children records can contain children of their
own we can use 'segment_id_level2' etc.

#### Generating segment ids

```scala
val df = spark
  .read
  .format("cobol")
  .option("copybook_contents", copybook)
  .option("schema_retention_policy", "collapse_root")
  .option("is_record_sequence", "true")
  .option("segment_field", "SEGMENT_ID")
  .option("segment_id_level0", "C")
  .option("segment_id_level1", "P")
  .load("examples/multisegment_data/COMP.DETAILS.SEP30.DATA.dat")
```

The resulting table will look like this:
```
df.show(10)
+------------------+-----------------------+----------+----------+--------------------+--------------------+
|           Seg_Id0|                Seg_Id1|SEGMENT_ID|COMPANY_ID|      STATIC_DETAILS|            CONTACTS|
+------------------+-----------------------+----------+----------+--------------------+--------------------+
|20181219130609_0_0|                   null|         C|9377942526|[Joan Q & Z,10 Sa...|[Joan Q & Z     1...|
|20181219130609_0_0|20181219130723_0_0_L1_1|         P|9377942526|[+(277) 944 44 5,...|[+(277) 944 44 55...|
|20181219130609_0_2|                   null|         C|3483483977|[Robotrd Inc.,2 P...|[Robotrd Inc.   2...|
|20181219130609_0_2|20181219130723_0_2_L1_1|         P|3483483977|[+(174) 970 97 5,...|[+(174) 970 97 54...|
|20181219130609_0_2|20181219130723_0_2_L1_2|         P|3483483977|[+(848) 832 61 6,...|[+(848) 832 61 68...|
|20181219130609_0_2|20181219130723_0_2_L1_3|         P|3483483977|[+(455) 184 13 3,...|[+(455) 184 13 39...|
|20181219130609_0_6|                   null|         C|7540764401|[Eqartion Inc.,87...|[Eqartion Inc.  8...|
|20181219130609_0_7|                   null|         C|4413124035|[Xingzhoug,74 Qin...|[Xingzhoug      7...|
|20181219130609_0_8|                   null|         C|9546291887|[ZjkLPj,5574, Tok...|[ZjkLPj         5...|
|20181219130609_0_8|20181219130723_0_8_L1_1|         P|9546291887|[+(300) 252 33 1,...|[+(300) 252 33 17...|
+------------------+-----------------------+----------+----------+--------------------+--------------------+
```

The data now contain 2 additional fields: 'Seg_Id0' and 'Seg_Id1'. The 'Seg_Id0' is an autogenerated id for each root
record. It is also unique for a root record. After splitting the segments you can use Seg_Id0 to join both tables.
The 'Seg_Id1' field contains a unique child id. It is equal to 'null' for all root records but uniquely identifies
child records.

You can now split these 2 segments and join them by Seg_Id0. The full example is available at
`spark-cobol/src/main/scala/za/co/absa/cobrix/spark/cobol/examples/CobolSparkExample2.scala`

To run it from an IDE you'll need to change Scala and Spark dependencies from 'provided' to 'compile' so the
jar file would contain all the dependencies. This is because Cobrix is a library to be used in Spark job projects.
Spark jobs uber jars should not contain Scala and Spark dependencies since Hadoop clusters have their Scala and Spark
dependencies provided by the infrastructure. Including Spark and Scala dependencies in an uber jar can produce
binary incompatibilities when these jars are used in `spark-submit` and `spark-shell`.

Here is our example tables to join:

##### Segment 1 (Companies)
```
dfCompanies.show(10, truncate = false)
+--------------------+----------+-------------+-------------------------+--------+
|Seg_Id0             |COMPANY_ID|COMPANY_NAME |ADDRESS                  |TAXPAYER|
+--------------------+----------+-------------+-------------------------+--------+
|20181219130723_0_0  |9377942526|Joan Q & Z   |10 Sandton, Johannesburg |92714306|
|20181219130723_0_2  |3483483977|Robotrd Inc. |2 Park ave., Johannesburg|31195396|
|20181219130723_0_6  |7540764401|Eqartion Inc.|871A Forest ave., Toronto|87432264|
|20181219130723_0_7  |4413124035|Xingzhoug    |74 Qing ave., Beijing    |50803302|
|20181219130723_0_8  |9546291887|ZjkLPj       |5574, Tokyo              |73538919|
|20181219130723_0_12 |9168453994|Test Bank    |1 Garden str., London    |82573513|
|20181219130723_0_15 |4225784815|ZjkLPj       |5574, Tokyo              |96136195|
|20181219130723_0_20 |8463159728|Xingzhoug    |74 Qing ave., Beijing    |17785468|
|20181219130723_0_24 |8180356010|Eqartion Inc.|871A Forest ave., Toronto|79054306|
|20181219130723_0_27 |7107728116|Xingzhoug    |74 Qing ave., Beijing    |70899995|
+--------------------+----------+-------------+-------------------------+--------+
```

##### Segment 2 (Contacts)
```
dfContacts.show(13, truncate = false)
+-------------------+----------+-------------------+----------------+
|Seg_Id0            |COMPANY_ID|CONTACT_PERSON     |PHONE_NUMBER    |
+-------------------+----------+-------------------+----------------+
|20181219130723_0_0 |9377942526|Janiece Newcombe    |+(277) 944 44 55|
|20181219130723_0_2 |3483483977|Tyesha Debow        |+(174) 970 97 54|
|20181219130723_0_2 |3483483977|Mindy Celestin      |+(848) 832 61 68|
|20181219130723_0_2 |3483483977|Mabelle Winburn     |+(455) 184 13 39|
|20181219130723_0_8 |9546291887|Carrie Celestin     |+(300) 252 33 17|
|20181219130723_0_8 |9546291887|Edyth Deveau        |+(907) 101 70 64|
|20181219130723_0_8 |9546291887|Jene Norgard        |+(694) 918 17 44|
|20181219130723_0_12|9168453994|Timika Bourke       |+(768) 691 44 85|
|20181219130723_0_12|9168453994|Lynell Riojas       |+(695) 918 33 16|
|20181219130723_0_15|4225784815|Jene Mackinnon      |+(540) 937 33 71|
|20181219130723_0_15|4225784815|Timika Concannon    |+(122) 216 11 25|
|20181219130723_0_15|4225784815|Jene Godfrey        |+(285) 643 50 47|
|20181219130723_0_15|4225784815|Gabriele Winburn    |+(489) 644 53 67|
+-------------------+----------+-------------------+----------------+

```

Let's now join these tables.

##### Joined datasets

The join statement in Spark:
```scala
val dfJoined = dfCompanies.join(dfContacts, "Seg_Id0")
```

The joined data looks like this:

```
dfJoined.show(13, truncate = false)
+--------------------+----------+-------------+-------------------------+--------+----------+--------------------+----------------+
|Seg_Id0             |COMPANY_ID|COMPANY_NAME |ADDRESS                  |TAXPAYER|COMPANY_ID|CONTACT_PERSON      |PHONE_NUMBER    |
+--------------------+----------+-------------+-------------------------+--------+----------+--------------------+----------------+
|20181219130723_0_0  |9377942526|Joan Q & Z   |10 Sandton, Johannesburg |92714306|9377942526|Janiece Newcombe    |+(277) 944 44 55|
|20181219131239_0_2  |3483483977|Robotrd Inc. |2 Park ave., Johannesburg|31195396|3483483977|Mindy Celestin      |+(848) 832 61 68|
|20181219131239_0_2  |3483483977|Robotrd Inc. |2 Park ave., Johannesburg|31195396|3483483977|Tyesha Debow        |+(174) 970 97 54|
|20181219131239_0_2  |3483483977|Robotrd Inc. |2 Park ave., Johannesburg|31195396|3483483977|Mabelle Winburn     |+(455) 184 13 39|
|20181219131344_0_8  |9546291887|ZjkLPj       |5574, Tokyo              |73538919|9546291887|Jene Norgard        |+(694) 918 17 44|
|20181219131344_0_8  |9546291887|ZjkLPj       |5574, Tokyo              |73538919|9546291887|Edyth Deveau        |+(907) 101 70 64|
|20181219131344_0_8  |9546291887|ZjkLPj       |5574, Tokyo              |73538919|9546291887|Carrie Celestin     |+(300) 252 33 17|
|20181219131344_0_12 |9168453994|Test Bank    |1 Garden str., London    |82573513|9168453994|Timika Bourke       |+(768) 691 44 85|
|20181219131344_0_12 |9168453994|Test Bank    |1 Garden str., London    |82573513|9168453994|Lynell Riojas       |+(695) 918 33 16|
|20181219131344_0_15 |4225784815|ZjkLPj       |5574, Tokyo              |96136195|4225784815|Jene Mackinnon      |+(540) 937 33 71|
|20181219131344_0_15 |4225784815|ZjkLPj       |5574, Tokyo              |96136195|4225784815|Timika Concannon    |+(122) 216 11 25|
|20181219131344_0_15 |4225784815|ZjkLPj       |5574, Tokyo              |96136195|4225784815|Jene Godfrey        |+(285) 643 50 47|
|20181219131344_0_15 |4225784815|ZjkLPj       |5574, Tokyo              |96136195|4225784815|Gabriele Winburn    |+(489) 644 53 67|
+--------------------+----------+-------------+-------------------------+--------+----------+--------------------+----------------+
```

Again, the full example is available at
`spark-cobol/src/main/scala/za/co/absa/cobrix/spark/cobol/examples/CobolSparkExample2.scala`

## Group Filler dropping

A FILLER is an anonymous field that is usually used for reserving space for new fields in a fixed record length data.
Or it is used to remove a field from a copybook without affecting compatibility.

```cobol
      05  COMPANY.
          10  NAME      PIC X(15).
          10  FILLER    PIC X(5).
          10  ADDRESS   PIC X(25).
          10  FILLER    PIC X(125).
``` 
Such fields are dropped when imported into a Spark data frame by Cobrix. Some copybooks, however, have FILLER groups that
contain non-filler fields. For example,
```cobol
      05  FILLER.
          10  NAME      PIC X(15).
          10  ADDRESS   PIC X(25).
      05  FILLER.
          10  AMOUNT    PIC 9(10)V96.
          10  COMMENT   PIC X(40).
``` 
By default Cobrix will retain such fields, but will rename each such filler to a unique name so each each individual struct
can be specified unambiguously. For example, in this case the filler groups will be renamed to `FILLER_1` and `FILLER_2`.
You can change this behaviour if you would like to drop such filler groups by providing this option:
```
.option("drop_group_fillers", "true")
```

## Performance Analysis

Performance tests were performed on synthetic datasets. The setup and results are as follows.

### Cluster setup

- Spark 2.2.1 
- Driver memory: 4GB
- Driver cores: 4
- Executor memory: 4GB
- Cores per executor: 1

### Test Applications

The test Spark Application is just a conversion from the mainframe format to Parquet.

For fixed record length tests:
```scala
    val sparkBuilder = SparkSession.builder().appName("Performance test")
    val spark = sparkBuilder
      .getOrCreate()

    val copybook = "...copybook contents..."
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("schema_retention_policy", "collapse_root")
      .load(args(0))
    
      df.write.mode(SaveMode.Overwrite).parquet(args(1))
```

For multisegment variable lengths tests:
```scala
    val sparkBuilder = SparkSession.builder().appName("Performance test")
    val spark = sparkBuilder
      .getOrCreate()

    val copybook = "...copybook contents..."
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("schema_retention_policy", "collapse_root")
      .option("is_record_sequence", "true")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_level0", "C")
      .load(args(0))
    
      df.write.mode(SaveMode.Overwrite).parquet(args(1))
```

### Performance Test 1. Fixed record length raw file

- Raw (single segment) fixed length record file
- 167 columns
- 1341 bytes per record
- 30,000,000 records
- 40 GB single input file size
- The data can be generated using `za.co.absa.cobrix.cobol.parser.examples.TestDataGen6TypeVariety` generqator app

![](performance/images/exp1_raw_records_time.svg) ![](performance/images/exp1_raw_efficiency.svg)    
![](performance/images/exp1_raw_records_throughput.svg) ![](performance/images/exp1_raw_mb_throughput.svg)

### Performance Test 2. Multisegment narrow file

- Multisegment variable length record file
- A copybook containing very few fields
- 68 bytes per segment 1 record and 64 bytes per segment 2 record
- 640,000,000 records
- 40 GB single input file size
- The data can be generated using `za.co.absa.cobrix.cobol.parser.examples.TestDataGen3Companies` generqator app

![](performance/images/exp2_multiseg_narrow_time.svg) ![](performance/images/exp2_multiseg_narrow_efficiency.svg)    
![](performance/images/exp2_multiseg_narrow_records_throughput.svg) ![](performance/images/exp2_multiseg_narrow_mb_throughput.svg)


### Performance Test 3. Multisegment wide file

- Multisegment variable length record file
- A copybook containing a segment with arrays of 2000 struct elements. This reproduces a case when a copybook contain a lot of fields.
- 16068 bytes per segment 1 record and 64 bytes per segment 2 record
- 8,000,000 records
- 40 GB single input file size
- The data can be generated using `za.co.absa.cobrix.cobol.parser.examples.generatorsTestDataGen4CompaniesWide` generqator app

![](performance/images/exp3_multiseg_wide_time.svg) ![](performance/images/exp3_multiseg_wide_efficiency.svg)    
![](performance/images/exp3_multiseg_wide_records_throughput.svg) ![](performance/images/exp3_multiseg_wide_mb_throughput.svg)

## Changelog

- #### 0.3.2 released 14 Feb 2019
  - This is a minor feature release.
  - Added support for big endian RDW headers in record sequence files `option("is_rdw_big_endian", "true")`. By default RDW headers are expected to be little-endian (for compatibility with earlier versions of Cobrix).
  - Improved default settings for sparse index generation to achieve better data locality.
  - Fixed segment id filter pushdown if the segment id field contains non-decodable values

- #### 0.3.1 released 11 Jan 2019
  - This is a maintenance release.
  - Added support for PICs specifying separate sign character (for example, `9(4)+`).
  - Added support for PICs characters specifying zeros masking (for example, `Z(4)`).
  - Added support for reading ASCII data files using `option("encoding", "ascii")`.

- #### 0.3.0 released 17 Dec 2018
  - This is a minor feature release. There are changes that change behavior.
  - Added balancing partitions among potentially idle executors. See the section on locality optimization.
  - Added performance charts to README.md
  - Added 2 standalone projects in the 'examples' folder. It can be used as templates for creating Spark jobs that use Cobrix
  - Added option to drop all FILLER GROUPs. See section "Group Filler dropping" describing the new option.
  - Fixed handling of GROUP fields having only FILLER nested fields. Such groups are removed automatically because some formats don't support empty struct fields.
  - All parser offsets are now in bytes instaed of bits.
  - COMP-5 is similar to COMP-4, the truncation happens by the size of the binary data, not exactly by PIC precision specification. For COMP-5 numbers are expected to be big endian.
  - Added artificial COMP-9 format for little endian binary numbers.

- #### 0.2.11 released 10 Dec 2018
  - Added partitioning at the record-level by leveraging HDFS locations. This makes Cobrix data locality aware data source.
  - Parser decoders have been rewritten resulting in about 20% increase of performance
  - Added support for IEEE-754 floating point numbers for COMP-1 and COMP-2
  - Added support for decimal numbers with explicit decimal point
  - Added support for DISPLAY-formatted numbers sign overpunching
  - Added support for DISPLAY-formatted numbers sign separate (both leading and trailing)
  - Added support for very big numbers (when precision is bigger than 18)
  - Added ability to filter by several segment ids in a multiple segment file (Thanks Peter Moon)
  - Syntax check made more strict, added more diagnostic messages.
  - The "is_xcom" option is renamed to "is_record_sequence" since other tools provide such a header as well. The old option remains for compatibility.

|            Option (usage example)          |                           Description |
| ------------------------------------------ |:--------------------------------------------------------- |
| .option("is_record_sequence", "true")      | Specifies that input files have byte record headers.      |


- #### 0.2.10 released 26 Nov 2018
  - Fixed file retrieval by complying with HDFS patterns and supporting glob patterns.
  - Increased performance of BINARY/COMP formatted numbers by rewriting binary number decoders.
  - Made partition split by size more accurate, make it the default split type
  - Aligned input split terminology according to other Spark data sources (see the table below)
    - When both split options are specified 'input_split_records' is used
    - When none split of the split options are specified the default is split by size of 100 MB

|            Option (usage example)          |                           Description |
| ------------------------------------------ |:----------------------------------------------------------------------------- |
| .option("input_split_records", 50000)      | Specifies how many records will be allocated to each split/partition. It will be processed by Spark tasks. (The default is 50K records) |
| .option("input_split_size_mb", 100)        | Specify how many megabytes to allocate to each partition/split. (The default is 100 MB) |

- #### 0.2.9 released 21 Nov 2018
  - Added an index generation for multisegment variable record size files to make the reader scalable.
    - It is turned on by default, you can restore old behaviour using .option("allow_indexing", "false")
  - Added options to control index generation (first table below).
  - Added generation of helper fields for hierarchical databases (second table below). These helper fields allows to split a dataset into individual segments and then join them.
    The helper fields will contain segment ids that can be used for joining the resulting tables. See [the guide on loading hierarchical data sets above](#ims).
  - Fixed many performance issues that should make reading mainframe files several times faster. The actual performance depends on concrete copybooks.

##### Multisegment indexing options

|            Option (usage example)          |                           Description |
| ------------------------------------------ |:----------------------------------------------------------------------------- |
| .option("is_xcom", "true")                 | Indexing is supported only on files having XCOM headers at the moment.        |
| .option("allow_indexing", "true")          | Turns on indexing of multisegment variable length files (on by default).      |
| .option("records_per_partition", 50000)    | Specifies how many records will be allocated to each partition. It will be processed by Spark tasks. |
| .option("partition_size_mb", 100)          | Specify how many megabytes to allocate to each partition. This overrides the above option. (This option is **experimental**) |
| .option("segment_field", "SEG-ID")         | Specify a segment id field name. This is to ensure the splitting is done using root record boundaries for hierarchical datasets. The first record will be considered a root segment record. |
     
##### Helper fields generation options    

|            Option (usage example)          |                           Description |
| ------------------------------------------ |:---------------------------------------------------------------------------- |
| .option("segment_field", "SEG-ID")         | Specified the field in the copybook containing values of segment ids.         |
| .option("segment_filter", "S0001")         | Allows to add a filter on the segment id that will be pushed down the reader. This is if the intent is to extract records only of a particular segments. |
| .option("segment_id_level0", "SEGID-ROOT") | Specifies segment id value for root level records. When this option is specified the Seg_Id0 field will be generated for each root record |
| .option("segment_id_level1", "SEGID-CLD1") | Specifies segment id value for child level records. When this option is specified the Seg_Id1 field will be generated for each root record |
| .option("segment_id_level2", "SEGID-CLD2") | Specifies segment id value for child of a child level records. When this option is specified the Seg_Id2 field will be generated for each root record. You can use levels 3, 4 etc. |
| .option("segment_id_prefix", "A_PREEFIX")  | Specifies a prefix to be added to each segment id value. This is to mage generated IDs globally unique. By default the prefix is the current timestamp in form of '201811122345_'. |
   

## Dicalaimer

Companies, Names, Ids and values in all examples present in this project/repository are completely fictional and
were generated randomly. Any resemblance to actual persons, companies or actual transactions is purely coincidental.
