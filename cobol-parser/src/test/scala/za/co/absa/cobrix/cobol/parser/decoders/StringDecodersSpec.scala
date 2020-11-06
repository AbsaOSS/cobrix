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
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePageCommon

class StringDecodersSpec extends WordSpec {

  import StringDecoders._
  import za.co.absa.cobrix.cobol.testutils.EbcdicEncoder._

  private val asciiString = "AbCdEfGhIjKlMnOpQrStUvWxYz 0123456789 !@#$%^&*()[]{};'\\\"/.,"
  private val unicodeString = "AbCdEfGhIjKlMnOpQrStUvWxYz ěščřžýáíé 0123456789 !@#$%^&*()[]{};'\\\"/.,"
  private val ebcdicBytes = Array[Byte](
    0xc1.toByte, 0x82.toByte, 0xc3.toByte, 0x84.toByte, 0xc5.toByte, 0x86.toByte, 0xc7.toByte, 0x88.toByte,
    0xc9.toByte, 0x91.toByte, 0xd2.toByte, 0x93.toByte, 0xd4.toByte, 0x95.toByte, 0xd6.toByte, 0x97.toByte,
    0xd8.toByte, 0x99.toByte, 0xe2.toByte, 0xa3.toByte, 0xe4.toByte, 0xa5.toByte, 0xe6.toByte, 0xa7.toByte,
    0xe8.toByte, 0xa9.toByte, 0x0.toByte, 0xf0.toByte, 0xf1.toByte, 0xf2.toByte, 0xf3.toByte, 0xf4.toByte,
    0xf5.toByte, 0xf6.toByte, 0xf7.toByte, 0xf8.toByte, 0xf9.toByte, 0x0.toByte, 0x5a.toByte, 0x7c.toByte,
    0x7b.toByte, 0x5b.toByte, 0x6c.toByte, 0xb0.toByte, 0x50.toByte, 0x5c.toByte, 0x4d.toByte, 0x5d.toByte,
    0xba.toByte, 0xbb.toByte, 0xc0.toByte, 0xd0.toByte, 0x5e.toByte, 0x7d.toByte, 0xe0.toByte, 0x7f.toByte,
    0x61.toByte, 0x4b.toByte, 0x6b.toByte
  )

  private val codePage = new CodePageCommon

