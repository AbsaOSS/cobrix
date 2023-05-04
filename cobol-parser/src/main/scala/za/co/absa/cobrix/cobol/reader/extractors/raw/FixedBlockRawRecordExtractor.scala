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

class FixedBlockRawRecordExtractor(ctx: RawRecordContext, fbParams: FixedBlockParameters) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private val recordQueue = new mutable.Queue[Array[Byte]]

  private val recordSize = fbParams.recordLength.getOrElse(ctx.copybook.getRecordSize)
  private val bdwSize = fbParams.blockLength.orElse(fbParams.recordsPerBlock.map(_ * recordSize))

  override def offset: Long = ctx.dataStream.offset

  override def hasNext: Boolean = {
    if (recordQueue.isEmpty) {
      readNextBlock()
    }
    recordQueue.nonEmpty
  }

  private def readNextBlock(): Unit = {
    if (!ctx.dataStream.isEndOfStream) {
      var bdwOffset = ctx.dataStream.offset

      val nextBlockSize = bdwSize.getOrElse({
        val bdw = ctx.dataStream.next(ctx.bdwDecoder.headerSize)
        val blockLength = ctx.bdwDecoder.getRecordLength(bdw, bdwOffset)
        bdwOffset += ctx.bdwDecoder.headerSize
        blockLength
      })

      val blockBuffer = ctx.dataStream.next(nextBlockSize)

      var blockIndex = 0

      while (blockIndex < blockBuffer.length) {
        val payload = blockBuffer.slice(blockIndex, blockIndex + recordSize)
        if (payload.length > 0) {
          recordQueue.enqueue(payload)
        }
        blockIndex += recordSize
      }
    }
  }


  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }
    recordQueue.dequeue()
  }
}
