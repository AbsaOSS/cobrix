/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.cobrix.cobol.parser.examples.generators

import java.io.{BufferedOutputStream, FileOutputStream}

import scodec.Attempt.Successful
import za.co.absa.cobrix.cobol.parser.decoders.BinaryUtils
import za.co.absa.cobrix.cobol.parser.examples.generators.model.Company

import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  */
object TestDataGen7Fillers {

  val numberOfRecordsToGenerate = 1000

  // seed=100 is used for the integration test
  val rand: Random = new Random(/*100*/)

  /*
        01  RECORD.
            05  COMPANY_NAME     PIC X(15).
            05  FILLER REDEFINES COMPANY_NAME.
               10   STR1         PIC X(5).
               10   STR2         PIC X(2).
               10   FILLER       PIC X(1).
            05  ADDRESS          PIC X(25).
            05  FILLER REDEFINES ADDRESS.
               10   STR4         PIC X(10).
               10   FILLER       PIC X(20).
            05  FILL_FIELD.
               10   FILLER       PIC X(5).
               10   FILLER       PIC X(2).
            05  CONTACT_PERSON REDEFINES FILL_FIELD.
               10  FIRST_NAME    PIC X(6).
            05  AMOUNT            PIC S9(09)V99  BINARY.
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

  val companies: Seq[Company] = CommonLists.companies

  val names: Seq[String] = CommonLists.firstNames

  def main(args: Array[String]): Unit = {

    val numOfCompanies = companies.size
    val numOfNames = names.size

    val byteArray: Array[Byte] = new Array[Byte](60)

    val bos = new BufferedOutputStream(new FileOutputStream("TEST.FILLERS.DEC07.DATA.dat"))
    var i = 0
    while (i < numberOfRecordsToGenerate) {

      val company = companies(rand.nextInt(numOfCompanies))
      putStringToArray(byteArray, company.companyName, 0, 14) // 15
      putStringToArray(byteArray, company.address, 15, 44) // 30

      val name = names(rand.nextInt(numOfNames))
      putStringToArray(byteArray, name, 45, 51) // 7

      val tp = rand.nextInt(100)
      val amountIntPart = if (tp<80){
        rand.nextInt(1000).toLong
      } else if (tp<95) {
        rand.nextInt(100000).toLong
      } else {
        rand.nextInt(10000000).toLong
      }

      val amountFracPart = if (amountIntPart < 10000) rand.nextInt(100) else 0

      putDecimalToArray(byteArray, amountIntPart, amountFracPart, 52, 59) // 8

      bos.write(byteArray)
      i += 1

    }
    bos.close()
  }
}
