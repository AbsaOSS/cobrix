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

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType

// This is an example Spark Job that uses COBOL data source.
// IMPORTANT! To run this locally change the scope of all Scala and Spark libraries from 'provided' to 'compile' in pom.xml
//            But revert it to 'provided' to create an uber jar for running on a cluster

object CobolSparkExample2 {

  def main(args: Array[String]): Unit = {

    val sparkBuilder = SparkSession.builder().appName("Cobol source reader example 2")
    val spark = sparkBuilder
      .master("local[*]")
      .getOrCreate()

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

    // This is an example read a multisegment variable length file from a mainframe.
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      //.option("generate_record_id", true)                   // Generates File_Id and Record_Id fields for line order dependent data
      .option("schema_retention_policy", "collapse_root")     // Collapses the root group returning it's field on the top level of the schema
      .option("is_xcom", "true")
      .load("examples/multisegment_data/COMP.DETAILS.SEP30.DATA.dat")

    import spark.implicits._

    val df2 = df.filter($"SEGMENT_ID"==="S01L1")
      .select($"COMPANY_ID", $"STATIC_DETAILS.COMPANY_NAME", $"STATIC_DETAILS.ADDRESS",
        when($"STATIC_DETAILS.TAXPAYER.TAXPAYER_TYPE" === "A", $"STATIC_DETAILS.TAXPAYER.TAXPAYER_STR")
          .otherwise($"STATIC_DETAILS.TAXPAYER.TAXPAYER_NUM").cast(StringType).as("TAXPAYER"))

    df2.printSchema
    //println(df.count)
    df2.show(50, truncate = false)

    val df3 = df.filter($"SEGMENT_ID"==="S01L2")
      .select($"COMPANY_ID", $"CONTACTS.CONTACT_PERSON", $"CONTACTS.PHONE_NUMBER")

    df3.printSchema
    //println(df.count)
    df3.show(50, truncate = false)

    val df4 = df2.join(df3, df2("COMPANY_ID") === df3("COMPANY_ID"))

    df4.printSchema
    //println(df.count)
    df4.show(20, truncate = false)
  }

}
