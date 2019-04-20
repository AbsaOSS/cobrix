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

package za.co.absa.cobrix.spark.cobol.reader.varlen.iterator

import org.apache.spark.sql.Row
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParser
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.validator.ReaderParametersValidator
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer

/**
  * This iterator is used to variable length data sequentially using the [[SimpleStream]] interface.
  *
  * @param cobolSchema        A parsed copybook.
  * @param dataStream         A source of bytes for sequential reading and parsing. It should implement [[SimpleStream]] interface.
  * @param readerProperties   Additional properties for customizing the reader.
  * @param recordHeaderParser A record parser for multisegment files
  * @param fileId             A FileId to put to the corresponding column
  * @param startRecordId      A starting record id value for this particular file/stream `dataStream`
  * @param startingFileOffset An offset of the file where parsing should be started
  * @param segmentIdPrefix    A prefix to be used for all segment ID generated fields
  */
@throws(classOf[IllegalStateException])
final class VarLenNestedIterator(cobolSchema: Copybook,
                                 dataStream: SimpleStream,
                                 readerProperties: ReaderParameters,
                                 recordHeaderParser: RecordHeaderParser,
                                 fileId: Int,
                                 startRecordId: Long,
                                 startingFileOffset: Long,
                                 segmentIdPrefix: String) extends Iterator[Row] {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val copyBookRecordSize = cobolSchema.getRecordSize
  private var byteIndex = startingFileOffset
  private var recordIndex = startRecordId
  private var cachedValue: Option[Row] = _
  private val lengthField = ReaderParametersValidator.getLengthField(readerProperties.lengthFieldName, cobolSchema)
  private val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, cobolSchema)
  private val segmentIdFilter = readerProperties.multisegment.flatMap(p => p.segmentIdFilter)
  private val segmentIdAccumulator = readerProperties.multisegment.map(p => new SegmentIdAccumulator(p.segmentLevelIds, segmentIdPrefix, fileId))
  private val segmentLevelIdsCount = readerProperties.multisegment.map(p => p.segmentLevelIds.size).getOrElse(0)
  private val segmentRedefineMap = readerProperties.multisegment.map(_.segmentIdRedefineMap).getOrElse(HashMap[String, String]())
  private val segmentRedefineAvailable = segmentRedefineMap.nonEmpty

  fetchNext()

  override def hasNext: Boolean = cachedValue.nonEmpty

  @throws(classOf[IllegalStateException])
  @throws(classOf[NoSuchElementException])
  override def next(): Row = {
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
      val binaryData = if (readerProperties.isRecordSequence) {
        fetchRecordUsingRdwHeaders()
      } else if (lengthField.isDefined) {
        fetchRecordUsingRecordLengthField()
      } else {
        fetchRecordUsingCopybookRecordLength()
      }

      binaryData match {
        case None =>
          cachedValue = None
          recordFetched = true
        case Some(data) =>
          val segmentId = getSegmentId(data)
          val segmentIdStr = segmentId.getOrElse("")
          val segmentLevelIds = getSegmentLevelIds(segmentIdStr)

          if (isSegmentMatchesTheFilter(segmentIdStr)) {
            val segmentRedefine = if (segmentRedefineAvailable) {
              segmentRedefineMap.getOrElse(segmentIdStr, "")
            } else ""

            cachedValue = Some(RowExtractors.extractRecord(cobolSchema.getCobolSchema,
              data,
              readerProperties.startOffset,
              readerProperties.schemaPolicy,
              readerProperties.generateRecordId,
              segmentLevelIds,
              fileId,
              recordIndex,
              activeSegmentRedefine = segmentRedefine
            ))

            byteIndex += data.length
            recordFetched = true
          }
      }
      recordIndex = recordIndex + 1
    }
  }

  private def fetchRecordUsingRecordLengthField(): Option[Array[Byte]] = {
    if (lengthField.isEmpty) {
      throw new IllegalStateException(s"For variable length reader either RDW record headers or record length field should be provided.")
    }

    val lengthFieldBlock = lengthField.get.binaryProperties.offset + lengthField.get.binaryProperties.actualSize

    val binaryDataStart = dataStream.next(readerProperties.startOffset + lengthFieldBlock)

    if (binaryDataStart.length < readerProperties.startOffset + lengthFieldBlock) {
      return None
    }

    var recordLength = lengthField match {
      case Some(lengthAST) =>
        cobolSchema.extractPrimitiveField(lengthAST, binaryDataStart, readerProperties.startOffset) match {
          case i: Int => i
          case l: Long => l.toInt
          case s: String => s.toInt
          case _ => throw new IllegalStateException(s"Record length value of the field ${lengthAST.name} must be an integral type.")
        }
      case None => copyBookRecordSize
    }

    val restOfDataLength = recordLength - lengthFieldBlock + readerProperties.endOffset

    if (restOfDataLength > 0) {
      Some(binaryDataStart ++ dataStream.next(restOfDataLength))
    } else {
      Some(binaryDataStart)
    }
  }

  private def fetchRecordUsingRdwHeaders(): Option[Array[Byte]] = {
    val rdwHeaderBlock = recordHeaderParser.getHeaderLength

    val binaryDataStart = dataStream.next(rdwHeaderBlock)

    // ToDo skip records for which `isValid` is set to `false` in metadata
    val recordMetadata = recordHeaderParser.getRecordMetadata(binaryDataStart, byteIndex)
    val recordLength = recordMetadata.recordLength

    byteIndex += binaryDataStart.length

    if (recordLength > 0) {
      Some(dataStream.next(recordLength))
    } else {
      None
    }
  }

  private def fetchRecordUsingCopybookRecordLength(): Option[Array[Byte]] = {
    val recordLength = cobolSchema.getRecordSize

    val bytes = dataStream.next(recordLength)

    if (bytes.length < recordLength) {
      None
    } else {
      Some(bytes)
    }
  }

  // The gets all values for the helper fields for the current record having a specific segment id
  // It is deliberately wtitten imperative style for performance
  private def getSegmentLevelIds(segmentId: String): Seq[Any] = {
    if (segmentLevelIdsCount > 0 && segmentIdAccumulator.isDefined) {
      val acc = segmentIdAccumulator.get
      acc.acquiredSegmentId(segmentId, recordIndex)
      val ids = new ListBuffer[Any]
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

  private def getSegmentId(data: Array[Byte]): Option[String] = {
    segmentIdField.map(field => {
      val fieldValue = cobolSchema.extractPrimitiveField(field, data, readerProperties.startOffset)
      if (fieldValue == null) {
        logger.error(s"An unexpected null encountered for segment id at $byteIndex")
        ""
      } else {
        fieldValue.toString.trim
      }
    }
    )
  }

  private def isSegmentMatchesTheFilter(segmentId: String): Boolean = {
    segmentIdFilter
      .forall(filter => filter.contains(segmentId))
  }
}
