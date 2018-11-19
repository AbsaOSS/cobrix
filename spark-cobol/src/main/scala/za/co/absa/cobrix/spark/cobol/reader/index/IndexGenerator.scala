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

package za.co.absa.cobrix.spark.cobol.reader.index

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.common.{BinaryUtils, DataExtractors}
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.Constants
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SimpleIndexEntry

import scala.collection.mutable.ArrayBuffer

object IndexGenerator {
  private val xcomHeaderBlock = 4

  def simpleIndexGenerator(fileId: Int, dataStream: SimpleStream, recordsPerIndexEntry: Int): ArrayBuffer[SimpleIndexEntry] = {
    var byteIndex = 0L
    val index = new ArrayBuffer[SimpleIndexEntry]
    var recordsInChunk = 0
    var recordIndex = 0
    var endOfFileReached = false
    while (!endOfFileReached) {
      val recordSize = getNextRecordSize(dataStream)
      if (recordSize <= 0) {
        endOfFileReached = true
      } else {
        val record = dataStream.next(recordSize)
        if (record.length < recordSize) {
          endOfFileReached = true
        } else {
          if (recordIndex == 0 || recordsInChunk >= recordsPerIndexEntry) {
            val indexEntry = SimpleIndexEntry(byteIndex, -1, fileId, recordIndex)
            index += indexEntry
            recordsInChunk = 0
          }
        }
      }
      byteIndex += xcomHeaderBlock + recordSize
      recordIndex += 1
      recordsInChunk += 1
    }

    // Setting offsetTo for each index record
    if (index.length > 1) {
      var i = 0
      while (i < index.length - 1) {
        index(i) = index(i).copy(offsetTo = index(i + 1).offsetFrom)
        i += 1
      }
    }

    index
  }

  def simpleIndexGenerator(fileId: Int, dataStream: SimpleStream, copybook: Copybook, segmentField: Primitive): ArrayBuffer[SimpleIndexEntry] = {
    var byteIndex = 0L
    val index = new ArrayBuffer[SimpleIndexEntry]
    var rootRecordId: String = ""
    var rootRecordSize = 0
    var recordsInChunk = 0
    var recordIndex = 0
    var endOfFileReached = false
    while (!endOfFileReached) {
      val recordSize = getNextRecordSize(dataStream)
      if (recordSize <= 0) {
        endOfFileReached = true
      } else {
        val record = dataStream.next(recordSize)
        if (record.length < recordSize) {
          endOfFileReached = true
        } else {
          if (recordIndex == 0) {
            rootRecordSize = recordSize
            rootRecordId = getSegmentId(copybook, segmentField, record)
            if (rootRecordId.isEmpty) {
              throw new IllegalStateException(s"Root record segment id cannot be empty at $byteIndex.")
            }
          }
          if (recordIndex == 0 || (recordsInChunk >= Constants.recordsPerIndexEntry && recordSize == rootRecordSize)) {
            if (rootRecordId == getSegmentId(copybook, segmentField, record)) {
              val indexEntry = SimpleIndexEntry(byteIndex, -1, fileId, recordIndex)
              index += indexEntry
              recordsInChunk = 0
            }
          }
        }
      }
      byteIndex += xcomHeaderBlock + recordSize
      recordIndex += 1
      recordsInChunk += 1
    }

    // Setting offsetTo for each index record
    if (index.length > 1) {
      var i = 0
      while (i < index.length - 1) {
        index(i) = index(i).copy(offsetTo = index(i + 1).offsetFrom)
        i += 1
      }
    }

    index
  }

  private def getNextRecordSize(dataStream: SimpleStream): Int = BinaryUtils.extractXcomRecordSize(dataStream.next(xcomHeaderBlock))

  private def getSegmentId(copybook: Copybook, segmentIdField: Primitive, data: Array[Byte]): String = {
    copybook.extractPrimitiveField(segmentIdField, data).toString.trim
  }
}
