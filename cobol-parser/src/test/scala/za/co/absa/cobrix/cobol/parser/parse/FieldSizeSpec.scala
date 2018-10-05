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
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}


class FieldSizeSpec extends FunSuite {
  val copyBookContents: String =
    """        01  RECORD.
      |           10  NUM1               PIC S9(2) USAGE COMP.
      |           10  DATE1              PIC X(10).
      |           10  DECIMAL-AMT        PIC S9(7)V9(2) USAGE COMP-3.
      |           10  DATE-TIME          PIC S9(4)V9(2) USAGE COMP-3.
      |           10  DECIMAL-NUM        PIC S9(15)V USAGE COMP-3.
      |           10  DECIMAL-NUM2       PIC S9(09)V99 BINARY.
      |           10  LONG_LEAD_SIG1     PIC S9(9) SIGN LEADING SEPARATE.
      |           10  DECIMAL_LEAD_SIG1  PIC S9(9)V99 SIGN LEADING SEPARATE.
      |""".stripMargin

  def fieldsize(index: Int, cpy: Copybook): Int = {
    val item = cpy.ast.head.children(index)
    val sizebits = item match {
      case grp: Group => grp.binaryProperties.actualSize
      case stat: Primitive => stat.binaryProperties.actualSize
    }
    sizebits
  }

  test("Test field sizes are correctly calculated") {
    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val size_s9_2_comp = fieldsize(0, copybook)
    val size_x10 = fieldsize(1, copybook)
    val size_s9_7_v_9_2_comp3 = fieldsize(2, copybook)
    val size_s9_4_v_9_2_comp3 = fieldsize(3, copybook)
    val size_s9_15_v_comp3 = fieldsize(4, copybook)
    val size_s9_11_v_binary = fieldsize(5, copybook)
    val size_s9_long_sign_leading_separate = fieldsize(6, copybook)
    val size_s11_decimal_sign_leading_separate = fieldsize(7, copybook)

    assert(size_s9_2_comp == 2 * 8)
    assert(size_x10 == 10 * 8)
    assert(size_s9_7_v_9_2_comp3 == 5 * 8)
    assert(size_s9_4_v_9_2_comp3 == 4 * 8)
    assert(size_s9_15_v_comp3 == 8 * 8)
    assert(size_s9_11_v_binary == 64)
    assert(size_s9_long_sign_leading_separate == 10 * 8)
    assert(size_s11_decimal_sign_leading_separate == 12 * 8)
  }
}