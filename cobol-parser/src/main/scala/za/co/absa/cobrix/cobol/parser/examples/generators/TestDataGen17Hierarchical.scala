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
  * This generates a multisegment hierarchical data file with little-endian RDW headers.
  */
object TestDataGen17Hierarchical {

  val numberOfRecordsToGenerate = 50

  // seed=100 is used for the integration test
  val rand: Random = new Random(100)

  /*
       01  ENTITY.

           05  SEGMENT-ID           PIC 9(1).

           05  COMPANY.
              10  COMPANY-NAME      PIC X(20).
              10  ADDRESS           PIC X(30).
              10  TAXPAYER          PIC 9(9) BINARY.
           05  DEPT REDEFINES COMPANY.
              10  DEPT-NAME         PIC X(22).
              10  EXTENSION         PIC 9(6).
           05  EMPLOYEE REDEFINES COMPANY.
              10  FIRST-NAME        PIC X(16).
              10  LAST-NAME         PIC X(16).
              10  ROLE              PIC X(18).
              10  HOME-ADDRESS      PIC X(40).
              10  PHONE-NUM         PIC X(17).
           05  OFFICE REDEFINES COMPANY.
              10  ADDRESS           PIC X(30).
              10  FLOOR             PIC 9(3).
              10  ROOM-NUMBER       PIC 9(4).
           05  CUSTOMER REDEFINES COMPANY.
              10  CUSTOMER-NAME     PIC X(20).
              10  POSTAL-ADDRESS    PIC X(30).
              10  ZIP               PIC X(10).
           05  CONTACT REDEFINES COMPANY.
              10  FIRST-NAME        PIC X(16).
              10  LAST-NAME         PIC X(16).
              10  PHONE-NUM         PIC X(17).
           05  CONTRACT REDEFINES COMPANY.
              10  CONTRACT-NUMBER   PIC X(15).
              10  STATE             PIC X(8).
              10  DUE-DATE          PIC X(10).
              10  AMOUNT            PIC 9(10)V9(2) COMP-3.
   */

  val companies: Seq[Company] = CommonLists.companies

  val departments: Seq[String] = CommonLists.departments

  val roles: Seq[String] = CommonLists.roles

  val firstNames: Seq[String] = CommonLists.firstNames

  val lastNames: Seq[String] = CommonLists.lastNames

  val contractStates: Seq[String] = CommonLists.contractStates

  def getRandomIsoDate: String = {
    val day: Int = rand.nextInt(28) + 1
    val month: Int = rand.nextInt(12) + 1
    val year: Int = rand.nextInt(30) + 1990
    f"$year%04d-$month%02d-$day%02d"
  }

