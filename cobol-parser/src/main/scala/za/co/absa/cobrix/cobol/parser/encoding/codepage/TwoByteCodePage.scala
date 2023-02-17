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
  * The base class for all two-byte EBCDIC decoders.
  */
abstract class TwoByteCodePage(ebcdicToAsciiMapping: Array[Char]) extends CodePage {
  /**
    * Decodes bytes encoded as single byte EBCDIC code page to string.
    */
  final def convert(bytes: Array[Byte]): String = {
    val tableLen = ebcdicToAsciiMapping.length
    val outputLen = bytes.length / 2

    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < outputLen) {
      val byte1 = bytes(i * 2)
      val byte2 = bytes(i * 2 + 1)
      val index = (byte1 + 256) % 256 * 256 + (byte2 + 256) % 256

      if (index < tableLen) {
        buf.append(ebcdicToAsciiMapping(index))
      }
      i = i + 1
    }
    buf.toString
  }
}
