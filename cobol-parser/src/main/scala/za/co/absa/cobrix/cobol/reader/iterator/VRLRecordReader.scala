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

import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParser
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream
import za.co.absa.cobrix.cobol.reader.validator.ReaderParametersValidator

/**
  * This iterator is used to read fixed length and variable length records from a binary COBOL-generated file.
  * It returns a pair of (segment_id, raw_data_byte_array) without actually decoding the data.
  *
  * @param cobolSchema        A parsed copybook.
  * @param dataStream         A source of bytes for sequential reading and parsing. It should implement [[SimpleStream]] interface.
  * @param readerProperties   Additional properties for customizing the reader.
  * @param recordHeaderParser A record parser for multisegment files
  * @param recordExtractor    A record extractor that can be used instead of the record header parser.
  * @param startRecordId      A starting record id value for this particular file/stream `dataStream`
  * @param startingFileOffset An offset of the file where parsing should be started
  */
class VRLRecordReader(cobolSchema: Copybook,
                      dataStream: SimpleStream,
                      readerProperties: ReaderParameters,
                      recordHeaderParser: RecordHeaderParser,
                      recordExtractor: Option[RawRecordExtractor],
                      startRecordId: Long,
                      startingFileOffset: Long) extends Iterator[(String, Array[Byte])] with Logging {

  type RawRecord = (String, Array[Byte])

  private var cachedValue: Option[RawRecord] = _
  private var byteIndex = startingFileOffset
  private var recordIndex = startRecordId - 1

  final private val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, cobolSchema)
  final private val minimumRecordLength = readerProperties.minimumRecordLength
  final private val maximumRecordLength = readerProperties.maximumRecordLength

  fetchNext()

  override def hasNext: Boolean = cachedValue.nonEmpty

  @throws(classOf[IllegalStateException])
  @throws(classOf[NoSuchElementException])
  override def next(): RawRecord = {
    cachedValue match {
      case None => throw new NoSuchElementException
      case Some(value) =>
        fetchNext()
        recordIndex = recordIndex + 1
        value
    }
  }

  @throws(classOf[IllegalStateException])
  private def fetchNext(): Unit = {
    var recordFetched = false
    while (!recordFetched) {
      val binaryData = recordExtractor match {
        case Some(extractor) =>
          if (extractor.hasNext) {
            Option(extractor.next())
          } else {
            None
          }
        case None =>
          fetchRecordUsingRdwHeaders()
      }

      binaryData match {
        case None =>
          cachedValue = None
          recordFetched = true
        case Some(data) if data.length < minimumRecordLength || data.length > maximumRecordLength =>
          recordFetched = false
        case Some(data) =>
          val segmentId = getSegmentId(data)
          val segmentIdStr = segmentId.getOrElse("")

          cachedValue = Some(segmentIdStr, data)
          recordFetched = true
      }
    }
  }

  def getRecordIndex: Long = recordIndex

  private def fetchRecordUsingRdwHeaders(): Option[Array[Byte]] = {
    val rdwHeaderBlock = recordHeaderParser.getHeaderLength

    var isValidRecord = false
    var isEndOfFile = false
    var headerBytes = Array[Byte]()
    var recordBytes = Array[Byte]()

    while (!isValidRecord && !isEndOfFile) {
      headerBytes = dataStream.next(rdwHeaderBlock)

      val recordMetadata = recordHeaderParser.getRecordMetadata(headerBytes, dataStream.offset, dataStream.size, dataStream.totalSize, recordIndex)
      val recordLength = recordMetadata.recordLength

      byteIndex += headerBytes.length

      isValidRecord = recordMetadata.isValid && recordLength >= minimumRecordLength && recordLength <= maximumRecordLength

      if (recordLength > 0) {
        recordBytes = dataStream.next(recordLength)
        byteIndex += recordBytes.length
      } else {
        isEndOfFile = true
      }
    }

    if (!isEndOfFile) {
      if (recordHeaderParser.isHeaderDefinedInCopybook) {
        Some(headerBytes ++ recordBytes)
      } else {
        Some(recordBytes)
      }
    } else {
      None
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
    })
  }

}
