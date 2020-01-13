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

package za.co.absa.cobrix.spark.cobol.source

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.TestUtils

class VarSizeArraysSpec extends FunSuite with SparkTestBase with BinaryFileFixture {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N      PIC 9(1).
                03 A      OCCURS 0 TO 9 TIMES DEPENDING ON N.
                  05  B1  PIC X(1).
                  05  B2  PIC X(1).
                03 C      PIC X(1).
    """

  private val binFixLenFileContents: Array[Byte] = Array[Byte](
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
    0x00, 0x00, 0x14.toByte, 0x00,
    0xF4.toByte,
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    0xF5.toByte, 0xF5.toByte, 0xF6.toByte, 0xF6.toByte,
    0xF7.toByte, 0xF7.toByte, 0xF8.toByte, 0xF8.toByte,
    0xF9.toByte, 0xF9.toByte,
    0xC2.toByte,
    // Record 2 (partial 2)
    0x00, 0x00, 0x14.toByte, 0x00,
    0xF1.toByte,
    0xF0.toByte, 0xF9.toByte, 0xF2.toByte, 0xF2.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF4.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF5.toByte, 0xF6.toByte, 0xF6.toByte,
    0xF7.toByte, 0xF7.toByte, 0xF8.toByte, 0xF8.toByte,
    0xF9.toByte, 0xF9.toByte,
    0xC3.toByte,
    // Record 3 (empty)
    0x00, 0x00, 0x14.toByte, 0x00,
    0xF0.toByte,
    0xF1.toByte, 0xF1.toByte, 0xF2.toByte, 0xF2.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF4.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF5.toByte, 0xF6.toByte, 0xF6.toByte,
    0xF7.toByte, 0xF7.toByte, 0xF8.toByte, 0xF8.toByte,
    0xF9.toByte, 0xF9.toByte,
    0xC4.toByte
  )

  private val binVarLenFileContents: Array[Byte] = Array[Byte](
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

  private val expected =
    """{"N":9,"A":[{"B1":"1","B2":"1"},{"B1":"2","B2":"2"},{"B1":"3","B2":"3"},{"B1":"4","B2":"4"},{"B1":"5","B2":"5"},{"B1":"6","B2":"6"},{"B1":"7","B2":"7"},{"B1":"8","B2":"8"},{"B1":"9","B2":"9"}],"C":"A"}
      |{"N":4,"A":[{"B1":"1","B2":"2"},{"B1":"3","B2":"4"},{"B1":"5","B2":"6"},{"B1":"7","B2":"8"}],"C":"B"}
      |{"N":1,"A":[{"B1":"0","B2":"9"}],"C":"C"}
      |{"N":0,"A":[],"C":"D"}""".stripMargin.replace("\r\n", "\n")

  test("Test input data file having a fixed length array field") {
    withTempBinFile("binary", ".dat", binFixLenFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("is_xcom", true)
        .option("schema_retention_policy", "collapse_root")
        .option("variable_size_occurs", "false")
        .load(tmpFileName)

      val actual = df.toJSON.collect().mkString("\n")
      assertResults(actual, expected)
    }

  }

  test("Test input data file having a variable length array field") {
    withTempBinFile("binary", ".dat", binVarLenFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("is_xcom", true)
        .option("schema_retention_policy", "collapse_root")
        .option("variable_size_occurs", "true")
        .load(tmpFileName)

      val actual = df.toJSON.collect().mkString("\n")
      assertResults(actual, expected)
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
