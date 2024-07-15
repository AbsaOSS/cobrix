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

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.cobol.parser.ast.datatype._
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC}
import za.co.absa.cobrix.cobol.parser.position

class BinaryDecoderSpec extends AnyFunSuite {
  import BinaryUtils.{addDecimalPoint, decodeBinaryNumber}

  test("Test string fields decoding") {
    assert(BinaryUtils.decodeString(ASCII, "TestString".toCharArray.map(_.toByte), 4) == "Test")
    assert(BinaryUtils.decodeString(ASCII, "TestString".toCharArray.map(_.toByte), 10) == "TestString")

    // "TestString"
    val ebcdicString = Array[Byte](0xE3.toByte, 0x85.toByte, 0xA2.toByte, 0xA3.toByte, 0xE2.toByte,
      0xA3.toByte, 0x99.toByte, 0x89.toByte, 0x95.toByte, 0x87.toByte)
    assert(BinaryUtils.decodeString(EBCDIC, ebcdicString, 10) == "TestString")
  }


  test("Test uncompressed number decoding") {
    assert(StringDecoders.decodeAsciiNumber("100200".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false).contains("100200"))
    assert(StringDecoders.decodeAsciiBigNumber("1002551".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 3).toString.contains("1002.551"))
    assert(StringDecoders.decodeAsciiBigNumber("1002.551".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0).toString.contains("1002.551"))

    // "1002551"
    val ebcdicNum = Array[Byte](0xF1.toByte, 0xF0.toByte, 0xF0.toByte, 0xF2.toByte, 0xF5.toByte, 0xF5.toByte, 0xF1.toByte)
    assert(StringDecoders.decodeEbcdicBigNumber(ebcdicNum, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 2).toString().contains("10025.51"))
  }

