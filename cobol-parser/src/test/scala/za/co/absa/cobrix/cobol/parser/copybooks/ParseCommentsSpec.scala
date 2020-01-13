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

class ParseCommentsSpec extends FunSuite {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val expectedLayout =
    """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
      |
      |GRP_01                                                       1     11     11
      |  3 FIELD1                                            1      1      1      1
      |  3 FIELD2                                            2      2     11     10"""
      .stripMargin.replace("\r\n", "\n")

  test("Test copybook parser handles comment lines") {
    val copybookWithCommentLines =
      """
      ******************************************************************
      01  GRP_01.
         03 FIELD1     PIC X(1).
         03 FIELD2     PIC X(10).
    """

    val copybook = CopybookParser.parseTree(copybookWithCommentLines)
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

  test("Test copybook parser handles comments at the beginning of the lines") {
    val copybookWithStartComments =
      """
01234501  GRP_01.
000001   03 FIELD1     PIC X(1).
000002   03 FIELD2     PIC X(10).
    """

    val copybook = CopybookParser.parseTree(copybookWithStartComments)
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

  test("Test copybook parser handles comments at the end of the lines") {
    val copybookWithEndComments =
      """
      01  GRP_01.                                                       12345
         03 FIELD1     PIC X(1).                                        23456
         03 FIELD2     PIC X(10).                                       34567
    """

    val copybook = CopybookParser.parseTree(copybookWithEndComments)
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

  test("Test copybook parser handles comments everywhere when appropriate") {
    val copybookWithMoreComments =
      """
      ******************************************************************
01234501  GRP_01.                                                       12345
000001   03 FIELD1     PIC X(1).                                        ABCDE
000002   03 FIELD2     PIC X(10).                                       34567
      ******************************************************************
*****************************************************************************
    """

    val copybook = CopybookParser.parseTree(copybookWithMoreComments)
    val layout = copybook.generateRecordLayoutPositions()

    assert(layout == expectedLayout)
  }

}
