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

object BCDNumberDecoders {

  /**
    * Decode an integral binary encoded decimal (BCD) aka COMP-3 format to a String
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed long representation of the binary data, null if the data is not properly formatted
    */
  def decodeBCDIntegralNumber(bytes: Array[Byte]): java.lang.Long = {
    if (bytes.length < 1) {
      return null
    }

    var sign: Byte = 1
    var outputNumber: Long = 0

    var i: Int = 0
    while (i < bytes.length) {
      val b = bytes(i)
      val lowNibble = b & 0x0f
      val highNibble = (b >> 4) & 0x0f
      if (highNibble >= 0 && highNibble < 10) {
        outputNumber = outputNumber * 10 + highNibble
      }
      else {
        // invalid nibble encountered - the format is wrong
        return null
      }

      if (i + 1 == bytes.length) {
        // The last nibble is a sign
        sign = lowNibble match {
          case 0x0C => 1 // +, signed
          case 0x0D => -1
          case 0x0F => 1 // +, unsigned
          case _ =>
            // invalid nibble encountered - the format is wrong
            return null
        }
      }
      else {
        if (lowNibble >= 0 && lowNibble < 10) {
          outputNumber = outputNumber * 10 + lowNibble
        }
        else {
          // invalid nibble encountered - the format is wrong
          return null
        }
      }
      i = i + 1
    }
    sign * outputNumber
  }

  /**
    * Decode a binary encoded decimal (BCD) aka COMP-3 format to a String
    *
    * @param bytes A byte array that represents the binary data
    * @param scale A decimal scale if a number is a decimal. Should be greater or equal to zero
    * @param scaleFactor Additional zeros to be added before of after the decimal point
    * @return A string representation of the binary data, null if the data is not properly formatted
    */
  def decodeBigBCDNumber(bytes: Array[Byte], scale: Int, scaleFactor: Int): String = {
    if (scale < 0) {
      throw new IllegalArgumentException(s"Invalid scale=$scale, should be greater or equal to zero.")
    }
    if (bytes.length < 1) {
      return null
    }

    var sign = ""

    val intendedDecimalPosition = bytes.length * 2 - (scale + 1)

    val additionalZeros = if (intendedDecimalPosition <= 0) {
      -intendedDecimalPosition + 1
    } else {
      0
    }

    val chars = new StringBuffer(bytes.length * 2 + 2 + additionalZeros)
    val decimalPointPosition = bytes.length * 2 - (scale + 1) + additionalZeros

    var i: Int = 0
    while (i < additionalZeros) {
      chars.append('0')
      i += 1
    }

    i = 0
    while (i < bytes.length) {
      val b = bytes(i)
      val lowNibble = b & 0x0f
      val highNibble = (b >> 4) & 0x0f
      if (highNibble >= 0 && highNibble < 10) {
        chars.append(('0'.toByte + highNibble).toChar)
      }
      else {
        // invalid nibble encountered - the format is wrong
        return null
      }

      if (i + 1 == bytes.length) {
        // The last nibble is a sign
        sign = lowNibble match {
          case 0x0C => "" // +, signed
          case 0x0D => "-"
          case 0x0F => "" // +, unsigned
          case _ =>
            // invalid nibble encountered - the format is wrong
            return null
        }
      }
      else {
        if (lowNibble >= 0 && lowNibble < 10) {
          chars.append(('0'.toByte + lowNibble).toChar)
        }
        else {
          // invalid nibble encountered - the format is wrong
          return null
        }
      }
      i = i + 1
    }
    if (scaleFactor == 0) {
      if (scale > 0) chars.insert(decimalPointPosition, '.')
      chars.insert(0, sign)
      chars.toString
    } else {
      if (scaleFactor < 0) {
        val zeros = "0" * (-scaleFactor)
        s"${sign}0.$zeros${chars.toString}"
      } else {
        val zeros = "0" * scaleFactor
        chars.insert(0, sign)
        s"${chars.toString}$zeros"
      }
    }
  }

  /** Malformed data does not cause exceptions in Spark. Null values are returned instead */
  def decodeBigBCDDecimal(binBytes: Array[Byte], scale: Int, scaleFactor: Int): BigDecimal = {
    try {
      BigDecimal(decodeBigBCDNumber(binBytes, scale, scaleFactor))
    } catch {
      case NonFatal(_) => null
    }
  }
}
