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

import java.nio.charset.StandardCharsets

import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.antlr.{ParserVisitor, ThrowErrorStrategy, copybookLexer, copybookParser}
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.ASCII
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy

class PicValidationSpec extends FunSuite {

  private def validatePic(pic: String) = {

    val visitor = new ParserVisitor(ASCII(), StringTrimmingPolicy.TrimNone,
      CodePage.getCodePageByName("common"),
      StandardCharsets.UTF_8,
      FloatingPointFormat.IBM)

    val charStream = CharStreams.fromString("01 RECORD.\n 05 ABC PIC " + pic + ".")
    val lexer = new copybookLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new copybookParser(tokens)
    parser.setErrorHandler(new ThrowErrorStrategy())
    visitor.visit(parser.main())
  }



  test("Test invalid sign definition for a string field") {
    val copyBookContents: String =
      """        01  RECORD.
        |           07  FIELD         PIC SX(30).
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Invalid input 'SX' at position 2:33"))
  }

  test("Test invalid decimal scale specifier") {
    val copyBookContents: String =
      """        01  RECORD.
        |           07  FIELD         PIC S9(5)V(5).
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Invalid input '(' at position 2:39"))
  }

  test("Test invalid mix string and numeric definition") {
    val copyBookContents: String =
      """        01  RECORD.
        |           07  FIELD         PIC 9(3)VXX.
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Invalid input 'XX' at position 2:38"))
  }

  test("Test various correct PICs") {
    validatePic("9999")
    validatePic("9(5)")
    validatePic("S99(5)99")
    validatePic("S9(3)9(2)")
    validatePic("XXX(5)")
    validatePic("S9(5)V9(5)")
    validatePic("S9(5)V99")
    validatePic("S99V9(5)")
    validatePic(".999")
    validatePic("V99")
    validatePic("9(10000)")
    validatePic("Z(5)")
    validatePic("Z(5)VZZ")
    validatePic("SV99")
    validatePic("PPP999")
    validatePic("S999PPP")
    validatePic("P(3)999")
    validatePic("S9(3)PPP")
    validatePic("P(3)9(3)")
    validatePic("S9(3)P(3)")
  }

  test("Test various malformed PICs") {
    intercept[SyntaxErrorException] { validatePic("Y") }
    intercept[SyntaxErrorException] { validatePic("(10)9") }
    intercept[SyntaxErrorException] { validatePic("XVX") }
    intercept[SyntaxErrorException] { validatePic("X.X") }
    intercept[SyntaxErrorException] { validatePic("9.A") }
    intercept[SyntaxErrorException] { validatePic("SXXX") }
    intercept[SyntaxErrorException] { validatePic("S(10)999") }
    intercept[SyntaxErrorException] { validatePic("9(10)S99") }
    intercept[SyntaxErrorException] { validatePic("999A") }
    intercept[SyntaxErrorException] { validatePic("9(2(3))") }
    intercept[SyntaxErrorException] { validatePic("9(2)(3)") }
    intercept[SyntaxErrorException] { validatePic("9((3))") }
    intercept[SyntaxErrorException] { validatePic("9(5") }
    intercept[SyntaxErrorException] { validatePic("9()") }
    intercept[SyntaxErrorException] { validatePic("95") }
    intercept[SyntaxErrorException] { validatePic("9(9V9)") }
    intercept[SyntaxErrorException] { validatePic("9.9.9") }
    intercept[SyntaxErrorException] { validatePic("99V99.99") }
    intercept[SyntaxErrorException] { validatePic("9(10)VV9") }
    intercept[SyntaxErrorException] { validatePic("S9(5)V(5)") }
    intercept[SyntaxErrorException] { validatePic("9(100000)") }
    intercept[SyntaxErrorException] { validatePic("9P9") }
    intercept[SyntaxErrorException] { validatePic("9(5)P(5)9(5)") }
    intercept[SyntaxErrorException] { validatePic("P(5)") }
    intercept[SyntaxErrorException] { validatePic("SP(5)") }
    intercept[SyntaxErrorException] { validatePic("S9P(5)9") }

    intercept[SyntaxErrorException] { validatePic("V") }
    intercept[SyntaxErrorException] { validatePic("SV") }
  }

}
