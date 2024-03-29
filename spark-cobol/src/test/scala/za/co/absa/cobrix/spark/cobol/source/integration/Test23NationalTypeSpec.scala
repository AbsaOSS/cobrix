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

import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test23NationalTypeSpec extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val copybook =
    """      01 RECORD.
          02 X PIC X(3).
          02 N PIC N(3).
    """

  val binFileContentsBE: Array[Byte] = Array[Byte](
    // Record 0
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, // "123"
    0x00, 0x31.toByte, 0x00, 0x32.toByte, 0x00, 0x33.toByte, // "123" in UTF-16 BE
    // Record 1
    0x81.toByte, 0x82.toByte, 0x83.toByte, // "abc"
    0x00, 0x61.toByte, 0x00, 0x62.toByte, 0x00, 0x63.toByte // "abc" in UTF-16 BE
  )

  val binFileContentsLE: Array[Byte] = Array[Byte](
    // Record 0
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, // "123"
    0x31.toByte, 0x00, 0x32.toByte, 0x00, 0x33.toByte, 0x00, // "123" in UTF-16 LE
    // Record 1
    0x81.toByte, 0x82.toByte, 0x83.toByte, // "abc"
    0x61.toByte, 0x00, 0x62.toByte, 0x00, 0x63.toByte, 0x00 // "abc" in UTF-16 LE
  )

  val expectedData =
    """[ {
      |  "X" : "123",
      |  "N" : "123"
      |}, {
      |  "X" : "abc",
      |  "N" : "abc"
      |} ]""".stripMargin.replace("\r\n", "\n")

  test("Test that international strings have proper sizes") {
    val parsedCopybook = CopybookParser.parseTree(copybook)

    assert(parsedCopybook.ast.children.head.isInstanceOf[Group])
    val record = parsedCopybook.ast.children.head.asInstanceOf[Group]

    assert(record.children.head.binaryProperties.actualSize == 3)
    assert(record.children(1).binaryProperties.actualSize == 6)
  }

  test("Test that big endian national strings are decode properly") {
    withTempBinFile("national_be", ".dat", binFileContentsBE) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)


      val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      assertEqualsMultiline(actual, expectedData)
    }
  }

  test("Test that little endian national strings are decode properly") {
    withTempBinFile("national_le", ".dat", binFileContentsLE) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("is_utf16_big_endian", "false")
        .load(tmpFileName)

      val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      assertEqualsMultiline(actual, expectedData)
    }
  }

}
