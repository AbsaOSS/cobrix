/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.cobrix.spark.cobol.source

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.TestUtils

class VarSizeArraysSpecFields extends FunSuite with SparkTestBase with BinaryFileFixture {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N      PIC 9(1).
                03 A      OCCURS 0 TO 9 TIMES DEPENDING ON N.
                  05  B1  PIC X(1).
                  05  B2  PIC X(1).
                03 C      PIC X(1).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // Record 0 (full)
    0x00, 0x00, 0x14.toByte, 0x00,
    0xF9.toByte,
    0xF1.toByte, 0xF1.toByte, 0xF2.toByte, 0xF2.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF4.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF5.toByte, 0xF6.toByte, 0xF6.toByte,
    0xF7.toByte, 0xF7.toByte, 0xF8.toByte, 0xF8.toByte,
    0xF9.toByte, 0xF9.toByte,
    0xC1.toByte,
    // Record 1 (partial 1)
    0x00, 0x00, 0x0A.toByte, 0x00,
    0xF4.toByte,
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    0xC2.toByte,
    // Record 2 (partial 2)
    0x00, 0x00, 0x04.toByte, 0x00,
    0xF1.toByte,
    0xF0.toByte, 0xF9.toByte,
    0xC3.toByte,
    // Record 3 (empty)
    0x00, 0x00, 0x02.toByte, 0x00,
    0xF0.toByte,
    0xC4.toByte
  )

  test("Test input data file having a variable length array field") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("is_xcom", true)
        .option("schema_retention_policy", "collapse_root")
        .option("variable_size_occurs", "true")
        .load(tmpFileName)

      val expected =
        """+---+---------------------------------------------------------------+----+
          ||N  |A                                                              |C   |
          |+---+---------------------------------------------------------------+----+
          ||9  |[[1,1], [2,2], [3,3], [4,4], [5,5], [6,6], [7,7], [8,8], [9,9]]|A   |
          ||4  |[[1,2], [3,4], [5,6], [7,8]]                                   |B   |
          ||1  |[[0,9]]                                                        |C   |
          ||0  |[]                                                             |D   |
          |+---+---------------------------------------------------------------+----+
          |
          |""".stripMargin.replace("\r\n", "\n")

      val actual = TestUtils.showString(df, 10)
      // ToDo This test should pass when support for variable length arrays is implemented
      //println(actual)
      //assertResults(actual, expected)
    }

  }

  def assertResults(actualResults: String, expectedResults: String): Unit = {
    if (actualResults != expectedResults) {
      logger.error(s"EXPECTED:\n$expectedResults")
      logger.error(s"ACTUAL:\n$actualResults")
      fail("Actual dataset data does not match the expected data (see above).")
    }
  }

}
