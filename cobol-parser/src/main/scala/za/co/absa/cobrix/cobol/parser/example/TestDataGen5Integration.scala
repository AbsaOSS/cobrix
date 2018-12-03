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

package za.co.absa.cobrix.cobol.parser.example

import java.io.{BufferedOutputStream, FileOutputStream}

import scodec.Attempt.Successful
import za.co.absa.cobrix.cobol.parser.common.{BinaryUtils, Constants}

import scala.util.Random

// This is a test data generator for the big integration test.
// The purpose of the data is to cover as much as possible the variety of primitive data types.
// The generated file won't contain XCOM headers

/*
        01  RECORD.

***********************************************************************
*******                   EDGE CASES
***********************************************************************
          10  ID                   PIC 9(7)  BINARY.

********** String
          10  STRING-VAL           PIC X(10).

********** Integral unsigned numbers formatted as strings
          10  NUM-STR-INT01        PIC 9(1).
          10  NUM-STR-INT02        PIC 9(2).
          10  NUM-STR-INT03        PIC 9(3).
          10  NUM-STR-INT04        PIC 9(4).
          10  NUM-STR-INT05        PIC 9(5).
          10  NUM-STR-INT06        PIC 9(8).
          10  NUM-STR-INT07        PIC 9(9).
          10  NUM-STR-INT08        PIC 9(10).
          10  NUM-STR-INT09        PIC 9(11).
          10  NUM-STR-INT10        PIC 9(17).
          10  NUM-STR-INT11        PIC 9(18).
          10  NUM-STR-INT12        PIC 9(19).
          10  NUM-STR-INT13        PIC 9(20).
          10  NUM-STR-INT14        PIC 9(37).

********** Integral signed numbers formatted as strings
          10  NUM-STR-SINT02       PIC S9(2).
          10  NUM-STR-SINT03       PIC S9(3).
          10  NUM-STR-SINT04       PIC S9(4).
          10  NUM-STR-SINT05       PIC S9(5).
          10  NUM-STR-SINT06       PIC S9(8).
          10  NUM-STR-SINT07       PIC S9(9).
          10  NUM-STR-SINT08       PIC S9(10).
          10  NUM-STR-SINT09       PIC S9(11).
          10  NUM-STR-SINT10       PIC S9(17).
          10  NUM-STR-SINT11       PIC S9(18).
          10  NUM-STR-SINT12       PIC S9(19).
          10  NUM-STR-SINT13       PIC S9(20).
          10  NUM-STR-SINT14       PIC S9(37).

********** Decimal numbers formatted as strings
          10  NUM-STR-DEC01       PIC 99V9.
          10  NUM-STR-DEC02       PIC 99V99.
          10  NUM-STR-DEC03       PIC 9(3)V99.
          10  NUM-STR-DEC04       PIC 9(4)V9(4).
          10  NUM-STR-DEC05       PIC 9(5)V9(4).
          10  NUM-STR-DEC06       PIC 9(5)V9(5).
          10  NUM-STR-DEC07       PIC 9(15)V99.
          10  NUM-STR-DEC08       PIC 9(16)V99.
          10  NUM-STR-DEC09       PIC 9(17)V99.
          10  NUM-STR-DEC10       PIC 9(18)V9(10).
          10  NUM-STR-SDEC01      PIC S99V9.
          10  NUM-STR-SDEC02      PIC S99V99.
          10  NUM-STR-SDEC03      PIC S9(3)V99.
          10  NUM-STR-SDEC04      PIC S9(4)V9(4).
          10  NUM-STR-SDEC05      PIC S9(5)V9(4).
          10  NUM-STR-SDEC06      PIC S9(5)V9(5).
          10  NUM-STR-SDEC07      PIC S9(15)V99.
          10  NUM-STR-SDEC08      PIC S9(16)V99.
          10  NUM-STR-SDEC09      PIC S9(17)V99.
          10  NUM-STR-SDEC10      PIC S9(18)V9(10).
********** These types are currently not supported, added for the future
********** 10  NUM-STR-EDEC03      PIC S9(3).99.
********** 10  NUM-STR-EDEC04      PIC S9(4).9(4).
********** 10  NUM-STR-EDEC05      PIC S9(5).9(4).
********** 10  NUM-STR-EDEC06      PIC S9(5).9(5).

********** Binary formatted integral numbers
          10  NUM-BIN-INT01       PIC 9(1)         COMP.
          10  NUM-BIN-INT02       PIC 9(2)         COMP.
          10  NUM-BIN-INT03       PIC 9(3)         COMP.
          10  NUM-BIN-INT04       PIC 9(4)         COMP.
          10  NUM-BIN-INT05       PIC 9(5)         COMP.
          10  NUM-BIN-INT06       PIC 9(8)         BINARY.
          10  NUM-BIN-INT07       PIC 9(9)         BINARY.
************* These types are currently not supported, added for the future
************* 10  NUM-BIN-INT08       PIC 9(10)       BINARY.
************* 10  NUM-BIN-INT09       PIC 9(11)       BINARY.
************* 10  NUM-BIN-INT10       PIC 9(17)       BINARY.
************* 10  NUM-BIN-INT11       PIC 9(18)       BINARY.
************* 10  NUM-BIN-INT12       PIC 9(19)       BINARY.
************* 10  NUM-BIN-INT13       PIC 9(20)       BINARY.
************* 10  NUM-BIN-INT14       PIC 9(37)       BINARY.
          10  NUM-SBIN-SINT01     PIC S9(1)        COMP.
          10  NUM-SBIN-SINT02     PIC S9(2)        COMP.
          10  NUM-SBIN-SINT03     PIC S9(3)        COMP.
          10  NUM-SBIN-SINT04     PIC S9(4)        COMP.
          10  NUM-SBIN-SINT05     PIC S9(5)        COMP.
          10  NUM-SBIN-SINT06     PIC S9(8)        BINARY.
          10  NUM-SBIN-SINT07     PIC S9(9)        BINARY.
          10  NUM-SBIN-SINT08     PIC S9(10)       BINARY.
          10  NUM-SBIN-SINT09     PIC S9(11)       BINARY.
          10  NUM-SBIN-SINT10     PIC S9(17)       BINARY.
          10  NUM-SBIN-SINT11     PIC S9(18)       BINARY.
************* These types are currently not supported, added for the future
************* 10  NUM-SBIN-SINT12     PIC S9(19)      BINARY.
************* 10  NUM-SBIN-SINT13     PIC S9(20)      BINARY.
************* 10  NUM-SBIN-SINT14     PIC S9(37)      BINARY.

********** Binary formatted decimal numbers
          10  NUM-BIN-DEC01       PIC 99V9         COMP.
          10  NUM-BIN-DEC02       PIC 99V99        COMP.
          10  NUM-BIN-DEC03       PIC 9(3)V99      COMP.
          10  NUM-BIN-DEC04       PIC 9(4)V9(4)    COMP.
          10  NUM-BIN-DEC05       PIC 9(5)V9(4)    COMP.
************* These types are currently not supported, added for the future
************* 10  NUM-BIN-DEC06       PIC 9(5)V9(5)   COMP.
************* 10  NUM-BIN-DEC07       PIC 9(15)V99    COMP.
************* 10  NUM-BIN-DEC08       PIC 9(16)V99    COMP.
************* 10  NUM-BIN-DEC09       PIC 9(17)V99    COMP.
************* 10  NUM-BIN-DEC10       PIC 9(18)V(10)  COMP.
          10  NUM-SBIN-DEC01      PIC S99V9        COMP.
          10  NUM-SBIN-DEC02      PIC S99V99       COMP.
          10  NUM-SBIN-DEC03      PIC S9(3)V99     COMP.
          10  NUM-SBIN-DEC04      PIC S9(4)V9(4)   COMP.
          10  NUM-SBIN-DEC05      PIC S9(5)V9(4)   COMP.
          10  NUM-SBIN-DEC06      PIC S9(5)V9(5)   COMP.
          10  NUM-SBIN-DEC07      PIC S9(15)V99    COMP.
          10  NUM-SBIN-DEC08      PIC S9(16)V99    COMP.
************* These types are currently not supported, added for the future
************* 10  NUM-SBIN-DEC09      PIC S9(17)V99   COMP.
************* 10  NUM-SBIN-DEC10      PIC S9(18)V(10) COMP.

********** BCD formatted integral numbers
          10  NUM-BCD-INT01       PIC 9(1)        COMP-3.
          10  NUM-BCD-INT02       PIC 9(2)        COMP-3.
          10  NUM-BCD-INT03       PIC 9(3)        COMP-3.
          10  NUM-BCD-INT04       PIC 9(4)        COMP-3.
          10  NUM-BCD-INT05       PIC 9(5)        COMP-3.
          10  NUM-BCD-INT06       PIC 9(8)        COMP-3.
          10  NUM-BCD-INT07       PIC 9(9)        COMP-3.
          10  NUM-BCD-INT08       PIC 9(10)       COMP-3.
          10  NUM-BCD-INT09       PIC 9(11)       COMP-3.
          10  NUM-BCD-INT10       PIC 9(17)       COMP-3.
          10  NUM-BCD-INT11       PIC 9(18)       COMP-3.
          10  NUM-BCD-INT12       PIC 9(19)       COMP-3.
          10  NUM-BCD-INT13       PIC 9(20)       COMP-3.
          10  NUM-BCD-INT14       PIC 9(37)       COMP-3.

          10  NUM-BCD-SINT01      PIC S9(1)       COMP-3.
          10  NUM-BCD-SINT02      PIC S9(2)       COMP-3.
          10  NUM-BCD-SINT03      PIC S9(3)       COMP-3.
          10  NUM-BCD-SINT04      PIC S9(4)       COMP-3.
          10  NUM-BCD-SINT05      PIC S9(5)       COMP-3.
          10  NUM-BCD-SINT06      PIC S9(8)       COMP-3.
          10  NUM-BCD-SINT07      PIC S9(9)       COMP-3.
          10  NUM-BCD-SINT08      PIC S9(10)      COMP-3.
          10  NUM-BCD-SINT09      PIC S9(11)      COMP-3.
          10  NUM-BCD-SINT10      PIC S9(17)      COMP-3.
          10  NUM-BCD-SINT11      PIC S9(18)      COMP-3.
          10  NUM-BCD-SINT12      PIC S9(19)      COMP-3.
          10  NUM-BCD-SINT13      PIC S9(20)      COMP-3.
          10  NUM-BCD-SINT14      PIC S9(37)      COMP-3.

********** BCD formatted decimal numbers
          10  NUM-BCD-DEC01       PIC 99V9        COMP-3.
          10  NUM-BCD-DEC02       PIC 99V99       COMP-3.
          10  NUM-BCD-DEC03       PIC 9(3)V99     COMP-3.
          10  NUM-BCD-DEC04       PIC 9(4)V9(4)   COMP-3.
          10  NUM-BCD-DEC05       PIC 9(5)V9(4)   COMP-3.
          10  NUM-BCD-DEC06       PIC 9(5)V9(5)   COMP-3.
          10  NUM-BCD-DEC07       PIC 9(15)V99    COMP-3.
          10  NUM-BCD-DEC08       PIC 9(16)V99    COMP-3.
          10  NUM-BCD-DEC09       PIC 9(17)V99    COMP-3.
          10  NUM-BCD-DEC10       PIC 9(18)V9(10) COMP-3.
          10  NUM-BCD-SDEC01      PIC S99V9       COMP-3.
          10  NUM-BCD-SDEC02      PIC S99V99      COMP-3.
          10  NUM-BCD-SDEC03      PIC S9(3)V99    COMP-3.
          10  NUM-BCD-SDEC04      PIC S9(4)V9(4)  COMP-3.
          10  NUM-BCD-SDEC05      PIC S9(5)V9(4)  COMP-3.
          10  NUM-BCD-SDEC06      PIC S9(5)V9(5)  COMP-3.
          10  NUM-BCD-SDEC07      PIC S9(15)V99   COMP-3.
          10  NUM-BCD-SDEC08      PIC S9(16)V99   COMP-3.
          10  NUM-BCD-SDEC09      PIC S9(17)V99   COMP-3.
          10  NUM-BCD-SDEC10      PIC S9(18)V9(10) COMP-3.

********** sign trailing numbers
          10  NUM-SL-STR-INT01    PIC S9(9) SIGN IS
		                          LEADING SEPARATE.
          10  NUM-SL-STR-DEC01    PIC 99V99 SIGN IS
                         LEADING SEPARATE CHARACTER.
          10  NUM-ST-STR-INT01    PIC S9(9) SIGN IS
		                          TRAILING SEPARATE.
          10  NUM-ST-STR-DEC01    PIC 99V99 SIGN
                         TRAILING SEPARATE.

***********************************************************************
*******                   COMMON TYPES
***********************************************************************
          10  COMMON-8-BIN        PIC 9(8)        BINARY.
          10  COMMON-S3-BIN       PIC S9(3)       BINARY.
          10  COMMON-S94COMP      PIC S9(04)      COMP.
          10  COMMON-S8-BIN       PIC S9(8)       BINARY.
          10  COMMON-DDC97-BIN    PIC S9V9(7)     BINARY.
          10  COMMON-97COMP3      PIC 9(07)       COMP-3.
          10  COMMON-915COMP3     PIC 9(15)       COMP-3.
          10  COMMON-S95COMP3     PIC S9(5)       COMP-3.
          10  COMMON-S999DCCOMP3  PIC S9(09)V99   COMP-3.
          10  COMMON-S913COMP3    PIC S9(13)      COMP-3.
          10  COMMON-S913DCCOMP3  PIC S9(13)V99   COMP-3.
          10  COMMON-S911DCC2     PIC S9(11)V99   COMP-3.
          10  COMMON-S910DCC3     PIC S9(10)V999  COMP-3.

***********************************************************************
*******            EXOTIC AND COMPILER SPECIFIC
***********************************************************************

 */

