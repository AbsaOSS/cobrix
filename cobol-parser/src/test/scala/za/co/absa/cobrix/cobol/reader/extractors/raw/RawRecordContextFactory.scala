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

import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.reader.memorystream.TestStringStream
import za.co.absa.cobrix.cobol.reader.recordheader.{RecordHeaderDecoder, RecordHeaderDecoderBdw, RecordHeaderDecoderRdw, RecordHeaderParameters}
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

object RawRecordContextFactory {
  private val copybookContent =
    """      01 RECORD.
          02 X PIC X(1).
    """
  private val copybook = CopybookParser.parseTree(copybookContent)

  def getDummyRawRecordContext(
                                startingRecordNumber: Long = 0L,
                                inputStream: SimpleStream = new TestStringStream("A1\nB2\n"),
                                headerStream: SimpleStream = new TestStringStream("A1\nB2\n"),
                                copybook: Copybook = copybook,
                                rdwDecoder: RecordHeaderDecoder = new RecordHeaderDecoderBdw(RecordHeaderParameters(isBigEndian = false, 0)),
                                bdwDecoder: RecordHeaderDecoder = new RecordHeaderDecoderRdw(RecordHeaderParameters(isBigEndian = false, 0)),
                                additionalInfo: String = ""
                              ): RawRecordContext = {
    RawRecordContext(startingRecordNumber, inputStream, headerStream, copybook, rdwDecoder, bdwDecoder, additionalInfo)
  }

}
