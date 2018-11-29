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

object BCDNumberDecoders {

  /**
    * Decode an integral binary encoded decimal (BCD) aka COMP-3 format to a String
    *
    * @param binBytes A byte array that represents the binary data
    * @param isSignSearate  if true the sign is contained in a separate byte
    * @param isSignLeading  if true the sign is contained in a first byte, otherwise at the last byte. This is only used if isSignSearate is true
    * @return A long representation of the binary data, null if the data is not properly formatted
    */
  def decodeBCDIntegralNumber(binBytes: Array[Byte], isSignSearate: Boolean, isSignLeading: Boolean): Any = {
    if (binBytes.length < 1) {
      return null
    }

    var applySign = 1
    val bytes = if (!isSignSearate) {
      binBytes
    }
    else {
      val (sign, bytes) = getSignAndBytes(binBytes, isSignLeading)
      applySign = sign
      bytes
    }

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
    applySign * outputNumber
  }

  /**
    * Decode a binary encoded decimal (BCD) aka COMP-3 format to a String
    *
    * @param binBytes A byte array that represents the binary data
    * @param scale A decimal scale if a number is a decimal. Should be greater or equal to zero
    * @param isSignSearate  if true the sign is contained in a separate byte
    * @param isSignLeading  if true the sign is contained in a first byte, otherwise at the last byte. This is only used if isSignSearate is true
    * @return A string representation of the binary data, null if the data is not properly formatted
    */
  def decodeBigBCDNumber(binBytes: Array[Byte], scale: Int, isSignSearate: Boolean, isSignLeading: Boolean): String = {
    if (scale < 0) {
      throw new IllegalArgumentException(s"Invalid scale=$scale, should be greater or equal to zero.")
    }
    if (binBytes.length < 1) {
      return null
    }

    var applySign = 1
    val bytes = if (!isSignSearate) {
      binBytes
    }
    else {
      val (sign, bytes) = getSignAndBytes(binBytes, isSignLeading)
      applySign = sign
      bytes
    }

    if (bytes.length < 1) {
      return null
    }

    var i: Int = 0
    var sign = ""
    val chars = new StringBuffer(bytes.length * 2 + 2)
    val decimalPointPosition = bytes.length * 2 - (scale + 1)

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
    if (scale > 0) chars.insert(decimalPointPosition, '.')
    if (isSignSearate) {
      if (applySign > 0)
        sign = ""
      else
        sign = "-"
    }
    chars.insert(0, sign)
    chars.toString
  }

  private def getSignAndBytes(bytes: Array[Byte], isLeading: Boolean): (Byte, Array[Byte]) = {
    val binBytes = if (isLeading)
      java.util.Arrays.copyOfRange(bytes, 1, bytes.length)
    else
      java.util.Arrays.copyOfRange(bytes, 0, bytes.length - 1)
    val signByte = if (isLeading) bytes(0) else bytes.last
    val sign: Byte = if (signByte == Constants.minusCharEBCIDIC || signByte == Constants.minusCharASCII) -1 else 1
    (sign, binBytes)
  }
}
