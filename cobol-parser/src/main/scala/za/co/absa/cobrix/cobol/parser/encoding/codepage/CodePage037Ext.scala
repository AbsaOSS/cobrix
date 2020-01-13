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
  * EBCDIC code page 37 contains all of the standard Latin-1 characters.
  *
  * In addition to "cp037" code page it contains conversions for non-printable characters.
  */
class CodePage037Ext extends CodePage {

  override def codePageShortName: String = "cp037_extended"

  override protected def ebcdicToAsciiMapping: Array[Char] = {
    /* This is the EBCDIC Code Page 37 to ASCII conversion table with non-printable characters mapping
       from https://en.wikipedia.org/wiki/EBCDIC_037 */
    val ebcdic2ascii: Array[Char] = {
      val clf = '\r'
      val ccr = '\n'
      val spc = ' '
      val qts = '\''
      val qtd = '\"'
      val bsh = '\\'

      val c00 = '\u0000'
      val c01 = '\u0001'
      val c02 = '\u0002'
      val c03 = '\u0003'
      val c04 = '\u0004'
      val c05 = '\u0005'
      val c06 = '\u0006'
      val c07 = '\u0007'
      val c08 = '\u0008'
      val c09 = '\u0009'
      val c0b = '\u000b'
      val c0c = '\u000c'
      val c0e = '\u000e'
      val c0f = '\u000f'
      val c10 = '\u0010'
      val c11 = '\u0011'
      val c12 = '\u0012'
      val c13 = '\u0013'
      val c14 = '\u0014'
      val c15 = '\u0015'
      val c16 = '\u0016'
      val c17 = '\u0017'
      val c18 = '\u0018'
      val c19 = '\u0019'
      val c1a = '\u001a'
      val c1b = '\u001b'
      val c1c = '\u001c'
      val c1d = '\u001d'
      val c1e = '\u001e'
      val c1f = '\u001f'
      val del = '\u007f'
      val shy = '\u00ad'
      val nel = '\u0085'
      val rsp = '\u00a0'

      // Non-printable characters map used: http://www.pacsys.com/asciitab.htm
      Array[Char](
        c00, c01, c02, c03, spc, c09, spc, del, spc, spc, spc, c0b, c0c, ccr, c0e, c0f, //   0 -  15
        c10, c11, c12, c13, spc, nel, c08, spc, c18, c19, spc, spc, c1c, c1d, c1e, c1f, //  16 -  31
        spc, spc, spc, spc, spc, clf, c17, c1b, spc, spc, spc, spc, spc, c05, c06, c07, //  32 -  47
        spc, spc, c16, spc, spc, spc, spc, c04, spc, spc, spc, spc, c14, c15, spc, c1a, //  48 -  63
        ' ', rsp, 'â', 'ä', 'à', 'á', 'ã', 'å', 'ç', 'ñ', '¢', '.', '<', '(', '+', '|', //  64 -  79
        '&', 'é', 'ê', 'ë', 'è', 'í', 'î', 'ï', 'ì', 'ß', '!', '$', '*', ')', ';', '¬', //  80 -  95
        '-', '/', 'Â', 'Ä', 'À', 'Á', 'Ã', 'Å', 'Ç', 'Ñ', '|', ',', '%', '_', '>', '?', //  96 - 111
        'ø', 'É', 'Ê', 'Ë', 'È', 'Í', 'Î', 'Ï', 'Ì', '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
        'Ø', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '«', '»', 'ð', 'ý', 'þ', '±', // 128 - 143
        '°', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'ª', 'º', 'æ', '¸', 'Æ', '¤', // 144 - 159
        'µ', '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '¡', '¿', 'Ð', 'Ý', 'Þ', '®', // 160 - 175
        '^', '£', '¥', '·', '©', '§', '¶', '¼', '½', '¾', '[', ']', '¯', '¨', '´', '×', // 176 - 191
        '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', shy, 'ô', 'ö', 'ò', 'ó', 'õ', // 192 - 207
        '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', '¹', 'û', 'ü', 'ù', 'ú', 'ÿ', // 208 - 223
        bsh, '÷', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '²', 'Ô', 'Ö', 'Ò', 'Ó', 'Õ', // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '³', 'Û', 'Ü', 'Ù', 'Ú', spc) // 240 - 255
    }
    ebcdic2ascii
  }
}
