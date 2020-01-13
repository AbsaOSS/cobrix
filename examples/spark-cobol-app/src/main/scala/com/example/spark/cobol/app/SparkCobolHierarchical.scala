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
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

/**
  * This is an example Spark/Cobol Application.
  *
  * To run this locally use `mvn test`
  * or change Scala and Spark dependencies from `provided` to `compile` in pom.xml
  *
  * To run this on cluster generate the uber jar by running
  * `mvn package` and use `spark-submit` on cluster.
  */
object SparkCobolHierarchical {

  /**
    * <h2>This is an example Cobrix Spark Application.</h2>
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
    * We instruct Cobrix to restore hierarchical structure of the records by providing
    * <ul>
    * <li>A field that contains a segment id. </ul>
    * <li>A mapping from segment redefine field names to segment ids. </ul>
    * <li>A parent-child relationships between segment redefines by specifying children for each segment. </ul>
    * </ul>
    * This is done via options to Spark reader:
    * <pre>
    * .option("segment_field", "SEGMENT_ID")
    * .option("redefine_segment_id_map:1", "STATIC-DETAILS => C")
    * .option("redefine-segment-id-map:2", "CONTACTS => P")
    * .option("segment-children:1", "STATIC-DETAILS => CONTACTS")
    * </pre>
    *
    * Each record of the resulting dataframe correspond to a root segment. Child segments
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
      .appName("Spark-Cobol Hierarchical record reader example")
      .getOrCreate()

    val df = spark
      .read
      .format("cobol")                             // Alternatively can use "za.co.absa.cobrix.spark.cobol.source"
      .option("copybook_contents", copybook)               // A copybook can be provided inline
      .option("is_record_sequence", "true")                // This file is a sequence of records with 4 byte record headers
      //.option("copybook", "../example_data/companies_copybook.cpy") // Or as a path name in HDFS. For local filesystem use file:// prefix
      //.option("generate_record_id", true)                // Generates File_Id and Record_Id fields for line order dependent data
      .option("schema_retention_policy", "collapse_root")  // Collapses the root group returning it's field on the top level of the schema
      .option("segment_field", "SEGMENT_ID")               // The SEGMENT_ID field contains IDs of segments

      // Specifies a mapping between segment IDs and corresponding REDEFINE fields
      .option("redefine_segment_id_map:1", "STATIC-DETAILS => C")
      .option("redefine-segment-id-map:2", "CONTACTS => P")

      // Specifies the parent-child relationship between segments
      .option("segment-children:1", "STATIC-DETAILS => CONTACTS")

      .load("../example_data/companies_data")                   // Location of data file(s)

    df.printSchema()

    df.show(10, truncate = false)

    val s = df.toJSON.take(5).mkString("[", ", ", "]")
    println(SparkUtils.prettyJSON(s))
  }

}
