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
import za.co.absa.cobrix.cobol.testutils.SimpleComparisonBase

class FlatCopybooksSpec extends AnyFunSuite with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  test("Flat copybooks should be parsed normally") {
    val copyBookContents: String =
      s"""        01  F1    PIC X(10).
         |        01  F2    PIC 9(4).
         |        01  F3    PIC S9(6).
         |        01  G1.
         |          03  F4  PIC 9(4).
         |""".stripMargin

    val parsed = CopybookParser.parseTree(copyBookContents)

    val actualLayout = parsed.generateRecordLayoutPositions()

    assertEqualsMultiline(actualLayout,
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |1 F1                                                  1      1     10     10
        |1 F2                                                  2     11     14      4
        |1 F3                                                  3     15     20      6
        |1 G1                                                  4     21     24      4
        |  3 F4                                                5     21     24      4"""
        .stripMargin.replace("\r\n", "\n"))
  }
}
