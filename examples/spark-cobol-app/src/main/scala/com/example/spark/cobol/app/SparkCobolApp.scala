/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spark.cobol.app

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType

/**
  * This is an example Spark/Cobol Application.
  *
  * To run this locally use `mvn test`
  * or change Scala and Spark dependencies from `provided` to `compile` in pom.xml
  *
  * To run this on cluster generate the uber jar by running
  * `mvn package` and use `spark-submit` on cluster.
  */
object SparkCobolApp {

  /**
    * <h2>This is an example Cobrix Spark Job.</h2>
    * The data file is a variable length multisegment record sequence extracted from a hierarchical database.
    * It consists of a common part (SEGMENT-ID, COMPANY-ID) and 2 segments:
    * <ul><li>Company details: name and address</li>
    *     <li>Contact person for the company</li></ul>
    * A company can have no contact people or can have several of them.
    *
    * <pre>COMPANY
    * +- CONTACTS</pre>
    *
    * Each segment is a GROUP in the copybook. STATIC-DETAILS and CONTACTS are groups that correspond
    * to record segments. Each segment REDEFINES previous segment. So CONTACTS REDEFINE STATIC-DETAILS.
    * For each record only one segment is valid. We use SEGMENT-ID field to distinguish which segment
    * a record belongs to.
    *
    * We instruct Cobrix to generate additional fields by specifying the following option:
    * <pre>
    * .option("segment_field", "SEGMENT_ID")
    * .option("segment_id_level0", "C")
    * .option("segment_id_prefix", "ID")
    * </pre>
    * The option says that root (level 0) segment has SEGMEIT_ID="C". For each record of the file
    * Cobrix should generate a field with the predefined name 'Seg_Id0' containing an autogenerated
    * ID that is unique as long as 'segment_id_prefix' is unique. If 'segment_id_prefix' is not specified
    * Cobrix will use a timestamp of copybook loading as an id prefix. So autogenerated ids will be unique across
    * each individual run of the spark job.
    *
    * The generated Seg_Id0 field will contain a generated unique root segment id for each record.
    *
    * We extract companies segment first as a data frame. Then we extract contacts segment as a second
    * data frame. Then we join both segments by Seg_Id0 to get all the companies and corresponding contacts.
    */
  def main(args: Array[String]): Unit = {
    // This is the copybook for the data file.
    val copybook =
      """        01  COMPANY-DETAILS.
        |            05  SEGMENT-ID           PIC X(5).
        |            05  COMPANY-ID           PIC X(10).
        |            05  STATIC-DETAILS.
        |               10  COMPANY-NAME      PIC X(15).
        |               10  ADDRESS           PIC X(25).
        |               10  TAXPAYER.
        |                  15  TAXPAYER-TYPE  PIC X(1).
        |                  15  TAXPAYER-STR   PIC X(8).
        |                  15  TAXPAYER-NUM  REDEFINES TAXPAYER-STR
        |                                     PIC 9(8) COMP.
        |
        |            05  CONTACTS REDEFINES STATIC-DETAILS.
        |               10  PHONE-NUMBER      PIC X(17).
        |               10  CONTACT-PERSON    PIC X(28).
        |""".stripMargin

    // Switch logging level to WARN
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val spark = SparkSession
      .builder()
      .appName("Spark-Cobol Companies example")
// Uncomment the following to run in local mode from an IDE.
//      .master("local[2]")
//      .config("spark.ui.enabled", "false")
//      .config("spark.driver.bindAddress","127.0.0.1")
//      .config("spark.driver.host", "127.0.0.1")
      .getOrCreate()

    import spark.implicits._

    val df = spark
      .read
      .format("cobol")                              // Alternatively can use "za.co.absa.cobrix.spark.cobol.source"
      .option("copybook_contents", copybook)               // A copybook can be provided inline
      .option("is_record_sequence", "true")                // This file is a sequence of records with 4 byte record headers
      //.option("copybook", "data/companies_copybook.cpy") // Or as a path name in Hadoop(HDFS/S3, etc). For local filesystem use file:// prefix
      //.option("generate_record_id", true)                // Generates File_Id and Record_Id fields for line order dependent data
      .option("schema_retention_policy", "collapse_root")  // Collapses the root group returning it's field on the top level of the schema
      .option("segment_field", "SEGMENT_ID")               // The SEGMENT_ID field contains IDs of segments
      .option("segment_id_level0", "C")                    // Root segment is equals "C" [company]. All other segments are it's childern
      .option("segment_id_prefix", "ID")                   // This prefix will be added to each record autogenerated ID
      .load("../example_data/companies_data")        // Location of data file(s)

    df.printSchema()

    // Extracting the first segment which contains company details such as address and tax payer id
    // Selecting only fields from that copybook that are valid for this segment.
    val dfCompanies = df.filter($"SEGMENT_ID" === "C")
      .select($"Seg_Id0", $"COMPANY_ID", $"STATIC_DETAILS.COMPANY_NAME", $"STATIC_DETAILS.ADDRESS",
        when($"STATIC_DETAILS.TAXPAYER.TAXPAYER_TYPE" === "A", $"STATIC_DETAILS.TAXPAYER.TAXPAYER_STR")
          .otherwise($"STATIC_DETAILS.TAXPAYER.TAXPAYER_NUM").cast(StringType).as("TAXPAYER"))

    dfCompanies.printSchema
    //println(df.count)
    dfCompanies.show(50, truncate = false)

    // Extracting the second segment which contains contcat people list
    // Selecting only fields from that copybook that are valid for this segment.
    val dfContacts = df.filter($"SEGMENT_ID" === "P")
      .select($"Seg_Id0", $"COMPANY_ID", $"CONTACTS.CONTACT_PERSON", $"CONTACTS.PHONE_NUMBER")

    dfContacts.printSchema
    //println(df.count)
    dfContacts.show(50, truncate = false)

    // The Seg_Id0 field is an autogenerated field that contains root segment id for each record
    // By joining company details dataframe with the contacts dataframe on Seg_Id0 we restore hierarchical relationships
    // that exists between companies and contacts. That is each company may have zero or more contacts.
    // After the join we get the full table containing company details and contacts together.
    // When converting from a hierarchical database it is common to extract each segment as a separate table, export it
    // into Parquet file or a relational database. The joins such as this are ususlly performed in a se[arate analisycal
    // jobs on these converted tables.
    val dfJoined = dfCompanies.join(dfContacts, "Seg_Id0")

    dfJoined.printSchema
    //println(df.count)
    dfJoined.orderBy($"Seg_Id0").show(50, truncate = false)
  }

}
