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

package za.co.absa.cobrix.cobol.reader.index

import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParser
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import scala.collection.mutable.ArrayBuffer

object IndexGenerator extends Logging {

  def sparseIndexGenerator(fileId: Int,
                           dataStream: SimpleStream,
                           fileStartOffset: Long,
                           recordHeaderParser: RecordHeaderParser,
                           recordExtractor: Option[RawRecordExtractor],
                           recordsPerIndexEntry: Option[Int],
                           sizePerIndexEntryMB: Option[Int],
                           copybook: Option[Copybook],
                           segmentField: Option[Primitive],
                           isHierarchical: Boolean,
                           rootSegmentId: String = ""): ArrayBuffer[SparseIndexEntry] = {


    var byteIndex = fileStartOffset
    var bytesInChunk = fileStartOffset
    var recordIndex = 0
    var recordsInChunk = 0

    val rootSegmentIds = rootSegmentId.split(',').toList
    var rootRecordId: String = ""
    val isReallyHierarchical = copybook.nonEmpty && segmentField.nonEmpty && isHierarchical
    val isSplitBySize = recordsPerIndexEntry.isEmpty && sizePerIndexEntryMB.nonEmpty
    val needSplit = getSplitCondition(recordsPerIndexEntry, sizePerIndexEntryMB)

    // Add the first mandatory index entry
    val index = new ArrayBuffer[SparseIndexEntry]
    val indexEntry = SparseIndexEntry(fileStartOffset, -1, fileId, recordIndex)
    index += indexEntry

    recordExtractor.foreach(extractor => {
      if (extractor.offset != fileStartOffset) {
        throw new IllegalStateException("The record extractor has returned the offset that is not the beginning of the file. " +
          s"Expected: $fileStartOffset. Got: ${extractor.offset}. File: ${dataStream.inputFileName}. " +
          "Make sure 'offset()' points to the record that is going to be returned with next().")
      }
    })

    var endOfFileReached = false
    while (!endOfFileReached) {
      var record: Array[Byte] = null
      val (recordSize: Long, isValid, hasMoreRecords, canSplit) = recordExtractor match {
        case Some(extractor) =>
          val offset0 = byteIndex
          val canSplit = extractor.canSplitHere
          val isValid = if (extractor.hasNext) {
            record = extractor.next()
            true
          } else {
            false
          }
          val offset1 = extractor.offset
          val recordLength = offset1 - offset0
          (recordLength, isValid, extractor.hasNext, canSplit)
        case None =>
          val headerSize = recordHeaderParser.getHeaderLength
          val headerBytes = dataStream.next(headerSize)
          val recordMetadata = recordHeaderParser.getRecordMetadata(headerBytes, dataStream.offset, dataStream.size, dataStream.totalSize, recordIndex)
          if (recordMetadata.recordLength > 0) {
            record = dataStream.next(recordMetadata.recordLength)
          }
          val recordSize = dataStream.offset - byteIndex
          val hasMoreRecords = recordSize > 0 && !dataStream.isEndOfStream
          (recordSize, recordMetadata.isValid, hasMoreRecords, true)
      }

      if (!hasMoreRecords) {
        endOfFileReached = true
      } else {
        if (isValid) {
          if (isReallyHierarchical && rootRecordId.isEmpty) {
            val curSegmentId = getSegmentId(copybook.get, segmentField.get, record)
            if ((curSegmentId.nonEmpty && rootSegmentIds.isEmpty)
              || (rootSegmentIds.nonEmpty && rootSegmentIds.contains(curSegmentId))) {
              rootRecordId = curSegmentId
            }
          }
          if (canSplit && needSplit(recordsInChunk, bytesInChunk)) {
            if (!isReallyHierarchical || isSegmentGoodForSplit(rootSegmentIds, copybook.get, segmentField.get, record)) {
              val indexEntry = SparseIndexEntry(byteIndex, -1, fileId, recordIndex)
              val len = index.length
              // Do not add an entry if we are still at the same position as the previous entry.
              if (index(len - 1).offsetFrom != indexEntry.offsetFrom) {
                index(len - 1) = index(len - 1).copy(offsetTo = indexEntry.offsetFrom)
                index += indexEntry
                recordsInChunk = 0
              }
              if (isSplitBySize) {
                // If indexes are split by size subtract the size of the split from the total bytes read.
                // This way the mismatch between Spark partitions and HDFS blocks won't accumulate.
                // This achieves better alignment between Spark partitions and HDFS blocks.
                bytesInChunk -= sizePerIndexEntryMB.get.toLong * Constants.megabyte
              } else {
                bytesInChunk = 0L
              }
            }
          }
        }
        recordIndex += 1
        recordsInChunk += 1
        byteIndex += recordSize
        bytesInChunk += recordSize
      }
    }
    if (isReallyHierarchical && rootSegmentId.nonEmpty && rootRecordId.isEmpty) {
      logger.error(s"Root segment ${segmentField.get.name}=='$rootSegmentId' not found in the data file.")
    } else if (isReallyHierarchical && rootRecordId.isEmpty) {
      logger.error(s"Root segment ${segmentField.get.name} ie empty for every record in the data file.")
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

  private def isSegmentGoodForSplit(rootSegmentIds: List[String],
                                    copybook: Copybook,
                                    segmentField: Primitive,
                                    record: Array[Byte]): Boolean = {
    val segmentId = getSegmentId(copybook, segmentField, record)
    rootSegmentIds.contains(segmentId)
  }

  private def getSegmentId(copybook: Copybook, segmentIdField: Primitive, data: Array[Byte]): String = {
    val v = copybook.extractPrimitiveField(segmentIdField, data)
    if (v == null) "" else v.toString.trim
  }
}
