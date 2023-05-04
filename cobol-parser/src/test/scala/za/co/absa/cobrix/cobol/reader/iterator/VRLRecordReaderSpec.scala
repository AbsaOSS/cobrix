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

package za.co.absa.cobrix.cobol.reader.iterator

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.mock.{ByteStreamMock, RecordExtractorMock, RecordHeadersParserMock}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.headerparsers.{RecordHeaderParser, RecordHeaderParserRDW}
import za.co.absa.cobrix.cobol.reader.extractors.raw.{RawRecordContext, RawRecordExtractor}
import za.co.absa.cobrix.cobol.reader.parameters.{MultisegmentParameters, ReaderParameters}

class VRLRecordReaderSpec extends AnyWordSpec {
  private val copybook =
    """       01  RECORD.
           05  N       PIC 9(2).
           05  A       PIC X(2).
    """

  private val defaultExample = Array(
    0x00, 0x04, 0x00, 0x00, 0xF0, 0xF1, 0xF2, 0xF3,
    0x00, 0x05, 0x00, 0x00, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8
    ).map(_.toByte)

  private val customHeaderRecords = Array(
    0x04, 0x00, 0xF0, 0xF1, 0xF2, 0xF3,
    0x05, 0x00, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8
    ).map(_.toByte)

  "hasNext() / next()" should {
    "work for RDW-based files" in {
      val reader = getUseCase()

      assert(reader.hasNext)
      val (segment1, record1) = reader.next()
      assert(reader.hasNext)
      val (segment2, record2) = reader.next()
      assert(!reader.hasNext)

      assert(segment1.isEmpty)
      assert(segment2.isEmpty)
      assert(record1.length == 4)
      assert(record2.length == 5)
      assert(record1(0) == 0xF0.toByte)
      assert(record1(1) == 0xF1.toByte)
      assert(record1(2) == 0xF2.toByte)
      assert(record1(3) == 0xF3.toByte)
      assert(record2(0) == 0xF4.toByte)
      assert(record2(4) == 0xF8.toByte)
    }

    "work for custom header parser" in {
      val recordHeaderParser = new RecordHeadersParserMock
      recordHeaderParser.isHeaderDefinedInCopybook = true
      val reader = getUseCase(
        records = customHeaderRecords,
        recordHeaderParserOpt = Some(recordHeaderParser))

      assert(reader.hasNext)
      val (segment1, record1) = reader.next()
      assert(reader.hasNext)
      val (segment2, record2) = reader.next()
      assert(!reader.hasNext)

      assert(segment1.isEmpty)
      assert(segment2.isEmpty)
      assert(record1.length == 6)
      assert(record2.length == 7)
      assert(record1(0) == 0x04.toByte)
      assert(record1(1) == 0x00.toByte)
      assert(record1(2) == 0xF0.toByte)
      assert(record1(3) == 0xF1.toByte)
      assert(record2(0) == 0x05.toByte)
      assert(record2(6) == 0xF8.toByte)
    }

    "work for custom record extractor" in {
      val stream = new ByteStreamMock(customHeaderRecords)
      val context = RawRecordContext(0, stream, stream, null, null, null, "")

      val reader = getUseCase(
        records = customHeaderRecords,
        recordExtractor = Some(new RecordExtractorMock(context)))

      assert(reader.hasNext)
      val (segment1, record1) = reader.next()
      assert(reader.hasNext)
      val (segment2, record2) = reader.next()
      assert(!reader.hasNext)

      assert(segment1.isEmpty)
      assert(segment2.isEmpty)
      assert(record1.length == 4)
      assert(record2.length == 5)
      assert(record1(0) == 0xF0.toByte)
      assert(record1(1) == 0xF1.toByte)
      assert(record1(2) == 0xF2.toByte)
      assert(record1(3) == 0xF3.toByte)
      assert(record2(0) == 0xF4.toByte)
      assert(record2(4) == 0xF8.toByte)
    }

    "work for record length fields" when {
      "the length is an int data type" in {
        val copybookWithFieldLength =
          """       01  RECORD.
            05  LEN     PIC 9(4) BINARY.
            05  N       PIC 9(2).
            05  A       PIC X(2).
          """

        val records = Array(
          0x00, 0x06, 0xF0, 0xF1, 0xF2, 0xF3,
          0x00, 0x07, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8
          ).map(_.toByte)

        val reader = getUseCase(
          copybook = copybookWithFieldLength,
          records = records,
          lengthFieldExpression = Some("LEN"))

        assert(reader.hasNext)
        val (segment1, record1) = reader.next()
        assert(reader.hasNext)
        val (segment2, record2) = reader.next()
        assert(!reader.hasNext)

        assert(segment1.isEmpty)
        assert(segment2.isEmpty)
        assert(record1.length == 6)
        assert(record2.length == 7)
        assert(record1(0) == 0x00.toByte)
        assert(record1(1) == 0x06.toByte)
        assert(record1(2) == 0xF0.toByte)
        assert(record1(3) == 0xF1.toByte)
        assert(record2(0) == 0x00.toByte)
        assert(record2(6) == 0xF8.toByte)
      }

      "the length is an long data type" in {
        val copybookWithFieldLength =
          """       01  RECORD.
           05  LEN     PIC 9(10).
           05  N       PIC 9(2).
           05  A       PIC X(2).
          """

        val records = Array(
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF1, 0xF4, 0xF0, 0xF1, 0xF2, 0xF3,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF1, 0xF5, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8
          ).map(_.toByte)

        val reader = getUseCase(
          copybook = copybookWithFieldLength,
          records = records,
          lengthFieldExpression = Some("LEN"))

        assert(reader.hasNext)
        val (segment1, record1) = reader.next()
        assert(reader.hasNext)
        val (segment2, record2) = reader.next()
        assert(!reader.hasNext)

        assert(segment1.isEmpty)
        assert(segment2.isEmpty)
        assert(record1.length == 14)
        assert(record2.length == 15)
        assert(record1(10) == 0xF0.toByte)
        assert(record1(11) == 0xF1.toByte)
        assert(record1(12) == 0xF2.toByte)
        assert(record1(13) == 0xF3.toByte)
        assert(record2(10) == 0xF4.toByte)
        assert(record2(14) == 0xF8.toByte)
      }

      "throw an exception on a fraction type" in {
        val copybookWithFieldLength =
          """       01  RECORD.
           05  LEN     PIC 9(8)V99.
           05  N       PIC 9(2).
           05  A       PIC X(2).
          """

        val records = Array[Byte](0x00)

        val ex = intercept[IllegalStateException] {
          getUseCase(
            copybook = copybookWithFieldLength,
            records = records,
            lengthFieldExpression = Some("LEN"))
        }

        assert(ex.getMessage == "The record length field LEN must be an integral type.")
      }
    }

    "work with record length expressions" in {
      val copybookWithFieldLength =
        """       01  RECORD.
          05  LEN     PIC 9(4) BINARY.
          05  N       PIC 9(2).
          05  A       PIC X(2).
        """

      val records = Array(
        0x00, 0x07, 0xF0, 0xF1, 0xF2, 0xF3,
        0x00, 0x08, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8
        ).map(_.toByte)

      val reader = getUseCase(
        copybook = copybookWithFieldLength,
        records = records,
        lengthFieldExpression = Some("LEN - 1"))

      assert(reader.hasNext)
      val (segment1, record1) = reader.next()
      assert(reader.hasNext)
      val (segment2, record2) = reader.next()
      assert(!reader.hasNext)

      assert(segment1.isEmpty)
      assert(segment2.isEmpty)
      assert(record1.length == 6)
      assert(record2.length == 7)
      assert(record1(0) == 0x00.toByte)
      assert(record1(1) == 0x07.toByte)
      assert(record1(2) == 0xF0.toByte)
      assert(record1(3) == 0xF1.toByte)
      assert(record2(1) == 0x08.toByte)
      assert(record2(6) == 0xF8.toByte)
    }

    "filter by minimum record size" in {
      val reader = getUseCase(minimumRecordLength = 5)

      assert(reader.hasNext)
      val (segment1, record1) = reader.next()
      assert(!reader.hasNext)

      assert(segment1.isEmpty)
      assert(record1.length == 5)
      assert(record1(0) == 0xF4.toByte)
      assert(record1(4) == 0xF8.toByte)
    }

    "extract record segment ids if specified" in {
      val reader = getUseCase(segmentIdField = "N")

      assert(reader.hasNext)
      val (segment1, record1) = reader.next()
      assert(reader.hasNext)
      val (segment2, record2) = reader.next()
      assert(!reader.hasNext)

      assert(segment1.contains("1"))
      assert(segment2.contains("45"))
      assert(record1.length == 4)
      assert(record2.length == 5)
      assert(record1(0) == 0xF0.toByte)
      assert(record1(1) == 0xF1.toByte)
      assert(record1(2) == 0xF2.toByte)
      assert(record1(3) == 0xF3.toByte)
      assert(record2(0) == 0xF4.toByte)
      assert(record2(4) == 0xF8.toByte)
    }
  }

  def getUseCase(copybook: String = copybook,
                 records: Array[Byte] = defaultExample,
                 recordHeaderParserOpt: Option[RecordHeaderParser] = None,
                 recordExtractor: Option[RawRecordExtractor] = None,
                 lengthFieldExpression: Option[String] = None,
                 minimumRecordLength: Int = 1,
                 segmentIdField: String = ""): VRLRecordReader = {
    val stream = new ByteStreamMock(records)
    val parsedCopybook = CopybookParser.parseTree(copybook)
    val multisegmentParameters = if (segmentIdField.isEmpty)
      None
    else
      Some(MultisegmentParameters(segmentIdField, None, Nil, "", null, null))
    val readerParameters = ReaderParameters(
      minimumRecordLength = minimumRecordLength,
      lengthFieldExpression = lengthFieldExpression,
      multisegment = multisegmentParameters)
    val recordHeaderParser = recordHeaderParserOpt.getOrElse(new RecordHeaderParserRDW(true, 0, 0, 0))

    new VRLRecordReader(parsedCopybook,
                        stream,
                        readerParameters,
                        recordHeaderParser,
                        recordExtractor,
                        0,
                        0
                        )
  }

}
