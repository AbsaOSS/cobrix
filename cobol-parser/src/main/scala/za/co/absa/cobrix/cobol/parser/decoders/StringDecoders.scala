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

import java.nio.charset.{Charset, StandardCharsets}

import za.co.absa.cobrix.cobol.parser.common.Constants

import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

object StringDecoders {

  // Simple constants are used instead of enumeration for better performance
  val TrimNone  = 1
  val TrimLeft  = 2
  val TrimRight = 3
  val TrimBoth  = 4

  /**
    * A decoder for any EBCDIC string fields (alphabetical or any char)
    *
    * @param bytes        A byte array that represents the binary data
    * @param trimmingType Specifies if and how the soutput string should be trimmed
    * @param conversionTable A conversion table to use to convert from EBCDIC to ASCII
    * @return A string representation of the binary data
    */
  def decodeEbcdicString(bytes: Array[Byte], trimmingType: Int, conversionTable: Array[Char]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(conversionTable((bytes(i) + 256) % 256))
      i = i + 1
    }

    if (trimmingType == TrimNone) {
      buf.toString
    } else if (trimmingType == TrimLeft) {
      StringTools.trimLeft(buf.toString)
    } else if (trimmingType == TrimRight) {
      StringTools.trimRight(buf.toString)
    } else {
      buf.toString.trim
    }
  }

  /**
    * A decoder for any ASCII string fields (alphabetical or any char)
    *
    * @param bytes        A byte array that represents the binary data
    * @param trimmingType Specifies if and how the soutput string should be trimmed
    * @return A string representation of the binary data
    */
  def decodeAsciiString(bytes: Array[Byte], trimmingType: Int): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      if (bytes(i) < 32 /*Special and high order characters are masked*/ )
        buf.append(' ')
      else
        buf.append(bytes(i).toChar)
      i = i + 1
    }
    if (trimmingType == TrimNone) {
      buf.toString
    } else if (trimmingType == TrimLeft) {
      StringTools.trimLeft(buf.toString)
    } else if (trimmingType == TrimRight) {
      StringTools.trimRight(buf.toString)
    } else {
      buf.toString.trim
    }
  }

  /**
    * A decoder for any EBCDIC uncompressed numbers supporting
    * <ul>
    * <li> Separate leading and trailing sign</li>
    * <li> Sign punched into the number</li>
    * <li> Explicit decimal point</li>
    * </ul>
    *
    * @param bytes      A byte array that represents the binary data
    * @param isUnsigned Is the number expected to be unsigned
    * @return A string representation of the binary data
    */
  def decodeEbcdicNumber(bytes: Array[Byte], isUnsigned: Boolean): String = {
    import Constants._
    val buf = new StringBuffer(bytes.length + 1)
    var sign = ' '
    var malformed = false
    var i = 0
    while (i < bytes.length) {
      val b = bytes(i) & 0xFF
      var ch = ' '
      if (sign != ' ') {
        // Handle characters after a sign character is encountered
        if (b >= 0xF0 && b <= 0xF9) {
          ch = (b - 0xF0 + 0x30).toChar // unsigned
        } else if (b == dotCharEBCIDIC || b == commaCharEBCIDIC) {
          ch = '.'
        }
        else if (b == spaceCharEBCIDIC || b == 0) {
          ch = ' '
        }
        else {
          malformed = true
        }
      } else if (b >= 0xF0 && b <= 0xF9) {
        ch = (b - 0xF0 + 0x30).toChar // unsigned
      }
      else if (b >= 0xC0 && b <= 0xC9) {
        ch = (b - 0xC0 + 0x30).toChar // positive sign punched
        sign = '+'
      }
      else if (b >= 0xD0 && b <= 0xD9) {
        ch = (b - 0xD0 + 0x30).toChar // negative sign punched
        sign = '-'
      }
      else if (b == minusCharEBCIDIC) {
        sign = '-'
      }
      else if (b == plusCharEBCIDIC) {
        sign = '+'
      }
      else if (b == dotCharEBCIDIC || b == commaCharEBCIDIC) {
        ch = '.'
      }
      else if (b == spaceCharEBCIDIC || b == 0) {
        ch = ' '
      }
      else
        malformed = true
      if (ch != ' ') {
        buf.append(ch)
      }
      i = i + 1
    }
    if (malformed)
      null
    else if (sign != ' ')
      if (sign == '-' && isUnsigned) null else sign + buf.toString.trim
    else
      buf.toString
  }

  /**
    * A decoder for any ASCII uncompressed numbers supporting leading and trailing sign
    *
    * @param bytes      A byte array that represents the binary data
    * @param isUnsigned Is the number expected to be unsigned
    * @return A string representation of the binary data
    */
  def decodeAsciiNumber(bytes: Array[Byte], isUnsigned: Boolean): String = {
    val buf = new StringBuffer(bytes.length)
    var sign = ' '
    var i = 0
    while (i < bytes.length) {
      val char = bytes(i).toChar
      if (char == '-' || char == '+') {
        sign = char
      } else {
        if (char == '.' || char == ',') {
          buf.append('.')
        } else {
          buf.append(char)
        }
      }
      i = i + 1
    }
    if (sign != ' ') {
      if (sign == '-' && isUnsigned) null else sign + buf.toString.trim
    }
    else
      buf.toString.trim
  }

  /**
    * Decode integral number from an EBCDIC string converting it to an integer
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed integer
    */
  def decodeEbcdicInt(bytes: Array[Byte], isUnsigned: Boolean): Integer = {
    try {
      decodeEbcdicNumber(bytes, isUnsigned).toInt
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
  def decodeAsciiInt(bytes: Array[Byte], isUnsigned: Boolean): Integer = {
    try {
      decodeAsciiNumber(bytes, isUnsigned).toInt
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
  def decodeEbcdicLong(bytes: Array[Byte], isUnsigned: Boolean): java.lang.Long = {
    try {
      decodeEbcdicNumber(bytes, isUnsigned).toLong
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
  def decodeAsciiLong(bytes: Array[Byte], isUnsigned: Boolean): java.lang.Long = {
    try {
      decodeAsciiNumber(bytes, isUnsigned).toLong
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode a number from an EBCDIC string converting it to a big decimal
    *
    * @param bytes A byte array that represents the binary data
    * @param scale A decimal scale in case decimal number with implicit decimal point is expected
    * @param scaleFactor Additional zeros to be added before of after the decimal point
    * @return A big decimal containing a big integral number
    */
  def decodeEbcdicBigNumber(bytes: Array[Byte], isUnsigned: Boolean, scale: Int = 0, scaleFactor: Int = 0): BigDecimal = {
    try {
      BigDecimal(BinaryUtils.addDecimalPoint(decodeEbcdicNumber(bytes, isUnsigned), scale, scaleFactor))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode a number from an ASCII string converting it to a big decimal
    *
    * @param bytes A byte array that represents the binary data
    * @param scale A decimal scale in case decimal number with implicit decimal point is expected
    * @param scaleFactor Additional zeros to be added before of after the decimal point
    * @return A big decimal containing a big integral number
    */
  def decodeAsciiBigNumber(bytes: Array[Byte], isUnsigned: Boolean, scale: Int = 0, scaleFactor: Int = 0): BigDecimal = {
    try {
      BigDecimal(BinaryUtils.addDecimalPoint(decodeAsciiNumber(bytes, isUnsigned), scale, scaleFactor))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode decimal number from an EBCDIC string converting it to a big decimal
    * This decoder is used to convert decimal numbers with explicit decimal point
    *
    * @param bytes A byte array that represents the binary data
    * @return A big decimal containing a big integral number
    */
  def decodeEbcdicBigDecimal(bytes: Array[Byte], isUnsigned: Boolean): BigDecimal = {
    try {
      BigDecimal(decodeEbcdicNumber(bytes, isUnsigned))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode decimal number from an ASCII string converting it to a big decimal
    * This decoder is used to convert decimal numbers with explicit decimal point
    *
    * @param bytes A byte array that represents the binary data
    * @return A big decimal containing a big integral number
    */
  def decodeAsciiBigDecimal(bytes: Array[Byte], isUnsigned: Boolean): BigDecimal = {
    try {
      BigDecimal(decodeAsciiNumber(bytes, isUnsigned))
    } catch {
      case NonFatal(_) => null
    }
  }

}
