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
import za.co.absa.cobrix.cobol.base.BinaryFileFixture
import za.co.absa.cobrix.cobol.mock.ByteStreamMock
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters

import java.io.ByteArrayOutputStream
import java.nio.file.Paths

class CobolProcessorBuilderSuite extends AnyWordSpec with BinaryFileFixture {
  private val copybook =
    """      01 RECORD.
      |         05  T     PIC X.
      |""".stripMargin

  "process" should {
    "process an input data stream into an output stream" in {
      val is = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val os = new ByteArrayOutputStream(10)
      val builder = CobolProcessor.builder
        .withCopybookContents(copybook)

      val processor = new RawRecordProcessor {
        override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
          record.map(v => (v - 1).toByte)
        }
      }

      val count = builder.build().process(is, os)(processor)

      val outputArray = os.toByteArray

      assert(count == 4)
      assert(outputArray.head == -16)
      assert(outputArray(1) == -15)
      assert(outputArray(2) == -14)
      assert(outputArray(3) == -13)
    }
  }

  "load and save" should {
    "process files as expected" in {
      withTempDirectory("cobol_processor") { tempDir =>
        val inputFile = Paths.get(tempDir, "input.dat").toString
        val outputFile = Paths.get(tempDir, "output.dat").toString

        writeBinaryFile(inputFile, Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))

        val count = CobolProcessor.builder
          .withCopybookContents(copybook)
          .withRecordProcessor(new RawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .load(inputFile)
          .save(outputFile)

        val outputArray = readBinaryFile(outputFile)

        assert(count == 4)
        assert(outputArray.head == -16)
        assert(outputArray(1) == -15)
        assert(outputArray(2) == -14)
        assert(outputArray(3) == -13)
      }
    }

    "support file_start_offset when processing files" in {
      withTempDirectory("cobol_processor") { tempDir =>
        val inputFile = Paths.get(tempDir, "input.dat").toString
        val outputFile = Paths.get(tempDir, "output.dat").toString

        writeBinaryFile(inputFile, Array(0x07, 0x07, 0x07, 0xF1, 0xF2, 0xF3, 0xF4, 0x08, 0x08).map(_.toByte))

        val count = CobolProcessor.builder
          .withCopybookContents(copybook)
          .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
          .withRecordProcessor(new RawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .option("file_start_offset", "3")
          .option("file_end_offset", "2")
          .load(inputFile)
          .save(outputFile)

        val outputData = readBinaryFile(outputFile)

        assert(count == 4)
        assert(outputData.sameElements(
          Array(0, 1, 0, 0, -16, 0, 1, 0, 0, -15, 0, 1, 0, 0, -14, 0, 1, 0, 0, -13).map(_.toByte)
        ))
      }
    }
  }

  "getCobolSchema" should {
    "return the schema of the copybook provided" in {
      val builder = CobolProcessor.builder
        .withCopybookContents(copybook)

      val cobolSchema = builder.getCobolSchema(ReaderParameters())

      assert(cobolSchema.copybook.ast.children.length == 1)
    }
  }

  "getReaderParameters" should {
    "return a reader according to passed options" in {
      val builder = CobolProcessor.builder
        .withCopybookContents(copybook)
        .option("record_format", "D")

      assert(builder.getReaderParameters.recordFormat == RecordFormat.AsciiText)
      assert(builder.getReaderParameters.options.contains("record_format"))
      assert(builder.getOptions.contains("record_format"))
    }
  }
}
