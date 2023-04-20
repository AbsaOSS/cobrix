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

package za.co.absa.cobrix.spark.cobol.source.text

import java.nio.charset.StandardCharsets
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.Cobrix
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test01AsciiTextFiles extends AnyFunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  RECORD.
           05  A1       PIC X(1).
           05  A2       PIC X(5).
           05  A3       PIC X(10).
    """

  private val textFileContent: String =
    Seq("1Tes  0123456789",
        "2 est2 SomeText ",
        "3None Data¡3    ",
        "4 on      Data 4").mkString("\n")

  test("Test EOL separated text files can be read basic ascii") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_text", "true")
        .option("encoding", "ascii")
        .option("ascii_charset", "US-ASCII")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)
        .orderBy("A1")

      val expected = """[{"A1":"1","A2":"Tes","A3":"0123456789"},{"A1":"2","A2":"est2","A3":"SomeText"},{"A1":"3","A2":"None","A3":"Data  3"},{"A1":"4","A2":"on","A3":"Data 4"}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test EOL separated text files can be read custom ascii") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_text", "true")
        .option("encoding", "ascii")
        .option("ascii_charset", "US-ASCII")
        .option("allow_partial_records", "true")
        .option("input_split_records", 2)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)
        .orderBy("A1")

      val expected = """[{"A1":"1","A2":"Tes","A3":"0123456789"},{"A1":"2","A2":"est2","A3":"SomeText"},{"A1":"3","A2":"None","A3":"Data  3"},{"A1":"4","A2":"on","A3":"Data 4"}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test basic ASCII record format can be read") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)
        .orderBy("A1")

      val expected = """[{"A1":"1","A2":"Tes","A3":"0123456789"},{"A1":"2","A2":"est2","A3":"SomeText"},{"A1":"3","A2":"None","A3":"Data¡3"},{"A1":"4","A2":"on","A3":"Data 4"}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test ASCII record format can be read") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D2")
        .option("ascii_charset", "UTF-8")
        .option("input_split_records", 2)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)
        .orderBy("A1")

      val expected = """[{"A1":"1","A2":"Tes","A3":"0123456789"},{"A1":"2","A2":"est2","A3":"SomeText"},{"A1":"3","A2":"None","A3":"Data¡3"},{"A1":"4","A2":"on","A3":"Data 4"}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test ASCII numerics empty values") {
    val copybook = """       05  A       PIC 9(2)V9(2). """

    val textFileContent = Seq("1234", "    ").mkString("\n")

    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("schema_retention_policy", "collapse_root")
        .option("improved_null_detection", "true")
        .load(tmpFileName)

      val expected = """[{"A":12.34},{}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test ASCII files with invalid record length (UTF-8)") {
    val copybook = """       05  A       PIC 9(2)V9(2). """

    val textFileContent = Seq("1234", "1", "23456").mkString("\n")

    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("minimum_record_length", 2)
        .option("maximum_record_length", 4)
        .load(tmpFileName)

      val expected = """[{"A":12.34}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test ASCII files with invalid record length (us-ascii)") {
    val copybook = """       05  A       PIC 9(2)V9(2). """

    val textFileContent = Seq("1234", "1", "23456").mkString("\n")

    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("ascii_charset", "us-ascii")
        .option("minimum_record_length", 2)
        .option("maximum_record_length", 4)
        .load(tmpFileName)

      val expected = """[{"A":12.34}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test ASCII from RDD with invalid record length") {
    val copybook = """       05  A       PIC 9(2)V9(2). """

    val textFileArray = Seq("1234", "1", "23456")

    val rdd = spark.sparkContext.parallelize(textFileArray).map(_.getBytes)

    val df = Cobrix.fromRdd(spark)
      .copybookContents(copybook)
      .option("pedantic", "true")
      .option("encoding", "ascii")
      .option("ascii_charset", "us-ascii")
      .option("minimum_record_length", "2")
      .option("maximum_record_length", "4")
      .load(rdd)

    val expected = """[{"A":12.34}]"""

    val actual = df.toJSON.collect().mkString("[", ",", "]")

    assertEqualsMultiline(actual, expected)
  }

  test("Test ASCII from Text RDD with invalid record length") {
    val copybook = """       05  A       PIC 9(2)V9(2). """

    val textFileArray = Seq("1234", "1", "23456")

    val rdd = spark.sparkContext.parallelize(textFileArray)

    val df = Cobrix.fromRdd(spark)
      .copybookContents(copybook)
      .option("pedantic", "true")
      .option("minimum_record_length", "2")
      .option("maximum_record_length", "4")
      .loadText(rdd)

    val expected = """[{"A":12.34}]"""

    val actual = df.toJSON.collect().mkString("[", ",", "]")

    assertEqualsMultiline(actual, expected)
  }

  test("Throw an exception if ASCII charset is specified for s EBCDIC file") {
    val ex = intercept[IllegalArgumentException] {
      spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("encoding", "ebcdic")
        .option("ascii_charset", "us-ascii")
        .load("dummy")
    }

    assert(ex.getMessage.contains("Option 'ascii_charset' cannot be used when 'encoding = ebcdic'"))
  }

  test("Throw an exception if EBCDIC code page is specified for an ASCII file") {
    val ex = intercept[IllegalArgumentException] {
      spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("encoding", "ascii")
        .option("ebcdic_code_page", "cp037")
        .load("dummy")
    }

    assert(ex.getMessage.contains("Option 'ebcdic_code_page' cannot be used when 'encoding = ascii'"))
  }
}
