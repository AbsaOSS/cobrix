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

object SparkCodecApp {

  /**
    * This example demonstrates how a custom record headers parser can be used to read a variable record length file having
    * non-standard RDW headers. In this example RDH header is 5 bytes instead of 4.
    *
    * The custom record header parser used here is defined in [[CustomRecordHeadersParser]] class.
    */
  def main(args: Array[String]): Unit = {
    // Switch logging level to WARN
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val spark = SparkSession
      .builder()
      .appName("Spark-Cobol Custom header parser example")
      .getOrCreate()

    val df = spark
      .read
      .format("cobol")
      .option("copybook", "../example_data/copybook_codec.cob")
      .option("is_record_sequence", "true")
      .option("generate_record_id", true)
      .option("schema_retention_policy", "collapse_root")
      .option("record_header_parser", "com.example.spark.cobol.app.CustomRecordHeadersParser") // Custom record header parser class
      .load("../example_data/data_codec/example.dat")

    df.printSchema()

    df.show
    df.count
  }

}
