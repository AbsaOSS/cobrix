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

package za.co.absa.cobrix.cobol.processor.impl

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.mock.ByteStreamMock
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.processor.{CobolProcessingStrategy, CobolProcessor, CobolProcessorContext, RawRecordProcessor}
import za.co.absa.cobrix.cobol.reader.extractors.raw.{FixedRecordLengthRawRecordExtractor, TextFullRecordExtractor}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters

import java.io.ByteArrayOutputStream

class CobolProcessorToRdwSuite extends AnyWordSpec {
  private val copybook =
    """      01 RECORD.
      |         05  T     PIC X.
      |""".stripMargin

  "process" should {
    "process an input data stream into an output stream" in {
      val is = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val os = new ByteArrayOutputStream(10)
      val builder = CobolProcessor.builder
        .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
        .withCopybookContents(copybook)

      val processor = new RawRecordProcessor {
        override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
          val v = record.head
          Array[Byte]((v - 1).toByte, (v - 1).toByte)
        }
      }

      val count = builder.build().process(is, os)(processor)

      val outputArray = os.toByteArray

      assert(count == 4)
      assert(outputArray.length == 24)
      assert(outputArray.sameElements(
        Array(0, 0, 2, 0, -16, -16, 0, 0, 2, 0, -15, -15, 0, 0, 2, 0, -14, -14, 0, 0, 2, 0, -13, -13)
      ))
    }
  }
}
