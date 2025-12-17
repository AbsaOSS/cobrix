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

package za.co.absa.cobrix.spark.cobol.source.regression

import org.apache.hadoop.conf.Configuration
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParserFactory
import za.co.absa.cobrix.cobol.reader.index.IndexGenerator
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer

class Test12MultiRootSparseIndex extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybookContents =
    """       01  R.
                03 S     PIC X(1).
                03 V     PIC X(2).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // Records
    0xF0.toByte, 0xF0.toByte, 0xF5.toByte,
    0xF2.toByte, 0xF2.toByte, 0xF6.toByte,
    0xF1.toByte, 0xF1.toByte, 0xF7.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF8.toByte,
    0xF4.toByte, 0xF4.toByte, 0xF9.toByte,
    0xF1.toByte, 0xF1.toByte, 0xF0.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF1.toByte,
    0xF1.toByte, 0xF1.toByte, 0xF2.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF3.toByte,
    0xF1.toByte, 0xF1.toByte, 0xF4.toByte,
    0xF3.toByte, 0xF3.toByte, 0xF5.toByte,
    0xF4.toByte, 0xF4.toByte, 0xF6.toByte
  )

  "IndexGenerator.sparseIndexGenerator" should {
    "be able to index files having 2 root ids" in {
      withTempBinFile("sp_ind_test1", ".dat", binFileContents) { tmpFileName =>
        val copybook = CopybookParser.parseTree(copybookContents)
        val segmentIdField = copybook.getFieldByName("S").asInstanceOf[Primitive]
        val segmentIdRootValues = "0,1"

        val stream = new FileStreamer(tmpFileName, new Configuration())

        val recordHeaderParser = RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwFixedLength, 3, 0, 0, 0)
        val indexes = IndexGenerator.sparseIndexGenerator(0, stream, 0L,
          recordHeaderParser = recordHeaderParser, recordExtractor = None, recordsPerIndexEntry = Some(4),  sizePerIndexEntryMB = None,
          copybook = Some(copybook), segmentField = Some(segmentIdField), isHierarchical = true, rootSegmentId = segmentIdRootValues)
        assert(indexes.length == 3)
      }
    }

    "be able to parse files that when record size does not divide file size" in {
      withTempBinFile("sp_ind_test1", ".dat", binFileContents.dropRight(2)) { tmpFileName =>
        val copybook = CopybookParser.parseTree(copybookContents)
        val segmentIdField = copybook.getFieldByName("S").asInstanceOf[Primitive]
        val segmentIdRootValues = "0,1"

        val stream = new FileStreamer(tmpFileName, new Configuration())

        val recordHeaderParser = RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwFixedLength, 3, 0, 0, 0)
        val indexes = IndexGenerator.sparseIndexGenerator(0, stream, 0L,
          recordHeaderParser = recordHeaderParser, recordExtractor = None, recordsPerIndexEntry = Some(4), sizePerIndexEntryMB = None,
          copybook = Some(copybook), segmentField = Some(segmentIdField), isHierarchical = true, rootSegmentId = segmentIdRootValues)
        assert(indexes.length == 3)
      }
    }
  }
}
