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

class FixedBlockRawRecordExtractorSuite extends AnyWordSpec {
  private val copybookContent =
    """      01 RECORD.
          02 X PIC X(2).
    """
  private val copybook = CopybookParser.parseTree(copybookContent)

  private val fbParams = FixedBlockParameters(None, Some(2), None)

  "fixed block fixed length records" should {
    "be able to read a FB file that has no data" in {
      val rc = getRawRecordContext(Array[Byte]())

      val extractor = new FixedBlockRawRecordExtractor(rc, fbParams)

      assert(!extractor.hasNext)

      intercept[NoSuchElementException] {
        extractor.next()
      }
    }

    "be able to read a FB file that has an incomplete record" in {
      val rc = getRawRecordContext(Array[Byte](0xF0.toByte))

      val extractor = new FixedBlockRawRecordExtractor(rc, fbParams)

      assert(extractor.hasNext)

      val r0 = extractor.next()

      assert(r0.length == 1)
      assert(r0.head == 0xF0.toByte)

      intercept[NoSuchElementException] {
        extractor.next()
      }
    }

    "be able to read a FB file that has one record per block" in {
      val rc = getRawRecordContext(1)

      val extractor = new FixedBlockRawRecordExtractor(rc, FixedBlockParameters(Some(2), None, Some(1)))

      assert(extractor.hasNext)

      val r0 = extractor.next()
      assert(r0.length == 2)
      assert(r0.head == 0xF0.toByte)
      assert(r0(1) == 0xF0.toByte)

      assert(extractor.next().head == 0xF1.toByte)
      assert(extractor.next().head == 0xF2.toByte)
      assert(!extractor.hasNext)
    }

    "be able to read a VBVR file that has multiple records per block" in {
      val rc = getRawRecordContext(3)

      val extractor = new FixedBlockRawRecordExtractor(rc, FixedBlockParameters(None, None, Some(3)))

      assert(extractor.hasNext)

      val r0 = extractor.next()
      assert(r0.length == 2)
      assert(r0.head == 0xF0.toByte)
      assert(r0(1) == 0xF0.toByte)

      assert(extractor.next().head == 0xF1.toByte)
      assert(extractor.next().head == 0xF2.toByte)
      assert(extractor.next().head == 0xF3.toByte)
      assert(extractor.next().head == 0xF4.toByte)
      assert(extractor.next().head == 0xF5.toByte)
      assert(extractor.next().head == 0xF6.toByte)
      assert(extractor.next().head == 0xF7.toByte)
      assert(extractor.next().head == 0xF8.toByte)
      assert(!extractor.hasNext)
    }

    "allow neither block length nor records per block to be specified" in {
      val fb = FixedBlockParameters(Some(1), None, None)

      assert(fb.blockLength.isEmpty)
      assert(fb.recordsPerBlock.isEmpty)
    }
  }

  "failures" should {
    "throw an exception when both block length and records per block are specified" in {
      val fb = FixedBlockParameters(Some(1), Some(1), Some(1))

      val ex = intercept[IllegalArgumentException] {
        FixedBlockParameters.validate(fb)
      }

      assert(ex.getMessage.contains("FB record format requires either block length or number records per block to be specified, but not both."))
    }

    "throw an exception when record length is zero" in {
      val fb = FixedBlockParameters(Some(0), Some(1), None)

      val ex = intercept[IllegalArgumentException] {
        FixedBlockParameters.validate(fb)
      }

      assert(ex.getMessage.contains("Record length should be positive. Got 0."))
    }

    "throw an exception when block size is zero" in {
      val fb = FixedBlockParameters(Some(1), Some(0), None)

      val ex = intercept[IllegalArgumentException] {
        FixedBlockParameters.validate(fb)
      }

      assert(ex.getMessage.contains("Block length should be positive. Got 0."))
    }

    "throw an exception when records per block is zero" in {
      val fb = FixedBlockParameters(Some(1), None, Some(0))

      val ex = intercept[IllegalArgumentException] {
        FixedBlockParameters.validate(fb)
      }

      assert(ex.getMessage.contains("Records per block should be positive. Got 0."))
    }
  }

  private def getRawRecordContext(recordsPerBlock: Int): RawRecordContext = {
    val numOfBlocks = 3

    val bytes = Range(0, numOfBlocks)
      .flatMap(i => {
        Range(0, recordsPerBlock).flatMap(j => {
          val num = (i * recordsPerBlock + j) % 10
          val v = (0xF0 + num).toByte
          Array[Byte](v, v)
        })
      }).toArray[Byte]

    getRawRecordContext(bytes)
  }

  private def getRawRecordContext(bytes: Array[Byte]): RawRecordContext = {
    val ibs = new TestByteStream(bytes)
    val hbs = new TestByteStream(bytes)

    RawRecordContext.builder(0L, ibs, hbs, copybook).build()
  }

}
