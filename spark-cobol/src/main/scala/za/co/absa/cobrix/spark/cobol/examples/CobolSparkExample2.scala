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
import org.apache.log4j.{Level, Logger}

// This is an example Spark Job that uses COBOL data source.
// IMPORTANT! To run this locally change the scope of all Scala and Spark libraries from 'provided' to 'compile' in pom.xml
//            But revert it to 'provided' to create an uber jar for running on a cluster

object CobolSparkExample2 {

  def main(args: Array[String]): Unit = {

    // Making the logs less verbose for this example
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val sparkBuilder = SparkSession.builder().appName("Cobol source reader example 2")
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
      .option("schema_retention_policy", "collapse_root")     // Collapses the root group returning it's field on the top level of the schema
      .option("is_record_sequence", "true")                   // Specifies that the input file is a sequence of records having RDW headers
      .load("examples/example_data/multisegment_data/COMP.DETAILS.SEP30.DATA.dat")

    import spark.implicits._

    df.printSchema
    //println(df.count)
    df.orderBy($"COMPANY_ID").show(10, false)


    val dfCompanies = df.filter($"SEGMENT_ID"==="C")
      .select($"COMPANY_ID", $"COMPANY.NAME".as("COMPANY_NAME"), $"COMPANY.ADDRESS",
        when($"COMPANY.TAXPAYER.TAXPAYER_TYPE" === "A", $"COMPANY.TAXPAYER.TAXPAYER_STR")
          .otherwise($"COMPANY.TAXPAYER.TAXPAYER_NUM").cast(StringType).as("TAXPAYER"))

    dfCompanies.printSchema
    //println(df.count)
    dfCompanies.show(50, truncate = false)

    val dfContacts = df.filter($"SEGMENT_ID"==="P")
      .select($"COMPANY_ID", $"CONTACT.CONTACT_PERSON", $"CONTACT.PHONE_NUMBER")

    dfContacts.printSchema
    //println(df.count)
    dfContacts.show(50, truncate = false)

    val dfJoined = dfCompanies.join(dfContacts, "COMPANY_ID")

    dfJoined.printSchema
    //println(df.count)
    dfJoined.orderBy($"COMPANY_ID").show(50, truncate = false)
  }

}
