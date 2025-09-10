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

import za.co.absa.cobrix.cobol.parser.position.Position

import java.math.RoundingMode

object DisplayEncoders {
  def encodeDisplayNumberSignSeparate(number: java.math.BigDecimal,
                                      signPosition: Option[Position],
                                      outputSize: Int,
                                      precision: Int,
                                      scale: Int,
                                      scaleFactor: Int,
                                      explicitDecimalPoint: Boolean): Array[Byte] = {
    val isSigned = signPosition.isDefined
    val lengthAdjustment = if (isSigned) 1 else 0
    val isSignPositionRight = signPosition.contains(za.co.absa.cobrix.cobol.parser.position.Right)
    val isNegative = number.signum() < 0
    val bytes = new Array[Byte](outputSize)

    if (number == null || precision < 1 || scale < 0 || outputSize < 1 || (scaleFactor > 0 && scale > 0))
      return bytes

    val num = if (explicitDecimalPoint) {
      val shift = scaleFactor

      val bigDecimal = if (shift == 0)
        number.abs().setScale(scale, RoundingMode.HALF_EVEN)
      else
        number.abs().movePointLeft(shift).setScale(scale, RoundingMode.HALF_EVEN)

      val bigDecimalValue1 = bigDecimal.toString

      val bigDecimalValue = if (bigDecimalValue1.startsWith("0."))
        bigDecimalValue1.drop(1)
      else
        bigDecimalValue1

      val bigDecimalValueLen = bigDecimalValue.length + lengthAdjustment

      if (bigDecimalValueLen > outputSize || (!isSigned && isNegative))
        return bytes

      bigDecimalValue
    } else {
      val shift = scaleFactor - scale

      val bigInt = if (shift == 0)
        number.abs().setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact
      else
        number.abs().movePointLeft(shift).setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact

      val intValue = bigInt.toString
      val intValueLen = intValue.length + lengthAdjustment

      if (intValueLen > outputSize || (!isSigned && isNegative))
        return bytes

      intValue
    }
    setPaddedEbcdicNumberWithSignSeparate(num, isSigned, isNegative, isSignPositionRight, bytes)
    bytes
  }

  def encodeDisplayNumberSignOverpunched(number: java.math.BigDecimal,
                                         signPosition: Option[Position],
                                         outputSize: Int,
                                         precision: Int,
                                         scale: Int,
                                         scaleFactor: Int,
                                         explicitDecimalPoint: Boolean): Array[Byte] = {
    val isSigned = signPosition.isDefined
    val isNegative = number.signum() < 0
    val bytes = new Array[Byte](outputSize)


    if (number == null || precision < 1 || scale < 0 || outputSize < 1 || (scaleFactor > 0 && scale > 0))
      return bytes

    val num = if (explicitDecimalPoint) {
      val shift = scaleFactor

      val bigDecimal = if (shift == 0)
        number.abs().setScale(scale, RoundingMode.HALF_EVEN)
      else
        number.abs().movePointLeft(shift).setScale(scale, RoundingMode.HALF_EVEN)

      val bigDecimalValue1 = bigDecimal.toString

      val bigDecimalValue = if (bigDecimalValue1.startsWith("0."))
        bigDecimalValue1.drop(1)
      else
        bigDecimalValue1

      val bigDecimalValueLen = bigDecimalValue.length

      if (bigDecimalValueLen > outputSize || (!isSigned && isNegative))
        return bytes

      bigDecimalValue
    } else {
      val shift = scaleFactor - scale

      val bigInt = if (shift == 0)
        number.abs().setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact
      else
        number.abs().movePointLeft(shift).setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact

      val intValue = bigInt.toString
      val intValueLen = intValue.length

      if (intValueLen > outputSize || (!isSigned && isNegative))
        return bytes

      intValue
    }
    setPaddedEbcdicNumberWithSignOverpunched(num, isSigned, isNegative, bytes)
    bytes
  }

  def setPaddedEbcdicNumberWithSignOverpunched(num: String, isSigned: Boolean, isNegative: Boolean, array: Array[Byte]): Unit = {
    val numLen = num.length
    val arLen = array.length

    if (numLen > arLen)
      return

    var i = 0
    while (i < arLen) {
      var ebcdic = 0x40.toByte

      if (i == 0) {
        // Signal overpunching
        val c = num(numLen - i - 1)
        if (c >= '0' && c <= '9') {
          val digit = c - '0'
          val zone = if (!isSigned) {
            0xF
          } else if (isNegative) {
            0xD
          } else {
            0xC
          }

          ebcdic = ((zone << 4) | digit).toByte
        }
      } else if (i < numLen) {
        val c = num(numLen - i - 1)
        if (c >= '0' && c <= '9') {
          ebcdic = ((c - '0') + 0xF0).toByte
        } else if (c == '.' || c == ',') {
          ebcdic = 0x4B
        }
      }

      array(arLen - i - 1) = ebcdic
      i += 1
    }
  }

  def setPaddedEbcdicNumberWithSignSeparate(absNum: String, isSigned: Boolean, isNegative: Boolean, isSignPositionRight: Boolean, array: Array[Byte]): Unit = {
    val num = if (isSigned) {
      if (isNegative) {
        if (isSignPositionRight) {
          s"$absNum-"
        } else {
          s"-$absNum"
        }
      } else {
        absNum
      }
    } else {
      absNum
    }
    val numLen = num.length
    val arLen = array.length

    if (numLen > arLen)
      return

    var i = 0
    while (i < arLen) {
      var ebcdic = 0x40.toByte

      if (i < numLen) {
        val c = num(numLen - i - 1)
        if (c >= '0' && c <= '9') {
          ebcdic = ((c - '0') + 0xF0).toByte
        } else if (c == '.' || c == ',') {
          ebcdic = 0x4B
        } else if (c == '-') {
          ebcdic = 0x60
        }
      }

      array(arLen - i - 1) = ebcdic
      i += 1
    }
  }
}
