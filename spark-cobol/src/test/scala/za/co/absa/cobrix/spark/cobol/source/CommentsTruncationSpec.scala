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

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.policies.CommentPolicy
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class CommentsTruncationSpec extends FunSuite with SparkTestBase with BinaryFileFixture {

  private val logger = LoggerFactory.getLogger(this.getClass)

  val binFileContents: Array[Byte] = Array[Byte](
    // Record 0 (full)
    0x00, 0x00, 0x0B.toByte, 0x00,
    0xF0.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte,
    0xF8.toByte, 0xF9.toByte, 0xF0.toByte,
    // Record 1 (full with spaces)
    0x00, 0x00, 0x0B.toByte, 0x00,
    0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    0x40.toByte, 0x40.toByte, 0x40.toByte,
    // Record 2 (partial 1)
    0x00, 0x00, 0x0A.toByte, 0x00,
    0xF2.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
    0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    0x40.toByte, 0x40.toByte,
    // Record 3 (partial 2)
    0x00, 0x00, 0x04.toByte, 0x00,
    0xF3.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte,
    // Record 4 (partial 2)
    0x00, 0x00, 0x02.toByte, 0x00,
    0xF4.toByte, 0xF1.toByte,
    // Record 5 (partial 3 - tiny)
    0x00, 0x00, 0x01.toByte, 0x00,
    0xF5.toByte
  )

  private val copybookWithComments =
    """
      ******************************************************************
01234501  GRP_01.                                                       12345
000001   03 FIELD1     PIC X(1).                                        ABCDE
000002   03 FIELD2     PIC X(10).                                       34567
      ******************************************************************
*****************************************************************************
    """

  private val copybookWithTruncatedComments =
    """
      ********************************************
34501  GRP_01.                                    12345
001   03 FIELD1     PIC X(1).                     ABCDE
002   03 FIELD2     PIC X(10).                    34567
      ********************************************
    """

  val copybookWithNoCommentTruncation =
    """
******************************************************************
01  GRP_01.
   03              FIELD1                                           PIC X(1).
   03              FIELD2                                           PIC X(10).
******************************************************************
    """


  private val expectedLayout =
    """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
      |
      |GRP_01                                                       1     11     11
      |  3 FIELD1                                            1      1      1      1
      |  3 FIELD2                                            2      2     11     10"""
      .stripMargin.replace("\r\n", "\n")

  private val expectedSchema =
    """root
      | |-- FIELD1: string (nullable = true)
      | |-- FIELD2: string (nullable = true)
      |""".stripMargin.replace("\r\n", "\n")

  test("Test copybook comments are parsed with the default comment positions") {
    val copybook = CopybookParser.parseTree(copybookWithComments)
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

  test("Test copybook comments are parsed with an adjusted comment positions") {
    val copybook = CopybookParser.parseTree(copybookWithTruncatedComments,
      commentPolicy = CommentPolicy(truncateComments = true, 3, 50))
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

  test("Test copybook comments are parsed with no comment truncation") {
    val copybook = CopybookParser.parseTree(copybookWithNoCommentTruncation,
      commentPolicy = CommentPolicy(truncateComments = false))
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

  test("Test copybook are processed by spark-cobol properly with the default comment positions") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookWithComments)
        .option("is_record_sequence", "true")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val actualSchema = df.schema.treeString

      assert(actualSchema == expectedSchema)
    }
  }

  test("Test copybook are processed by spark-cobol properly with an adjusted comment positions") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookWithTruncatedComments)
        .option("is_record_sequence", "true")
        .option("comments_lbound", 3)
        .option("comments_ubound", 50)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val actualSchema = df.schema.treeString

      assert(actualSchema == expectedSchema)
    }
  }

  test("Test copybook are processed by spark-cobol properly with no comment truncation") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookWithNoCommentTruncation)
        .option("is_record_sequence", "true")
        .option("truncate_comments", "false")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val actualSchema = df.schema.treeString

      assert(actualSchema == expectedSchema)
    }
  }

  test("Test an exception is thrown when 'comments_truncated' is used with 'comments_lbound'") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookWithTruncatedComments)
          .option("is_record_sequence", "true")
          .option("comments_lbound", 3)
          .option("truncate_comments", "false")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)
      }
      assert(ex.getMessage.contains("the following parameters cannot be used"))
    }
  }

  test("Test an exception is thrown when 'comments_truncated' is used with 'comments_ubound'") {
    withTempBinFile("binary", ".dat", binFileContents) { tmpFileName =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookWithTruncatedComments)
          .option("is_record_sequence", "true")
          .option("comments_ubound", 50)
          .option("truncate_comments", "false")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)
      }
      assert(ex.getMessage.contains("the following parameters cannot be used"))
    }
  }

}
