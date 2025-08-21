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

import org.scalatest.Assertion
import org.scalatest.wordspec.AnyWordSpec

class BCDNumberEncodersSuite extends AnyWordSpec {
  "encodeBCDNumber" should {
    "integral number" when {
      "encode a number" in  {
        val expected = Array[Byte](0x12, 0x34, 0x5C)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(12345), 5, 0, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a small number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x5C)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(5), 5, 0, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode an unsigned number" in  {
        val expected = Array[Byte](0x12, 0x34, 0x5F)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(12345), 5, 0, 0, signed = false, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a negative number" in  {
        val expected = Array[Byte](0x12, 0x34, 0x5D)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-12345), 5, 0, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a small negative number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x7D)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-7), 4, 0, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a number without sign nibble" in  {
        val expected = Array[Byte](0x01, 0x23, 0x45)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(12345), 5, 0, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "encode a too big number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(123456), 5, 0, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "encode a too big negative number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-123456), 5, 0, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "attempt to encode a negative number without sign nibble" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-12345), 5, 0, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "attempt to encode a signed number without a sign nibble" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-12345), 5, 0, 0, signed = true, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "attempt to encode a number with an incorrect precision" in  {
        val expected = Array[Byte](0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(12345), 4, 0, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "attempt to encode a number with an incorrect precision with sign nibble" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(12345), 4, 0, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }
    }

    "decimal number" when {
      "encode a number" in  {
        val expected = Array[Byte](0x12, 0x34, 0x5C)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(123.45), 5, 2, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a small number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x5C)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(0.05), 5, 2, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode an unsigned number" in  {
        val expected = Array[Byte](0x12, 0x34, 0x5F)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(1234.5), 5, 1, 0, signed = false, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a negative number" in  {
        val expected = Array[Byte](0x12, 0x34, 0x5D)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-12.345), 5, 3, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a small negative number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x7D)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-0.00007), 4, 5, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a number without sign nibble" in  {
        val expected = Array[Byte](0x01, 0x23, 0x45)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(123.45), 5, 2, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "encode a too precise number" in  {
        val expected = Array[Byte](0x01, 0x23, 0x46)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(123.456), 5, 2, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "encode a too big number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(1234.56), 5, 2, 0, signed = false, mandatorySignNibble = false)

        checkExpected(actual, expected)
      }

      "encode a too big negative number" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(-1234.56), 5, 2, 0, signed = true, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a number with positive scale factor" in  {
        val expected = Array[Byte](0x00, 0x12, 0x3F)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(12300), 5, 0, 2, signed = false, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }

      "encode a number with negative scale factor" in  {
        val expected = Array[Byte](0x00, 0x12, 0x3F)
        val actual = BCDNumberEncoders.encodeBCDNumber(new java.math.BigDecimal(1.23), 5, 0, -2, signed = false, mandatorySignNibble = true)

        checkExpected(actual, expected)
      }
    }
  }

  def checkExpected(actual: Array[Byte], expected: Array[Byte]): Assertion = {
    if (!actual.sameElements(expected)) {
      val actualHex = actual.map(b => f"$b%02X").mkString(" ")
      val expectedHex = expected.map(b => f"$b%02X").mkString(" ")
      fail(s"Actual: $actualHex\nExpected: $expectedHex")
    } else {
      succeed
    }
  }


}
