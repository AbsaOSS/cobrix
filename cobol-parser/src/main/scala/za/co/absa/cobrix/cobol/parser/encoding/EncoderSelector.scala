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

package za.co.absa.cobrix.cobol.parser.encoding

import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType}
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}

import java.nio.charset.{Charset, StandardCharsets}

object EncoderSelector {
  type Encoder = Any => Array[Byte]

  def getEncoder(dataType: CobolType,
                 ebcdicCodePage: CodePage = new CodePageCommon,
                 asciiCharset: Charset = StandardCharsets.US_ASCII): Option[Encoder] = {
    dataType match {
      case alphaNumeric: AlphaNumeric if alphaNumeric.compact.isEmpty =>
        getStringEncoder(alphaNumeric.enc.getOrElse(EBCDIC), ebcdicCodePage, asciiCharset, alphaNumeric.length)
      case _ =>
        None
    }
  }

  /** Gets a decoder function for a string data type. Encoder is chosen depending on whether input encoding is EBCDIC or ASCII */
  private def getStringEncoder(encoding: Encoding,
                               ebcdicCodePage: CodePage,
                               asciiCharset: Charset,
                               fieldLength: Int
                              ): Option[Encoder] = {
    encoding match {
      case EBCDIC =>
        val encoder = (a: Any) => {
          encodeEbcdicString(a.toString, CodePageCommon.asciiToEbcdicMapping, fieldLength)
        }
        Option(encoder)
      case ASCII =>
        None
      case _ =>
        None
    }
  }

  /**
    * An encoder from a ASCII basic string to an EBCDIC byte array
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
