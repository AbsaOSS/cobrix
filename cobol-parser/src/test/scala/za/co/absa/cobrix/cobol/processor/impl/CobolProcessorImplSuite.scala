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
import za.co.absa.cobrix.cobol.processor.CobolProcessor
import za.co.absa.cobrix.cobol.reader.extractors.raw.{FixedRecordLengthRawRecordExtractor, TextFullRecordExtractor}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters

class CobolProcessorImplSuite extends AnyWordSpec {
  private val copybook =
    """      01 RECORD.
      |         05  T     PIC X.
      |""".stripMargin

  "getRecordExtractor" should {
    "work for an fixed-record-length files" in {
      val stream = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val processor = CobolProcessor.builder(copybook).build().asInstanceOf[CobolProcessorImpl]

      val ext = processor.getRecordExtractor(ReaderParameters(recordLength = Some(2), options = Map("test" -> "option")), stream)

      assert(ext.isInstanceOf[FixedRecordLengthRawRecordExtractor])

      assert(ext.hasNext)
      assert(ext.next().sameElements(Array(0xF1, 0xF2).map(_.toByte)))
      assert(ext.next().sameElements(Array(0xF3, 0xF4).map(_.toByte)))
      assert(!ext.hasNext)
    }

    "work for an variable-record-length files" in {
      val stream = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val processor = CobolProcessor.builder(copybook).build().asInstanceOf[CobolProcessorImpl]

      val ext = processor.getRecordExtractor(ReaderParameters(
        recordFormat = RecordFormat.VariableLength,
        isText = true
      ), stream)

      assert(ext.isInstanceOf[TextFullRecordExtractor])
    }

    "throw an exception on a non-supported record format for processing" in {
      val stream = new ByteStreamMock(Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte))
      val processor = CobolProcessor.builder(copybook).build().asInstanceOf[CobolProcessorImpl]

      val ex = intercept[IllegalArgumentException] {
        processor.getRecordExtractor(ReaderParameters(
          recordFormat = RecordFormat.VariableLength,
          isRecordSequence = true
        ), stream)
      }

      assert(ex.getMessage.contains("Cannot create a record extractor for the given reader parameters."))
    }
  }
}
