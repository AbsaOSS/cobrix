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
  * EBCDIC code page 875 contains all of the Greek characters.
  *
  */
class CodePage875 extends CodePage {

  override def codePageShortName: String = "cp875"

  override protected def ebcdicToAsciiMapping: Array[Char] = {
    /* This is the EBCDIC Code Page 875 to ASCII conversion table with non-printable characters mapping
       from https://en.wikipedia.org/wiki/EBCDIC_037 */
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
            ' ', 'Α', 'Β', 'Γ', 'Δ', 'Ε', 'Ζ', 'Η', 'Θ', 'Ι', '[', '.', '<', '(', '+', '!', //  64 -  79
            '&', 'Κ', 'Λ', 'Μ', 'Ν', 'Ξ', 'Ο', 'Π', 'Ρ', 'Σ', ']', '$', '*', ')', ';', '^', //  80 -  95
            '-', '/', 'Τ', 'Υ', 'Φ', 'Χ', 'Ψ', 'Ω', 'Ϊ', 'Ϋ', '|', ',', '%', '_', '>', '?', //  96 - 111
            '¨', 'Ά', 'Έ', 'Ή', spc, 'Ί', 'Ό', 'Ύ', 'Ώ', '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
            '΅', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'α', 'β', 'γ', 'δ', 'ε', 'ζ', // 128 - 143
            '°', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'η', 'θ', 'ι', 'κ', 'λ', 'μ', // 144 - 159
            '´', '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ν', 'ξ', 'ο', 'π', 'ρ', 'σ', // 160 - 175
            '£', 'ά', 'έ', 'ή', 'ϊ', 'ί', 'ό', 'ύ', 'ϋ', 'ώ', 'ς', 'τ', 'υ', 'φ', 'χ', 'ψ', // 176 - 191
            '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', '-', 'ω', 'ΐ', 'ΰ', '‘', '―', // 192 - 207
            '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', '±', '½', spc, '·', '’', '¦', // 208 - 223
            bsh, '₯', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '²', '§', 'ͺ', spc, '«', '¬', // 224 - 239
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '³', '©', '€', spc, '»', spc) // 240 - 255
        }
    ebcdic2ascii
  }
}
