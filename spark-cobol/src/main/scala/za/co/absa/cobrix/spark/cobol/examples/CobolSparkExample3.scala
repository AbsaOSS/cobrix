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
package za.co.absa.cobrix.spark.cobol.examples

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType

// This is an example Spark Job that uses COBOL data source.
// This is a multisegment file reader example similar to CobolSparkExample3, but segment ids are configured to be auto generated
// IMPORTANT! To run this locally change the scope of all Scala and Spark libraries from 'provided' to 'compile' in pom.xml
//            But revert it to 'provided' to create an uber jar for running on a cluster

object CobolSparkExample3 {

  def main(args: Array[String]): Unit = {

    // Making the logs less verbose for this example
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val sparkBuilder = SparkSession.builder().appName("Cobol source reader example 3")
    val spark = sparkBuilder
      .master("local[*]")
      .getOrCreate()

    val copybook =
      """        01  COMPANY-DETAILS.
        |            05  SEGMENT-ID           PIC X(5).
        |            05  COMPANY-ID           PIC X(10).
        |            05  COMPANY.
        |               10  NAME              PIC X(15).
        |               10  ADDRESS           PIC X(25).
        |               10  TAXPAYER.
        |                  15  TAXPAYER-TYPE  PIC X(1).
        |                  15  TAXPAYER-STR   PIC X(8).
        |                  15  TAXPAYER-NUM  REDEFINES TAXPAYER-STR
        |                                     PIC 9(8) COMP.
        |            05  CONTACT REDEFINES COMPANY.
        |               10  PHONE-NUMBER      PIC X(17).
        |               10  CONTACT-PERSON    PIC X(28).
        |""".stripMargin

    // This is an example read a multisegment variable length file from a mainframe.
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      //.option("generate_record_id", true)                   // Generates File_Id and Record_Id fields for line order dependent data
      .option("schema_retention_policy", "collapse_root")     // Collapses the root group returning it's field on the top level of the schema
      .option("is_record_sequence", "true")                   // Specifies that the input file is a sequence of records having RDW headers
      .option("segment_field", "SEGMENT_ID")                  // Specified that segment id field is 'SEGMENT_ID'
      .option("segment_id_level0", "C")                       // If SEGMENT_ID='C' then the segment contains company's info
      .option("segment_id_level1", "P")                       // If SEGMENT_ID='P' then the segment contains contact person's info
      .option("redefine-segment-id: COMPANY", "C,D")
      .option("redefine-segment-id: CONTACT", "P")
      .load("examples/example_data/multisegment_data/COMP.DETAILS.SEP30.DATA.dat")

    df.printSchema
    //println(df.count)
    df.show(10, false)

    import spark.implicits._

    val dfCompanies = df.filter($"SEGMENT_ID"==="C")
      .select($"Seg_Id0", $"COMPANY_ID", $"COMPANY.NAME".as("COMPANY_NAME"), $"COMPANY.ADDRESS",
        when($"COMPANY.TAXPAYER.TAXPAYER_TYPE" === "A", $"COMPANY.TAXPAYER.TAXPAYER_STR")
          .otherwise($"COMPANY.TAXPAYER.TAXPAYER_NUM").cast(StringType).as("TAXPAYER"))

    dfCompanies.printSchema
    //println(df.count)
    dfCompanies.show(50, truncate = false)

    val dfContacts = df.filter($"SEGMENT_ID"==="P")
      .select($"Seg_Id0", $"COMPANY_ID", $"CONTACT.CONTACT_PERSON", $"CONTACT.PHONE_NUMBER")

    dfContacts.printSchema
    //println(df.count)
    dfContacts.show(50, truncate = false)

    val dfJoined = dfCompanies.join(dfContacts, "Seg_Id0")

    dfJoined.printSchema
    //println(df.count)
    dfJoined.orderBy($"Seg_Id0").show(50, truncate = false)
  }

}
