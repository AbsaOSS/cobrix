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
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.position.Position

import scala.util.control.NonFatal

object DecoderSelector {
  type Decoder = Array[Byte] => Any

  /**
    * Gets a decoder function suitable for converting the specified COBOL data type
    * to a target type. The target type is determined based on Spark expectations.
    *
    * <ul>
    * <li> Alphanumeric type is converted to String </li>
    * <li> Decimal types are represented as BigDecimal </li>
    * <li> Integral types are represented as boxed integers and longs. Larger integral numbers are represented as BigDecimal </li>
    * </ul>
    *
    * @param dataType A daatype of a copybook field
    * @return A function that converts an array of bytes to the target data type.
    */
  def getDecoder(dataType: CobolType): Decoder = {
    val decoder = dataType match {
      case alphaNumeric: AlphaNumeric => getStringDecoder(alphaNumeric.enc.getOrElse(EBCDIC()))
      case decimalType: Decimal => getDecimalDecoder(decimalType)
      case integralType: Integral => getIntegralDecoder(integralType)
      case _ => throw new IllegalStateException("Unknown AST object")
    }
    decoder
  }

  /** Gets a decoder function for a string data type. Decoder is chosed depending on whether input encoding is ENCDIC or ASCII */
  private def getStringDecoder(encoding: Encoding): Decoder = {
    encoding match {
      case _: EBCDIC => StringDecoders.decodeEbcdicString
      case _: ASCII => StringDecoders.decodeAsciiString
    }
  }

