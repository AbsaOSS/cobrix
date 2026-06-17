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

import java.nio.{ByteBuffer, ByteOrder}

object FloatingPointEncoders {
  /**
    * An encoder for IEEE-754 64 bit little-endian floats
    *
    * @param f A single precision floating point number.
    * @return An array of 8 bytes representing the number.
    */
  def encodeIeee754SingleLittleEndian(f: Float): Array[Byte] = {
    val byteBuffer = ByteBuffer.allocate(4)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.putFloat(f)
    byteBuffer.array()
  }

  /**
    * An encoder for IEEE-754 64 bit little-endian floats
    *
    * @param d A double precision floating point number.
    * @return An array of 8 bytes representing the number.
    */
  def encodeIeee754DoubleLittleEndian(d: Double): Array[Byte] = {
    val byteBuffer = ByteBuffer.allocate(8)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.putDouble(d)
    byteBuffer.array()
  }

  /**
    * An encoder for IEEE-754 64 bit big-endian floats
    *
    * @param f A single precision floating point number.
    * @return An array of 8 bytes representing the number.
    */
  def encodeIeee754SingleBigEndian(f: Float): Array[Byte] = {
    val byteBuffer = ByteBuffer.allocate(4)
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.putFloat(f)
    byteBuffer.array()
  }

  /**
    * An encoder for IEEE-754 64 bit big-endian floats
    *
    * @param d A double precision floating point number.
    * @return An array of 8 bytes representing the number.
    */
  def encodeIeee754DoubleBigEndian(d: Double): Array[Byte] = {
    val byteBuffer = ByteBuffer.allocate(8)
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.putDouble(d)
    byteBuffer.array()
  }

  /**
    * Encode a float into IBM single precision big endian format (4 bytes).
    *
    * IBM single precision hex float format:
    *   - 1 bit sign
    *   - 7 bits exponent (base-16, biased by 64)
    *   - 24 bits fraction (normalized so that the high-order nibble is non-zero)
    *
    * @param f A single precision floating point number.
    * @return An array of 4 bytes in IBM single precision big-endian format.
    */
  def encodeIbmSingleBigEndian(f: Float): Array[Byte] = {
    if (f == 0.0f) {
      return Array[Byte](0, 0, 0, 0)
    }

    if (f.isNaN) {
      return Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
    }

    if (f.isInfinite) {
      if (f > 0)
        return Array[Byte](0x7F.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
      else
        return Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
    }

    val sign: Int = if (f < 0) 0x80 else 0x00
    var absVal = Math.abs(f).toDouble

    // Determine the base-16 exponent
    var exp16 = 0
    if (absVal >= 1.0) {
      while (absVal >= 1.0) {
        absVal /= 16.0
        exp16 += 1
      }
    } else {
      while (absVal < (1.0 / 16.0)) {
        absVal *= 16.0
        exp16 -= 1
      }
    }

    val biasedExp = exp16 + 64

    // fraction is in [1/16, 1), scale to 24-bit integer
    val fracInt = (absVal * (1 << 24)).toLong & 0xFFFFFFL

    val b0 = (sign | (biasedExp & 0x7F)).toByte
    val b1 = ((fracInt >> 16) & 0xFF).toByte
    val b2 = ((fracInt >> 8) & 0xFF).toByte
    val b3 = (fracInt & 0xFF).toByte

    Array(b0, b1, b2, b3)
  }

  /**
    * Encode a double into IBM double precision big endian format (8 bytes).
    *
    * IBM double precision hex float format:
    *   - 1 bit sign
    *   - 7 bits exponent (base-16, biased by 64)
    *   - 56 bits fraction (normalized so that the high-order nibble is non-zero)
    *
    * @param d A double precision floating point number.
    * @return An array of 8 bytes in IBM double precision big-endian format.
    */
  def encodeIbmDoubleBigEndian(d: Double): Array[Byte] = {
    if (d == 0.0) {
      return Array[Byte](0, 0, 0, 0, 0, 0, 0, 0)
    }

    if (d.isNaN) {
      return Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
    }

    if (d.isInfinite) {
      if (d > 0)
        return Array[Byte](0x7F.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
      else
        return Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
    }

    val sign: Int = if (d < 0) 0x80 else 0x00
    var absVal = Math.abs(d)

    // Determine the base-16 exponent
    var exp16 = 0
    if (absVal >= 1.0) {
      while (absVal >= 1.0) {
        absVal /= 16.0
        exp16 += 1
      }
    } else {
      while (absVal < (1.0 / 16.0)) {
        absVal *= 16.0
        exp16 -= 1
      }
    }

    val biasedExp = exp16 + 64

    // fraction is in [1/16, 1), scale to 56-bit integer
    val fracLong = (absVal * (1L << 56).toDouble).toLong & 0x00FFFFFFFFFFFFFFL

    val b0 = (sign | (biasedExp & 0x7F)).toByte
    val b1 = ((fracLong >> 48) & 0xFF).toByte
    val b2 = ((fracLong >> 40) & 0xFF).toByte
    val b3 = ((fracLong >> 32) & 0xFF).toByte
    val b4 = ((fracLong >> 24) & 0xFF).toByte
    val b5 = ((fracLong >> 16) & 0xFF).toByte
    val b6 = ((fracLong >> 8) & 0xFF).toByte
    val b7 = (fracLong & 0xFF).toByte

    Array(b0, b1, b2, b3, b4, b5, b6, b7)
  }

  /** Encode a float into IBM single precision little endian format (4 bytes). */
  def encodeIbmSingleLittleEndian(f: Float): Array[Byte] = {
    encodeIbmSingleBigEndian(f).reverse
  }

  /** Encode a double into IBM double precision little endian format (8 bytes). */
  def encodeIbmDoubleLittleEndian(d: Double): Array[Byte] = {
    encodeIbmDoubleBigEndian(d).reverse
  }
}
