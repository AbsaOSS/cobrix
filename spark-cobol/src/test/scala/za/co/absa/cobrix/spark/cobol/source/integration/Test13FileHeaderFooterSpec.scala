/*
 * Copyright 2018-2019 ABSA Group Limited
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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

//noinspection NameBooleanParameters
class Test13FileHeaderFooterSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test13"
  private val inputCopybookPath = "../data/test13_file_header_footer.cob"
  private val inpudDataPath = "../data/test13_data"

  private val expectedSchemaPath = "../data/test13_expected/test13_schema.json"
  private val actualSchemaPath = "../data/test13_expected/test13_schema_actual.json"
  private val expectedResultsPath = "../data/test13_expected/test13.txt"
  private val actualResultsPath = "../data/test13_expected/test13_actual.txt"

  test(s"Test layout created from $exampleName data") {
    val expectedLayoutPath = "../data/test13_expected/test13_layout.txt"
    val actualLayoutPath = "../data/test13_expected/test13_layout_actual.txt"

    // Comparing layout
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val cobolSchema = CopybookParser.parseTree(copybookContents)
    val actualLayout = cobolSchema.generateRecordLayoutPositions()
    val expectedLayout = Files.readAllLines(Paths.get(expectedLayoutPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    if (actualLayout != expectedLayout) {
      FileUtils.writeStringToFile(actualLayout, actualLayoutPath)
      assert(false, s"The actual layout doesn't match what is expected for $exampleName example. " +
        s"Please compare contents of $expectedLayoutPath to " +
        s"$actualLayoutPath for details.")
    }
  }

  test("Test failure if file offset options are not specified") {
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybookContents)
      .option("schema_retention_policy", "collapse_root")
      .load(inpudDataPath)

    val exception = intercept[IllegalArgumentException] {
      df.count
    }

    assert(exception.getMessage.contains("NOT DIVISIBLE by the RECORD SIZE"))
  }

  /*
  test(s"Test dataframe created from $exampleName data") {
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybookContents)
      .option("schema_retention_policy", "collapse_root")
      .option("file_start_offset", 10)
      .option("file_end_offset", 12)
      .load(inpudDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. " +
        s"Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. " +
        s"Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }
  */

}
