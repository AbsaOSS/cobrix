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

class ParseCopybooksFeauresSpec extends FunSuite with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

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
}
