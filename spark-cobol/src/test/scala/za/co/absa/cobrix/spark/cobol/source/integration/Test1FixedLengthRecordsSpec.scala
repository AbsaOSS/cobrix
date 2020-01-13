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

package za.co.absa.cobrix.spark.cobol.source.integration

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

//noinspection NameBooleanParameters
class Test1FixedLengthRecordsSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test1"
  private val inputCopybookPath = "file://../data/test1_copybook.cob"
  private val inpudDataPath = "../data/test1_data"

  private val expectedSchemaPath = "../data/test1_expected/test1_schema.json"
  private val actualSchemaPath = "../data/test1_expected/test1_schema_actual.json"
  private val expectedResultsPath = "../data/test1_expected/test1.txt"
  private val actualResultsPath = "../data/test1_expected/test1_actual.txt"

  test(s"Integration test on $exampleName data") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .load(inpudDataPath)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }

  test(s"Test failure on an invalid copybook") {
    val copybook =
      """        01  COMPANY-DETAILS.
        |            05  SEGMENT-ID           PIC X(5).
        |            05  COMPANY-ID           PIC X(11).
        |""".stripMargin

    val df1 = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("schema_retention_policy", "collapse_root")
      .load(inpudDataPath)

    val exception = intercept[IllegalArgumentException]{
      df1.take(60).foreach(_ => true)
    }
    assert(exception.getMessage.contains("NOT DIVISIBLE by the RECORD SIZE"))
  }

  test(s"Test success on an invalid copybook with debug override") {
    val copybook =
      """        01  COMPANY-DETAILS.
        |            05  SEGMENT-ID           PIC X(5).
        |            05  COMPANY-ID           PIC X(11).
        |""".stripMargin

    val df1 = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("schema_retention_policy", "collapse_root")
      .option("debug_ignore_file_size", "true")
      .load(inpudDataPath)

    df1.take(60).foreach(_ => true)
  }

  test(s"Test failure on unrecognized options (pedantic mode on)") {
    val copybook =
      """        01  COMPANY-DETAILS.
        |            05  SEGMENT-ID           PIC X(5).
        |            05  COMPANY-ID           PIC X(11).
        |""".stripMargin

    val exception = intercept[IllegalArgumentException] {
      val df1 = spark
        .read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("schema_retention_policy", "collapse_root")
        .option("dummy", "unknown")
        .option("pedantic", "true")
        .load(inpudDataPath)
    }
    assert(exception.getMessage.contains("Redundant or unrecognized option(s) to 'spark-cobol': dummy."))
  }

}
