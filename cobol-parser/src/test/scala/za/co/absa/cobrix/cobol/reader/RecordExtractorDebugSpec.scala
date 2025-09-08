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

import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.mock.CustomRecordExtractorMock
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordContext
import za.co.absa.cobrix.cobol.reader.memorystream.TestStringStream

class RecordExtractorDebugSpec extends AnyWordSpec {
  private val log = LoggerFactory.getLogger("debug")

  private val data = "AABBBCCDDDEEFFF"

  // This is a code snippet that can be used to test custom record extractors
  "record extractor data should be extracted as expected" in {
    val dataStream = new TestStringStream(data)
    val headerStream = new TestStringStream(data)

    val ctx = RawRecordContext.builder(0L, dataStream, headerStream, null).build()
    val extractor = new CustomRecordExtractorMock(ctx)

    var i = 0
    log.info(s"${headerStream.offset} : headers")

    while (extractor.hasNext) {
      val offset = dataStream.offset
      val record = extractor.next()
      val recordHex = record.map(b => f"$b%02X").mkString.take(10)
      log.info(s"$offset : ${record.length} : $recordHex")
      i += 1
    }

    assert(i == 6)
  }
}
