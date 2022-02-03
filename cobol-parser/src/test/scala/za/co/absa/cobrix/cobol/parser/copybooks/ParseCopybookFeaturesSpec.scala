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
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.testutils.SimpleComparisonBase

class ParseCopybookFeaturesSpec extends FunSuite with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybookFillers =
    """        01  RECORD.
      |            05  FILLER           PIC X(1).
      |            05  COMPANY_PREFIX   PIC X(3).
      |            05  FILLER           PIC X(1).
      |            05  FILLER           PIC X(1).
      |            05  COMPANY_NAME     PIC X(9).
      |            05  FILLER REDEFINES COMPANY_NAME.
      |               10   STR1         PIC X(5).
      |               10   STR2         PIC X(2).
      |               10   FILLER       PIC X(1).
      |            05  ADDRESS          PIC X(25).
      |            05  FILLER REDEFINES ADDRESS.
      |               10   STR4         PIC X(10).
      |               10   FILLER       PIC X(20).
      |            05  FILL_FIELD.
      |               10   FILLER       PIC X(5).
      |               10   FILLER       PIC X(2).
      |            05  CONTACT_PERSON REDEFINES FILL_FIELD.
      |               10  FIRST_NAME    PIC X(6).
      |            05  AMOUNT            PIC S9(09)V99  BINARY.
      |""".stripMargin

  test("Test copybooks with indexed by clauses with multiple indexes separated by comma") {
    val copybookContents =
      """
      01  ROOT-GROUP.
          05  FLD   OCCURS 7 TIMES
                    INDEXED BY A1,
                               A2.
              10  B1  PIC X(1).
      """

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 ROOT_GROUP                                          1      1      7      7
        |  5 FLD                                []             2      1      7      7
        |    10 B1                                             3      1      1      1"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookContents)
    val layout = copybook.generateRecordLayoutPositions()

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test copybooks with indexed by clauses with multiple indexes separated by EOL") {
    val copybookContents =
      """
      01  ROOT-GROUP.
          05  FLD   OCCURS 7 TIMES
                    INDEXED BY A1
                               A2.
              10  B1  PIC X(1).
      """

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 ROOT_GROUP                                          1      1      7      7
        |  5 FLD                                []             2      1      7      7
        |    10 B1                                             3      1      1      1"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookContents)
    val layout = copybook.generateRecordLayoutPositions()

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test copybooks containing identifiers with '@'") {
    val copybookContents =
      """
      01  ROOT-GROUP.
          05  F@TEST PIC X(1).
      """

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 ROOT_GROUP                                          1      1      1      1
        |  5 F@TEST                                            2      1      1      1"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookContents)
    val layout = copybook.generateRecordLayoutPositions()

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test parseSimple() not dropping fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, giveUniqueNameToValueFillers = true, giveUniqueNameToGroupFillers = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1     60     60
        |  5 FILLER_P1                                         2      1      1      1
        |  5 COMPANY_PREFIX                                    3      2      4      3
        |  5 FILLER_P2                                         4      5      5      1
        |  5 FILLER_P3                                         5      6      6      1
        |  5 COMPANY_NAME                       r              6      7     15      9
        |  5 FILLER_1                           R              7      7     15      9
        |    10 STR1                                           8      7     11      5
        |    10 STR2                                           9     12     13      2
        |    10 FILLER_P4                                     10     14     14      1
        |  5 ADDRESS                            r             11     16     45     30
        |  5 FILLER_2                           R             12     16     45     30
        |    10 STR4                                          13     16     25     10
        |    10 FILLER_P5                                     14     26     45     20
        |  5 FILL_FIELD                         r             15     46     52      7
        |    10 FILLER_P6                                     16     46     50      5
        |    10 FILLER_P7                                     17     51     52      2
        |  5 CONTACT_PERSON                     R             18     46     52      7
        |    10 FIRST_NAME                                    19     46     51      6
        |  5 AMOUNT                                           20     53     60      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
  }
  test("Test parseSimple() drop value fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, giveUniqueNameToValueFillers = false, giveUniqueNameToGroupFillers = true, dropFillersFromAst = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1     60     60
        |  5 COMPANY_PREFIX                                    2      2      4      3
        |  5 COMPANY_NAME                       r              3      7     15      9
        |  5 FILLER_1                           R              4      7     15      9
        |    10 STR1                                           5      7     11      5
        |    10 STR2                                           6     12     13      2
        |  5 ADDRESS                            r              7     16     45     30
        |  5 FILLER_2                           R              8     16     45     30
        |    10 STR4                                           9     16     25     10
        |  5 CONTACT_PERSON                     R             10     46     52      7
        |    10 FIRST_NAME                                    11     46     51      6
        |  5 AMOUNT                                           12     53     60      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test parseSimple() drop group fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, giveUniqueNameToValueFillers = true, giveUniqueNameToGroupFillers = false, dropFillersFromAst = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1     60     60
        |  5 FILLER_P1                                         2      1      1      1
        |  5 COMPANY_PREFIX                                    3      2      4      3
        |  5 FILLER_P2                                         4      5      5      1
        |  5 FILLER_P3                                         5      6      6      1
        |  5 COMPANY_NAME                       r              6      7     15      9
        |  5 ADDRESS                            r              7     16     45     30
        |  5 FILL_FIELD                         r              8     46     52      7
        |    10 FILLER_P6                                      9     46     50      5
        |    10 FILLER_P7                                     10     51     52      2
        |  5 CONTACT_PERSON                     R             11     46     52      7
        |    10 FIRST_NAME                                    12     46     51      6
        |  5 AMOUNT                                           13     53     60      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test parseSimple() drop all fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, giveUniqueNameToValueFillers = false, giveUniqueNameToGroupFillers = false, dropFillersFromAst = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1     60     60
        |  5 COMPANY_PREFIX                                    2      2      4      3
        |  5 COMPANY_NAME                       r              3      7     15      9
        |  5 ADDRESS                            r              4     16     45     30
        |  5 CONTACT_PERSON                     R              5     46     52      7
        |    10 FIRST_NAME                                     6     46     51      6
        |  5 AMOUNT                                            7     53     60      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
  }
}
