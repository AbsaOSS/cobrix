/*
 * Copyright 2018-2019 ABSA Group Limited
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
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParser
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.Constants
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry

import scala.collection.mutable.ArrayBuffer

object IndexGenerator {
  def sparseIndexGenerator(fileId: Int,
                           dataStream: SimpleStream,
                           isRdwBigEndian: Boolean,
                           recordHeaderParser: RecordHeaderParser,
                           recordsPerIndexEntry: Option[Int] = None,
                           sizePerIndexEntryMB: Option[Int] = None,
                           copybook: Option[Copybook] = None,
                           segmentField: Option[Primitive] = None,
                           rootSegmentId: String = ""): ArrayBuffer[SparseIndexEntry] = {
    var byteIndex = 0L
    val index = new ArrayBuffer[SparseIndexEntry]
    var rootRecordId: String = ""
    var rootRecordSize = -1
    var recordsInChunk = 0
    var bytesInChunk = 0L
    var recordIndex = 0
    var isHierarchical = copybook.nonEmpty && segmentField.nonEmpty
    val isSplitBySize = recordsPerIndexEntry.isEmpty && sizePerIndexEntryMB.nonEmpty

    val needSplit = getSplitCondition(recordsPerIndexEntry, sizePerIndexEntryMB)

    // Add the first mandatory index entry
    val indexEntry = SparseIndexEntry(0, -1, fileId, recordIndex)
    index += indexEntry

    var endOfFileReached = false
    while (!endOfFileReached) {
      val headerSize = recordHeaderParser.getHeaderLength
      val headerBytes = dataStream.next(headerSize)
      val recordMetadata = recordHeaderParser.getRecordMetadata(headerBytes, byteIndex)
      val recordSize = recordMetadata.recordLength
      if (recordSize <= 0) {
        endOfFileReached = true
      } else {
        val record = dataStream.next(recordSize)
        if (record.length < recordSize) {
          endOfFileReached = true
        } else if (recordMetadata.isValid) {
          if (isHierarchical && rootRecordId.isEmpty) {
            val curSegmentId = getSegmentId(copybook.get, segmentField.get, record)
            if ((curSegmentId.nonEmpty && rootSegmentId.isEmpty)
              || (rootSegmentId.nonEmpty && curSegmentId == rootSegmentId)) {
              rootRecordSize = recordSize
              rootRecordId = curSegmentId
            }
          }
          if (needSplit(recordsInChunk, bytesInChunk)) {
            if (!isHierarchical || isSegmentGoodForSplit(rootRecordSize, rootRecordId, copybook.get, segmentField.get, record)) {
              val indexEntry = SparseIndexEntry(byteIndex, -1, fileId, recordIndex)
              val len = index.length
              index(len - 1) = index(len - 1).copy(offsetTo = indexEntry.offsetFrom)
              index += indexEntry
              recordsInChunk = 0
              if (isSplitBySize) {
                // If indexes are split by size subtract the size of the split from the total bytes read.
                // This way the mismatch between Spark partitions and HDFS blocks won't accumulate.
                // This wahieves better alignment between Spark partitions and HDFS blocks.
                bytesInChunk -= sizePerIndexEntryMB.get.toLong * Constants.megabyte
              } else {
                bytesInChunk = 0L
              }
            }
          }
        }
      }
      byteIndex += headerSize + recordSize
      recordIndex += 1
      recordsInChunk += 1
      bytesInChunk += headerSize + recordSize
    }
    if (isHierarchical && rootSegmentId.nonEmpty && rootRecordId.isEmpty) {
      throw new IllegalStateException(s"Root segment ${segmentField.get.name}=='$rootSegmentId' not found in the data file.")
    }
    if (isHierarchical && rootRecordId.isEmpty) {
      throw new IllegalStateException(s"Root segment ${segmentField.get.name} ie empty for every record in the data file.")
    }
    index
  }

  /** Returns a predicate that returns true when current index entry has reached the required size */
  private def getSplitCondition(recordsPerIndexEntry: Option[Int], sizePerIndexEntryMB: Option[Int]) = {
    val bytesPerIndexEntry = sizePerIndexEntryMB.getOrElse(Constants.defaultIndexEntrySizeMB).toLong * Constants.megabyte
    val recPerIndexEntry = recordsPerIndexEntry.getOrElse(1)

    if (recordsPerIndexEntry.isDefined) {
      (records: Int, currentSize: Long) => {
        records >= recPerIndexEntry
      }
    } else {
      (records: Int, currentSize: Long) => {
        currentSize >= bytesPerIndexEntry
      }
    }
  }

  private def isSegmentGoodForSplit(rootRecordSize: Int,
                                    rootRecordId: String,
                                    copybook: Copybook,
                                    segmentField: Primitive,
                                    record: Array[Byte]): Boolean = {
    if (record.length != rootRecordSize) {
      false
    } else {
      rootRecordId == getSegmentId(copybook, segmentField, record)
    }
  }

  private def getSegmentId(copybook: Copybook, segmentIdField: Primitive, data: Array[Byte]): String = {
    val v = copybook.extractPrimitiveField(segmentIdField, data)
    if (v == null) "" else v.toString.trim
  }
}
