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

package za.co.absa.cobrix.cobol.parser.encoding.codepage

import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Failure, Success, Try}

class CodePageSingleByteSpec extends AnyFunSuite {

  test("Ensure codepage 'common' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("common")
    assert(codePage.codePageShortName == "common")
  }

  test("Ensure codepage 'common_extended' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("common_extended")
    assert(codePage.codePageShortName == "common_extended")
  }

  test("Ensure codepage 'cp037' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp037")
    assert(codePage.codePageShortName == "cp037")
  }

  test("Ensure codepage 'cp037_extended' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp037_extended")
    assert(codePage.codePageShortName == "cp037_extended")
  }

  test("Ensure codepage 'cp273' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp273")
    assert(codePage.codePageShortName == "cp273")
  }

  test("Ensure codepage 'cp274' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp274")
    assert(codePage.codePageShortName == "cp274")
  }

  test("Ensure codepage 'cp275' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp275")
    assert(codePage.codePageShortName == "cp275")
  }

  test("Ensure codepage 'cp277' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp277")
    assert(codePage.codePageShortName == "cp277")
  }

  test("Ensure codepage 'cp278' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp278")
    assert(codePage.codePageShortName == "cp278")
  }

  test("Ensure codepage 'cp280' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp280")
    assert(codePage.codePageShortName == "cp280")
  }

  test("Ensure codepage 'cp284' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp284")
    assert(codePage.codePageShortName == "cp284")
  }

  test("Ensure codepage 'cp285' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp285")
    assert(codePage.codePageShortName == "cp285")
  }

  test("Ensure codepage 'cp297' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp297")
    assert(codePage.codePageShortName == "cp297")
  }

  test("Ensure codepage 'cp300' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp300")
    assert(codePage.codePageShortName == "cp300")
  }

  test("Ensure codepage 'cp500' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp500")
    assert(codePage.codePageShortName == "cp500")
  }

  test("Ensure codepage 'cp838' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp838")
    assert(codePage.codePageShortName == "cp838")
  }

  test("Ensure codepage 'cp870' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp870")
    assert(codePage.codePageShortName == "cp870")
  }

  test("Ensure codepage 'cp875' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp875")
    assert(codePage.codePageShortName == "cp875")
  }

  test("Ensure codepage 'cp1025' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1025")
    assert(codePage.codePageShortName == "cp1025")
  }

  test("Ensure codepage 'cp1047' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1047")
    assert(codePage.codePageShortName == "cp1047")
  }

  test("Ensure codepage 'cp1140' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1140")
    assert(codePage.codePageShortName == "cp1140")
  }

  test("Ensure codepage 'cp1141' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1141")
    assert(codePage.codePageShortName == "cp1141")
  }

  test("Ensure codepage 'cp1142' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1142")
    assert(codePage.codePageShortName == "cp1142")
  }

  test("Ensure codepage 'cp1143' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1143")
    assert(codePage.codePageShortName == "cp1143")
  }

  test("Ensure codepage 'cp1144' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1144")
    assert(codePage.codePageShortName == "cp1144")
  }

  test("Ensure codepage 'cp1145' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1145")
    assert(codePage.codePageShortName == "cp1145")
  }

  test("Ensure codepage 'cp1146' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1146")
    assert(codePage.codePageShortName == "cp1146")
  }

  test("Ensure codepage 'cp1147' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1147")
    assert(codePage.codePageShortName == "cp1147")
  }

  test("Ensure codepage 'cp1148' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1148")
    assert(codePage.codePageShortName == "cp1148")
  }

  test("Ensure codepage 'cp1364' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1364")
    assert(codePage.codePageShortName == "cp1364")
  }

  test("Ensure codepage 'cp1388' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp1388")
    assert(codePage.codePageShortName == "cp1388")
  }

  test("Ensure an unknown codepage throws an IllegalArgumentException") {
    assert(Try {
      CodePage.getCodePageByName("sdw")
    } match {
             case Success(_) => false
             case Failure(ex) =>
               if (ex.getMessage == "The code page 'sdw' is not one of the builtin EBCDIC code pages.") true
               else false
           })
  }

  test("Ensure getting a code page by its class gets a code page object") {
    val codePage = CodePage.getCodePageByClass("za.co.absa.cobrix.cobol.parser.encoding.codepage.FakeCodePage")
    assert(codePage.codePageShortName == "fake_code_page")
  }
}
