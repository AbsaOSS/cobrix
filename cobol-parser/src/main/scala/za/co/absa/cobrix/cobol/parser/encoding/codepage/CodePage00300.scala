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
  * EBCDIC code page 300 Japanese Latin Host Double-Byte.
  */
class CodePage00300 extends TwoByteCodePage(CodePage00300.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "cp00300"
}

object CodePage00300 {
  val ebcdicToAsciiMapping: Array[Char] = {
    /**
      * This is the EBCDIC Code Page 00300 contributed by https://github.com/BenceBenedek
      * https://public.dhe.ibm.com/software/globalization/gcoc/attachments/CP00300.pdf
      */
    val ebcdic2ascii: Array[Char] = {
      val directMapping = new Array[Char](65536)

      val ebcdic300 = TwoByteTables.mappingTableEbcdic300()
      val unicode300 = TwoByteTables.mappingTableUnicode300()

      var i = 0
      val len = ebcdic300.length
      while (i < len) {
        val unicode = unicode300(i)
        val ebcdic = ebcdic300(i)
        directMapping(ebcdic) = unicode.toChar
        i += 1
      }
      directMapping
    }


    ebcdic2ascii
  }
}
