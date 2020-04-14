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

class Test10DeepSegmentRedefines extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  ENTITY.
        02 NESTED1.
           03 NESTED2.
              05  ID                      PIC X(1).
           03 NESTED3.
              04 NESTED4.
                 05  SEG1.
                    10  A                 PIC X(1).
                 05  SEG2 REDEFINES SEG1.
                    10  B                 PIC X(1).
                 05  SEG3 REDEFINES SEG1.
                    10  C                 PIC X(1).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 'Aa'
    0x00, 0x00, 0x02, 0x00, 0xC1.toByte, 0x81.toByte,
    // 'Bb'
    0x00, 0x00, 0x02, 0x00, 0xC2.toByte, 0x82.toByte,
    // 'Cc'
    0x00, 0x00, 0x02, 0x00, 0xC3.toByte, 0x83.toByte,
    // 'Dd'
    0x00, 0x00, 0x02, 0x00, 0xC4.toByte, 0x84.toByte
  )

  test("Test a segment redefines work for deeply nested segment fields") {
    withTempBinFile("binary_nested1", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_record_sequence", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("segment_field", "ID")
        .option("redefine_segment_id_map:1", "SEG1 => A")
        .option("redefine-segment-id-map:2", "SEG2 => B")
        .option("redefine-segment-id-map:3", "SEG3 => C")
        .load(tmpFileName)

      val expected = """[{"NESTED1":{"NESTED2":{"ID":"A"},"NESTED3":{"NESTED4":{"SEG1":{"A":"a"}}}}},{"NESTED1":{"NESTED2":{"ID":"B"},"NESTED3":{"NESTED4":{"SEG2":{"B":"b"}}}}},{"NESTED1":{"NESTED2":{"ID":"C"},"NESTED3":{"NESTED4":{"SEG3":{"C":"c"}}}}},{"NESTED1":{"NESTED2":{"ID":"D"},"NESTED3":{"NESTED4":{}}}}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

}
