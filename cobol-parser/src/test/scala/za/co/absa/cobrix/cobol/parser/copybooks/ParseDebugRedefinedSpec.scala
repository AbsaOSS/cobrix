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
import za.co.absa.cobrix.cobol.parser.ast.{Group, Statement}
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy.HexValue

class ParseDebugRedefinedSpec extends FunSuite {

  private val logger = LoggerFactory.getLogger(this.getClass)

  test("Test copybook parser generates debug columns for redefined fields") {
    val copybookWithRedefined: String =
      """        01  TRANSDATA.
        |           10 CURRENCY                PIC X(3).
        |           10 FIELD1                  PIC X(2).
        |           10 FIELD2 REDEFINES FIELD1 PIC X.
        |""".stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookWithRedefined, debugFieldsPolicy = HexValue)
    val fields: Set[Statement] = copybook.ast.children(0).asInstanceOf[Group].children.toSet
    val expectedFields: Set[String] =
      Set("CURRENCY", "CURRENCY_debug",
        "FIELD1", "FIELD1_debug",
        "FIELD2", "FIELD2_debug")

    var actualFields: Set[String] = Set()
    fields.foreach(i => {
      actualFields += i.name
    })

    assert(fields.size == 6)
    assert(expectedFields.equals(actualFields))
  }

  test("Test redefined and redefines flags are retained when adding debugging fields") {
    val copybookWithRedefined: String =
      """        01  TRANSDATA.
        |           10 CURRENCY                PIC X(3).
        |           10 FIELD1                  PIC X(2).
        |           10 FIELD2 REDEFINES FIELD1 PIC X.
        |           10 FIELD3 REDEFINES FIELD1 PIC X(4).
        |           10 FIELD4 REDEFINES FIELD2 PIC XX.
        |                                          |"""
        .stripMargin.replace("\r\n", "\n")

    val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |TRANSDATA                                                    1      7      7
        |  10 CURRENCY                          r              1      1      3      3
        |  10 CURRENCY_debug                    R              2      1      3      3
        |  10 FIELD1                            r              3      4      7      4
        |  10 FIELD1_debug                      rR             4      4      7      4
        |  10 FIELD2                            rR             5      4      7      4
        |  10 FIELD2_debug                      rR             6      4      7      4
        |  10 FIELD3                            rR             7      4      7      4
        |  10 FIELD3_debug                      rR             8      4      7      4
        |  10 FIELD4                            rR             9      4      7      4
        |  10 FIELD4_debug                      R             10      4      7      4"""
        .stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookWithRedefined, debugFieldsPolicy = HexValue)


    val actualLayout = copybook.generateRecordLayoutPositions()

    assert(actualLayout == expectedLayout)
  }
}
