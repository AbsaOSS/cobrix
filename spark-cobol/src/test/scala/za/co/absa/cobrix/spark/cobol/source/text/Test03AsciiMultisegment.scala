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

import org.apache.spark.sql.DataFrame
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test03AsciiMultisegment extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  RECORD.
           05  T          PIC X(1).
           05  R1.
             10  A2       PIC X(5).
             10  A3       PIC X(10).
           05  R2 REDEFINES R1.
             10  B1       PIC X(5).
             10  B2       PIC X(5).
    """

  "Multisegment ASCII files should be parsed" when {
    "Records perfectly match the schema" in {
      val textFileContent: String =
        Seq(
          "1Tes  0123456789",
          "2Test 01234",
          "1None Data  3   ",
          "2 on  Data ").mkString("\n")

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = getDataFrame(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  { "T": "1", "R1": { "A2": "Tes", "A3": "0123456789" } },
            |  { "T": "2", "R2": { "B1": "Test", "B2": "01234" } },
            |  { "T": "1", "R1": { "A2": "None", "A3": "Data  3" } },
            |  { "T": "2", "R2": { "B1": "on", "B2": "Data" } }
            |]""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "segment filter is supported" in {
      val textFileContent: String =
        Seq(
          "1Tes  0123456789",
          "2Test 01234",
          "1None Data  3   ",
          "2 on  Data ").mkString("\n")

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("segment_field", "T")
          .option("segment_filter", "2")
          .load(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  { "T": "2", "R1": {"A2": "Test", "A3":"01234"}, "R2": {"B1": "Test", "B2": "01234"} },
            |  { "T": "2", "R1": {"A2": "on", "A3": "Data"}, "R2": {"B1": "on", "B2": "Data"} }
            |]""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "segment filter with record offset is supported" in {
      val textFileContent: String =
        Seq(
          "==1Tes  0123456789",
          "==2Test 01234",
          "==1None Data  3   ",
          "==2 on  Data ").mkString("\n")

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("segment_field", "T")
          .option("segment_filter", "2")
          .option("record_start_offset", 2)
          .load(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  { "T": "2", "R1": {"A2": "Test", "A3":"01234"}, "R2": {"B1": "Test", "B2": "01234"} },
            |  { "T": "2", "R1": {"A2": "on", "A3": "Data"}, "R2": {"B1": "on", "B2": "Data"} }
            |]""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "segment filter with one byte segments" in {
      val textFileContent: String = "1\n2\n1\n2"

      val copybook =
        """       01  RECORD.
                 05  T          PIC X(1).
          """

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("segment_field", "T")
          .option("segment_filter", "2")
          .load(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  { "T": "2" },
            |  { "T": "2" }
            |]""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "Records are separated by CRLF" in {
      val textFileContent: String =
        Seq(
          "1Tes  0123456789",
          "2Test 01234",
          "1None Data  3   ",
          "2 on  Data ").mkString("\r\n")

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = getDataFrame(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  { "T": "1", "R1": { "A2": "Tes", "A3": "0123456789" } },
            |  { "T": "2", "R2": { "B1": "Test", "B2": "01234" } },
            |  { "T": "1", "R1": { "A2": "None", "A3": "Data  3" } },
            |  { "T": "2", "R2": { "B1": "on", "B2": "Data" } }
            |]""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "Records are too short or too long" in {
      val textFileContent: String =
        Seq(
          "1Tes  0123456",
          "2Test 01234567",
          "1None Data   3",
          "2 on  Data 411111111",
          "2222222222").mkString("\r\n")

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = getDataFrame(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  { "T": "1", "R1": { "A2": "Tes", "A3": "0123456" } },
            |  { "T": "2", "R2": { "B1": "Test", "B2": "01234" } },
            |  { "T": "1", "R1": { "A2": "None", "A3": "Data   3" } },
            |  { "T": "2", "R2": { "B1": "on", "B2": "Data" } },
            |  { "T": "2", "R2": { "B1": "22222", "B2": "2222" } }
            |]""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

    "Support hierarchical data" in {
      val textFileContent: String =
        Seq(
          "1Root10123456789",
          "2Chld101234",
          "2Chld2abcde",
          "1Root2AbCdE",
          "2Chld31").mkString("\n")

      withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("schema_retention_policy", "collapse_root")
          .option("segment_field", "T")
          .option("redefine-segment-id-map:00", "R1 => 1")
          .option("redefine-segment-id-map:01", "R2 => 2")
          .option("segment-children:1", "R1 => R2")
          .load(tmpFileName)

        val expected = removeWhiteSpace(
          """[
            |  {
            |    "T": "1",
            |    "R1": {
            |      "A2": "Root1",
            |      "A3": "0123456789",
            |      "R2": [
            |        { "B1": "Chld1", "B2": "01234" },
            |        { "B1": "Chld2", "B2": "abcde" }
            |      ]
            |    }
            |  },
            |  {
            |    "T": "1",
            |    "R1": {
            |      "A2": "Root2",
            |      "A3": "AbCdE",
            |      "R2": [{ "B1": "Chld3", "B2": "1" }] }
            |  }
            |]
            |""".stripMargin)

        val actual = removeWhiteSpace(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actual, expected)
      }
    }

  }

  def getDataFrame(tmpFileName: String): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("pedantic", "true")
      .option("record_format", "D")
      .option("segment_field", "T")
      .option("redefine-segment-id-map:00", "R1 => 1")
      .option("redefine-segment-id-map:01", "R2 => 2")
      .load(tmpFileName)
  }

}
