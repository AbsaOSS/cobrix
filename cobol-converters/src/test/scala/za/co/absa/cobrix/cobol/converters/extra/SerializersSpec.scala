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

package za.co.absa.cobrix.cobol.converters.extra

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.extractors.record.{RecordHandler, RecordExtractors}


class SerializersSpec extends AnyFunSuite {
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


  class StructHandler extends RecordHandler[Map[String, Any]] {
    override def create(values: Array[Any], group: Group): Map[String, Any] = {
        (group.children zip values).map(t => t._1.name -> (t._2 match {
          case s: Array[Any] => s.toSeq
          case s => s
        })).toMap
    }

    override def toSeq(record: Map[String, Any]): Seq[Any] = {
      record.values.toSeq
    }
  }


  test("Test JSON generation") {
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
    import com.fasterxml.jackson.module.scala.ScalaObjectMapper

    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)

    val handler = new StructHandler()
    val row = RecordExtractors.extractRecord(copybook.ast, bytes, startOffset, handler = handler)
    val record = handler.create(row.toArray, copybook.ast)

    val json = mapper.writeValueAsString(record)
    assert(json === """{"RECORD":{"ID":6,"COMPANY":{"SHORT_NAME":"EXAMPLE4","COMPANY_ID_NUM":0,"COMPANY_ID_STR":""},"METADATA":{"CLIENTID":"","REGISTRATION_NUM":"","NUMBER_OF_ACCTS":3,"ACCOUNT":{"ACCOUNT_DETAIL":[{"ACCOUNT_NUMBER":"000000000000002000400012","ACCOUNT_TYPE_N":0,"ACCOUNT_TYPE_X":""},{"ACCOUNT_NUMBER":"000000000000003000400102","ACCOUNT_TYPE_N":1,"ACCOUNT_TYPE_X":""},{"ACCOUNT_NUMBER":"000000005006001200301000","ACCOUNT_TYPE_N":2,"ACCOUNT_TYPE_X":""}]}}}}""")
  }


  test("Test XML generation") {
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
    import com.fasterxml.jackson.module.scala.ScalaObjectMapper
    import com.fasterxml.jackson.databind.SerializationFeature
    import com.fasterxml.jackson.dataformat.xml.XmlMapper

    val mapper = new XmlMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false)

    val handler = new StructHandler()
    val row = RecordExtractors.extractRecord(copybook.ast, bytes, startOffset, handler = handler)
    val record = handler.create(row.toArray, copybook.ast)

    val xml = mapper.writer().withRootName("COBOL-RECORD").writeValueAsString(record)
    assert(xml === """<COBOL-RECORD><RECORD><ID>6</ID><COMPANY><SHORT_NAME>EXAMPLE4</SHORT_NAME><COMPANY_ID_NUM>0</COMPANY_ID_NUM><COMPANY_ID_STR></COMPANY_ID_STR></COMPANY><METADATA><CLIENTID></CLIENTID><REGISTRATION_NUM></REGISTRATION_NUM><NUMBER_OF_ACCTS>3</NUMBER_OF_ACCTS><ACCOUNT><ACCOUNT_DETAIL><ACCOUNT_NUMBER>000000000000002000400012</ACCOUNT_NUMBER><ACCOUNT_TYPE_N>0</ACCOUNT_TYPE_N><ACCOUNT_TYPE_X></ACCOUNT_TYPE_X></ACCOUNT_DETAIL><ACCOUNT_DETAIL><ACCOUNT_NUMBER>000000000000003000400102</ACCOUNT_NUMBER><ACCOUNT_TYPE_N>1</ACCOUNT_TYPE_N><ACCOUNT_TYPE_X></ACCOUNT_TYPE_X></ACCOUNT_DETAIL><ACCOUNT_DETAIL><ACCOUNT_NUMBER>000000005006001200301000</ACCOUNT_NUMBER><ACCOUNT_TYPE_N>2</ACCOUNT_TYPE_N><ACCOUNT_TYPE_X></ACCOUNT_TYPE_X></ACCOUNT_DETAIL></ACCOUNT></METADATA></RECORD></COBOL-RECORD>""")
  }


  val flatCopyBookContents: String =
    """       01  RECORD.
      |           05  ID                   PIC S9(4)  COMP.
      |           05  SHORT-NAME           PIC X(10).
      |           05  COMPANY-ID-NUM       PIC 9(5) COMP-3.
      |           05  COMPANY-ID-STR
      |			         REDEFINES  COMPANY-ID-NUM PIC X(3).
      |           05  CLIENTID             PIC X(15).
      |           05  REGISTRATION-NUM     PIC X(10).
      |           05  NUMBER-OF-ACCTS      PIC 9(03) COMP-3.
      |           05  ACCOUNT.
      |           05  ACCOUNT-NUMBER-1     PIC X(24).
      |           05  ACCOUNT-TYPE-N-1     PIC 9(5) COMP-3.
      |           05  ACCOUNT-TYPE-X-1
      |              REDEFINES ACCOUNT-TYPE-N-1  PIC X(3).
      |           05  ACCOUNT-NUMBER-2     PIC X(24).
      |           05  ACCOUNT-TYPE-N-2     PIC 9(5) COMP-3.
      |           05  ACCOUNT-TYPE-X-2
      |              REDEFINES ACCOUNT-TYPE-N-2  PIC X(3).
      |           05  ACCOUNT-NUMBER-3     PIC X(24).
      |           05  ACCOUNT-TYPE-N-3     PIC 9(5) COMP-3.
      |           05  ACCOUNT-TYPE-X-3     REDEFINES
      |                           ACCOUNT-TYPE-N-3  PIC X(3).
      |
      |""".stripMargin

  val flatCopybook: Copybook = CopybookParser.parseTree(flatCopyBookContents)

  test("Test CSV generation") {
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
    import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
    import com.fasterxml.jackson.dataformat.csv.CsvMapper
    import com.fasterxml.jackson.dataformat.csv.CsvGenerator
    import com.fasterxml.jackson.dataformat.csv.CsvSchema

    val mapper = new CsvMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true)

    val handler = new StructHandler()
    val row = RecordExtractors.extractRecord(flatCopybook.ast, bytes, startOffset, policy=SchemaRetentionPolicy.CollapseRoot, handler = handler)

    // In Scala 2.13 hash maps sort columns differently, so need to test this method differently as well
    //val csv = mapper.writeValueAsString(row)
    //assert(csv.stripLineEnd === """"",6,0,"","","","","EXAMPLE4","000000000000003000400102",2,"","000000000000002000400012",3,1,"000000005006001200301000",0""")
  }

}
