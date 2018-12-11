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
import za.co.absa.cobrix.cobol.parser.decoders.BinaryUtils
import za.co.absa.cobrix.cobol.parser.stream.{SimpleMemoryStream, SimpleStream}
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

/**
  * This iterator is used to variable length data sequentially using the [[za.co.absa.cobrix.cobol.parser.stream.SimpleStream]] interface.
  * This iterator fetches rows from binary data stream by searching the stream for the signature sequence of bytes.
  * This sequence of bytes we call signature. Once a signature is found in the stream a row is extracted.
  * As an additional check is a length field is specified the value of the field is checked against the expected range of values.
  *
  * @param cobolSchema A parsed copybook.
  * @param dataStream  A source of bytes for sequential reading and parsing. It should implement
  *                    [[za.co.absa.cobrix.cobol.parser.stream.SimpleStream]] interface.
  * @param signatureFieldName  The name of the field that contains the signature.
  * @param signatureFieldValue The value of the signature should match.
  * @param lengthFieldName  A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param minimumLength  The mininum possible value of the length field.
  * @param maximumLength  The maximum possible value of the length field.
  * @param startOffset  An offset to the start of the record in each binary data block.
  * @param endOffset  An offset from the end of the record to the end of the binary data block.
  * @param generateRecordId  If true, a record id field will be prepended to each record.
  * @param fileId  A FileId to put to the corresponding column
  * @param policy  Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  */
@throws(classOf[IllegalStateException])
@deprecated("This class is deprecated. It will beremoved in the future versions")
final class VarLenSearchIterator(cobolSchema: Copybook,
                           dataStream: SimpleStream,
                           signatureFieldName: String,
                           signatureFieldValue: String,
                           lengthFieldName: Option[String],
                           minimumLength: Option[Int],
                           maximumLength: Option[Int],
                           startOffset: Int,
                           endOffset: Int,
                           generateRecordId: Boolean,
                           fileId: Int,
                           policy: SchemaRetentionPolicy) extends Iterator[Row] {

  private val copyBookRecordSize = cobolSchema.getRecordSize
  private var byteIndex = 0L
  private var cachedValue: Option[Row] = _
  private var recordIndex = 0
  private val lengthField = getLengthField

  private val signatureField = cobolSchema.getFieldByName(signatureFieldName)
  private val signature: Array[Byte] = signatureFieldValue.toCharArray.map(char => BinaryUtils.asciiToEbcdic(char))

  private val memoryStream = new SimpleMemoryStream(dataStream, copyBookRecordSize * 3)
  private val buffer = new Array[Byte](copyBookRecordSize + startOffset + endOffset)

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
    val signatureOffset = signatureField.binaryProperties.offset

    def isLengthFieldValid(lengthFieldValue: Int): Boolean = {
      val lengthValid = for (minValue <- minimumLength;
                             maxValue <- maximumLength) yield lengthFieldValue > minValue && lengthFieldValue < maxValue

      if (lengthValid.isDefined) {
        lengthValid.get
      } else {
        true
      }
    }

    def advanceByteIndex(recordMarkStart: Long, lengthFieldValue: Int, isFound: Boolean): Unit = {
      byteIndex = if (isFound) {
        if (lengthField.isDefined && lengthFieldValue > 0)
          recordMarkStart + lengthFieldValue
        else
          recordMarkStart + copyBookRecordSize / 2
      } else {
        recordMarkStart + signatureOffset
      }
    }

    var isFound = false
    var recordMarkStart = byteIndex

    while (!isFound) {
      recordMarkStart = memoryStream.search(signature, byteIndex)

      if (recordMarkStart < byteIndex) {
        cachedValue = None
        return
      }

      val fullRecordSize = copyBookRecordSize + startOffset + endOffset

      val bytesRead = memoryStream.getBytes(buffer, recordMarkStart - signatureOffset, recordMarkStart + fullRecordSize - signatureOffset - 1)

      val minimumBytesRequired = getMinimumParsableNumOfBytes
      if (bytesRead < minimumBytesRequired) {
        cachedValue = None
        return
      }

      val lengthFieldValue = getLengthFieldValue
      isFound = isLengthFieldValid(lengthFieldValue)

      // Advance to the next search point
      advanceByteIndex(recordMarkStart, lengthFieldValue, isFound)
    }

    cachedValue = Some(RowExtractors.extractRecord(cobolSchema.getCobolSchema, buffer, startOffset, policy, generateRecordId, Nil, fileId,
      /*recordMarkStart - signatureOffset)*/
      recordIndex))
    recordIndex += 1
  }

  private def getMinimumParsableNumOfBytes: Int = {
    lengthField match {
      case Some(lengthFld) => lengthFld.binaryProperties.offset + lengthFld.binaryProperties.actualSize
      case None => copyBookRecordSize + startOffset + endOffset
    }
  }

  private def getLengthFieldValue: Int = {
    if (lengthField.isDefined) {
      cobolSchema.extractPrimitiveField(lengthField.get, buffer, startOffset) match {
        case i: Int => i
        case l: Long => l.toInt
        case s: String => s.toInt
        case _ => throw new IllegalStateException(s"Record length value of the field $lengthFieldName must be an integral type.")
      }
    } else {
      0
    }
  }

  @throws(classOf[IllegalStateException])
  private def getLengthField: Option[Primitive] = {
    lengthFieldName.flatMap(fieldName => {
      val field = cobolSchema.getFieldByName(fieldName)
      val astNode = field match {
        case s: Primitive =>
          if (!s.dataType.isInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral]) {
            throw new IllegalStateException(s"The record length field $lengthFieldName must be an integral type.")
          }
          s
        case _ =>
          throw new IllegalStateException(s"The record length field $lengthFieldName must be an primitive integral type.")
      }
      Some(astNode)
    })
  }

}
