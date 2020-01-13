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
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser

class ParseFieldNamesSpec extends FunSuite {

  private val logger = LoggerFactory.getLogger(this.getClass)

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
        |GRP_01_02                                                    1     11     11
        |  3 FIELD_1                                           1      1      1      1
        |  3 FIELD_2                                           2      2     11     10
        |GRP_01_02                                                   12     22     11
        |  3 FIELD_1                                           3     12     12      1
        |  3 FIELD_2                                           4     13     22     10
        |GRP0102                                                     23     33     11
        |  3 FIELD1                                            5     23     23      1
        |  3 FIELD2                                            6     24     33     10
        |SOMETHING_SOMETHING_DATE_NUM                                34     44     11
        |  3 FIELD1                                            7     34     34      1
        |  3 FIELD2                                            8     35     44     10"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookWithCommentLines)
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }
}
