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

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.ASCII
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParserFactory
import za.co.absa.cobrix.cobol.reader.MemoryStream.TestStringStream
import za.co.absa.cobrix.cobol.reader.extractors.raw.TextRecordExtractor
import za.co.absa.cobrix.cobol.reader.index.IndexGenerator


class SparseIndexSpecSpec extends WordSpec  {

  private val copybookContents =
    """       01  RECORD.
           05  T          PIC X(1).
           05  R1.
             10  A2       PIC X(5).
             10  A3       PIC X(10).
           05  R2 REDEFINES R1.
             10  B1       PIC X(5).
             10  B2       PIC X(5).
    """

  ""

  "sparseIndexGenerator()" should {
    "Generate a sparse index for ASCII text data" in {
      val copybook = CopybookParser.parse(copybookContents, ASCII)

      val textFileContent: String =
        Seq("1Tes  0123456789",
          "2Test 012345",
          "1None Data¡3    ",
          "2 on  Data 4",
          "1Tes  0123456789",
          "2Test 012345",
          "1None Data¡3    ",
          "2 on  Data 4").mkString("\n")

      val segmentIdField = copybook.getFieldByName("T").asInstanceOf[Primitive]
      val segmentIdRootValue = "1"

      val stream = new TestStringStream(textFileContent)

      val recordHeaderParser = RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwLittleEndian, 0, 0, 0, 0)

      val recordExtractor = new TextRecordExtractor(0L, stream, copybook, "")

      val indexes = IndexGenerator.sparseIndexGenerator(0, stream, isRdwBigEndian = false,
        recordHeaderParser = recordHeaderParser, recordExtractor = Some(recordExtractor), recordsPerIndexEntry = Some(2),  sizePerIndexEntryMB = None,
        copybook = Some(copybook), segmentField = Some(segmentIdField), isHierarchical = true, rootSegmentId = segmentIdRootValue)
      assert(indexes.length == 4)
    }
  }


}
