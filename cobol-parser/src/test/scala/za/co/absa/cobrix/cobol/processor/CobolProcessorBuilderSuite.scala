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

package za.co.absa.cobrix.cobol.processor

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.mock.ByteStreamMock
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.extractors.raw.{FixedRecordLengthRawRecordExtractor, TextFullRecordExtractor}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters

import java.io.ByteArrayOutputStream

class CobolProcessorBuilderSuite extends AnyWordSpec {
  private val copybook =
    """      01 RECORD.
      |         05  T     PIC X.
      |""".stripMargin
  "process" should {
    "process an input data stream into an output stream" in {
      val is = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val os = new ByteArrayOutputStream(10)
      val builder = CobolProcessor.builder(copybook)

      val processor = new RawRecordProcessor {
        override def processRecord(copybook: Copybook, options: Map[String, String], record: Array[Byte], offset: Long): Array[Byte] = {
          record.map(v => (v - 1).toByte)
        }
      }

      builder.build().process(is, os)(processor)

      val outputArray = os.toByteArray

      assert(outputArray.head == -16)
      assert(outputArray(1) == -15)
      assert(outputArray(2) == -14)
      assert(outputArray(3) == -13)
    }
  }

  "getCobolSchema" should {
    "return the schema of the copybook provided" in {
      val builder = CobolProcessor.builder(copybook)

      val cobolSchema = builder.getCobolSchema(ReaderParameters())

      assert(cobolSchema.copybook.ast.children.length == 1)
    }
  }

  "getReaderParameters" should {
    "return a reader according to passed options" in {
      val builder = CobolProcessor.builder(copybook)
        .option("record_format", "D")

      assert(builder.getReaderParameters.recordFormat == RecordFormat.AsciiText)
      assert(builder.getReaderParameters.options.contains("record_format"))
      assert(builder.getOptions.contains("record_format"))
    }
  }

  "getRecordExtractor" should {
    "work for an fixed-record-length files" in {
      val stream = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val builder = CobolProcessor.builder(copybook)

      val ext = builder.getRecordExtractor(ReaderParameters(recordLength = Some(2)), stream)

      assert(ext.isInstanceOf[FixedRecordLengthRawRecordExtractor])

      assert(ext.hasNext)
      assert(ext.next().sameElements(Array(0xF1, 0xF2).map(_.toByte)))
      assert(ext.next().sameElements(Array(0xF3, 0xF4).map(_.toByte)))
      assert(!ext.hasNext)
    }

    "work for an variable-record-length files" in {
      val stream = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val builder = CobolProcessor.builder(copybook)

      val ext = builder.getRecordExtractor(ReaderParameters(
        recordFormat = RecordFormat.VariableLength,
        isText = true
      ), stream)

      assert(ext.isInstanceOf[TextFullRecordExtractor])
    }

    "throw an exception on a non-supported record format for processing" in {
      val stream = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val builder = CobolProcessor.builder(copybook)

      val ex = intercept[IllegalArgumentException] {
        builder.getRecordExtractor(ReaderParameters(
          recordFormat = RecordFormat.VariableLength,
          isRecordSequence = true
        ), stream)
      }

      assert(ex.getMessage.contains("Cannot create a record extractor for the given reader parameters."))
    }
  }

}
