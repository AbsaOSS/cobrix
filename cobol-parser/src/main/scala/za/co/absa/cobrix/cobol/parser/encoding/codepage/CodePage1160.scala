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

/**
  * EBCDIC code page 1160 with support for Thai script used in IBM mainframes which is same as 838
  * with € at the position 0xFE.
  */
class CodePage1160 extends SingleByteCodePage(CodePage1160.ebcdicToAsciiMapping, CodePage1160.asciiToEbcdicMapping) {
  override def codePageShortName: String = "cp1160"
}

object CodePage1160 {
  val ebcdicToAsciiMapping: Array[Char] = {
    import EbcdicNonPrintable._

    /* This is the EBCDIC Code Page 1160 to ASCII conversion table
       from https://en.wikibooks.org/wiki/Character_Encodings/Code_Tables/EBCDIC/EBCDIC_838 */
    val ebcdic2ascii: Array[Char] = {
      val c01 = '\u0E48'
      val c02 = '\u0E4E'
      val c03 = '\u0E31'
      val c04 = '\u0E34'
      val c05 = '\u0E49'
      val c06 = '\u0E35'
      val c07 = '\u0E36'
      val c08 = '\u0E37'
      val c09 = '\u0E38'
      val c10 = '\u0E39'
      val c11 = '\u0E3A'
      val c12 = '\u0E47'
      val c13 = '\u0E48'
      val c14 = '\u0E49'
      val c15 = '\u0E4A'
      val c16 = '\u0E4B'
      val c18 = '\u0E4D'

      Array[Char](
        spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, ccr, spc, spc, //   0 -  15
        spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  16 -  31
        spc, spc, spc, spc, spc, clf, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  32 -  47
        spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  48 -  63
        spc, spc, 'ก', 'ข', 'ฃ', 'ค', 'ฅ', 'ฆ', 'ง', '[', '¢', '.', '<', '(', '+', '|', //  64 -  79
        '&', c01, 'จ', 'ฉ', 'ช', 'ซ', 'ฌ', 'ญ', 'ฎ', ']', '!', '$', '*', ')', ';', '¬', //  80 -  95
        '-', '/', 'ฏ', 'ฐ', 'ฑ', 'ฒ', 'ณ', 'ด', 'ต', '^', '¦', ',', '%', '_', '>', '?', //  96 - 111
        '฿', c02, 'ถ', 'ท', 'ธ', 'น', 'บ', 'ป', 'ผ', '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
        '๏', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'ฝ', 'พ', 'ฟ', 'ภ', 'ม', 'ย', // 128 - 143
        '๚', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'ร', 'ฤ', 'ล', 'ฦ', 'ว', 'ศ', // 144 - 159
        '๛', '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ษ', 'ส', 'ห', 'ฬ', 'อ', 'ฮ', // 160 - 175
        '๐', '๑', '๒', '๓', '๔', '๕', '๖', '๗', '๘', '๙', 'ฯ', 'ะ', c03, 'า', 'ำ', c04, // 176 - 191
        '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', c05, c06, c07, c08, c09, c10, // 192 - 207
        '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', c11, 'เ', 'แ', 'โ', 'ใ', 'ไ', // 208 - 223
        bsh, c15, 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'ๅ', 'ๆ', c12, c13, c14, c15, // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', c16, c17, c18, c16, '€', spc) // 240 - 255
    }
    ebcdic2ascii
  }

  lazy val asciiToEbcdicMapping: Array[Byte] = SingleByteCodePage.getReverseTable(ebcdicToAsciiMapping)
}
