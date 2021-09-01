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
  VariableBlockVariableRecordExtractor.additionalInfo = ctx.additionalInfo
  private var recordQueue = new mutable.Queue[Array[Byte]]
  private var initialRead = true
  private var recordNumber = ctx.startingRecordNumber
  override def offset: Long = ctx.inputStream.offset

  override def hasNext: Boolean = {
    var output: Boolean = true
    if (initialRead == true) {
      readNextBlock()
      initialRead = false
    } else {
      if (recordQueue.isEmpty && !ctx.inputStream.isEndOfStream) {
        readNextBlock()
      } else {
        if (recordQueue.isEmpty && ctx.inputStream.isEndOfStream) {
          output = false
        }
      }
    }
    output
  }

  private def readNextBlock(): Unit = {

    if (!ctx.inputStream.isEndOfStream) {
      val bdwFirstPart = BigInt(ctx.inputStream.next(2)).toInt
      val bdwSecondPart = BigInt(ctx.inputStream.next(2)).toInt
      var blockBuffer  = ctx.inputStream.next(bdwFirstPart-4)
      var blockPointer: Int = 0
      var recordCounter: Int =0
      while (blockPointer < blockBuffer.length) {
        recordCounter += 1
        var rdwFirstPart = BigInt(blockBuffer.slice(blockPointer, blockPointer+2)).toInt
        var rdwSecondPart = BigInt(blockBuffer.slice(blockPointer+2, blockPointer+4)).toInt
        val payload = blockBuffer.slice(blockPointer+4, blockPointer + rdwFirstPart)
        recordQueue.enqueue(payload)
        blockPointer += rdwFirstPart
      }
    }
  }


  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    var rawRecord: Array[Byte] = new Array[Byte](0)
    if (!hasNext) {
      throw new NoSuchElementException
    }
    rawRecord = recordQueue.dequeue()
    rawRecord
  }
}

object VariableBlockVariableRecordExtractor {
  var additionalInfo: String = ""
}
