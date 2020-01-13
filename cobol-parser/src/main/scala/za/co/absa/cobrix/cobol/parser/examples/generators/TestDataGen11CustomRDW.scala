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

import za.co.absa.cobrix.cobol.parser.examples.generators.model.{CommonLists, Company}
import za.co.absa.cobrix.cobol.parser.examples.generators.utils.GeneratorTools._

import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  * This program generates a custom RDW header that consists of 1 byte for a validity flag and 4 bytes for record length.
  * It is used for custom record header parser testing.
  */
object TestDataGen11CustomRDW {

  val numberOfRecordsToGenerate = 1000

  // seed=100 is used for the integration test
  val rand: Random = new Random(100)

  /*
          01  COMPANY-DETAILS.
              05  SEGMENT-ID        PIC X(5).
              05  COMPANY-ID        PIC X(10).
              05  STATIC-DETAILS.
                 10  COMPANY-NAME      PIC X(15).
                 10  ADDRESS           PIC X(25).
                 10  TAXPAYER.
                    15  TAXPAYER-TYPE  PIC X(1).
                    15  TAXPAYER-STR   PIC X(8).
                    15  TAXPAYER-NUM  REDEFINES TAXPAYER-STR
                                       PIC 9(8) COMP.
              05  CONTACTS REDEFINES STATIC-DETAILS.
                 10  PHONE-NUMBER      PIC X(17).
                 10  CONTACT-PERSON    PIC X(28).
   */

  val segments: Seq[String] = Seq("C", "P")

  val companies: Seq[Company] = CommonLists.companies

  val firstNames: Seq[String] = CommonLists.firstNames

  val lastNames: Seq[String] = CommonLists.lastNames

  def main(args: Array[String]): Unit = {

    val numOfCompanies = companies.size

    val byteArray1: Array[Byte] = new Array[Byte](69)
    val byteArray2: Array[Byte] = new Array[Byte](65)
    val byteArray3: Array[Byte] = new Array[Byte](20)

    val bos = new BufferedOutputStream(new FileOutputStream("COMP.DETAILS.MAY30.DATA.dat"))
    var i = 0
    while (i < numberOfRecordsToGenerate) {

      // SEGMENT 1 (root)

      val company = companies(rand.nextInt(numOfCompanies))

      // Generating random company id for join on company id to work as expected
      val companyId = s"${rand.nextInt(89999) + 10000}${rand.nextInt(89999) + 10000}"

      val isValidRecord = rand.nextBoolean()

      if (isValidRecord) {
        // Custom RDW header
        byteArray1(0) = 1 // isValid = true
        byteArray1(1) = 0
        byteArray1(2) = 0
        putShortToArray(byteArray1, 64, 3, 4) // record size = 64

        // Common values
        putStringToArray(byteArray1, segments(0), 5, 9) // 5
        putStringToArray(byteArray1, companyId, 10, 19) // 10

        // Static details
        putStringToArray(byteArray1, company.companyName, 20, 34) // 15
        putStringToArray(byteArray1, company.address, 35, 59) // 25

        val taxPayerType = rand.nextBoolean()
        val taxPayerNum = rand.nextInt(89999999) + 10000000

        if (taxPayerType) {
          putStringToArray(byteArray1, "A", 60, 60) // 1
          putStringToArray(byteArray1, taxPayerNum.toString, 61, 68) // 8
        }

        else {
          putStringToArray(byteArray1, "N", 60, 60) // 1
          putIntToArray(byteArray1, taxPayerNum, 61, 64) // 4
          byteArray1(65) = 0
          byteArray1(66) = 0
          byteArray1(67) = 0
          byteArray1(68) = 0
        }

        bos.write(byteArray1)
        i += 1

        // SEGMENT 2 (child)

        val numOfContacts = rand.nextInt(5)

        var j = 0

        while (j < numOfContacts && i < numberOfRecordsToGenerate) {
          // Custom RDW header
          byteArray2(0) = 1 // isValid = true
          byteArray2(1) = 0
          byteArray2(2) = 0
          putShortToArray(byteArray2, 60, 3, 4) // record size = 60

          // Common values
          putStringToArray(byteArray2, segments(1), 5, 9) // 5
          putStringToArray(byteArray2, companyId, 10, 19) // 10

          // Contacts
          val phoneNum = s"+(${rand.nextInt(920)+1}) ${rand.nextInt(899)+100} ${rand.nextInt(89)+10} ${rand.nextInt(89)+10}"
          putStringToArray(byteArray2, phoneNum, 20, 36) // 17

          val contactPerson = firstNames(rand.nextInt(firstNames.length)) + " " + lastNames(rand.nextInt(lastNames.length))
          putStringToArray(byteArray2, contactPerson, 37, 64) // 17

          bos.write(byteArray2)
          i += 1

          j += 1
        }
      } else {
        byteArray3(0) = 0
        byteArray3(1) = 0
        byteArray3(2) = 0
        putShortToArray(byteArray3, 15, 3, 4) // record size = 15
        bos.write(byteArray3)
      }
    }
    bos.close()
  }
}
