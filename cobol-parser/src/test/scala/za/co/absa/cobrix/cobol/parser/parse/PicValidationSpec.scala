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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.validators.CobolValidators

class PicValidationSpec extends FunSuite {

  test("Test invalid sign definition for a string field") {
    val copyBookContents: String =
      """        01  RECORD.
        |           07  FIELD         PIC SX(30).
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 2)
    assert(syntaxErrorException.msg.contains("Invalid 'PIC SX(30)'. Unexpected character 'X' at position 2."))
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
    assert(syntaxErrorException.msg.contains("Unexpected character '(' at position 7."))
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
    assert(syntaxErrorException.msg.contains("Cannot mix 'X' with '9' at position 6."))
  }

  test("Test various correct PICs") {
    CobolValidators.validatePic(0, "A", "9999")
    CobolValidators.validatePic(0, "A", "9(5)")
    CobolValidators.validatePic(0, "A", "S99(5)99")
    CobolValidators.validatePic(0, "A", "S9(3)9(2)")
    CobolValidators.validatePic(0, "A", "XXX(5)")
    CobolValidators.validatePic(0, "A", "S9(5)V9(5)")
    CobolValidators.validatePic(0, "A", "S9(5)V99")
    CobolValidators.validatePic(0, "A", "S99V9(5)")
    CobolValidators.validatePic(0, "A", ".999")
    CobolValidators.validatePic(0, "A", "V99")
    CobolValidators.validatePic(0, "A", "9(10000)")
    CobolValidators.validatePic(0, "A", "Z(5)")
    CobolValidators.validatePic(0, "A", "Z(5)VZZ")
  }

  test("Test various malformed PICs") {
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "Y") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "(10)9") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "XVX") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "X.X") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9.A") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "SXXX") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "S(10)999") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(10)S99") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "SV99") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "999A") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(2(3))") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(2)(3)") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9((3))") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(5") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9()") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "95") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(9V9)") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9.9.9") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "99V99.99") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(10)VV9") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "S9(5)V(5)") }
    intercept[SyntaxErrorException] { CobolValidators.validatePic(0, "A", "9(100000)") }
  }


}
