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

package za.co.absa.cobrix.cobol.parser.encoders

object StringEncoders {
  /**
    * An encoder from a string to an EBCDIC byte array
    *
    * @param string          An input string
    * @param conversionTable A conversion table to use to convert from ASCII to EBCDIC
    * @param length          The length of the output (in bytes)
    * @return A string representation of the binary data
    */
  def encodeEbcdicString(string: String, conversionTable: Array[Byte], length: Int): Array[Byte] = {
    require(length >= 0, s"Field length cannot be negative, got $length")

    var i = 0
    val buf = new Array[Byte](length)

    while (i < string.length && i < length) {
      val asciiByte = string(i).toByte
      buf(i) = conversionTable((asciiByte + 256) % 256)
      i = i + 1
    }
    buf
  }

}
