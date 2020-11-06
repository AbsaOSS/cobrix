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
    val copybookWithRedefined: String = """        01  TRANSDATA.
                                          |           10 CURRENCY          PIC X(3).
                                          |           10 field1 PIC X(2).
                                          |           10 field2 redefines field1 PIC X.
                                          |""".stripMargin.replace("\r\n", "\n")

    val copybook = CopybookParser.parseTree(copybookWithRedefined, debugFieldsPolicy = HexValue)
    val attributes: Set[Statement] = copybook.ast.children(0).asInstanceOf[Group].children.toSet
    val expectedAttributes: Set[String] = Set("CURRENCY","CURRENCY_debug",
                                         "field1", "field1_debug",
                                         "field2","field2_debug")
    var actualAttributes: Set[String] = Set()
    attributes.foreach(i => {
      actualAttributes += i.name
    })

    assert(attributes.size == 6)
    assert(expectedAttributes.equals(actualAttributes))
  }
}
