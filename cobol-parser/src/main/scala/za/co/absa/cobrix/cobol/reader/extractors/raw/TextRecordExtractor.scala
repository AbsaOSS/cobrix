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

import java.util

/**
  * This implementation of a record extractor for ASCII test files.
  *
  * Record extractors are used for in situations where the size of records in a file is not fixed and cannot be
  * determined neither from the copybook nor from record headers.
  */
class TextRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  private val maxRecordSize = ctx.copybook.getRecordSize + 2
  private val pendingBytes = new Array[Byte](maxRecordSize)
  private var pendingBytesSize = 0
  private var recordBytes: Option[Array[Byte]] = None
  private var curRecordSize = 0
  private var lastFooterSize = 1

  override def hasNext: Boolean = {
    if (recordBytes.isEmpty) {
      ensureBytesRead(maxRecordSize)
      fetchNextRecord()
    }

    recordBytes.get.length > 0
  }

  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }
    val bytesToReturn = recordBytes.get
    curRecordSize = 0
    recordBytes = None
    bytesToReturn
  }

  override def offset: Long = ctx.inputStream.offset - pendingBytesSize - curRecordSize

  private def ensureBytesRead(numOfBytes: Int): Unit = {
    val bytesToRead = numOfBytes - pendingBytesSize
    if (bytesToRead > 0) {
      val newBytes = ctx.inputStream.next(bytesToRead)
      if (newBytes.length > 0) {
        System.arraycopy(newBytes, 0, pendingBytes, pendingBytesSize, newBytes.length)
        pendingBytesSize = pendingBytesSize + newBytes.length
      }
    }
  }

  private def skipEmptyLines(): Unit = {
    var i = 0
    while (i < pendingBytesSize && (pendingBytes(i) == 0x0D || pendingBytes(i) == 0x0A)) {
      i += 1
    }
    if (i > 0) {
      advanceArray(i)
      ensureBytesRead(maxRecordSize)
    }
  }

  private def findNextNonEmptyRecord(): (Int, Int) = {
    var recordLength = 0
    var recordPayload = 0
    var i = 0

    while (recordLength == 0 && i < pendingBytesSize) {
      if (pendingBytes(i) == 0x0D) {
        if (i + 1 < maxRecordSize && pendingBytes(i + 1) == 0x0A) {
          recordLength = i + 2
          recordPayload = i
        }
      } else if (pendingBytes(i) == 0x0A) {
        recordLength = i + 1
        recordPayload = i
      }
      i += 1
    }
    (recordLength, recordPayload)
  }

  private def fetchNextRecord(): Unit = {
    skipEmptyLines()

    var (recordLength, recordPayload) = findNextNonEmptyRecord()

    recordBytes = if (recordLength > 0) {
      curRecordSize = recordLength
      Some(pendingBytes.take(recordPayload))
    } else {
      // Last record or a record is too large?
      // In the latter case
      if (ctx.inputStream.isEndOfStream) {
        // Last record
        recordLength = pendingBytesSize
        recordPayload = pendingBytesSize
      } else {
        // This is an errors situation - no line breaks between records
        // Return a record worth of data minus line break.
        recordLength = pendingBytesSize - lastFooterSize
        recordPayload = pendingBytesSize - lastFooterSize
      }
      curRecordSize = recordLength
      Some(pendingBytes.take(recordLength))
    }

    advanceArray(recordLength)

    lastFooterSize = recordLength - recordPayload
  }

  private def advanceArray(recordLength: Int): Unit = {
    if (pendingBytesSize > recordLength) {
      System.arraycopy(pendingBytes, recordLength, pendingBytes, 0, pendingBytesSize - recordLength)
    }
    pendingBytesSize -= recordLength

    util.Arrays.fill(pendingBytes, pendingBytesSize, maxRecordSize, 0.toByte)
  }
}
