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

object ebcdicEncoder {

  // Routines to be used to generate test examples. Not to be used in production code.

  def encodeEbcdicString(str: String, conversionTable: Array[Char]): Array[Byte] = {
    var i = 0
    val buf = new Array[Byte](str.length)

    while (i < str.length) {
      val ind = conversionTable.indexOf(str.charAt(i))

      if (ind >= 0) {
        buf(i) = ind.toByte
      } else {
        buf(i) = 0x40
      }
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
