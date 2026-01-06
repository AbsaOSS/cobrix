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

class Test19SignOverpunching extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  R.
           05  A    PIC 9(4).
           05  B    PIC S9(4).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 123{456}
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xC0.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xD0.toByte,
    // 789J123A
    0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xD1.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xC1.toByte,
    // 65432101
    0xF6.toByte, 0xF5.toByte, 0xF4.toByte, 0xF3.toByte, 0xF2.toByte, 0xF1.toByte, 0xF0.toByte, 0xF1.toByte
  )

  "ASCII files" should {
    val data =
      """123{456}
        |789J123A
        |65432101
        |""".stripMargin

    "correctly decode sign overpunched numbers with default sign overpunch settings" in {
      withTempTextFile("sign_overpunch", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"B":-4560},{"B":1231},{"A":6543,"B":2101}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly decode sign overpunched numbers with relaxed sign overpunch settings" in {
      withTempTextFile("sign_overpunch", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("strict_sign_overpunching", "false")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"A":1230,"B":-4560},{"B":1231},{"A":6543,"B":2101}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly decode sign overpunched numbers with strict sign overpunch settings" in {
      withTempTextFile("sign_overpunch", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("strict_sign_overpunching", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"B":-4560},{"B":1231},{"A":6543,"B":2101}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "EBCDIC files" should {
    val data =
      """123{456}
        |789J123A
        |65432101
        |""".stripMargin

    "correctly decode sign overpunched numbers with default sign overpunch settings" in {
      withTempBinFile("sign_overpunch", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"B":-4560},{"B":1231},{"A":6543,"B":2101}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }
    "correctly decode sign overpunched numbers with relaxed sign overpunch settings" in {
      withTempBinFile("sign_overpunch", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("strict_sign_overpunching", "false")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"A":1230,"B":-4560},{"B":1231},{"A":6543,"B":2101}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly decode sign overpunched numbers with strict sign overpunch settings" in {
      withTempBinFile("sign_overpunch", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("strict_sign_overpunching", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"B":-4560},{"B":1231},{"A":6543,"B":2101}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }

    "Test relaxed sign overpunching" when {
      val copybook =
        """         01  R.
           05  A    PIC S9(5)V9.
    """
      "The number is EBCDIC" in {
        val binFileContents: Array[Byte] = Array[Byte](
          0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xD0.toByte, 0xC4.toByte, 0x40
        )

        withTempBinFile("sign_overpunch", ".dat", binFileContents) { tmpFileName =>
          val df = spark
            .read
            .format("cobol")
            .option("copybook_contents", copybook)
            .option("record_format", "F")
            .option("strict_sign_overpunching", "false")
            .option("pedantic", "true")
            .load(tmpFileName)

          val actual = df.toJSON.collect().mkString("[", ",", "]")
          val expected = """[{"A":1230.4}]"""
          assertEqualsMultiline(actual, expected)
        }
      }

      "The number is ASCII" in {
        val data = "123}D "
        withTempTextFile("sign_overpunch", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
          val df = spark
            .read
            .format("cobol")
            .option("copybook_contents", copybook)
            .option("record_format", "D")
            .option("strict_sign_overpunching", "false")
            .option("pedantic", "true")
            .load(tmpFileName)

          val expected = """[{"A":1230.4}]"""

          val actual = df.toJSON.collect().mkString("[", ",", "]")

          assertEqualsMultiline(actual, expected)
        }
      }
    }
  }
}
