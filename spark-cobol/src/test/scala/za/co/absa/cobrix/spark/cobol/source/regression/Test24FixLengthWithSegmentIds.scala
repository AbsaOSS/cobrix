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

import java.nio.charset.StandardCharsets

class Test24FixLengthWithSegmentIds extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  R.
           05  IND           PIC X(1).
           05  SEGMENT1.
              10    FIELD1   PIC X(1).
           05  SEGMENT2 REDEFINES SEGMENT1.
              10    FIELD2   PIC X(2).
           05  SEGMENT3 REDEFINES SEGMENT1.
              10    FIELD3   PIC X(3).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // A1
    0xC1.toByte, 0xF1.toByte, 0xF0.toByte, 0xF0.toByte,
    // B22
    0xC2.toByte, 0xF2.toByte, 0xF2.toByte, 0xF0.toByte,
    // B33
    0xC2.toByte, 0xF3.toByte, 0xF3.toByte, 0xF0.toByte,
    // B44
    0xC2.toByte, 0xF4.toByte, 0xF4.toByte, 0xF0.toByte,
    // C555
    0xC3.toByte, 0xF5.toByte, 0xF5.toByte, 0xF5.toByte
  )

  val textFileContents: String = "A1\nB22\nB33\nB44\nC555\n"

  val expected = """[{"Seg_Id0":"ID_0_0","IND":"A","SEGMENT1":{"FIELD1":"1"}},{"Seg_Id0":"ID_0_0","IND":"B","SEGMENT2":{"FIELD2":"22"}},{"Seg_Id0":"ID_0_0","IND":"B","SEGMENT2":{"FIELD2":"33"}},{"Seg_Id0":"ID_0_0","IND":"B","SEGMENT2":{"FIELD2":"44"}},{"Seg_Id0":"ID_0_0","IND":"C","SEGMENT3":{"FIELD3":"555"}}]"""

  "ASCII files" should {
    "correctly work with segment id generation option" in {
      withTempTextFile("fix_length_reg", ".txt", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("segment_field", "IND")
          .option("segment_id_level0", "A")
          .option("segment_id_prefix", "ID")
          .option("redefine-segment-id-map:0", "SEGMENT1 => A")
          .option("redefine-segment-id-map:1", "SEGMENT2 => B")
          .option("redefine-segment-id-map:2", "SEGMENT3 => C")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "EBCDIC files" should {
    "correctly work with segment id generation option" in {
      withTempBinFile("fix_length_reg", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("segment_field", "IND")
          .option("segment_id_prefix", "ID")
          .option("segment_id_level0", "A")
          .option("redefine-segment-id-map:0", "SEGMENT1 => A")
          .option("redefine-segment-id-map:1", "SEGMENT2 => B")
          .option("redefine-segment-id-map:2", "SEGMENT3 => C")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly work with segment id root generation option" in {
      withTempBinFile("fix_length_reg", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("segment_field", "IND")
          .option("segment_id_prefix", "ID")
          .option("segment_id_root", "A")
          .option("redefine-segment-id-map:0", "SEGMENT1 => A")
          .option("redefine-segment-id-map:1", "SEGMENT2 => B")
          .option("redefine-segment-id-map:2", "SEGMENT3 => C")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }
  }
}
