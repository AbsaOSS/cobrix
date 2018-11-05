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

package za.co.absa.cobrix.cobol.parser.common

import java.nio.ByteBuffer

import org.slf4j.LoggerFactory
import scodec._
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.position

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

/** Utilites for decoding Cobol binary data files **/
//noinspection RedundantBlock
object BinaryUtils {
  private val logger = LoggerFactory.getLogger(this.getClass)

  // Binary number format codecs (big endian and little endian)
  lazy val int8B: Codec[Int] = scodec.codecs.int8
  lazy val int8L: Codec[Int] = scodec.codecs.int8L
  lazy val int16B: Codec[Int] = scodec.codecs.int16
  lazy val int16L: Codec[Int] = scodec.codecs.int16L
  lazy val int32B: Codec[Int] = scodec.codecs.int32
  lazy val int32L: Codec[Int] = scodec.codecs.int32L
  lazy val int64B: Codec[Long] = scodec.codecs.int64
  lazy val int64L: Codec[Long] = scodec.codecs.int64L
  lazy val uint8B: Codec[Int] = scodec.codecs.uint8
  lazy val uint8L: Codec[Int] = scodec.codecs.uint8L
  lazy val uint16B: Codec[Int] = scodec.codecs.uint16
  lazy val uint16L: Codec[Int] = scodec.codecs.uint16L
  lazy val uint32B: Codec[Long] = scodec.codecs.uint32
  lazy val uint32L: Codec[Long] = scodec.codecs.uint32L
  lazy val floatB: Codec[Float] = scodec.codecs.float
  lazy val floatL: Codec[Float] = scodec.codecs.floatL
  lazy val doubleB: Codec[Double] = scodec.codecs.double
  lazy val doubleL: Codec[Double] = scodec.codecs.doubleL