  test("Test positive sign overpunching") {
    assert(StringDecoders.decodeAsciiNumber("{".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+0")
    assert(StringDecoders.decodeAsciiNumber("10020{".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100200")
    assert(StringDecoders.decodeAsciiNumber("10020A".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100201")
    assert(StringDecoders.decodeAsciiNumber("10020B".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100202")
    assert(StringDecoders.decodeAsciiNumber("10020C".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100203")
    assert(StringDecoders.decodeAsciiNumber("10020D".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100204")
    assert(StringDecoders.decodeAsciiNumber("10020E".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100205")
    assert(StringDecoders.decodeAsciiNumber("10020F".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100206")
    assert(StringDecoders.decodeAsciiNumber("10020G".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100207")
    assert(StringDecoders.decodeAsciiNumber("10020H".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100208")
    assert(StringDecoders.decodeAsciiNumber("10020I".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100209")
  }

  test("Test negative sign overpunching") {
    assert(StringDecoders.decodeAsciiNumber("}".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-0")
    assert(StringDecoders.decodeAsciiNumber("10020}".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100200")
    assert(StringDecoders.decodeAsciiNumber("10020J".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100201")
    assert(StringDecoders.decodeAsciiNumber("10020K".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100202")
    assert(StringDecoders.decodeAsciiNumber("10020L".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100203")
    assert(StringDecoders.decodeAsciiNumber("10020M".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100204")
    assert(StringDecoders.decodeAsciiNumber("10020N".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100205")
    assert(StringDecoders.decodeAsciiNumber("10020O".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100206")
    assert(StringDecoders.decodeAsciiNumber("10020P".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100207")
    assert(StringDecoders.decodeAsciiNumber("10020Q".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100208")
    assert(StringDecoders.decodeAsciiNumber("10020R".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100209")
  }

  test("Test incorrect sign overpunching") {
    assert(StringDecoders.decodeAsciiNumber("1002}0".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    assert(StringDecoders.decodeAsciiNumber("100K00".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    assert(StringDecoders.decodeAsciiNumber("1A0200".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)

    assert(StringDecoders.decodeAsciiNumber("10020}".toCharArray.map(_.toByte), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    assert(StringDecoders.decodeAsciiNumber("}".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-0")
    assert(StringDecoders.decodeAsciiNumber("{".toCharArray.map(_.toByte), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+0")
    assert(StringDecoders.decodeAsciiNumber("}".toCharArray.map(_.toByte), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    assert(StringDecoders.decodeAsciiNumber("{".toCharArray.map(_.toByte), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "+0")
    assert(StringDecoders.decodeAsciiNumber("{".toCharArray.map(_.toByte), isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
  }

  test("Test positive COMP-3 format decoding") {
    val comp3BytesPositive = Array[Byte](0x10.toByte,0x11.toByte,0x44.toByte, 0x75.toByte,0x00.toByte,0x00.toByte,0x00.toByte,0x4F.toByte)
    val comp3ValuePositive = "101144750000004"

    val v = BCDNumberDecoders.decodeBigBCDNumber(comp3BytesPositive, scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v.contains(comp3ValuePositive))
  }

  test("Test positive COMP-3U format decoding") {
    val comp3BytesPositive = Array[Byte](0x10.toByte, 0x11.toByte)
    val comp3ValuePositive = "1011"

    val v = BCDNumberDecoders.decodeBigBCDNumber(comp3BytesPositive, scale = 0, scaleFactor = 0, mandatorySignNibble = false)
    assert(v.contains(comp3ValuePositive))
  }

  test("Test positive COMP-3 format decoding2") {
    val comp3BytesPositive = Array[Byte](0x00.toByte,0x00.toByte,0x5F.toByte)
    val comp3ValuePositive = "5"

    val v = BCDNumberDecoders.decodeBigBCDNumber(comp3BytesPositive, scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v.contains(comp3ValuePositive))
  }

  test("Test negative COMP-3 format decoding") {
    val comp3BytesNegative = Array[Byte](0x10.toByte,0x11.toByte,0x44.toByte, 0x75.toByte,0x00.toByte,0x00.toByte,0x00.toByte,0x4D.toByte)
    val comp3ValueNegative = "-101144750000004"

    val v = BCDNumberDecoders.decodeBigBCDNumber(comp3BytesNegative, scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v.contains(comp3ValueNegative))
  }

  test("Test unsigned COMP-3 format decoding") {
    val comp3BytesUnsigned = Array[Byte](0x10.toByte,0x11.toByte,0x44.toByte, 0x75.toByte,0x00.toByte,0x00.toByte,0x00.toByte,0x4C.toByte)
    val comp3ValueUnsigned = "101144750000004"

    val v = BCDNumberDecoders.decodeBCDIntegralNumber(comp3BytesUnsigned, mandatorySignNibble = true).toString
    assert (v.contains(comp3ValueUnsigned))
  }

  test("Test unsigned COMP-3U format decoding") {
    val comp3BytesUnsigned = Array[Byte](0x10.toByte, 0x11.toByte, 0x44.toByte, 0x75.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x45.toByte)
    val comp3ValueUnsigned = "1011447500000045"

    val v = BCDNumberDecoders.decodeBCDIntegralNumber(comp3BytesUnsigned, mandatorySignNibble = false).toString
    assert(v.contains(comp3ValueUnsigned))
  }

  test("Test COMP-3 wrong format cases") {
    // The low order nybble is >= 10
    val v1 = BCDNumberDecoders.decodeBCDIntegralNumber(Array[Byte](0x1A.toByte,0x11.toByte,0x4C.toByte), mandatorySignNibble = true)
    assert (v1 == null)

    // The high order nybble is >= 10
    val v2 = BCDNumberDecoders.decodeBCDIntegralNumber(Array[Byte](0xA1.toByte,0x11.toByte,0x4F.toByte), mandatorySignNibble = true)
    assert (v2 == null)

    // The sign nybble is wrong
    val v3 = BCDNumberDecoders.decodeBCDIntegralNumber(Array[Byte](0x11.toByte,0x11.toByte,0x40.toByte), mandatorySignNibble = true)
    assert (v3 == null)

    // The sign nybble is present in comp-3u
    val v3u = BCDNumberDecoders.decodeBCDIntegralNumber(Array[Byte](0x11.toByte, 0x11.toByte, 0x4C.toByte), mandatorySignNibble = false)
    assert(v3u == null)

    // This should be a normal number
    val v4 = BCDNumberDecoders.decodeBCDIntegralNumber(Array[Byte](0x11.toByte,0x22.toByte,0x4C.toByte), mandatorySignNibble = true)
    assert (v4 != null)

    // This should be null
    val v5 = BCDNumberDecoders.decodeBCDIntegralNumber(Array[Byte](), mandatorySignNibble = true)
    assert (v5 == null)

    // Use string decoder
    // The low order nybble is >= 10
    val v6 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x1A.toByte,0x11.toByte,0x4C.toByte), scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v6 == null)

    // The high order nybble is >= 10
    val v7 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0xA1.toByte,0x11.toByte,0x4F.toByte), scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v7 == null)

    // The sign nybble is wrong
    val v8 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x11.toByte,0x11.toByte,0x40.toByte), scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v8 == null)

    // This should be a normal number
    val v9 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x11.toByte,0x22.toByte,0x4C.toByte), scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v9 != null)

    // This should be null
    val v10 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](), scale = 0, scaleFactor = 0, mandatorySignNibble = true)
    assert (v10 == null)
  }

  test("Test COMP-3 decimal cases") {
    // A simple decimal number
    val v1 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x15.toByte,0x88.toByte,0x4D.toByte), scale = 2, scaleFactor = 0, mandatorySignNibble = true)
    assert (v1.contains("-158.84"))

    // A simple decimal number with an odd scale
    val v3 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x15.toByte,0x88.toByte,0x4D.toByte), scale = 3, scaleFactor = 0, mandatorySignNibble = true)
    assert (v3.contains("-15.884"))

    // A number the doesn't fit Double
    val byteArray = Array[Byte](0x92.toByte, 0x23.toByte, 0x37.toByte, 0x20.toByte, 0x36.toByte,
                                0x85.toByte, 0x47.toByte, 0x75.toByte, 0x79.toByte, 0x8F.toByte)
    val v2 = BCDNumberDecoders.decodeBigBCDNumber(byteArray, scale = 2, scaleFactor = 0, mandatorySignNibble = true)
    assert (v2.contains("92233720368547757.98"))
  }

  test("Test COMP-3U decimal cases") {
    // A simple decimal number
    val v1 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x15.toByte, 0x88.toByte, 0x40.toByte), scale = 2, scaleFactor = 0, mandatorySignNibble = false)
    assert(v1.contains("1588.40"))
    assert(v1.toDouble == 1588.4)

    // A simple decimal number with an odd scale
    val v3 = BCDNumberDecoders.decodeBigBCDNumber(Array[Byte](0x01.toByte, 0x58.toByte, 0x84.toByte), scale = 3, scaleFactor = 0, mandatorySignNibble = false)
    assert(v3.contains("015.884"))
    assert(v3.toDouble == 15.884)

    // A number the doesn't fit Double
    val byteArray = Array[Byte](0x92.toByte, 0x23.toByte, 0x37.toByte, 0x20.toByte, 0x36.toByte,
      0x85.toByte, 0x47.toByte, 0x75.toByte, 0x79.toByte, 0x81.toByte)
    val v2 = BCDNumberDecoders.decodeBigBCDNumber(byteArray, scale = 2, scaleFactor = 0, mandatorySignNibble = false)
    assert(v2.contains("922337203685477579.81"))
  }


  test("Test Integer to decimal conversion") {
    assert(addDecimalPoint("1238767", 10,0) == "0.0001238767")
    assert(addDecimalPoint("1238767", 9, 0) == "0.001238767")
    assert(addDecimalPoint("1238767", 8, 0) == "0.01238767")
    assert(addDecimalPoint("1238767", 7, 0) == "0.1238767")
    assert(addDecimalPoint("1238767", 6, 0) == "1.238767")
    assert(addDecimalPoint("1238767", 5, 0) == "12.38767")
    assert(addDecimalPoint("1238767", 4, 0) == "123.8767")
    assert(addDecimalPoint("1238767", 3, 0) == "1238.767")
    assert(addDecimalPoint("1238767", 2, 0) == "12387.67")
    assert(addDecimalPoint("1238767", 1, 0) == "123876.7")
    assert(addDecimalPoint("1238767", 0, 0) == "1238767")

    assert(addDecimalPoint("-1238767", 10, 0) == "-0.0001238767")
    assert(addDecimalPoint("-1238767", 9, 0) == "-0.001238767")
    assert(addDecimalPoint("-1238767", 8, 0) == "-0.01238767")
    assert(addDecimalPoint("-1238767", 7, 0) == "-0.1238767")
    assert(addDecimalPoint("-1238767", 6, 0) == "-1.238767")
    assert(addDecimalPoint("-1238767", 5, 0) == "-12.38767")
    assert(addDecimalPoint("-1238767", 4, 0) == "-123.8767")
    assert(addDecimalPoint("-1238767", 3, 0) == "-1238.767")
    assert(addDecimalPoint("-1238767", 2, 0) == "-12387.67")
    assert(addDecimalPoint("-1238767", 1, 0) == "-123876.7")
    assert(addDecimalPoint("-1238767", 0, 0) == "-1238767")
  }

  test("Test Binary numbers decoder for 8 bit numbers") {
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte), bigEndian = true, signed = true) == "0")
    assert(decodeBinaryNumber(Array[Byte](0x01.toByte), bigEndian = false, signed = true) == "1")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte), bigEndian = false, signed = true) == "-1")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte), bigEndian = false, signed = false) == "255")
  }

  test("Test Binary numbers decoder for 16 bit numbers") {
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x00.toByte), bigEndian = true, signed = true) == "0")
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x01.toByte), bigEndian = true, signed = true) == "1")
    assert(decodeBinaryNumber(Array[Byte](0x01.toByte, 0x00.toByte), bigEndian = false, signed = true) == "1")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte), bigEndian = true, signed = true) == "-1")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte), bigEndian = false, signed = false) == "65535")
    assert(decodeBinaryNumber(Array[Byte](0xFE.toByte, 0xFF.toByte), bigEndian = false, signed = true) == "-2")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFE.toByte), bigEndian = true, signed = true) == "-2")
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x16.toByte), bigEndian = true, signed = true, 1) == "2.2")
  }

  test("Test Binary numbers decoder for 32 bit numbers") {
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte), bigEndian = true, signed = true) == "256")
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte), bigEndian = false, signed = true, 2) == "2.56")
    assert(decodeBinaryNumber(Array[Byte](0xFE.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte), bigEndian = false, signed = true) == "-2")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFE.toByte), bigEndian = true, signed = true) == "-2")
    assert(decodeBinaryNumber(Array[Byte](0xFE.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte), bigEndian = false, signed = false) == "4294967294")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFE.toByte), bigEndian = true, signed = false) == "4294967294")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFE.toByte), bigEndian = true, signed = false) == "4294967294")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFE.toByte), bigEndian = true, signed = false, 6) == "4294.967294")
    assert(decodeBinaryNumber(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFE.toByte), bigEndian = true, signed = false, 12) == "0.004294967294")
  }

  test("Test Binary numbers decoder for 64 bit numbers") {
    assert(decodeBinaryNumber(Array[Byte]
      (0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte), bigEndian = false, signed = true) == "65536")
    assert(decodeBinaryNumber(Array[Byte]
      (0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte, 0x00.toByte, 0x00.toByte), bigEndian = true, signed = true) == "65536")
    assert(decodeBinaryNumber(Array[Byte]
      (0xFE.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte), bigEndian = false, signed = true) == "-2")
    assert(decodeBinaryNumber(Array[Byte]
      (0x20.toByte, 0x30.toByte, 0x41.toByte, 0x50.toByte, 0x60.toByte, 0x70.toByte, 0x80.toByte, 0x9F.toByte), bigEndian = true, signed = true, 4) == "231942562156698.0255")
    assert(decodeBinaryNumber(Array[Byte]
      (0xA0.toByte, 0x30.toByte, 0x41.toByte, 0x50.toByte, 0x60.toByte, 0x70.toByte, 0x80.toByte, 0x9F.toByte), bigEndian = true, signed = false, 4) == "1154279765842175.6063")
    assert(decodeBinaryNumber(Array[Byte]
      (0xA0.toByte, 0x30.toByte, 0x41.toByte, 0x50.toByte, 0x60.toByte, 0x70.toByte, 0x80.toByte, 0x9F.toByte), bigEndian = true, signed = true, 4) == "-690394641528779.5553")
  }

  test("Test Binary numbers decoder for non-standard number of bits") {
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x00.toByte, 0x01.toByte), bigEndian = true, signed = true) == "1")
    assert(decodeBinaryNumber(Array[Byte](0x02.toByte, 0x00.toByte, 0x00.toByte), bigEndian = false, signed = true) == "2")
    assert(decodeBinaryNumber(Array[Byte](0x00.toByte, 0x10.toByte, 0x03.toByte), bigEndian = true, signed = true) == "4099")
    assert(decodeBinaryNumber(Array[Byte](0x04.toByte, 0x20.toByte, 0x00.toByte), bigEndian = false, signed = true) == "8196")
    assert(decodeBinaryNumber(Array[Byte](0x80.toByte, 0x10.toByte, 0x03.toByte), bigEndian = true, signed = true) == "-8384509")
    assert(decodeBinaryNumber(Array[Byte](0x04.toByte, 0x20.toByte, 0x80.toByte), bigEndian = false, signed = true, 1) == "-838041.2")
    assert(decodeBinaryNumber(Array[Byte](0x80.toByte, 0x10.toByte, 0x03.toByte), bigEndian = true, signed = false) == "8392707")
    assert(decodeBinaryNumber(Array[Byte](0x04.toByte, 0x20.toByte, 0x80.toByte), bigEndian = false, signed = false, 1) == "839680.4")
  }

  test("Test Binary numbers decoder for very large numbers (over 64 bit)") {
    assert(decodeBinaryNumber(Array[Byte]
       (0x2A.toByte, 0xBB.toByte, 0x3D.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x55.toByte, 0x55.toByte, 0x55.toByte,
        0x88.toByte, 0x31.toByte, 0x01.toByte, 0x55.toByte, 0x24.toByte, 0x08.toByte, 0x77.toByte, 0xCE.toByte, 0x42.toByte),
      bigEndian = true, signed = true) == "3722429894655915141082544883936251449167426")
    assert(decodeBinaryNumber(Array[Byte]
       (0xAA.toByte, 0xBB.toByte, 0x3D.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x55.toByte, 0x55.toByte, 0x55.toByte,
        0x88.toByte, 0x31.toByte, 0x01.toByte, 0x55.toByte, 0x24.toByte, 0x08.toByte, 0x77.toByte, 0xCE.toByte, 0x42.toByte),
      bigEndian = true, signed = false) == "14872802493921226711850404020260432202157634")
    assert(decodeBinaryNumber(Array[Byte]
       (0xAA.toByte, 0xBB.toByte, 0x3D.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x55.toByte, 0x55.toByte, 0x55.toByte,
        0x88.toByte, 0x31.toByte, 0x01.toByte, 0x55.toByte, 0x24.toByte, 0x08.toByte, 0x77.toByte, 0xCE.toByte, 0x42.toByte),
      bigEndian = true, signed = true) == "-7427942704609396429685314252387929303822782")
    assert(decodeBinaryNumber(Array[Byte]
       (0xAA.toByte, 0xBB.toByte, 0x3D.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x55.toByte, 0x55.toByte, 0x55.toByte,
        0x88.toByte, 0x31.toByte, 0x01.toByte, 0x55.toByte, 0x24.toByte, 0x08.toByte, 0x77.toByte, 0xCE.toByte, 0x82.toByte),
      bigEndian = false, signed = true) == "-10905891639409302971822296036747173305664598")
    assert(decodeBinaryNumber(Array[Byte]
       (0xAA.toByte, 0xBB.toByte, 0x3D.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x55.toByte, 0x55.toByte, 0x55.toByte,
        0x88.toByte, 0x31.toByte, 0x01.toByte, 0x55.toByte, 0x24.toByte, 0x08.toByte, 0x77.toByte, 0xCE.toByte, 0x82.toByte),
      bigEndian = false, signed = false) == "11394853559121320169713422235901188200315818")
  }

  test("Test EBCDIC not strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("999", 3, Some(position.Left), isSignSeparate = true, None, None, Some(EBCDIC), None)

    val decoderInt = DecoderSelector.getIntegralDecoder(integralType, strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 14, isSignSeparate = false), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)

    val num1 = decoderInt(Array(0xF1, 0xF2, 0xF3).map(_.toByte))
    assert(num1.isInstanceOf[Integer])
    assert(num1.asInstanceOf[Integer] == 123)

    val num2 = decoderInt(Array(0x60, 0xF2, 0xF3).map(_.toByte))
    assert(num2.isInstanceOf[Integer])
    assert(num2.asInstanceOf[Integer] == -23)

    val num3 = decoderLong(Array(0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0, 0xF1, 0xF2, 0xF3).map(_.toByte))
    assert(num3.isInstanceOf[Long])
    assert(num3.asInstanceOf[Long] == 1234567890123L)

    val num4 = decoderLong(Array(0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0, 0xF1, 0xF2, 0xD3).map(_.toByte))
    assert(num4.isInstanceOf[Long])
    assert(num4.asInstanceOf[Long] == -1234567890123L)
  }

  test("Test EBCDIC strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("999", 3, Some(position.Left), isSignSeparate = true, None, None, Some(EBCDIC), None)

    val decoderInt = DecoderSelector.getIntegralDecoder(integralType, strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 14, isSignSeparate = false), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)

    val num1 = decoderInt(Array(0xF1, 0xF2, 0xF3).map(_.toByte))
    assert(num1.isInstanceOf[BigDecimal])
    assert(num1.asInstanceOf[BigDecimal] == 123)

    val num2 = decoderInt(Array(0x60, 0xF2, 0xF3).map(_.toByte))
    assert(num2.isInstanceOf[BigDecimal])
    assert(num2.asInstanceOf[BigDecimal] == -23)

    val num3 = decoderLong(Array(0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0, 0xF1, 0xF2, 0xF3).map(_.toByte))
    assert(num3.isInstanceOf[BigDecimal])
    assert(num3.asInstanceOf[BigDecimal] == 1234567890123L)

    val num4 = decoderLong(Array(0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0, 0xF1, 0xF2, 0xD3).map(_.toByte))
    assert(num4.isInstanceOf[BigDecimal])
    assert(num4.asInstanceOf[BigDecimal] == -1234567890123L)
  }

  test("Test ASCII not strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("999", 3, Some(position.Left), isSignSeparate = true, None, None, Some(ASCII), None)

    val decoderInt = DecoderSelector.getIntegralDecoder(integralType, strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 14, isSignSeparate = false), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)

    val num1 = decoderInt("123".getBytes)
    assert(num1.isInstanceOf[Integer])
    assert(num1.asInstanceOf[Integer] == 123)

    val num2 = decoderInt("-23".getBytes)
    assert(num2.isInstanceOf[Integer])
    assert(num2.asInstanceOf[Integer] == -23)

    val num3 = decoderLong("1234567890123".getBytes)
    assert(num3.isInstanceOf[Long])
    assert(num3.asInstanceOf[Long] == 1234567890123L)

    val num4 = decoderLong("123456789012L".getBytes)
    assert(num4.isInstanceOf[Long])
    assert(num4.asInstanceOf[Long] == -1234567890123L)
  }

  test("Test ASCII strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("999", 3, Some(position.Left), isSignSeparate = true, None, None, Some(ASCII), None)

    val decoderInt = DecoderSelector.getIntegralDecoder(integralType, strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 14, isSignSeparate = false), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)

    val num1 = decoderInt("123".getBytes)
    assert(num1.isInstanceOf[BigDecimal])
    assert(num1.asInstanceOf[BigDecimal] == 123)

    val num2 = decoderInt("-23".getBytes)
    assert(num2.isInstanceOf[BigDecimal])
    assert(num2.asInstanceOf[BigDecimal] == -23)

    val num3 = decoderLong("1234567890123".getBytes)
    assert(num3.isInstanceOf[BigDecimal])
    assert(num3.asInstanceOf[BigDecimal] == 1234567890123L)

    val num4 = decoderLong("123456789012L".getBytes)
    assert(num4.isInstanceOf[BigDecimal])
    assert(num4.asInstanceOf[BigDecimal] == -1234567890123L)
  }

  test("Test BCD not strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("S99999", 6, Some(position.Right), isSignSeparate = false, None, Some(COMP3()), Some(EBCDIC), None)

    val decoderInt = DecoderSelector.getIntegralDecoder(integralType, strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 13, compact = Some(COMP3U())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)

    val num1 = decoderInt(Array(0x12, 0x34, 0x5C).map(_.toByte))
    assert(num1.isInstanceOf[Integer])
    assert(num1.asInstanceOf[Integer] == 12345)

    val num2 = decoderInt(Array(0x12, 0x34, 0x5D).map(_.toByte))
    assert(num2.isInstanceOf[Integer])
    assert(num2.asInstanceOf[Integer] == -12345)

    val num3 = decoderLong(Array(0x12, 0x34, 0x56, 0x78, 0x90, 0x12, 0x30).map(_.toByte))
    assert(num3.isInstanceOf[Long])
    assert(num3.asInstanceOf[Long] == 12345678901230L)
  }

  test("Test BCD strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("S99999", 6, Some(position.Right), isSignSeparate = false, None, Some(COMP3()), Some(EBCDIC), None)

    val decoderInt = DecoderSelector.getIntegralDecoder(integralType, strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 13, compact = Some(COMP3U())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)

    val num1 = decoderInt(Array(0x12, 0x34, 0x5C).map(_.toByte))
    assert(num1.isInstanceOf[BigDecimal])
    assert(num1.asInstanceOf[BigDecimal] == 12345)

    val num2 = decoderInt(Array(0x12, 0x34, 0x5D).map(_.toByte))
    assert(num2.isInstanceOf[BigDecimal])
    assert(num2.asInstanceOf[BigDecimal] == -12345)

    val num3 = decoderLong(Array(0x12, 0x34, 0x56, 0x78, 0x90, 0x12, 0x30).map(_.toByte))
    assert(num3.isInstanceOf[BigDecimal])
    assert(num3.asInstanceOf[BigDecimal] == 12345678901230L)
  }

  test("Test Binary not strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("999", 3, Some(position.Left), isSignSeparate = true, None, Some(COMP4()), Some(EBCDIC), None)

    val decoderSignedByte = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 1, compact = Some(COMP9())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderUnsignedByte = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 1, compact = Some(COMP9()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderSignedShort = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 3, compact = Some(COMP4())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderUnsignedShort = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 3, compact = Some(COMP5()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderSignedInt = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 8, compact = Some(COMP4())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderUnsignedIntBe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 8, compact = Some(COMP5()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderUnsignedIntLe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 8, compact = Some(COMP9()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderSignedLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 15, compact = Some(COMP4())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderUnsignedLongBe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 15, compact = Some(COMP5()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)
    val decoderUnsignedLongLe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 15, compact = Some(COMP9()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = false)

    val num1 = decoderSignedByte(Array(0x10).map(_.toByte))
    assert(num1.isInstanceOf[Integer])
    assert(num1.asInstanceOf[Integer] == 16)

    val num2 = decoderSignedByte(Array(0x90).map(_.toByte))
    assert(num2.isInstanceOf[Integer])
    assert(num2.asInstanceOf[Integer] == -112)

    val num3 = decoderUnsignedByte(Array(0x90).map(_.toByte))
    assert(num3.isInstanceOf[Integer])
    assert(num3.asInstanceOf[Integer] == 144)

    val num4 = decoderSignedShort(Array(0x10, 0x01).map(_.toByte))
    assert(num4.isInstanceOf[Integer])
    assert(num4.asInstanceOf[Integer] == 4097)

    val num5 = decoderSignedShort(Array(0x90, 0x00).map(_.toByte))
    assert(num5.isInstanceOf[Integer])
    assert(num5.asInstanceOf[Integer] == -28672)

    val num6 = decoderUnsignedShort(Array(0x90, 0x00).map(_.toByte))
    assert(num6.isInstanceOf[Integer])
    assert(num6.asInstanceOf[Integer] == 36864)

    val num7 = decoderSignedInt(Array(0x01, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num7.isInstanceOf[Integer])
    assert(num7.asInstanceOf[Integer] == 16777216)

    val num8 = decoderSignedInt(Array(0x90, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num8.isInstanceOf[Integer])
    assert(num8.asInstanceOf[Integer] == -1879048192)

    val num9 = decoderUnsignedIntBe(Array(0x00, 0x90, 0x00, 0x00).map(_.toByte))
    assert(num9.isInstanceOf[Integer])
    assert(num9.asInstanceOf[Integer] == 9437184)

    val num10 = decoderUnsignedIntLe(Array(0x00, 0x00, 0x90, 0x00).map(_.toByte))
    assert(num10.isInstanceOf[Integer])
    assert(num10.asInstanceOf[Integer] == 9437184)

    val num11 = decoderSignedLong(Array(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num11.isInstanceOf[Long])
    assert(num11.asInstanceOf[Long] == 72057594037927936L)

    val num12 = decoderSignedLong(Array(0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num12.isInstanceOf[Long])
    assert(num12.asInstanceOf[Long] == -8070450532247928832L)

    val num13 = decoderUnsignedLongBe(Array(0x00, 0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num13.isInstanceOf[Long])
    assert(num13.asInstanceOf[Long] == 40532396646334464L)

    val num14 = decoderUnsignedLongLe(Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x90, 0x00).map(_.toByte))
    assert(num14.isInstanceOf[Long])
    assert(num14.asInstanceOf[Long] == 40532396646334464L)
  }

  test("Test Binary strict integral precision numbers") {
    val integralType = za.co.absa.cobrix.cobol.parser.ast.datatype.Integral("999", 3, Some(position.Left), isSignSeparate = true, None, Some(COMP4()), Some(EBCDIC), None)

    val decoderSignedByte = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 1, compact = Some(COMP9())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderUnsignedByte = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 1, compact = Some(COMP9()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderSignedShort = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 3, compact = Some(COMP4())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderUnsignedShort = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 3, compact = Some(COMP5()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderSignedInt = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 8, compact = Some(COMP4())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderUnsignedIntBe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 8, compact = Some(COMP5()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderUnsignedIntLe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 8, compact = Some(COMP9()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderSignedLong = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 15, compact = Some(COMP4())), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderUnsignedLongBe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 15, compact = Some(COMP5()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)
    val decoderUnsignedLongLe = DecoderSelector.getIntegralDecoder(integralType.copy(precision = 15, compact = Some(COMP9()), signPosition = None), strictSignOverpunch = false, improvedNullDetection = false, strictIntegralPrecision = true)

    val num1 = decoderSignedByte(Array(0x10).map(_.toByte))
    assert(num1.isInstanceOf[BigDecimal])
    assert(num1.asInstanceOf[BigDecimal] == 16)

    val num2 = decoderSignedByte(Array(0x90).map(_.toByte))
    assert(num2.isInstanceOf[BigDecimal])
    assert(num2.asInstanceOf[BigDecimal] == -112)

    val num3 = decoderUnsignedByte(Array(0x90).map(_.toByte))
    assert(num3.isInstanceOf[BigDecimal])
    assert(num3.asInstanceOf[BigDecimal] == 144)

    val num4 = decoderSignedShort(Array(0x10, 0x01).map(_.toByte))
    assert(num4.isInstanceOf[BigDecimal])
    assert(num4.asInstanceOf[BigDecimal] == 4097)

    val num5 = decoderSignedShort(Array(0x90, 0x00).map(_.toByte))
    assert(num5.isInstanceOf[BigDecimal])
    assert(num5.asInstanceOf[BigDecimal] == -28672)

    val num6 = decoderUnsignedShort(Array(0x90, 0x00).map(_.toByte))
    assert(num6.isInstanceOf[BigDecimal])
    assert(num6.asInstanceOf[BigDecimal] == 36864)

    val num7 = decoderSignedInt(Array(0x01, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num7.isInstanceOf[BigDecimal])
    assert(num7.asInstanceOf[BigDecimal] == 16777216)

    val num8 = decoderSignedInt(Array(0x90, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num8.isInstanceOf[BigDecimal])
    assert(num8.asInstanceOf[BigDecimal] == -1879048192)

    val num9 = decoderUnsignedIntBe(Array(0x00, 0x90, 0x00, 0x00).map(_.toByte))
    assert(num9.isInstanceOf[BigDecimal])
    assert(num9.asInstanceOf[BigDecimal] == 9437184)

    val num10 = decoderUnsignedIntLe(Array(0x00, 0x00, 0x90, 0x00).map(_.toByte))
    assert(num10.isInstanceOf[BigDecimal])
    assert(num10.asInstanceOf[BigDecimal] == 9437184)

    val num11 = decoderSignedLong(Array(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num11.isInstanceOf[BigDecimal])
    assert(num11.asInstanceOf[BigDecimal] == 72057594037927936L)

    val num12 = decoderSignedLong(Array(0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num12.isInstanceOf[BigDecimal])
    assert(num12.asInstanceOf[BigDecimal] == -8070450532247928832L)

    val num13 = decoderUnsignedLongBe(Array(0x00, 0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte))
    assert(num13.isInstanceOf[BigDecimal])
    assert(num13.asInstanceOf[BigDecimal] == 40532396646334464L)

    val num14 = decoderUnsignedLongLe(Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x90, 0x00).map(_.toByte))
    assert(num14.isInstanceOf[BigDecimal])
    assert(num14.asInstanceOf[BigDecimal] == 40532396646334464L)
  }

}
