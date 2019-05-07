/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.cobrix.cobol.parser.parse

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}


class NonTerminalsSpec extends FunSuite {
  val copyBookContents: String =
    """        01  RECORD.
      |           05  GROUP-1.
      |              06  FIELD-1            PIC X(10).
      |              06  FILLER             PIC X(5).
      |              06  GROUP-2.
      |                 10  NESTED-FIELD-1  PIC 9(10).
      |                 10  FILLER          PIC 9(5).
      |""".stripMargin

  test("Test non-terminal fields") {
    val copybook1 = CopybookParser.parseTree(copyBookContents)
    val copybook2 = CopybookParser.parseTree(copyBookContents, nonTerminals = Seq("GROUP-1"))
    val copybook3 = CopybookParser.parseTree(copyBookContents, nonTerminals = Seq("GROUP-1", "GROUP-2"))

    // size
    assert(copybook1.getRecordSize == copybook2.getRecordSize)
    assert(copybook1.getRecordSize == copybook3.getRecordSize)

    // one more element
    assert(copybook1.ast.children.head.asInstanceOf[Group].children.size == 1)
    assert(copybook2.ast.children.head.asInstanceOf[Group].children.size == 2)
    assert(copybook2.ast.children.head.asInstanceOf[Group].children.last.isInstanceOf[Primitive])
    assert(copybook2.ast.children.head.asInstanceOf[Group].children.last.asInstanceOf[Primitive].binaryProperties.actualSize == 30)
    assert(copybook2.ast.children.head.asInstanceOf[Group].children.last.asInstanceOf[Primitive].dataType.pic == "X(30)")
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.size == 2)
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.last.isInstanceOf[Primitive])
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.last.asInstanceOf[Primitive].binaryProperties.actualSize == 30)
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.last.asInstanceOf[Primitive].dataType.pic == "X(30)")

    // nested
    assert(copybook1.ast.children.head.asInstanceOf[Group].children.head.asInstanceOf[Group].children.size == 3)
    assert(copybook2.ast.children.head.asInstanceOf[Group].children.head.asInstanceOf[Group].children.size == 3)
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.head.asInstanceOf[Group].children.size == 4)
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.head.asInstanceOf[Group].children.last.asInstanceOf[Primitive].binaryProperties.actualSize == 15)
    assert(copybook3.ast.children.head.asInstanceOf[Group].children.head.asInstanceOf[Group].children.last.asInstanceOf[Primitive].dataType.pic == "X(15)")

    // read data


  }
}