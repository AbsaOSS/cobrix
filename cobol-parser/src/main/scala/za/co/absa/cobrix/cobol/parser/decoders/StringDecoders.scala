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

import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage

import java.nio.charset.StandardCharsets
import scala.util.control.NonFatal

object StringDecoders {

  import StringTools._

  // Simple constants are used instead of enumeration for better performance
  val TrimNone  = 1
  val TrimLeft  = 2
  val TrimRight = 3
  val TrimBoth  = 4
  val KeepAll   = 5

  // Characters used for HEX conversion
  private val HEX_ARRAY = "0123456789ABCDEF".toCharArray

  /**
    * A decoder for any EBCDIC string fields (alphabetical or any char)
    *
    * @param bytes                 A byte array that represents the binary data
    * @param trimmingType          Specifies if and how the soutput string should be trimmed
    * @param codePage              EBCDIC code page
    * @param improvedNullDetection if true, return null if all bytes are zero
    * @return A string representation of the binary data
    */
  final def decodeEbcdicString(bytes: Array[Byte], trimmingType: Int, codePage: CodePage, improvedNullDetection: Boolean): String = {
    if (improvedNullDetection && trimmingType != KeepAll && isArrayNull(bytes))
      return null

    val str = codePage.convert(bytes)

    if (trimmingType == TrimNone || trimmingType == KeepAll ) {
      str
    } else if (trimmingType == TrimLeft) {
      StringTools.trimLeft(str)
    } else if (trimmingType == TrimRight) {
      StringTools.trimRight(str)
    } else {
      str.trim
    }
  }

  /**
    * A decoder for any ASCII string fields (alphabetical or any char)
    *
    * @param bytes                 A byte array that represents the binary data
    * @param trimmingType          Specifies if and how the soutput string should be trimmed
    * @param improvedNullDetection if true, return null if all bytes are zero
    * @return A string representation of the binary data
    */
  final def decodeAsciiString(bytes: Array[Byte], trimmingType: Int, improvedNullDetection: Boolean): String = {
    if (improvedNullDetection && trimmingType != KeepAll && isArrayNull(bytes))
      return null

    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      if (trimmingType == KeepAll || bytes(i) >= 32) {
        buf.append(bytes(i).toChar)
      } else if (bytes(i) < 0) {
        buf.append(' ')
      }
      i = i + 1
    }

