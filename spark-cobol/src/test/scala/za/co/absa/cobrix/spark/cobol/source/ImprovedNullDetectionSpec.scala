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
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.TestUtils

class ImprovedNullDetectionSpec extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N     PIC X(3).
                03 V     PIC 9(3).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // Record 0
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // Record 1
    0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
    // Record 2
    0x00, 0xF1.toByte, 0x00, 0x00, 0xF1.toByte, 0x00,
    // Record 3
    0x00, 0x40, 0x00, 0x00, 0x40, 0x00
  )

  test("Test when improved null detection is turned off") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("schema_retention_policy", "collapse_root")
        .option("pedantic", "true")
        .load(tmpFileName)

      val expected =
        """+---+----+
          ||N  |V   |
          |+---+----+
          ||   |null|
          ||   |null|
          ||1  |1   |
          ||   |null|
          |+---+----+""".stripMargin

      val actual = TestUtils.showString(df, 10)

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test when improved null detection is turned on") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("schema_retention_policy", "collapse_root")
        .option("pedantic", "true")
        .option("improved_null_detection", "true")
        .load(tmpFileName)

      val expected =
        """+----+----+
          ||N   |V   |
          |+----+----+
          ||null|null|
          ||    |null|
          ||1   |1   |
          ||    |null|
          |+----+----+""".stripMargin

      val actual = TestUtils.showString(df, 10)

      assertEqualsMultiline(actual, expected)
    }
  }
}
