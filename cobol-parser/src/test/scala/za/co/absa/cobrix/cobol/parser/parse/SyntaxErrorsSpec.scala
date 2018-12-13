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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException

class SyntaxErrorsSpec extends FunSuite {

  test("Test handle group field having a PIC modifier") {
    val copyBookContents: String =
      """********************************************
        |
        |        01  RECORD.
        |
        |           10  GRP-FIELD           PIC X(6).
        |              15  SUB_FLD1         PIC X(3).
        |              15  SUB_FLD2         PIC X(3).
        |
        |********************************************
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 5)
    assert(syntaxErrorException.msg.contains("The field is a leaf element"))
  }

  test("Test handle malformed redefines") {
    val copyBookContents: String =
      """        01  RECORD.
        |           07  SUB_FLD1         PIC X(30).
        |           07  FILLER2          PIC X(20).
        |           07  SUB_FLD2         PIC X(30) REDEFINES SUB_FLD1.
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 4)
    assert(syntaxErrorException.msg.contains("The field SUB_FLD2 redefines SUB_FLD1, which is not part if the redefined fields block"))
  }

  test("Test too big decimal precision") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(39)V9(5).
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }
    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Decimal numbers with precision bigger"))
  }

  test("Test too big decimal scale") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(38)V9(19).
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }
    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Decimal numbers with scale bigger"))
  }

  test("Test invalid LEVEL token") {
    val copyBookContents: String =
      """        01  RECORD.
        |        ///   10  FIELD           PIC 9(38)V9(19).
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }
    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Unable to parse the value of LEVEL"))
  }

  test("Test invalid placement of SIGN SEPARATE clause") {
    val copyBookContents: String =
      """        01  RECORD.
        |          10  FIELD    PIC 9(38)V9(19) COMP-3
        |          SIGN IS LEADING SEPARATE CHARACTER.
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }
    assert(syntaxErrorException.lineNumber == 3)
    assert(syntaxErrorException.msg.contains("SIGN SEPARATE clause is not supported for COMP-3"))
  }

  test("Test invalid explicit decimal for COMP-3") {
    val copyBookContents: String =
      """        01  RECORD.
        |          10  FIELD    PIC 9(8).9(9) COMP-3.
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }
    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Explicit decimal point in 'PIC 9(8).9(9)' is not supported for COMP-3."))
  }

}
