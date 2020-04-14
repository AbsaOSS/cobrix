package za.co.absa.cobrix.cobol.parser.encoding.codepage

class FakeCodePage extends CodePage {
  /**
   * A short name is used to distinguish between different code pages, so it must be unique
   */
  override def codePageShortName: String = "fake_code_page"

  /**
   * Each class inherited from CodePage should provide its own conversion table
   */
  override protected def ebcdicToAsciiMapping: Array[Char] = Array[Char]('s', 'd', 'w')
}
