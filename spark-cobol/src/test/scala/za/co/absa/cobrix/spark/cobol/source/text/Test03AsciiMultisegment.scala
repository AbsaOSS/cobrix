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

class Test03AsciiMultisegment extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
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

  test("Test multisegment records parsing") {
    val textFileContent: String =
      Seq("1Tes  0123456789",
        "2Test 012345",
        "1None Data¡3    ",
        "2 on  Data 4").mkString("\n")

    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_text", "true")
        .option("encoding", "ascii")
        .option("is_record_sequence", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("segment_field", "T")
        .option("redefine-segment-id-map:00", "R1 => 1")
        .option("redefine-segment-id-map:01", "R2 => 2")
        .load(tmpFileName)

      val expected = """[{"T":"1","R1":{"A2":"Tes","A3":"0123456789"}},{"T":"2","R2":{"B1":"Test","B2":"01234"}},{"T":"1","R1":{"A2":"None","A3":"Data  3"}},{"T":""}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

}
