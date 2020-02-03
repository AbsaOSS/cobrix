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
import za.co.absa.cobrix.cobol.parser.antlr.{ParserVisitor, ThrowErrorStrategy, copybookLexer, copybookParser}
import za.co.absa.cobrix.cobol.parser.ast.datatype.{Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.ASCII
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy

class DataSizeSpec extends FunSuite {
  private def parse(pic: String): Primitive = {
    val visitor = new ParserVisitor(ASCII(), StringTrimmingPolicy.TrimNone,
      CodePage.getCodePageByName("common"),
      StandardCharsets.US_ASCII,
      FloatingPointFormat.IBM)

    val charStream = CharStreams.fromString("01 RECORD.\n 05 ABC PIC " + pic + ".")
    val lexer = new copybookLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new copybookParser(tokens)
    parser.setErrorHandler(new ThrowErrorStrategy())
    visitor.visit(parser.main())
    visitor.ast.children.head.asInstanceOf[Group].children.head.asInstanceOf[Primitive]
  }

  private def decimalLength(pic: String) = {
    parse(pic).dataType match {
      case dec: Decimal => (dec.precision - dec.scale, dec.scale, dec.scaleFactor)
      case int: Integral => (int.precision, 0, 0)
    }
  }

  private def compressPic(pic: String): String = {
    parse(pic).dataType.pic
  }


  test("Test PIC values are compressed correctly") {
    assert(compressPic("99999V99") == "9(5)V9(2)")
    assert(compressPic("S9") == "S9(1)")
    assert(compressPic("9(3)") == "9(3)")
    assert(compressPic("999") == "9(3)")
    assert(compressPic("X(3)XXX") == "X(6)")
    assert(compressPic("X(3)XX(5)X") == "X(10)")
    assert(compressPic("A(3)AAA") == "A(6)")
    assert(compressPic("A(3)AA(5)A") == "A(10)")
    assert(compressPic("99(3)9.9(5)9") == "9(5).9(6)")
  }

  test("Test Number of Decimal digits are reported correctly for a given PIC") {
    assert(decimalLength("99999V99") == (5, 2, 0))
    assert(decimalLength("99999V99") == (5, 2, 0))
    assert(decimalLength("9(13)V99") == (13, 2, 0))
    assert(decimalLength("9(13)V9(2)") == (13, 2, 0))
    assert(decimalLength("9999999999V9(2)") == (10, 2, 0))
    assert(decimalLength("99(5)V99(2)") == (6, 3, 0))
    assert(decimalLength("99(5)99V99(2)99") == (8, 5, 0))

    assert(decimalLength("99999.99") == (5, 2, 0))
    assert(decimalLength("9(13).99") == (13, 2, 0))
    assert(decimalLength("9(13)V") == (13, 0, 0))
    assert(decimalLength("9(13).9(2)") == (13, 2, 0))
    assert(decimalLength("9999999999.9(2)") == (10, 2, 0))
    assert(decimalLength("99(5).99(2)") == (6, 3, 0))
    assert(decimalLength("99(5)99.99(2)99") == (8, 5, 0))

    assert(decimalLength("99999,99") == (5, 2, 0))
    assert(decimalLength("9(13),99") == (13, 2, 0))
    assert(decimalLength("9(13),9(2)") == (13, 2, 0))
    assert(decimalLength("9999999999,9(2)") == (10, 2, 0))
    assert(decimalLength("99(5),99(2)") == (6, 3, 0))
    assert(decimalLength("99(5)99,99(2)99") == (8, 5, 0))

    assert(decimalLength("PPP99999") == (5, 0, -3))
    assert(decimalLength("P(3)9(10)") == (10, 0, -3))
    assert(decimalLength("9(10)PPP") == (10, 0, 3))
    assert(decimalLength("SPPP99999") == (5, 0, -3))
    assert(decimalLength("SP(3)9(10)") == (10, 0, -3))
    assert(decimalLength("S9(10)PPP") == (10, 0, 3))

    assert(decimalLength("ZZZ99(5)") == (9, 0, 0))
    assert(decimalLength("ZZZ999") == (6, 0, 0))
    assert(decimalLength("ZZZ999PPP") == (6, 0, 3))
    assert(decimalLength("ZZZ999V99") == (6, 2, 0))
    assert(decimalLength("ZZZ999VPP99") == (6, 2, -2))
    assert(decimalLength("ZZZ999.99") == (6, 2, 0))
    assert(decimalLength("ZZZ999.99ZZ") == (6, 4, 0))
    assert(decimalLength("ZZZ999V99ZZ") == (6, 4, 0))
    assert(decimalLength("ZZZ999,99") == (6, 2, 0))
    assert(decimalLength("ZZZ999,99ZZ") == (6, 4, 0))
  }

}
