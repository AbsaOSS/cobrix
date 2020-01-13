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

package za.co.absa.cobrix.cobol.parser.examples.generators.utils

import scodec.Attempt.Successful
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.decoders.BinaryUtils

import scala.util.Random

/**
  * This object contains a set of tooling methods for mainframe data generation
  */
object GeneratorTools {

  /** Generates a random array of a specified length. */
  def generateRandomByteArray(rand: Random, length: Int): Array[Byte] = {
    val array = new Array[Byte](length)
    rand.nextBytes(array)
    array
  }

  /** Copies a smaller]array into a larger array into positions determined by indexes. */
  def putArrayToArray(bytes: Array[Byte], str: Array[Byte], index0: Int, index1: Int): Unit = {
    var i = index0
    var j = 0
    while (i <= index1) {
      if (j < str.length)
        bytes(i) = str(j)
      else bytes(i) = 0
      i += 1
      j += 1
    }
  }

  /**
    * Puts an ASCII string into a byte array into positions determined by indexes.
    * The passed string is automatically converted to EBCDIC common page encoding.
    */
  def putStringToArray(bytes: Array[Byte], str: String, index0: Int, index1: Int): Unit = {
    var i = index0
    var j = 0
    while (i <= index1) {
      if (j < str.length)
        bytes(i) = BinaryUtils.asciiToEbcdic(str.charAt(j))
      else bytes(i) = 0
      i += 1
      j += 1
    }
  }

  /**
    * Puts an ASCII string into a byte array into positions determined by a starting index (offset).
    * The new offset is returned.
    * The passed string is automatically converted to EBCDIC common page encoding.
    */
  def putStringToArray(fieldName: String, bytes: Array[Byte], str: String, index0: Int, length: Int): Int = {
    println(s"Putting string $fieldName to offsets $index0 .. ${index0 + length - 1}. New offset = ${index0 + length}")
    var i = index0
    var j = 0
    while (i < index0 + length) {
      if (j < str.length)
        bytes(i) = BinaryUtils.asciiToEbcdic(str.charAt(j))
      else bytes(i) = 0
      i += 1
      j += 1
    }
    index0 + length
  }

  /**
    * Puts a string into a byte array into positions determined by a indexes.
    * No conversion from ASCII to EBCDIC is performed.
    */
  def putEbcdicStringToArray(bytes: Array[Byte], str: String, index0: Int, index1: Int): Unit = {
    var i = index0
    var j = 0
    while (i <= index1) {
      if (j < str.length)
        bytes(i) = str.charAt(j).toByte
      else bytes(i) = 0
      i += 1
      j += 1
    }
  }

  /**
    * Puts a little-endian short number to a byte array into positions determined by a indexes.
    */
  def putShortToArray(bytes: Array[Byte], number: Short, index0: Int, index1: Int): Unit = {
    val coded = scodec.codecs.int16L.encode(number)

    coded match {
      case Successful(a) =>
        var i = index0
        while (i <= index1) {
          bytes(i) = a.getByte(i - index0)
          i += 1
        }
      case _ =>
        var i = index0
        while (i <= index1) {
          bytes(i) = 0
          i += 1
        }
    }
  }

  /**
    * Puts a big-endian short number to a byte array into positions determined by a indexes.
    */
  def putShortToArrayBigEndian(bytes: Array[Byte], number: Short, index0: Int, index1: Int): Unit = {
    val coded = scodec.codecs.int16.encode(number)

    coded match {
      case Successful(a) =>
        var i = index0
        while (i <= index1) {
          bytes(i) = a.getByte(i - index0)
          i += 1
        }
      case _ =>
        var i = index0
        while (i <= index1) {
          bytes(i) = 0
          i += 1
        }
    }
  }

