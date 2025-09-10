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

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.testutils.ComparisonUtils._

class BinaryEncodersSuite extends AnyWordSpec {
  "encodeBinaryNumber" should {
    "encode a null" in  {
      val expected = Array(0x00, 0x00, 0x00, 0x00).map(_.toByte)
      val actual = BinaryEncoders.encodeBinaryNumber(null: java.math.BigDecimal, isSigned = true, outputSize = 4, bigEndian = true, precision = 5, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "encode a positive integer in big-endian format" in {
      val expected = Array(0x00, 0x00, 0x30, 0x39).map(_.toByte) // 12345 in hex
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(12345), isSigned = true, outputSize = 4, bigEndian = true, precision = 5, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "encode a positive integer in little-endian format" in {
      val expected = Array(0x39, 0x30, 0x00, 0x00).map(_.toByte) // 12345 in hex reversed
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(12345), isSigned = true, outputSize = 4, bigEndian = false, precision = 5, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "encode a negative integer -1 big-endian format" in {
      val expected = Array(0xFF, 0xFF, 0xFF, 0xFF).map(_.toByte)
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(-1), isSigned = true, outputSize = 4, bigEndian = true, precision = 5, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "encode a negative integer in big-endian format" in {
      val expected = Array(0xFF, 0xFF, 0xCF, 0xC7).map(_.toByte) // -12345 in hex (two's complement)
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(-12345), isSigned = true, outputSize = 4, bigEndian = true, precision = 5, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "encode a negative integer -1 little-endian format" in {
      val expected = Array(0xFF, 0xFF, 0xFF, 0xFF).map(_.toByte)
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(-1), isSigned = true, outputSize = 4, bigEndian = false, precision = 4, scale = 0, scaleFactor = 0)
      assertArraysEqual(actual, expected)
    }

    "encode a negative integer in little-endian format" in {
      val expected = Array(0xC7, 0xCF, 0xFF, 0xFF).map(_.toByte) // -12345 in hex reversed (two's complement)
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(-12345), isSigned = true, outputSize = 4, bigEndian = false, precision = 5, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "attempt to encode a number with the maximum digits of the precision" in {
      val expected = Array(0xF1, 0xD8, 0xFF, 0xFF).map(_.toByte)
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(-9999), isSigned = true, outputSize = 4, bigEndian = false, precision = 4, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "attempt to encode a number bigger than the precision" in {
      val expected = Array(0xF0, 0xD8, 0xFF, 0xFF).map(_.toByte)
      val actual = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(-10000), isSigned = true, outputSize = 4, bigEndian = false, precision = 4, scale = 0, scaleFactor = 0)

      assertArraysEqual(actual, expected)
    }

    "handle zero correctly" in {
      val expected = Array[Byte](0x00, 0x00, 0x00, 0x00)

      val actualBigEndian = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(0), isSigned = true, outputSize = 4, bigEndian = true, precision = 1, scale = 0, scaleFactor = 0)
      val actualLittleEndian = BinaryEncoders.encodeBinaryNumber(new java.math.BigDecimal(0), isSigned = true, outputSize = 4, bigEndian = false, precision = 1, scale = 0, scaleFactor = 0)

      assertArraysEqual(actualBigEndian, expected)
      assertArraysEqual(actualLittleEndian, expected)
    }
  }
}
