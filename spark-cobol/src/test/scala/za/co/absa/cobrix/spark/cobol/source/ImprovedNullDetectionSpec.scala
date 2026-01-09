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

import org.apache.spark.sql.DataFrameReader
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.TestUtils

class ImprovedNullDetectionSpec extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val defaultCopybook =
    """       01  R.
                03 N     PIC X(3).
                03 V     PIC 9(3).
    """

  val binFileContents: Array[Byte] = Array(
    // Record 0
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // Record 1
    0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
    // Record 2
    0x00, 0xF1, 0x00, 0x00, 0xF1, 0x00,
    // Record 3
    0x00, 0x40, 0x00, 0x00, 0x40, 0x00
  ).map(_.toByte)

  // Helper method to create DataFrame with options
  private def readCobolDF(copybook: String,
                          improvedNullDetection: Boolean,
                          strictSignOverpunching: Boolean = true): DataFrameReader = {
    val reader = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("schema_retention_policy", "collapse_root")
      .option("pedantic", "true")
      .option("improved_null_detection", improvedNullDetection.toString)
      .option("strict_sign_overpunching", strictSignOverpunching.toString)
    reader
  }

  // Helper method for assertion
  private def assertDFResult(copybook: String,
                             improvedNullDetection: Boolean,
                             strictSignOverpunching: Boolean = true,
                             expected: String): Unit = {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = readCobolDF(copybook, improvedNullDetection, strictSignOverpunching).load(tmpFileName)
      val actual = TestUtils.showString(df, 10)
      assertEqualsMultiline(actual, expected)
    }
  }

  test("improved_null_detection = false, default copybook, expect nulls for V") {
    val expected =
      """+---+----+
        ||N  |V   |
        |+---+----+
        ||   |null|
        ||   |null|
        ||1  |1   |
        ||   |null|
        |+---+----+""".stripMargin

    assertDFResult(defaultCopybook, improvedNullDetection = false, expected = expected)
  }

  test("improved_null_detection = true, strict_sign_overpunching = false, decimal values") {
    val copybook =
      """       01  R.
                03 N     PIC X(3).
                03 V     PIC 9(2)V9.
    """
    val expected =
      """+----+----+
        ||N   |V   |
        |+----+----+
        ||null|null|
        ||    |null|
        ||1   |0.1 |
        ||    |null|
        |+----+----+""".stripMargin

    assertDFResult(copybook, improvedNullDetection = true, strictSignOverpunching = false, expected)
  }

  test("improved_null_detection = false, strict_sign_overpunching = false, decimal valuse") {
    val copybook =
      """       01  R.
                03 N     PIC X(3).
                03 V     PIC 9(2)V9.
    """
    val expected =
      """+---+---+
        ||N  |V  |
        |+---+---+
        ||   |0.0|
        ||   |0.0|
        ||1  |0.1|
        ||   |0.0|
        |+---+---+""".stripMargin

    assertDFResult(copybook, improvedNullDetection = false, strictSignOverpunching = false, expected)
  }

  test("improved_null_detection = true, default copybook, expect nulls for N and V") {
    val expected =
      """+----+----+
        ||N   |V   |
        |+----+----+
        ||null|null|
        ||    |null|
        ||1   |1   |
        ||    |null|
        |+----+----+""".stripMargin

    assertDFResult(defaultCopybook, improvedNullDetection = true, expected = expected)
  }
}
