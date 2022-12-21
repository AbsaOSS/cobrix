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
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordContextFactory.getDummyRawRecordContext
import za.co.absa.cobrix.cobol.reader.memorystream.TestStringStream

class RawRecordExtractorFactorySpec extends AnyWordSpec {
  "createRecordHeaderParser()" should {

    val copybookContent =
      """      01 RECORD.
          02 X PIC X(3).
          02 N PIC N(3).
    """
    val copybook = CopybookParser.parseTree(copybookContent)

    "be able to create a record extractor" in {
      val recordExtractor = RawRecordExtractorFactory.createRecordHeaderParser("za.co.absa.cobrix.cobol.reader.extractors.raw.TextRecordExtractor",
        getDummyRawRecordContext(inputStream = new TestStringStream("AAA111\nBBB22\nCCC3333\nDDD444\n"), copybook = copybook))

      assert(recordExtractor.isInstanceOf[TextRecordExtractor])
      assert(recordExtractor.offset == 0)
      recordExtractor.hasNext
      assert(recordExtractor.offset == 0)
      assert(recordExtractor.next().length == 6)
      assert(recordExtractor.offset == 7)
      recordExtractor.hasNext
      assert(recordExtractor.offset == 7)
      assert(recordExtractor.next().length == 5)
      assert(recordExtractor.offset == 13)
      recordExtractor.hasNext
      assert(recordExtractor.offset == 13)
      assert(recordExtractor.next().length == 7)
      assert(recordExtractor.offset == 21)
      recordExtractor.hasNext
      assert(recordExtractor.offset == 21)
    }

    "throw an exception if class not fount" in {
      val ex = intercept[ClassNotFoundException] {
        RawRecordExtractorFactory.createRecordHeaderParser("com.example.DoesNotExist",
          getDummyRawRecordContext(inputStream = new TestStringStream("A"), copybook = copybook))
      }

      assert(ex.getMessage.contains("com.example.DoesNotExist"))
    }

    "throw an exception when the extractor type is wrong" in {
      val ex = intercept[IllegalArgumentException] {
        RawRecordExtractorFactory.createRecordHeaderParser("za.co.absa.cobrix.cobol.reader.extractors.raw.DummyTestClass",
          getDummyRawRecordContext(inputStream = new TestStringStream("A"), copybook = copybook))
      }

      assert(ex.getMessage.contains("does not extend RawRecordExtractor"))
    }

    "throw an exception when constructor parameters are wrong" in {
      val ex = intercept[IllegalArgumentException] {
        RawRecordExtractorFactory.createRecordHeaderParser("za.co.absa.cobrix.cobol.reader.extractors.raw.DummyWrongRecordExtractor",
          getDummyRawRecordContext(inputStream = new TestStringStream("A"), copybook = copybook))
      }

      assert(ex.getMessage.contains("does not conform to the required signature"))
    }
  }
}
