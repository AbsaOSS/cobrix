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

package za.co.absa.cobrix.spark.cobol

import org.apache.spark.sql.Row
import org.scalatest.FunSuite
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

import scala.collection.mutable

class RowExtractorSpec extends FunSuite {
  val copyBookContents: String =
    """       01  RECORD.
      |           05  ID                        PIC S9(4)  COMP.
      |           05  COMPANY.
      |               10  SHORT-NAME            PIC X(10).
      |               10  COMPANY-ID-NUM        PIC 9(5) COMP-3.
      |               10  COMPANY-ID-STR
      |			         REDEFINES  COMPANY-ID-NUM PIC X(3).
      |           05  METADATA.
      |               10  CLIENTID              PIC X(15).
      |               10  REGISTRATION-NUM      PIC X(10).
      |               10  NUMBER-OF-ACCTS       PIC 9(03) COMP-3.
      |               10  ACCOUNT.
      |                   12  ACCOUNT-DETAIL    OCCURS 80
      |                                         DEPENDING ON NUMBER-OF-ACCTS.
      |                      15  ACCOUNT-NUMBER     PIC X(24).
      |                      15  ACCOUNT-TYPE-N     PIC 9(5) COMP-3.
      |                      15  ACCOUNT-TYPE-X     REDEFINES
      |                           ACCOUNT-TYPE-N  PIC X(3).
      |
      |""".stripMargin

  //  Data sample:
  //  JSON:
  //
  //  {
  //    "ID": 6,
  //    "COMPANY": {
  //      "SHORT_NAME": "EXAMPLE4",
  //      "COMPANY_ID_NUM": 0,
  //      "COMPANY_ID_STR": ""
  //    },
  //    "METADATA": {
  //      "CLIENTID": "",
  //      "REGISTRATION_NUM": "",
  //      "NUMBER_OF_ACCTS": 3,
  //      "ACCOUNT": {
  //      "ACCOUNT_DETAIL": [
  //    {
  //      "ACCOUNT_NUMBER": "000000000000002000400012",
  //      "ACCOUNT_TYPE_N": 0,
  //      "ACCOUNT_TYPE_X": ""
  //    },
  //    {
  //      "ACCOUNT_NUMBER": "000000000000003000400102",
  //      "ACCOUNT_TYPE_N": 1,
  //      "ACCOUNT_TYPE_X": ""
  //    },
  //    {
  //      "ACCOUNT_NUMBER": "000000005006001200301000",
  //      "ACCOUNT_TYPE_N": 2,
  //      "ACCOUNT_TYPE_X": ""
  //    }
  //      ]
  //    }
  //    }
  //  }
  //
  //  Binary:
  //  00 06 C5 E7 C1 D4 D7 D3 C5 F4 40 40 00 00
  //  0F 40 40 40 40 40 40 40 40 40 40 40 40 40 40 40
  //  40 40 40 40 40 40 40 40 40 40 00 3F F0 F0 F0 F0
  //  F0 F0 F0 F0 F0 F0 F0 F0 F0 F0 F2 F0 F0 F0 F4 F0
  //  F0 F0 F1 F2 00 00 0F F0 F0 F0 F0 F0 F0 F0 F0 F0
  //  F0 F0 F0 F0 F0 F3 F0 F0 F0 F4 F0 F0 F1 F0 F2 00
  //  00 1F F0 F0 F0 F0 F0 F0 F0 F0 F5 F0 F0 F6 F0 F0
  //  F1 F2 F0 F0 F3 F0 F1 F0 F0 F0 00 00 2F

  val bytes: Array[Byte]  = Array[Byte](
    0x00.toByte, 0x06.toByte, 0xC5.toByte, 0xE7.toByte, 0xC1.toByte, 0xD4.toByte, 0xD7.toByte, 0xD3.toByte,
    0xC5.toByte, 0xF4.toByte, 0x40.toByte, 0x40.toByte, 0x00.toByte, 0x00.toByte, 0x0F.toByte, 0x40.toByte,
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,
    0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte, 0x40.toByte,
    0x00.toByte, 0x3F.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0xF2.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF4.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0xF1.toByte, 0xF2.toByte, 0x00.toByte, 0x00.toByte, 0x0F.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF3.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF4.toByte,
    0xF0.toByte, 0xF0.toByte, 0xF1.toByte, 0xF0.toByte, 0xF2.toByte, 0x00.toByte, 0x00.toByte, 0x1F.toByte,
    0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0xF5.toByte, 0xF0.toByte, 0xF0.toByte, 0xF6.toByte, 0xF0.toByte, 0xF0.toByte, 0xF1.toByte, 0xF2.toByte,
    0xF0.toByte, 0xF0.toByte, 0xF3.toByte, 0xF0.toByte, 0xF1.toByte, 0xF0.toByte, 0xF0.toByte, 0xF0.toByte,
    0x00.toByte, 0x00.toByte, 0x2F.toByte
  )

  val copybook: Copybook = CopybookParser.parseTree(copyBookContents)
  val startOffset: Int = 0

  test("Test row extractor") {
    val row: Row = RowExtractors.extractRecord(copybook.ast, bytes, startOffset)
    // [[6,[EXAMPLE4,0,],[,,3,[Vector([000000000000002000400012,0,], [000000000000003000400102,1,], [000000005006001200301000,2,])]]]]
    val innerRow: Row = row(0).asInstanceOf[Row]

    // id
    assert(innerRow(0).asInstanceOf[Int] === 6)

    // short name
    assert(innerRow(1).asInstanceOf[Row](0).asInstanceOf[String] === "EXAMPLE4")

    // company id
    assert(innerRow(1).asInstanceOf[Row](1).asInstanceOf[Int] === 0)

    // number of accounts
    assert(innerRow(2).asInstanceOf[Row](2).asInstanceOf[Int] === 3)

    // account detail
    val accounts: Array[Any] = innerRow(2).asInstanceOf[Row](3).asInstanceOf[Row](0).asInstanceOf[Array[Any]]
    val account: Row = accounts(0).asInstanceOf[Row]

    // account number
    assert(account(0).asInstanceOf[String] === "000000000000002000400012")

    //account type
    assert(account(1).asInstanceOf[Int] === 0)
  }
}
