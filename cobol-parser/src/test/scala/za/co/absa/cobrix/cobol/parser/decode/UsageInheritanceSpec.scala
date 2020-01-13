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

package za.co.absa.cobrix.cobol.parser.decode

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.datatype.{COMP3, COMP4}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException

class UsageInheritanceSpec extends FunSuite {

  test("Test nested fields inherit group usage fields") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMP-3.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val primitive = firstRecord.children.head.asInstanceOf[Group].children.head.asInstanceOf[Primitive]

    val dataType = primitive.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral].compact

    assert(compact.isDefined)
    assert(compact.contains(COMP3()))
  }

  test("Test alternative COMP type definition") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMPUTATIONAL-3.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val primitive = firstRecord.children.head.asInstanceOf[Group].children.head.asInstanceOf[Primitive]

    val dataType = primitive.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral].compact

    assert(compact.isDefined)
    assert(compact.contains(COMP3()))
  }

  test("Test group COMPUTATIONAL USAGE") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMPUTATIONAL.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val primitive = firstRecord.children.head.asInstanceOf[Group].children.head.asInstanceOf[Primitive]

    val dataType = primitive.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral].compact

    assert(compact.isDefined)
    assert(compact.contains(COMP4()))
  }

  test("Test default field USAGE") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val primitive = firstRecord.children.head.asInstanceOf[Group].children.head.asInstanceOf[Primitive]

    val dataType = primitive.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral].compact

    assert(compact.isEmpty)
  }

  test("Test conflict between group's COMP and field's COMP values") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMP-3.
        |              15  FLD       PIC 9(7)  COMP.
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 3)
    assert(syntaxErrorException.msg.contains("Field USAGE (COMP-4) doesn't match group's USAGE (COMP-3)"))
  }

}
