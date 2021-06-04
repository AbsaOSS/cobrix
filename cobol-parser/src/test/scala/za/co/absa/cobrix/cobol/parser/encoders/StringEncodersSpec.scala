package za.co.absa.cobrix.cobol.parser.encoders

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.decoders.StringDecoders
import za.co.absa.cobrix.cobol.parser.decoders.StringDecoders.TrimNone
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePageCommon

class StringEncodersSpec extends WordSpec {
  "encodeEbcdicString" should {
    "be able to covert a basic ASCII string to EBCDIC" in {
      val input = "0123456789 abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+-=<>[](){},./;:?!|$*~^`#@%_\\\'\"\r\n"

      val codePage = new CodePageCommon

      val ebcdic = StringEncoders.encodeEbcdicString(input, codePage.getAsciiToEbcdicMapping, input.length)
      val ascii = StringDecoders.decodeEbcdicString(ebcdic, TrimNone, codePage.getEbcdicToAsciiMapping)

      assert(ascii == input)
    }

    "be able to covert shorter strings" in {
      val input = "0123456789"
      val expected = "01234"

      val codePage = new CodePageCommon

      val ebcdic = StringEncoders.encodeEbcdicString(input, codePage.getAsciiToEbcdicMapping, 5)
      val ascii = StringDecoders.decodeEbcdicString(ebcdic, TrimNone, codePage.getEbcdicToAsciiMapping)

      assert(ebcdic.length == 5)
      assert(ascii == expected)
    }

    "be able to covert longer strings" in {
      val input = "012"
      val expected = "F0F1F20000"

      val codePage = new CodePageCommon

      val ebcdic = StringEncoders.encodeEbcdicString(input, codePage.getAsciiToEbcdicMapping, 5)
      val ebcdicHex = StringDecoders.decodeHex(ebcdic)

      assert(ebcdic.length == 5)
      assert(expected == ebcdicHex)
    }

    "be able to covert an empty string" in {
      val codePage = new CodePageCommon

      val ebcdic = StringEncoders.encodeEbcdicString("", codePage.getAsciiToEbcdicMapping, 0)

      assert(ebcdic.length == 0)
    }

    "throws an exception if a negative value was passed" in {
      val codePage = new CodePageCommon

      val ex = intercept[IllegalArgumentException] {
        StringEncoders.encodeEbcdicString("123", codePage.getAsciiToEbcdicMapping, -1)
      }

      assert(ex.getMessage.contains("requirement failed: Field length cannot be negative, got -1"))
    }
  }

}