  def putCompany(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](59)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 55, 2, 3) // record size = 59 bytes

    // Data to save
    val company = companies(rand.nextInt(companies.size))
    val taxPayerNum = rand.nextInt(899999999) + 100000000
    val numDepartments = rand.nextInt(5)
    val numCustomers = rand.nextInt(5)

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "1", offset, 1)

    // Company details
    offset = putStringToArray("COMPANY-NAME", bytes, company.companyName, offset, 20)
    offset = putStringToArray("ADDRESS", bytes, company.address, offset, 30)
    offset = putEncodedNumStrToArray(encodeBinUnsigned, "TAXPAYER", bytes, taxPayerNum.toString, offset, 9, signed = false)

    bos.write(bytes)
    //println(s"Company: $offset")

    for (_ <- 0 until numDepartments) {
      putDepartment(bos)
    }

    for (_ <- 0 until numCustomers) {
      putCustomer(bos)
    }
  }

  def putDepartment(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](33)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 29, 2, 3) // record size = 28 bytes

    // Data to save
    val dept = departments(rand.nextInt(departments.size))
    val ext = rand.nextInt(899999) + 100000
    val numEmployees = rand.nextInt(7)
    val numOffices = rand.nextInt(4)

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "2", offset, 1)

    // Department details
    offset = putStringToArray("DEPT-NAME", bytes, dept, offset, 22)
    offset = putNumStrToArray("EXTENSION", bytes, ext.toString, offset, 6, signed = false)

    bos.write(bytes)
    //println(s"Dept: $offset")

    for (_ <- 0 until numEmployees) {
      putEmployee(bos)
    }

    for (_ <- 0 until numOffices) {
      putOffice(bos)
    }
  }

  def putEmployee(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](112)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 108, 2, 3) // record size = 107 bytes

    // Data to save
    val fname = firstNames(rand.nextInt(firstNames.size))
    val lname = lastNames(rand.nextInt(lastNames.size))
    val role = roles(rand.nextInt(roles.size))
    val address = companies(rand.nextInt(companies.size)).address
    val phoneNum = s"+(${rand.nextInt(920)+1}) ${rand.nextInt(899)+100} ${rand.nextInt(89)+10} ${rand.nextInt(89)+10}"

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "3", offset, 1)

    // Employee details
    offset = putStringToArray("FIRST-NAME", bytes, fname, offset, 16)
    offset = putStringToArray("LAST-NAME", bytes, lname, offset, 16)
    offset = putStringToArray("ROLE", bytes, role, offset, 18)
    offset = putStringToArray("HOME-ADDRESS", bytes, address, offset, 40)
    offset = putStringToArray("PHONE-NUM", bytes, phoneNum, offset, 17)

    bos.write(bytes)
  }

  def putOffice(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](42)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 38, 2, 3) // record size = 37 bytes

    // Data to save
    val address = companies(rand.nextInt(companies.size)).address
    val floor = rand.nextInt(120).toString
    val roomNumber = rand.nextInt(3000).toString

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "4", offset, 1)

    // Office details
    offset = putStringToArray("ADDRESS", bytes, address, offset, 30)
    offset = putStringToArray("FLOOR", bytes, floor, offset, 3)
    offset = putStringToArray("ROOM-NUMBER", bytes, roomNumber, offset, 4)

    bos.write(bytes)
  }

  def putCustomer(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](65)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 61, 2, 3) // record size = 60 bytes

    // Data to save
    val name = companies(rand.nextInt(companies.size)).companyName
    val address = companies(rand.nextInt(companies.size)).address
    val zip = (rand.nextInt(899999999) + 100000000).toString
    val numContacts = rand.nextInt(3)
    val numContracts = rand.nextInt(5)

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "5", offset, 1)

    // Customer details
    offset = putStringToArray("CUSTOMER-NAME", bytes, name, offset, 20)
    offset = putStringToArray("POSTAL-ADDRESS", bytes, address, offset, 30)
    offset = putStringToArray("ZIP", bytes, zip, offset, 10)

    bos.write(bytes)
    //println(s"Customer: $offset")

    for (_ <- 0 until numContacts) {
      putContact(bos)
    }

    for (_ <- 0 until numContracts) {
      putContract(bos)
    }
  }

  def putContact(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](54)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 50, 2, 3) // record size = 49 bytes

    // Data to save
    val fname = firstNames(rand.nextInt(firstNames.size))
    val lname = lastNames(rand.nextInt(lastNames.size))
    val phoneNum = s"+(${rand.nextInt(920)+1}) ${rand.nextInt(899)+100} ${rand.nextInt(89)+10} ${rand.nextInt(89)+10}"

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "6", offset, 1)

    // Contact details
    offset = putStringToArray("FIRST-NAME", bytes, fname, offset, 16)
    offset = putStringToArray("LAST-NAME", bytes, lname, offset, 16)
    offset = putStringToArray("PHONE-NUM", bytes, phoneNum, offset, 17)

    bos.write(bytes)
  }

  def putContract(bos: BufferedOutputStream): Unit = {
    val bytes: Array[Byte] = new Array[Byte](45)

    // RDW header
    bytes(0) = 0
    bytes(1) = 0
    putShortToArray(bytes, 41, 2, 3) // record size = 40 bytes

    // Data to save
    val contractNumber = rand.nextInt(1000000).toString
    val state = contractStates(rand.nextInt(contractStates.size))
    val dueDate = getRandomIsoDate

    val amountType = rand.nextInt(4)

    val amount = amountType match {
      case 0 => rand.nextInt(89999999) + 10000
      case 1 => rand.nextInt(99)*100 + 10000
      case 2 => rand.nextInt(89999) + 100000
      case 3 => rand.nextInt(89999999) + 10000000
      case _ => rand.nextInt(1000) + 1000
    }

    val amountPadded = f"$amount%012d"

    // SegmentId
    var offset = 4
    offset = putStringToArray("SEGMENT-ID", bytes, "7", offset, 1)

    // Contract details
    val encodeBcdUnsigned = (str: String) => encodeBcd(str, isSigned = false)
    offset = putStringToArray("CONTRACT-NUMBER", bytes, contractNumber, offset, 15)
    offset = putStringToArray("STATE", bytes, state, offset, 8)
    offset = putStringToArray("DUE-DATE", bytes, dueDate, offset, 10)
    offset = putEncodedNumStrToArray(encodeBcdUnsigned, "AMOUNT", bytes, amountPadded, offset, 12, signed = false)

    bos.write(bytes)
  }

  def main(args: Array[String]): Unit = {
    val bytes: Array[Byte] = new Array[Byte](1000)

    val bos = new BufferedOutputStream(new FileOutputStream("HIERARCHICAL.DATA.RDW.dat"))

    for (_ <- 0 until numberOfRecordsToGenerate) {
      putCompany(bos)
    }

    bos.close()
  }
}