  /** This is the EBCDIC to ASCII conversion table **/
  lazy val ebcdic2ascii: Array[Char] = {
    val clf = '\r'
    val ccr = '\n'
    val spc = ' '
    val qts = '\''
    val qtd = '\"'
    val bsh = '\\'
    Array[Char](
      spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, ccr, spc, spc, //   0 -  15
      spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  16 -  31
      spc, spc, spc, spc, spc, clf, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  32 -  47
      spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, spc, //  48 -  63
      ' ', ' ', spc, spc, spc, spc, spc, spc, spc, spc, '¢', '.', '<', '(', '+', '|', //  64 -  79
      '&', spc, spc, spc, spc, spc, spc, spc, spc, spc, '!', '$', '*', ')', ';', '¬', //  80 -  95
      '-', '/', spc, spc, spc, spc, spc, spc, spc, spc, '¦', ',', '%', '_', '>', '?', //  96 - 111
      spc, spc, spc, spc, spc, spc, spc, spc, spc, '`', ':', '#', '@', qts, '=', qtd, // 112 - 127
      spc, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', spc, spc, spc, spc, spc, '±', // 128 - 143
      spc, 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', spc, spc, spc, spc, spc, spc, // 144 - 159
      spc, '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', spc, spc, spc, spc, spc, spc, // 160 - 175
      '^', spc, spc, spc, spc, spc, spc, spc, spc, spc, '[', ']', spc, spc, spc, spc, // 176 - 191
      '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', '-', spc, spc, spc, spc, spc, // 192 - 207
      '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', spc, spc, spc, spc, spc, spc, // 208 - 223
      bsh, spc, 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', spc, spc, spc, spc, spc, spc, // 224 - 239
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', spc, spc, spc, spc, spc, spc) // 240 - 255
  }

  lazy val ascii2ebcdic: Array[Byte] = Array[Byte] (
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  //   0 -   7
    0x40.toByte, 0x40.toByte, 0x0d.toByte, 0x40.toByte, 0x40.toByte, 0x25.toByte, 0x40.toByte, 0x40.toByte,  //   8 -  15
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  //  16 -  23
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  //  24 -  31
    0x40.toByte, 0x5a.toByte, 0x7f.toByte, 0x7b.toByte, 0x5b.toByte, 0x6c.toByte, 0x50.toByte, 0x7d.toByte,  //  32 -  39
    0x4d.toByte, 0x5d.toByte, 0x5c.toByte, 0x4e.toByte, 0x6b.toByte, 0x60.toByte, 0x4b.toByte, 0x61.toByte,  //  40 -  47
    0xf0.toByte, 0xf1.toByte, 0xf2.toByte, 0xf3.toByte, 0xf4.toByte, 0xf5.toByte, 0xf6.toByte, 0xf7.toByte,  //  48 -  55
    0xf8.toByte, 0xf9.toByte, 0x7a.toByte, 0x5e.toByte, 0x4c.toByte, 0x7e.toByte, 0x6e.toByte, 0x6f.toByte,  //  56 -  63
    0x7c.toByte, 0xc1.toByte, 0xc2.toByte, 0xc3.toByte, 0xc4.toByte, 0xc5.toByte, 0xc6.toByte, 0xc7.toByte,  //  64 -  71
    0xc8.toByte, 0xc9.toByte, 0xd1.toByte, 0xd2.toByte, 0xd3.toByte, 0xd4.toByte, 0xd5.toByte, 0xd6.toByte,  //  72 -  79
    0xd7.toByte, 0xd8.toByte, 0xd9.toByte, 0xe2.toByte, 0xe3.toByte, 0xe4.toByte, 0xe5.toByte, 0xe6.toByte,  //  80 -  87
    0xe7.toByte, 0xe8.toByte, 0xe9.toByte, 0xba.toByte, 0xe0.toByte, 0xbb.toByte, 0xb0.toByte, 0x6d.toByte,  //  88 -  95
    0x79.toByte, 0x81.toByte, 0x82.toByte, 0x83.toByte, 0x84.toByte, 0x85.toByte, 0x86.toByte, 0x87.toByte,  //  96 - 103
    0x88.toByte, 0x89.toByte, 0x91.toByte, 0x92.toByte, 0x93.toByte, 0x94.toByte, 0x95.toByte, 0x96.toByte,  // 104 - 111
    0x97.toByte, 0x98.toByte, 0x99.toByte, 0xa2.toByte, 0xa3.toByte, 0xa4.toByte, 0xa5.toByte, 0xa6.toByte,  // 112 - 119
    0xa7.toByte, 0xa8.toByte, 0xa9.toByte, 0xc0.toByte, 0x4f.toByte, 0xd0.toByte, 0xa1.toByte, 0x40.toByte,  // 120 - 127
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 128 - 135
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 136 - 143
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 144 - 151
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 152 - 159
    0x40.toByte, 0x40.toByte, 0x4a.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x6a.toByte, 0x40.toByte,  // 160 - 167
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x5f.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 168 - 175
    0x40.toByte, 0x8f.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 176 - 183
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 184 - 191
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 192 - 199
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 200 - 207
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 208 - 215
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 216 - 223
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 224 - 231
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 232 - 239
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,  // 240 - 247
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte   // 248 - 255
  )

  /** Convert an EBCDIC character to ASCII */
  def ebcdicToAscii(byte: Byte): Char = ebcdic2ascii(byte)

  /** Convert an ASCII character to EBCDIC */
  def asciiToEbcdic(char: Char): Byte = ascii2ebcdic(char.toByte)

  def wordAlign(f: BitVector, wordSize: Int, align: position.Position): BitVector = {
    require(f.size <= wordSize)
    align match {
      case position.Left if f.size != wordSize => f.padLeft(wordSize - f.size)
      case position.Right if f.size != wordSize => f.padRight(wordSize - f.size)
      case _ => f
    }
  }

  /** Get the bit count of a cobol data type
    *
    * @param codec     EBCDIC / ASCII
    * @param comp      A type of compact stirage
    * @param precision The precision (the number of digits) of the type
    * @return
    */
  def getBitCount(codec: Codec[_ <: AnyVal], comp: Option[Int], precision: Int): Int = {
    comp match {
      case Some(value) =>
        value match {
          case compact if compact == 3 =>
            (precision + 1) * codec.sizeBound.lowerBound.toInt //bcd
          case _ => codec.sizeBound.lowerBound.toInt // bin/float/floatL
        }
      case None => precision * codec.sizeBound.lowerBound.toInt
    }
  }

  /** Decode the bits that are located in a binary file to actual human readable information
    *
    * @param codec        scodec codec
    * @param enc          encoding type
    * @param scale        size of data stucture
    * @param bits         bits that need to be decoded
    * @param comp         compaction of the bits
    * @param align        bits alignment
    * @param signPosition sign position of a signed data type
    * @return
    */
  def decode(codec: Codec[_ <: AnyVal], enc: Encoding, scale: Int, bits: BitVector, comp: Option[Int], align: Option[position.Position] = None,
             signPosition: Option[position.Position]): Array[Byte] = {
    val digitBitSize = codec.sizeBound.lowerBound.toInt
    val bytes = enc match {
      case _: ASCII => comp match {
        case Some(compact) => compact match {
          case a if a == 3 => { //bcd
            val bte = for (x <- 0 until scale) yield {
              val bts = wordAlign(bits.slice(x * digitBitSize, (x * digitBitSize) + digitBitSize), digitBitSize, align.getOrElse(position.Left))
              Codec.decode(bts)(codec).require.value.asInstanceOf[Double].toByte
            }
            bte.toArray
          }
          case _ => { //bin
            //            val bts = wordAlign(bits, digitBitSize, align.getOrElse(Left))
            val bte = Codec.decode(bits)(codec).require.value.asInstanceOf[Double].toByte
            (bte :: Nil).toArray
          }
        }
        case None => { // display i.e. no comp
          val bte = for (x <- 0 until scale) yield {
            val bts = wordAlign(bits.slice(x * digitBitSize, (x * digitBitSize) + digitBitSize), digitBitSize, align.getOrElse(position.Left))
            Codec.decode(bts)(codec).require.value.asInstanceOf[Double].toByte
          }
          bte.toArray
        }
      }
      case _: EBCDIC => comp match {
        case Some(compact) => compact match {
          case a if a == 3 => { //bcd
            logger.debug("BCD, bits : " + bits)
            val bte = for (x <- 0 to scale) yield {
              val bts = wordAlign(bits.slice(x * digitBitSize, (x * digitBitSize) + digitBitSize), digitBitSize, align.getOrElse(position.Left))
              logger.debug("bts : " + bts.toBin)
              logger.debug("codec : " + codec)
              logger.debug("value : " + Codec.decode(bts)(codec).require.value.asInstanceOf[Number].doubleValue())
              Codec.decode(bts)(codec).require.value.asInstanceOf[Number].doubleValue().toByte
            }
            bte.toArray
          }
          case _ => { //bin
            //            val bts = wordAlign(bits, digitBitSize, align.getOrElse(Left))
            logger.debug("bts : " + bits.toBin)
            logger.debug("codec : " + codec)
            val buf = ByteBuffer.allocate(8)
            logger.debug("codec : " + codec.toString)
            val decValue = Codec.decode(bits)(codec).require.value.asInstanceOf[Number].doubleValue()
            logger.debug("decValue : " + decValue)
            val byteArr = buf.putDouble(decValue).array()
            logger.debug("byteArr : " + byteArr)
            byteArr
          }
        }
        case None => { // display i.e. no comp
          val bte = for (x <- 0 until scale) yield {
            val bts = wordAlign(bits.slice(x * digitBitSize, (x * digitBitSize) + digitBitSize), digitBitSize, align.getOrElse(position.Left))
            logger.debug("bts : " + bts.toBin)
            Codec.decode(bts)(codec).require.value.asInstanceOf[Number].doubleValue().toByte
          }
          bte.toArray
        }
      }
    }
    bytes
  }

  /** decode an array of bytes to actual characters that represent their binary counterparts.
    *
    * @param byteArr byte array that represents the binary data
    * @param enc     encoding type
    * @param comp    binary compaction type
    * @return a string representation of the binary data
    */
  def charDecode(byteArr: Array[Byte], enc: Option[Encoding], comp: Option[Int]): String = {
    val ans = enc match {
      case Some(ASCII()) => byteArr.map(byte => {
        byte.toInt
      })
      case Some(EBCDIC()) =>
        val finalStringVal = comp match {
          case Some(compact) => {
            val compValue = compact match {
              case a if a == 3 => { //bcd
                val digitString = for {
                  idx <- byteArr.indices
                } yield {
                  if (idx == byteArr.length - 1) { //last byte is sign
                    byteArr(idx) match {
                      case 0x0C => " " // was +
                      case 0x0D => "-"
                      case 0x0F => " " // was +, unsigned
                      case _ =>
                        // Todo Remove this
                        println(s"Unknown singature nybble encountered! ${byteArr(idx).toString}")
                        byteArr(idx).toString // No sign
                    }
                  }
                  else {
                    byteArr(idx).toString
                  }
                }
                logger.debug("digitString : " + digitString)
                s"${digitString.last}${digitString.head}${digitString.tail.dropRight(1).mkString("")}"
              }
              case _ => { //bin
                val buf = ByteBuffer.wrap(byteArr)
                // Todo Why Double??? Revis the logic
                buf.getDouble.toString //returns number value as a string "1500"
              }
            }
            compValue
          }
          case None => {
            val digitString = for {
              idx <- byteArr.indices
            } yield {
              val unsignedByte = (256 + byteArr(idx).toInt) % 256
              ebcdic2ascii(unsignedByte)
            }
            digitString.mkString("")
          }
        }
        finalStringVal

      case _ => throw new Exception("No character set was defined for decoding")
    }
    ans.toString
  }

  /** A decoder for any string fields (alphabetical or any char)
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeString(enc: Encoding, bytes: Array[Byte], length: Int): String = {
    val str = enc match {
      case _: EBCDIC => {
        var i = 0
        val buf = new StringBuffer(length)
        while (i < bytes.length && i < length) {
          buf.append(ebcdic2ascii((bytes(i) + 256) % 256))
          i = i + 1
        }
        buf.toString
      }
      case _ => {
        var i = 0
        val buf = new StringBuffer(length)
        while (i < bytes.length && i < length) {
          buf.append(bytes(i).toChar)
          i = i + 1
        }
        buf.toString
      }
    }
    str.trim
  }

  /** A decoder for various numeric formats
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeCobolNumber(enc: Encoding, bytes: Array[Byte], compact: Option[Int], precision: Int, scale: Int, explicitDecimal: Boolean, signed: Boolean, isSignSeparate: Boolean): Option[String] = {
    compact match {
      case None =>
        // DISPLAY format
        decodeUncompressedNumber(enc, bytes, explicitDecimal, scale, isSignSeparate)
      case Some(1) =>
        // COMP-1 aka 32-bit floating point number
        Some(decodeFloatingPointNumber(bytes, bigEndian = true))
      case Some(2) =>
        // COMP-2 aka 64-bit floating point number
        Some(decodeFloatingPointNumber(bytes, bigEndian = true))
      case Some(3) =>
        // COMP-3 aka BCD-encoded number
        decodeSignedBCD(bytes, scale)
      case Some(4) =>
        // COMP aka BINARY encoded number
        Some(decodeBinaryNumber(bytes, bigEndian = true, signed = signed, scale))
      case Some(5) =>
        // COMP aka BINARY encoded number
        Some(decodeBinaryNumber(bytes, bigEndian = false, signed = signed, scale))
      case _ =>
        throw new IllegalStateException(s"Unknown compression format ($compact).")
    }
  }
  /** A decoder for uncompressed (aka DISPLAY) binary numbers
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeUncompressedNumber(enc: Encoding, bytes: Array[Byte], explicitDecimal: Boolean, scale: Int, isSignSeparate: Boolean): Option[String] = {
    val chars: ListBuffer[Char] = new ListBuffer[Char]()
    val extendedScale = if (isSignSeparate) scale + 1 else scale
    val decimalPointPosition = bytes.length - extendedScale
    var i = 0
    while (i < bytes.length) {
      if (i == decimalPointPosition && !explicitDecimal) {
        chars += '.'
      }
      enc match {
        case _: EBCDIC => chars += ebcdic2ascii((bytes(i) + 256) % 256)
        case _ => chars += bytes(i).toChar
      }

      i += 1
    }
    validateAndFormatNumber(chars.mkString)
  }

  /** Decode a binary encoded decimal (BCD) aka COMP-3 format to a String
    *
    * @param bytes A byte array that represents the binary data
    * @param scale A decimal scale if a number is a decimal. Should be greater or equal to zero
    * @return Some(str) - a string representation of the binary data, None if the data is not properly formatted
    */
  def decodeSignedBCD(bytes: Array[Byte], scale: Int = 0): Option[String] = {
    if (scale < 0) {
      throw new IllegalArgumentException(s"Invalid scele=$scale, should be greater or equal to zero.")
    }
    if (bytes.length < 1) {
      return Some("0")
    }
    val bits = BitVector(bytes)
    var i: Int = 0
    var sign = ""
    val chars: ListBuffer[Char] = new ListBuffer[Char]()
    val decimalPointPosition = bits.length - (scale + 1) * 4
    while (i < bits.length) {
      val nybble = bits.slice(i, i + 4).toByte(false)
      if (i >= bits.length - 4) {
        // The last nybble is a sign
        sign = nybble match {
          case 0x0C => "" // +, signed
          case 0x0D => "-"
          case 0x0F => "" // +, unsigned
          case _ =>
            // invalid nybble encountered - the format is wrong
            return None
        }
      } else {
        if (nybble >= 0 && nybble < 10) {
          if (i == decimalPointPosition) {
            chars += '.'
          }
          chars += ('0'.toByte + nybble).toChar
        }
        else {
          // invalid nybble encountered - the format is wrong
          return None
        }
      }
      i += 4
    }
    validateAndFormatNumber(sign + chars.mkString(""))
  }

  /** Transforms a string representation of an integer to a string representation of decimal
    * by adding a decimal point into the proper place
    *
    * @param intValue A number as an integer
    * @param scale    A scale - the number of digits to the right of decimal point separator
    * @return A string representation of decimal
    */
  private[cobol] def addDecimalPoint(intValue: String, scale: Int): String = {
    if (scale < 0) {
      throw new IllegalArgumentException(s"Invalid scele=$scale, should be greater or equal to zero.")
    }
    if (scale == 0) {
      intValue
    } else {
      val isNegative = intValue.length > 0 && intValue(0) == '-'
      if (isNegative) {
        if (intValue.length - 1 > scale) {
          val (part1, part2) = intValue.splitAt(intValue.length - scale)
          part1 + '.' + part2
        } else {
          "-" + "0." + "0" * (scale - intValue.length + 1) + intValue.splitAt(1)._2
        }
      } else {
        if (intValue.length > scale) {
          val (part1, part2) = intValue.splitAt(intValue.length - scale)
          part1 + '.' + part2
        } else {
          "0." + "0" * (scale - intValue.length) + intValue.splitAt(0)._2
        }
      }
    }
  }

  /** A generic decoder for 2s compliment binary numbers aka COMP
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeBinaryNumber(bytes: Array[Byte], bigEndian: Boolean, signed: Boolean, scale: Int = 0): String = {
    if (bytes.length == 0) {
      return "0"
    }

    val bits = BitVector(bytes)
    val value = (signed, bigEndian, bytes.length) match {
      case (true, true, 1) => int8B.decode(bits).require.value
      case (true, true, 2) => int16B.decode(bits).require.value
      case (true, true, 4) => int32B.decode(bits).require.value
      case (true, true, 8) => int64B.decode(bits).require.value
      case (true, false, 1) => int8L.decode(bits).require.value
      case (true, false, 2) => int16L.decode(bits).require.value
      case (true, false, 4) => int32L.decode(bits).require.value
      case (true, false, 8) => int64L.decode(bits).require.value
      case (false, true, 1) => uint8B.decode(bits).require.value
      case (false, true, 2) => uint16B.decode(bits).require.value
      case (false, true, 4) => uint32B.decode(bits).require.value
      case (false, false, 1) => uint8L.decode(bits).require.value
      case (false, false, 2) => uint16L.decode(bits).require.value
      case (false, false, 4) => uint32L.decode(bits).require.value
      case _ =>
        // Generic arbitrary precision decoder
        val bigInt = (bigEndian, signed) match {
          case (false, false) => BigInt(1, bytes.reverse)
          case (false, true) => BigInt(bytes.reverse)
          case (true, false) => BigInt(1, bytes)
          case (true, true) => BigInt(bytes)
        }
        bigInt
    }
    addDecimalPoint(value.toString, scale)
  }

  /** A decoder for floating point numbers
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeFloatingPointNumber(bytes: Array[Byte], bigEndian: Boolean): String = {
    val bits = BitVector(bytes)
    val value = (bigEndian, bytes.length) match {
      case (true, 4) => floatB.decode(bits).require.value
      case (true, 8) => doubleB.decode(bits).require.value
      case (false, 4) => floatL.decode(bits).require.value
      case (false, 8) => doubleL.decode(bits).require.value
      case _ => throw new IllegalArgumentException(s"Illegal number of bytes to decode (${bytes.length}). Expected either 4 or 8 for floating point" +
        s" type.")
    }
    value.toString
  }

  /** Formats and validates a string as a number. Returns None if the string doesn't pass the validation **/
  private def validateAndFormatNumber(str: String): Option[String] = {
    val value = try {
      Some(BigDecimal(str).toString)
    } catch {
      case NonFatal(_) => None
    }
    value
  }
}