  "decodeEbcdicString()" should {
    "decode an EBCDIC string" in {
      val actual = decodeEbcdicString(ebcdicBytes, TrimNone, codePage.getEbcdicToAsciiMapping)

      assert(actual == asciiString)
    }

    "decode an EBCDIC string with left trimming" in {
      val bytes = toEbcdic(asciiString + "  \t ")
      val actual = decodeEbcdicString(bytes, TrimRight, codePage.getEbcdicToAsciiMapping)

      assert(actual == asciiString)
    }

    "decode an EBCDIC string with right trimming" in {
      val bytes = toEbcdic("  \t " + asciiString)
      val actual = decodeEbcdicString(bytes, TrimLeft, codePage.getEbcdicToAsciiMapping)

      assert(actual == asciiString)
    }

    "decode an EBCDIC string with left+right trimming" in {
      val bytes = toEbcdic("  \t " + asciiString + "  \t ")
      val actual = decodeEbcdicString(bytes, TrimBoth, codePage.getEbcdicToAsciiMapping)

      assert(actual == asciiString)
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
    "decode ebcdic strings as parsable strings" in {
      assert(decodeEbcdicNumber(toEbcdic("1"), isUnsigned = true) == "1")
      assert(decodeEbcdicNumber(toEbcdic("1"), isUnsigned = false) == "1")
      assert(decodeEbcdicNumber(toEbcdic(" 1 "), isUnsigned = true) == "1")
      assert(decodeEbcdicNumber(toEbcdic(" 1 "), isUnsigned = false) == "1")
      assert(decodeEbcdicNumber(toEbcdic("-1"), isUnsigned = false) == "-1")

      assert(decodeEbcdicNumber(toEbcdic(" 18938717862,00 "), isUnsigned = true) == "18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" 18938717862.00 "), isUnsigned = true) == "18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" + 18938717862.00 "), isUnsigned = true) == "+18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" - 18938717862.00 "), isUnsigned = false) == "-18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" + 18938717862.00 "), isUnsigned = false) == "+18938717862.00")
    }

    "return null if negative number encountered while parsing unsigned numbers" in {
      assert(decodeEbcdicNumber(toEbcdic("-1"), isUnsigned = true) == null)
      assert(decodeEbcdicNumber(toEbcdic(" - 18938717862.00 "), isUnsigned = true) == null)
    }

    "re-position leading and trailing sign" in {
      assert(decodeEbcdicNumber(toEbcdic("+100,00"), isUnsigned = false) == "+100.00")
      assert(decodeEbcdicNumber(toEbcdic("100.00+"), isUnsigned = false) == "+100.00")
      assert(decodeEbcdicNumber(toEbcdic("-100.00"), isUnsigned = false) == "-100.00")
      assert(decodeEbcdicNumber(toEbcdic("100,00-"), isUnsigned = false) == "-100.00")
    }

    "decode sign punched numbers" in {
      assert(decodeEbcdicNumber(toEbcdic("A00,00"), isUnsigned = false) == "+100.00")
      assert(decodeEbcdicNumber(toEbcdic("J00,00"), isUnsigned = false) == "-100.00")
      assert(decodeEbcdicNumber(toEbcdic("B02"), isUnsigned = false) == "+202")
      assert(decodeEbcdicNumber(toEbcdic("K02"), isUnsigned = false) == "-202")
      assert(decodeEbcdicNumber(toEbcdic("30C"), isUnsigned = false) == "+303")
      assert(decodeEbcdicNumber(toEbcdic("30L"), isUnsigned = false) == "-303")
      assert(decodeEbcdicNumber(toEbcdic("40D"), isUnsigned = false) == "+404")
      assert(decodeEbcdicNumber(toEbcdic("40M"), isUnsigned = false) == "-404")
      assert(decodeEbcdicNumber(toEbcdic("E05"), isUnsigned = false) == "+505")
      assert(decodeEbcdicNumber(toEbcdic("N05"), isUnsigned = false) == "-505")
      assert(decodeEbcdicNumber(toEbcdic("F06"), isUnsigned = false) == "+606")
      assert(decodeEbcdicNumber(toEbcdic("O06"), isUnsigned = false) == "-606")
      assert(decodeEbcdicNumber(toEbcdic("G07"), isUnsigned = false) == "+707")
      assert(decodeEbcdicNumber(toEbcdic("P07"), isUnsigned = false) == "-707")
      assert(decodeEbcdicNumber(toEbcdic("H08"), isUnsigned = false) == "+808")
      assert(decodeEbcdicNumber(toEbcdic("Q08"), isUnsigned = false) == "-808")
      assert(decodeEbcdicNumber(toEbcdic("I09"), isUnsigned = false) == "+909")
      assert(decodeEbcdicNumber(toEbcdic("R09"), isUnsigned = false) == "-909")
      assert(decodeEbcdicNumber(toEbcdic("90{"), isUnsigned = false) == "+900")
      assert(decodeEbcdicNumber(toEbcdic("90}"), isUnsigned = false) == "-900")
    }

    "return null if a number is malformed" in {
      assert(decodeEbcdicNumber(toEbcdic("AAABBBCCC"), isUnsigned = false) == null)
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
    "decode parsable ints" in {
      assert(decodeEbcdicInt(toEbcdic("+100"), isUnsigned = false) == 100)
      assert(decodeEbcdicInt(toEbcdic("100+"), isUnsigned = false) == 100)
      assert(decodeEbcdicInt(toEbcdic("-100"), isUnsigned = false) == -100)
      assert(decodeEbcdicInt(toEbcdic("100-"), isUnsigned = false) == -100)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeEbcdicInt(toEbcdic("+100"), isUnsigned = true) == 100)
      assert(decodeEbcdicInt(toEbcdic("100+"), isUnsigned = true) == 100)
      assert(decodeEbcdicInt(toEbcdic("-100"), isUnsigned = true) == null)
      assert(decodeEbcdicInt(toEbcdic("100-"), isUnsigned = true) == null)
    }

    "return null on non-ints ints" in {
      assert(decodeEbcdicInt(toEbcdic("+100,0"), isUnsigned = true) == null)
      assert(decodeEbcdicInt(toEbcdic("100.00+"), isUnsigned = false) == null)
      assert(decodeEbcdicInt(toEbcdic("-100,000"), isUnsigned = false) == null)
      assert(decodeEbcdicInt(toEbcdic("100.000-"), isUnsigned = false) == null)
    }

    "return null on unparsable ints" in {
      assert(decodeEbcdicInt(toEbcdic("+1000000000000"), isUnsigned = true) == null)
      assert(decodeEbcdicInt(toEbcdic("AAA"), isUnsigned = false) == null)
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
    "decode parsable longs" in {
      assert(decodeEbcdicLong(toEbcdic("+1000000000000000"), isUnsigned = false) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000+"), isUnsigned = false) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("-1000000000000000"), isUnsigned = false) == -1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000-"), isUnsigned = false) == -1000000000000000L)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeEbcdicLong(toEbcdic("+1000000000000000"), isUnsigned = true) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000+"), isUnsigned = true) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("-1000000000000000"), isUnsigned = true) == null)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000-"), isUnsigned = true) == null)
    }

    "return null on non-ints longs" in {
      assert(decodeEbcdicLong(toEbcdic("+1000000000000000,0"), isUnsigned = true) == null)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000.00+"), isUnsigned = false) == null)
      assert(decodeEbcdicLong(toEbcdic("-1000000000000000,000"), isUnsigned = false) == null)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000.000-"), isUnsigned = false) == null)
    }

    "return null on unparsable longs" in {
      assert(decodeEbcdicLong(toEbcdic("AAA"), isUnsigned = false) == null)
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
    "decode parsable decimals" in {
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 0, 0) == BigDecimal("1000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 1, 0) == BigDecimal("100.0"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 3, 0) == BigDecimal("1.000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 4, 0) == BigDecimal("0.1000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 5, 0) == null)
    }

    "decode numbers with scale factor" in {
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 0, -1) == BigDecimal("0.01000"))
      assert(decodeEbcdicBigNumber(toEbcdic("1000"), isUnsigned = false, 0, -1) == BigDecimal("0.01000"))

      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 1, 1) == BigDecimal("10000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 3, 2) == BigDecimal("100000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 4, 3) == BigDecimal("1000000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, 5, 4) == BigDecimal("10000000"))
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
    "decode parsable decimals" in {
      assert(decodeEbcdicBigDecimal(toEbcdic("+1000"), isUnsigned = false) == BigDecimal("1000"))
      assert(decodeEbcdicBigDecimal(toEbcdic("1000,25+"), isUnsigned = false) == BigDecimal("1000.25"))
      assert(decodeEbcdicBigDecimal(toEbcdic("-1000"), isUnsigned = false) == BigDecimal("-1000"))
      assert(decodeEbcdicBigDecimal(toEbcdic("1000,25-"), isUnsigned = false) == BigDecimal("-1000.25"))
      assert(decodeEbcdicBigDecimal(toEbcdic("12345678901234567890123456"), isUnsigned = true) == BigDecimal("12345678901234567890123456"))
      assert(decodeEbcdicBigDecimal(toEbcdic("12345678901234567890123456.12345678901234567890123456"), isUnsigned = true) == BigDecimal("12345678901234567890123456.12345678901234567890123456"))
    }

    "return null for numbers in scientific format" in {
      assert(decodeEbcdicBigDecimal(toEbcdic("200E+10"), isUnsigned = false) == null)
    }

    "return null for malformed numbers" in {
      assert(decodeEbcdicBigDecimal(toEbcdic("ABC"), isUnsigned = false) == null)
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

    "decode numbers in scientific format" in {
      assert(decodeAsciiBigDecimal("200E+10".getBytes, isUnsigned = false) == 2.00E+12)
    }

    "return null for malformed numbers" in {
      assert(decodeAsciiBigDecimal("ABC".getBytes(), isUnsigned = false) == null)
    }
  }

}
