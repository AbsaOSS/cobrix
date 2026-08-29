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

import scala.collection.mutable

/**
  * A raw record extractor for variable block length files (`record_format = VB`), where records
  * are grouped into blocks and both blocks and records are prefixed with their own headers.
  *
  * The extractor reads the input stream block by block. Each block begins with a block descriptor
  * word (BDW), decoded by `ctx.bdwDecoder`, that defines the length of the block payload. The block
  * payload, in turn, contains a sequence of records, each one prefixed with a record descriptor word
  * (RDW), decoded by `ctx.rdwDecoder`, that defines the length of the record. All records of a block
  * are buffered in an internal queue and are returned one by one by `next()`. Records with an empty
  * payload are skipped. Once the queue is exhausted, the next block is fetched from the stream, and
  * the iteration ends when the end of the stream is reached.
  *
  * Since block headers can only be interpreted at block boundaries, splitting the input is allowed
  * only at offsets that point to the beginning of a block, which is reflected by `canSplitHere`.
  * The offset returned by `offset` always points to the absolute beginning of the next record to be
  * returned, including the BDW of the block it belongs to when that record is the first record of
  * the block.
  *
  * The header stream is not used by this extractor and is closed on construction.
  *
  * @param ctx a context that holds the input stream, the copybook, the block and record header
  *            decoders and the options passed to `spark-cobol`.
  */
class VariableBlockVariableRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private val recordQueue = new mutable.Queue[Array[Byte]]
  private var canSplitAtCurrentOffset = true
  private var recordOffset: Long = ctx.inputStream.offset

  /** Returns the byte offset of the next record in the input stream. */
  override def offset: Long = recordOffset

  /** Returns true if the input stream can be split at the current offset. */
  override def canSplitHere: Boolean = canSplitAtCurrentOffset

  /** Returns true if there are more records to be read from the input stream. */
  override def hasNext: Boolean = {
    if (recordQueue.isEmpty) {
      readNextBlock()
    }
    recordQueue.nonEmpty
  }

  /** Returns the next record from the input stream. */
  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }
    if (canSplitAtCurrentOffset) {
      recordOffset += ctx.bdwDecoder.headerSize
    }
    val record = recordQueue.dequeue()
    recordOffset += ctx.rdwDecoder.headerSize + record.length

    canSplitAtCurrentOffset = recordQueue.isEmpty
    record
  }

  /**
    * Reads the next block (BDW) from the input stream and splits it into individual records.
    *
    * The block descriptor word is decoded first in order to determine the length of the block payload.
    * The payload is then traversed record by record: for each record the record descriptor word is decoded
    * to get the record length, and the corresponding non-empty payload is put into the internal record queue,
    * from which subsequent records are served.
    *
    * If the end of the input stream has been reached, nothing is read and the record queue stays unchanged.
    *
    * @return nothing, the decoded records are added to the internal record queue as a side effect
    */
  private def readNextBlock(): Unit = {
    val bdwSize = ctx.bdwDecoder.headerSize
    val rdwSize = ctx.rdwDecoder.headerSize

    if (!ctx.inputStream.isEndOfStream) {
      val bdwOffset = ctx.inputStream.offset
      val bdw = ctx.inputStream.next(bdwSize)

      val blockLength = ctx.bdwDecoder.getRecordLength(bdw, bdwOffset)
      val blockBuffer = ctx.inputStream.next(blockLength)

      var blockIndex = 0

      while (blockIndex < blockBuffer.length) {
        val rdwOffset = bdwOffset + blockIndex
        val rdw = blockBuffer.slice(blockIndex, blockIndex + rdwSize)
        val recordLength = ctx.rdwDecoder.getRecordLength(rdw, rdwOffset)

        val payload = blockBuffer.slice(blockIndex + rdwSize, blockIndex + recordLength + rdwSize)
        if (payload.length > 0) {
          recordQueue.enqueue(payload)
        }
        blockIndex += recordLength + rdwSize
      }
    }
  }
}
