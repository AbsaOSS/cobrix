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
  * A "common extended" EBCDIC to ASCII table.
  *
  * It is an "invariant" subset of EBCDIC. Each converted symbol should be present in all EBCDIC pages.
  * In addition to "common" code page it contains conversions for non-printable characters.
  */
class CodePageCommonExt extends CodePage {

  override def codePageShortName: String = "common_extended"

  override protected def ebcdicToAsciiMapping: Array[Char] = {
    /* This is the EBCDIC "invariant" code page from https://en.wikipedia.org/wiki/EBCDIC */
    lazy val ebcdic2ascii: Array[Char] = {
      val clf = '\r'
      val ccr = '\n'
      val spc = ' '
      val qts = '\''
      val qtd = '\"'
      val bsh = '\\'

      val c00 = '\00'
      val c01 = '\01'
      val c02 = '\02'
      val c03 = '\03'
      val c04 = '\04'
      val c05 = '\05'
      val c06 = '\06'
      val c07 = '\07'
      val c08 = '\10'
      val c09 = '\11'
      val c0b = '\13'
      val c0c = '\14'
      val c0e = '\16'
      val c0f = '\17'
      val c10 = '\20'
      val c11 = '\21'
      val c12 = '\22'
      val c13 = '\23'
      val c14 = '\24'
      val c15 = '\25'
      val c16 = '\26'
      val c17 = '\27'
      val c18 = '\30'
      val c19 = '\31'
      val c1a = '\32'
      val c1b = '\33'
      val c1c = '\34'
      val c1d = '\35'
      val c1e = '\36'
      val c1f = '\37'

      // Non-printable characters map used: http://www.pacsys.com/asciitab.htm
      Array[Char](
        c00, c01, c02, c03, c1a, c09, c1a, spc, c1a, c1a, c1a, c0b, c0c, ccr, c0e, c0f, //   0 -  15
        c10, c11, c12, c13, c1a, c1a, c08, c1a, c18, c19, c1a, c1a, c1c, c1d, c1e, c1f, //  16 -  31
        spc, spc, spc, spc, spc, clf, c17, c1b, spc, spc, spc, spc, spc, c05, c06, c07, //  32 -  47
        spc, spc, c16, spc, spc, spc, spc, c04, spc, spc, spc, spc, c14, c15, spc, spc, //  48 -  63
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
