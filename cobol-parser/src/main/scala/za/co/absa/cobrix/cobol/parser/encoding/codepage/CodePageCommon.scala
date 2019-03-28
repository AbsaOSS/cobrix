/*
 * Copyright 2018-2019 ABSA Group Limited
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
  * A "common" EBCDIC to ASCII table.
  *
  * It is an "invariant" subset of EBCDIC. Each converted symbol should be present in all EBCDIC pages.
  */
class CodePageCommon extends CodePage {

  override def codePageShortName: String = "common"

  override protected def ebcdicToAsciiMapping: Array[Char] = {
    /* This is the EBCDIC "invariant" code page from https://en.wikipedia.org/wiki/EBCDIC */
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
        ' ', ' ', spc, spc, spc, spc, spc, spc, spc, spc, spc, '.', '<', '(', '+', '|', //  64 -  79
        '&', spc, spc, spc, spc, spc, spc, spc, spc, spc, '!', '$', '*', ')', ';', spc, //  80 -  95
        '-', '/', spc, spc, spc, spc, spc, spc, spc, spc, '|', ',', '%', '_', '>', '?', //  96 - 111
        spc, spc, spc, spc, spc, spc, spc, spc, spc, '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
        spc, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', spc, spc, spc, spc, spc, spc, // 128 - 143
        spc, 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', spc, spc, spc, spc, spc, spc, // 144 - 159
        spc, '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', spc, spc, spc, spc, spc, spc, // 160 - 175
        '^', spc, spc, spc, spc, spc, spc, spc, spc, spc, '[', ']', spc, spc, spc, spc, // 176 - 191
        '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', '-', spc, spc, spc, spc, spc, // 192 - 207
        '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', spc, spc, spc, spc, spc, spc, // 208 - 223
        bsh, spc, 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', spc, spc, spc, spc, spc, spc, // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', spc, spc, spc, spc, spc, spc) // 240 - 255
    }
    ebcdic2ascii
  }
}
