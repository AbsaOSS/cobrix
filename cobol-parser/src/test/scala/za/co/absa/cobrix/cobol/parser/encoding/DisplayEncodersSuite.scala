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
import za.co.absa.cobrix.cobol.testutils.ComparisonUtils.assertArraysEqual

class DisplayEncodersSuite extends AnyWordSpec {
  "encodeDisplayNumber" should {
    "integral number" when {
      "encode a number" in  {
        val expected = Array(0x40, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(12345), isSigned = true, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a number with an even precision" in  {
        val expected = Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1234), isSigned = true, 4, 4, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small number" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x40, 0x40, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(5), isSigned = true, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode an unsigned number" in  {
        val expected = Array(0x40, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(12345), isSigned = false, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a negative number" in  {
        val expected = Array(0x60, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-12345), isSigned = true, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small negative number" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x40, 0x60, 0xF7).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-7), isSigned = true, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a too big number" ignore  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(123456), isSigned = true, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a too big negative number" in  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-123456), isSigned = true, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a number with negative scale" in  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(12345), isSigned = true, 6, 5, -1, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "attempt to encode a signed number when unsigned is expected" in  {
        val expected = Array[Byte](0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-12345), isSigned = false, 6, 5, 0, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "attempt to encode a number with an incorrect precision" ignore  {
        val expected = Array[Byte](0x00, 0x00, 0x00, 0x00, 0x00)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(12345), isSigned = false, 5, 4, 0, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "attempt to encode a number with zero precision" in  {
        val expected = Array[Byte](0x00)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(12345), isSigned = false, 1, 0, 0, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }
    }

    "decimal number" when {
      "encode a number" in  {
        val expected = Array(0x40, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(123.45), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a number with explicit decimal" in  {
        val expected = Array(0x40, 0xF1, 0xF2, 0xF3, 0x4B, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(123.45), isSigned = true, 7, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a small number 1" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x40, 0x40, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.05), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small number 2" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x40, 0xF5, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.5), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small number 3" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x40, 0x40, 0xF1).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.005), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small number 1 with explicit decimal point" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x4B, 0xF0, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.05), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a small number 2 with explicit decimal point" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x4B, 0xF5, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.5), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a small number 3 with explicit decimal point" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x4B, 0xF0, 0xF1).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.005), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode an unsigned number" in  {
        val expected = Array(0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1234.5), isSigned = false, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode an unsigned number with explicit decimal point" in  {
        val expected = Array(0xF1, 0xF2, 0xF3, 0xF4, 0x4B, 0xF5, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1234.5), isSigned = false, 7, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a negative number" in {
        val expected = Array(0x60, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-12.345), isSigned = true, 6, 5, 3, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a negative number with explicit decimal point" in {
        val expected = Array(0x60, 0xF1, 0xF2, 0x4B, 0xF3, 0xF4, 0xF5).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-12.345), isSigned = true, 7, 5, 3, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a small negative number" in  {
        val expected = Array(0x40, 0x40, 0x40, 0x60, 0xF7).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-0.00007), isSigned = true, 5, 4, 5, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small negative number with explicit decimal point" in  {
        val expected = Array(0x40, 0x60, 0x4B, 0xF0, 0xF0, 0xF0, 0xF0, 0xF7).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-0.00007), isSigned = true, 8, 4, 5, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a too precise number" in  {
        val expected = Array(0x40, 0xF1, 0xF2, 0xF3, 0xF4, 0xF6).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(123.456), isSigned = false, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a too precise number with explicit decimal point" in  {
        val expected = Array(0xF1, 0xF2, 0xF3, 0x4B, 0xF4, 0xF6).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(123.456), isSigned = false, 6, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a too big number" ignore  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1234.56), isSigned = false, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a too big number with explicit decimal point" ignore {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1234.56), isSigned = false, 7, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a too big negative number" in  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-1234.56), isSigned = true, 6, 5, 2, 0, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a too big negative number with explicit decimal point" in  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-1234.56), isSigned = true, 7, 5, 2, 0, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a number with positive scale factor" in  {
        val expected = Array(0x40, 0x40, 0x40, 0xF1, 0xF2, 0xF3).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(12300), isSigned = true, 6, 5, 0, 2, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a number with positive scale factor with explicit decimal point" in  {
        val expected = Array(0x00, 0x00, 0x00, 0x00, 0x00, 0x00).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(123400), isSigned = true, 6, 5, 1, 2, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a number with negative scale factor" in  {
        val expected = Array(0x40, 0x40, 0x40, 0xF1, 0xF2, 0xF3).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1.23), isSigned = true, 6, 5, 0, -2, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a number with negative scale factor with explicit decimal point" in  {
        val expected = Array(0x40, 0xF1, 0xF2, 0xF3, 0x4B, 0xF4).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(1.234), isSigned = true, 6, 5, 1, -2, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a small number with negative scale factor" in  {
        val expected = Array(0x40, 0x40, 0xF1, 0xF2, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.00012), isSigned = false, 5, 4, 3, -3, explicitDecimalPoint = false)

        assertArraysEqual(actual, expected)
      }

      "encode a small number with negative scale factor with explicit decimal point" in  {
        val expected = Array(0x4B, 0xF1, 0xF2, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(0.00012), isSigned = false, 4, 4, 3, -3, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

      "encode a small negative number with negative scale factor with explicit decimal point" in  {
        val expected = Array(0x60, 0x4B, 0xF1, 0xF2, 0xF0).map(_.toByte)
        val actual = DisplayEncoders.encodeDisplayNumber(new java.math.BigDecimal(-0.00012), isSigned = true, 5, 4, 3, -3, explicitDecimalPoint = true)

        assertArraysEqual(actual, expected)
      }

    }
  }


}
