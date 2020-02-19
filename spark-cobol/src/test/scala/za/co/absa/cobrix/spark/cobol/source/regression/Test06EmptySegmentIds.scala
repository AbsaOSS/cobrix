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

import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test06EmptySegmentIds extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  ENTITY.
           05  SEGMENT-ID           PIC X(1).
           05  SEG1.
              10  A                 PIC X(1).
           05  SEG2 REDEFINES SEG1.
              10  B                 PIC X(1).
           05  SEG3 REDEFINES SEG1.
              10  E                 PIC X(1).
    """

  val binFileContents1: Array[Byte] = Array[Byte](
    // 'Aa'
    0x00, 0x00, 0x02, 0x00, 0xC1.toByte, 0x81.toByte,
    // 'Bb'
    0x00, 0x00, 0x02, 0x00, 0xC2.toByte, 0x82.toByte,
    // ' e'
    0x00, 0x00, 0x02, 0x00, 0x40.toByte, 0x85.toByte
  )

  val binFileContents2: Array[Byte] = Array[Byte](
    // 'Aa'
    0x00, 0x00, 0x02, 0x00, 0xC1.toByte, 0x81.toByte,
    // 'Bb'
    0x00, 0x00, 0x02, 0x00, 0xC2.toByte, 0x82.toByte,
    // ' e'
    0x00, 0x00, 0x02, 0x00, 0x40.toByte, 0x85.toByte,
    // 'Dd'
    0x00, 0x00, 0x02, 0x00, 0xC4.toByte, 0x84.toByte
  )

  test("Test a segment redefine case with an empty segment id") {
    withTempBinFile("binary", ".dat", binFileContents1) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_record_sequence", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("segment_field", "SEGMENT_ID")
        .option("redefine_segment_id_map:1", "SEG1 => A")
        .option("redefine-segment-id-map:2", "SEG2 => B")
        .option("redefine-segment-id-map:3", "SEG3 => ")
        .load(tmpFileName)

      val expected = """[{"SEGMENT_ID":"A","SEG1":{"A":"a"}},{"SEGMENT_ID":"B","SEG2":{"B":"b"}},{"SEGMENT_ID":"","SEG3":{"E":"e"}}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test a segment redefine case with an empty segment id AND some id") {
    withTempBinFile("binary", ".dat", binFileContents2) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_record_sequence", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("segment_field", "SEGMENT_ID")
        .option("redefine_segment_id_map:1", "SEG1 => A")
        .option("redefine-segment-id-map:2", "SEG2 => B")
        .option("redefine-segment-id-map:3", "SEG3 => ,D")
        .load(tmpFileName)

      val expected = """[{"SEGMENT_ID":"A","SEG1":{"A":"a"}},{"SEGMENT_ID":"B","SEG2":{"B":"b"}},{"SEGMENT_ID":"","SEG3":{"E":"e"}},{"SEGMENT_ID":"D","SEG3":{"E":"d"}}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }


}
