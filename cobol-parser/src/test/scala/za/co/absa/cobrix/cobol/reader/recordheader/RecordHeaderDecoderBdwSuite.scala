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

package za.co.absa.cobrix.cobol.reader.recordheader

import org.scalatest.wordspec.AnyWordSpec

class RecordHeaderDecoderBdwSuite extends AnyWordSpec {
  "headerSize" should {
    "always return 4" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters())

      assert(rhd.headerSize == 4)
    }
  }

  "getRecordLength for nonextended BDW" should {
    "support big-endian non-adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))

      assert(rhd.getRecordLength(Array[Byte](0, 4, 0, 0), 0) == 4)
      assert(rhd.getRecordLength(Array[Byte](0, 10, 0, 0), 0) == 10)
      assert(rhd.getRecordLength(Array[Byte](1, 0, 0, 0), 0) == 256)
      assert(rhd.getRecordLength(Array[Byte](10, 0, 0, 0), 0) == 2560)
    }

    "support big-endian adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 1))

      assert(rhd.getRecordLength(Array[Byte](0, 4, 0, 0), 0) == 5)
      assert(rhd.getRecordLength(Array[Byte](0, 10, 0, 0), 0) == 11)
      assert(rhd.getRecordLength(Array[Byte](1, 0, 0, 0), 0) == 257)
      assert(rhd.getRecordLength(Array[Byte](10, 0, 0, 0), 0) == 2561)
    }

    "support little-endian non-adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(false, 0))

      assert(rhd.getRecordLength(Array[Byte](0, 0, 4, 0), 0) == 4)
      assert(rhd.getRecordLength(Array[Byte](0, 0, 10, 0), 0) == 10)
      assert(rhd.getRecordLength(Array[Byte](0, 0, 0, 1), 0) == 256)
      assert(rhd.getRecordLength(Array[Byte](0, 0, 0, 10), 0) == 2560)
    }

    "support little-endian adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(false, -1))

      assert(rhd.getRecordLength(Array[Byte](0, 0, 5, 0), 0) == 4)
      assert(rhd.getRecordLength(Array[Byte](0, 0, 10, 0), 0) == 9)
      assert(rhd.getRecordLength(Array[Byte](0, 0, 0, 1), 0) == 255)
      assert(rhd.getRecordLength(Array[Byte](0, 0, 0, 10), 0) == 2559)
    }
  }

  "getRecordLength for extended BDW" should {
    "support big-endian non-adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))

      assert(rhd.getRecordLength(Array[Byte](128.toByte, 0, 0, 4), 0) == 4)
      assert(rhd.getRecordLength(Array[Byte](128.toByte, 0, 0, 10), 0) == 10)
      assert(rhd.getRecordLength(Array[Byte](129.toByte, 1, 2, 3), 0) == 16843267)
      assert(rhd.getRecordLength(Array[Byte](138.toByte, 1, 2, 3), 0) == 167838211)
    }

    "support big-endian adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 1))

      assert(rhd.getRecordLength(Array[Byte](128.toByte, 0, 0, 4), 0) == 5)
      assert(rhd.getRecordLength(Array[Byte](128.toByte, 0, 0, 10), 0) == 11)
      assert(rhd.getRecordLength(Array[Byte](129.toByte, 1, 2, 3), 0) == 16843268)
      assert(rhd.getRecordLength(Array[Byte](138.toByte, 1, 2, 3), 0) == 167838212)
    }

    "support little-endian non-adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(false, 0))

      assert(rhd.getRecordLength(Array[Byte](4, 0, 0, 128.toByte), 0) == 4)
      assert(rhd.getRecordLength(Array[Byte](10, 0, 0, 128.toByte), 0) == 10)
      assert(rhd.getRecordLength(Array[Byte](3, 2, 1, 129.toByte), 0) == 16843267)
      assert(rhd.getRecordLength(Array[Byte](3, 2, 1, 138.toByte), 0) == 167838211)
    }

    "support little-endian adjusted headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(false, -1))

      assert(rhd.getRecordLength(Array[Byte](5, 0, 0, 128.toByte), 0) == 4)
      assert(rhd.getRecordLength(Array[Byte](10, 0, 0, 128.toByte), 0) == 9)
      assert(rhd.getRecordLength(Array[Byte](3, 2, 1, 129.toByte), 0) == 16843266)
      assert(rhd.getRecordLength(Array[Byte](3, 2, 1, 138.toByte), 0) == 167838210)
    }
  }

  "getRecordLength non-happy path" should {
    "fail when header size is lesser than expected" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))

      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](0, 0, 2), 123)
      }

      assert(ex.getMessage.contains("The length of BDW headers is unexpected. Expected: 4, got 3. Header: 0,0,2, offset: 123."))
    }

    "fail when header size is bigger than expected" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))

      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](0, 0, 2, 0, 0), 123)
      }

      assert(ex.getMessage.contains("The length of BDW headers is unexpected. Expected: 4, got 5. Header: 0,0,2,0,0, offset: 123."))
    }

    "fail when big-endian header is used for little-endian headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(false, 0))


      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](0, 1, 0, 0), 234)
      }

      assert(ex.getMessage.contains("BDW headers contain non-zero values where zeros are expected (check 'rdw_big_endian' flag"))
    }

    "fail when little-endian header is used for big-endian headers" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))


      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](0, 0, 0, 1), 234)
      }

      assert(ex.getMessage.contains("BDW headers contain non-zero values where zeros are expected (check 'rdw_big_endian' flag"))
    }

    "fail when record size is incorrect" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, -10))


      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](0, 1, 0, 0), 234)
      }

      assert(ex.getMessage.contains("BDW headers contain an invalid value (-9). Header: 0,1,0,0, offset: 234."))
    }

    "fail when record size is too small" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))

      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](0, 3, 0, 0), 0)
      }

      assert(ex.getMessage.contains("BDW headers should have a length of at least 4 bytes. Got 3 bytes. Header: 0,3,0,0, offset: 0."))
    }

    "fail when block size is too big" in {
      val rhd = new RecordHeaderDecoderBdw(RecordHeaderParametersFactory.getDummyRecordHeaderParameters(true, 0))

      val ex = intercept[IllegalStateException] {
        rhd.getRecordLength(Array[Byte](228.toByte, 0, 0, 1), 0)
      }

      assert(ex.getMessage.contains("The length of BDW block is too big. Got 1677721601. Header: 228,0,0,1, offset: 0."))
    }
  }

}
