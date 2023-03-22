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

package com.example.spark.cobol.examples.parser.generators

import com.example.spark.cobol.examples.parser.generators.model.{CommonLists, Company}
import com.example.spark.cobol.examples.parser.generators.utils.GeneratorTools._

import java.io.{BufferedOutputStream, FileOutputStream}
import scala.collection.mutable
import scala.tools.nsc.util.HashSet
import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  */
object TestDataGen18EbcdicTransactions {

  val numberOfRecordsToGenerate = 10000

  // seed=100 is used for the integration test
  val rand: Random = new Random(100)

  /*
          01  RECORD.
              05  TXN-ID            PIC X(5).
              05  CURRENCY          PIC X(3).
              05  COMPANY-NAME      PIC X(15).
              05  COMPANY-ID        PIC X(10).
              05  AMOUNT            PIC 9(10)V99.
   */

  val currencies: Seq[String] = CommonLists.currencies

  val companies: Seq[Company] = CommonLists.companies

  def main(args: Array[String]): Unit = {

    val numOfCurrencies = currencies.size
    val numOfCompanies = companies.size

    val txnIds = mutable.HashSet[String]()

    val byteArray: Array[Byte] = new Array[Byte](45)
    val bos = new BufferedOutputStream(new FileOutputStream("EBCDIC.TXN.MAR20.DATA.dat"))
    var i = 0
    while (i< numberOfRecordsToGenerate) {

      var txnId = rand.nextInt(90000) + 10000

      while (txnIds.contains(txnId.toString)){
        txnId = rand.nextInt(90000) + 10000
      }

      txnIds.add(txnId.toString)

      val currency = currencies(rand.nextInt(numOfCurrencies))
      val company = companies(rand.nextInt(numOfCompanies))

      putStringToArray(byteArray, txnId.toString, 0, 4) // 5
      putStringToArray(byteArray, currency, 5, 7) // 3
      putStringToArray(byteArray, company.companyName, 8, 22) // 15
      putStringToArray(byteArray, company.companyId, 23, 32)  // 10

      val tp = rand.nextInt(100)
      val amountIntPart = if (tp<80){
        rand.nextInt(1000).toLong
      } else if (tp<95) {
        rand.nextInt(100000).toLong
      } else {
        rand.nextInt(10000000).toLong
      }

      val intPartLen = amountIntPart.toString.length
      val lpad = " " * (10 - intPartLen)
      val amountIntPartStr = lpad + amountIntPart.toString

      val amountFracPart = if (amountIntPart < 10000) rand.nextInt(100) else 0

      putStringToArray(byteArray, amountIntPartStr.toString, 33, 42) // 10

      putStringToArray(byteArray, amountFracPart.toString, 43, 44) // 2

      bos.write(byteArray)
      i += 1
    }
    bos.close()
  }
}
