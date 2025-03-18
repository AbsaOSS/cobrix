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

package za.co.absa.cobrix.spark.cobol.source.regression

import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import java.nio.charset.StandardCharsets

class Test18AsciiNulChars extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  ENTITY.
           05  A    PIC X(1).
           05  B    PIC X(3).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 0 - spaces
    0x31.toByte, 0x20.toByte, 0x20.toByte, 0x20.toByte, 0x0D.toByte, 0x0A.toByte,
    // 1 - spaces and NUL
    0x31.toByte, 0x00.toByte, 0x20.toByte, 0x00.toByte, 0x0D.toByte, 0x0A.toByte,
    // 2 - only NULL
    0x31.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x0D.toByte, 0x0A.toByte,
    // 3 - control characters
    0x31.toByte, 0x01.toByte, 0x02.toByte, 0x03.toByte, 0x0D.toByte, 0x0A.toByte
  )

  "Test ASCII text file with NUL characters" should {
    "correctly parse the file contents" in {
      withTempBinFile("ascii_nul", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D2")
          .option("encoding", "ascii")
          .option("string_trimming_policy", "none")
          .load(tmpFileName)

        val expected = """[{"A":"1","B":"   "},{"A":"1","B":" "},{"A":"1"},{"A":"1","B":""}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
      }
    }

    "keep control characters for 'keep_all' trimming policy" in {
      withTempBinFile("ascii_nul", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D2")
          .option("encoding", "ascii")
          .option("ascii_charset", "US-ASCII")
          .option("string_trimming_policy", "keep_all")
          .load(tmpFileName)

        val b = "\\"
        val expected = s"""[{"A":"1","B":"   "},{"A":"1","B":"${b}u0000 ${b}u0000"},{"A":"1","B":"${b}u0000${b}u0000${b}u0000"},{"A":"1","B":"${b}u0001${b}u0002${b}u0003"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "ASCII text file with uneven records" should {
    val text =
      """1
        |12
        |123
        |1234
        |12345
        |123456
        |1234567
        |12345678
        |123456789
        |12345678901234567890123456789
        |5678
        |""".stripMargin.replaceAll("\r", "")

    "not generate redundant records" in {
      withTempTextFile("ascii_nul", ".dat", StandardCharsets.UTF_8, text) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D2")
          .option("encoding", "ascii")
          .option("string_trimming_policy", "keep_all")
          .option("generate_record_id", "true")
          .option("improved_null_detection", "false")
          .load(tmpFileName)

        val expected =
          """[ {
            |  "File_Id" : 0,
            |  "Record_Id" : 0,
            |  "Record_Byte_Length" : 1,
            |  "A" : "1",
            |  "B" : ""
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 1,
            |  "Record_Byte_Length" : 2,
            |  "A" : "1",
            |  "B" : "2"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 2,
            |  "Record_Byte_Length" : 3,
            |  "A" : "1",
            |  "B" : "23"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 3,
            |  "Record_Byte_Length" : 4,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 4,
            |  "Record_Byte_Length" : 5,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 5,
            |  "Record_Byte_Length" : 6,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 6,
            |  "Record_Byte_Length" : 7,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 7,
            |  "Record_Byte_Length" : 8,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 8,
            |  "Record_Byte_Length" : 9,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 9,
            |  "Record_Byte_Length" : 4,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "File_Id" : 0,
            |  "Record_Id" : 10,
            |  "Record_Byte_Length" : 4,
            |  "A" : "5",
            |  "B" : "678"
            |} ]
            |""".stripMargin

        val count = df.count()
        val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assert(count == 11)
        assertEqualsMultiline(actual, expected)
      }
    }

    "allow partial records" in {
      withTempTextFile("ascii_nul", ".dat", StandardCharsets.UTF_8, text) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D2")
          .option("improved_null_detection", "false")
          .option("encoding", "ascii")
          .option("string_trimming_policy", "keep_all")
          .option("generate_record_id", "true")
          .option("allow_partial_records", "true")
          .load(tmpFileName)

        val count = df.count()

        assert(count == 21)
      }
    }

    "allow partial records with indexing" in {
      withTempTextFile("ascii_nul", ".dat", StandardCharsets.UTF_8, text) { tmpFileName =>
        val expected =
          """[ {
            |  "Record_Id" : 0,
            |  "A" : "1",
            |  "B" : ""
            |}, {
            |  "Record_Id" : 1,
            |  "A" : "1",
            |  "B" : "2"
            |}, {
            |  "Record_Id" : 2,
            |  "A" : "1",
            |  "B" : "23"
            |}, {
            |  "Record_Id" : 3,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 4,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 5,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 6,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 7,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 8,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 9,
            |  "A" : "1",
            |  "B" : "234"
            |}, {
            |  "Record_Id" : 10,
            |  "A" : "5",
            |  "B" : "678"
            |} ]
            |""".stripMargin

        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("improved_null_detection", "false")
          .option("input_split_records", 2)
          .option("encoding", "ascii")
          .option("string_trimming_policy", "keep_all")
          .option("generate_record_id", "true")
          .load(tmpFileName)
          .select("Record_Id", "A", "B")
          .orderBy("Record_Id")

        val count = df.count()
        val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assert(count == 11)
        assertEqualsMultiline(actual, expected)
      }
    }

    "don't lose any records" in {
      val copybook =
        """         01  ENTITY.
           05  A    PIC X(3).
           05  B    PIC X(3).
    """

      val expected =
        """[ {
          |  "Record_Id" : 0,
          |  "A" : "123",
          |  "B" : "456"
          |}, {
          |  "Record_Id" : 1,
          |  "A" : "567",
          |  "B" : "890"
          |}, {
          |  "Record_Id" : 2,
          |  "A" : "123",
          |  "B" : "456"
          |}, {
          |  "Record_Id" : 3,
          |  "A" : "7"
          |} ]""".stripMargin

      val text = "123456\n567890\n123456\n7"

      withTempTextFile("ascii_nul", ".dat", StandardCharsets.UTF_8, text) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("input_split_records", 3)
          .option("generate_record_id", "true")
          .load(tmpFileName)
          .select("Record_Id", "A", "B")
          .orderBy("Record_Id")

        val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

  }
}
