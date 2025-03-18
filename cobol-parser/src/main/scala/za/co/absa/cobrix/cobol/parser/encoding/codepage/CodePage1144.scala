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
  * EBCDIC code page 1144. Italy.
  *
  * It corresponds to code page 280 and only differs from it in position 9F, where the euro sign € is located instead
  * of the international currency symbol ¤.
  */
class CodePage1144 extends SingleByteCodePage(CodePage1144.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "cp1144"
}

object CodePage1144 {
  val ebcdicToAsciiMapping: Array[Char] = {
    import EbcdicNonPrintable._

    /* This is the EBCDIC Code Page 1144 to ASCII conversion table
       from https://en.wikibooks.org/wiki/Character_Encodings/Code_Tables/EBCDIC/EBCDIC_280 */
    val ebcdic2ascii: Array[Char] = {
      // Non-printable characters map used: http://www.pacsys.com/asciitab.htm
      Array[Char](
        c00, c01, c02, c03, spc, c09, spc, del, spc, spc, spc, c0b, c0c, ccr, c0e, c0f, //   0 -  15
        c10, c11, c12, c13, spc, nel, c08, spc, c18, c19, spc, spc, c1c, c1d, c1e, c1f, //  16 -  31
        spc, spc, spc, spc, spc, clf, c17, c1b, spc, spc, spc, spc, spc, c05, c06, c07, //  32 -  47
        spc, spc, c16, spc, spc, spc, spc, c04, spc, spc, spc, spc, c14, c15, spc, c1a, //  48 -  63
        ' ', rsp, 'â', 'ä', '{', 'á', 'ã', 'å', bsh, 'ñ', '°', '.', '<', '(', '+', '!', //  64 -  79
        '&', ']', 'ê', 'ë', '}', 'í', 'î', 'ï', '~', 'ß', 'é', '$', '*', ')', ';', '^', //  80 -  95
        '-', '/', 'Â', 'Ä', 'À', 'Á', 'Ã', 'Å', 'Ç', 'Ñ', 'ò', ',', '%', '_', '>', '?', //  96 - 111
        'ø', 'É', 'Ê', 'Ë', 'È', 'Í', 'Î', 'Ï', 'Ì', 'ù', ':', '£', '§', qts, '=', qtd, // 112 - 127
        'Ø', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '«', '»', 'ð', 'ý', 'þ', '±', // 128 - 143
        '[', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'ª', 'º', 'æ', '¸', 'Æ', '€', // 144 - 159
        'µ', 'ì', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '¡', '¿', 'Ð', 'Ý', 'Þ', '®', // 160 - 175
        '¢', '#', '¥', '·', '©', '@', '¶', '¼', '½', '¾', '¬', '|', '¯', '¨', '´', '×', // 176 - 191
        'à', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', shy, 'ô', 'ö', '¦', 'ó', 'õ', // 192 - 207
        'è', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', '¹', 'û', 'ü', '`', 'ú', 'ÿ', // 208 - 223
        'ç', '÷', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '²', 'Ô', 'Ö', 'Ò', 'Ó', 'Õ', // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '³', 'Û', 'Ü', 'Ù', 'Ú', spc) // 240 - 255
    }
    ebcdic2ascii
  }
}
