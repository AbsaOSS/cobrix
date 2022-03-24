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

import org.scalatest.WordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test18AsciiNulChars extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  ENTITY.
           05  A    PIC X(1).
           05  B    PIC X(3).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 0 - spaces
    0x31.toByte, 0x20.toByte, 0x20.toByte, 0x20.toByte, 0x0D.toByte, 0x0A.toByte,
    // 1 - spaces and NUL
    0x31.toByte, 0x00.toByte, 0x20.toByte, 0x00.toByte, 0x0D.toByte, 0x0A.toByte,
    // 2 - only NULL
    0x31.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x0D.toByte, 0x0A.toByte,
    // 3 - control characters
    0x31.toByte, 0x01.toByte, 0x02.toByte, 0x03.toByte, 0x0D.toByte, 0x0A.toByte
  )

  "Test ASCII text file with NUL characters" should {
    "correctly parse the file contents" in {
      withTempBinFile("ascii_nul", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("encoding", "ascii")
          .option("string_trimming_policy", "none")
          .load(tmpFileName)

        val expected = """[{"A":"1","B":"   "},{"A":"1","B":" "},{"A":"1","B":""},{"A":"1","B":""}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
      }
    }

    "keep control characters for 'keep_all' trimming policy" in {
      withTempBinFile("ascii_nul", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("encoding", "ascii")
          .option("string_trimming_policy", "keep_all")
          .load(tmpFileName)

        val b = "\\"
        val expected = s"""[{"A":"1","B":"   "},{"A":"1","B":"${b}u0000 ${b}u0000"},{"A":"1","B":"${b}u0000${b}u0000${b}u0000"},{"A":"1","B":"${b}u0001${b}u0002${b}u0003"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
      }
    }
  }
}