  /**
    * Puts a little-endian integer to a byte array into positions determined by a starting index (offset).
    * The new offset is returned.
    */
  def putIntToArray(bytes: Array[Byte], number: Int, index0: Int): Int = {
    val coded = scodec.codecs.int32.encode(number)

    coded match {
      case Successful(a) =>
        var i = index0
        while (i < index0 + 4) {
          bytes(i) = a.getByte(i - index0)
          i += 1
        }
      case _ =>
        var i = index0
        while (i < index0 + 4) {
          bytes(i) = 0
          i += 1
        }
    }
    index0 + 4
  }

  /**
    * Puts a little-endian short number to a byte array into positions determined by a indexes.
    */
  def putIntToArray(bytes: Array[Byte], number: Int, index0: Int, index1: Int): Unit = {
    val coded = scodec.codecs.int32.encode(number)

    coded match {
      case Successful(a) =>
        var i = index0
        while (i <= index1) {
          bytes(i) = a.getByte(i - index0)
          i += 1
        }
      case _ =>
        var i = index0
        while (i <= index1) {
          bytes(i) = 0
          i += 1
        }
    }
  }

  /**
    * Puts a 64 bit little-endian decimal number defined by integral and fracture parts to a byte array into
    * positions determined by a indexes.
    */
  def putDecimalToArray(bytes: Array[Byte], intpart: Long, fractPart: Int, index0: Int, index1: Int): Unit = {

    val lng = intpart.toLong * 100 + fractPart

    val coded = scodec.codecs.int64.encode(lng)

    coded match {
      case Successful(a) =>
        var i = index0
        while (i <= index1) {
          bytes(i) = a.getByte(i - index0)
          i += 1
        }
      case _ =>
        var i = index0
        while (i <= index1) {
          bytes(i) = 0
          i += 1
        }
    }
  }

  /**
    * Puts a 8 digit BCD number (COMP-3) a byte array into positions determined by a indexes.
    */
  def putComp3ToArrayS8(bytes: Array[Byte], number: Int, index0: Int, index1: Int): Unit = {
    var num = number

    val startNibble = num % 10
    num /= 10

    bytes(index0 + 3) = (12 + startNibble * 16).toByte

    var i = 0
    while (i < 3) {
      val lowNibble = num % 10
      num /= 10
      val highNibble = num % 10
      num /= 10
      bytes(index0 + 2 - i) = (lowNibble + highNibble * 16).toByte

      i += 1
    }

  }

  /**
    * Puts an arbitrary number represented as a string into a byte array according to provided encoding.
    * The resulting bytes are inserted into an array according to a starting index (offset).
    * The new offset is returned.
    */
  def putEncodedNumStrToArray(
                               encoder: String => Array[Byte],
                               fieldName: String,
                               bytes: Array[Byte],
                               bigNumber: String,
                               index0: Int,
                               length: Int,
                               signed: Boolean,
                               isNegative: Boolean = false,
                               isSignSeparate: Boolean = false,
                               isSignLeading: Boolean = true,
                               isSignPunching: Boolean = true,
                               explicitDecimalPosition: Int = -1): Int = {

    val explicitDecimalChars = if (explicitDecimalPosition >= 0) 1 else 0
    val explicitSignChars = if (isSignSeparate) 1 else 0
    val trailingSignChars = if (isSignSeparate && !isSignLeading) 1 else 0
    val numLen = length + explicitSignChars

    var str = bigNumber.take(length)
    if (!isSignSeparate) {
      if (isNegative) str = "-" + str
    }

    val encodedValue = encoder(str)
    val binLength = encodedValue.length

    var i = index0
    if (isSignSeparate) {
      if (isSignLeading) {
        if (isNegative)
          bytes(i) = Constants.minusCharEBCIDIC
        else
          bytes(i) = Constants.plusCharEBCIDIC
        i += 1
      } else {
        if (isNegative)
          bytes(index0 + binLength + explicitDecimalChars) = Constants.minusCharEBCIDIC
        else
          bytes(index0 + binLength + explicitDecimalChars) = Constants.plusCharEBCIDIC
      }
    }
    val index1 = i
    val newOffset = index1 + binLength + explicitDecimalChars + trailingSignChars

    println(s"Putting number $fieldName <- (size=${newOffset - index0}) '$str' to offsets $index0 .. ${newOffset - 1}. New offset = $newOffset")

    var j = 0
    while (j < binLength) {
      if (j == explicitDecimalPosition) {
        bytes(i) = Constants.dotCharEBCIDIC
        i += 1
      }
      bytes(i) = encodedValue(j)
      i += 1
      j += 1
    }
    newOffset
  }

