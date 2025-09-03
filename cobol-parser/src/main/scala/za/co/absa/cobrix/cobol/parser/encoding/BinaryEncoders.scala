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

object BinaryEncoders {
  def encodeBinaryNumber(number: java.math.BigDecimal,
                         isSigned: Boolean,
                         outputSize: Int,
                         bigEndian: Boolean,
                         precision: Int,
                         scale: Int,
                         scaleFactor: Int): Array[Byte] = {
    val bytes = new Array[Byte](outputSize)

    if (number == null || precision < 1 || scale < 0 || outputSize < 1)
      return bytes

    val shift = scaleFactor - scale
    val bigInt = if (shift == 0)
      number.setScale(0, RoundingMode.HALF_DOWN).toBigIntegerExact
    else
      number.movePointLeft(shift).setScale(0, RoundingMode.HALF_DOWN).toBigIntegerExact

    val intValue = bigInt.toByteArray
    val intValueLen = intValue.length

    if (intValueLen > outputSize || (!isSigned && bigInt.signum() < 0))
      return bytes

    val paddingByte = if (bigInt.signum() < 0) 0xFF.toByte else 0x00.toByte

    if (bigEndian) {
      var i = 0
      while (i < outputSize) {
        if (i < intValueLen) {
          bytes(outputSize - i - 1) = intValue(intValueLen - i - 1)
        } else {
          bytes(outputSize - i - 1) = paddingByte
        }
        i += 1
      }
    } else {
      var i = 0
      while (i < outputSize) {
        if (i < intValueLen) {
          bytes(i) = intValue(intValueLen - i - 1)
        } else {
          bytes(i) = paddingByte
        }
        i += 1
      }
    }
    bytes
  }
}
