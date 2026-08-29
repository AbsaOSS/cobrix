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

/**
  * A raw record extractor for files that consist of records of the same fixed length.
  *
  * Records are read sequentially from the input stream, each record consuming exactly the configured
  * number of bytes. When the record length is not provided explicitly, it is derived from the copybook,
  * so the extractor can be used for any file which layout implies a constant record size.
  *
  * The extraction stops as soon as the end of the input stream is reached, or when no more bytes can be
  * fetched from it. The last chunk of data is returned as a record even if it is shorter than the expected
  * record size.
  *
  * The header stream is not used by this extractor since no file level headers need to be inspected,
  * therefore it is closed immediately on construction.
  *
  * @param ctx               A context of the record extractor containing the input stream, the copybook
  *                          and the options passed to `spark-cobol`.
  * @param fixedRecordLength An optional record length in bytes. If not specified, the record size
  *                          calculated from the copybook is used.
  */
class FixedRecordLengthRawRecordExtractor(ctx: RawRecordContext, fixedRecordLength: Option[Int]) extends Serializable with RawRecordExtractor {
  private var byteOffset: Long = ctx.inputStream.offset
  private val recordSize = fixedRecordLength.getOrElse(ctx.copybook.getRecordSize)
  private var currentRecordOpt: Option[Array[Byte]] = None

  ctx.headerStream.close()

  /** Returns the byte offset of the next record in the input stream. */
  override def offset: Long = byteOffset

  override def hasNext: Boolean = {
    if (currentRecordOpt.isEmpty) {
      readNextRecord()
    }
    currentRecordOpt.nonEmpty
  }

  /** Returns the next record from the input stream. */
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

  /** Reads the next record from the input stream if available. */
  private def readNextRecord(): Unit = {
    if (!ctx.inputStream.isEndOfStream) {
      val nextRecord = ctx.inputStream.next(recordSize)

      if (nextRecord.length > 0) {
        currentRecordOpt = Some(nextRecord)
      }
    }
  }
}
