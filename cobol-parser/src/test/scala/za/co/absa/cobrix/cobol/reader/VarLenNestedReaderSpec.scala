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

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.iterator.{VarLenHierarchicalIterator, VarLenNestedIterator}
import za.co.absa.cobrix.cobol.reader.memorystream.{TestByteStream, TestStringStream}
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

class VarLenNestedReaderSpec extends AnyWordSpec {
  private val fixedLengthDataExample = Array(
    0xF1, 0xF2, 0xF3, 0xF4, // record 0
    0xF5, 0xF6, 0xF7, 0xF8, // record 1
    0xF9, 0xF0, 0xF1, 0xF2, // record 2
    0xF3, 0xF4, 0xF5, 0xF6  // record 3
  ).map(_.toByte)

  private val rdwLittleEndianExample = Array(
    0x04, 0x00, 0x00, 0x00, // RDW header
    0xF0, 0xF1, 0xF2, 0xF3, // record 0
    0x03, 0x00, 0x00, 0x00, // RDW header
    0xF4, 0xF5, 0xF6,       // record 1
    0x04, 0x00, 0x00, 0x00, // RDW header
    0xF7, 0xF8, 0xF9, 0xF0  // record 2
    ).map(_.toByte)

  private val rdwBigEndianExample = Array(
    0x00, 0x04, 0x00, 0x00, // RDW header
    0xF0, 0xF1, 0xF2, 0xF3, // record 0
    0x00, 0x03, 0x00, 0x00, // RDW header
    0xF4, 0xF5, 0xF6, // record 1
    0x00, 0x04, 0x00, 0x00, // RDW header
    0xF7, 0xF8, 0xF9, 0xF0 // record 2
    ).map(_.toByte)

  "generateIndex" should {
    "work for fixed length flat files" in {
      val (reader, dataStream, headerStream) = getFlatUseCase(fixedLengthDataExample)

      val index = reader.generateIndex(dataStream, headerStream, 0, isRdwBigEndian = false)

      assert(index.length == 3)
      assert(index(0).offsetFrom == 0)
      assert(index(0).offsetTo == 4)
      assert(index(0).recordIndex == 0)
      assert(index(1).offsetFrom == 4)
      assert(index(1).offsetTo == 8)
      assert(index(1).recordIndex == 1)

      assert(reader.getCobolSchema.copybook.getRecordSize == 4)
      assert(reader.getRecordSize == 4)
      assert(reader.isIndexGenerationNeeded)
    }

    "work for fixed multi-copybook length flat files" in {
      val (reader, dataStream, headerStream) = getFlatUseCase(fixedLengthDataExample, multiCopyBook = true)

      val index = reader.generateIndex(dataStream, headerStream, 0, isRdwBigEndian = false)

      assert(index.length == 7)
      assert(index(0).offsetFrom == 0)
      assert(index(0).offsetTo == 2)
      assert(index(0).recordIndex == 0)
      assert(index(1).offsetFrom == 2)
      assert(index(1).offsetTo == 4)
      assert(index(1).recordIndex == 1)
    }

    "work for files with little-endian RDWs" in {
      val (reader, dataStream, headerStream) = getFlatUseCase(rdwLittleEndianExample, hasRDW = true)

      val index = reader.generateIndex(dataStream, headerStream, 0, isRdwBigEndian = false)

      assert(index.length == 2)
      assert(index(0).offsetFrom == 0)
      assert(index(0).offsetTo == 8)
      assert(index(0).recordIndex == 0)
      assert(index(1).offsetFrom == 8)
      assert(index(1).offsetTo == -1)
      assert(index(1).recordIndex == 1)
    }

    "work for files with big-endian RDWs" in {
      val (reader, dataStream, headerStream) = getFlatUseCase(rdwBigEndianExample, hasRDW = true, isRdwBigEndian = true)

      val index = reader.generateIndex(dataStream, headerStream, 0, isRdwBigEndian = true)

      assert(index.length == 2)
      assert(index(0).offsetFrom == 0)
      assert(index(0).offsetTo == 8)
      assert(index(0).recordIndex == 0)
      assert(index(1).offsetFrom == 8)
      assert(index(1).offsetTo == -1)
      assert(index(1).recordIndex == 1)
    }

    "work for files with a custom record header parser" in {
      val recordHeaderParserClass = Some("za.co.absa.cobrix.cobol.mock.RecordHeadersParserMock")

      val data = Array(
        0x04, 0x00,             // Header
        0xF0, 0xF1, 0xF2, 0xF3, // record 0
        0x03, 0x00,             // Header
        0xF4, 0xF5, 0xF6,       // record 1
        0x04, 0x00,             // RDW header
        0xF7, 0xF8, 0xF9, 0xF0  // record 2
        ).map(_.toByte)
      val (reader, dataStream, headerStream) = getFlatUseCase(data, recordHeaderParser = recordHeaderParserClass)

      val index = reader.generateIndex(dataStream, headerStream, 0, isRdwBigEndian = true)

      assert(index.length == 2)
      assert(index(0).offsetFrom == 0)
      assert(index(0).offsetTo == 6)
      assert(index(0).recordIndex == 0)
      assert(index(1).offsetFrom == 6)
      assert(index(1).offsetTo == -1)
      assert(index(1).recordIndex == 1)
    }

    "work for hierarchical files" in {
      val (reader, dataStream, headerStream) = getHierarchicalUseCase

      val index = reader.generateIndex(dataStream, headerStream, 0, isRdwBigEndian = false)

      assert(index.length == 2)
      assert(index(0).offsetFrom == 0)
      assert(index(0).offsetTo == 5)
      assert(index(0).recordIndex == 0)
      assert(index(1).offsetFrom == 5)
      assert(index(1).offsetTo == -1)
      assert(index(1).recordIndex == 1)
    }
  }

