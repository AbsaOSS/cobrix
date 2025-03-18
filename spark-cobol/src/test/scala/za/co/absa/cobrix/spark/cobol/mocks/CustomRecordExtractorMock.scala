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

package za.co.absa.cobrix.spark.cobol.mocks

import za.co.absa.cobrix.cobol.reader.extractors.raw.{RawRecordContext, RawRecordExtractor}

/**
  * This is a record extractor for a binary file where
  * - even records have the size of 2 bytes.
  * - odd records have the size of 3 bytes.
  */
class CustomRecordExtractorMock(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  CustomRecordExtractorMock.additionalInfo = ctx.additionalInfo
  CustomRecordExtractorMock.catchContext = ctx

  private var recordNumber = ctx.startingRecordNumber

  override def offset: Long = ctx.inputStream.offset

  override def hasNext: Boolean = !ctx.inputStream.isEndOfStream

  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }

    val rawRecord = if (recordNumber % 2 == 0) {
      ctx.inputStream.next(2)
    } else {
      ctx.inputStream.next(3)
    }

    recordNumber += 1

    rawRecord
  }
}

object CustomRecordExtractorMock {
  var additionalInfo: String = ""
  var catchContext: RawRecordContext = _
}
