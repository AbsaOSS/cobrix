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

class Test22RecordLengthGenId extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  R.
           05  A    PIC 9(1).
           05  B    PIC X(2).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 123{456}
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xC0.toByte,
    // 789J123A
    0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xD1.toByte,
    // 65432101
    0xF6.toByte, 0xF5.toByte, 0xF4.toByte, 0xF3.toByte
  )

  "EBCDIC files" should {
    "correctly work without record it generation" in {
      withTempBinFile("sign_overpunch", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_LENGTH", "4")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[{"A":1,"B":"23"},{"A":7,"B":"89"},{"A":6,"B":"54"}]"""

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly work with record it generation" in {
      withTempBinFile("sign_overpunch", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length", "4")
          .option("generate_record_id", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expected = """[ {
          |  "File_Id" : 0,
          |  "Record_Id" : 0,
          |  "Record_Byte_Length" : 4,
          |  "A" : 1,
          |  "B" : "23"
          |}, {
          |  "File_Id" : 0,
          |  "Record_Id" : 1,
          |  "Record_Byte_Length" : 4,
          |  "A" : 7,
          |  "B" : "89"
          |}, {
          |  "File_Id" : 0,
          |  "Record_Id" : 2,
          |  "Record_Byte_Length" : 4,
          |  "A" : 6,
          |  "B" : "54"
          |} ]""".stripMargin

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

        assertEqualsMultiline(actual, expected)
      }
    }
  }
}
