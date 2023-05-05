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

class VariableBlockVariableRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private val recordQueue = new mutable.Queue[Array[Byte]]
  private var canSplitAtCurrentOffset = true
  private var recordOffset: Long = ctx.inputStream.offset

  override def offset: Long = recordOffset

  override def canSplitHere: Boolean = canSplitAtCurrentOffset

  override def hasNext: Boolean = {
    if (recordQueue.isEmpty) {
      readNextBlock()
    }
    recordQueue.nonEmpty
  }

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
}
