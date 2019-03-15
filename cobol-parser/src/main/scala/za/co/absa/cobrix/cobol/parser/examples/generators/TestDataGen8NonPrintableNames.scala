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
object TestDataGen8NonPrintableNames {

  val numberOfRecordsToGenerate = 1000

  // seed=100 is used for the integration test
  val rand: Random = new Random(100)

  /*
          01  TRANSDATA.
              05  CURRENCY          PIC X(3).
              05  SIGNATURE         PIC X(8).
              05  COMPANY-NAME-NP   PIC X(15).
              05  COMPIANY-ID       PIC X(10).
              05  WEALTH-QFY        PIC 9(1).
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

  def putDecimalToArray(bytes: Array[Byte], intpart: Long, fractPart: Int, index0: Int, index1: Int): Unit = {

    val lng = intpart.toLong*100 + fractPart

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

  val currencies: Seq[String] = CommonLists.currencies

  val companies: Seq[Company] = CommonLists.companiesWithNonPrintableCharacters

  def main(args: Array[String]): Unit = {

    val numOfCurrencies = currencies.size
    val numOfCompanies = companies.size

    val byteArray: Array[Byte] = new Array[Byte](45)
    val bos = new BufferedOutputStream(new FileOutputStream("TRAN2.MAR14.DATA.dat"))
    var i = 0
    val sig = "S9276511"
    while (i< numberOfRecordsToGenerate) {
      val currency = currencies(rand.nextInt(numOfCurrencies))
      val company = companies(rand.nextInt(numOfCompanies))

      putStringToArray(byteArray, currency, 0, 2) // 3
      putStringToArray(byteArray, sig, 3, 10)  // 8

      putEbcdicStringToArray(byteArray, company.companyName, 11, 25) //15
      putStringToArray(byteArray, company.companyId, 26, 35) //10

      val tp = rand.nextInt(100)
      val amountIntPart = if (tp<80){
        rand.nextInt(1000).toLong
      } else if (tp<95) {
        rand.nextInt(100000).toLong
      } else {
        rand.nextInt(10000000).toLong
      }

      val amountFracPart = if (amountIntPart < 10000) rand.nextInt(100) else 0

      putDecimalToArray(byteArray, amountIntPart, amountFracPart, 37, 44) // 8

      if ( rand.nextInt(100) < 37 )
        putStringToArray(byteArray, "1", 36, 36) // 1
      else
        putStringToArray(byteArray, "0", 36, 36)

      bos.write(byteArray)
      i += 1
    }
    bos.close()
  }
}
