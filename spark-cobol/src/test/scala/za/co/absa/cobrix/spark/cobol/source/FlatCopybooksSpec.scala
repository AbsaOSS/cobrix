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

import org.scalatest.WordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.{SparkUtils, TestUtils}

import java.nio.charset.StandardCharsets

class FlatCopybooksSpec extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  "flat copybooks" should {
    val copybook =
      """       01 N     PIC X(1).
                01 V     PIC X(2).
                01 G.
                  03 A   PIC X(1).
    """

    val binFileContents: Array[Byte] = Array[Byte](
      0xF0.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
      0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte,
      0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte
    )

    val expected =
      """[ {
        |  "N" : "0",
        |  "V" : "12",
        |  "G" : {
        |    "A" : "3"
        |  }
        |}, {
        |  "N" : "4",
        |  "V" : "56",
        |  "G" : {
        |    "A" : "7"
        |  }
        |}, {
        |  "N" : "1",
        |  "V" : "23",
        |  "G" : {
        |    "A" : "4"
        |  }
        |} ]""".stripMargin.replace("\r\n", "\n")

    "be read normally for 'collapse_root'" in {
      withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

        assertEqualsMultiline(actual, expected)
      }
    }

    "be read normally for 'keep_original'" in {
      withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("schema_retention_policy", "keep_original")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

        assertEqualsMultiline(actual, expected)
      }
    }
  }

  "flat hierarchical copybooks" should {
    val copybook =
      """       01 N     PIC X(1).
                01 S1.
                  03  A   PIC 9(1).
                01 S2 REDEFINES S1.
                  03  B   PIC X(2).
    """

    val textData =
      """12
        |2A1
        |2A2
        |2A3
        |18
        |2B1
        |19
        |""".stripMargin

    val expected =
      """[ {
        |  "N" : "1",
        |  "S1" : {
        |    "A" : 2,
        |    "S2" : [ {
        |      "B" : "A1"
        |    }, {
        |      "B" : "A2"
        |    }, {
        |      "B" : "A3"
        |    } ]
        |  }
        |}, {
        |  "N" : "1",
        |  "S1" : {
        |    "A" : 8,
        |    "S2" : [ {
        |      "B" : "B1"
        |    } ]
        |  }
        |}, {
        |  "N" : "1",
        |  "S1" : {
        |    "A" : 9,
        |    "S2" : [ ]
        |  }
        |} ]
        |""".stripMargin.replace("\r\n", "\n")

    "be read normally for 'collapse_root'" in {
      withTempTextFile("text", ".dat", StandardCharsets.UTF_8, textData) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("schema_retention_policy", "collapse_root")
          .option("segment_field", "N")
          .option("redefine_segment_id_map:1", "S1 => 1")
          .option("redefine-segment-id-map:2", "S2 => 2")
          .option("segment-children:1", "S1 => S2")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

        assertEqualsMultiline(actual, expected)
      }
    }

    "be read normally for 'keep_original'" in {
      withTempTextFile("text", ".dat", StandardCharsets.UTF_8, textData) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("schema_retention_policy", "keep_original")
          .option("segment_field", "N")
          .option("redefine_segment_id_map:1", "S1 => 1")
          .option("redefine-segment-id-map:2", "S2 => 2")
          .option("segment-children:1", "S1 => S2")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

        assertEqualsMultiline(actual, expected)
      }
    }
  }
}
