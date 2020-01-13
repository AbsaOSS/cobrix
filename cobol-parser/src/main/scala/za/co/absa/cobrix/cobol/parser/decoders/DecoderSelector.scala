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

import java.nio.charset.{Charset, StandardCharsets}

import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, COMP1, COMP2, COMP3, COMP4, COMP5, COMP9, CobolType, Decimal, Integral, Usage}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.position.Position

import scala.util.control.NonFatal

object DecoderSelector {
  type Decoder = Array[Byte] => Any

  import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy._


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
    * @param dataType             A daatype of a copybook field
    * @param stringTrimmingPolicy Specifies how the decoder should handle string types
    * @param ebcdicCodePage       Specifies a code page to use for EBCDIC to ASCII/Unicode conversion
    * @param asciiCharset         A charset for ASCII encoded data
    * @param floatingPointFormat  Specifies a floating point format (IBM or IEEE754)
    * @return A function that converts an array of bytes to the target data type.
    */
  def getDecoder(dataType: CobolType,
                 stringTrimmingPolicy: StringTrimmingPolicy = TrimBoth,
                 ebcdicCodePage: CodePage = new CodePageCommon,
                 asciiCharset: Charset = StandardCharsets.US_ASCII,
                 floatingPointFormat: FloatingPointFormat = FloatingPointFormat.IBM): Decoder = {
    val decoder = dataType match {
      case alphaNumeric: AlphaNumeric => getStringDecoder(alphaNumeric.enc.getOrElse(EBCDIC()), stringTrimmingPolicy, ebcdicCodePage, asciiCharset)
      case decimalType: Decimal => getDecimalDecoder(decimalType, floatingPointFormat)
      case integralType: Integral => getIntegralDecoder(integralType)
      case _ => throw new IllegalStateException("Unknown AST object")
    }
    decoder
  }

  /** Gets a decoder function for a string data type. Decoder is chosed depending on whether input encoding is EBCDIC or ASCII */
  private def getStringDecoder(encoding: Encoding, stringTrimmingPolicy: StringTrimmingPolicy, ebcdicCodePage: CodePage, asciiCharset: Charset): Decoder = {
    encoding match {
      case _: EBCDIC =>
        StringDecoders.decodeEbcdicString(_, getStringStrimmingType(stringTrimmingPolicy), ebcdicCodePage.getEbcdicToAsciiMapping)
      case _: ASCII =>
        if (asciiCharset.name() == "US-ASCII") {
          StringDecoders.decodeAsciiString(_, getStringStrimmingType(stringTrimmingPolicy))
        } else {
          // A workaround for non serializable class: Charset
          new AsciiStringDecoderWrapper(getStringStrimmingType(stringTrimmingPolicy), asciiCharset.name())
        }
    }
  }

  private def getStringStrimmingType(stringTrimmingPolicy: StringTrimmingPolicy): Int = {
    stringTrimmingPolicy match {
      case TrimNone => StringDecoders.TrimNone
      case TrimLeft => StringDecoders.TrimLeft
      case TrimRight => StringDecoders.TrimRight
      case TrimBoth => StringDecoders.TrimBoth
    }
  }

