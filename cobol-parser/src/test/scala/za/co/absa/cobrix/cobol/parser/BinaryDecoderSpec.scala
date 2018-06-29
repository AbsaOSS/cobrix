/*
 * Copyright 2018 Barclays Africa Group Limited
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

package za.co.absa.cobrix.cobol.parser

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.common.BinaryUtils
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC}
import za.co.absa.cobrix.cobol.parser.common.BinaryUtils

class BinaryDecoderSpec extends FunSuite {
  import za.co.absa.cobrix.cobol.parser.common.BinaryUtils.decodeBinaryNumber
  import za.co.absa.cobrix.cobol.parser.common.BinaryUtils.addDecimalPoint

  test("Test string fields decoding") {
    assert(BinaryUtils.decodeString(ASCII(), "TestString".toCharArray.map(_.toByte), 4) == "Test")
    assert(BinaryUtils.decodeString(ASCII(), "TestString".toCharArray.map(_.toByte), 10) == "TestString")

    // "TestString"
    val ebcdicString = Array[Byte](0xE3.toByte, 0x85.toByte, 0xA2.toByte, 0xA3.toByte, 0xE2.toByte,
      0xA3.toByte, 0x99.toByte, 0x89.toByte, 0x95.toByte, 0x87.toByte)
    assert(BinaryUtils.decodeString(EBCDIC(), ebcdicString, 10) == "TestString")
  }

  test("Test uncompressed number decoding") {
    assert(BinaryUtils.decodeUncompressedNumber(ASCII(), "100200".toCharArray.map(_.toByte), explicitDecimal = false, 0).contains("100200"))
    assert(BinaryUtils.decodeUncompressedNumber(ASCII(), "1002551".toCharArray.map(_.toByte), explicitDecimal = false, 3).contains("1002.551"))
    assert(BinaryUtils.decodeUncompressedNumber(ASCII(), "1002.551".toCharArray.map(_.toByte), explicitDecimal = true, 0).contains("1002.551"))

    // "1002551"
    val ebcdicNum = Array[Byte](0xF1.toByte, 0xF0.toByte, 0xF0.toByte, 0xF2.toByte, 0xF5.toByte, 0xF5.toByte, 0xF1.toByte)
    assert(BinaryUtils.decodeUncompressedNumber(EBCDIC(), ebcdicNum, explicitDecimal = false, 2).contains("10025.51"))
  }

  test("Test positive COMP-3 format decoding") {
    val comp3BytesPositive = Array[Byte](0x10.toByte,0x11.toByte,0x44.toByte, 0x75.toByte,0x00.toByte,0x00.toByte,0x00.toByte,0x4F.toByte)
    val comp3ValuePositive = "101144750000004"

    val v = BinaryUtils.decodeSignedBCD(comp3BytesPositive)
    assert (v.contains(comp3ValuePositive))
  }

  test("Test negative COMP-3 format decoding") {
    val comp3BytesNegative = Array[Byte](0x10.toByte,0x11.toByte,0x44.toByte, 0x75.toByte,0x00.toByte,0x00.toByte,0x00.toByte,0x4D.toByte)
    val comp3ValueNegative = "-101144750000004"

    val v = BinaryUtils.decodeSignedBCD(comp3BytesNegative)
    assert (v.contains(comp3ValueNegative))
  }

  test("Test unsigned COMP-3 format decoding") {
    val comp3BytesUnsigned = Array[Byte](0x10.toByte,0x11.toByte,0x44.toByte, 0x75.toByte,0x00.toByte,0x00.toByte,0x00.toByte,0x4C.toByte)
    val comp3ValueUnsigned = "101144750000004"

    val v = BinaryUtils.decodeSignedBCD(comp3BytesUnsigned)
    assert (v.contains(comp3ValueUnsigned))
  }

  test("Test COMP-3 wrong format cases") {
    // The low order nybble is >= 10
    val v1 = BinaryUtils.decodeSignedBCD(Array[Byte](0x1A.toByte,0x11.toByte,0x4C.toByte))
    assert (v1.isEmpty)

    // The high order nybble is >= 10
    val v2 = BinaryUtils.decodeSignedBCD(Array[Byte](0xA1.toByte,0x11.toByte,0x4F.toByte))
    assert (v2.isEmpty)

    // The sign nybble is wrong
    val v3 = BinaryUtils.decodeSignedBCD(Array[Byte](0x11.toByte,0x11.toByte,0x40.toByte))
    assert (v3.isEmpty)

    // This should be a normal number
    val v4 = BinaryUtils.decodeSignedBCD(Array[Byte](0x11.toByte,0x22.toByte,0x4C.toByte))
    assert (v4.nonEmpty)

    // This should be zero
    val v5 = BinaryUtils.decodeSignedBCD(Array[Byte]())
    assert (v5.contains("0"))
  }

  test("Test COMP-3 decimal cases") {
    // A simple decimal number
    val v1 = BinaryUtils.decodeSignedBCD(Array[Byte](0x15.toByte,0x88.toByte,0x4D.toByte), 2)
    assert (v1.contains("-158.84"))

    // A number the doesn't fit Double
    val byteArray = Array[Byte](0x92.toByte, 0x23.toByte, 0x37.toByte, 0x20.toByte, 0x36.toByte,
                                0x85.toByte, 0x47.toByte, 0x75.toByte, 0x79.toByte, 0x8F.toByte)
    val v2 = BinaryUtils.decodeSignedBCD(byteArray, 2)
    assert (v2.contains("92233720368547757.98"))
  }

  test("Test Integer to decimal conversion") {
    assert(addDecimalPoint("1238767", 10) == "0.0001238767")
    assert(addDecimalPoint("1238767", 9) == "0.001238767")
    assert(addDecimalPoint("1238767", 8) == "0.01238767")
    assert(addDecimalPoint("1238767", 7) == "0.1238767")
    assert(addDecimalPoint("1238767", 6) == "1.238767")
    assert(addDecimalPoint("1238767", 5) == "12.38767")
    assert(addDecimalPoint("1238767", 4) == "123.8767")
    assert(addDecimalPoint("1238767", 3) == "1238.767")
    assert(addDecimalPoint("1238767", 2) == "12387.67")
    assert(addDecimalPoint("1238767", 1) == "123876.7")
    assert(addDecimalPoint("1238767", 0) == "1238767")

    assert(addDecimalPoint("-1238767", 10) == "-0.0001238767")
    assert(addDecimalPoint("-1238767", 9) == "-0.001238767")
    assert(addDecimalPoint("-1238767", 8) == "-0.01238767")
    assert(addDecimalPoint("-1238767", 7) == "-0.1238767")
    assert(addDecimalPoint("-1238767", 6) == "-1.238767")
    assert(addDecimalPoint("-1238767", 5) == "-12.38767")
    assert(addDecimalPoint("-1238767", 4) == "-123.8767")
    assert(addDecimalPoint("-1238767", 3) == "-1238.767")
    assert(addDecimalPoint("-1238767", 2) == "-12387.67")
    assert(addDecimalPoint("-1238767", 1) == "-123876.7")
    assert(addDecimalPoint("-1238767", 0) == "-1238767")
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


}
