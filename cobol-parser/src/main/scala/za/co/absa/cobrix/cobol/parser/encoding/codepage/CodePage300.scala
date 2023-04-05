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
  * EBCDIC code page 300 Japanese Latin Host Double-Byte.
  */
class CodePage300 extends TwoByteCodePage(CodePage300.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "cp300"
}

object CodePage300 {
  val ebcdicToAsciiMapping: Array[Char] = {
    /**
      * This is the EBCDIC Code Page CCSID-00300 contributed by https://github.com/BenceBenedek
      * https://public.dhe.ibm.com/software/globalization/gcoc/attachments/CP00300.pdf
      */
    createEbcdicToUnicodeTable(TwoByteTables300.mappingTableEbcdic300(), TwoByteTables300.mappingTableUnicode300())
  }
}
