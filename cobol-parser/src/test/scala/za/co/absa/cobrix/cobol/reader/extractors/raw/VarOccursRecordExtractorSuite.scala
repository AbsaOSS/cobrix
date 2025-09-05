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

package za.co.absa.cobrix.cobol.reader.extractors.raw

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.reader.memorystream.TestByteStream
import za.co.absa.cobrix.cobol.reader.recordheader.{RecordHeaderDecoderBdw, RecordHeaderDecoderRdw, RecordHeaderParametersFactory}

class VarOccursRecordExtractorSuite extends AnyWordSpec {
  "variable occurs record extractor" should {
    "be able to process data without variable occurs" in {
      val copybookContent =
        """      01 RECORD.
                02 F1    PIC 9(1).
                02 A1    PIC X(1) OCCURS 2 TIMES.
                02 GRP.
                  03 F2  PIC X(1).
          """
      val copybook = CopybookParser.parseTree(copybookContent)
      val recordData = Array(
        // Records having array of 2 elements
        0xF0, 0xF1, 0xF2, 0xF9,
        0xF1, 0xF3, 0xF4, 0xF9,
        0xF5, 0xF5, 0xF6, 0xF9
      ).map(_.toByte)

      val ibs = new TestByteStream(recordData)
      val hbs = new TestByteStream(recordData)

      val rc = RawRecordContext.builder(0, ibs, hbs, copybook).build()

      val extractor = new VarOccursRecordExtractor(rc)

      assert(extractor.hasNext)
      assert(extractor.offset == 0)

      val record0 = extractor.next()
      assert(extractor.offset == 4)
      assert(record0.length == 4)
      assert(record0.sameElements(Array(0xF0, 0xF1, 0xF2, 0xF9).map(_.toByte)))

      val record1 = extractor.next()
      assert(extractor.offset == 8)
      assert(record1.length == 4)
      assert(record1.sameElements(Array(0xF1, 0xF3, 0xF4, 0xF9).map(_.toByte)))

      val record2 = extractor.next()
      assert(extractor.offset == 12)
      assert(record2.length == 4)
      assert(record2.sameElements(Array(0xF5, 0xF5, 0xF6, 0xF9).map(_.toByte)))

      assert(!extractor.hasNext)
    }

    "be able to process data with variable occurs in the middle" in {
      val copybookContent =
        """      01 RECORD.
                02 F1    PIC 9(1).
                02 A1    PIC X(1) OCCURS 0 TO 5 TIMES DEPENDING ON F1.
                02 GRP.
                  03 F2  PIC X(1).
          """
      val copybook = CopybookParser.parseTree(copybookContent)
      val recordData = Array(
        0xF0, 0xF9,                                // an empty array
        0xF1, 0xF0, 0xF9,                          // an array with 1 element
        0xF6, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF9   // an array with 5 elements, but the dependee says 6 (bigger than max size)
      ).map(_.toByte)

      val ibs = new TestByteStream(recordData)
      val hbs = new TestByteStream(recordData)

      val rc = RawRecordContext.builder(0, ibs, hbs, copybook).build()


      val extractor = new VarOccursRecordExtractor(rc)

      assert(extractor.hasNext)
      assert(extractor.offset == 0)

      val record0 = extractor.next()
      assert(extractor.offset == 2)
      assert(record0.length == 2)
      assert(record0.sameElements(Array(0xF0, 0xF9).map(_.toByte)))

      val record1 = extractor.next()
      assert(extractor.offset == 5)
      assert(record1.length == 3)
      assert(record1.sameElements(Array(0xF1, 0xF0, 0xF9).map(_.toByte)))

      val record2 = extractor.next()
      assert(extractor.offset == 12)
      assert(record2.length == 7)
      assert(record2.sameElements(Array(0xF6, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF9).map(_.toByte)))

      assert(!extractor.hasNext)
    }
  }

  "be able to process data with groups in variable occurs" in {
    val copybookContent =
      """      01 RECORD.
            02 F1    PIC X(1).
            02 GRP1  OCCURS 0 TO 5 TIMES DEPENDING ON F1.
              03 A1 PIC X(1).
              03 A2 PIC X(1) REDEFINES A1.
            02 GRP.
              03 F2  PIC X(1).
      """

    val occursHandlers = Map[String, Map[String, Int]](
      "GRP1" -> Map(
        "A" -> 0,
        "B" -> 1,
        "C" -> 5
      )
    )

    val copybook = CopybookParser.parseTree(copybookContent, occursHandlers = occursHandlers)
    val recordData = Array(
      0xC1, 0xF9,                              // an empty array. Dependee = 'A'
      0xC2, 0xF0, 0xF9,                        // an array with 1 element. Dependee = 'B'
      0xC3, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF9 // an array with 5 elements. Dependee = 'C'
    ).map(_.toByte)

    val ibs = new TestByteStream(recordData)
    val hbs = new TestByteStream(recordData)

    val rc = RawRecordContext.builder(0, ibs, hbs, copybook).build()

    val extractor = new VarOccursRecordExtractor(rc)

    assert(extractor.hasNext)
    assert(extractor.offset == 0)

    val record0 = extractor.next()
    assert(extractor.offset == 2)
    assert(record0.length == 2)
    assert(record0.sameElements(Array(0xC1, 0xF9).map(_.toByte)))

    val record1 = extractor.next()
    assert(extractor.offset == 5)
    assert(record1.length == 3)
    assert(record1.sameElements(Array(0xC2, 0xF0, 0xF9).map(_.toByte)))

    val record2 = extractor.next()
    assert(extractor.offset == 12)
    assert(record2.length == 7)
    assert(record2.sameElements(Array(0xC3, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF9).map(_.toByte)))

    assert(!extractor.hasNext)
  }
}
