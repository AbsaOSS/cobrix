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
    * Byte indicates the transition to the shifted state
    */
  val TO_SHIFTED_STATE = 0x0E
  /**
    * Byte indicates the transition to the normal (non-shifted) state
    */
  val FROM_SHIFTED_STATE = 0x0F

  /**
    * Decodes bytes encoded as single byte EBCDIC code page to string.
    */
  final def convert(bytes: Array[Byte]): String = {
    val tableLen = ebcdicToAsciiMapping.length
    val bytesLen = bytes.length

    var offset = 0
    var shiftedState = false

    def readSingleByte: Int = {
      val byte = bytes(offset)
      offset += 1

      if (byte == TO_SHIFTED_STATE) {
        shiftedState = true
        if (offset < bytesLen) {
          readDoubleByte
        }
        else
          0
      } else {
        (byte + 256) % 256
      }
    }

    def readDoubleByte: Int = {
      val byte1 = bytes(offset)
      offset += 1

      if (byte1 == FROM_SHIFTED_STATE) {
        shiftedState = false
        readSingleByte
      } else {
        val byte2 = if (offset < bytesLen) {
          bytes(offset)
        } else {
          return 0
        }

        offset += 1
        (byte1 + 256) % 256 * 256 + (byte2 + 256) % 256
      }
    }

    val buf = new StringBuilder(bytes.length)
    while (offset < bytesLen) {
      val index = if (shiftedState)
        readDoubleByte
      else
        readSingleByte

      if (index < tableLen) {
        val c = ebcdicToAsciiMapping(index)
        if (c != 0.toChar) {
          buf.append(ebcdicToAsciiMapping(index))
        }
      }
    }
    buf.toString
  }
}
