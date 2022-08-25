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

package za.co.absa.cobrix.cobol.testutils

import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePageCommon

object EbcdicEncoder {

  // Routines to be used to generate test examples. Not to be used in production code.

  private val ebcdicToAsciiTable = (new CodePageCommon).getEbcdicToAsciiMapping

  private val asciiToEbcdicTable = {
    Range(0, 256).map(charCode => {
      val ind = ebcdicToAsciiTable.indexOf(charCode.toByte.toChar)
      if (ind >= 0) {
        ind.toByte
      } else {
        0x40.toByte
      }
    }).toArray
  }

  def toEbcdic(str: String): Array[Byte] = {
    var i = 0
    val buf = new Array[Byte](str.length)

    while (i < str.length) {
      buf(i) = asciiToEbcdicTable(str.charAt(i).toByte & 0xFF)
      i = i + 1
    }
    buf
  }

  def toScalaDefinition(bytes: Array[Byte]): String = {
    bytes.map(b =>
      s"0x${(b& 0xFF).toHexString}.toByte, "
    ).mkString
  }
}
