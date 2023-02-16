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
  * The base class for all single-byte EBCDIC decoders.
  */
abstract class SingleByteCodePage(ebcdicToAsciiMapping: Array[Char]) extends CodePage {
  private val ConversionTableElements = 256
  private val conversionTable = ebcdicToAsciiMapping

  if (conversionTable.length != ConversionTableElements) {
    throw new IllegalArgumentException(
      s"An EBCDIC to ASCII conversion table should have exactly $ConversionTableElements elements. It has ${conversionTable.length} elements.")
  }

  /**
    * Decodes bytes encoded as single byte EBCDIC code page to string.
    */
  final def convert(bytes: Array[Byte]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(conversionTable((bytes(i) + 256) % 256))
      i = i + 1
    }
    buf.toString
  }
}