  /**
    * Encodes a number according to the rules of uncompressed (DISPLAY) COBOL EBCDIC format. Returns the corresponding array
    * of bytes.
    */
  def encodeUncompressed(numStr: String, targetLength: Int, isSigned: Boolean, signPunch: Boolean, isSignLeading: Boolean): Array[Byte] = {
    val sign = if (isSigned && numStr(0) == '-') '-' else '+'
    val str = if (numStr(0) == '-') numStr.drop(1) else numStr
    var j = 0
    val outputArray = new Array[Byte](targetLength)
    while (j < targetLength) {
      if (j < str.length) {
        var c = BinaryUtils.asciiToEbcdic(str.charAt(j))
        val punchSignal = (isSignLeading && j == 0) || (!isSignLeading && j == str.length - 1)
        if (punchSignal && isSigned && signPunch) {
          val num = if (sign == '-') {
            str.charAt(j).toInt - 0x30 + 0xD0
          } else {
            str.charAt(j).toInt - 0x30 + 0xC0
          }
          c = num.toByte
        }
        outputArray(j) = c
      }
      else outputArray(j) = 0
      j += 1
    }
    outputArray
  }
  /**
    * Encodes a number according to the rules of singed binary compressed big-endian numbers.
    * Returns the corresponding array of bytes.
    */
  def encodeBinSigned(numStr: String): Array[Byte] = {
    val len = if (numStr(0) == '-') numStr.length - 1 else numStr.length
    if (len <= Constants.maxShortPrecision) {
      scodec.codecs.int16.encode(numStr.toInt).require.toByteArray
    } else if (len <= Constants.maxIntegerPrecision) {
      scodec.codecs.int32.encode(numStr.toInt).require.toByteArray
    } else if (len <= Constants.maxLongPrecision) {
      scodec.codecs.int64.encode(numStr.toLong).require.toByteArray
    } else {
      strToBigArray(numStr, isSigned = true)
    }
  }

  /**
    * Encodes a number according to the rules of unsigned binary compressed big-endian numbers.
    * Returns the corresponding array of bytes.
    */
  def encodeBinUnsigned(numStr: String): Array[Byte] = {
    val len = numStr.length
    if (len <= Constants.maxShortPrecision) {
      scodec.codecs.uint16.encode(numStr.toInt).require.toByteArray
    } else if (len <= Constants.maxIntegerPrecision) {
      scodec.codecs.uint32.encode(numStr.toLong).require.toByteArray
    } else if (len <= Constants.maxLongPrecision) {
      scodec.codecs.int64.encode(BigInt(numStr).toLong).require.toByteArray
    } else {
      strToBigArray(numStr, isSigned = false)
    }
  }

  /**
    * Encodes a number according to the rules of IEEE-754 float format (COMP-1).
    * Returns the corresponding array of bytes.
    */
  def encodeFloat(numStr: String): Array[Byte] = scodec.codecs.float.encode(numStr.toFloat).require.toByteArray

  /**
    * Encodes a number according to the rules of IEEE-754 double format (COMP-1).
    * Returns the corresponding array of bytes.
    */
  def encodeDouble(numStr: String): Array[Byte] = scodec.codecs.double.encode(numStr.toDouble).require.toByteArray

