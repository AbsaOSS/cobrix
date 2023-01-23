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

class Test13AsciiCrLfText extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

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
          .option("record_format", "D")
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
          .option("record_format", "D")
          .load(tmpFileName)

        val count = df.count()

        assert(count == 0)
      }
    }

    "correctly read text files without EOL characters partial" in {
      val text = "AABBCC"
      withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("allow_partial_records", "true")
          .load(tmpFileName)

        val expected = """[{"A":"AA"},{"A":"BB"},{"A":"CC"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 3)
        assertEqualsMultiline(actual, expected)
      }
    }

    "correctly read text files without EOL characters non-partial" in {
      val text = "AABBCC"
      withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .load(tmpFileName)

        val expected = """[{"A":"AA"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 1)
        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "correctly read text files with a single EOL characters partial" in {
    val text = "AA\nBBCC"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("allow_partial_records", "true")
        .load(tmpFileName)

      val expected = """[{"A":"AA"},{"A":"BB"},{"A":"CC"}]"""

      val count = df.count()
      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(count == 3)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with a single EOL characters non-partial" in {
    val text = "AA\nBBCC"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .load(tmpFileName)

      val expected = """[{"A":"AA"},{"A":"BB"}]"""

      val count = df.count()
      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(count == 2)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with a double EOL characters partial" in {
    val text = "AA\r\nBBCC"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("allow_partial_records", "true")
        .load(tmpFileName)

      val expected = """[{"A":"AA"},{"A":"BB"},{"A":"CC"}]"""

      val count = df.count()
      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(count == 3)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with a double EOL characters non-partial" in {
    val text = "AA\r\nBBCC"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .load(tmpFileName)

      val expected = """[{"A":"AA"},{"A":"BB"}]"""

      val count = df.count()
      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(count == 2)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with a double EOL characters and 1 byte record and index" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(1).
    """

    val text = "A\r\nB\r\nC\nD\nE\nF"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook2)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("ascii_charset", "UTF-8")
        .load(tmpFileName)

      val expected = """[{"A":"A"},{"A":"B"},{"A":"C"},{"A":"D"},{"A":"E"},{"A":"F"}]"""

      val count = df.count()
      val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

      assert(count == 6)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with a double EOL characters and the last record is too short partial" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(4).
    """

    val text = "AAAA\r\nBBBBCCC"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook2)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("allow_partial_records", "true")
        .load(tmpFileName)

      val expected = """[{"A":"AAAA"},{"A":"BBBB"},{"A":"CCC"}]"""

      val count = df.count()
      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(count == 3)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with a double EOL characters and the last record is too short non-partial" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(4).
    """

    val text = "AAAA\r\nBBBBCCC"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook2)
        .option("pedantic", "true")
        .option("record_format", "D")
        .load(tmpFileName)

      val expected = """[{"A":"AAAA"},{"A":"BBBB"}]"""

      val count = df.count()
      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(count == 2)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files with indexing and a copybook that has a slightly greater record size partial" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(7).
    """

    val text = "AAAAA\r\nBBBBB\nCCCCC\nDDDDD\nEEEEE\nFFFFF\nGGGGG\nHHHHH"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook2)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("input_split_records", 2)
        .option("allow_partial_records", "true")
        .load(tmpFileName)

      val expected = """[{"A":"AAAAA"},{"A":"BBBBB"},{"A":"CCCCC"},{"A":"DDDDD"},{"A":"EEEEE"},{"A":"FFFFF"},{"A":"GGGGG"},{"A":"HHHHH"}]"""

      val count = df.count()
      val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

      assert(count == 8)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read text files and a copybook that has a slightly greater record size non-partial" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(7).
    """

    val text = "AAAAA\r\nBBBBB\nCCCCC\nDDDDD\nEEEEE\nFFFFF\nGGGGG\nHHHHH"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook2)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("ascii_charset", "UTF-8")
        .load(tmpFileName)

      val expected = """[{"A":"AAAAA"},{"A":"BBBBB"},{"A":"CCCCC"},{"A":"DDDDD"},{"A":"EEEEE"},{"A":"FFFFF"},{"A":"GGGGG"},{"A":"HHHHH"}]"""

      val count = df.count()
      val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

      assert(count == 8)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read basic ASCII text files with a copybook that has a slightly greater record size" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(7).
    """

    val text = "AAAAA\nBBBBB\nCCCCC\nDDDDD\nEEEEE\nFFFFF\nGGGGG\nHHHHH"
    withTempBinFile("crlf_empty", ".dat", text.getBytes()) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook2)
        .option("pedantic", "true")
        .option("record_format", "D2")
        .load(tmpFileName)

      val expected = """[{"A":"AAAAA"},{"A":"BBBBB"},{"A":"CCCCC"},{"A":"DDDDD"},{"A":"EEEEE"},{"A":"FFFFF"},{"A":"GGGGG"},{"A":"HHHHH"}]"""

      val count = df.count()
      val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

      assert(count == 8)
      assertEqualsMultiline(actual, expected)
    }
  }

  "correctly read basic ASCII text files in multiple directories" in {
    val copybook2 =
      """         01  ENTITY.
           05  A    PIC X(7).
    """

    val text1 = "AAAAA\nBBBBB\nCCCCC\nDDDDD"
    val text2 = "EEEEE\nFFFFF\nGGGGG\nHHHHH"
    withTempBinFile("crlf_empty1", ".dat", text1.getBytes()) { tmpFileName1 =>
      withTempBinFile("crlf_empty2", ".dat", text2.getBytes()) { tmpFileName2 =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook2)
          .option("pedantic", "true")
          .option("record_format", "D2")
          .option("data_paths", s"$tmpFileName1,$tmpFileName2")
          .load()
          .orderBy("A")

        val expected = """[{"A":"AAAAA"},{"A":"BBBBB"},{"A":"CCCCC"},{"A":"DDDDD"},{"A":"EEEEE"},{"A":"FFFFF"},{"A":"GGGGG"},{"A":"HHHHH"}]"""

        val count = df.count()
        val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

        assert(count == 8)
        assertEqualsMultiline(actual, expected)
      }
    }
  }

}
