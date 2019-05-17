/*
 * Copyright 2018-2019 ABSA Group Limited
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
        |                 10  FILLER          PIC 9(10)
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
        |MERGE_RECORD_AREA                                            1     90     90
        |  2 DATA                                              1      1     90     90
        |RECORD_COPYBOOK_1                                            1     90     90
        |  5 GROUP_1                                           7      1     30     30
        |    6 FIELD_1                                         3      1     10     10
        |    6 FILLER                                          4     11     15      5
        |    6 GROUP_2                                         7     16     30     15
        |      10 NESTED_FIELD_1                               6     16     25     10
        |      10 FILLER                                       7     26     30      5
        |RECORD_COPYBOOK_2A                                           1     90     90
        |  5 GROUP_1                                          13      1     60     60
        |    6 FIELD_1                                         9      1     20     20
        |    6 FILLER                                         10     21     30     10
        |    6 GROUP_2                                        13     31     60     30
        |      10 NESTED_FIELD_1                              12     31     50     20
        |      10 FILLER                                      13     51     60     10
        |RECORD_COPYBOOK_2B                                           1     90     90
        |  5 GROUP_1                                          19      1     60     60
        |    6 FIELD_1                                        15      1     20     20
        |    6 FILLER                                         16     21     30     10
        |    6 GROUP_2                                        19     31     60     30
        |      10 NESTED_FIELD_1                              18     31     50     20
        |      10 FILLER                                      19     51     60     10
        |RECORD_COPYBOOK_3                                            1     90     90
        |  5 GROUP_1                                          25      1     90     90
        |    6 FIELD_1                                        21      1     30     30
        |    6 FILLER                                         22     31     45     15
        |    6 GROUP_2                                        25     46     90     45
        |      10 NESTED_FIELD_1                              24     46     75     30
        |      10 FILLER                                      25     76     90     15""".stripMargin)
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
        |                 10  FILLER          PIC 9(10)
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

  test("Test merge copybooks: name clash with merge area") {
    val copyBookContents1: String =
      """        01  MERGE-RECORD-AREA.
        |           02  FIELD-1            PIC X(10).
        |""".stripMargin
    val copyBookContents2: String =
      """        01  MERGE-RECORD-AREA-0.
        |           02  FIELD-1            PIC X(20).
        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybook2 = CopybookParser.parseTree(copyBookContents2)

    assert(copybook1.getRecordSize == 10)
    assert(copybook2.getRecordSize == 20)

    val copybook12 = Copybook.merge(List(copybook1, copybook2))
    assert(copybook12.getRecordSize == 20)

    assert(copybook12.generateRecordLayoutPositions ==
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |MERGE_RECORD_AREA_1                                          1     20     20
        |  2 DATA                                              1      1     20     20
        |MERGE_RECORD_AREA                                            1     20     20
        |  2 FIELD_1                                           2      1     10     10
        |MERGE_RECORD_AREA_0                                          1     20     20
        |  2 FIELD_1                                           3      1     20     20""".stripMargin)
  }


}
