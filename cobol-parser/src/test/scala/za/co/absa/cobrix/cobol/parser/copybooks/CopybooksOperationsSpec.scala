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
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}


class CopybooksOperationsSpec extends FunSuite {

  test("Test drop root from copybook") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |""".stripMargin.replace("\r\n", "\n")
    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybookDR1 = copybook1.dropRoot()

    assert(copybook1.getRecordSize == 30)
    assert(copybookDR1.getRecordSize == 30)

    assert(copybook1.generateRecordLayoutPositions ==
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
    assert(copybookDR1.generateRecordLayoutPositions ==
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |GROUP_1                                                      1     30     30
        |  6 FIELD_1                                           1      1     10     10
        |  6 FILLER                                            2     11     15      5
        |  6 GROUP_2                                           5     16     30     15
        |    10 NESTED_FIELD_1                                 4     16     25     10
        |    10 FILLER                                         5     26     30      5"""
        .stripMargin.replace("\r\n", "\n"))

    val exception = intercept[RuntimeException] {
      copybookDR1.dropRoot()
    }
    assert(exception.getMessage.contains("All elements of the root element must be record groups"))
  }


  test("Test drop root from copybook fails") {
    val exception1 = intercept[RuntimeException] {
      new Copybook(Group.root.copy()(None)).dropRoot()
    }
    assert(exception1.getMessage.contains("Cannot drop the root of an empty copybook"))

    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |        01  RECORD-COPYBOOK-2.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |""".stripMargin.replace("\r\n", "\n")
    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val exception2 = intercept[RuntimeException] {
       copybook1.dropRoot()
    }
    assert(exception2.getMessage.contains("Cannot drop the root of a copybook with more than one root segment"))
  }


  test("Test restrictTo statement") {
    val copyBookContents1: String =
      """        01  RECORD-COPYBOOK-1.
        |           05  GROUP-1A.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2A.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |        01  RECORD-COPYBOOK-2.
        |           05  GROUP-1B.
        |              06  FIELD-1            PIC X(20).
        |              06  FILLER             PIC X(10).
        |              06  GROUP-2B.
        |                 10  NESTED-FIELD-1  PIC 9(20).
        |                 10  FILLER          PIC 9(10).
        |""".stripMargin.replace("\r\n", "\n")
    val copybook1 = CopybookParser.parseTree(copyBookContents1)
    val copybookR1 = copybook1.restrictTo("GROUP-1A")
    val copybookR2 = copybook1.restrictTo("GROUP-1B")

    assert(copybook1.getRecordSize == 90)
    assert(copybookR1.getRecordSize == 30)
    assert(copybookR2.getRecordSize == 60)

    assert(copybookR1.generateRecordLayoutPositions ==
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |GROUP_1A                                                     1     30     30
        |  6 FIELD_1                                           1      1     10     10
        |  6 FILLER                                            2     11     15      5
        |  6 GROUP_2A                                          5     16     30     15
        |    10 NESTED_FIELD_1                                 4     16     25     10
        |    10 FILLER                                         5     26     30      5"""
        .stripMargin.replace("\r\n", "\n"))
    assert(copybookR2.generateRecordLayoutPositions ==
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |GROUP_1B                                                     1     60     60
        |  6 FIELD_1                                           1      1     20     20
        |  6 FILLER                                            2     21     30     10
        |  6 GROUP_2B                                          5     31     60     30
        |    10 NESTED_FIELD_1                                 4     31     50     20
        |    10 FILLER                                         5     51     60     10"""
        .stripMargin.replace("\r\n", "\n"))

    val exception1 = intercept[RuntimeException] {
      copybook1.restrictTo("GROUP-1A.FIELD-1")
    }
    assert(exception1.getMessage.contains("Can only restrict the copybook to a group element"))


  }

}
