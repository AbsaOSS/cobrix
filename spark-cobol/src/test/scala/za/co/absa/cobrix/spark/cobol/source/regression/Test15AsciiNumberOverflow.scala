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
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import java.nio.charset.StandardCharsets

/**
  * This suite checks if Spark is able to read numbers from ASCII files that overflow the expected data type.
  */
class Test15AsciiNumberOverflow extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  "Test ASCII CRLF text file with various numeric fields" should {
    "decode integral types" when {
      val copybook1 =
        """         01  R.
           05  N1    PIC 9(1).
           05  N2    PIC 9(2).
           05  N3    PIC +9(2).
           05  N4    PIC +9(18).
           05  N5    PIC +9(20).
    """

      val textFileContents: String = "122+33+123456789012345678+12345678901234567890\n3445551234567890123456789123456789012345678901\n"

      withTempTextFile("num_overflow", ".dat", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook1)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString
        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "schema should match" in {
          val expectedSchema =
            """root
              | |-- N1: integer (nullable = true)
              | |-- N2: integer (nullable = true)
              | |-- N3: integer (nullable = true)
              | |-- N4: long (nullable = true)
              | |-- N5: decimal(20,0) (nullable = true)""".stripMargin

          assertEqualsMultiline(actualSchema, expectedSchema)
        }

        "data should match" in {
          val expectedData =
            """[ {
              |  "N1" : 1,
              |  "N2" : 22,
              |  "N3" : 33,
              |  "N4" : 123456789012345678,
              |  "N5" : 12345678901234567890
              |}, {
              |  "N1" : 3,
              |  "N2" : 44,
              |  "N3" : 555,
              |  "N4" : 1234567890123456789
              |} ]""".stripMargin

          assertEqualsMultiline(actualData, expectedData)
        }
      }
    }

    "decode decimal types" in {
      val copybook2 =
        """         01  R.
           05  D1    PIC 9V9.
           05  D2    PIC 9.9.
           05  D3    PIC +99.999.
        """
      val textFileContents: String = "112.2+10.123\n334.4+55.666\n778.8999.999\n889.9-110222\n991.122"

      withTempTextFile("num_overflow", ".dat", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook2)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)

        val expected =
          """[ {
            |  "D1" : 1.1,
            |  "D2" : 2.2,
            |  "D3" : 10.123
            |}, {
            |  "D1" : 3.3,
            |  "D2" : 4.4,
            |  "D3" : 55.666
            |}, {
            |  "D1" : 7.7,
            |  "D2" : 8.8
            |}, {
            |  "D1" : 8.8,
            |  "D2" : 9.9
            |}, {
            |  "D1" : 9.9,
            |  "D2" : 1.1
            |} ]""".stripMargin

        val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }
  }

  // Ignore exhaustive overflow tests since they take too much time. These tests were used to catch overflow exceptions
  "Integral variants" ignore {
    for (len <- Range(2, 40)) {
      s"parse integers with length $len" in {
            val fieldPic = "9" * len

            val copybook =
              s"""         01  R.
           05  F    PIC +$fieldPic.
              """
            val n1 = getNumber(len-1, 0, sign = true)
            val n2 = getNumber(len-1, 0, sign = false)
            val n3 = getNumber(len, 0, sign = true)
            val n4 = getNumber(len, 0, sign = false)
            val n5 = getNumber(len+1, 0, sign = true)
            val n6 = getNumber(len+1, 0, sign = false)
            val n7 = getNumber(len+2, 0, sign = true)
            val n8 = getNumber(len+2, 0, sign = false)
            val n9 = getNumber(len+3, 0, sign = true)
            val n10 = getNumber(len+3, 0, sign = false)

            val asciiFile = s"$n1\n$n2\n$n3\n$n4\n$n5\n$n6\n$n7\n$n8\n$n9\n$n10"

            //println(asciiFile)
            withTempTextFile("num_overflow", ".dat", StandardCharsets.UTF_8, asciiFile) { tmpFileName =>
              val df = spark
                .read
                .format("cobol")
                .option("copybook_contents", copybook)
                .option("pedantic", "true")
                .option("is_text", "true")
                .option("encoding", "ascii")
                .option("schema_retention_policy", "collapse_root")
                .load(tmpFileName)

              df.count
            }
      }
    }
  }

  // Ignore exhaustive overflow tests since they take too much time. These tests were used to catch overflow exceptions
  "Decimal variants" ignore {
    for (len <- Range(2, 24)) {
      s"parse decimal with length $len" when {
        for (dec <- Range(1, len)) {
          s"decimal point is placed at $dec" in {
            val fieldPic = "9" * dec + "." + "9" * (len - dec)

            val copybook =
              s"""         01  R.
           05  F    PIC +$fieldPic.
              """
            val n1 = getNumber(len, dec, sign = true)
            val n2 = getNumber(len+1, dec, sign = false)
            val n3 = getNumber(len+2, 0, sign = false)
            val n4 = getNumber(len+1, 0, sign = false)
            val n5 = getNumber(len+1, 0, sign = true)
            val n6 = getNumber(len+2, dec, sign = true)
            val n7 = getNumber(len+2, dec, sign = false)
            val n8 = getNumber(len+3, dec, sign = true)
            val n9 = getNumber(len+3, dec, sign = false)

            val asciiFile = s"$n1\n$n2\n$n3\n$n4\n$n5\n$n6\n$n7\n$n8\n$n9"

            //println(asciiFile)
            withTempTextFile("num_overflow", ".dat", StandardCharsets.UTF_8, asciiFile) { tmpFileName =>
              val df = spark
                .read
                .format("cobol")
                .option("copybook_contents", copybook)
                .option("pedantic", "true")
                .option("is_text", "true")
                .option("encoding", "ascii")
                .option("schema_retention_policy", "collapse_root")
                .load(tmpFileName)

              df.count
            }

          }

        }
      }
    }
  }

  private def getNumber(len: Int, dec: Int, sign: Boolean): String = {
    val model = "123456789012345678901234567890"
    val s = if (sign) "+" else ""
    val num = if (dec <= 0) {
      model.take(len)
    } else {
      model.take(dec) + "." + model.take(len-dec)
    }

    s + num
  }
}
