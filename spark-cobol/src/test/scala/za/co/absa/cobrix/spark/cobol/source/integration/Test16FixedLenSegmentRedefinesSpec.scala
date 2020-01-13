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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

//noinspection NameBooleanParameters
class Test16FixedLenSegmentRedefinesSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test16"
  private val inputCopybookPath = "../data/test16_fix_len_segments.cob"
  private val inputDataPath = "../data/test16_data"

  private val expectedSchemaPath = "../data/test16_expected/test16_schema.json"
  private val actualSchemaPath = "../data/test16_expected/test16_schema_actual.json"
  private val expectedResultsPath = "../data/test16_expected/test16.txt"
  private val actualResultsPath = "../data/test16_expected/test16_actual.txt"

  test(s"Test layout created from $exampleName data") {
    val expectedLayoutPath = "../data/test16_expected/test16_layout.txt"
    val actualLayoutPath = "../data/test16_expected/test16_layout_actual.txt"

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

  test(s"Test schema created from $exampleName data") {
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybookContents)
      .option("schema_retention_policy", "collapse_root")
      .load(inputDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. " +
        s"Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }
  }


  test(s"Test dataframe contents created from $exampleName data") {
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybookContents)
      .option("schema_retention_policy", "collapse_root")
      .option("segment_field", "SEGMENT_ID")
      .option("redefine_segment_id_map:0", "COMPANY => C")
      .option("redefine-segment-id-map:1", "PERSON => P")
      .option("redefine-segment-id-map:2", "PO-BOX => B")

      .load(inputDataPath)

    val actual = df
      .toJSON
      .take(50)
      .map(SparkUtils.prettyJSON)
      .mkString("\n")
      .replace("\r\n", "\n")

    val expected = Files
      .readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1)
      .toArray
      .mkString("\n")
      .replace("\r\n", "\n")

    if (actual != expected) {
      FileUtils.writeStringToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. " +
        s"Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }

}
