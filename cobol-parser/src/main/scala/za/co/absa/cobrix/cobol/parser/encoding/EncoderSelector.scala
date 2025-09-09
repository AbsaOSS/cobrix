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

import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, COMP3, COMP3U, COMP4, COMP9, CobolType, Decimal, Integral, Usage}
import za.co.absa.cobrix.cobol.parser.decoders.BinaryUtils
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}
import za.co.absa.cobrix.cobol.parser.position.Position

import java.nio.charset.{Charset, StandardCharsets}
import java.util

object EncoderSelector {
  type Encoder = Any => Array[Byte]

  def getEncoder(dataType: CobolType,
                 ebcdicCodePage: CodePage = new CodePageCommon,
                 asciiCharset: Charset = StandardCharsets.US_ASCII): Option[Encoder] = {
    dataType match {
      case alphaNumeric: AlphaNumeric if alphaNumeric.compact.isEmpty                       =>
        getStringEncoder(alphaNumeric.enc.getOrElse(EBCDIC), ebcdicCodePage, asciiCharset, alphaNumeric.length)
      case integralComp3: Integral if integralComp3.compact.exists(_.isInstanceOf[COMP3])   =>
        Option(getBdcEncoder(integralComp3.precision, 0, 0, integralComp3.signPosition.isDefined, mandatorySignNibble = true))
      case integralComp3: Integral if integralComp3.compact.exists(_.isInstanceOf[COMP3U])  =>
        Option(getBdcEncoder(integralComp3.precision, 0, 0, integralComp3.signPosition.isDefined, mandatorySignNibble = false))
      case decimalComp3: Decimal if decimalComp3.compact.exists(_.isInstanceOf[COMP3])      =>
        Option(getBdcEncoder(decimalComp3.precision, decimalComp3.scale, decimalComp3.scaleFactor, decimalComp3.signPosition.isDefined, mandatorySignNibble = true))
      case decimalComp3: Decimal if decimalComp3.compact.exists(_.isInstanceOf[COMP3U])     =>
        Option(getBdcEncoder(decimalComp3.precision, decimalComp3.scale, decimalComp3.scaleFactor, decimalComp3.signPosition.isDefined, mandatorySignNibble = false))
      case integralBinary: Integral if integralBinary.compact.exists(_.isInstanceOf[COMP4]) =>
        Option(getBinaryEncoder(integralBinary.compact, integralBinary.precision, 0, 0, integralBinary.signPosition.isDefined, isBigEndian = true))
      case integralBinary: Integral if integralBinary.compact.exists(_.isInstanceOf[COMP9]) =>
        Option(getBinaryEncoder(integralBinary.compact, integralBinary.precision, 0, 0, integralBinary.signPosition.isDefined, isBigEndian = false))
      case decimalBinary: Decimal if decimalBinary.compact.exists(_.isInstanceOf[COMP4]) =>
        Option(getBinaryEncoder(decimalBinary.compact, decimalBinary.precision, decimalBinary.scale, decimalBinary.scaleFactor, decimalBinary.signPosition.isDefined, isBigEndian = true))
      case decimalBinary: Decimal if decimalBinary.compact.exists(_.isInstanceOf[COMP9]) =>
        Option(getBinaryEncoder(decimalBinary.compact, decimalBinary.precision, decimalBinary.scale, decimalBinary.scaleFactor, decimalBinary.signPosition.isDefined, isBigEndian = false))
      case integralDisplay: Integral if integralDisplay.compact.isEmpty =>
        Option(getDisplayEncoder(integralDisplay.precision, 0, 0, integralDisplay.signPosition, isExplicitDecimalPt = false, isSignSeparate = integralDisplay.isSignSeparate))
      case decimalDisplay: Decimal if decimalDisplay.compact.isEmpty =>
        Option(getDisplayEncoder(decimalDisplay.precision, decimalDisplay.scale, decimalDisplay.scaleFactor, decimalDisplay.signPosition, decimalDisplay.explicitDecimal, decimalDisplay.isSignSeparate))
      case _ =>
        None
    }
  }

