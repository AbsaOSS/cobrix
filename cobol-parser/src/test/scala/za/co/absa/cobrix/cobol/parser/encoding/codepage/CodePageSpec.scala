package za.co.absa.cobrix.cobol.parser.encoding.codepage

import org.scalatest.FunSuite

import scala.util.{Failure, Success, Try}

class CodePageSpec extends FunSuite {

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
