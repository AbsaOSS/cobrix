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
  * EBCDIC code page with full Latin-2-charset used in IBM mainframes
  * in Albania, Bosnia and Herzegovina, Croatia, Czech Republic, Hungary, Poland, Romania, Slovakia, and Slovenia
  */
class CodePage870 extends SingleByteCodePage(CodePage870.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "cp870"
}

object CodePage870 {
  val ebcdicToAsciiMapping: Array[Char] = {
    /* This is the EBCDIC Code Page 870 to ASCII conversion table with non-printable characters mapping
       from https://en.everybodywiki.com/EBCDIC_870 */
    val ebcdic2ascii: Array[Char] = {
      val clf = '\r'
      val ccr = '\n'
      val spc = ' '
      val qts = '\''
      val qtd = '\"'
      val bsh = '\\'


      Array[Char](
        spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, ccr, spc, spc, //   0 -  15
        spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  16 -  31
        spc, spc, spc, spc, spc, clf, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  32 -  47
        spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  48 -  63
        spc, spc, 'â', 'ä', 'ţ', 'á', 'ă', 'č', 'ç', 'ć', '[', '.', '<', '(', '+', '!', //  64 -  79
        '&', 'é', 'ę', 'ë', 'ů', 'í', 'î', 'ľ', 'ĺ', 'ß', ']', '$', '*', ')', ';', '^', //  80 -  95
        '-', '/', 'Â', 'Ä', '˝', 'Á', 'Ă', 'Č', 'Ç', 'Ć', '|', ',', '%', '_', '>', '?', //  96 - 111
        'ˇ', 'É', 'Ę', 'Ë', 'Ů', 'Í', 'Î', 'Ľ', 'Ĺ', '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
        '˘', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'ś', 'ň', 'đ', 'ý', 'ř', 'ş', // 128 - 143
        '˚', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'ł', 'ń', 'š', '¸', '˛', '¤', // 144 - 159
        'ą', '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'Ś', 'Ň', 'Đ', 'Ý', 'Ř', 'Ş', // 160 - 175
        '˙', 'Ą', 'ż', 'Ţ', 'Ż', '§', 'ž', 'ź', 'Ž', 'Ź', 'Ł', 'Ń', 'Š', '¨', '´', '×', // 176 - 191
        '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', spc, 'ô', 'ö', 'ŕ', 'ó', 'ő', // 192 - 207
        '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'Ě', 'ű', 'ü', 'ť', 'ú', 'ě', // 208 - 223
        bsh, '÷', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'ď', 'Ô', 'Ö', 'Ŕ', 'Ó', 'Ő', // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'Ď', 'Ű', 'Ü', 'Ť', 'Ú', spc) // 240 - 255
    }
    ebcdic2ascii
  }
}
