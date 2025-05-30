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

import org.apache.spark.SparkException
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test26FixLengthWithIdGeneration extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  R.
           05  IND           PIC X(1).
           05  LEN           PIC 9(1).
           05  SEGMENT1.
              10    FIELD1   PIC X(1).
           05  SEGMENT2 REDEFINES SEGMENT1.
              10    FIELD2   PIC X(2).
           05  SEGMENT3 REDEFINES SEGMENT1.
              10    FIELD3   PIC X(3).
    """

  val binFileContentsLengthField: Array[Byte] = Array[Byte](
    // A1
    0xC1.toByte, 0xF3.toByte, 0xF1.toByte,
    // B22
    0xC2.toByte, 0xF4.toByte, 0xF2.toByte, 0xF2.toByte,
    // C333
    0xC3.toByte, 0xF5.toByte, 0xF3.toByte, 0xF3.toByte, 0xF3.toByte,
    // A2
    0xC1.toByte, 0xF3.toByte, 0xF2.toByte,
    // B23
    0xC2.toByte, 0xF4.toByte, 0xF2.toByte, 0xF3.toByte,
    // A3
    0xC1.toByte, 0xF3.toByte, 0xF3.toByte,
    // C345
    0xC3.toByte, 0xF5.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte,
    // A4
    0xC1.toByte, 0xF3.toByte, 0xF4.toByte,
    // A5
    0xC1.toByte, 0xF3.toByte, 0xF5.toByte
  )

  val binFileContentsLengthExpr: Array[Byte] = Array[Byte](
    // A1
    0xC1.toByte, 0xF2.toByte, 0xF1.toByte,
    // B22
    0xC2.toByte, 0xF3.toByte, 0xF2.toByte, 0xF2.toByte,
    // C333
    0xC3.toByte, 0xF4.toByte, 0xF3.toByte, 0xF3.toByte, 0xF3.toByte,
    // A2
    0xC1.toByte, 0xF2.toByte, 0xF2.toByte,
    // B23
    0xC2.toByte, 0xF3.toByte, 0xF2.toByte, 0xF3.toByte,
    // A3
    0xC1.toByte, 0xF2.toByte, 0xF3.toByte,
    // C345
    0xC3.toByte, 0xF4.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte,
    // A4
    0xC1.toByte, 0xF2.toByte, 0xF4.toByte,
    // A5
    0xC1.toByte, 0xF2.toByte, 0xF5.toByte
  )

  val expected: String =
  """[ {
    |  "Seg_Id0" : "ID_0_0",
    |  "IND" : "A",
    |  "SEGMENT1" : {
    |    "FIELD1" : "1"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_0",
    |  "Seg_Id1" : "ID_0_0_L1_1",
    |  "IND" : "B",
    |  "SEGMENT2" : {
    |    "FIELD2" : "22"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_0",
    |  "Seg_Id1" : "ID_0_0_L1_2",
    |  "IND" : "C",
    |  "SEGMENT3" : {
    |    "FIELD3" : "333"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_3",
    |  "IND" : "A",
    |  "SEGMENT1" : {
    |    "FIELD1" : "2"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_3",
    |  "Seg_Id1" : "ID_0_3_L1_1",
    |  "IND" : "B",
    |  "SEGMENT2" : {
    |    "FIELD2" : "23"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_5",
    |  "IND" : "A",
    |  "SEGMENT1" : {
    |    "FIELD1" : "3"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_5",
    |  "Seg_Id1" : "ID_0_5_L1_1",
    |  "IND" : "C",
    |  "SEGMENT3" : {
    |    "FIELD3" : "345"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_7",
    |  "IND" : "A",
    |  "SEGMENT1" : {
    |    "FIELD1" : "4"
    |  }
    |}, {
    |  "Seg_Id0" : "ID_0_8",
    |  "IND" : "A",
    |  "SEGMENT1" : {
    |    "FIELD1" : "5"
    |  }
    |} ]""".stripMargin

  "EBCDIC files" should {
    "correctly work with segment id generation option with length field" in {
      withTempBinFile("fix_length_reg1", ".dat", binFileContentsLengthField) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "LEN")
          .option("segment_field", "IND")
          .option("segment_id_prefix", "ID")
          .option("segment_id_level0", "A")
          .option("segment_id_level1", "_")
          .option("redefine-segment-id-map:0", "SEGMENT1 => A")
          .option("redefine-segment-id-map:1", "SEGMENT2 => B")
          .option("redefine-segment-id-map:2", "SEGMENT3 => C")
          .option("input_split_records", 1)
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df.drop("LEN").orderBy("Seg_Id0", "Seg_Id1"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly work with segment id generation option with length expression" in {
      withTempBinFile("fix_length_reg2", ".dat", binFileContentsLengthExpr) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "LEN + 1")
          .option("segment_field", "IND")
          .option("segment_id_prefix", "ID")
          .option("segment_id_level0", "A")
          .option("segment_id_level1", "_")
          .option("redefine-segment-id-map:0", "SEGMENT1 => A")
          .option("redefine-segment-id-map:1", "SEGMENT2 => B")
          .option("redefine-segment-id-map:2", "SEGMENT3 => C")
          .option("input_split_records", 1)
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df.drop("LEN").orderBy("Seg_Id0", "Seg_Id1"))

        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "correctly work with segment id generation option with length field" in {
    withTempBinFile("fix_length_reg3", ".dat", binFileContentsLengthField) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("record_format", "F")
        .option("record_length_field", "LEN")
        .option("strict_integral_precision", "true")
        .option("segment_field", "IND")
        .option("segment_id_prefix", "ID")
        .option("segment_id_level0", "A")
        .option("segment_id_level1", "_")
        .option("redefine-segment-id-map:0", "SEGMENT1 => A")
        .option("redefine-segment-id-map:1", "SEGMENT2 => B")
        .option("redefine-segment-id-map:2", "SEGMENT3 => C")
        .option("input_split_records", 1)
        .option("pedantic", "true")
        .load(tmpFileName)

      val actual = SparkUtils.convertDataFrameToPrettyJSON(df.drop("LEN").orderBy("Seg_Id0", "Seg_Id1"))

      assertEqualsMultiline(actual, expected)
    }
  }

  "work with string values" in {
    val copybook =
      """       01  R.
           05  LEN      PIC X(1).
           05  FIELD1   PIC X(1).
      """

    val binFileContentsLengthField: Array[Byte] = Array[Byte](
      // A1
      0xF2.toByte, 0xF3.toByte, 0xF3.toByte, 0xF4.toByte
    ).map(_.toByte)

    withTempBinFile("fix_length_str", ".dat", binFileContentsLengthField) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("record_format", "F")
        .option("record_length_field", "LEN")
        .option("pedantic", "true")
        .load(tmpFileName)

      assert(df.count() == 2)
    }
  }

  "fail for incorrect string values" in {
    val copybook =
      """       01  R.
           05  LEN      PIC X(1).
           05  FIELD1   PIC X(1).
      """

    val binFileContentsLengthField: Array[Byte] = Array[Byte](
      // A1
      0xF2.toByte, 0xF3.toByte, 0xC3.toByte, 0xF4.toByte
    ).map(_.toByte)

    withTempBinFile("fix_length_str", ".dat", binFileContentsLengthField) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("record_format", "F")
        .option("record_length_field", "LEN")
        .option("pedantic", "true")
        .load(tmpFileName)

      val ex = intercept[SparkException] {
        df.count()
      }

      assert(ex.getCause.getMessage.contains("Record length value of the field LEN must be an integral type, encountered: 'C'"))
    }
  }

}
