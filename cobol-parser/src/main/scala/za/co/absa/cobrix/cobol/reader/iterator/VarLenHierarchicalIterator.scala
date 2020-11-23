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

package za.co.absa.cobrix.cobol.reader.iterator

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParser
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.extractors.record.{RecordExtractors, RecordHandler}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
  * This iterator is used to variable length data sequentially using the [[SimpleStream]] interface.
  *
  * @param cobolSchema        A parsed copybook.
  * @param dataStream         A source of bytes for sequential reading and parsing. It should implement [[SimpleStream]] interface.
  * @param readerProperties   Additional properties for customizing the reader.
  * @param recordHeaderParser A record parser for multisegment files
  * @param rawRecordExtractor A raw record extractor, if available
  * @param fileId             A FileId to put to the corresponding column
  * @param startRecordId      A starting record id value for this particular file/stream `dataStream`
  * @param startingFileOffset An offset of the file where parsing should be started
  */
@throws(classOf[IllegalStateException])
final class VarLenHierarchicalIterator[T: ClassTag](
                                                     cobolSchema: Copybook,
                                                     dataStream: SimpleStream,
                                                     readerProperties: ReaderParameters,
                                                     recordHeaderParser: RecordHeaderParser,
                                                     rawRecordExtractor: Option[RawRecordExtractor],
                                                     fileId: Int,
                                                     startRecordId: Long,
                                                     startingFileOffset: Long,
                                                     handler: RecordHandler[T]) extends Iterator[Seq[Any]] {

  type RawData = Array[Byte]
  type RawRecord = (String, Array[Byte])

  private val rawRecordIterator = new VRLRecordReader(cobolSchema, dataStream, readerProperties, recordHeaderParser, rawRecordExtractor, startRecordId, startingFileOffset)

  private var recordIndex = startRecordId
  private var cachedValue: Option[Seq[Any]] = _
  private val segmentRedefines = cobolSchema.getAllSegmentRedefines.toArray

  private val segmentIdRedefines = readerProperties.multisegment.map(p => p.segmentIdRedefineMap).get.toList
  private val generateInputFileName = readerProperties.inputFileNameColumn.nonEmpty

  private val rootSegmentIds = segmentRedefines
    .filter(_.parentSegment.isEmpty)
    .flatMap(rootField => {
      segmentIdRedefines.filter(a => a._2 == rootField.name).map(_._1)
    })

  private val parentChildrenMap = cobolSchema.getParentChildrenSegmentMap

  private val segmentRedefineMap: Map[String, Group] = readerProperties.multisegment.map(
    _.segmentIdRedefineMap.map { case (segmentId, redefineName) =>
      val redefineField = segmentRedefines.find(_.name == redefineName).get
      (segmentId, redefineField)
    }
  ).getOrElse(HashMap[String, Group]())

  // Contains all segments for the current root
  private var fetchedRecords = ArrayBuffer[RawRecord]()

  fetchNext()

  override def hasNext: Boolean = cachedValue.nonEmpty

  @throws(classOf[IllegalStateException])
  @throws(classOf[NoSuchElementException])
  override def next(): Seq[Any] = {
    cachedValue match {
      case None => throw new NoSuchElementException
      case Some(value) =>
        fetchNext()
        value
    }
  }

  @throws(classOf[IllegalStateException])
  private def fetchNext(): Unit = {
    var recordFetched = false
    while (!recordFetched) {
      if (rawRecordIterator.hasNext) {
        val record = rawRecordIterator.next()

        record match {
          case (segmentIdStr, _) =>
            if (isRootSegment(segmentIdStr)) {
              if (fetchedRecords.nonEmpty) {
                // Extract an accumulated record
                val row: Seq[Any] = extractRow(fetchedRecords)
                fetchedRecords = ArrayBuffer[RawRecord]()
                cachedValue = Some(row)
                recordFetched = true
              }
              fetchedRecords += record
            } else {
              if (isRootSegmentReached) {
                fetchedRecords += record
              }
            }
        }
        recordIndex = recordIndex + 1
      } else {
        if (fetchedRecords.nonEmpty) {
          // Extract the last record
          val row: Seq[Any] = extractRow(fetchedRecords)
          fetchedRecords = ArrayBuffer[RawRecord]()
          cachedValue = Some(row)
          recordIndex = recordIndex + 1
        } else {
          cachedValue = None
        }
        recordFetched = true
      }
    }
  }

  private def extractRow(records: ArrayBuffer[RawRecord]): Seq[Any] = {
    RecordExtractors.extractHierarchicalRecord(cobolSchema.getCobolSchema,
      records,
      segmentRedefines,
      segmentRedefineMap,
      parentChildrenMap,
      readerProperties.startOffset,
      readerProperties.schemaPolicy,
      readerProperties.variableSizeOccurs,
      readerProperties.generateRecordId,
      fileId,
      recordIndex,
      generateInputFileName,
      dataStream.inputFileName,
      handler
    )
  }

  private def isRootSegment(segmentId: String): Boolean = {
    rootSegmentIds.contains(segmentId)
  }

  private def isRootSegmentReached: Boolean = fetchedRecords.nonEmpty

}
