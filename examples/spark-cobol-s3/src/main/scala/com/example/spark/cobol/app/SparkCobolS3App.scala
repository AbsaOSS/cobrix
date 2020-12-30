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
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

/**
  * This is an example Spark/Cobol Application reading data from S3.
  *
  * To run this locally, uncomment master options and
  * run in an IDE enabling 'include dependencies with "Provided" scope'.
  *
  * To run this on cluster generate the uber jar by running
  * `mvn package` and use `spark-submit` on cluster.
  */
object SparkCobolS3App {

  def main(args: Array[String]): Unit = {

    // This is the copybook for the data file.
    // Alternatively, you can,
    // - put the file locally and use "file:///path/to/copybook"
    // - put the file to S3 and use "s3a://bucket/path/to/copybook"
    val copybook =
      """        01  RECORD.
        |           05  ID                        PIC S9(4)  COMP.
        |           05  COMPANY.
        |               10  SHORT-NAME            PIC X(10).
        |               10  COMPANY-ID-NUM        PIC 9(5) COMP-3.
        |               10  COMPANY-ID-STR
        |			         REDEFINES  COMPANY-ID-NUM PIC X(3).
        |           05  METADATA.
        |               10  CLIENTID              PIC X(15).
        |               10  REGISTRATION-NUM      PIC X(10).
        |               10  NUMBER-OF-ACCTS       PIC 9(03) COMP-3.
        |               10  ACCOUNT.
        |                   12  ACCOUNT-DETAIL    OCCURS 80
        |                                         DEPENDING ON NUMBER-OF-ACCTS.
        |                      15  ACCOUNT-NUMBER     PIC X(24).
        |                      15  ACCOUNT-TYPE-N     PIC 9(5) COMP-3.
        |                      15  ACCOUNT-TYPE-X     REDEFINES
        |                           ACCOUNT-TYPE-N  PIC X(3).
        |""".stripMargin

    // Switch logging level to WARN
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val spark = SparkSession
      .builder()
      .appName("Spark-Cobol read from S3 example")
      // Uncomment the following to run in local mode from an IDE.
//      .master("local[2]")
//      .config("spark.ui.enabled", "false")
//      .config("spark.driver.bindAddress","127.0.0.1")
//      .config("spark.driver.host", "127.0.0.1")
      .getOrCreate()

    // Initialize S3A connector
    // Usually this step is not needed since S3 credentials and endpoints are configured in core-site.xml
    setupS3Access(spark)

    val df = spark.read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("schema_retention_policy", "collapse_root")
      .load("s3a://bucket/path/file.bin")

    df.printSchema()

    df.show

    val json = SparkUtils.prettyJSON(df.toJSON.take(3).mkString("[", ",", "]"))
    println(json)
  }

  def setupS3Access(spark: SparkSession): Unit = {
    val sc = spark.sparkContext
    sc.hadoopConfiguration.set("fs.file.impl", classOf[org.apache.hadoop.fs.LocalFileSystem].getName)
    sc.hadoopConfiguration.set("fs.s3a.impl", classOf[org.apache.hadoop.fs.s3a.S3AFileSystem].getName)

    // Put your AWS credentials here. Good practice is to get these credentials from environment variables.
    sc.hadoopConfiguration.set("fs.s3a.access.key", "")
    sc.hadoopConfiguration.set("fs.s3a.secret.key", "")

    // Optionally specify the region and endpoint
    //sc.hadoopConfiguration.set("fs.s3a.endpoint", "")
    //sc.hadoopConfiguration.set("fs.s3a.region", "")
  }

}
