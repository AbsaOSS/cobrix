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
import za.co.absa.cobrix.spark.cobol.source.utils.Test10CustomRDWParser
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

import scala.collection.JavaConverters._

//noinspection NameBooleanParameters
class Test11CustomRDWParser extends FunSuite with SparkTestBase {

  private val exampleName = "Test10(custom record header parser)"
  private val inputCopybookPath = "file://../data/test11_copybook.cob"
  private val inputCopybookFSPath = "../data/test11_copybook.cob"
  private val inputDataPath = "../data/test11_data"

  test(s"Integration test on $exampleName - segment ids, ebcdic") {

    val expectedLayoutPath = "../data/test11_expected/test11_layout.txt"
    val actualLayoutPath = "../data/test11_expected/test11_layout_actual.txt"
    val expectedSchemaPath = "../data/test11_expected/test11_schema.json"
    val actualSchemaPath = "../data/test11_expected/test11_schema_actual.json"
    val expectedResultsPath = "../data/test11_expected/test11.txt"
    val actualResultsPath = "../data/test11_expected/test11_actual.txt"
    Test10CustomRDWParser.additionalInfo = ""

    val copybookContents = Files.readAllLines(Paths.get(inputCopybookFSPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val cobolSchema = CopybookParser.parseTree(copybookContents)
    val actualLayout = cobolSchema.generateRecordLayoutPositions()
    val expectedLayout = Files.readAllLines(Paths.get(expectedLayoutPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    if (actualLayout != expectedLayout) {
      FileUtils.writeStringToFile(actualLayout, actualLayoutPath)
      assert(false, s"The actual layout doesn't match what is expected for $exampleName example. Please compare contents of $expectedLayoutPath to " +
        s"$actualLayoutPath for details.")
    }

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_record_sequence", "true")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .option("record_header_parser", "za.co.absa.cobrix.spark.cobol.source.utils.Test10CustomRDWParser")
      .option("rhp_additional_info", "rhp info")
      .load(inputDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    FileUtils.writeStringsToFile(actualDf, actualResultsPath)

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))

    assert(Test10CustomRDWParser.additionalInfo == "rhp info")
  }

}