  /** Gets a decoder function for a decimal data type. The input array of bytes is always converted to string and then to BigDecimal */
  private def getDecimalDecoder(decimalType: Decimal, floatingPointFormat: FloatingPointFormat): Decoder = {
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
            StringDecoders.decodeEbcdicBigDecimal(_, !isSigned)
          else
            StringDecoders.decodeAsciiBigDecimal(_, !isSigned)
        } else {
          if (isEbcidic)
            StringDecoders.decodeEbcdicBigNumber(_, !isSigned, decimalType.scale, decimalType.scaleFactor)
          else
            StringDecoders.decodeAsciiBigNumber(_, !isSigned, decimalType.scale, decimalType.scaleFactor)
        }
//      case Some(COMP()) =>
//        // COMP aka BINARY encoded number
//        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = true, signed = isSigned, decimalType.scale, decimalType.scaleFactor))
      case Some(COMP1()) =>
        // COMP-1 aka 32-bit floating point number
        getSinglePrecisionFpDecoder(floatingPointFormat)
      case Some(COMP2()) =>
        // COMP-2 aka 64-bit floating point number
        getDoublePrecisionFpDecoder(floatingPointFormat)
      case Some(COMP3()) =>
        // COMP-3 aka BCD-encoded number
        BCDNumberDecoders.decodeBigBCDDecimal(_, decimalType.scale, decimalType.scaleFactor)
      case Some(COMP4()) =>
        // COMP aka BINARY encoded number
        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = true, signed = isSigned, decimalType.scale, decimalType.scaleFactor))
      case Some(COMP5()) =>
        // COMP aka BINARY encoded number
        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = true, signed = isSigned, decimalType.scale, decimalType.scaleFactor))
      case Some(COMP9()) =>
        // COMP aka BINARY encoded number
        (bytes: Array[Byte]) => toBigDecimal(BinaryUtils.decodeBinaryNumber(bytes, bigEndian = false, signed = isSigned, decimalType.scale, decimalType.scaleFactor))
      case _ =>
        throw new IllegalStateException(s"Unknown number compression format (COMP-${decimalType.compact}).")
    }

  }

  private def getSinglePrecisionFpDecoder(floatingPointFormat: FloatingPointFormat): Decoder = {
    import FloatingPointFormat._
    floatingPointFormat match {
      case IBM =>        FloatingPointDecoders.decodeIbmSingleBigEndian
      case IBM_LE =>     FloatingPointDecoders.decodeIbmSingleLittleEndian
      case IEEE754 =>    FloatingPointDecoders.decodeIeee754SingleBigEndian
      case IEEE754_LE => FloatingPointDecoders.decodeIeee754SingleLittleEndian
      case _ => throw new IllegalStateException(s"Unknown floating point format ($floatingPointFormat).")
    }
  }

  private def getDoublePrecisionFpDecoder(floatingPointFormat: FloatingPointFormat): Decoder = {
    import FloatingPointFormat._
    floatingPointFormat match {
      case IBM =>        FloatingPointDecoders.decodeIbmDoubleBigEndian
      case IBM_LE =>     FloatingPointDecoders.decodeIbmDoubleLittleEndian
      case IEEE754 =>    FloatingPointDecoders.decodeIeee754DoubleBigEndian
      case IEEE754_LE => FloatingPointDecoders.decodeIeee754DoubleLittleEndian
      case _ => throw new IllegalStateException(s"Unknown floating point format ($floatingPointFormat).")
    }
  }

  /** Gets a decoder function for an integral data type. A direct conversion from array of bytes to the target type is used where possible. */
  private def getIntegralDecoder(integralType: Integral): Decoder = {
    val encoding = integralType.enc.getOrElse(EBCDIC())

    val isEbcidic = encoding match {
      case _: EBCDIC => true
      case _: ASCII => false
    }

    val isSigned = integralType.signPosition.isDefined

    integralType.compact match {
      case None =>
        if (integralType.precision <= Constants.maxIntegerPrecision) {
          if (isEbcidic)
            StringDecoders.decodeEbcdicInt(_, !isSigned)
          else
            StringDecoders.decodeAsciiInt(_, !isSigned)
        } else if (integralType.precision <= Constants.maxLongPrecision) {
          if (isEbcidic)
            StringDecoders.decodeEbcdicLong(_, !isSigned)
          else
            StringDecoders.decodeAsciiLong(_, !isSigned)
        } else {
          if (isEbcidic)
            StringDecoders.decodeEbcdicBigNumber(_, !isSigned)
          else
            StringDecoders.decodeAsciiBigNumber(_, !isSigned)
        }
//      case Some(Constants.compBinary1) =>
//        // COMP aka BINARY encoded number
//        getBinaryEncodedIntegralDecoder(Some(0), integralType.precision, integralType.signPosition, isBigEndian = true)
      case Some(COMP1()) =>
        throw new IllegalStateException("Unexpected error. COMP-1 (float) is incorrect for an integral number.")
      case Some(COMP2()) =>
        throw new IllegalStateException("Unexpected error. COMP-2 (double) is incorrect for an integral number.")
      case Some(COMP3()) =>
        // COMP-3 aka BCD-encoded number
        getBCDIntegralDecoder(integralType.precision)
      case Some(COMP4()) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(COMP4()), integralType.precision, integralType.signPosition, isBigEndian = true)
      case Some(COMP5()) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(COMP5()), integralType.precision, integralType.signPosition, isBigEndian = true)
      case Some(COMP9()) =>
        // COMP aka BINARY encoded number
        getBinaryEncodedIntegralDecoder(Some(COMP9()), integralType.precision, integralType.signPosition, isBigEndian = false)
      case _ =>
        throw new IllegalStateException(s"Unknown number compression format (COMP-${integralType.compact}).")
    }
  }

  /** Gets a decoder function for a binary encoded integral data type. A direct conversion from array of bytes to the target type is used where possible. */
  private def getBinaryEncodedIntegralDecoder(compact: Option[Usage], precision: Int, signPosition: Option[Position] = None, isBigEndian: Boolean): Decoder = {
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
          val bcdDecoded = BCDNumberDecoders.decodeBigBCDNumber(a, 0, 0)
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
