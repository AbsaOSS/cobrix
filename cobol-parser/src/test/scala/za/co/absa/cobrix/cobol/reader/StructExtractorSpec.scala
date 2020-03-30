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

package za.co.absa.cobrix.cobol.reader

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}


class StructExtractorSpec extends FunSuite {
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
    0x00.toByte, 0x00.toByte, 0x2F.toByte,
    0x00.toByte, 0x08.toByte, 0xC5.toByte, 0xE7.toByte, 0xC1.toByte, 0xD4.toByte, 0xD7.toByte, 0xD3.toByte,
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


  class StructHandler extends RecordHandler[Any] {

    override def create(values: Array[Any], group: Group): Map[String, Any] = {
        (group.children zip values).map(t => t._1.name -> (t._2 match {
          case s: Array[Any] => s.toSeq
          case s => s
        })).toMap
    }

    override def toSeq(record: Any): Seq[Any] = {
      record.asInstanceOf[Map[String, Any]].values.toSeq
    }
  }

  test("Test simple struct generation") {
    val row = RowExtractors.extractRecord(copybook.ast, bytes, startOffset, handler = new StructHandler()).asInstanceOf[Seq[Map[String, Any]]]

    assert(row.head("COMPANY").asInstanceOf[Map[String, Any]]("SHORT_NAME") === "EXAMPLE4")
    assert(row.head("METADATA").asInstanceOf[Map[String, Any]]("ACCOUNT").asInstanceOf[Map[String, Any]]("ACCOUNT_DETAIL").asInstanceOf[Seq[Any]].length === 3)
  }

  test("Test simple struct generation with record id") {
    var row = RowExtractors.extractRecord(copybook.ast, bytes, startOffset, generateRecordId = true, handler = new StructHandler()).asInstanceOf[Seq[Map[String, Any]]]
    assert(row.asInstanceOf[Seq[Any]](0) == 0)
    assert(row.asInstanceOf[Seq[Any]](1) == 0)

    row = RowExtractors.extractRecord(copybook.ast, bytes, 123, generateRecordId = true, recordId = 1, handler = new StructHandler()).asInstanceOf[Seq[Map[String, Any]]]
    assert(row.asInstanceOf[Seq[Any]](0) == 0)
    assert(row.asInstanceOf[Seq[Any]](1) == 1)
  }

  test("Test simple struct generation with collapsing root") {
    val row = RowExtractors.extractRecord(copybook.ast, bytes, startOffset, policy = SchemaRetentionPolicy.CollapseRoot, handler = new StructHandler()).asInstanceOf[Seq[Map[String, Any]]]

    assert(row(1)("SHORT_NAME") === "EXAMPLE4")
    assert(row(2)("ACCOUNT").asInstanceOf[Map[String, Any]]("ACCOUNT_DETAIL").asInstanceOf[Seq[Any]].length === 3)
  }
}