  /** Gets an encoder function for a string data type. The encoder is chosen depending on whether the output encoding is EBCDIC or ASCII. */
  private def getStringEncoder(encoding: Encoding,
                               ebcdicCodePage: CodePage,
                               asciiCharset: Charset,
                               fieldLength: Int
                              ): Option[Encoder] = {
    encoding match {
      case EBCDIC =>
        val encoder = (a: Any) => {
          encodeEbcdicString(a.toString, CodePageCommon.asciiToEbcdicMapping, fieldLength)
        }
        Option(encoder)
      case ASCII =>
        None
      case _ =>
        None
    }
  }

  /**
    * An encoder from a ASCII basic string to an EBCDIC byte array
    *
    * @param string          An input string
    * @param conversionTable A conversion table to use to convert from ASCII to EBCDIC
    * @param length          The length of the output (in bytes)
    * @return A string representation of the binary data
    */
  def encodeEbcdicString(string: String, conversionTable: Array[Byte], length: Int): Array[Byte] = {
    require(length >= 0, s"Field length cannot be negative, got $length")

    var i = 0
    val buf = new Array[Byte](length)

    // PIC X fields are space-filled on mainframe. Use EBCDIC space 0x40.
    util.Arrays.fill(buf, 0x40.toByte)

    while (i < string.length && i < length) {
      val asciiByte = string(i).toByte
      buf(i) = conversionTable((asciiByte + 256) % 256)
      i = i + 1
    }
    buf
  }

  def getBinaryEncoder(compression: Option[Usage],
                       precision: Int,
                       scale: Int,
                       scaleFactor: Int,
                       isSigned: Boolean,
                       isBigEndian: Boolean): Encoder = {
    val numBytes = BinaryUtils.getBytesCount(compression, precision, isSigned, isExplicitDecimalPt = false, isSignSeparate = false)
    (a: Any) => {
      val number = a match {
        case null                    => null
        case d: java.math.BigDecimal => d
        case n: java.math.BigInteger => new java.math.BigDecimal(n)
        case n: Byte                 => new java.math.BigDecimal(n)
        case n: Int                  => new java.math.BigDecimal(n)
        case n: Long                 => new java.math.BigDecimal(n)
        case x                       => new java.math.BigDecimal(x.toString)
      }
      BinaryEncoders.encodeBinaryNumber(number, isSigned, numBytes, isBigEndian, precision, scale, scaleFactor)
    }
  }

  def getBdcEncoder(precision: Int,
                    scale: Int,
                    scaleFactor: Int,
                    signed: Boolean,
                    mandatorySignNibble: Boolean): Encoder = {
    if (signed && !mandatorySignNibble)
      throw new IllegalArgumentException("If signed is true, mandatorySignNibble must also be true.")

    (a: Any) => {
      val number = a match {
        case null => null
        case d: java.math.BigDecimal => d
        case n: java.math.BigInteger => new java.math.BigDecimal(n)
        case n: Byte => new java.math.BigDecimal(n)
        case n: Int => new java.math.BigDecimal(n)
        case n: Long => new java.math.BigDecimal(n)
        case x => new java.math.BigDecimal(x.toString)
      }
      BCDNumberEncoders.encodeBCDNumber(number, precision, scale, scaleFactor, signed, mandatorySignNibble)
    }
  }

  def getDisplayEncoder(precision: Int,
                        scale: Int,
                        scaleFactor: Int,
                        signPosition: Option[Position],
                        isExplicitDecimalPt: Boolean,
                        isSignSeparate: Boolean): Encoder = {
    val isSigned = signPosition.isDefined
    val numBytes = BinaryUtils.getBytesCount(None, precision, isSigned, isExplicitDecimalPt = isExplicitDecimalPt, isSignSeparate = isSignSeparate)
    (a: Any) => {
      val number = a match {
        case null                    => null
        case d: java.math.BigDecimal => d
        case n: java.math.BigInteger => new java.math.BigDecimal(n)
        case n: Byte                 => new java.math.BigDecimal(n)
        case n: Int                  => new java.math.BigDecimal(n)
        case n: Long                 => new java.math.BigDecimal(n)
        case x                       => new java.math.BigDecimal(x.toString)
      }
      if (isSignSeparate) {
        DisplayEncoders.encodeDisplayNumberSignSeparate(number, signPosition, numBytes, precision, scale, scaleFactor, explicitDecimalPoint = isExplicitDecimalPt)
      } else {
        DisplayEncoders.encodeDisplayNumberSignOverpunched(number, signPosition, numBytes, precision, scale, scaleFactor, explicitDecimalPoint = isExplicitDecimalPt)
      }
    }
  }

}
