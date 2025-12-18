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

import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

class Test40CompressesFilesSpec extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val exampleName = "Test40 (compressed files)"

  private val inputCopybookPath = "file://../data/test40_copybook.cob"
  private val inputCopybookFSPath = "../data/test40_copybook.cob"
  private val expectedSchemaPath = "../data/test40_expected/test40_schema.json"
  private val expectedLayoutPath = "../data/test40_expected/test40_layout.txt"
  private val actualSchemaPath = "../data/test40_expected/test40_schema_actual.json"
  private val actualLayoutPath = "../data/test40_expected/test40_layout_actual.txt"
  private val expectedResultsPath = "../data/test40_expected/test40.txt"
  private val actualResultsPath = "../data/test40_expected/test40_actual.txt"

  def testCompressedFile(inputDataPath: String): Assertion = {
    // Comparing layout
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookFSPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val cobolSchema = CopybookParser.parseTree(copybookContents, debugFieldsPolicy = DebugFieldsPolicy.HexValue)
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
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754")
      .option("strict_sign_overpunching", "true")
      .option("pedantic", "true")
      .load(inputDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    // Fill nulls with zeros so by looking at json you can tell a field is missing. Otherwise json won't contain null fields.
    val actualDf = df.orderBy("ID").na.fill(0).toJSON.take(20)
    FileUtils.writeStringsToFile(actualDf, actualResultsPath)
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))

    succeed
  }

  test("Test gzip") {
    testCompressedFile("../data/test40_data/example.dat.gz")
  }

  test("Test bzip2") {
    testCompressedFile("../data/test40_data/example.dat.bz2")
  }

  test("read mixed compressed files") {
    val inputDataPath = "../data/test40_data"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754")
      .option("strict_sign_overpunching", "true")
      .option("pedantic", "true")
      .load(inputDataPath)

    assert(df.count == 300)
  }
}
