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

package za.co.absa.cobrix.spark.cobol.reader.varlen.iterator

import org.apache.spark.sql.Row
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.validator.ReaderParametersValidator
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

/**
  * This iterator is used to variable length data sequentially using the [[SimpleStream]] interface.
  *
  * @param cobolSchema      A parsed copybook.
  * @param dataStream       A source of bytes for sequential reading and parsing. It should implement [[SimpleStream]] interface.
  * @param readerProperties Additional properties for customizing the reader.
  * @param fileId           A FileId to put to the corresponding column
  * @param startRecordId    A starting record id value for this particular file/stream `dataStream`
  */
@throws(classOf[IllegalStateException])
class VarLenNestedIterator(cobolSchema: Copybook,
                           dataStream: SimpleStream,
                           readerProperties: ReaderParameters,
                           fileId: Int,
                           startRecordId: Long) extends Iterator[Row] {

  private val copyBookRecordSize = cobolSchema.getRecordSize
  private var byteIndex = 0L
  private var recordIndex = startRecordId
  private var cachedValue: Option[Row] = _
  private val lengthField = ReaderParametersValidator.getLengthField(readerProperties.lengthFieldName, cobolSchema)
  private val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.segmentIdField, cobolSchema)
  private val segmentIdFilter = readerProperties.segmentIdFilter.getOrElse("").trim
  private val isFilteredBySegment = segmentIdField.isDefined && readerProperties.segmentIdFilter.isDefined

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
      val binaryData = if (readerProperties.isXCOM) {
        fetchRecordUsingXcomHeaders()
      } else {
        fetchRecordUsingRecordLengthField()
      }

      binaryData match {
        case None =>
          cachedValue = None
          recordFetched = true
        case Some(data) =>
          if (isSegmentMatchesTheFilter(data)) {
            cachedValue = Some(RowExtractors.extractRecord(cobolSchema.getCobolSchema,
              BitVector(data),
              readerProperties.startOffset * 8,
              readerProperties.generateRecordId,
              readerProperties.policy,
              fileId,
              recordIndex))

            byteIndex += data.length
            recordIndex = recordIndex + 1
            recordFetched = true
          }
      }
    }
  }

  private def fetchRecordUsingRecordLengthField(): Option[Array[Byte]] = {
    if (lengthField.isEmpty) {
      throw new IllegalStateException(s"For variable length reader either XCOM headers or record length field should be provided.")
    }

    val lengthFieldBlock = lengthField.get.binaryProperties.offset / 8 + lengthField.get.binaryProperties.actualSize / 8

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

  private def fetchRecordUsingXcomHeaders(): Option[Array[Byte]] = {
    val xcomHeaderBlock = 4

    val binaryDataStart = dataStream.next(xcomHeaderBlock)

    if (binaryDataStart.length < xcomHeaderBlock) {
      return None
    }

    var recordLength = (binaryDataStart(2) & 0xFF) + 256*(binaryDataStart(3) & 0xFF)

    if (recordLength > 0) {
      Some(dataStream.next(recordLength))
    } else {
      val xcomHeaders = binaryDataStart.map(_ & 0xFF).mkString(",")
      throw new IllegalStateException(s"XCOM headers should never be zero ($xcomHeaders). Found zero size record at $byteIndex.")
    }
  }

  private def isSegmentMatchesTheFilter(data: Array[Byte]): Boolean = {
    !isFilteredBySegment || cobolSchema.extractPrimitiveField(segmentIdField.get, data, readerProperties.startOffset).toString.trim == segmentIdFilter
  }
}
