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

import com.example.spark.cobol.examples.parser.generators.TestDataGen17Hierarchical.{rand, roles}
import com.example.spark.cobol.examples.parser.generators.model.{CommonLists, Company}
import com.example.spark.cobol.examples.parser.generators.utils.GeneratorTools._

import java.io.{BufferedOutputStream, FileOutputStream}
import scala.collection.mutable
import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  */
object TestDataGen19WideTransactions {

  // seed=100 is used for the integration test
  val rand: Random = new Random(102)

  val numberOfRecordsToGenerate = rand.nextInt(900000) + 1000000
  //val numberOfRecordsToGenerate = rand.nextInt(90000) + 10000

  /*
          01  RECORD.
              05  ACCOUNT-ID        PIC X(15).
              05  CURRENCY          PIC X(3).
              05  BALANCE           PIC 9(12)V99.
              05  COMPANY-NAME      PIC X(15).
              05  COMPANY-ID        PIC X(10).
              05  DEPT-NAME         PIC X(22).
              05  FIRST-NAME        PIC X(16).
              05  LAST-NAME         PIC X(16).
              05  AGE               PIC 9(3).
              05  MARRIAGE-STATUS   PIC X(1).
              05  ADDRESS           PIC X(30).
              05  ZIP               PIC X(10).
              05  PHONE-NUM         PIC X(17).
              05  ROLE              PIC X(18).
   */

  val currencies: Seq[String] = CommonLists.currencies

  val companies: Seq[Company] = CommonLists.companies

  val departments: Seq[String] = CommonLists.departments

  val roles: Seq[String] = CommonLists.roles

  val firstNames: Seq[String] = CommonLists.firstNames

  val lastNames: Seq[String] = CommonLists.lastNames


  def main(args: Array[String]): Unit = {

    val numOfCurrencies = currencies.size
    val numOfCompanies = companies.size

    val byteArray: Array[Byte] = new Array[Byte](190)
    val bos = new BufferedOutputStream(new FileOutputStream("EBCDIC.ACCOUNTS.FEATURES.DATA.MAR24.dat"))
    var i = 0
    while (i< numberOfRecordsToGenerate) {
      // Account Id 100012300012345
      val accountId0 = rand.nextInt(4)
      val accountId1 = rand.nextInt(900) + 100
      val accountId2 = rand.nextInt(90000) + 10000
      val accountId = accountId0.toString + "000" + accountId1.toString + "000" + accountId2.toString

      val currency = currencies(rand.nextInt(numOfCurrencies))
      val company = companies(rand.nextInt(numOfCompanies))

      val dept = departments(rand.nextInt(departments.size))

      val fname = firstNames(rand.nextInt(firstNames.size))
      val lname = lastNames(rand.nextInt(lastNames.size))
      val role = roles(rand.nextInt(roles.size))
      val address = companies(rand.nextInt(companies.size)).address
      val phoneNum = s"+(${rand.nextInt(920) + 1}) ${rand.nextInt(899) + 100} ${rand.nextInt(89) + 10} ${rand.nextInt(89) + 10}"
      val zip = (rand.nextInt(899999999) + 100000000).toString
      val age = (rand.nextInt(60) + 19).toString
      val mstatuses = "SSSSSSMMMDW"
      val mstatus = mstatuses(rand.nextInt(mstatuses.length)).toString

      // Department details

      val tp = rand.nextInt(100)
      val amountIntPart = if (tp < 20) {
        rand.nextInt(1000).toLong
      } else if (tp < 65) {
        rand.nextInt(100000).toLong
      } else {
        rand.nextInt(10000000).toLong
      }

      val intPartLen = amountIntPart.toString.length
      val lpad = " " * (12 - intPartLen)
      val amountIntPartStr = lpad + amountIntPart.toString

      val amountFracPart = if (amountIntPart < 10000) rand.nextInt(100) else 0

      var offset = 0
      offset = putStringToArray("ACCOUNT-ID", byteArray, accountId, offset, 15)
      offset = putStringToArray("CURRENCY", byteArray, currency, offset, 3)
      offset = putStringToArray("BALANCE_i", byteArray, amountIntPartStr, offset, 12)
      offset = putStringToArray("BALANCE_f", byteArray, amountFracPart.toString, offset, 2)
      offset = putStringToArray("COMPANY-NAME", byteArray, company.companyName, offset, 15)
      offset = putStringToArray("COMPANY-ID", byteArray, company.companyId, offset, 10)
      offset = putStringToArray("DEPT-NAME", byteArray, dept, offset, 22)
      offset = putStringToArray("FIRST-NAME", byteArray, fname, offset, 16)
      offset = putStringToArray("LAST-NAME", byteArray, lname, offset, 16)
      offset = putStringToArray("AGE", byteArray, age, offset, 3)
      offset = putStringToArray("MSTATUS", byteArray, mstatus, offset, 1)
      offset = putStringToArray("ADDRESS", byteArray, address, offset, 30)
      offset = putStringToArray("ZIP", byteArray, zip, offset, 10)
      offset = putStringToArray("PHONE-NUM", byteArray, phoneNum, offset, 17)
      offset = putStringToArray("ROLE", byteArray, role, offset, 18)

      bos.write(byteArray)
      i += 1
    }
    bos.close()
  }
}
