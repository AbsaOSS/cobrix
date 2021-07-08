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

class CopybookRequirementsSpec extends FunSuite {
  test("Test that parsing fails if the copybook doesn't have a root record") {
    val copyBookContents: String =
      """********************************************
        |
        |         15  SUB_FLD1         PIC X(3).
        |         15  SUB_FLD2         PIC 9999 COMP-3.
        |
        |********************************************
        |""".stripMargin

    val ex = intercept[IllegalArgumentException] {
      val copybook = CopybookParser.parseTree(copyBookContents)
      copybook.generateRecordLayoutPositions()
    }

    assert(ex.getMessage.contains("Found a non-GROUP field at the root level (SUB_FLD1)."))
  }

}
