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

import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.testutils.SimpleComparisonBase

class ParseCopybookFeaturesSpec extends AnyFunSuite with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybookFillers =
    """        01  RECORD.
      |            05  FILLER           PIC X(1).
      |            05  COMPANY_PREFIX   PIC X(3).
      |            05  FIELD1.
      |              07 FILLER OCCURS 12 TIMES.
      |               10 CHILD1 PIC S9(7) COMP-3.
      |               10 CHILD2 PIC S99V99999 COMP-3.
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

  test("Test copybooks containing COMP-3U Cobrix extension data type") {
    val copybookContents =
      """
      01  ROOT-GROUP.
          05  NUM1 PIC X(4)    COMP-3U.
          05  NUM2 PIC 9(4)V99 COMP-3U.
      """

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 ROOT_GROUP                                          1      1      7      7
        |  5 NUM1                                              2      1      4      4
        |  5 NUM2                                              3      5      7      3"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookContents)
    val layout = copybook.generateRecordLayoutPositions()

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test throwning an exception when a USAGE clause is unexpected for a PIC") {
    val copybookContents =
      """
      01  ROOT-GROUP.
          05  NUM PIC X(4)    COMP-3.
      """

    val ex = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copybookContents)
    }

    assert(ex.msg.contains("The field should be numeric"))
  }

  test("Test parseSimple() not dropping fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, dropGroupFillers = false, dropValueFillers = false, dropFillersFromAst = false)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1    156    156
        |  5 FILLER_P1                                         2      1      1      1
        |  5 COMPANY_PREFIX                                    3      2      4      3
        |  5 FIELD1                                            4      5    100     96
        |    7 FILLER_1                         []             5      5    100     96
        |      10 CHILD1                                       6      5      8      4
        |      10 CHILD2                                       7      9     12      4
        |  5 FILLER_P2                                         8    101    101      1
        |  5 FILLER_P3                                         9    102    102      1
        |  5 COMPANY_NAME                       r             10    103    111      9
        |  5 FILLER_2                           R             11    103    111      9
        |    10 STR1                                          12    103    107      5
        |    10 STR2                                          13    108    109      2
        |    10 FILLER_P4                                     14    110    110      1
        |  5 ADDRESS                            r             15    112    141     30
        |  5 FILLER_3                           R             16    112    141     30
        |    10 STR4                                          17    112    121     10
        |    10 FILLER_P5                                     18    122    141     20
        |  5 FILL_FIELD                         r             19    142    148      7
        |    10 FILLER_P6                                     20    142    146      5
        |    10 FILLER_P7                                     21    147    148      2
        |  5 CONTACT_PERSON                     R             22    142    148      7
        |    10 FIRST_NAME                                    23    142    147      6
        |  5 AMOUNT                                           24    149    156      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
    assert(!copybook.ast.children(0).asInstanceOf[Group].children(2).isFiller)
  }

  test("Test parseSimple() drop value fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, dropGroupFillers = false, dropValueFillers = true, dropFillersFromAst = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1    156    156
        |  5 COMPANY_PREFIX                                    2      2      4      3
        |  5 FIELD1                                            3      5    100     96
        |    7 FILLER_1                         []             4      5    100     96
        |      10 CHILD1                                       5      5      8      4
        |      10 CHILD2                                       6      9     12      4
        |  5 COMPANY_NAME                       r              7    103    111      9
        |  5 FILLER_2                           R              8    103    111      9
        |    10 STR1                                           9    103    107      5
        |    10 STR2                                          10    108    109      2
        |  5 ADDRESS                            r             11    112    141     30
        |  5 FILLER_3                           R             12    112    141     30
        |    10 STR4                                          13    112    121     10
        |  5 CONTACT_PERSON                     R             14    142    148      7
        |    10 FIRST_NAME                                    15    142    147      6
        |  5 AMOUNT                                           16    149    156      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
    assert(!copybook.ast.children(0).asInstanceOf[Group].children(2).isFiller)
  }

  test("Test parseSimple() drop group fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, dropGroupFillers = true, dropValueFillers = false, dropFillersFromAst = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1    156    156
        |  5 FILLER_P1                                         2      1      1      1
        |  5 COMPANY_PREFIX                                    3      2      4      3
        |  5 FILLER_P2                                         4    101    101      1
        |  5 FILLER_P3                                         5    102    102      1
        |  5 COMPANY_NAME                       r              6    103    111      9
        |  5 ADDRESS                            r              7    112    141     30
        |  5 FILL_FIELD                         r              8    142    148      7
        |    10 FILLER_P6                                      9    142    146      5
        |    10 FILLER_P7                                     10    147    148      2
        |  5 CONTACT_PERSON                     R             11    142    148      7
        |    10 FIRST_NAME                                    12    142    147      6
        |  5 AMOUNT                                           13    149    156      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test parseSimple() drop all fillers") {
    val copybook = CopybookParser.parseSimple(copybookFillers, dropGroupFillers = true, dropValueFillers = true, dropFillersFromAst = true)
    val layout = copybook.generateRecordLayoutPositions()

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 RECORD                                              1      1    156    156
        |  5 COMPANY_PREFIX                                    2      2      4      3
        |  5 COMPANY_NAME                       r              3    103    111      9
        |  5 ADDRESS                            r              4    112    141     30
        |  5 CONTACT_PERSON                     R              5    142    148      7
        |    10 FIRST_NAME                                     6    142    147      6
        |  5 AMOUNT                                            7    149    156      8
        |"""
        .stripMargin.replace("\r\n", "\n")

    assertEqualsMultiline(layout, expectedLayout)
  }
}
