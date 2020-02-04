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
import za.co.absa.cobrix.spark.cobol.utils.TestUtils

class Test04VarcharFields extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N     PIC X(1).
                03 V     PIC X(10).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // Record 0 (full)
    0x00, 0x00, 0x0B.toByte, 0x00,
    0xF0.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte,
    0xF8.toByte, 0xF9.toByte, 0xF0.toByte,
    // Record 1 (full with spaces)
    0x00, 0x00, 0x0B.toByte, 0x00,
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    0x40.toByte, 0x40.toByte, 0x40.toByte,
    // Record 2 (partial 1)
    0x00, 0x00, 0x0A.toByte, 0x00,
    0xF2.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    0x40.toByte, 0x40.toByte,
    // Record 3 (partial 2)
    0x00, 0x00, 0x04.toByte, 0x00,
    0xF3.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    // Record 4 (partial 2)
    0x00, 0x00, 0x02.toByte, 0x00,
    0xF4.toByte, 0xF1.toByte,
    // Record 5 (partial 3 - tiny)
    0x00, 0x00, 0x01.toByte, 0x00,
    0xF5.toByte
  )

  val binFileContentsNoTrim: Array[Byte] = Array[Byte](
    // Record 0 (partial 1)
    0x00, 0x00, 0x04.toByte, 0x00,
    0xF3.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    // Record 1 (partial 2)
    0x00, 0x00, 0x02.toByte, 0x00,
    0xF4.toByte, 0xF1.toByte,
    // Record 2 (partial 3 - tiny)
    0x00, 0x00, 0x01.toByte, 0x00,
    0xF5.toByte
  )


  test("Test input data file having a varchar text field at the end of the copybook") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("generate_record_id", true)
        .option("is_xcom", true)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val expected =
        """+-------+---------+---+----------+
          ||File_Id|Record_Id|N  |V         |
          |+-------+---------+---+----------+
          ||0      |0        |0  |1234567890|
          ||0      |1        |1  |2345678   |
          ||0      |2        |2  |2345678   |
          ||0      |3        |3  |123       |
          ||0      |4        |4  |1         |
          ||0      |5        |5  |          |
          |+-------+---------+---+----------+
          |
          |""".stripMargin.replace("\r\n", "\n")

      val actual = TestUtils.showString(df, 10)

      assertEqualsMultiline(actual, expected)
    }

  }

  test("Test input data file having a varchar text field when string trimming is turned off") {
    withTempBinFile("binary", ".dat", binFileContentsNoTrim) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("generate_record_id", true)
        .option("is_xcom", true)
        .option("string_trimming_policy", "none")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val expected =
        """+-------+---------+---+---+
          ||File_Id|Record_Id|N  |V  |
          |+-------+---------+---+---+
          ||0      |0        |3  |123|
          ||0      |1        |4  |1  |
          ||0      |2        |5  |   |
          |+-------+---------+---+---+
          |
          |""".stripMargin.replace("\r\n", "\n")

      val expectedJson =
        """{"File_Id":0,"Record_Id":0,"N":"3","V":"123"},{"File_Id":0,"Record_Id":1,"N":"4","V":"1"},{"File_Id":0,"Record_Id":2,"N":"5","V":""}"""

      val actual = TestUtils.showString(df, 10)
      val actualJson = df.toJSON.collect().mkString(",")

      assertEqualsMultiline(actual, expected)
      assertEqualsMultiline(actualJson, expectedJson)
    }

  }

}
