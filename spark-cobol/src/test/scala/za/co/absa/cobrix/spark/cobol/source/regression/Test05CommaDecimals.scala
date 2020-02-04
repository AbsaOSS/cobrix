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

class Test05CommaDecimals extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N     PIC +999,99 USAGE DISPLAY.
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // +112,34
    0x4E.toByte, 0xF1.toByte, 0xF1.toByte, 0xF2.toByte, 0x6B.toByte, 0xF3.toByte, 0xF4.toByte,
    //  -23,45
    0x40.toByte, 0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0x6B.toByte, 0xF4.toByte, 0xF5.toByte,
    // +005,00
    0x4E.toByte, 0xF0.toByte, 0xF0.toByte, 0xF5.toByte, 0x6B.toByte, 0xF0.toByte, 0xF0.toByte
  )

  test("Test input data file having a numeric field with a commaa as the decimal separator") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val expected = """[{"N":112.34},{"N":-23.45},{"N":5.00}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }

  }

}
