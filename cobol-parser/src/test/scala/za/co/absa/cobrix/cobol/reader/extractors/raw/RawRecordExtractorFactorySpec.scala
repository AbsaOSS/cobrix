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

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.reader.memorystream.TestStringStream

class RawRecordExtractorFactorySpec extends WordSpec {
  "createRecordHeaderParser()" should {

    val copybookContent =
      """      01 RECORD.
          02 X PIC X(3).
          02 N PIC N(3).
    """
    val copybook = CopybookParser.parseTree(copybookContent)

    "be able to create a record extractor" in {
      val recordExtractor = RawRecordExtractorFactory.createRecordHeaderParser("za.co.absa.cobrix.cobol.reader.extractors.raw.TextRecordExtractor",
        RawRecordContext(0L, new TestStringStream("AAA111\nBBB222\n"), copybook, ""))

      assert(recordExtractor.isInstanceOf[TextRecordExtractor])
      assert(recordExtractor.next().length == 6)
      assert(recordExtractor.offset == 7)
    }

    "throw an exception if class not fount" in {
      val ex = intercept[ClassNotFoundException] {
        RawRecordExtractorFactory.createRecordHeaderParser("com.example.DoesNotExist",
          RawRecordContext(0L, new TestStringStream("A"), copybook, ""))
      }

      assert(ex.getMessage.contains("com.example.DoesNotExist"))
    }

    "throw an exception when the extractor type is wrong" in {
      val ex = intercept[IllegalArgumentException] {
        RawRecordExtractorFactory.createRecordHeaderParser("za.co.absa.cobrix.cobol.reader.extractors.raw.DummyTestClass",
          RawRecordContext(0L, new TestStringStream("A"), copybook, ""))
      }

      assert(ex.getMessage.contains("does not extend RawRecordExtractor"))
    }

    "throw an exception when constructor parameters are wrong" in {
      val ex = intercept[IllegalArgumentException] {
        RawRecordExtractorFactory.createRecordHeaderParser("za.co.absa.cobrix.cobol.reader.extractors.raw.DummyWrongRecordExtractor",
          RawRecordContext(0L, new TestStringStream("A"), copybook, ""))
      }

      assert(ex.getMessage.contains("does not conform to the required signature"))
    }
  }
}
