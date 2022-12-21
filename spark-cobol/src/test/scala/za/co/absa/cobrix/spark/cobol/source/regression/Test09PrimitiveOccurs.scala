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

import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test09PrimitiveOccurs extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  ENTITY.
           05  CNT    PIC 9(1).
           05  A      PIC 9(2) OCCURS 0 TO 5 DEPENDING ON CNT.
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 0
    0xF0.toByte,
    // 1
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    // 3
    0xF3.toByte, 0xF2.toByte, 0xF3.toByte, 0xF0.toByte,
    0xF1.toByte, 0xF5.toByte, 0xF6.toByte,
    // 5
    0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte,
    0xF8.toByte, 0xF9.toByte, 0xF0.toByte
  )

  test("Test an OCCURS of primitives") {
    withTempBinFile("occurs_prim", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("variable_size_occurs", "true")
        .load(tmpFileName)

      val expected = """[{"CNT":0,"A":[]},{"CNT":1,"A":[23]},{"CNT":3,"A":[23,1,56]},{"CNT":5,"A":[12,34,56,78,90]}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test debugging of an OCCURS of primitives") {
    withTempBinFile("occurs_prim", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("variable_size_occurs", "true")
        .option("debug", "true")
        .load(tmpFileName)

      val expected = """[{"CNT":0,"CNT_debug":"F0","A":[],"A_debug":[]},{"CNT":1,"CNT_debug":"F1","A":[23],"A_debug":["F2F3"]},{"CNT":3,"CNT_debug":"F3","A":[23,1,56],"A_debug":["F2F3","F0F1","F5F6"]},{"CNT":5,"CNT_debug":"F5","A":[12,34,56,78,90],"A_debug":["F1F2","F3F4","F5F6","F7F8","F9F0"]}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

}
