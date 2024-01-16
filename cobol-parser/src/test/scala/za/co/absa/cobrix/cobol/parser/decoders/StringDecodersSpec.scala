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
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.encoding.codepage._

class StringDecodersSpec extends AnyWordSpec {

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
    "be able to decode empty strings" in {
      val actual = decodeEbcdicString(Array[Byte](0, 0, 0, 0), TrimBoth, codePage, improvedNullDetection = false)

      assert(actual == "")
    }

    "be able to decode nulls" in {
      val actual = decodeEbcdicString(Array[Byte](0, 0, 0, 0), TrimNone, codePage, improvedNullDetection = true)

      assert(actual == null)
    }

    "decode an EBCDIC string" in {
      val actual = decodeEbcdicString(ebcdicBytes, TrimNone, codePage, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "decode an EBCDIC string with left trimming" in {
      val bytes = toEbcdic(asciiString + "  \t ")
      val actual = decodeEbcdicString(bytes, TrimRight, codePage, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "decode an EBCDIC string with right trimming" in {
      val bytes = toEbcdic("  \t " + asciiString)
      val actual = decodeEbcdicString(bytes, TrimLeft, codePage, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "decode an EBCDIC string with left+right trimming" in {
      val bytes = toEbcdic("  \t " + asciiString + "  \t ")
      val actual = decodeEbcdicString(bytes, TrimBoth, codePage, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "EBCDIC with code pages" should {
      "decode a CP273 string special characters" in {
        val expected = " {Ä!~Ü^[ö§¤ß¢@ä¦ü}Ö\\] "
        val bytes = Array(0x40, 0x43, 0x4A, 0x4F, 0x59, 0x5A, 0x5F, 0x63, 0x6A, 0x7C, 0x9F,
          0xA1, 0xB0, 0xB5, 0xC0, 0xCC, 0xD0, 0xDC, 0xE0, 0xEC, 0xFC, 0x40).map(_.toByte)

        val actual = decodeEbcdicString(bytes, KeepAll, new CodePage273, improvedNullDetection = false)

        assert(actual == expected)
      }

      "decode a CP273 string example" in {
        val expected = "Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich"

        val bytes = Array(0xE5, 0x89, 0x83, 0xA3, 0x96, 0x99, 0x40, 0x91, 0x81, 0x87, 0xA3, 0x40, 0xA9, 0xA6,
          0x6A, 0x93, 0x86, 0x40, 0xC2, 0x96, 0xA7, 0x92, 0xC0, 0x94, 0x97, 0x86, 0x85, 0x99, 0x40, 0x98,
          0xA4, 0x85, 0x99, 0x40, 0xD0, 0x82, 0x85, 0x99, 0x40, 0x84, 0x85, 0x95, 0x40, 0x87, 0x99, 0x96,
          0xA1, 0x85, 0x95, 0x40, 0xE2, 0xA8, 0x93, 0xA3, 0x85, 0x99, 0x40, 0xC4, 0x85, 0x89, 0x83, 0x88).map(_.toByte)

        val actual = decodeEbcdicString(bytes, KeepAll, new CodePage273, improvedNullDetection = false)

        assert(actual == expected)
      }

      "decode a CP500 string special characters" in {
        val expected = "âäàáãåçñ[.<(+!&éêëèíîïìß]$*);^-/ÂÄÀÁÃÅÇÑ¦,%_>?øÉÊËÈÍÎÏÌ`:#@'=\"Øabcdefghi«»ðýþ±°jklmnopqrªºæ¸Æ¤µ~stuvwxyz¡¿ÐÝÞ®¢£¥·©§¶¼½¾¬|¯¨´×{ABCDEFGHI\u00ADôöòóõ}JKLMNOPQR¹ûüùúÿ\\÷STUVWXYZ²ÔÖÒÓÕ0123456789³ÛÜÙÚ"
        val bytes = Array(
                      0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
          0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
          0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
          0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
          0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
          0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
          0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
          0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
          0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
          0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
          0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
          0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE
        ).map(_.toByte)

        val actual = decodeEbcdicString(bytes, KeepAll, new CodePage500, improvedNullDetection = false)

        println(actual)

        assert(actual == expected)
      }

      "decode a CP1140 string special characters" in {
        val expected = "âäàáãåçñ¢.<(+|&éêëèíîïìß!$*);¬-/ÂÄÀÁÃÅÇÑ¦,%_>?øÉÊËÈÍÎÏÌ`:#@'=\"Øabcdefghi«»ðýþ±°jklmnopqrªºæ¸Æ€µ~stuvwxyz¡¿ÐÝÞ®^£¥·©§¶¼½¾[]¯¨´×{ABCDEFGHI\u00ADôöòóõ}JKLMNOPQR¹ûüùúÿ\\÷STUVWXYZ²ÔÖÒÓÕ0123456789³ÛÜÙÚ"
        val bytes = Array(
                      0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
          0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
          0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
          0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
          0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
          0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
          0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
          0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
          0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
          0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
          0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
          0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE
        ).map(_.toByte)

        val actual = decodeEbcdicString(bytes, KeepAll, new CodePage1140, improvedNullDetection = false)

        assert(actual == expected)
      }

      "decode a CP1141 string special characters" in {
        val expected = "â{àáãåçñÄ.<(+!&éêëèíîïì~Ü$*);^-/Â[ÀÁÃÅÇÑö,%_>?øÉÊËÈÍÎÏÌ`:#§'=\"Øabcdefghi«»ðýþ±°jklmnopqrªºæ¸Æ€µßstuvwxyz¡¿ÐÝÞ®¢£¥·©@¶¼½¾¬|¯¨´×äABCDEFGHI\u00ADô¦òóõüJKLMNOPQR¹û}ùúÿÖ÷STUVWXYZ²Ô\\ÒÓÕ0123456789³Û]ÙÚ"
        val bytes = Array(
                      0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
          0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
          0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
          0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
          0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
          0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
          0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
          0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
          0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
          0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
          0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
          0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE
        ).map(_.toByte)

        val actual = decodeEbcdicString(bytes, KeepAll, new CodePage1141, improvedNullDetection = false)

        assert(actual == expected)
      }

      "decode a CP1148 string special characters" in {
        val expected = "âäàáãåçñ[.<(+!&éêëèíîïìß]$*);^-/ÂÄÀÁÃÅÇÑ¦,%_>?øÉÊËÈÍÎÏÌ`:#@'=\"Øabcdefghi«»ðýþ±°jklmnopqrªºæ¸Æ€µ~stuvwxyz¡¿ÐÝÞ®¢£¥·©§¶¼½¾¬|¯¨´×{ABCDEFGHI\u00ADôöòóõ}JKLMNOPQR¹ûüùúÿ\\÷STUVWXYZ²ÔÖÒÓÕ0123456789³ÛÜÙÚ"
        val bytes = Array(
                      0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
          0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
          0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
          0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
          0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
          0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
          0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
          0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
          0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
          0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
          0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
          0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE
        ).map(_.toByte)

        val actual = decodeEbcdicString(bytes, KeepAll, new CodePage1148, improvedNullDetection = false)

        assert(actual == expected)
      }
    }
  }

  "decodeAsciiString()" should {
    "decode an ASCII string" in {
      val bytes = asciiString.getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimNone, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "decode an ASCII string with left trimming" in {
      val bytes = (asciiString + "  \t ").getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimRight, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "decode an ASCII string with right trimming" in {
      val bytes = ("  \t " + asciiString).getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimLeft, improvedNullDetection = false)

      assert(actual == asciiString)
    }

    "decode an ASCII string with left+right trimming" in {
      val bytes = ("  \t " + asciiString + "  \t ").getBytes(StandardCharsets.US_ASCII)
      val actual = decodeAsciiString(bytes, TrimBoth, improvedNullDetection = false)

      assert(actual == asciiString)
    }
  }

  "decodeUtf16String()" should {
    "decode an UTF-16 BE string" in {
      val bytes = unicodeString.getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimNone, isUtf16BigEndian = true, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string" in {
      val bytes = unicodeString.getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimNone, isUtf16BigEndian = false, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 BE string with left trimming" in {
      val bytes = (unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimRight, isUtf16BigEndian = true, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string with left trimming" in {
      val bytes = (unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimRight, isUtf16BigEndian = false, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 BE string with right trimming" in {
      val bytes = ("  \t " + unicodeString).getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimLeft, isUtf16BigEndian = true, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string with right trimming" in {
      val bytes = ("  \t " + unicodeString).getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimLeft, isUtf16BigEndian = false, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 BE string with left+right trimming" in {
      val bytes = ("  \t " + unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16BE)
      val actual = decodeUtf16String(bytes, TrimBoth, isUtf16BigEndian = true, improvedNullDetection = false)

      assert(actual == unicodeString)
    }

    "decode an UTF-16 LE string with left+right trimming" in {
      val bytes = ("  \t " + unicodeString + "  \t ").getBytes(StandardCharsets.UTF_16LE)
      val actual = decodeUtf16String(bytes, TrimBoth, isUtf16BigEndian = false, improvedNullDetection = false)

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
      assert(decodeEbcdicNumber(toEbcdic("1"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "1")
      assert(decodeEbcdicNumber(toEbcdic("1"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "1")
      assert(decodeEbcdicNumber(toEbcdic(" 1 "), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "1")
      assert(decodeEbcdicNumber(toEbcdic(" 1 "), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "1")
      assert(decodeEbcdicNumber(toEbcdic("-1"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-1")

      assert(decodeEbcdicNumber(toEbcdic(" 18938717862,00 "), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" 18938717862.00 "), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" + 18938717862.00 "), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "+18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" - 18938717862.00 "), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-18938717862.00")
      assert(decodeEbcdicNumber(toEbcdic(" + 18938717862.00 "), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+18938717862.00")
    }

    "return null if negative number encountered while parsing unsigned numbers" in {
      assert(decodeEbcdicNumber(toEbcdic("-1"), isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic("-1"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic(" - 18938717862.00 "), isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic(" - 18938717862.00 "), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "re-position leading and trailing sign" in {
      assert(decodeEbcdicNumber(toEbcdic("+100,00"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100.00")
      assert(decodeEbcdicNumber(toEbcdic("100.00+"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100.00")
      assert(decodeEbcdicNumber(toEbcdic("-100.00"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100.00")
      assert(decodeEbcdicNumber(toEbcdic("100,00-"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100.00")
    }

    "decode sign punched numbers" in {
      assert(decodeEbcdicNumber(toEbcdic("A00,00"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100.00")
      assert(decodeEbcdicNumber(toEbcdic("J00,00"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100.00")
      assert(decodeEbcdicNumber(toEbcdic("B02"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+202")
      assert(decodeEbcdicNumber(toEbcdic("K02"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-202")
      assert(decodeEbcdicNumber(toEbcdic("30C"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+303")
      assert(decodeEbcdicNumber(toEbcdic("30L"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-303")
      assert(decodeEbcdicNumber(toEbcdic("40D"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+404")
      assert(decodeEbcdicNumber(toEbcdic("40M"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-404")
      assert(decodeEbcdicNumber(toEbcdic("E05"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+505")
      assert(decodeEbcdicNumber(toEbcdic("N05"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-505")
      assert(decodeEbcdicNumber(toEbcdic("F06"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+606")
      assert(decodeEbcdicNumber(toEbcdic("O06"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-606")
      assert(decodeEbcdicNumber(toEbcdic("G07"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+707")
      assert(decodeEbcdicNumber(toEbcdic("P07"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-707")
      assert(decodeEbcdicNumber(toEbcdic("H08"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+808")
      assert(decodeEbcdicNumber(toEbcdic("Q08"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-808")
      assert(decodeEbcdicNumber(toEbcdic("I09"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+909")
      assert(decodeEbcdicNumber(toEbcdic("R09"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-909")
      assert(decodeEbcdicNumber(toEbcdic("90{"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+900")
      assert(decodeEbcdicNumber(toEbcdic("90}"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-900")
    }

    "return null if a number is malformed" in {
      assert(decodeEbcdicNumber(toEbcdic("AAABBBCCC"), isUnsigned = false, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic("{"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+0")
      assert(decodeEbcdicNumber(toEbcdic("}"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-0")
      assert(decodeEbcdicNumber(toEbcdic("{"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "+0")
      assert(decodeEbcdicNumber(toEbcdic("}"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic("{"), isUnsigned = false, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic("}"), isUnsigned = false, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic("{"), isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeEbcdicNumber(toEbcdic("}"), isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
    }
  }

  "decodeAsciiNumber()" should {
    "decode strings as parsable strings" in {
      assert(decodeAsciiNumber("1".getBytes, isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == "1")
      assert(decodeAsciiNumber("1".getBytes, isUnsigned = false, allowSignOverpunch = false, improvedNullDetection = false) == "1")
      assert(decodeAsciiNumber(" 1 ".getBytes, isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == "1")
      assert(decodeAsciiNumber(" 1 ".getBytes, isUnsigned = false, allowSignOverpunch = false, improvedNullDetection = false) == "1")
      assert(decodeAsciiNumber("-1".getBytes, isUnsigned = false, allowSignOverpunch = false, improvedNullDetection = false) == "-1")

      assert(decodeAsciiNumber(" 18938717862,00 ".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "18938717862.00")
      assert(decodeAsciiNumber(" 18938717862.00 ".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "18938717862.00")
      assert(decodeAsciiNumber(" + 18938717862.00 ".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == "+18938717862.00")
      assert(decodeAsciiNumber(" - 18938717862.00 ".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-18938717862.00")
      assert(decodeAsciiNumber(" + 18938717862.00 ".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+18938717862.00")
    }

    "return null if negative number encountered while parsing unsigned numbers" in {
      assert(decodeAsciiNumber("-1".getBytes, isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeAsciiNumber("-1".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiNumber(" - 18938717862.00 ".getBytes, isUnsigned = true, allowSignOverpunch = false, improvedNullDetection = false) == null)
      assert(decodeAsciiNumber(" - 18938717862.00 ".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "re-position leading and trailing sign" in {
      assert(decodeAsciiNumber("+100,00".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100.00")
      assert(decodeAsciiNumber("100.00+".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "+100.00")
      assert(decodeAsciiNumber("-100.00".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100.00")
      assert(decodeAsciiNumber("100,00-".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == "-100.00")
    }

    "return null if non-digit characters are encountered" in {
      assert(decodeAsciiNumber("AAABBBCCC".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }
  }

  "decodeEbcdicInt()" should {
    "decode parsable ints" in {
      assert(decodeEbcdicInt(toEbcdic("+100"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeEbcdicInt(toEbcdic("100+"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeEbcdicInt(toEbcdic("-100"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -100)
      assert(decodeEbcdicInt(toEbcdic("100-"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -100)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeEbcdicInt(toEbcdic("+100"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeEbcdicInt(toEbcdic("100+"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeEbcdicInt(toEbcdic("-100"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicInt(toEbcdic("100-"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on non-ints ints" in {
      assert(decodeEbcdicInt(toEbcdic("+100,0"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicInt(toEbcdic("100.00+"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicInt(toEbcdic("-100,000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicInt(toEbcdic("100.000-"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on unparsable ints" in {
      assert(decodeEbcdicInt(toEbcdic("+1000000000000"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicInt(toEbcdic("AAA"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }
  }

  "decodeAsciiInt()" should {
    "decode parsable ints" in {
      assert(decodeAsciiInt("+100".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeAsciiInt("100+".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeAsciiInt("-100".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -100)
      assert(decodeAsciiInt("100-".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -100)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeAsciiInt("+100".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeAsciiInt("100+".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 100)
      assert(decodeAsciiInt("-100".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiInt("100-".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on non-ints ints" in {
      assert(decodeAsciiInt("+100,0".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiInt("100.00+".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiInt("-100,000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiInt("100.000-".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on unparsable ints" in {
      assert(decodeAsciiInt("+1000000000000".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiInt("AAA".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }
  }

  "decodeEbcdicLong()" should {
    "decode parsable longs" in {
      assert(decodeEbcdicLong(toEbcdic("+1000000000000000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000+"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("-1000000000000000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000-"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -1000000000000000L)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeEbcdicLong(toEbcdic("+1000000000000000"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000+"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeEbcdicLong(toEbcdic("-1000000000000000"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000-"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on non-ints longs" in {
      assert(decodeEbcdicLong(toEbcdic("+1000000000000000,0"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000.00+"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicLong(toEbcdic("-1000000000000000,000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeEbcdicLong(toEbcdic("1000000000000000.000-"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on unparsable longs" in {
      assert(decodeEbcdicLong(toEbcdic("AAA"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

  }

  "decodeAsciiLong()" should {
    "decode parsable longs" in {
      assert(decodeAsciiLong("+1000000000000000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeAsciiLong("1000000000000000+".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeAsciiLong("-1000000000000000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -1000000000000000L)
      assert(decodeAsciiLong("1000000000000000-".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == -1000000000000000L)
    }

    "decode unsigned numbers with sign" in {
      assert(decodeAsciiLong("+1000000000000000".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeAsciiLong("1000000000000000+".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == 1000000000000000L)
      assert(decodeAsciiLong("-1000000000000000".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiLong("1000000000000000-".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on non-ints longs" in {
      assert(decodeAsciiLong("+1000000000000000,0".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiLong("1000000000000000.00+".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiLong("-1000000000000000,000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
      assert(decodeAsciiLong("1000000000000000.000-".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null on unparsable longs" in {
      assert(decodeAsciiLong("AAA".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }
  }

  "decodeEbcdicBigNumber()" should {
    "decode parsable decimals" in {
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0, 0) == BigDecimal("1000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 1, 0) == BigDecimal("100.0"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 3, 0) == BigDecimal("1.000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 4, 0) == BigDecimal("0.1000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 5, 0) == null)
    }

    "decode numbers with scale factor" in {
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0, -1) == BigDecimal("0.01000"))
      assert(decodeEbcdicBigNumber(toEbcdic("1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0, -1) == BigDecimal("0.01000"))

      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 1, 1) == BigDecimal("10000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 3, 2) == BigDecimal("100000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 4, 3) == BigDecimal("1000000"))
      assert(decodeEbcdicBigNumber(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 5, 4) == BigDecimal("10000000"))
    }
  }

  "decodeAsciiBigNumber()" should {
    "decode parsable decimals" in {
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0, 0) == BigDecimal("1000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 1, 0) == BigDecimal("100.0"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 3, 0) == BigDecimal("1.000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 4, 0) == BigDecimal("0.1000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 5, 0) == null)
    }

    "decode numbers with scale factor" in {
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0, -1) == BigDecimal("0.01000"))
      assert(decodeAsciiBigNumber("1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 0, -1) == BigDecimal("0.01000"))

      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 1, 1) == BigDecimal("10000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 3, 2) == BigDecimal("100000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 4, 3) == BigDecimal("1000000"))
      assert(decodeAsciiBigNumber("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false, 5, 4) == BigDecimal("10000000"))
    }
  }

  "decodeEbcdicBigDecimal()" should {
    "decode parsable decimals" in {
      assert(decodeEbcdicBigDecimal(toEbcdic("+1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("1000"))
      assert(decodeEbcdicBigDecimal(toEbcdic("1000,25+"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("1000.25"))
      assert(decodeEbcdicBigDecimal(toEbcdic("-1000"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("-1000"))
      assert(decodeEbcdicBigDecimal(toEbcdic("1000,25-"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("-1000.25"))
      assert(decodeEbcdicBigDecimal(toEbcdic("12345678901234567890123456"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("12345678901234567890123456"))
      assert(decodeEbcdicBigDecimal(toEbcdic("12345678901234567890123456.12345678901234567890123456"), isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("12345678901234567890123456.12345678901234567890123456"))
    }

    "return null for numbers in scientific format" in {
      assert(decodeEbcdicBigDecimal(toEbcdic("200E+10"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null for malformed numbers" in {
      assert(decodeEbcdicBigDecimal(toEbcdic("ABC"), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }
  }

  "decodeAsciiBigDecimal()" should {
    "decode parsable decimals" in {
      assert(decodeAsciiBigDecimal("+1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("1000"))
      assert(decodeAsciiBigDecimal("1000,25+".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("1000.25"))
      assert(decodeAsciiBigDecimal("-1000".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("-1000"))
      assert(decodeAsciiBigDecimal("1000,25-".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("-1000.25"))
      assert(decodeAsciiBigDecimal("12345678901234567890123456".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("12345678901234567890123456"))
      assert(decodeAsciiBigDecimal("12345678901234567890123456.12345678901234567890123456".getBytes, isUnsigned = true, allowSignOverpunch = true, improvedNullDetection = false) == BigDecimal("12345678901234567890123456.12345678901234567890123456"))
    }

    "not decode numbers in scientific format" in {
      assert(decodeAsciiBigDecimal("200E+10".getBytes, isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }

    "return null for malformed numbers" in {
      assert(decodeAsciiBigDecimal("ABC".getBytes(), isUnsigned = false, allowSignOverpunch = true, improvedNullDetection = false) == null)
    }
  }

}
