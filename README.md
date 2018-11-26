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

#### Linking

Coordinates for Maven POM dependency

```xml
<dependency>
      <groupId>za.co.absa.cobrix</groupId>
      <artifactId>spark-cobol</artifactId>
      <version>0.2.10</version>
</dependency>
```

## Usage

### Reading Cobol binary files from HDFS/local and querying them 

1. Create a Spark ```SQLContext```

2. Start a ```sqlContext.read``` operation specifying ```za.co.absa.cobrix.spark.cobol.source``` as the format

3. Inform the path to the copybook describing the files through ```... .option("copybook", "path_to_copybook_file")```

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

For an alternative copybook specification you can use `.option("copybook_contents", contents)` to provide copybook contents directly. 

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

### XCOM headers support

As you may already know file in mainframe world does not mean the same as in PC world.
On PCs we think of a file as a stream of bytes that we can open, read/write and close.
On mainframes a file can be a set of records that we can query. Record is a blob of bytes,
can have different size. Mainframe's 'filesystem' handles the mapping between logical
records and physical location of data.

> _Details are available at this [Wikipedia article](https://en.wikipedia.org/wiki/MVS) (look for MVS filesystem)._ 

So usually a file cannot simply be 'copied' from a mainframe. When files are transferred
using tools like XCOM each record is prepended with an additional header. This header
allows readers of a file in PC to restore the 'set of records' nature of the file.

Mainframe files coming from IMS and copied through XCOM contain records (the payload)
having schema of DBs copybook warped with DB export tool headers wrapped with XCOM headers.
Like this:

XCOM_HEADERS ( TOOL_HEADERS ( PAYLOAD ) )

> _Similar to Internet's IP protocol   IP_HEADERS ( TCP_HEADERS ( PAYLOAD ) )._

TOOL_HEADERS are application dependent. Often it contains the length of the payload. But this length is sometime
not very reliable. XCOM_HEADERS contain the record length (including TOOL_HEADERS length) and are proved to be reliable.

For fixed record length files XCOM files can be ignored since we already know the record length. But for variable record
length files and for multisegment files XCOM headers can be considered the most reliable single point of truth about record length.

You can instruct the reader to use XCOM headers to extract records from a mainframe file.

```
.option("is_xcom", "true")
```

This is very helpful for multisegment files when segments have different lengths. Since each segment has it's own
copybook it is very convenient to extract segments one by one by combining 'is_xcom' option with segment filter option.

```
.option("segment_field", "SEG-ID")
.option("segment_filter", "1122334")
```

In this example it is expected that the copybook has a field with the name 'SEG-ID'. The data source will
read all segments, but will parse only ones that have `SEG-ID = "1122334"`.

## <a id="ims"/>Reading hierarchical data sets

Let's imagine we have a multisegment file with 2 segments having parent-child relationships. Each segment has a different
record type. The root record contains company info, addreess and a taxpayer number. The child segment contains a contact
person for a company. Each company can have zero or more contact persons. So each root record can be followed by zero or
more child records.

To load such data in Spark the first thing you need to do is to create a copybook that contains all segment specific fields
in redefined groups. Here is the copybook for our example:

```
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

Because the records have different lengths the 'is_xcom' option should be set to 'true'.

If you load this file as is you will get the schema and the data similar to this.

#### Spark App:
```scala
val df = spark
  .read
  .format("cobol")
  .option("copybook", "/path/to/thecopybook")
  .option("schema_retention_policy", "collapse_root")     // Collapses the root group returning it's field on the top level of the schema
  .option("is_xcom", "true")
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
|     S01L1|2998421316|[ECRONO,123/B Pro...|[ECRONO         1...|
|     S01L1|7888716268|[ABCD Ltd.,74 Law...|[ABCD Ltd.      7...|
|     S01L2|7888716268|[+(782) 772 45 6,...|[+(782) 772 45 69...|
|     S01L1|7929524264|[Roboco Inc.,2 Pa...|[Roboco Inc.    2...|
|     S01L1|2193550322|[Prime Bank,1 Gar...|[Prime Bank     1...|
|     S01L1|5830860727|[ZjkLPj,5574, Tok...|[ZjkLPj         5...|
|     S01L1|4169179307|[Dobry Pivivar,74...|[Dobry Pivivar  7...|
|     S01L2|4169179307|[+(589) 102 29 6,...|[+(589) 102 29 62...|
|     S01L1|4007588657|[ABCD Ltd.,74 Law...|[ABCD Ltd.      7...|
|     S01L2|4007588657|[+(406) 714 80 9,...|[+(406) 714 80 90...|
+----------+----------+--------------------+--------------------+
```

As you can see Cobrix loaded all redefines for every record. Each record contains data from all of the segments. But only
one redefine is valid for every segment. So we need to split the data set into 2 datasets or tables. The distinguisher is
the 'SEGMENT_ID' field. All company details will go into one data sets (segment id = 'S01L1'') while contacts will go in
the second data set (segment id = 'S01L2'). While doing the split we can also collapse the groups so the table won't
contain nested structures. This can be helpful to simplify the analysis of the data.

While doing it you might notice that the taxpayer number field is actually a redefine. Depending on the 'TAXPAYER_TYPE'
either 'TAXPAYER_NUM' or 'TAXPAYER_STR' is used. We can resolve this in our Spark app as well. 

#### Getting the first segment
```scala
import spark.implicits._

val dfCompanies = df
  // Filtering the first segment by segment id
  .filter($"SEGMENT_ID"==="S01L1")
  // Selecting fields that are only available in the first segment
  .select($"COMPANY_ID", $"STATIC_DETAILS.COMPANY_NAME", $"STATIC_DETAILS.ADDRESS",
  // Resolving the taxpayer redefine
    when($"STATIC_DETAILS.TAXPAYER.TAXPAYER_TYPE" === "A", $"STATIC_DETAILS.TAXPAYER.TAXPAYER_STR")
      .otherwise($"STATIC_DETAILS.TAXPAYER.TAXPAYER_NUM").cast(StringType).as("TAXPAYER"))
```

The sesulting table looks like this:
```
dfCompanies.show(10, truncate = false)
+----------+-------------+-------------------------+--------+
|COMPANY_ID|COMPANY_NAME |ADDRESS                  |TAXPAYER|
+----------+-------------+-------------------------+--------+
|2998421316|ECRONO       |123/B Prome str., Denver |40098248|
|7888716268|ABCD Ltd.    |74 Lawn ave., New York   |59017432|
|7929524264|Roboco Inc.  |2 Park ave., Johannesburg|60931086|
|2193550322|Prime Bank   |1 Garden str., London    |37798023|
|5830860727|ZjkLPj       |5574, Tokyo              |17017107|
|4169179307|Dobry Pivivar|74 Staromestka., Prague  |56802354|
|4007588657|ABCD Ltd.    |74 Lawn ave., New York   |15746762|
|9665677039|Prime Bank   |1 Garden str., London    |79830357|
|8766651850|Xingzhoug    |74 Qing ave., Beijing    |40657364|
|4179823966|Johnson & D  |10 Sandton, Johannesburg |37099628|
+----------+-------------+-------------------------+--------+
```

This looks like a valid and clean table containing the list of companies. Now let's do the same for the second segment.

#### Getting the second segment
```scala
    val dfContacts = df
      // Filtering the second segment by segment id
      .filter($"SEGMENT_ID"==="S01L2")
      // Selecting the fields only valid for the second segment
      .select($"COMPANY_ID", $"CONTACTS.CONTACT_PERSON", $"CONTACTS.PHONE_NUMBER")
```

The resulting data loons like this:
```
dfContacts.show(10, truncate = false)
+----------+----------------+----------------+
|COMPANY_ID|CONTACT_PERSON  |PHONE_NUMBER    |
+----------+----------------+----------------+
|7888716268|Lynell Flatt    |+(782) 772 45 69|
|4169179307|Mabelle Bourke  |+(589) 102 29 62|
|4007588657|Lynell Lepe     |+(406) 714 80 90|
|9665677039|Carrie Hisle    |+(115) 514 77 48|
|9665677039|Deshawn Shapiro |+(10) 945 77 74 |
|9665677039|Alona Boehme    |+(43) 922 55 37 |
|9665677039|Cassey Shapiro  |+(434) 242 37 43|
|4179823966|Beatrice Godfrey|+(339) 323 81 40|
|9081730154|Wilbert Winburn |+(139) 236 46 45|
|9081730154|Carrie Godfrey  |+(356) 451 77 64|
+----------+----------------+----------------+
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
  .option("is_xcom", "true")
  .option("segment_field", "SEGMENT_ID")
  .option("segment_id_level0", "S01L1")
  .option("segment_id_level1", "S01L2")
  .load("examples/multisegment_data/COMP.DETAILS.SEP30.DATA.dat")
```

The resulting table will look like this:
```
df.show(10)
+------------------+--------------------+----------+----------+--------------------+--------------------+
|           Seg_Id0|             Seg_Id1|SEGMENT_ID|COMPANY_ID|      STATIC_DETAILS|            CONTACTS|
+------------------+--------------------+----------+----------+--------------------+--------------------+
|20181119145226_0_0|                null|     S01L1|2998421316|[ECRONO,123/B Pro...|[ECRONO         1...|
|20181119145226_0_1|                null|     S01L1|7888716268|[ABCD Ltd.,74 Law...|[ABCD Ltd.      7...|
|20181119145226_0_1|20181119145226_0_...|     S01L2|7888716268|[+(782) 772 45 6,...|[+(782) 772 45 69...|
|20181119145226_0_3|                null|     S01L1|7929524264|[Roboco Inc.,2 Pa...|[Roboco Inc.    2...|
|20181119145226_0_4|                null|     S01L1|2193550322|[Prime Bank,1 Gar...|[Prime Bank     1...|
|20181119145226_0_5|                null|     S01L1|5830860727|[ZjkLPj,5574, Tok...|[ZjkLPj         5...|
|20181119145226_0_6|                null|     S01L1|4169179307|[Dobry Pivivar,74...|[Dobry Pivivar  7...|
|20181119145226_0_6|20181119145226_0_...|     S01L2|4169179307|[+(589) 102 29 6,...|[+(589) 102 29 62...|
|20181119145226_0_8|                null|     S01L1|4007588657|[ABCD Ltd.,74 Law...|[ABCD Ltd.      7...|
|20181119145226_0_8|20181119145226_0_...|     S01L2|4007588657|[+(406) 714 80 9,...|[+(406) 714 80 90...|
+------------------+--------------------+----------+----------+--------------------+--------------------+
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
dependencies provided by 'spark-submit'.

Here is the example output of the joined tables:

##### Segment 1 (Companies)
```
dfCompanies.show(11, truncate = false)
+--------------------+----------+-------------+-------------------------+--------+
|Seg_Id0             |COMPANY_ID|COMPANY_NAME |ADDRESS                  |TAXPAYER|
+--------------------+----------+-------------+-------------------------+--------+
|20181119145346_0_0  |2998421316|ECRONO       |123/B Prome str., Denver |40098248|
|20181119145346_0_1  |7888716268|ABCD Ltd.    |74 Lawn ave., New York   |59017432|
|20181119145346_0_3  |7929524264|Roboco Inc.  |2 Park ave., Johannesburg|60931086|
|20181119145346_0_4  |2193550322|Prime Bank   |1 Garden str., London    |37798023|
|20181119145346_0_5  |5830860727|ZjkLPj       |5574, Tokyo              |17017107|
|20181119145346_0_6  |4169179307|Dobry Pivivar|74 Staromestka., Prague  |56802354|
|20181119145346_0_8  |4007588657|ABCD Ltd.    |74 Lawn ave., New York   |15746762|
|20181119145346_0_10 |9665677039|Prime Bank   |1 Garden str., London    |79830357|
|20181119145346_0_15 |8766651850|Xingzhoug    |74 Qing ave., Beijing    |40657364|
|20181119145346_0_16 |4179823966|Johnson & D  |10 Sandton, Johannesburg |37099628|
|20181119145346_0_18 |9081730154|Pear GMBH.   |107 Labe str., Berlin    |65079222|
+--------------------+----------+-------------+-------------------------+--------+
```

##### Segment 2 (Contacts)
```
dfContacts.show(12, truncate = false)
+-------------------+----------+-------------------+----------------+
|Seg_Id0            |COMPANY_ID|CONTACT_PERSON     |PHONE_NUMBER    |
+-------------------+----------+-------------------+----------------+
|20181119145346_0_1 |7888716268|Lynell Flatt       |+(782) 772 45 69|
|20181119145346_0_6 |4169179307|Mabelle Bourke     |+(589) 102 29 62|
|20181119145346_0_8 |4007588657|Lynell Lepe        |+(406) 714 80 90|
|20181119145346_0_10|9665677039|Carrie Hisle       |+(115) 514 77 48|
|20181119145346_0_10|9665677039|Deshawn Shapiro    |+(10) 945 77 74 |
|20181119145346_0_10|9665677039|Alona Boehme       |+(43) 922 55 37 |
|20181119145346_0_10|9665677039|Cassey Shapiro     |+(434) 242 37 43|
|20181119145346_0_16|4179823966|Beatrice Godfrey   |+(339) 323 81 40|
|20181119145346_0_18|9081730154|Wilbert Winburn    |+(139) 236 46 45|
|20181119145346_0_18|9081730154|Carrie Godfrey     |+(356) 451 77 64|
|20181119145346_0_18|9081730154|Suk Wallingford    |+(57) 570 39 41 |
|20181119145346_0_18|9081730154|Tyesha Debow       |+(258) 914 73 28|
+-------------------+----------+-------------------+----------------+

```

##### Joined datasets

The join statement in Spark:
```scala
val dfJoined = dfCompanies.join(dfContacts, "Seg_Id0")
```

The joined data looks like this:

```
dfJoined.show(12, truncate = false)
+--------------------+----------+-------------+-------------------------+--------+----------+-------------------+----------------+
|Seg_Id0             |COMPANY_ID|COMPANY_NAME |ADDRESS                  |TAXPAYER|COMPANY_ID|CONTACT_PERSON     |PHONE_NUMBER    |
+--------------------+----------+-------------+-------------------------+--------+----------+-------------------+----------------+
|20181119145346_0_1  |7888716268|ABCD Ltd.    |74 Lawn ave., New York   |59017432|7888716268|Lynell Flatt       |+(782) 772 45 69|
|20181119145346_0_10 |9665677039|Prime Bank   |1 Garden str., London    |79830357|9665677039|Carrie Hisle       |+(115) 514 77 48|
|20181119145346_0_10 |9665677039|Prime Bank   |1 Garden str., London    |79830357|9665677039|Cassey Shapiro     |+(434) 242 37 43|
|20181119145346_0_10 |9665677039|Prime Bank   |1 Garden str., London    |79830357|9665677039|Deshawn Shapiro    |+(10) 945 77 74 |
|20181119145346_0_10 |9665677039|Prime Bank   |1 Garden str., London    |79830357|9665677039|Alona Boehme       |+(43) 922 55 37 |
|20181119145346_0_102|2631415894|Pear GMBH.   |107 Labe str., Berlin    |59705976|2631415894|Maya Bourke        |+(311) 260 97 83|
|20181119145346_0_102|2631415894|Pear GMBH.   |107 Labe str., Berlin    |59705976|2631415894|Estelle Godfrey    |+(563) 491 37 73|
|20181119145346_0_105|6650267075|Johnson & D  |10 Sandton, Johannesburg |70174320|6650267075|Maya Boehme        |+(754) 274 56 63|
|20181119145346_0_105|6650267075|Johnson & D  |10 Sandton, Johannesburg |70174320|6650267075|Starr Benally      |+(303) 750 91 27|
|20181119145346_0_105|6650267075|Johnson & D  |10 Sandton, Johannesburg |70174320|6650267075|Wilbert Concannon  |+(344) 192 14 38|
|20181119145346_0_110|9825971915|Beierbauh.   |901 Ztt, Munich          |87008159|9825971915|Tyson Brandis      |+(598) 710 69 45|
|20181119145346_0_110|9825971915|Beierbauh.   |901 Ztt, Munich          |87008159|9825971915|Tyesha Deveau      |+(68) 759 48 98 |
+--------------------+----------+-------------+-------------------------+--------+----------+-------------------+----------------+
```

Again, the full example is available at
`spark-cobol/src/main/scala/za/co/absa/cobrix/spark/cobol/examples/CobolSparkExample2.scala`


## Performance

Tests were performed on a synthetic dataset. The setup and results are as follows.
These results were obtained on fixed size record files. 

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

## Changelog

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
   
