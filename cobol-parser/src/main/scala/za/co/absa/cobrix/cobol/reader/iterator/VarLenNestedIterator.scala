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
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParser
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream
import za.co.absa.cobrix.cobol.reader.extractors.record.{RecordHandler, RecordExtractors}

import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
  * This iterator is used to variable length data sequentially using the [[SimpleStream]] interface.
  *
  * @param cobolSchema        A parsed copybook.
  * @param dataStream         A source of bytes for sequential reading and parsing. It should implement [[SimpleStream]] interface.
  * @param readerProperties   Additional properties for customizing the reader.
  * @param recordHeaderParser A record parser for multisegment files
  * @param recordExtractor    A record extractor that can be used instead of the record header parser.
  * @param fileId             A FileId to put to the corresponding column
  * @param startRecordId      A starting record id value for this particular file/stream `dataStream`
  * @param startingFileOffset An offset of the file where parsing should be started
  * @param segmentIdPrefix    A prefix to be used for all segment ID generated fields
  */
final class VarLenNestedIterator[T: ClassTag](cobolSchema: Copybook,
                                              dataStream: SimpleStream,
                                              readerProperties: ReaderParameters,
                                              recordHeaderParser: RecordHeaderParser,
                                              recordExtractor: Option[RawRecordExtractor],
                                              fileId: Int,
                                              startRecordId: Long,
                                              startingFileOffset: Long,
                                              segmentIdPrefix: String,
                                              handler: RecordHandler[T]) extends Iterator[Seq[Any]] {

  private val rawRecordIterator = new VRLRecordReader(cobolSchema, dataStream, readerProperties, recordHeaderParser, recordExtractor, startRecordId, startingFileOffset)

  private var cachedValue: Option[Seq[Any]] = _
  private val segmentIdFilter = readerProperties.multisegment.flatMap(p => p.segmentIdFilter)
  private val segmentIdAccumulator = readerProperties.multisegment.map(p => new SegmentIdAccumulator(p.segmentLevelIds, segmentIdPrefix, fileId))
  private val segmentLevelIdsCount = readerProperties.multisegment.map(p => p.segmentLevelIds.size).getOrElse(0)
  private val segmentRedefineMap = readerProperties.multisegment.map(_.segmentIdRedefineMap).getOrElse(HashMap[String, String]())
  private val segmentRedefineAvailable = segmentRedefineMap.nonEmpty
  private val generateInputFileName = readerProperties.inputFileNameColumn.nonEmpty

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
          case (segmentIdStr, data) =>
            val segmentLevelIds = getSegmentLevelIds(segmentIdStr)

            if (isSegmentMatchesTheFilter(segmentIdStr, segmentLevelIds)) {
              val segmentRedefine = if (segmentRedefineAvailable) {
                segmentRedefineMap.getOrElse(segmentIdStr, "")
              } else ""

              cachedValue = Some(RecordExtractors.extractRecord(cobolSchema.getCobolSchema,
                data,
                readerProperties.startOffset,
                readerProperties.schemaPolicy,
                readerProperties.variableSizeOccurs,
                readerProperties.generateRecordId,
                segmentLevelIds,
                fileId,
                rawRecordIterator.getRecordIndex,
                activeSegmentRedefine = segmentRedefine,
                generateInputFileName,
                dataStream.inputFileName,
                handler
              ))

              recordFetched = true
            }
        }
      } else {
        cachedValue = None
        recordFetched = true
      }
    }
  }

  // The gets all values for the helper fields for the current record having a specific segment id
  // It is deliberately written imperative style for performance
  private def getSegmentLevelIds(segmentId: String): Seq[String] = {
    if (segmentLevelIdsCount > 0 && segmentIdAccumulator.isDefined) {
      val acc = segmentIdAccumulator.get
      acc.acquiredSegmentId(segmentId, rawRecordIterator.getRecordIndex)
      val ids = new ListBuffer[String]
      var i = 0
      while (i < segmentLevelIdsCount) {
        ids += acc.getSegmentLevelId(i)
        i += 1
      }
      ids
    } else {
      Nil
    }
  }

  private def isSegmentMatchesTheFilter(segmentId: String, segmentLevels: Seq[String]): Boolean = {
    if (!isRootSegmentReached(segmentLevels)) {
      false
    } else {
      segmentIdFilter
        .forall(filter => filter.contains(segmentId))
    }
  }

  private def isRootSegmentReached(segmentLevels: Seq[String]): Boolean = segmentLevels.isEmpty || segmentLevels.head != null
}
