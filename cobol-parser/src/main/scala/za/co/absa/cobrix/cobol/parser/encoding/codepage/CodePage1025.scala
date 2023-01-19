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
  * EBCDIC code page with full Cyrillic-charset
  */
class CodePage1025 extends CodePage {
  override def codePageShortName: String = "cp1025"

  override protected def ebcdicToAsciiMapping: Array[Char] = {
    /* This is the EBCDIC Code Page 1025 to ASCII conversion table with non-printable characters mapping
       from https://en.everybodywiki.com/EBCDIC_1025  */
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
        spc, spc, 'ђ', 'ѓ', 'ё', 'є', 'ѕ', 'і', 'ї', 'ј', '[', '.', '<', '(', '+', '!', //  64 -  79
        '&', 'љ', 'њ', 'ћ', 'ќ', 'ў', 'џ', 'Ъ', '№', 'Ђ', ']', '$', '*', ')', ';', '^', //  80 -  95
        '-', '/', 'Ѓ', 'Ё', 'Є', 'Ѕ', 'І', 'Ї', 'Ј', 'Љ', '|', ',', '%', '_', '>', '?', //  96 - 111
        'Њ', 'Ћ', 'Ќ', spc, 'Ў', 'Џ', 'ю', 'а', 'б', '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
        'ц', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'д', 'е', 'ф', 'г', 'х', 'и', // 128 - 143
        'й', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'к', 'л', 'м', 'н', 'о', 'п', // 144 - 159
        'я', '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'р', 'с', 'т', 'у', 'ж', 'в', // 160 - 175
        'ь', 'ы', 'з', 'ш', 'э', 'щ', 'ч', 'ъ', 'Ю', 'А', 'Б', 'Ц', 'Д', 'Е', 'Ф', 'Г', // 176 - 191
        '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'Х', 'И', 'Й', 'К', 'Л', 'М', // 192 - 207
        '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'Н', 'О', 'П', 'Я', 'Р', 'С', // 208 - 223
        bsh, '§', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Т', 'У', 'Ж', 'В', 'Ь', 'Ы', // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'З', 'Ш', 'Э', 'Щ', 'Ч', spc) // 240 - 255
    }
    ebcdic2ascii
  }
}
