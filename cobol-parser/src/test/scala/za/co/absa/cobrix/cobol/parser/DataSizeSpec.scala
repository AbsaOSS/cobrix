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

class DataSizeSpec extends FunSuite {

  test("Test PIC values are expanded correctly") {
    assert (CopybookParser.expandPic("99999V99") == "99999V99")
    assert (CopybookParser.expandPic("9(3)") == "999")
    assert (CopybookParser.expandPic("X(3)XXX") == "XXXXXX")
    assert (CopybookParser.expandPic("X(3)XX(5)X") == "XXXXXXXXXX")
    assert (CopybookParser.expandPic("XX(3)X.X(5)X") == "XXXXX.XXXXXX")
  }

  test("Test Number of Decimal digits are reported correctly for a given PIC") {
    assert (CopybookParser.decimalLength("99999V99") == (5,2))
    assert (CopybookParser.decimalLength("9(13)V99") == (13,2))
    assert (CopybookParser.decimalLength("9(13)V9(2)") == (13,2))
    assert (CopybookParser.decimalLength("9999999999V9(2)") == (10,2))
    assert (CopybookParser.decimalLength("99(5)V99(2)") == (6,3))
    assert (CopybookParser.decimalLength("99(5)99V99(2)99") == (8,5))

    assert (CopybookParser.decimalLength("99999.99") == (5,2))
    assert (CopybookParser.decimalLength("9(13).99") == (13,2))
    assert (CopybookParser.decimalLength("9(13).9(2)") == (13,2))
    assert (CopybookParser.decimalLength("9999999999.9(2)") == (10,2))
    assert (CopybookParser.decimalLength("99(5).99(2)") == (6,3))
    assert (CopybookParser.decimalLength("99(5)99.99(2)99") == (8,5))
  }

}
