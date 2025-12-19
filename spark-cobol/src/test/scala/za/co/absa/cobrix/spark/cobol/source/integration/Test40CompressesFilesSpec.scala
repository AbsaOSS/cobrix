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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

class Test40CompressesFilesSpec extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private val exampleName = "Test40 (compressed files)"

  private val inputCopybookPath = "file://../data/test40_copybook.cob"
  private val inputCopybookFSPath = "../data/test40_copybook.cob"
  private val expectedSchemaPath = "../data/test40_expected/test40_schema.json"
  private val expectedLayoutPath = "../data/test40_expected/test40_layout.txt"
  private val actualSchemaPath = "../data/test40_expected/test40_schema_actual.json"
  private val actualLayoutPath = "../data/test40_expected/test40_layout_actual.txt"
  private val expectedResultsPath = "../data/test40_expected/test40.txt"
  private val actualResultsPath = "../data/test40_expected/test40_actual.txt"

  def testCompressedFile(inputDataPath: String, useIndexes: Boolean = false): Assertion = {
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

    val options = if (useIndexes) {
      Map(
        "input_split_records" -> "1",
        "enable_index_cache" -> "false",
        "generate_record_id" -> "true"
      )
    } else {
      Map.empty[String, String]
    }

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754")
      .option("strict_sign_overpunching", "true")
      .options(options)
      .option("pedantic", "true")
      .load(inputDataPath)
      .drop("File_Id",  "Record_Id", "Record_Byte_Length")

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

  def testCompressedAsciiFile(options: Map[String, String]): Assertion = {
    val inputDataPath = "../data/test40_data_ascii/ascii.txt.gz"

    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents",
        """
          |      01 RECORD.
          |         05 DATA PIC X(5).
          |""".stripMargin)
      .option("record_format", "D")
      .option("pedantic", "true")
      .options(options)
      .load(inputDataPath)

    assert(df.count == 3)

    val actual = df.orderBy("data")
      .collect()
      .map(a => a.getString(0))
      .mkString(",")

    assert(actual == "12345,67890,A1234")
  }

  def testMixedAsciiFiles(options: Map[String, String]): Assertion = {
    val inputDataPath = "../data/test40_data_ascii/"

    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents",
        """
          |      01 RECORD.
          |         05 DATA PIC X(5).
          |""".stripMargin)
      .option("record_format", "D")
      .option("pedantic", "true")
      .options(options)
      .load(inputDataPath)

    assert(df.count == 6)

    val actual = df.orderBy("data")
      .collect()
      .map(a => a.getString(0))
      .mkString(",")

    assert(actual == "12345,12345,67890,67890,A1234,A1234")
  }

  test("Test compressed EBCDIC gzip file without indexes") {
    testCompressedFile("../data/test40_data/example.dat.gz")
  }

  test("Test compressed EBCDIC  bzip2 file without indexes") {
    testCompressedFile("../data/test40_data/example.dat.bz2")
  }

  test("Test compressed EBCDIC gzip file with indexes") {
    testCompressedFile("../data/test40_data/example.dat.gz", useIndexes = true)
  }

  test("Test compressed EBCDIC bzip2 file with indexes") {
    testCompressedFile("../data/test40_data/example.dat.bz2", useIndexes = true)
  }

  test("read mixed compressed EBCDIC files") {
    val inputDataPath = "../data/test40_data"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754")
      .option("strict_sign_overpunching", "true")
      .option("generate_record_id", "true")
      .option("enable_index_cache", "false")
      .option("pedantic", "true")
      .load(inputDataPath)

    assert(df.count == 300)
  }

  test("read mixed compressed EBCDIC files and file_end_offset") {
    val inputDataPath = "../data/test40_data"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754")
      .option("strict_sign_overpunching", "true")
      .option("file_end_offset", 1493)
      .option("enable_index_cache", "false")
      .option("pedantic", "true")
      .load(inputDataPath)

    assert(df.count == 297)
  }

  test("read a compressed ASCII file 1") {
    testCompressedAsciiFile(Map(
      "record_format" -> "D"
    ))
  }

  test("read a compressed ASCII file 2") {
    testCompressedAsciiFile(Map(
      "record_format" -> "D",
      "ascii_charset" -> "ISO-8859-1"
    ))
  }

  test("read a compressed ASCII file 3") {
    testCompressedAsciiFile(Map(
      "record_format" -> "D2"
    ))
  }

  test("read a mixed ASCII files 1") {
    testMixedAsciiFiles(Map(
      "record_format" -> "D"
    ))
  }

  test("read a mixed ASCII files 2") {
    testMixedAsciiFiles(Map(
      "record_format" -> "D",
      "ascii_charset" -> "ISO-8859-1"
    ))
  }

  test("read a mixed ASCII files 3") {
    testMixedAsciiFiles(Map(
      "record_format" -> "D2"
    ))
  }
}


