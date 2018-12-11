package za.co.absa.cobrix.cobol.parser.examples.generators

import java.io.{BufferedOutputStream, FileOutputStream}

import scodec.Attempt.Successful
import za.co.absa.cobrix.cobol.parser.decoders.BinaryUtils

import scala.util.Random

/**
  * This is a test data generator. The copybook for it is listed below.
  */
object TestDataGen2Accounts {

  val numberOfRecordsToGenerate = 1000000

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

  case class Company(companyName: String, companyId: String, address: String)

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

  val segments = Seq("S01L1", "S01L2")

  val companies = Seq(
    Company("ABCD Ltd.", "0039887123", "74 Lawn ave., New York"),
    Company("ECRONO", "0039567812", "123/B Prome str., Denver"),
    Company("ZjkLPj", "0034412331", "5574, Tokyo"),
    Company("Envision Inc.", "0039003991", "871A Forest ave., Toronto"),
    Company("Prime Bank", "0092317899", "1 Garden str., London"),
    Company("Pear GMBH.", "0002377771", "107 Labe str., Berlin"),
    Company("Beierbauh.", "0123330087", "901 Ztt, Munich"),
    Company("Johnson & D", "0039887123", "10 Sandton, Johannesburg"),
    Company("Roboco Inc.", "0039801988", "2 Park ave., Johannesburg"),
    Company("Beierbauh.", "0038903321", "2 G. str., Johannesburg"),
    Company("Dobry Pivivar", "0021213441", "74 Staromestka., Prague"),
    Company("Xingzhoug", "8822278911", "74 Qing ave., Beijing")
  )

  val firstNames = Seq(
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

  val lastNames = Seq(
      "Corle",
      "Mackinnon",
      "Mork",
      "Shapiro",
      "Boettcher",
      "Flatt",
      "Acuna",
      "Thorpe",
      "Riojas",
      "Lepe",
      "Maxim",
      "Gagliano",
      "Benally",
      "Ortego",
      "Winburn",
      "Sauve",
      "Concannon",
      "Newcombe",
      "Boehme",
      "Hisle",
      "Godfrey",
      "Wallingford",
      "Debow",
      "Bourke",
      "Deveau",
      "Batman",
      "Norgard",
      "Tumlin",
      "Celestin",
      "Brandis"
  )

  def main(args: Array[String]): Unit = {

    val numOfCompanies = companies.size

    val rand = new Random()

    val byteArray1: Array[Byte] = new Array[Byte](68)
    val byteArray2: Array[Byte] = new Array[Byte](64)

    val bos = new BufferedOutputStream(new FileOutputStream("COMP.DETAILS.SEP30.DATA.dat"))
    var i = 0
    while (i < numberOfRecordsToGenerate) {

      // SEGMENT 1 (root)

      val company = companies(rand.nextInt(numOfCompanies))

      // Generating random company id for join on company id to work as expected
      val companyId = s"${rand.nextInt(89999) + 10000}${rand.nextInt(89999) + 10000}"

      // XCOM header
      byteArray1(0) = 0
      byteArray1(1) = 0
      putShortToArray(byteArray1, 64, 2, 3) // record size = 64

      // Common values
      putStringToArray(byteArray1, segments(0), 4, 8) // 5
      putStringToArray(byteArray1, companyId, 9, 18) // 10

      // Static details
      putStringToArray(byteArray1, company.companyName, 19, 33) // 15
      putStringToArray(byteArray1, company.address, 34, 58) // 25

      val taxPayerType = rand.nextBoolean()
      val taxPayerNum = rand.nextInt(89999999) + 10000000

      if (taxPayerType) {
        putStringToArray(byteArray1, "A", 59, 59) // 1
        putStringToArray(byteArray1, taxPayerNum.toString, 60, 67) // 8
      }

      else {
        putStringToArray(byteArray1, "N", 59, 59) // 1
        putIntToArray(byteArray1, taxPayerNum, 60, 63) // 4
        byteArray1(64) = 0
        byteArray1(65) = 0
        byteArray1(66) = 0
        byteArray1(67) = 0
      }

      bos.write(byteArray1)
      i += 1

      // SEGMENT 1 (child)

      val numOfContacts = rand.nextInt(5)

      var j = 0

      while (j < numOfContacts && i < numberOfRecordsToGenerate) {
        // XCOM header
        byteArray2(0) = 0
        byteArray2(1) = 0
        putShortToArray(byteArray2, 60, 2, 3) // record size = 60

        // Common values
        putStringToArray(byteArray2, segments(1), 4, 8) // 5
        putStringToArray(byteArray2, companyId, 9, 18) // 10

        // Contacts
        val phoneNum = s"+(${rand.nextInt(920)+1}) ${rand.nextInt(899)+100} ${rand.nextInt(89)+10} ${rand.nextInt(89)+10}"
        putStringToArray(byteArray2, phoneNum, 19, 35) // 17

        val contactPerson = firstNames(rand.nextInt(firstNames.length)) + " " + lastNames(rand.nextInt(lastNames.length))
        putStringToArray(byteArray2, contactPerson, 36, 63) // 17

        bos.write(byteArray2)
        i += 1

        j += 1
      }

    }
    bos.close()
  }
}
