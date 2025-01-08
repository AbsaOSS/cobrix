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

import java.nio.{ByteBuffer, ByteOrder}
import scala.util.control.NonFatal

object FloatingPointDecoders {
  private val BIT_COUNT_MAGIC = 0x000055AFL

  /**
    * A decoder for IEEE-754 32 bit big endian floats
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed float
    */
  def decodeFloatB(bytes: Array[Byte]): Float = {
    require(bytes.length == 4, "Input must be exactly 4 bytes for a 32-bit float")

    val byteBuffer = ByteBuffer.wrap(bytes)
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.getFloat
  }

  /**
    * A decoder for IEEE-754 32 bit little endian floats
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed float
    */
  def decodeFloatL(bytes: Array[Byte]): Float = {
    require(bytes.length == 4, "Input must be exactly 4 bytes for a 32-bit float")

    val byteBuffer = ByteBuffer.wrap(bytes)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.getFloat
  }

  /**
    * A decoder for IEEE-754 64 bit big endian floats
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed float
    */
  def decodeDoubleB(bytes: Array[Byte]): Double = {
    require(bytes.length == 8, "Input must be exactly 8 bytes for a 64-bit float")

    val byteBuffer = ByteBuffer.wrap(bytes)
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.getDouble
  }

  /**
    * A decoder for IEEE-754 64 bit little endian floats
    *
    * @param bytes A byte array that represents the binary data
    * @return A boxed float
    */
  def decodeDoubleL(bytes: Array[Byte]): Double = {
    require(bytes.length == 8, "Input must be exactly 8 bytes for a 64-bit float")

    val byteBuffer = ByteBuffer.wrap(bytes)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.getDouble
  }

  /** Decode IEEE754 single precision big endian encoded number. */
  def decodeIeee754SingleBigEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      decodeFloatB(bytes)
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IEEE754 double precision big endian encoded number. */
  def decodeIeee754DoubleBigEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      decodeDoubleB(bytes)
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IEEE754 single precision little endian encoded number. */
  def decodeIeee754SingleLittleEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      decodeFloatL(bytes)
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IEEE754 double precision little endian encoded number. */
  def decodeIeee754DoubleLittleEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      decodeDoubleL(bytes)
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode IBM single precision big endian encoded number.
    *
    * The source code of this function is based on ibm2ieee NumPy library
    * Copyright (c) 2018, Enthought, Inc.
    *
    * The source code for this method is distributed via 3-clause BSD license.
    *
    * @param bytes An array of bytes
    * @return a converted single precision floating point number
    */
  def decodeIbmSingleBigEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      val IBM32_SIGN_MASK = 0x80000000
      val IBM32_EXPONENT_MASK = 0x80000000
      val IBM32_FRACTURE_MASK = 0x00FFFFFF
      val IBM32_MS_NIBBLE = 0x00F00000

      val mantissa = (bytes(0) << 24) | ((bytes(1) & 255) << 16) | ((bytes(2) & 255) << 8) | (bytes(3) & 255)
      val sign = mantissa & IBM32_SIGN_MASK
      var fracture = mantissa & IBM32_FRACTURE_MASK
      var exponent = (mantissa & IBM32_EXPONENT_MASK) >> 22

      if (fracture == 0L) {
        0.0f
      } else {
        var topNibble = fracture & IBM32_MS_NIBBLE
        while (topNibble == 0) {
          fracture <<= 4
          exponent -= 4
          topNibble = fracture & IBM32_MS_NIBBLE
        }
        val leadingZeros = ((BIT_COUNT_MAGIC >> (topNibble >> 19)) & 3).toInt
        fracture <<= leadingZeros
        val convertedExp = exponent + 131 - leadingZeros

        if (convertedExp >=0 && convertedExp < 254) {
          val ieee754Int = sign + (convertedExp << 23) + fracture
          java.lang.Float.intBitsToFloat(ieee754Int)
        } else if (convertedExp > 254) {
          java.lang.Float.POSITIVE_INFINITY
        } else if (convertedExp >= -32) {
          val mask = ~(0xFFFFFFFD << (-1 - convertedExp))
          val roundUp = if ((fracture & mask) > 0) 1 else 0
          val convertedFract = ((fracture >> (-1 - convertedExp)) + roundUp) >> 1
          val ieee754Int = sign + convertedFract
          java.lang.Float.intBitsToFloat(ieee754Int)
        } else {
          0.0f
        }
      }
    } catch {
      case NonFatal(_) => null
    }
  }

  /**
    * Decode IBM double precision big endian encoded number.
    *
    * The source code of this function is based on ibm2ieee NumPy library
    * Copyright (c) 2018, Enthought, Inc.
    *
    * The source code for this method is distributed via 3-clause BSD license.
    *
    * @param bytes An array of bytes
    * @return a converted double precision floating point number
    */
  def decodeIbmDoubleBigEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      val IBM64_SIGN_MASK = 0x8000000000000000L
      val IBM64_EXPONENT_MASK = 0x7F00000000000000L
      val IBM64_FRACTURE_MASK = 0x00FFFFFFFFFFFFFFL
      val IBM64_MS_NIBBLE = 0x00F0000000000000L

      val mantissa = ((bytes(0) & 255L) << 56) | ((bytes(1) & 255L) << 48) | ((bytes(2) & 255L) << 40) |
        ((bytes(3) & 255L) << 32) | ((bytes(4) & 255L) << 24) | ((bytes(5) & 255L) << 16) |
        ((bytes(6) & 255L) << 8) | (bytes(7) & 255L)
      val sign = mantissa & IBM64_SIGN_MASK
      var fracture = mantissa & IBM64_FRACTURE_MASK
      var exponent = (mantissa & IBM64_EXPONENT_MASK) >> 54

      if (fracture == 0L) {
        0.0
      } else {
        var topNibble = fracture & IBM64_MS_NIBBLE
        while (topNibble == 0) {
          fracture <<= 4
          exponent -= 4
          topNibble = fracture & IBM64_MS_NIBBLE
        }
        val leadingZeros = (BIT_COUNT_MAGIC >> (topNibble >> 51)) & 3
        fracture <<= leadingZeros
        val convertedExp = exponent + 765 - leadingZeros

        val roundUp = if ((fracture & 0xb) > 0) 1 else 0
        val convertedFract = ((fracture >> 2) + roundUp) >> 1
        val ieee754Long = sign + (convertedExp << 52) + convertedFract
        java.lang.Double.longBitsToDouble(ieee754Long)
      }
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IBM single precision little endian encoded number. */
  def decodeIbmSingleLittleEndian(bytes: Array[Byte]): java.lang.Float = {
    decodeIbmSingleBigEndian(bytes.reverse)
  }

  /** Decode IBM double precision little endian encoded number. */
  def decodeIbmDoubleLittleEndian(bytes: Array[Byte]): java.lang.Double = {
    decodeIbmDoubleBigEndian(bytes.reverse)
  }

}
