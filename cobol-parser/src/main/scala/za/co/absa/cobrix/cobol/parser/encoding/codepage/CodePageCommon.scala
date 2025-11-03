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
  * A "common" EBCDIC to ASCII table.
  *
  * It is an "invariant" subset of EBCDIC. Each converted symbol should be present in all EBCDIC pages.
  */
class CodePageCommon extends SingleByteCodePage(CodePageCommon.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "common"
}

object CodePageCommon {
  val ebcdicToAsciiMapping: Array[Char] = {
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
        '-', '/', spc, spc, spc, spc, spc, spc, spc, spc, '¦', ',', '%', '_', '>', '?', //  96 - 111
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

  private val data = Array[Byte](
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x0D.toByte, 0x00.toByte, 0x00.toByte, 0x25.toByte, 0x00.toByte, 0x00.toByte, //   0 -  15
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, //  16 -  31
    0x40.toByte, 0x5A.toByte, 0x7F.toByte, 0x7B.toByte, 0x5B.toByte, 0x6C.toByte, 0x50.toByte, 0x7D.toByte, 0x4D.toByte, 0x5D.toByte, 0x5C.toByte, 0x4E.toByte, 0x6B.toByte, 0x60.toByte, 0x4B.toByte, 0x61.toByte, //  32 -  47
    0xF0.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0x7A.toByte, 0x5E.toByte, 0x4C.toByte, 0x7E.toByte, 0x6E.toByte, 0x6F.toByte, //  48 -  63
    0x7C.toByte, 0xC1.toByte, 0xC2.toByte, 0xC3.toByte, 0xC4.toByte, 0xC5.toByte, 0xC6.toByte, 0xC7.toByte, 0xC8.toByte, 0xC9.toByte, 0xD1.toByte, 0xD2.toByte, 0xD3.toByte, 0xD4.toByte, 0xD5.toByte, 0xD6.toByte, //  64 -  79
    0xD7.toByte, 0xD8.toByte, 0xD9.toByte, 0xE2.toByte, 0xE3.toByte, 0xE4.toByte, 0xE5.toByte, 0xE6.toByte, 0xE7.toByte, 0xE8.toByte, 0xE9.toByte, 0xBA.toByte, 0xE0.toByte, 0xBB.toByte, 0xB0.toByte, 0x6D.toByte, //  80 -  95
    0x79.toByte, 0x81.toByte, 0x82.toByte, 0x83.toByte, 0x84.toByte, 0x85.toByte, 0x86.toByte, 0x87.toByte, 0x88.toByte, 0x89.toByte, 0x91.toByte, 0x92.toByte, 0x93.toByte, 0x94.toByte, 0x95.toByte, 0x96.toByte, //  96 - 111
    0x97.toByte, 0x98.toByte, 0x99.toByte, 0xA2.toByte, 0xA3.toByte, 0xA4.toByte, 0xA5.toByte, 0xA6.toByte, 0xA7.toByte, 0xA8.toByte, 0xA9.toByte, 0xC0.toByte, 0x6A.toByte, 0xD0.toByte, 0xA1.toByte, 0x00.toByte, // 112 - 127
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 128 - 143
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 144 - 159
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 160 - 175
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 176 - 191
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 192 - 207
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 208 - 223
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 224 - 239
    0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte  // 240 - 255
  )
  /**
    * This is the table for converting basic ASCII symbols to EBCDIC common code page
    */
  def asciiToEbcdicMapping: Int => Byte = (y : Int) => {
    data.applyOrElse(y, (x : Int) => 0x40.toByte)
  }
}
