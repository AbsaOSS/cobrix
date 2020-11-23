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

import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

/**
  * This implementation of a record extractor for ASCII test files.
  *
  * Record extractors are used for in situations where the size of records in a file is not fixed and cannot be
  * determined neither from the copybook nor from record headers.
  */
class TextRecordExtractor(inputStream: SimpleStream, maxRecordSize: Int) extends RawRecordExtractor {
  private val bytes = new Array[Byte](maxRecordSize)
  private var bytesSize = 0
  private var lastFooterSize = 1

  override def hasNext: Boolean = !inputStream.isEndOfStream || bytesSize > 0

  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }
    ensureBytesRead(maxRecordSize)
    findEol()
  }

  override def getHeaderSize: Int = 0

  override def getFooterSize: Int = lastFooterSize

  private def findEol(): Array[Byte] = {
    var recordLength = 0
    var recordPayload = 0

    var i = 0
    while (recordLength == 0 && i < bytesSize) {
      if (bytes(i) == 0x0D) {
        if (i + 1 < maxRecordSize && bytes(i + 1) == 0x0A) {
          recordLength = i + 2
          recordPayload = i
        }
      } else if (bytes(i) == 0x0A) {
        recordLength = i + 1
        recordPayload = i
      }
      i += 1
    }

    val record = if (recordLength > 0) {
      bytes.take(recordPayload)
    } else {
      // Last record or a record is too large?
      // In the latter case
      if (inputStream.isEndOfStream) {
        // Last record
        recordLength = bytesSize
        recordPayload = bytesSize
      } else {
        // This is an errors situation - no line breaks between records
        // Return a record worth of data minus line break.
        recordLength = bytesSize - lastFooterSize
        recordPayload = bytesSize - lastFooterSize
      }
      bytes.take(recordLength)
    }

    if (bytesSize > recordLength) {
      System.arraycopy(bytes, recordLength, bytes, 0, bytesSize - recordLength)
    }
    bytesSize -= recordLength

    util.Arrays.fill(bytes, bytesSize, maxRecordSize, 0.toByte)

    lastFooterSize = recordLength - recordPayload

    record
  }

  private def ensureBytesRead(numOfBytes: Int): Unit = {
    val bytesToRead = numOfBytes - bytesSize
    if (bytesToRead > 0) {
      val newBytes = inputStream.next(bytesToRead)
      if (newBytes.length > 0) {
        System.arraycopy(newBytes, 0, bytes, bytesSize, newBytes.length)
        bytesSize = numOfBytes
      }
    }
  }
}
