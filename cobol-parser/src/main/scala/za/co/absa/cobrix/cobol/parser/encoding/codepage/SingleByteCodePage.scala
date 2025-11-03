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

import java.util

/**
  * The base class for all single-byte EBCDIC decoders.
  */
abstract class SingleByteCodePage(ebcdicToAsciiMapping: Array[Char])
  extends CodePage {
  private val ConversionTableElements = 256
  private val conversionTable = ebcdicToAsciiMapping

  if (conversionTable.length != ConversionTableElements) {
    throw new IllegalArgumentException(
      s"An EBCDIC to ASCII conversion table should have exactly $ConversionTableElements elements. It has ${conversionTable.length} elements.")
  }

  /**
    * Decodes bytes encoded as single byte EBCDIC code page to string.
    */
  final override def convert(bytes: Array[Byte]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(conversionTable((bytes(i) + 256) % 256))
      i = i + 1
    }
    buf.toString
  }

  /**
   * An encoder from a ASCII basic string to an EBCDIC byte array
   *
   * @param string          An input string
   * @param length          The length of the output (in bytes)
   * @return A string representation of the binary data
   */
  final override def convert(string: String, length: Int): Array[Byte] = {
    require(length >= 0, s"Field length cannot be negative, got $length")

    var i = 0
    val buf = new Array[Byte](length)

    // PIC X fields are space-filled on mainframe. Use EBCDIC space 0x40.
    util.Arrays.fill(buf, 0x40.toByte)

    while (i < string.length && i < length) {
      buf(i) = reverseEbcdicToAsciiMapping.getOrElse(string.charAt(i), 0x40.toByte)
      i = i + 1
    }
    buf
  }

  override def supportsEncoding: Boolean = true

  lazy val reverseEbcdicToAsciiMapping: Map[Char, Byte] = {
    val map = scala.collection.mutable.Map[Char, Byte]()
    for (i <- ebcdicToAsciiMapping.indices) {
      val asciiChar = ebcdicToAsciiMapping(i)
      if (!map.contains(asciiChar)) {
        map(asciiChar) = i.toByte
      }
    }
    map.toMap
  }
}