  /** Gets a decoder function for a decimal data type. The input array of bytes is always converted to string and then to BigDecimal */
  private def getDecimalDecoder(decimalType: Decimal): Decoder = {
    val encoding = decimalType.enc.getOrElse(EBCDIC())

    val isEbcidic = encoding match {
      case _: EBCDIC => true
      case _: ASCII => false
    }

    val isSigned = decimalType.signPosition.isDefined

    decimalType.compact match {
      case None =>
        if (decimalType.explicitDecimal) {
          if (isEbcidic)
            StringDecoders.decodeEbcdicBigDecimal
          else
            StringDecoders.decodeAsciiBigDecimal
        } else {
          if (isEbcidic)
            StringDecoders.decodeEbcdicBigNumber(_, decimalType.scale)
          else
            StringDecoders.decodeAsciiBigNumber(_, decimalType.scale)
        }
      case Some(0) =>
        // COMP aka BINARY encoded number
        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = true, signed = isSigned, decimalType.scale))
      case Some(1) =>
        // COMP-1 aka 32-bit floating point number
        (bytes: Array[Byte]) => BinaryUtils.decodeFloatingPointNumber(bytes, bigEndian = true).toFloat
      case Some(2) =>
        // COMP-2 aka 64-bit floating point number
        (bytes: Array[Byte]) => BinaryUtils.decodeFloatingPointNumber(bytes, bigEndian = true).toDouble
      case Some(3) =>
        // COMP-3 aka BCD-encoded number
        BCDNumberDecoders.decodeBigBCDDecimal(_, decimalType.scale)
      case Some(4) =>
        // COMP aka BINARY encoded number
        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = true, signed = isSigned, decimalType.scale))
      case Some(5) =>
        // COMP aka BINARY encoded number
        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = false, signed = isSigned, decimalType.scale))
      case _ =>
        throw new IllegalStateException(s"Unknown number compression format (COMP-${decimalType.compact}).")
    }

  }

  /** Gets a decoder function for an integral data type. A direct conversion from array of bytes to the target type is used where possible. */
  private def getIntegralDecoder(integralType: Integral): Decoder = {
    val encoding = integralType.enc.getOrElse(EBCDIC())

    val isEbcidic = encoding match {
      case _: EBCDIC => true
      case _: ASCII => false
    }

    integralType.compact match {
      case None =>
        if (integralType.precision <= Constants.maxIntegerPrecision) {
          if (isEbcidic)
            StringDecoders.decodeEbcdicInt
          else
            StringDecoders.decodeAsciiInt
        } else if (integralType.precision <= Constants.maxLongPrecision) {
          if (isEbcidic)
            StringDecoders.decodeEbcdicLong
          else
            StringDecoders.decodeAsciiLong
        } else {
          if (isEbcidic)
            StringDecoders.decodeEbcdicBigNumber(_)
          else
            StringDecoders.decodeAsciiBigNumber(_)
        }
      case Some(0) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(0), integralType.precision, integralType.signPosition, isBigEndian = true)
      case Some(1) =>
        // COMP-1 aka 32-bit floating point number
        (bytes: Array[Byte]) => BinaryUtils.decodeFloatingPointNumber(bytes, bigEndian = true)
      case Some(2) =>
        // COMP-2 aka 64-bit floating point number
        (bytes: Array[Byte]) => BinaryUtils.decodeFloatingPointNumber(bytes, bigEndian = true)
      case Some(3) =>
        // COMP-3 aka BCD-encoded number
        getBCDIntegralDecoder(integralType.precision)
      case Some(4) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(4), integralType.precision, integralType.signPosition, isBigEndian = true)
      case Some(5) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(5), integralType.precision, integralType.signPosition, isBigEndian = false)
      case _ =>
        throw new IllegalStateException(s"Unknown number compression format (COMP-${integralType.compact}).")
    }
  }

  /** Gets a decoder function for a binary encoded integral data type. A direct conversion from array of bytes to the target type is used where possible. */
  private def getBinaryEncodedIntegralDecoder(compact: Option[Int], precision: Int, signPosition: Option[Position] = None, isBigEndian: Boolean): Decoder = {
    val isSigned = signPosition.nonEmpty
    val isSignLeft = signPosition.forall(sp => if (sp == za.co.absa.cobrix.cobol.parser.position.Left) true else false)

    val numOfBytes = BinaryUtils.getBytesCount(compact, precision, isSigned, isExplicitDecimalPt = false, isSignSeparate = false)
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
      case (false, true, 8) => BinaryNumberDecoders.decodeBinaryUnsignedLongBigEndian _
      case (false, false, 1) => BinaryNumberDecoders.decodeUnsignedByte _
      case (false, false, 2) => BinaryNumberDecoders.decodeBinaryUnsignedShortLittleEndian _
      case (false, false, 4) => BinaryNumberDecoders.decodeBinaryUnsignedIntLittleEndian _
      case (false, false, 8) => BinaryNumberDecoders.decodeBinaryUnsignedLongLittleEndian _
      case _ =>
        (a: Array[Byte]) => BinaryNumberDecoders.decodeBinaryAribtraryPrecision(a, isBigEndian, isSigned)
    }
    decoder // 999 999 999
  }

  /** Gets a decoder function for a BCD-encoded integral data type. A direct conversion from array of bytes to the target type is used where possible. */
  private def getBCDIntegralDecoder(precision: Int): Decoder = {
    val decoder =
      if (precision <= Constants.maxIntegerPrecision) {
        a: Array[Byte] => {
          val num = BCDNumberDecoders.decodeBCDIntegralNumber(a)
          if (num != null) {
            num.asInstanceOf[Long].toInt
          } else {
            null
          }
        }
      } else if (precision <= Constants.maxLongPrecision) {
        a: Array[Byte] => BCDNumberDecoders.decodeBCDIntegralNumber(a)
      } else {
        a: Array[Byte] =>
          val bcdDecoded = BCDNumberDecoders.decodeBigBCDNumber(a, 0)
          if (bcdDecoded != null)
            BigDecimal(bcdDecoded)
          else
            null
      }
    decoder
  }

  /** Malformed data does not cause exceptions in Spark. Null values are returned instead */
  private def toBigDecimal(str: String): BigDecimal = {
    try {
      BigDecimal(str)
    } catch {
      case NonFatal(_) => null
    }
  }


}
