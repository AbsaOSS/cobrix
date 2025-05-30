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

import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test01DisplayPicAsStrings extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N1    PIC 9(4).
                03 N2    PIC S9(4).
                03 D1    PIC 99V99.
                03 D2    PIC 99.99.
    """

  val binFileContents: Array[Byte] = "0001  2{011012.342010K111002200.01 300001J  1   .02".getBytes()

  test("Test a numeric fields having DISPLAY format are parsed as numbers") {
    withTempBinFile("num_display1", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("encoding", "ascii")
        .load(tmpFileName)

      val expectedSchema =
        """root
          | |-- N1: integer (nullable = true)
          | |-- N2: integer (nullable = true)
          | |-- D1: decimal(4,2) (nullable = true)
          | |-- D2: decimal(4,2) (nullable = true)
          |""".stripMargin.replace("\r\n", "\n")

      val expectedData =
        """[ {
          |  "N1" : 1,
          |  "N2" : 20,
          |  "D1" : 1.1,
          |  "D2" : 12.34
          |}, {
          |  "N1" : 2010,
          |  "N2" : -2111,
          |  "D1" : 0.22,
          |  "D2" : 0.01
          |}, {
          |  "N1" : 300,
          |  "N2" : -11,
          |  "D1" : 0.01,
          |  "D2" : 0.02
          |} ]""".stripMargin.replace("\r\n", "\n")

      val actualSchema = df.schema.treeString
      val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      assertEqualsMultiline(actualSchema, expectedSchema)
      assertEqualsMultiline(actualData, expectedData)
    }
  }

  test("Test a numeric fields having DISPLAY format are parsed as strings") {
    withTempBinFile("num_display2", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("encoding", "ascii")
        .option("display_pic_always_string", "true")
        .option("pedantic", "true")
        .load(tmpFileName)

      val expectedSchema =
        """root
          | |-- N1: string (nullable = true)
          | |-- N2: string (nullable = true)
          | |-- D1: decimal(4,2) (nullable = true)
          | |-- D2: decimal(4,2) (nullable = true)
          |""".stripMargin.replace("\r\n", "\n")

      val expectedData =
        """[ {
          |  "N1" : "0001",
          |  "N2" : "+20",
          |  "D1" : 1.1,
          |  "D2" : 12.34
          |}, {
          |  "N1" : "2010",
          |  "N2" : "-2111",
          |  "D1" : 0.22,
          |  "D2" : 0.01
          |}, {
          |  "N1" : "300",
          |  "N2" : "-0011",
          |  "D1" : 0.01,
          |  "D2" : 0.02
          |} ]""".stripMargin.replace("\r\n", "\n")

      val actualSchema = df.schema.treeString
      val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      assertEqualsMultiline(actualSchema, expectedSchema)
      assertEqualsMultiline(actualData, expectedData)
    }
  }

  test("Test incompatible options used together") {
    withTempBinFile("num_display2", ".dat", binFileContents) { tmpFileName =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ascii")
          .option("strict_integral_precision", "true")
          .option("display_pic_always_string", "true")
          .option("pedantic", "true")
          .load(tmpFileName)
      }

      assert(ex.getMessage == "Options 'display_pic_always_string' and 'strict_integral_precision' cannot be used together.")
    }
  }
}
