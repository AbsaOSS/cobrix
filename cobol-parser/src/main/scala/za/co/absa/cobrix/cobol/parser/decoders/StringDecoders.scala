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

package za.co.absa.cobrix.cobol.parser.decoders

import scala.util.control.NonFatal

object StringDecoders {

  /**
    * A decoder for any EBCDIC string fields (alphabetical or any char)
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeEbcdicString(bytes: Array[Byte]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(BinaryUtils.ebcdic2ascii((bytes(i) + 256) % 256))
      i = i + 1
    }
    buf.toString.trim
  }

  /**
    * A decoder for any ASCII string fields (alphabetical or any char)
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeAsciiString(bytes: Array[Byte]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(bytes(i).toChar)
      i = i + 1
    }
    buf.toString.trim
  }

  /**
    * A decoder for any EBCDIC uncompressed numbers supporting leading and trailing sign
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeEbcdicNumber(bytes: Array[Byte]): String = {
    val buf = new StringBuffer(bytes.length + 1)
    var sign = ' '
    var i = 0
    while (i < bytes.length) {
      val char = BinaryUtils.ebcdic2ascii((bytes(i) + 256) % 256)
      if (char == '-' || char == '+') {
        sign = char
      } else {
        buf.append(char)
      }
      i = i + 1
    }
    if (sign != ' ')
      sign + buf.toString.trim
    else
      buf.toString.trim
  }

  /**
    * A decoder for any ASCII uncompressed numbers supporting leading and trailing sign
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeAsciiNumber(bytes: Array[Byte]): String = {
    val buf = new StringBuffer(bytes.length)
    var sign = ' '
    var i = 0
    while (i < bytes.length) {
      val char = bytes(i).toChar
      if (char == '-' || char == '+') {
        sign = char
      } else {
        buf.append(char)
      }
      i = i + 1
    }
    if (sign != ' ')
      sign + buf.toString.trim
    else
      buf.toString.trim
  }

  /**
    * Decode integral number from an EBCDIC string converting it to an integer
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed integer
    */
  def decodeEbcdicInt(bytes: Array[Byte]): Integer = {
    try {
      decodeEbcdicNumber(bytes).toInt
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an ASCII string converting it to an integer
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed integer
    */
  def decodeAsciiInt(bytes: Array[Byte]): Integer = {
    try {
      decodeAsciiNumber(bytes).toInt
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an EBCDIC string converting it to an long
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed long
    */
  def decodeEbcdicLong(bytes: Array[Byte]): java.lang.Long = {
    try {
      decodeEbcdicNumber(bytes).toLong
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an ASCII string converting it to an long
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed long
    */
  def decodeAsciiLong(bytes: Array[Byte]): java.lang.Long = {
    try {
      decodeAsciiNumber(bytes).toLong
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an EBCDIC string converting it to a big decimal
    *
    * @param bytes A byte array that represents the binary data
    * @param scale A decimal scale in case decimal number with implicit decimal point is expected
    * @return A big decimal containing a big integral number
    */
  def decodeEbcdicBigNumber(bytes: Array[Byte], scale: Int = 0): BigDecimal = {
    try {
      BigDecimal(BinaryUtils.addDecimalPoint(decodeEbcdicNumber(bytes), scale))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an ASCII string converting it to a big decimal
    *
    * @param bytes A byte array that represents the binary data
    * @param scale A decimal scale in case decimal number with implicit decimal point is expected
    * @return A big decimal containing a big integral number
    */
  def decodeAsciiBigNumber(bytes: Array[Byte], scale: Int = 0): BigDecimal = {
    try {
      BigDecimal(BinaryUtils.addDecimalPoint(decodeAsciiNumber(bytes), scale))
    } catch {
      case NonFatal(_) => null
    }
  }

}
