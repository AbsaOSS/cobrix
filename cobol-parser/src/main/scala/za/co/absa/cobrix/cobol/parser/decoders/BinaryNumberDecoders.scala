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

object BinaryNumberDecoders {

  def decodeSignedByte(bytes: Array[Byte]): Any = {
    if (bytes.length < 1) {
      return null
    }
    bytes(0).toInt
  }

  def decodeUnsignedByte(bytes: Array[Byte]): Any = {
    if (bytes.length < 1) {
      return null
    }
    bytes(0) & 255
  }

  def decodeBinarySignedShortBigEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 2) {
      return null
    }
    (bytes(0) << 8) | (bytes(1) & 255)
  }

  def decodeBinarySignedShortLittleEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 2) {
      return null
    }
    (bytes(1) << 8) | (bytes(0) & 255)
  }

  def decodeBinaryUnsignedShortBigEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 2) {
      return null
    }
    ((bytes(0) & 255) << 8) | (bytes(1) & 255)
  }

  def decodeBinaryUnsignedShortLittleEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 2) {
      return null
    }
    ((bytes(1) & 255) << 8) | (bytes(0) & 255)
  }

  def decodeBinarySignedIntBigEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 4) {
      return null
    }
    (bytes(0) << 24) | ((bytes(1) & 255) << 16) | ((bytes(2) & 255) << 8) | (bytes(3) & 255)
  }

  def decodeBinarySignedIntLittleEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 4) {
      return null
    }
    (bytes(3) << 24) | ((bytes(2) & 255) << 16) | ((bytes(1) & 255) << 8) | (bytes(0) & 255)
  }

  def decodeBinaryUnsignedIntBigEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 4) {
      return null
    }
    val v: Int = (((bytes(0) & 255L) << 24L) | ((bytes(1) & 255L) << 16L) | ((bytes(2) & 255L) << 8L) | (bytes(3) & 255L)).toInt
    if (v<0) null else v
  }

  def decodeBinaryUnsignedIntLittleEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 4) {
      return null
    }
    val v: Int = (((bytes(3) & 255L) << 24L) | ((bytes(2) & 255L) << 16L) | ((bytes(1) & 255L) << 8L) | (bytes(0) & 255L)).toInt
    if (v<0) null else v
  }

  def decodeBinarySignedLongBigEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 8) {
      return null
    }
    ((bytes(0) & 255L) << 56) | ((bytes(1) & 255L) << 48) | ((bytes(2) & 255L) << 40) | ((bytes(3) & 255L) << 32) | ((bytes(4) & 255L) << 24) | ((bytes(5) & 255L) << 16) | ((bytes(6) & 255L) << 8) | (bytes(7) & 255L)
  }

  def decodeBinarySignedLongLittleEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 8) {
      return null
    }
    ((bytes(7) & 255L) << 56) | ((bytes(6) & 255L) << 48) | ((bytes(5) & 255L) << 40) | ((bytes(4) & 255L) << 32) | ((bytes(3) & 255L) << 24) | ((bytes(2) & 255L) << 16) | ((bytes(1) & 255L) << 8) | (bytes(0) & 255L)
  }

  def decodeBinaryUnsignedLongBigEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 8) {
      return null
    }
    val v = ((bytes(0) & 255L) << 56) | ((bytes(1) & 255L) << 48) | ((bytes(2) & 255L) << 40) | ((bytes(3) & 255L) << 32) | ((bytes(4) & 255L) << 24) | ((bytes(5) & 255L) << 16) | ((bytes(6) & 255L) << 8) | (bytes(7) & 255L)
    if (v < 0L) null else v
  }

  def decodeBinaryUnsignedLongLittleEndian(bytes: Array[Byte]): Any = {
    if (bytes.length < 8) {
      return null
    }
    val v = ((bytes(7) & 255L) << 56) | ((bytes(6) & 255L) << 48) | ((bytes(5) & 255L) << 40) | ((bytes(4) & 255L) << 32) | ((bytes(3) & 255L) << 24) | ((bytes(2) & 255L) << 16) | ((bytes(1) & 255L) << 8) | (bytes(0) & 255L)
    if (v < 0L) null else v
  }

  def decodeBinaryAribtraryPrecision(bytes: Array[Byte], isBigEndian: Boolean, isSigned: Boolean): BigInt = {
    if (bytes.length == 0) {
      return null
    }
    val bigInt = (isBigEndian, isSigned) match {
      case (false, false) => BigInt(1, bytes.reverse)
      case (false, true) => BigInt(bytes.reverse)
      case (true, false) => BigInt(1, bytes)
      case (true, true) => BigInt(bytes)
    }
    bigInt
  }

  def decodeSignSeparatedByte(bytes: Array[Byte], isLeading: Boolean): Any = {
    if (bytes.length < 2) {
      return null
    }
    val (sign, binBytes) = getSignAndBytes(bytes, isLeading, 1)
    sign * (binBytes(0) & 255)
  }

  def decodeSignSeparateShortBigEndian(bytes: Array[Byte], isLeading: Boolean): Any = {
    if (bytes.length < 3) {
      return null
    }
    val (sign, binBytes) = getSignAndBytes(bytes, isLeading, 2)
    sign * ((binBytes(0) << 8) | (binBytes(1) & 255))
  }

  def decodeSignSeparateShortLittleEndian(bytes: Array[Byte], isLeading: Boolean): Any = {
    if (bytes.length < 3) {
      return null
    }
    val (sign, binBytes) = getSignAndBytes(bytes, isLeading, 2)
    sign * ((binBytes(1) << 8) | (binBytes(0) & 255))
  }

  def decodeSignSeparateIntBigEndian(bytes: Array[Byte], isLeading: Boolean): Any = {
    if (bytes.length < 5) {
      return null
    }
    val (sign, binBytes) = getSignAndBytes(bytes, isLeading, 4)
    sign * ((binBytes(0) & 255L) << 24L) | ((binBytes(1) & 255L) << 16L) | ((binBytes(2) & 255L) << 8L) | (binBytes(3) & 255L)
  }

  def decodeSignSeparateIntLittleEndian(bytes: Array[Byte], isLeading: Boolean): Any = {
    if (bytes.length < 5) {
      return null
    }
    val (sign, binBytes) = getSignAndBytes(bytes, isLeading, 4)
    sign * ((binBytes(3) & 255L) << 24L) | ((binBytes(2) & 255L) << 16L) | ((binBytes(1) & 255L) << 8L) | (binBytes(0) & 255L)
  }

  def decodeSignSeparatedAribtraryPrecision(bytes: Array[Byte], isBigEndian: Boolean, isLeading: Boolean): BigInt = {
    if (bytes.length < 2) {
      return null
    }
    val (sign, binBytes) = getSignAndBytes(bytes, isLeading, bytes.length - 1)
    val bigInt = if (isBigEndian) {
      BigInt(1, binBytes)
    } else {
      BigInt(1, binBytes.reverse)
    }
    sign * bigInt
  }

  private def getSignAndBytes(bytes: Array[Byte], isLeading: Boolean, numBytes: Int): (Byte, Array[Byte]) = {
    val binBytes = if (isLeading)
      java.util.Arrays.copyOfRange(bytes, 1, numBytes + 1)
    else
      java.util.Arrays.copyOfRange(bytes, 0, numBytes)
    val signByte = if (isLeading) bytes(0) else bytes(numBytes)
    val sign: Byte = if (signByte == Constants.minusCharEBCIDIC || signByte == Constants.minusCharASCII) -1 else 1
    (sign, binBytes)
  }

}
