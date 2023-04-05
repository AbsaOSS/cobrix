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

import za.co.absa.cobrix.cobol.parser.encoding.codepage.TwoByteCodePage.createEbcdicToUnicodeTable

/**
  * EBCDIC code page CCSID-1388 (Simplified Chinese)
  */
class CodePage1388 extends TwoByteCodePage(CodePage1388.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "cp1388"
}

object CodePage1388 {
  val ebcdicToAsciiMapping: Array[Char] = {
    /**
      * This is the EBCDIC Code Page 1388 contributed by https://github.com/BenceBenedek
      * https://www.ibm.com/docs/en/i/7.3?topic=reference-ccsid-values
      */
    createEbcdicToUnicodeTable(TwoByteTables1388.mappingTableEbcdic1388(), TwoByteTables1388.mappingTableUnicode1388())
  }
}
