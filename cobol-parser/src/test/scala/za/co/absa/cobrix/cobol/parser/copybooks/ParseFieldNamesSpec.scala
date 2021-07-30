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

class ParseFieldNamesSpec extends FunSuite with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  test("Test copybook parser handles comment lines") {
    val copybookWithCommentLines =
      """
      01  GRP-01-02.
         03 FIELD_1      PIC X(1).
         03 FIELD_2      PIC X(10).
      01  GRP_01-02.
         03 FIELD-1      PIC X(1).
         03 FIELD-2      PIC X(10).
      01  GRP:01:02.
         03 FIELD:1:     PIC X(1).
         03 FIELD:2:     PIC X(10).
      01  :SOMETHING:-SOMETHING-DATE-NUM.
         03 :FIELD:1     PIC X(1).
         03 :FIELD:2     PIC X(10).
      """

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 GRP_01_02                                           1      1     11     11
        |  3 FIELD_1                                           2      1      1      1
        |  3 FIELD_2                                           3      2     11     10
        |1 GRP_01_02                                           4     12     22     11
        |  3 FIELD_1                                           5     12     12      1
        |  3 FIELD_2                                           6     13     22     10
        |1 GRP0102                                             7     23     33     11
        |  3 FIELD1                                            8     23     23      1
        |  3 FIELD2                                            9     24     33     10
        |1 SOMETHING_SOMETHING_DATE_NUM                       10     34     44     11
        |  3 FIELD1                                           11     34     34      1
        |  3 FIELD2                                           12     35     44     10"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookWithCommentLines)
    val layout = copybook.generateRecordLayoutPositions()

    assertEqualsMultiline(layout, expectedLayout)
  }
}
