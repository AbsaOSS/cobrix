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

import java.nio.charset.StandardCharsets

import org.scalatest.WordSpec

class StringDecodersSpec extends WordSpec {
  import StringDecoders._

  private val asciiString = "AbCdEfGhIjKlMnOpQrStUvWxYz 0123456789 !@#$%^&*()[]{};'\\\"/.,"
  private val unicodeString = "AbCdEfGhIjKlMnOpQrStUvWxYz ěščřžýáíé 0123456789 !@#$%^&*()[]{};'\\\"/.,"

  "decodeEbcdicString()" should {
    "work" in {

    }
  }

  "decodeAsciiString()" should {
    "decode an ASCII string" in {
      val bytes = asciiString.getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimNone)

      assert(actual == asciiString)
    }

    "decode an ASCII string with left trimming" in {
      val bytes = (asciiString + "  \t ").getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimRight)

      assert(actual == asciiString)
    }

    "decode an ASCII string with right trimming" in {
      val bytes = ("  \t " + asciiString).getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimLeft)

      assert(actual == asciiString)
    }

    "decode an ASCII string with left+right trimming" in {
      val bytes = ("  \t " + asciiString + "  \t ").getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimBoth)

      assert(actual == asciiString)
    }
  }

  "decodeUtf16String()" should {
    "decode an UTF-16 BE string" in {
      val bytes = unicodeString.getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimNone, isUtf16BigEndian = true)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string" in {
      val bytes = unicodeString.getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimNone, isUtf16BigEndian = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 BE string with left trimming" in {
      val bytes = (unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimRight, isUtf16BigEndian = true)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string with left trimming" in {
      val bytes = (unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimRight, isUtf16BigEndian = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 BE string with right trimming" in {
      val bytes = ("  \t " + unicodeString).getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimLeft, isUtf16BigEndian = true)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string with right trimming" in {
      val bytes = ("  \t " + unicodeString).getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimLeft, isUtf16BigEndian = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 BE string with left+right trimming" in {
      val bytes = ("  \t " + unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimBoth, isUtf16BigEndian = true)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string with left+right trimming" in {
      val bytes = ("  \t " + unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimBoth, isUtf16BigEndian = false)

      assert(actual == unicodeString)
    }
  }

  "decodeHex()" should {
    "decode bytes as HEX strings" in {
      val hex = decodeHex(Array[Byte](0, 3, 16, 127, -1, -127))

      assert(hex == "0003107FFF81")
    }
  }

  "decodeRaw()" should {
    "should work on empty arrays" in {
      val data = Array.empty[Byte]
      assert(decodeRaw(data) sameElements data)
    }

    "should not do any transformations on the input data" in {
      val data = Array[Byte](0, 1, 2, -1)
      assert(decodeRaw(data) sameElements data)
    }
  }

  "decodeEbcdicNumber()" should {
    "work" in {

    }
  }

  "decodeAsciiNumber()" should {
    "decode strings as parsable strings" in {
      assert(decodeAsciiNumber("1".getBytes, isUnsigned = true) == "1")
      assert(decodeAsciiNumber("1".getBytes, isUnsigned = false) == "1")
      assert(decodeAsciiNumber(" 1 ".getBytes, isUnsigned = true) == "1")
      assert(decodeAsciiNumber(" 1 ".getBytes, isUnsigned = false) == "1")
      assert(decodeAsciiNumber("-1".getBytes, isUnsigned = false) == "-1")

      assert(decodeAsciiNumber(" 18938717862,00 ".getBytes, isUnsigned = true) == "18938717862.00")
      assert(decodeAsciiNumber(" 18938717862.00 ".getBytes, isUnsigned = true) == "18938717862.00")
      assert(decodeAsciiNumber(" + 18938717862.00 ".getBytes, isUnsigned = true) == "+18938717862.00")
      assert(decodeAsciiNumber(" - 18938717862.00 ".getBytes, isUnsigned = false) == "-18938717862.00")
      assert(decodeAsciiNumber(" + 18938717862.00 ".getBytes, isUnsigned = false) == "+18938717862.00")
    }

    "return null if negative number encountered while parsing unsigned numbers" in {
      assert(decodeAsciiNumber("-1".getBytes, isUnsigned = true) == null)
      assert(decodeAsciiNumber(" - 18938717862.00 ".getBytes, isUnsigned = true) == null)
    }

    "re-position leading and trailing sign" in {
      assert(decodeAsciiNumber("+100,00".getBytes, isUnsigned = false) == "+100.00")
      assert(decodeAsciiNumber("100.00+".getBytes, isUnsigned = false) == "+100.00")
      assert(decodeAsciiNumber("-100.00".getBytes, isUnsigned = false) == "-100.00")
      assert(decodeAsciiNumber("100,00-".getBytes, isUnsigned = false) == "-100.00")
    }

    "return trimmed string if non-digit characters are encountered" in {
      assert(decodeAsciiNumber("AAABBBCCC".getBytes, isUnsigned = false) == "AAABBBCCC")
    }
  }

  "decodeEbcdicInt()" should {
    "work" in {

    }
  }

  "decodeAsciiInt()" should {
    "decode parsable ints" in {
      assert(decodeAsciiInt("+100".getBytes, isUnsigned = false) == 100)
      assert(decodeAsciiInt("100+".getBytes, isUnsigned = false) == 100)
      assert(decodeAsciiInt("-100".getBytes, isUnsigned = false) == -100)
      assert(decodeAsciiInt("100-".getBytes, isUnsigned = false) == -100)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeAsciiInt("+100".getBytes, isUnsigned = true) == 100)
      assert(decodeAsciiInt("100+".getBytes, isUnsigned = true) == 100)
      assert(decodeAsciiInt("-100".getBytes, isUnsigned = true) == null)
      assert(decodeAsciiInt("100-".getBytes, isUnsigned = true) == null)
    }

    "return null on non-ints ints" in {
      assert(decodeAsciiInt("+100,0".getBytes, isUnsigned = true) == null)
      assert(decodeAsciiInt("100.00+".getBytes, isUnsigned = false) == null)
      assert(decodeAsciiInt("-100,000".getBytes, isUnsigned = false) == null)
      assert(decodeAsciiInt("100.000-".getBytes, isUnsigned = false) == null)
    }

    "return null on unparsable ints" in {
      assert(decodeAsciiInt("+1000000000000".getBytes, isUnsigned = true) == null)
      assert(decodeAsciiInt("AAA".getBytes, isUnsigned = false) == null)
    }
  }

  "decodeEbcdicLong()" should {
    "work" in {

    }
  }

  "decodeAsciiLong()" should {
    "decode parsable longs" in {
      assert(decodeAsciiLong("+1000000000000000".getBytes, isUnsigned = false) == 1000000000000000L)
      assert(decodeAsciiLong("1000000000000000+".getBytes, isUnsigned = false) == 1000000000000000L)
      assert(decodeAsciiLong("-1000000000000000".getBytes, isUnsigned = false) == -1000000000000000L)
      assert(decodeAsciiLong("1000000000000000-".getBytes, isUnsigned = false) == -1000000000000000L)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeAsciiLong("+1000000000000000".getBytes, isUnsigned = true) == 1000000000000000L)
      assert(decodeAsciiLong("1000000000000000+".getBytes, isUnsigned = true) == 1000000000000000L)
      assert(decodeAsciiLong("-1000000000000000".getBytes, isUnsigned = true) == null)
      assert(decodeAsciiLong("1000000000000000-".getBytes, isUnsigned = true) == null)
    }

    "return null on non-ints longs" in {
      assert(decodeAsciiLong("+1000000000000000,0".getBytes, isUnsigned = true) == null)
      assert(decodeAsciiLong("1000000000000000.00+".getBytes, isUnsigned = false) == null)
      assert(decodeAsciiLong("-1000000000000000,000".getBytes, isUnsigned = false) == null)
      assert(decodeAsciiLong("1000000000000000.000-".getBytes, isUnsigned = false) == null)
    }

    "return null on unparsable longs" in {
      assert(decodeAsciiLong("AAA".getBytes, isUnsigned = false) == null)
    }
  }

  "decodeEbcdicBigNumber()" should {
    "work" in {

    }
  }

  "decodeAsciiBigNumber()" should {
    "decode parsable decimals" in {
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 0, 0) == BigDecimal("1000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 1, 0) == BigDecimal("100.0"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 3, 0) == BigDecimal("1.000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 4, 0) == BigDecimal("0.1000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 5, 0) == null)
    }

    "decode numbers with scale factor" in {
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 0, -1) == BigDecimal("0.01000"))
      assert(decodeAsciiBigNumber("1000".getBytes, isUnsigned = false, 0, -1) == BigDecimal("0.01000"))

      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 1, 1) == BigDecimal("10000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 3, 2) == BigDecimal("100000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 4, 3) == BigDecimal("1000000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, 5, 4) == BigDecimal("10000000"))
    }
  }

  "decodeEbcdicBigDecimal()" should {
    "work" in {

    }
  }

  "decodeAsciiBigDecimal()" should {
    "decode parsable decimals" in {
      assert(decodeAsciiBigDecimal("+1000".getBytes, isUnsigned = false) == BigDecimal("1000"))
      assert(decodeAsciiBigDecimal("1000,25+".getBytes, isUnsigned = false) == BigDecimal("1000.25"))
      assert(decodeAsciiBigDecimal("-1000".getBytes, isUnsigned = false) == BigDecimal("-1000"))
      assert(decodeAsciiBigDecimal("1000,25-".getBytes, isUnsigned = false) == BigDecimal("-1000.25"))
      assert(decodeAsciiBigDecimal("12345678901234567890123456".getBytes, isUnsigned = true) == BigDecimal("12345678901234567890123456"))
      assert(decodeAsciiBigDecimal("12345678901234567890123456.12345678901234567890123456".getBytes, isUnsigned = true) == BigDecimal("12345678901234567890123456.12345678901234567890123456"))
    }

    "thrown an exceotion on numbers in scientific format" in {
      intercept[NumberFormatException] {
        decodeAsciiBigDecimal("200E+10".getBytes, isUnsigned = false) == BigDecimal("200+E10")
      }
    }
  }

}
