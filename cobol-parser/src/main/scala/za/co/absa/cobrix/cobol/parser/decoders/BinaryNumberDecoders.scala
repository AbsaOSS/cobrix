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

object BinaryNumberDecoders {

  def decodeSignedByte(bytes: Array[Byte]): Integer = {
    if (bytes.length < 1) {
      return null
    }
    bytes(0).toInt
  }

  def decodeUnsignedByte(bytes: Array[Byte]): Integer = {
    if (bytes.length < 1) {
      return null
    }
    bytes(0) & 255
  }

  def decodeBinarySignedShortBigEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 2) {
      return null
    }
    (bytes(0) << 8) | (bytes(1) & 255)
  }

  def decodeBinarySignedShortLittleEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 2) {
      return null
    }
    (bytes(1) << 8) | (bytes(0) & 255)
  }

  def decodeBinaryUnsignedShortBigEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 2) {
      return null
    }
    ((bytes(0) & 255) << 8) | (bytes(1) & 255)
  }

  def decodeBinaryUnsignedShortLittleEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 2) {
      return null
    }
    ((bytes(1) & 255) << 8) | (bytes(0) & 255)
  }

  def decodeBinarySignedIntBigEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 4) {
      return null
    }
    (bytes(0) << 24) | ((bytes(1) & 255) << 16) | ((bytes(2) & 255) << 8) | (bytes(3) & 255)
  }

  def decodeBinarySignedIntLittleEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 4) {
      return null
    }
    (bytes(3) << 24) | ((bytes(2) & 255) << 16) | ((bytes(1) & 255) << 8) | (bytes(0) & 255)
  }

  def decodeBinaryUnsignedIntBigEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 4) {
      return null
    }
    val v: Int = (((bytes(0) & 255L) << 24L) | ((bytes(1) & 255L) << 16L) | ((bytes(2) & 255L) << 8L) | (bytes(3) & 255L)).toInt
    if (v<0) null else v
  }

  def decodeBinaryUnsignedIntLittleEndian(bytes: Array[Byte]): Integer = {
    if (bytes.length < 4) {
      return null
    }
    val v: Int = (((bytes(3) & 255L) << 24L) | ((bytes(2) & 255L) << 16L) | ((bytes(1) & 255L) << 8L) | (bytes(0) & 255L)).toInt
    if (v<0) null else v
  }

  def decodeBinarySignedLongBigEndian(bytes: Array[Byte]): java.lang.Long = {
    if (bytes.length < 8) {
      return null
    }
    ((bytes(0) & 255L) << 56) | ((bytes(1) & 255L) << 48) | ((bytes(2) & 255L) << 40) | ((bytes(3) & 255L) << 32) | ((bytes(4) & 255L) << 24) | ((bytes(5) & 255L) << 16) | ((bytes(6) & 255L) << 8) | (bytes(7) & 255L)
  }

  def decodeBinarySignedLongLittleEndian(bytes: Array[Byte]):java.lang.Long = {
    if (bytes.length < 8) {
      return null
    }
    ((bytes(7) & 255L) << 56) | ((bytes(6) & 255L) << 48) | ((bytes(5) & 255L) << 40) | ((bytes(4) & 255L) << 32) | ((bytes(3) & 255L) << 24) | ((bytes(2) & 255L) << 16) | ((bytes(1) & 255L) << 8) | (bytes(0) & 255L)
  }

  def decodeBinaryUnsignedLongBigEndian(bytes: Array[Byte]): java.lang.Long = {
    if (bytes.length < 8) {
      return null
    }
    val v = ((bytes(0) & 255L) << 56) | ((bytes(1) & 255L) << 48) | ((bytes(2) & 255L) << 40) | ((bytes(3) & 255L) << 32) | ((bytes(4) & 255L) << 24) | ((bytes(5) & 255L) << 16) | ((bytes(6) & 255L) << 8) | (bytes(7) & 255L)
    if (v < 0L) null else v
  }

  def decodeBinaryUnsignedLongLittleEndian(bytes: Array[Byte]): java.lang.Long = {
    if (bytes.length < 8) {
      return null
    }
    val v = ((bytes(7) & 255L) << 56) | ((bytes(6) & 255L) << 48) | ((bytes(5) & 255L) << 40) | ((bytes(4) & 255L) << 32) | ((bytes(3) & 255L) << 24) | ((bytes(2) & 255L) << 16) | ((bytes(1) & 255L) << 8) | (bytes(0) & 255L)
    if (v < 0L) null else v
  }

  def decodeBinaryAribtraryPrecision(bytes: Array[Byte], isBigEndian: Boolean, isSigned: Boolean): BigDecimal = {
    if (bytes.length == 0) {
      return null
    }
    // BigInt is used co convert bytes to a big integer. BigDecimal is the type that Spark expects for such values
    val bigInt = (isBigEndian, isSigned) match {
      case (false, false) => BigDecimal(BigInt(1, bytes.reverse).toString())
      case (false, true) => BigDecimal(BigInt(bytes.reverse).toString())
      case (true, false) => BigDecimal(BigInt(1, bytes).toString())
      case (true, true) => BigDecimal(BigInt(bytes).toString())
    }
    bigInt
  }
}