    if (trimmingType == TrimNone || trimmingType == KeepAll) {
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
    * A decoder for any UTF-16 string field
    *
    * @param bytes                 A byte array that represents the binary data
    * @param trimmingType          Specifies if and how the soutput string should be trimmed
    * @param improvedNullDetection if true, return null if all bytes are zero
    * @return A string representation of the binary data
    */
  final def decodeUtf16String(bytes: Array[Byte], trimmingType: Int, isUtf16BigEndian: Boolean, improvedNullDetection: Boolean): String = {
    if (improvedNullDetection && isArrayNull(bytes))
      return null

    val utf16Str = if (isUtf16BigEndian) {
      new String(bytes, StandardCharsets.UTF_16BE)
    } else {
      new String(bytes, StandardCharsets.UTF_16LE)
    }

    if (trimmingType == TrimNone || trimmingType == KeepAll) {
      utf16Str
    } else if (trimmingType == TrimLeft) {
      StringTools.trimLeft(utf16Str)
    } else if (trimmingType == TrimRight) {
      StringTools.trimRight(utf16Str)
    } else {
      utf16Str.trim
    }
  }

  /**
    * A decoder for representing bytes as hex strings
    *
    * @param bytes A byte array that represents the binary data
    * @return A HEX string representation of the binary data
    */
  final def decodeHex(bytes: Array[Byte]): String = {
    val hexChars = new Array[Char](bytes.length * 2)
    var i = 0
    while (i < bytes.length) {
      val v = bytes(i) & 0xFF
      hexChars(i * 2) = HEX_ARRAY(v >>> 4)
      hexChars(i * 2 + 1) = HEX_ARRAY(v & 0x0F)
      i += 1
    }
    new String(hexChars)
  }

  /**
    * A decoder that doesn't decode, but just passes the bytes the way they are.
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the bytes
    */
  final def decodeRaw(bytes: Array[Byte]): Array[Byte] = bytes

  /**
    * A decoder for any EBCDIC uncompressed numbers supporting
    * <ul>
    * <li> Separate leading and trailing sign</li>
    * <li> Sign punched into the number</li>
    * <li> Explicit decimal point</li>
    * </ul>
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A string representation of the binary data
    */
  final def decodeEbcdicNumber(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): String = {
    if (improvedNullDetection && isNumberNull(bytes))
      return null

    import Constants._
    val buf = new StringBuffer(bytes.length + 1)
    var sign = ' '
    var malformed = false
    var i = 0
    while (i < bytes.length) {
      val b = bytes(i) & 0xFF
      var ch = ' '
      if (sign != ' ' && !relaxedOvepunch) {
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
      else if ((allowSignOverpunch || relaxedOvepunch) && b >= 0xC0 && b <= 0xC9) {
        ch = (b - 0xC0 + 0x30).toChar // positive sign punched
        sign = '+'
      }
      else if ((allowSignOverpunch || relaxedOvepunch) && b >= 0xD0 && b <= 0xD9) {
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
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed in first or last position
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A string representation of the binary data
    */
  final def decodeAsciiNumber(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): String = {
    val allowedDigitChars = " 0123456789"
    val punchedSignChars = "{ABCDEFGHI}JKLMNOPQR"

    if (improvedNullDetection && isNumberNull(bytes))
      return null

    val buf = new StringBuffer(bytes.length)
    var sign = ' '

    def decodeOverpunchedSign(char: Char): Unit = {
      val idx = punchedSignChars.indexOf(char)
      if (idx >= 10) {
        sign = '-'
        buf.append(('0'.toByte + idx - 10).toChar)
      } else {
        sign = '+'
        buf.append(('0'.toByte + idx).toChar)
      }
    }

    var i = 0
    while (i < bytes.length) {
      val char = bytes(i).toChar
      if (allowedDigitChars.contains(char))
        buf.append(char)
      else {
        if (char == '-' || char == '+') {
          sign = char
        } else if (char == '.' || char == ',') {
          buf.append('.')
        } else {
          if (relaxedOvepunch) {
            if (punchedSignChars.contains(char)) {
              decodeOverpunchedSign(char)
            } else {
              return null
            }
          } else {
            if (allowSignOverpunch && (i == 0 || i == bytes.length - 1) && punchedSignChars.contains(char)) {
              decodeOverpunchedSign(char)
            } else {
              return null
            }
          }
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
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A boxed integer
    */
  final def decodeEbcdicInt(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): Integer = {
    try {
      decodeEbcdicNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection).toInt
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an ASCII string converting it to an integer
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed in first or last position
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A boxed integer
    */
  final def decodeAsciiInt(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): Integer = {
    try {
      decodeAsciiNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection).toInt
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an EBCDIC string converting it to an long
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @return A boxed long
    */
  final def decodeEbcdicLong(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, improvedNullDetection: Boolean, relaxedOvepunch: Boolean): java.lang.Long = {
    try {
      decodeEbcdicNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection).toLong
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode integral number from an ASCII string converting it to an long
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed in first or last position
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A boxed long
    */
  final def decodeAsciiLong(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): java.lang.Long = {
    try {
      decodeAsciiNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection).toLong
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode a number from an EBCDIC string converting it to a big decimal
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @param scale                 A decimal scale in case decimal number with implicit decimal point is expected
    * @param scaleFactor           Additional zeros to be added before of after the decimal point
    * @return A big decimal containing a big integral number
    */
  final def decodeEbcdicBigNumber(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean, scale: Int = 0, scaleFactor: Int = 0): BigDecimal = {
    try {
      BigDecimal(BinaryUtils.addDecimalPoint(decodeEbcdicNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection), scale, scaleFactor))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode a number from an ASCII string converting it to a big decimal
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed in first or last position
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @param scale                 A decimal scale in case decimal number with implicit decimal point is expected
    * @param scaleFactor           Additional zeros to be added before of after the decimal point
    * @return A big decimal containing a big integral number
    */
  final def decodeAsciiBigNumber(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean, scale: Int = 0, scaleFactor: Int = 0): BigDecimal = {
    try {
      BigDecimal(BinaryUtils.addDecimalPoint(decodeAsciiNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection), scale, scaleFactor))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode decimal number from an EBCDIC string converting it to a big decimal
    * This decoder is used to convert decimal numbers with explicit decimal point
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed in first or last position
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A big decimal containing a big integral number
    */
  final def decodeEbcdicBigDecimal(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): BigDecimal = {
    try {
      BigDecimal(decodeEbcdicNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection))
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode decimal number from an ASCII string converting it to a big decimal
    * This decoder is used to convert decimal numbers with explicit decimal point
    *
    * @param bytes                 A byte array that represents the binary data
    * @param isUnsigned            If true, negative numbers will be considered invalid and return null
    * @param allowSignOverpunch    If true, sign overpunching is allowed in first or last position
    * @param relaxedOvepunch       If true, multiple published signs are allowed. The last is going to be effective
    * @param improvedNullDetection If true, return null if all bytes are zero
    * @return A big decimal containing a big integral number
    */
  final def decodeAsciiBigDecimal(bytes: Array[Byte], isUnsigned: Boolean, allowSignOverpunch: Boolean, relaxedOvepunch: Boolean, improvedNullDetection: Boolean): BigDecimal = {
    try {
      BigDecimal(decodeAsciiNumber(bytes, isUnsigned, allowSignOverpunch, relaxedOvepunch, improvedNullDetection))
    } catch {
      case NonFatal(_) => null
    }
  }
}
