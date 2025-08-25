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

object BCDNumberEncoders {
  /**
    * Encode a number as a binary encoded decimal (BCD) aka COMP-3 format to an array of bytes.
    *
    * Output length (bytes):
    * - With mandatory sign nibble (signed or unsigned): ceil((precision + 1) / 2)
    * - Unsigned without sign nibble: ceil(precision / 2).
    *
    * @param number              The number to encode.
    * @param precision           Total number of digits in the number.
    * @param scale               A decimal scale if a number is a decimal. Should be greater or equal to zero.
    * @param scaleFactor         Additional zeros to be added before of after the decimal point.
    * @param signed              if true, sign nibble is added and negative numbers are supported.
    * @param mandatorySignNibble If true, the BCD number should contain the sign nibble. Otherwise, the number is
    *                            considered unsigned, and negative numbers are encoded as null (zero bytes).
    * @return A BCD representation of the number, array of zero bytes if the data is not properly formatted.
    */
  def encodeBCDNumber(number: java.math.BigDecimal,
                      precision: Int,
                      scale: Int,
                      scaleFactor: Int,
                      signed: Boolean,
                      mandatorySignNibble: Boolean): Array[Byte] = {
    if (precision < 1)
      throw new IllegalArgumentException(s"Invalid BCD precision=$precision, should be greater than zero.")

    val totalDigits = if (mandatorySignNibble) {
      if (precision % 2 == 0) precision + 2 else precision + 1
    } else {
      if (precision % 2 == 0) precision else precision + 1
    }

    val byteCount = totalDigits / 2
    val bytes = new Array[Byte](byteCount)

    if (number == null) {
      return bytes
    }

    val shift = scaleFactor - scale
    val shifted = if (shift == 0) number else number.movePointLeft(shift)

    val isNegative = number.signum() < 0
    val digitsOnly = shifted.abs().setScale(0, RoundingMode.HALF_DOWN).toPlainString

    if (isNegative && (!signed || !mandatorySignNibble)) {
      return bytes
    }

    if (digitsOnly.length > precision || scale < 0)
      return bytes

    val signNibble: Byte =  if (signed) {
      if (isNegative) 0x0D else 0x0C
    } else {
      0x0F
    }

    val padded = if (mandatorySignNibble) {
      if (digitsOnly.length == totalDigits - 1)
        digitsOnly + "0"
      else
        "0"*(totalDigits - digitsOnly.length - 1) + digitsOnly + "0"
    } else {
      if (digitsOnly.length == totalDigits)
        digitsOnly
      else
        "0"*(totalDigits - digitsOnly.length) + digitsOnly
    }

    var bi = 0

    while (bi < byteCount) {
      val high = padded.charAt(bi * 2).asDigit
      val low = padded.charAt(bi * 2 + 1).asDigit

      bytes(bi) = ((high << 4) | low).toByte
      bi += 1
    }

    if (mandatorySignNibble) {
      bytes(byteCount - 1) = ((bytes(byteCount - 1) & 0xF0) | signNibble).toByte
    }

    bytes
  }

}
