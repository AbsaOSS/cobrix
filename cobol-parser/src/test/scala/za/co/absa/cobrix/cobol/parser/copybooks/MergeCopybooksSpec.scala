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

package za.co.absa.cobrix.cobol.parser.copybooks

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}


class MergeCopybooksSpec extends FunSuite {

  test("Test merge copybooks") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |""".stripMargin
    val copyBookContents2: String =
      """        01  RECORD-COPYBOOK-2A.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |        01  RECORD-COPYBOOK-2B REDEFINES RECORD-COPYBOOK-2A.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |""".stripMargin
    val copyBookContents3: String =
      """        01  RECORD-COPYBOOK-3.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(30).
        |              06  FILLER             PIC X(15).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(30).
        |                 10  FILLER          PIC 9(15).
        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybook2 = CopybookParser.parseTree(copyBookContents2)
    val copybook3 = CopybookParser.parseTree(copyBookContents3)

    assert(copybook1.getRecordSize == 30)
    assert(copybook2.getRecordSize == 60)
    assert(copybook3.getRecordSize == 90)

    val copybook12 = Copybook.merge(List(copybook1, copybook2))
    val copybook123 = Copybook.merge(List(copybook1, copybook2, copybook3))
    val copybook321 = Copybook.merge(List(copybook3, copybook2, copybook1))
    assert(copybook12.getRecordSize == 60)
    assert(copybook123.getRecordSize == 90)
    assert(copybook321.getRecordSize == 90)

    assert(copybook123.generateRecordLayoutPositions ==
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |RECORD_COPYBOOK_1                                            1     90     90
        |  5 GROUP_1                                           6      1     30     30
        |    6 FIELD_1                                         2      1     10     10
        |    6 FILLER                                          3     11     15      5
        |    6 GROUP_2                                         6     16     30     15
        |      10 NESTED_FIELD_1                               5     16     25     10
        |      10 FILLER                                       6     26     30      5
        |RECORD_COPYBOOK_2A                                           1     90     90
        |  5 GROUP_1                                          12      1     60     60
        |    6 FIELD_1                                         8      1     20     20
        |    6 FILLER                                          9     21     30     10
        |    6 GROUP_2                                        12     31     60     30
        |      10 NESTED_FIELD_1                              11     31     50     20
        |      10 FILLER                                      12     51     60     10
        |RECORD_COPYBOOK_2B                                           1     90     90
        |  5 GROUP_1                                          18      1     60     60
        |    6 FIELD_1                                        14      1     20     20
        |    6 FILLER                                         15     21     30     10
        |    6 GROUP_2                                        18     31     60     30
        |      10 NESTED_FIELD_1                              17     31     50     20
        |      10 FILLER                                      18     51     60     10
        |RECORD_COPYBOOK_3                                            1     90     90
        |  5 GROUP_1                                          24      1     90     90
        |    6 FIELD_1                                        20      1     30     30
        |    6 FILLER                                         21     31     45     15
        |    6 GROUP_2                                        24     46     90     45
        |      10 NESTED_FIELD_1                              23     46     75     30
        |      10 FILLER                                      24     76     90     15"""
        .stripMargin.replace("\r\n", "\n"))
  }

  test("Test merge one copybook only") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents1)

    assert(copybook1.getRecordSize == 30)

    val copybook1M = Copybook.merge(List(copybook1))
    assert(copybook1M.getRecordSize == 30)

    assert(copybook1M.generateRecordLayoutPositions == copybook1.generateRecordLayoutPositions)
    assert(copybook1M.generateRecordLayoutPositions ==
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |RECORD_COPYBOOK_1                                            1     30     30
        |  5 GROUP_1                                           6      1     30     30
        |    6 FIELD_1                                         2      1     10     10
        |    6 FILLER                                          3     11     15      5
        |    6 GROUP_2                                         6     16     30     15
        |      10 NESTED_FIELD_1                               5     16     25     10
        |      10 FILLER                                       6     26     30      5"""
        .stripMargin.replace("\r\n", "\n"))
  }


  test("Test merge copybooks fail: differing levels") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |""".stripMargin
    val copyBookContents2: String =
      """        02  RECORD-COPYBOOK-2A.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybook2 = CopybookParser.parseTree(copyBookContents2)

    assert(copybook1.getRecordSize == 30)
    assert(copybook2.getRecordSize == 60)

    val exception = intercept[RuntimeException] {
      Copybook.merge(List(copybook1, copybook2))
    }
    assert(exception.getMessage.contains("Cannot merge copybooks with differing root levels"))
  }

  test("Test merge copybooks fail: repeated identifiers") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |""".stripMargin
    val copyBookContents2: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybook2 = CopybookParser.parseTree(copyBookContents2)

    assert(copybook1.getRecordSize == 30)
    assert(copybook2.getRecordSize == 60)

    val exception = intercept[RuntimeException] {
      Copybook.merge(List(copybook1, copybook2))
    }
    assert(exception.getMessage.contains("Cannot merge copybooks with repeated segment identifiers"))
  }

  test("Test merge copybooks fail: must redefine top segment") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |
        |""".stripMargin
    val copyBookContents2: String =
      """        01  RECORD-COPYBOOK-2.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |        01  RECORD-COPYBOOK-2B.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybook2 = CopybookParser.parseTree(copyBookContents2)

    assert(copybook1.getRecordSize == 30)
    assert(copybook2.getRecordSize == 120)

    val exception = intercept[RuntimeException] {
      Copybook.merge(List(copybook1, copybook2))
    }
    assert(exception.getMessage.contains("Copybook segments must redefine top segment"))
  }
}
