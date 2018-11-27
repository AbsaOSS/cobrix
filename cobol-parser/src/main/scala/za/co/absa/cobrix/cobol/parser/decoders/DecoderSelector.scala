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

import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.common.BinaryUtils._
import za.co.absa.cobrix.cobol.parser.common.{BinaryUtils, Constants}
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.position.Position

import scala.util.control.NonFatal

object DecoderSelector {
  type Decoder = Array[Byte] => Any

  def getDecoder(dataType: CobolType): Decoder = {
    val decoder = dataType match {
      case alphaNumeric: AlphaNumeric => getStringDecoder(alphaNumeric.enc.getOrElse(EBCDIC()))
      case decimalType: Decimal => getDecimalDecoder(decimalType)
      case integralType: Integral => getIntegralDecoder(integralType)
      case _ => throw new IllegalStateException("Unknown AST object")
    }
    decoder
  }

  def getStringDecoder(encoding: Encoding): Decoder = {
    encoding match {
      case _: EBCDIC => StringDecoders.decodeEbcdicString
      case _: ASCII => StringDecoders.decodeAsciiString
    }
  }

  def getDecimalDecoder(decimalType: Decimal): Decoder = {
    val encoding = decimalType.enc.getOrElse(EBCDIC())
    // ToDo optimize this
    bytes: Array[Byte] => decodeCobolNumber(encoding, bytes, decimalType.compact, decimalType.precision, decimalType.scale, decimalType.explicitDecimal, decimalType.signPosition.nonEmpty, decimalType.isSignSeparate)
  }

  def getIntegralDecoder(integralType: Integral): Decoder = {
    val encoding = integralType.enc.getOrElse(EBCDIC())
    integralType.compact match {
      case None =>
        // ToDo add support for trailing sign
        (bytes: Array[Byte]) => BinaryUtils.decodeUncompressedNumber(encoding, bytes, explicitDecimal = false, 0, isSignSeparate = integralType.isSignSeparate)
      case Some(0) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(0), integralType.precision, integralType.signPosition, integralType.isSignSeparate, isBigEndian = true)
      case Some(1) =>
        // COMP-1 aka 32-bit floating point number
        (bytes: Array[Byte]) => BinaryUtils.decodeFloatingPointNumber(bytes, bigEndian = true)
      case Some(2) =>
        // COMP-2 aka 64-bit floating point number
        (bytes: Array[Byte]) => BinaryUtils.decodeFloatingPointNumber(bytes, bigEndian = true)
      case Some(3) =>
        // COMP-3 aka BCD-encoded number
        getBCDIntegralDecoder(integralType.precision, integralType.isSignSeparate)
      case Some(4) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(4), integralType.precision, integralType.signPosition, integralType.isSignSeparate, isBigEndian = true)
      case Some(5) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(5), integralType.precision, integralType.signPosition, integralType.isSignSeparate, isBigEndian = false)
      case _ =>
        throw new IllegalStateException(s"Unknown number compression format (COMP-${integralType.compact}).")
    }
    null
  }

  def getBinaryEncodedIntegralDecoder(compact: Option[Int], precision: Int, signPosition: Option[Position] = None, isSignSeparate: Boolean = false, isBigEndian: Boolean): Decoder = {
    val isSigned = signPosition.nonEmpty
    val isSignLeft = signPosition.forall(sp => if (sp == za.co.absa.cobrix.cobol.parser.position.Left) true else false)

    val numOfBytes = BinaryUtils.getBytesCount(compact, precision, isSigned, isSignSeparate)
    if (isSignSeparate) {
      // ToDo add support for trailing sign
      val decoder = (isBigEndian, numOfBytes) match {
        case (true, 1) => (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparatedByte(a, isLeading = true)
        case (true, 2) => (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparateShortBigEndian(a, isLeading = true)
        case (true, 4) => (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparateIntBigEndian(a, isLeading = true)
        case (false, 1) => (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparatedByte(a, isLeading = true)
        case (false, 2) => (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparateShortLittleEndian(a, isLeading = true)
        case (false, 4) => (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparateIntLittleEndian(a, isLeading = true)
        case _ =>
          (a: Array[Byte]) => BinaryNumberDecoders.decodeSignSeparatedAribtraryPrecision(a, isBigEndian, isLeading = true)
      }
      decoder

    } else {
      val decoder = (isSigned, isBigEndian, numOfBytes) match {
        case (true, true, 1) => BinaryNumberDecoders.decodeSignedByte _
        case (true, true, 2) => BinaryNumberDecoders.decodeBinarySignedShortBigEndian _
        case (true, true, 4) => BinaryNumberDecoders.decodeBinarySignedIntBigEndian _
        case (true, true, 8) => BinaryNumberDecoders.decodeBinarySignedLongBigEndian _
        case (true, false, 1) => BinaryNumberDecoders.decodeSignedByte _
        case (true, false, 2) => BinaryNumberDecoders.decodeBinarySignedShortLittleEndian _
        case (true, false, 4) => BinaryNumberDecoders.decodeBinarySignedIntLittleEndian _
        case (true, false, 8) => BinaryNumberDecoders.decodeBinarySignedLongLittleEndian _
        case (false, true, 1) => BinaryNumberDecoders.decodeUnsignedByte _
        case (false, true, 2) => BinaryNumberDecoders.decodeBinaryUnsignedShortBigEndian _
        case (false, true, 4) => BinaryNumberDecoders.decodeBinaryUnsignedIntBigEndian _
        case (false, false, 1) => BinaryNumberDecoders.decodeUnsignedByte _
        case (false, false, 2) => BinaryNumberDecoders.decodeBinaryUnsignedShortLittleEndian _
        case (false, false, 4) => BinaryNumberDecoders.decodeBinaryUnsignedIntLittleEndian _
        case _ =>
          (a: Array[Byte]) => BinaryNumberDecoders.decodeBinaryAribtraryPrecision(a, isBigEndian, isSigned)
      }
      decoder
    }
  }

  def getBCDIntegralDecoder(precision: Int, isSignSeparate: Boolean): Decoder = {
    // ToDo add support for trailing sign
    val decoder =
      if (precision < 3) {
        a: Array[Byte] => {
          val num = BCDNumberDecoders.decodeBCDIntegralNumber(a, isSignSeparate, isSignLeading = true)
          if (num != null) {
            num.asInstanceOf[Long].toByte
          } else {
            null
          }
        }
      } else if (precision < Constants.maxIntegerPrecision) {
        a: Array[Byte] => {
          val num = BCDNumberDecoders.decodeBCDIntegralNumber(a, isSignSeparate, isSignLeading = true)
          if (num != null) {
            num.asInstanceOf[Long].toInt
          } else {
            null
          }
        }
      } else if (precision < Constants.maxLongPrecision) {
        a: Array[Byte] => BCDNumberDecoders.decodeBCDIntegralNumber(a, isSignSeparate, isSignLeading = true)
      } else {
        a: Array[Byte] => BigInt(BCDNumberDecoders.decodeBigBCDNumber(a, 0, isSignSeparate, isSignLeading = true))
      }
    decoder
  }


}
