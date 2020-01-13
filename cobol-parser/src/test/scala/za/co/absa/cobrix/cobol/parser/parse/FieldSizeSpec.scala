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
import za.co.absa.cobrix.cobol.parser.ast.datatype.{Decimal, Integral}
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
      |           10  DECIMAL_P1         PIC S9(9)PPP.
      |           10  DECIMAL_P2         PIC SPPP9(9).
      |           10  DECIMAL_P3         PIC SVPP9(5).
      |           10  DECIMAL_P4         PIC SPP9999.
      |           10  TWO_SETS_BRACES    PIC S9(15)V99.
      |           10  TWO_SETS_BRACES2   PIC S9(15)V9(2).
      |           10  SEVEN_DIGITS_L     PIC SV9(7) SIGN LEADING.
      |           10  SEVEN_DIGITS_T     PIC SV9(7) SIGN TRAILING.
      |           10  EX-NUM-INT01        PIC +9(8).
      |           10  EX-NUM-INT02        PIC 9(8)+.
      |           10  EX-NUM-INT03        PIC -9(8).
      |           10  EX-NUM-INT04        PIC Z(8)-.
      |           10  EX-NUM-DEC01        PIC +9(6)V99.
      |           10  EX-NUM-DEC02        PIC Z(6)VZZ-.
      |           10  EX-NUM-DEC03        PIC 9(6).99-.
      |""".stripMargin

  def fieldsize(index: Int, cpy: Copybook): Int = {
    val item = cpy.ast.children.head.asInstanceOf[Group].children(index)
    val sizebits = item match {
      case grp: Group => grp.binaryProperties.actualSize
      case stat: Primitive => stat.binaryProperties.actualSize
    }
    sizebits
  }

  def scale(index: Int, cpy: Copybook): (Int, Int) = {
    val item = cpy.ast.children.head.asInstanceOf[Group].children(index)
    item match {
      case _: Group => (0, 0)
      case stat: Primitive => {
        stat.dataType match {
          case x: Decimal => (x.scale, x.scaleFactor)
          case _ => (0, 0)
        }
      }
    }
  }

  test("Test field sizes are correctly calculated") {
    val copybook = CopybookParser.parseTree(copyBookContents)
    val size_s9_2_comp = fieldsize(0, copybook)
    val size_x10 = fieldsize(1, copybook)
    val size_s9_7_v_9_2_comp3 = fieldsize(2, copybook)
    val size_s9_4_v_9_2_comp3 = fieldsize(3, copybook)
    val size_s9_15_v_comp3 = fieldsize(4, copybook)
    val size_s9_11_v_binary = fieldsize(5, copybook)
    val size_s9_long_sign_leading_separate = fieldsize(6, copybook)
    val size_s11_decimal_sign_leading_separate = fieldsize(7, copybook)
    val size_s9_decimal_ppp_1 = fieldsize(8, copybook)
    val size_s9_decimal_ppp_2 = fieldsize(9, copybook)
    val size_s9_decimal_ppp_3 = fieldsize(10, copybook)
    val size_s9_decimal_ppp_4 = fieldsize(11, copybook)
    val two_sets_braces = fieldsize(12, copybook)
    val two_sets_braces2 = fieldsize(13, copybook)
    val seven_digits_l = fieldsize(14, copybook)
    val seven_digits_t = fieldsize(15, copybook)
    val signs_int_1 = fieldsize(16, copybook)
    val signs_int_2 = fieldsize(17, copybook)
    val signs_int_3 = fieldsize(18, copybook)
    val signs_int_4 = fieldsize(19, copybook)
    val signs_dec_1 = fieldsize(20, copybook)
    val signs_dec_2 = fieldsize(21, copybook)
    val signs_dec_3 = fieldsize(22, copybook)

    assert(size_s9_2_comp == 2)
    assert(size_x10 == 10)
    assert(size_s9_7_v_9_2_comp3 == 5)
    assert(size_s9_4_v_9_2_comp3 == 4)
    assert(size_s9_15_v_comp3 == 8)
    assert(size_s9_11_v_binary == 8)
    assert(size_s9_long_sign_leading_separate == 10)
    assert(size_s11_decimal_sign_leading_separate == 12)
    assert(size_s9_decimal_ppp_1 == 9)
    assert(scale(8, copybook) == (0, 3))
    assert(size_s9_decimal_ppp_2 == 9)
    assert(scale(9, copybook) == (0, -3))
    assert(size_s9_decimal_ppp_3 == 5)
    assert(scale(10, copybook) == (5, 2))
    assert(size_s9_decimal_ppp_4 == 4)
    assert(scale(11, copybook) == (0, -2))
    assert(two_sets_braces == 17)
    assert(two_sets_braces2 == 17)
    assert(seven_digits_l == 7)
    assert(seven_digits_t == 7)
    assert(signs_int_1 == 9)
    assert(signs_int_2 == 9)
    assert(signs_int_3 == 9)
    assert(signs_int_4 == 9)
    assert(signs_dec_1 == 9)
    assert(signs_dec_2 == 9)
    assert(signs_dec_3 == 10)
  }
}