  "getRecordIterator" should {
    "work for nested records" in {
      val (reader, dataStream, headerStream) = getFlatUseCase(fixedLengthDataExample)

      val it = reader.getRecordIterator(dataStream, headerStream, 0, 0, 0)

      assert(it.isInstanceOf[VarLenNestedIterator[scala.Array[Any]]])
    }

    "work for hierarchical records" in {
      val (reader, dataStream, headerStream) = getHierarchicalUseCase

      val it = reader.getRecordIterator(dataStream, headerStream, 0, 0, 0)

      assert(it.isInstanceOf[VarLenHierarchicalIterator[scala.Array[Any]]])
    }
  }

  def getFlatUseCase(data: Array[Byte],
                     recordFormat: RecordFormat = RecordFormat.VariableLength,
                     recordHeaderParser: Option[String] = None,
                     hasRDW: Boolean = false,
                     isRdwBigEndian: Boolean = false,
                     multiCopyBook: Boolean = false
                    ): (VarLenNestedReader[scala.Array[Any]], SimpleStream, SimpleStream) = {
    val copybookContents: String =
      """       01  RECORD.
        |          05  A PIC X(2).
        |          05  B PIC X(2).
        |      |""".stripMargin

    val copybookContents1: String =
      """       01  RECORD1.
        |          05  A PIC X(2).
        |      |""".stripMargin

    val copybookContents2: String =
      """       01  RECORD2.
        |          05  B PIC X(2).
        |      |""".stripMargin

    val readerProperties = za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters(
      recordFormat = recordFormat,
      recordHeaderParser = recordHeaderParser,
      isRecordSequence = hasRDW,
      isRdwBigEndian = isRdwBigEndian,
      isIndexGenerationNeeded = true,
      inputSplitRecords = Some(1)
    )

    val copybooks = if (multiCopyBook) {
      Seq(copybookContents1, copybookContents2)
    } else {
      Seq(copybookContents)
    }

    val dataStream = new TestByteStream(data)
    val headerStreat = new TestByteStream(data)

    val reader = new VarLenNestedReader[scala.Array[Any]](
      copybooks, readerProperties, new SimpleRecordHandler)

    (reader, dataStream, headerStreat)
  }

  def getHierarchicalUseCase: (VarLenNestedReader[scala.Array[Any]], SimpleStream, SimpleStream) = {
    val copybookContents: String =
      """       01  RECORD.
        |          05  SEGMENT   PIC X(1).
        |          05  PARENT.
        |            10 A PIC X(3).
        |          05  CHILD REDEFINES PARENT.
        |            10 B PIC 9(2).
        |      |""".stripMargin

    val multiSegment = za.co.absa.cobrix.cobol.reader.parameters.MultisegmentParameters(
      segmentIdField = "SEGMENT",
      segmentIdFilter = None,
      segmentLevelIds = Nil,
      segmentIdPrefix = "A",
      segmentIdRedefineMap = Map("P" -> "PARENT", "C" -> "CHILD"),
      fieldParentMap = Map("CHILD" -> "PARENT"),
      None
    )

    val readerProperties = za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters(
      isEbcdic = false,
      isText = true,
      recordFormat = RecordFormat.AsciiText,
      multisegment = Option(multiSegment),
      isIndexGenerationNeeded = true,
      inputSplitRecords = Some(1)
      )

    val data = "P123\nP456\nC78\nC90\nP876\n"
    val dataStream = new TestStringStream(data)
    val headerStream = new TestStringStream(data)

    val reader = new VarLenNestedReader[scala.Array[Any]](
      Seq(copybookContents), readerProperties, new SimpleRecordHandler)

    (reader, dataStream, headerStream)
  }

}
