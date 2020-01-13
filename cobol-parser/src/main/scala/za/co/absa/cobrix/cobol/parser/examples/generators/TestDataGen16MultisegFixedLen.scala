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
import java.util

import za.co.absa.cobrix.cobol.parser.examples.generators.model.{CommonLists, Company}
import za.co.absa.cobrix.cobol.parser.examples.generators.utils.GeneratorTools._

import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  * This generates a multisegment data file in which all record lengths are fixed.
  */
object TestDataGen16MultisegFixedLen {

  val numberOfRecordsToGenerate = 50

  // seed=100 is used for the integration test
  val rand: Random = new Random(100)

  /*
          01  ENTITY.
              05  SEGMENT-ID        PIC X(1).
              05  COMPANY.
                 10  COMPANY-NAME      PIC X(20).
                 10  ADDRESS           PIC X(30).
                 10  TAXPAYER          PIC X(8).
              05  PERSON REDEFINES COMPANY.
                 10  FIRST-NAME        PIC X(16).
                 10  LAST-NAME         PIC X(16).
                 10  ADDRESS           PIC X(20).
                 10  PHONE-NUM         PIC X(11).
              05  PO-BOX REDEFINES COMPANY.
                 10  PO-NUMBER         PIC X(12).
                 10  BRANCH-ADDRESS    PIC X(20).
   */

  val segments: Seq[String] = Seq("C", "P", "B")

  val companies: Seq[Company] = CommonLists.companies

  val firstNames: Seq[String] = CommonLists.firstNames

  val lastNames: Seq[String] = CommonLists.lastNames

  def main(args: Array[String]): Unit = {

    val numOfCompanies = companies.size

    val byteArray1: Array[Byte] = new Array[Byte](64)

    val bos = new BufferedOutputStream(new FileOutputStream("ENTITY.DB.AUG12.DATA.FIX.LEN.dat"))

    var i = 0
    while (i < numberOfRecordsToGenerate) {

      util.Arrays.fill(byteArray1, 64.toByte)
      val segmentId = rand.nextInt(3)

      val company = companies(rand.nextInt(numOfCompanies))

      segmentId match {
        case 0 =>
          val taxPayerNum = rand.nextInt(89999999) + 10000000

          // SegmentId
          putStringToArray(byteArray1, segments.head, 0, 0) // 1

          // Company details
          putStringToArray(byteArray1, company.companyName, 1, 20) // 20
          putStringToArray(byteArray1, company.address, 21, 50) // 30
          putStringToArray(byteArray1, taxPayerNum.toString, 51, 58) // 8
        case 1 =>
          val firstName = firstNames(rand.nextInt(firstNames.length))
          val lastName = lastNames(rand.nextInt(lastNames.length))
          val phoneNum = s"+(${rand.nextInt(920)+1}) ${rand.nextInt(899)+100} ${rand.nextInt(89)+10} ${rand.nextInt(89)+10}"

          // SegmentId
          putStringToArray(byteArray1, segments(1), 0, 0) // 1

          putStringToArray(byteArray1, firstName, 1, 16) // 16
          putStringToArray(byteArray1, lastName, 17, 32) // 16
          putStringToArray(byteArray1, company.address, 33, 52) // 20
          putStringToArray(byteArray1, phoneNum, 53, 63) // 11
        case 2 =>
          val poNum = Math.abs(rand.nextLong) % 100000000000L

          // SegmentId
          putStringToArray(byteArray1, segments(2), 0, 0) // 1

          // PO Box details
          putStringToArray(byteArray1, poNum.toString, 1, 12) // 12
          putStringToArray(byteArray1, company.address, 13, 32) // 20
      }

      bos.write(byteArray1)
      i += 1
    }
    bos.close()
  }
}