  /**
    * Encodes an arbitrary big integer in a big-endian binary (COMP) format.
    * Returns the corresponding array of bytes.
    */
  def strToBigArray(numStr: String, isSigned: Boolean): Array[Byte] = {
    val precision = if (numStr(0) == '-' || numStr(0) == '+') numStr.length - 1 else numStr.length
    val numberOfBytes = ((Math.log(10) / Math.log(2)) * precision + 1) / 8
    val expectedNumOfBytes = math.ceil(numberOfBytes).toInt

    val bytes = new Array[Byte](expectedNumOfBytes)
    val bigIntBytes = BigInt(numStr).toByteArray

    var j = bigIntBytes.length - 1
    var i = bytes.length - 1

    while (i >= 0 && j >= 0) {
      bytes(i) = bigIntBytes(j)
      i -= 1
      j -= 1
    }
    if (i >= 0) {
      if (numStr(0) == '-') bytes(0) = -1
    }

    bytes
  }

  /**
    * Encodes an arbitrary number in BCD format.
    * Returns the corresponding array of bytes.
    */
  def encodeBcd(numStr: String, isSigned: Boolean): Array[Byte] = {
    val isNegative = numStr(0) == '-'
    val str = if (isNegative) numStr.drop(1) else numStr
    val outputArray = new Array[Byte](str.length / 2 + 1)
    var i = 0
    var j = 0
    val signNibble: Byte = if (isSigned) {
      if (isNegative) 13 else 12
    } else 15
    if (str.length % 2 == 0) {
      val highNibble: Byte = (str(i).toByte - 48).toByte
      outputArray(j) = highNibble
      i += 1
      j += 1
    }
    while (i < str.length) {
      val highNibble: Byte = (str(i).toByte - 48).toByte
      val lowNibble: Byte = if (i == str.length - 1) {
        signNibble
      } else {
        (str(i + 1).toByte - 48).toByte
      }
      outputArray(j) = (highNibble * 16 + lowNibble).toByte
      i += 2
      j += 1
    }
    outputArray
  }


  /**
    * Puts an arbitrary number in EBCDIC uncompressed (DISPLAY) format into an array.
    * The starting offset is determined by 'index0'. The resulting offset is returned.
    */
  def putNumStrToArray(fieldName: String,
                       bytes: Array[Byte],
                       bigNumber: String,
                       index0: Int,
                       length: Int,
                       signed: Boolean,
                       isNegative: Boolean = false,
                       isSignSeparate: Boolean = false,
                       isSignLeading: Boolean = false,
                       explicitDecimalPosition: Int = -1): Int = {
    putEncodedNumStrToArray((str: String) => encodeUncompressed(str, length, signed, !isSignSeparate, isSignLeading),
      fieldName, bytes, bigNumber, index0, length, signed, isNegative, isSignSeparate, isSignLeading, !isSignSeparate, explicitDecimalPosition)
  }

  /**
    * Puts an number as an IEEE-754 float value into an array.
    * The starting offset is determined by 'index0'. The resulting offset is returned.
    */
  def putFloat(fieldName: String, bytes: Array[Byte], bigNumber: String, index0: Int, isNegative: Boolean = false): Int = {
    val floatNum: String = bigNumber.take(5) + "." + bigNumber.slice(5, 7)
    putEncodedNumStrToArray((str: String) => encodeFloat(str),
      fieldName, bytes, floatNum, index0, 9, signed = true, isNegative = isNegative)
  }


  /**
    * Puts an number as an IEEE-754 double value into an array.
    * The starting offset is determined by 'index0'. The resulting offset is returned.
    */
  def putDouble(fieldName: String, bytes: Array[Byte], bigNumber: String, index0: Int, isNegative: Boolean = false): Int = {
    val floatNum: String = bigNumber.take(10) + "." + bigNumber.slice(10, 14)
    putEncodedNumStrToArray((str: String) => encodeDouble(str),
      fieldName, bytes, floatNum, index0, 20, signed = true, isNegative = isNegative)
  }

}