/**
  * This is a test data generator. The copybook for it is listed above.
  */
object TestDataGen5Integration {

  var debugPrint = true

  def putStringToArray(fieldName: String, bytes: Array[Byte], str: String, index0: Int, length: Int): Int = {
    if (debugPrint) {
      println(s"Putting string $fieldName to offsets $index0 .. ${index0 + length - 1}. New offset = ${index0 + length}")
    }
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
                               explicitDecimalPosition: Int = -1): Int = {

    val explicitDecimalChars = if (explicitDecimalPosition >= 0) 1 else 0
    val explicitSignChars = if (isSignSeparate) 1 else 0
    val trailingSignChars = if (isSignSeparate && !isSignLeading) 1 else 0
    val numLen = if (isSignSeparate) {
      length + explicitSignChars
    } else {
      if (signed) length + explicitSignChars - 1 else length + explicitSignChars
    }

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
          bytes(index0 + binLength) = Constants.minusCharEBCIDIC
        else
          bytes(index0 + binLength) = Constants.plusCharEBCIDIC
      }
    }
    val index1 = i
    val newOffset = index1 + binLength + explicitDecimalChars + trailingSignChars

    if (debugPrint) {
      println(s"Putting number $fieldName <- '$str' to offsets $index0 .. ${newOffset - 1}. New offset = $newOffset")
    }

    var j = 0
    while (j < binLength) {
      if (i == explicitDecimalPosition) {
        bytes(i) = '.'
        i += 1
      }
      bytes(i) = encodedValue(j)
      i += 1
      j += 1
    }
    newOffset
  }

  def encodeUncompressed(numStr: String, targetLength: Int): Array[Byte] = {
    var j = 0
    val outputArray = new Array[Byte](targetLength)
    while (j < targetLength) {
      if (j < numStr.length)
        outputArray(j) = BinaryUtils.asciiToEbcdic(numStr.charAt(j))
      else outputArray(j) = 0
      j += 1
    }
    outputArray
  }

  def encodeBinSigned(numStr: String): Array[Byte] = {
    val len = if (numStr(0) == '-') numStr.length - 1 else numStr.length
    if (len <= Constants.maxShortPrecision) {
      scodec.codecs.int16.encode(numStr.toInt).require.toByteArray
    } else if (len <= Constants.maxIntegerPrecision) {
      scodec.codecs.int32.encode(numStr.toInt).require.toByteArray
    } else {
      scodec.codecs.int64.encode(numStr.toLong).require.toByteArray
    }
  }

  def encodeBinUnsigned(numStr: String): Array[Byte] = {
    val len = numStr.length
    if (len <= Constants.maxShortPrecision) {
      scodec.codecs.uint16.encode(numStr.toInt).require.toByteArray
    } else if (len <= Constants.maxIntegerPrecision) {
      scodec.codecs.uint32.encode(numStr.toLong).require.toByteArray
    } else {
      scodec.codecs.int64.encode(BigInt(numStr).toLong).require.toByteArray
    }
  }

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
    putEncodedNumStrToArray((str: String) => encodeUncompressed(str, length),
      fieldName, bytes, bigNumber, index0, length, signed, isNegative, isSignSeparate, isSignLeading, explicitDecimalPosition)
  }

  val strings = Seq(
    "Jene",
    "Maya",
    "Starr",
    "Lynell",
    "Eliana",
    "Tyesha",
    "Beatrice",
    "Otelia",
    "Timika",
    "Wilbert",
    "Mindy",
    "Sunday",
    "Tyson",
    "Cliff",
    "Mabelle",
    "Verdie",
    "Sulema",
    "Alona",
    "Suk",
    "Deandra",
    "Doretha",
    "Cassey",
    "Janiece",
    "Deshawn",
    "Willis",
    "Carrie",
    "Gabriele",
    "Inge",
    "Edyth",
    "Estelle"
  )

  def getVeryBigNumber(rng: Random): String = {
    val num1 = rng.nextInt(89999999) + 10000000
    val num2 = rng.nextInt(89999999) + 10000000
    val num3 = rng.nextInt(89999999) + 10000000
    val num4 = rng.nextInt(89999999) + 10000000
    val num5 = rng.nextInt(89999999) + 10000000
    val num6 = rng.nextInt(89999999) + 10000000
    val num7 = rng.nextInt(89999999) + 10000000
    s"$num1$num2$num3$num4$num5$num6$num7"
  }

  def main(args: Array[String]): Unit = {

    val numberOfrecodsToGenerate = 100

    // seed=100 is used for the integration test
    val rand: Random = new Random(/*100*/)

    val byteArray: Array[Byte] = new Array[Byte](1127)

    val bos = new BufferedOutputStream(new FileOutputStream("INTEGR.TYPES.NOV28.DATA.dat"))
    var i = 0
    while (i < numberOfrecodsToGenerate) {
      var offset = 0

      val bigNum = getVeryBigNumber(rand)
      val isNegative = i == 0 || rand.nextBoolean()

      offset = putIntToArray(byteArray, i + 1, offset)

      val randomString = strings(rand.nextInt(strings.length))
      offset = putStringToArray("STRING-VAL", byteArray, randomString, offset, 10)

      // Unsigned numbers formatted as strings
      offset = putNumStrToArray("NUM-STR-INT01", byteArray, bigNum, offset, 1, signed = false)
      offset = putNumStrToArray("NUM-STR-INT02", byteArray, bigNum, offset, 2, signed = false)
      offset = putNumStrToArray("NUM-STR-INT03", byteArray, bigNum, offset, 3, signed = false)
      offset = putNumStrToArray("NUM-STR-INT04", byteArray, bigNum, offset, 4, signed = false)
      offset = putNumStrToArray("NUM-STR-INT05", byteArray, bigNum, offset, 5, signed = false)
      offset = putNumStrToArray("NUM-STR-INT06", byteArray, bigNum, offset, 8, signed = false)
      offset = putNumStrToArray("NUM-STR-INT07", byteArray, bigNum, offset, 9, signed = false)
      offset = putNumStrToArray("NUM-STR-INT08", byteArray, bigNum, offset, 10, signed = false)
      offset = putNumStrToArray("NUM-STR-INT09", byteArray, bigNum, offset, 11, signed = false)
      offset = putNumStrToArray("NUM-STR-INT10", byteArray, bigNum, offset, 17, signed = false)
      offset = putNumStrToArray("NUM-STR-INT11", byteArray, bigNum, offset, 18, signed = false)
      offset = putNumStrToArray("NUM-STR-INT12", byteArray, bigNum, offset, 19, signed = false)
      offset = putNumStrToArray("NUM-STR-INT13", byteArray, bigNum, offset, 20, signed = false)
      offset = putNumStrToArray("NUM-STR-INT14", byteArray, bigNum, offset, 37, signed = false)

      // Signed numbers formatted as strings
      offset = putNumStrToArray("NUM-STR-SINT02", byteArray, bigNum, offset, 2, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT03", byteArray, bigNum, offset, 3, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT04", byteArray, bigNum, offset, 4, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT05", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT06", byteArray, bigNum, offset, 8, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT07", byteArray, bigNum, offset, 9, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT08", byteArray, bigNum, offset, 10, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT09", byteArray, bigNum, offset, 11, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT10", byteArray, bigNum, offset, 17, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT11", byteArray, bigNum, offset, 18, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT12", byteArray, bigNum, offset, 19, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT13", byteArray, bigNum, offset, 20, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SINT14", byteArray, bigNum, offset, 37, signed = true, isNegative)

      // Decimal numbers formatted as strings
      offset = putNumStrToArray("NUM-STR-DEC01", byteArray, bigNum, offset, 3, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC02", byteArray, bigNum, offset, 4, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC03", byteArray, bigNum, offset, 5, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC04", byteArray, bigNum, offset, 8, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC05", byteArray, bigNum, offset, 9, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC06", byteArray, bigNum, offset, 10, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC07", byteArray, bigNum, offset, 17, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC08", byteArray, bigNum, offset, 18, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC09", byteArray, bigNum, offset, 19, signed = false)
      offset = putNumStrToArray("NUM-STR-DEC10", byteArray, bigNum, offset, 28, signed = false)
      offset = putNumStrToArray("NUM-STR-SDEC01", byteArray, bigNum, offset, 3, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC02", byteArray, bigNum, offset, 4, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC03", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC04", byteArray, bigNum, offset, 8, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC05", byteArray, bigNum, offset, 9, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC06", byteArray, bigNum, offset, 10, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC07", byteArray, bigNum, offset, 17, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC08", byteArray, bigNum, offset, 18, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC09", byteArray, bigNum, offset, 19, signed = true, isNegative)
      offset = putNumStrToArray("NUM-STR-SDEC10", byteArray, bigNum, offset, 28, signed = true, isNegative)

      // Binary formatted integral numbers
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT01", byteArray, bigNum, offset, 1, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT02", byteArray, bigNum, offset, 2, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT03", byteArray, bigNum, offset, 3, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT04", byteArray, bigNum, offset, 4, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT05", byteArray, bigNum, offset, 5, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT06", byteArray, bigNum, offset, 8, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-INT07", byteArray, bigNum, offset, 9, signed = false)

      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT01", byteArray, bigNum, offset, 1, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT02", byteArray, bigNum, offset, 2, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT03", byteArray, bigNum, offset, 3, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT04", byteArray, bigNum, offset, 4, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT05", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT06", byteArray, bigNum, offset, 8, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT07", byteArray, bigNum, offset, 9, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT08", byteArray, bigNum, offset, 10, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT09", byteArray, bigNum, offset, 11, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT10", byteArray, bigNum, offset, 17, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-SINT11", byteArray, bigNum, offset, 18, signed = true, isNegative)

      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-DEC01", byteArray, bigNum, offset, 3, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-DEC02", byteArray, bigNum, offset, 4, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-DEC03", byteArray, bigNum, offset, 5, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-DEC04", byteArray, bigNum, offset, 8, signed = false)
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "NUM-BIN-DEC05", byteArray, bigNum, offset, 9, signed = false)

      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC01", byteArray, bigNum, offset, 3, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC02", byteArray, bigNum, offset, 4, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC03", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC04", byteArray, bigNum, offset, 8, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC05", byteArray, bigNum, offset, 9, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC06", byteArray, bigNum, offset, 10, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC07", byteArray, bigNum, offset, 17, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBinSigned, "NUM-SBIN-DEC08", byteArray, bigNum, offset, 18, signed = true, isNegative)

      // BCD formatted integral numbers
      val encodeBcdSigned = (str: String) => encodeBcd(str, isSigned = true)
      val encodeBcdUnsigned = (str: String) => encodeBcd(str, isSigned = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT01", byteArray, bigNum, offset, 1, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT02", byteArray, bigNum, offset, 2, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT03", byteArray, bigNum, offset, 3, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT04", byteArray, bigNum, offset, 4, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT05", byteArray, bigNum, offset, 5, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT06", byteArray, bigNum, offset, 8, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT07", byteArray, bigNum, offset, 9, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT08", byteArray, bigNum, offset, 10, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT09", byteArray, bigNum, offset, 11, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT10", byteArray, bigNum, offset, 17, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT11", byteArray, bigNum, offset, 18, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT12", byteArray, bigNum, offset, 19, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT13", byteArray, bigNum, offset, 20, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-INT14", byteArray, bigNum, offset, 37, signed = false)

      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT01", byteArray, bigNum, offset, 1, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT02", byteArray, bigNum, offset, 2, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT03", byteArray, bigNum, offset, 3, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT04", byteArray, bigNum, offset, 4, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT05", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT06", byteArray, bigNum, offset, 8, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT07", byteArray, bigNum, offset, 9, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT08", byteArray, bigNum, offset, 10, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT09", byteArray, bigNum, offset, 11, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT10", byteArray, bigNum, offset, 17, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT11", byteArray, bigNum, offset, 18, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT12", byteArray, bigNum, offset, 19, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT13", byteArray, bigNum, offset, 20, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SINT14", byteArray, bigNum, offset, 37, signed = true, isNegative)

      // BCD formatted decimal numbers
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC01", byteArray, bigNum, offset, 3, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC02", byteArray, bigNum, offset, 4, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC03", byteArray, bigNum, offset, 5, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC04", byteArray, bigNum, offset, 8, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC05", byteArray, bigNum, offset, 9, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC06", byteArray, bigNum, offset, 10, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC07", byteArray, bigNum, offset, 17, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC08", byteArray, bigNum, offset, 18, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC09", byteArray, bigNum, offset, 19, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "NUM-BCD-DEC10", byteArray, bigNum, offset, 28, signed = false)

      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC01", byteArray, bigNum, offset, 3, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC02", byteArray, bigNum, offset, 4, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC03", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC04", byteArray, bigNum, offset, 8, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC05", byteArray, bigNum, offset, 9, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC06", byteArray, bigNum, offset, 10, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC07", byteArray, bigNum, offset, 17, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC08", byteArray, bigNum, offset, 18, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC09", byteArray, bigNum, offset, 19, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "NUM-BCD-SDEC10", byteArray, bigNum, offset, 28, signed = true, isNegative)

      // Sign separate numbers
      offset = putNumStrToArray("NUM-SL-STR-INT01", byteArray, bigNum, offset, 9, signed = true, isNegative, isSignSeparate = true, isSignLeading = true)
      offset = putNumStrToArray("NUM-SL-STR-DEC01", byteArray, bigNum, offset, 4, signed = true, isNegative, isSignSeparate = true, isSignLeading = true)
      offset = putNumStrToArray("NUM-ST-STR-INT01", byteArray, bigNum, offset, 9, signed = true, isNegative, isSignSeparate = true, isSignLeading = false)
      offset = putNumStrToArray("NUM-ST-STR-DEC01", byteArray, bigNum, offset, 4, signed = true, isNegative, isSignSeparate = true, isSignLeading = false)

      // Common types
      offset = putEncodedNumStrToArray(encodeBinUnsigned, "COMMON-8-BIN", byteArray, bigNum, offset, 8, signed = false)
      offset = putEncodedNumStrToArray(encodeBinSigned, "COMMON-S3-BIN", byteArray, bigNum, offset, 3, signed = true)
      offset = putEncodedNumStrToArray(encodeBinSigned, "COMMON-S94COMP", byteArray, bigNum, offset, 4, signed = true)
      offset = putEncodedNumStrToArray(encodeBinSigned, "COMMON-S8-BIN", byteArray, bigNum, offset, 8, signed = true)
      offset = putEncodedNumStrToArray(encodeBinSigned, "COMMON-DDC97-BIN", byteArray, bigNum, offset, 8, signed = true)

      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "COMMON-97COMP3", byteArray, bigNum, offset, 7, signed = false)
      offset = putEncodedNumStrToArray(encodeBcdUnsigned, "COMMON-915COMP3", byteArray, bigNum, offset, 15, signed = false)

      offset = putEncodedNumStrToArray(encodeBcdSigned, "COMMON-S95COMP3", byteArray, bigNum, offset, 5, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "COMMON-S999DCCOMP3", byteArray, bigNum, offset, 11, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "COMMON-S913COMP3", byteArray, bigNum, offset, 13, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "COMMON-S913DCCOMP3", byteArray, bigNum, offset, 15, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "COMMON-S911DCC2", byteArray, bigNum, offset, 13, signed = true, isNegative)
      offset = putEncodedNumStrToArray(encodeBcdSigned, "COMMON-S910DCC3", byteArray, bigNum, offset, 13, signed = true, isNegative)

      bos.write(byteArray)
      i += 1
      debugPrint = false
    }
    bos.close()
  }
}
