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

package za.co.absa.cobrix.spark.cobol.source.integration

import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.utils.JsonUtils
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

//noinspection NameBooleanParameters
class Test22HierarchicalOccursSpec extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val copybook =
    """      01 RECORD.
          02 SEG PIC X(1).
          02 SEG1.
            03 COUNT1 PIC 9(1).
            03 GROUP1 OCCURS 0 TO 2 TIMES DEPENDING ON COUNT1.
               04 INNER-COUNT1 PIC 9(1).
               04 INNER-GROUP1 OCCURS 0 TO 3 TIMES
                                DEPENDING ON INNER-COUNT1.
                  05 FIELD1 PIC X.
          02 SEG2 REDEFINES SEG1.
            03 COUNT2 PIC 9(1).
            03 GROUP2 OCCURS 0 TO 2 TIMES DEPENDING ON COUNT2.
               04 INNER-COUNT2 PIC 9(1).
               04 INNER-GROUP2 OCCURS 0 TO 3 TIMES
                                DEPENDING ON INNER-COUNT2.
                  05 FIELD2 PIC X.
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // Record 0
    0x00, 0x00, 0x02, 0x00, // RDW
    0xF1.toByte, // SegmentId = 1
    0xF0.toByte, // Count1 = 0
    // Record 1
    0x00, 0x00, 0x03, 0x00, // RDW
    0xF1.toByte, // SegmentId = 1
    0xF1.toByte, // Count1 = 1
    0xF0.toByte, // InnerCount = 0
    // Record 2
    0x00, 0x00, 0x04, 0x00, // RDW
    0xF1.toByte, // SegmentId = 1
    0xF1.toByte, // Count1 = 1
    0xF1.toByte, // InnerCount = 1
    0xC1.toByte, // InnerGroup = ['A']
    // Record 3
    0x00, 0x00, 0x05, 0x00, // RDW
    0xF1.toByte, // SegmentId = 1
    0xF1.toByte, // Count1 = 1
    0xF2.toByte, // InnerCount = 2
    0xC1.toByte, 0xC2.toByte, // InnerGroup = ['A', 'B']
    // Record 3
    0x00, 0x00, 0x08, 0x00, // RDW
    0xF1.toByte, // SegmentId = 1
    0xF2.toByte, // Count1 = 2
    0xF2.toByte, // InnerCount = 2
    0xC3.toByte, 0xC4.toByte, // InnerGroup = ['C', 'D']
    0xF2.toByte, // InnerCount = 2
    0xC5.toByte, 0xC6.toByte, // InnerGroup = ['E', 'F']
    // Record 4
    0x00, 0x00, 0x08, 0x00, // RDW
    0xF2.toByte, // SegmentId = 2
    0xF2.toByte, // Count1 = 2
    0xF2.toByte, // InnerCount = 2
    0xC7.toByte, 0xC8.toByte, // InnerGroup = ['G', 'H']
    0xF2.toByte, // InnerCount = 2
    0xC9.toByte, 0xD1.toByte // InnerGroup = ['I', 'J']
  )

  test("Test that variable length OCCURS in a hierarchical file are loaded correctly") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("is_record_sequence", "true")
        .option("schema_retention_policy", "collapse_root")
        .option("generate_record_id", "true")
        .option("variable_size_occurs", "true")
        .option("segment_field", "SEG")
        .option("redefine_segment_id_map:1", "SEG1 => 1")
        .option("redefine-segment-id-map:2", "SEG2 => 2")
        .option("segment-children:1", "SEG1 => SEG2")
        .load(tmpFileName)

      val expected =
        """[ {
          |  "File_Id" : 0,
          |  "Record_Id" : 1,
          |  "SEG" : "1",
          |  "SEG1" : {
          |    "COUNT1" : 0,
          |    "GROUP1" : [ ],
          |    "SEG2" : [ ]
          |  }
          |}, {
          |  "File_Id" : 0,
          |  "Record_Id" : 2,
          |  "SEG" : "1",
          |  "SEG1" : {
          |    "COUNT1" : 1,
          |    "GROUP1" : [ {
          |      "INNER_COUNT1" : 0,
          |      "INNER_GROUP1" : [ ]
          |    } ],
          |    "SEG2" : [ ]
          |  }
          |}, {
          |  "File_Id" : 0,
          |  "Record_Id" : 3,
          |  "SEG" : "1",
          |  "SEG1" : {
          |    "COUNT1" : 1,
          |    "GROUP1" : [ {
          |      "INNER_COUNT1" : 1,
          |      "INNER_GROUP1" : [ {
          |        "FIELD1" : "A"
          |      } ]
          |    } ],
          |    "SEG2" : [ ]
          |  }
          |}, {
          |  "File_Id" : 0,
          |  "Record_Id" : 4,
          |  "SEG" : "1",
          |  "SEG1" : {
          |    "COUNT1" : 1,
          |    "GROUP1" : [ {
          |      "INNER_COUNT1" : 2,
          |      "INNER_GROUP1" : [ {
          |        "FIELD1" : "A"
          |      }, {
          |        "FIELD1" : "B"
          |      } ]
          |    } ],
          |    "SEG2" : [ ]
          |  }
          |}, {
          |  "File_Id" : 0,
          |  "Record_Id" : 6,
          |  "SEG" : "1",
          |  "SEG1" : {
          |    "COUNT1" : 2,
          |    "GROUP1" : [ {
          |      "INNER_COUNT1" : 2,
          |      "INNER_GROUP1" : [ {
          |        "FIELD1" : "C"
          |      }, {
          |        "FIELD1" : "D"
          |      } ]
          |    }, {
          |      "INNER_COUNT1" : 2,
          |      "INNER_GROUP1" : [ {
          |        "FIELD1" : "E"
          |      }, {
          |        "FIELD1" : "F"
          |      } ]
          |    } ],
          |    "SEG2" : [ {
          |      "COUNT2" : 2,
          |      "GROUP2" : [ {
          |        "INNER_COUNT2" : 2,
          |        "INNER_GROUP2" : [ {
          |          "FIELD2" : "G"
          |        }, {
          |          "FIELD2" : "H"
          |        } ]
          |      }, {
          |        "INNER_COUNT2" : 2,
          |        "INNER_GROUP2" : [ {
          |          "FIELD2" : "I"
          |        }, {
          |          "FIELD2" : "J"
          |        } ]
          |      } ]
          |    } ]
          |  }
          |} ]""".stripMargin.replace("\r\n", "\n")

      val actual = JsonUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      assertEqualsMultiline(actual, expected)
    }

  }


}
