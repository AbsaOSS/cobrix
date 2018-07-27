/*
 * Copyright 2018 Barclays Africa Group Limited
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

package za.co.absa.cobrix.cobol.parser

import org.scalatest.FunSuite
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
      CopybookParser.parseTree(EBCDIC(), copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 5)
    assert(syntaxErrorException.msg.contains("Field 'GRP_FIELD' is a leaf element"))
  }


}
