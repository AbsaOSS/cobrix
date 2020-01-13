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

package za.co.absa.cobrix.cobol.parser.examples.generators

import java.io.{BufferedOutputStream, FileOutputStream}

import za.co.absa.cobrix.cobol.parser.examples.generators.model.CommonLists
import za.co.absa.cobrix.cobol.parser.examples.generators.utils.GeneratorTools._

import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  */
object TestDataGen9CodePages {

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

  val currencies: Seq[String] = CommonLists.currencies

  def main(args: Array[String]): Unit = {

    val numOfCurrencies = currencies.size

    val byteArray: Array[Byte] = new Array[Byte](45)
    val bos = new BufferedOutputStream(new FileOutputStream("TRAN.APR14.NPT.DATA.dat"))
    var i = 0
    val sig = "S9276511"
    while (i < numberOfRecordsToGenerate) {
      val currency = currencies(rand.nextInt(numOfCurrencies))
      val company = generateRandomByteArray(rand, 14)

      putStringToArray(byteArray, currency, 0, 2) // 3
      putStringToArray(byteArray, sig, 3, 10) // 8

      putArrayToArray(byteArray, company, 11, 25) //15
      putStringToArray(byteArray, "00000000", 26, 35) //10


      val tp = rand.nextInt(100)
      val amountIntPart = if (tp < 80) {
        rand.nextInt(1000).toLong
      } else if (tp < 95) {
        rand.nextInt(100000).toLong
      } else {
        rand.nextInt(10000000).toLong
      }

      val amountFracPart = if (amountIntPart < 10000) rand.nextInt(100) else 0

      putDecimalToArray(byteArray, amountIntPart, amountFracPart, 37, 44) // 8

      if (rand.nextInt(100) < 37)
        putStringToArray(byteArray, "1", 36, 36) // 1
      else
        putStringToArray(byteArray, "0", 36, 36)

      bos.write(byteArray)
      i += 1
    }
    bos.close()
  }
}
