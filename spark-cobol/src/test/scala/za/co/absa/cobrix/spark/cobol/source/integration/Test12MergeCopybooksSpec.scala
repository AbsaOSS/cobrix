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

class Test12MergeCopybooksSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test12"
  private val inputCopybookPath = "file://../data/test12_copybook.cob"
  private val inputCopybookPathA = "file://../data/test12_copybook_a.cob"
  private val inputCopybookPathB = "file://../data/test12_copybook_b.cob"
  private val inputDataPath = "../data/test12_data"

  private val expectedSchemaPath = "../data/test12_expected/test12_schema.json"
  private val actualSchemaPath = "../data/test12_expected/test12_schema_actual.json"
  private val actualSchemaPathM = "../data/test12_expected/test12_schema_actual_merged.json"
  private val expectedResultsPath = "../data/test12_expected/test12.txt"
  private val actualResultsPath = "../data/test12_expected/test12_actual.txt"
  private val actualResultsPathM = "../data/test12_expected/test12_actual_merged.txt"

  test(s"Integration test on $exampleName data") {
    val df = spark
      .read
      .format("cobol")
      .option("copybooks", inputCopybookPath)
      .option("encoding", "ascii")
      .load(inputDataPath)

    val dfM = spark
      .read
      .format("cobol")
      .option("copybooks", s"$inputCopybookPathA,$inputCopybookPathB")
      .option("encoding", "ascii")
      .load(inputDataPath)

    // This is to print the actual output
    //    println(df.schema.json)
    //    df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json
    val actualSchemaM = dfM.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    if (actualSchema != actualSchemaM) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPathM)
      assert(false, s"The actual merged schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPathM for details.")
    }

    val actual = df.toJSON.take(60)
    val actualM = dfM.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }

    if (!actual.sameElements(actualM)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual merged data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPathM for details.")
    }
  }

}
