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

package za.co.absa.cobrix.cobol.parser.parse

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}

import scala.collection.mutable.ArrayBuffer


class NonTerminalsSpec extends FunSuite {


  test("Test non-terminal fields") {
    val copyBookContents: String =
      """        01  RECORD.
        |           05  GROUP-1.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |""".stripMargin

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
  }

  test("Test non-terminal fields - repeated names") {
    val copyBookContents: String =
      """        01  RECORD.
        |           05  GROUP-A.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |           05  GROUP-B.
        |              06  FIELD-1            PIC X(10).
        |              06  FILLER             PIC X(5).
        |              06  GROUP-2.
        |                 10  NESTED-FIELD-1  PIC 9(10).
        |                 10  FILLER          PIC 9(5).
        |           05  GROUP-A-NT            PIC X.
        |           05  GROUP-B-NT            PIC X.
        |           05  GROUP-B-NT1           PIC X.
        |           05  GROUP-B-NT2           PIC X.
        |           05  GROUP-B-NT3           PIC X.
        |           05  GROUP-B-NT4           PIC X.
        |""".stripMargin

    val copybook1 = CopybookParser.parseTree(copyBookContents)
    val copybook2 = CopybookParser.parseTree(copyBookContents, nonTerminals = Seq("GROUP-A"))
    val copybook3 = CopybookParser.parseTree(copyBookContents, nonTerminals = Seq("GROUP-B", "GROUP-2"))

    def flattenNames(g: Group): ArrayBuffer[String] = {
      g.name +: g.children.flatMap {
        case p: Primitive => ArrayBuffer(p.name)
        case g: Group => flattenNames(g)
      }
    }

    val names1 = flattenNames(copybook1.ast)
    val names2 = flattenNames(copybook2.ast)
    val names3 = flattenNames(copybook3.ast)

    assert(names1(8) == "GROUP_B")
    assert(names2(8) == "GROUP_A_NT1")
    assert(names3(8) == "GROUP_2_NT")
    assert(names3(15) == "GROUP_2_NT")
    assert(names3(16) == "GROUP_B_NT5")
  }
}
