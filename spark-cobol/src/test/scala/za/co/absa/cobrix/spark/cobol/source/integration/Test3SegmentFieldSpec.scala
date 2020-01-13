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

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

// This test suite will be deprecated soon since search reader is deprecated

//noinspection NameBooleanParameters
class Test3SegmentFieldSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test3"
  private val inputCopybookPath = "file://../data/test3_copybook.cob"
  private val inputDataPath = "../data/test3_data"

  private val expectedSchemaPath = "../data/test3_expected/test3_schema.json"
  private val actualSchemaPath = "../data/test3_expected/test3_schema_actual.json"

  def runTest(expectPrefix: String, options: Seq[(String, String)]): Unit = {
    val expectedResultsPath = s"../data/test3_expected/test3$expectPrefix.txt"
    val actualResultsPath = s"../data/test3_expected/test3${expectPrefix}_actual.txt"

    val df = {
      val loadDf = spark
        .read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("schema_retention_policy", "collapse_root")

      val loadDfWithOptions = options.foldLeft(loadDf)((a, b) => {
        a.option(b._1, b._2)
      })

      loadDfWithOptions.load(inputDataPath)
    }

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

  test(s"Integration test on $exampleName data") {
    runTest("",
      "segment_field" -> "SIGNATURE" ::
      "segment_filter" -> "S9276511" ::
        Nil)
  }

  test(s"Test trimming = none on $exampleName data") {
    runTest("_trim_none",
      "segment_field" -> "SIGNATURE" ::
      "segment_filter" -> "S9276511" ::
      "string_trimming_policy" -> "none" ::
        Nil)
  }

  test(s"Test trimming = left on $exampleName data") {
    runTest("_trim_left",
      "segment_field" -> "SIGNATURE" ::
        "segment_filter" -> "S9276511" ::
        "string_trimming_policy" -> "left" ::
        Nil)
  }

  test(s"Test trimming = right on $exampleName data") {
    runTest("_trim_right",
      "segment_field" -> "SIGNATURE" ::
        "segment_filter" -> "S9276511" ::
        "string_trimming_policy" -> "right" ::
        Nil)
  }

  test(s"Test trimming = both on $exampleName data") {
    runTest("_trim_both",
      "segment_field" -> "SIGNATURE" ::
        "segment_filter" -> "S9276511" ::
        "string_trimming_policy" -> "both" ::
        Nil)
  }

}
