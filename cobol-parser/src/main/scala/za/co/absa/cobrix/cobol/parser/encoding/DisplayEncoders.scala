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

import java.math.RoundingMode

object DisplayEncoders {
  def encodeDisplayNumber(number: java.math.BigDecimal,
                         isSigned: Boolean,
                         outputSize: Int,
                         precision: Int,
                         scale: Int,
                         scaleFactor: Int,
                         explicitDecimalPoint: Boolean): Array[Byte] = {
    val bytes = new Array[Byte](outputSize)

    if (number == null || precision < 1 || scale < 0 || outputSize < 1 || (scaleFactor > 0 && scale > 0))
      return bytes

    val num = if (explicitDecimalPoint) {
      val shift = scaleFactor

      val bigDecimal = if (shift == 0)
        number.setScale(scale, RoundingMode.HALF_EVEN)
      else
        number.movePointLeft(shift).setScale(scale, RoundingMode.HALF_EVEN)

      val bigDecimalValue1 = bigDecimal.toString

      val bigDecimalValue = if (bigDecimalValue1.startsWith("0."))
        bigDecimalValue1.drop(1)
      else if (bigDecimalValue1.startsWith("-0."))
        "-" + bigDecimalValue1.drop(2)
      else
        bigDecimalValue1

      val bigDecimalValueLen = bigDecimalValue.length

      if (bigDecimalValueLen > outputSize || (!isSigned && bigDecimal.signum() < 0))
        return bytes

      bigDecimalValue
    } else {
      val shift = scaleFactor - scale

      val bigInt = if (shift == 0)
        number.setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact
      else
        number.movePointLeft(shift).setScale(0, RoundingMode.HALF_EVEN).toBigIntegerExact

      val intValue = bigInt.toString
      val intValueLen = intValue.length

      if (intValueLen > outputSize || (!isSigned && bigInt.signum() < 0))
        return bytes

      intValue
    }
    setPaddedEbcdicNumber(num, bytes)
    bytes
  }

  def setPaddedEbcdicNumber(num: String, array: Array[Byte]): Unit = {
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
