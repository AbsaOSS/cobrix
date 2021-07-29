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

class Test13AsciiCrLfText extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  ENTITY.
           05  A    PIC X(2).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // 0
    0x66.toByte, 0x64.toByte, 0x0D.toByte, 0x0A.toByte,
    // 1
    0x68.toByte, 0x64.toByte, 0x0D.toByte, 0x0A.toByte,
    // 2 - empty line
    0x0D.toByte, 0x0A.toByte,
    // 3
    0x73.toByte, 0x64.toByte, 0x0D.toByte, 0x0A.toByte,
    // 4 - empty line
    0x0D.toByte, 0x0A.toByte
  )

  val emptyFileContents: Array[Byte] = Array[Byte](
    // 0 - empty line
    0x0D.toByte, 0x0A.toByte,
    // 1 - empty line
    0x0D.toByte, 0x0A.toByte
  )

  "Test ASCII CRLF text file" should {
    "correctly identify empty lines when read as a text file" in {
      withTempBinFile("crlf", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .load(tmpFileName)

        val expected = """[{"A":"fd"},{"A":"hd"},{"A":"sd"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 3)
        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly identify empty lines when read as a record sequence" in {
      withTempBinFile("crlf", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("is_record_sequence", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .load(tmpFileName)

        val expected = """[{"A":"fd"},{"A":"hd"},{"A":"sd"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 3)
        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "Test empty ASCII CRLF text file" should {
    "correctly identify empty lines when read as a text file" in {
      withTempBinFile("crlf_empty", ".dat", emptyFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .load(tmpFileName)

        val count = df.count()

        assert(count == 0)
      }
    }

    "correctly identify empty lines when read as a record sequence" in {
      withTempBinFile("crlf_empty", ".dat", emptyFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("is_record_sequence", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .load(tmpFileName)

        val count = df.count()

        assert(count == 0)
      }
    }
  }
}
