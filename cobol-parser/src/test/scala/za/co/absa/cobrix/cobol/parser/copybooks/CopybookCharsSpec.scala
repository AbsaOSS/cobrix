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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Group

class CopybookCharsSpec extends FunSuite {
  test("Test a copybook that has unbreakable spaces (0xA0) and tabs") {
    val c = '\u00a0'
    val t = 0x09.toByte.toChar
    val copyBookContents: String =
      s"""        01  RECORD.
         |            05  F1$c$c$c$c${c}PIC X(10).
         |            05  F2$c$c$c  PIC 9(10).
         |            05 ${c}F3$c$c$c  PIC 9(10).
         |           ${c}05$c${c}F4$c  ${c}PIC 9(10).
         |           ${t}05$t${t}F5$t  ${t}PIC 9(10).
         |""".stripMargin

    val parsed = CopybookParser.parseTree(copyBookContents)

    val root = parsed.ast.children.head.asInstanceOf[Group]

    val field1 = root.children.head.name
    val field2 = root.children(1).name
    val field3 = root.children(2).name
    val field4 = root.children(3).name
    val field5 = root.children(4).name

    assert(field1 == "F1")
    assert(field2 == "F2")
    assert(field3 == "F3")
    assert(field4 == "F4")
    assert(field5 == "F5")
  }

  test("Test a copybook that have '-' characters at the end of a field") {
    val copyBookContents: String =
      s"""        01  RECORD.
         |            05  F1  PIC X(10).
         |            05  F2- PIC 9(10).
         |            05  F3_ PIC A(10).
         |""".stripMargin

    val parsed = CopybookParser.parseTree(copyBookContents)

    val root = parsed.ast.children.head.asInstanceOf[Group]

    val field1 = root.children.head.name
    val field2 = root.children(1).name
    val field3 = root.children(2).name

    assert(field1 == "F1")
    assert(field2 == "F2_")
    assert(field3 == "F3_")
  }

}
