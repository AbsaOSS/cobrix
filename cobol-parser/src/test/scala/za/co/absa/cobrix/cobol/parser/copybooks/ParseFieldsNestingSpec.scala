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
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.testutils.SimpleComparisonBase

class ParseFieldsNestingSpec extends FunSuite with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  test("Test copybook parser handling of levels nesting") {
    val copybookWithCommentLines =
      """
      01  ROOT-GROUP.
          03  NESTED-PRIMITIVE-01  PIC 9(7)    COMP-3.
          03  NESTED-GRP-01.
            05  NESTED-NESTED-02   PIC X(7).
          03  FILL                 PIC X(07).
        02  FILLER_1.
          03  NUMERIC-FIELD-01     PIC S9(04)  COMP.
      """

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 ROOT_GROUP                                          1      1     20     20
        |  3 NESTED_PRIMITIVE_01                               2      1      4      4
        |  3 NESTED_GRP_01                                     3      5     11      7
        |    5 NESTED_NESTED_02                                4      5     11      7
        |  3 FILL                                              5     12     18      7
        |  2 FILLER_1                                          6     19     20      2
        |    3 NUMERIC_FIELD_01                                7     19     20      2"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookWithCommentLines)
    val layout = copybook.generateRecordLayoutPositions()

    assertEqualsMultiline(layout, expectedLayout)
  }

  test("Test copybook parser doesn't allow nesting leaf statements") {
    val copybookWithCommentLines =
      """
      01  ROOT-GROUP.
          03  NESTED-PRIMITIVE-01  PIC 9(7)    COMP-3.
          03  NESTED-GRP-01.
            05  NESTED-NESTED-02   PIC X(7).
          03  FILL                 PIC X(07).
        02  FILLER_1               PIC XX.
          03  NUMERIC-FIELD-01     PIC S9(04)  COMP.
      """

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copybookWithCommentLines)
    }
    assert(syntaxErrorException.lineNumber == 7)
    assert(syntaxErrorException.msg.contains("The field is a leaf element and cannot contain nested fields."))
  }
}
