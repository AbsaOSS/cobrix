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
  *
  * Empty lines (ones that contain only LF / CRLF) are skipped.
  *
  * The implementation is optimized for performance, so might be not obviously readable.
  * Hopefully, comments will help anyone reading this.
  */
class TextFullRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private val recordSize = ctx.copybook.getRecordSize

  // Maximum possible record size is the size of the copybook record + maximum size of the delimiter (2 characters for CRLF).
  private val maxRecordSize = recordSize * 2 + 2

  // This is the buffer to keep the part of the stream that will be split by records.
  // The size of the array is always the maximum record size. The number of bytes that contain useful payload is specified
  // in pendingBytesSize.
  private val pendingBytes = new Array[Byte](maxRecordSize)
  private var pendingBytesSize = 0

  // If true, curRecordSize and curPayloadSize point to a record, otherwise the next record needs to be found
  private var isRawRecordFound = false
  // The number of bytes from pendingBytes that correspond to a record, including line break character(s)
  private var curRecordSize = 0
  // The number of bytes from pendingBytes that correspond to a record, without line break character(s)
  private var curPayloadSize = 0

  override def hasNext: Boolean = {
    if (!isRawRecordFound) {
      ensureBytesRead(maxRecordSize)
      findNextRecord()
    }

    curRecordSize > 0
  }

  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }
    fetchNextRecord()
  }

  override def offset: Long = ctx.inputStream.offset - pendingBytesSize

  // This method ensures that pendingBytes contains the specified number of bytes read from the input stream
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

  // This method skips empty lines, by ignoring lines that begin from CR / LF
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

  // This method finds the location of the end of the next record by searching for line ending characters
  // The data in pendingBytes is expected to be the length of maxRecordSize, or can be smaller for the last
  // record in the file
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

  private def findNextLineBreak(recordLength: Int): Unit = {
    var found = false
    var endOfStream = false

    while (!found && !endOfStream) {
      val start = recordLength
      val size = pendingBytesSize - recordLength
      var i = 0

      while (!found && i < size) {
        if (pendingBytes(start + i) == 0x0D || pendingBytes(start + i) == 0x0A) {
          found = true
        } else {
          i += 1
        }
      }
      if (i > 0) {
        System.arraycopy(pendingBytes, recordLength + i, pendingBytes, recordLength, size - i)
        pendingBytesSize -= i
      }
      endOfStream = ctx.inputStream.isEndOfStream
      if (!found && !endOfStream) {
        ensureBytesRead(maxRecordSize)
      }
    }
  }

  // This method finds the location of the end of the next record and adjusts curRecordSize and curPayloadSize
  // so that the next record can be fetched. Skips empty lines.
  private def findNextRecord(): Unit = {
    skipEmptyLines()

    val (recordLength, recordPayload) = findNextNonEmptyRecord()

    if (recordLength > 0) {
      curRecordSize = recordLength
      curPayloadSize = recordPayload
    } else {
      // Last record or a record is too large?
      // In the latter case
      if (pendingBytesSize <= recordSize && ctx.inputStream.isEndOfStream) {
        // Last record
        curRecordSize = pendingBytesSize
        curPayloadSize = pendingBytesSize
      } else {
        // This is an errors situation - no line breaks between records
        // Return a record worth of data.
        curRecordSize = recordSize
        curPayloadSize = recordSize

        findNextLineBreak(recordSize)
      }
    }

    isRawRecordFound = true
  }

  // This method extracts the current record from the buffer array.
  // It should only be called when curRecordSize and curPayloadSize are set properly.
  private def fetchNextRecord(): Array[Byte] = {
    val bytes = pendingBytes.take(curPayloadSize)
    advanceArray(curRecordSize)
    isRawRecordFound = false
    curPayloadSize = 0
    curRecordSize = 0
    bytes
  }

  // This method shifts the internal buffer pendingBytes to the left by the size of the record.
  // It also fills the rest of the array with 0x0 character.
  private def advanceArray(recordLength: Int): Unit = {
    if (pendingBytesSize > recordLength) {
      System.arraycopy(pendingBytes, recordLength, pendingBytes, 0, pendingBytesSize - recordLength)
    }
    pendingBytesSize -= recordLength

    util.Arrays.fill(pendingBytes, pendingBytesSize, maxRecordSize, 0.toByte)
  }
}
