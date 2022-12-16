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

import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test01AsciiTextFiles extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
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

  test("Test EOL separated text files can be read") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_text", "true")
        .option("encoding", "ascii")
        .option("ascii_charset", "US-ASCII")
        .option("input_split_records", 2)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

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
        .option("record_format", "D2")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val expected = """[{"A1":"1","A2":"Tes","A3":"0123456789"},{"A1":"2","A2":"est2","A3":"SomeText"},{"A1":"3","A2":"None","A3":"Data  3"},{"A1":"4","A2":"on","A3":"Data 4"}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

  test("Test  ASCII record format can be read") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("ascii_charset", "UTF-8")
        .option("input_split_records", 2)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

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

}
