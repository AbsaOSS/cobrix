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

class FixedRecordLengthRawRecordExtractor(ctx: RawRecordContext, fixedRecordLength: Option[Int]) extends Serializable with RawRecordExtractor {
  private var byteOffset: Long = ctx.inputStream.offset
  private val recordSize = fixedRecordLength.getOrElse(ctx.copybook.getRecordSize)
  private var currentRecordOpt: Option[Array[Byte]] = None

  ctx.headerStream.close()

  override def offset: Long = byteOffset

  override def hasNext: Boolean = {
    if (currentRecordOpt.isEmpty) {
      readNextRecord()
    }
    currentRecordOpt.nonEmpty
  }

  private def readNextRecord(): Unit = {
    if (!ctx.inputStream.isEndOfStream) {
      val nextRecord = ctx.inputStream.next(recordSize)

      if (nextRecord.length > 0) {
        currentRecordOpt = Some(nextRecord)
      }
    }
  }


  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }
    val record = currentRecordOpt.get
    byteOffset += record.length
    currentRecordOpt = None
    record
  }
}
