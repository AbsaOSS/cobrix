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
  * the first byte of the file specifies the record size.
  */
class CustomRecordExtractorWithFileHeaderMock(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  private var recordNumber = ctx.startingRecordNumber
  private val recordSize = ctx.headerStream.next(1).head

  ctx.headerStream.close()

  override def offset: Long = ctx.dataStream.offset

  override def hasNext: Boolean = !ctx.dataStream.isEndOfStream

  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }

    val rawRecord = ctx.dataStream.next(recordSize)

    if (rawRecord.length != recordSize || ctx.dataStream.isEndOfStream) {
      ctx.dataStream.close()
    }

    recordNumber += 1

    rawRecord
  }
}
