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
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

/**
  *  This iterator is used to variable length data sequentially using the [[SimpleStream]] interface.
  *
  * @param cobolSchema           A parsed copybook.
  * @param dataStream            A source of bytes for sequential reading and parsing. It should implement [[SimpleStream]] interface.
  * @param lengthFieldName       A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param startOffset           An offset to the start of the record in each binary data block.
  * @param endOffset             An offset from the end of the record to the end of the binary data block.
  * @param generateRecordId      If true, a record id field will be prepended to each record.
  * @param policy                Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param fileId                A FileId to put to the corresponding column
  * @param startRecordId         A starting record id value for this particular file/stream `dataStream`
  */
@throws(classOf[IllegalStateException])
class VarLenNestedIterator(cobolSchema: Copybook,
                           dataStream: SimpleStream,
                           lengthFieldName: Option[String],
                           startOffset: Int,
                           endOffset: Int,
                           generateRecordId: Boolean,
                           policy: SchemaRetentionPolicy,
                           fileId: Int,
                           startRecordId: Long) extends Iterator[Row] {

  private val copyBookRecordSize = cobolSchema.getRecordSize
  private var byteIndex = 0L
  private var recordIndex = startRecordId
  private var cachedValue: Option[Row] = _
  private val lengthField = getLengthField

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
    val binaryData = dataStream.next(startOffset + copyBookRecordSize)

    if (binaryData.length < startOffset + copyBookRecordSize) {
      cachedValue = None
      return
    }

    val recordLength = if (lengthField.isDefined) {
      cobolSchema.extractPrimitiveField(lengthField.get, binaryData, startOffset) match {
        case i: Int => i
        case l: Long => l.toInt
        case s: String => s.toInt
        case _=> throw new IllegalStateException(s"Record length value of the field $lengthFieldName must be an integral type.")
      }
    } else {
      copyBookRecordSize
    }

    // Drop out of the record values
    val dropLength = recordLength - copyBookRecordSize + endOffset
    if (dropLength > 0)
      dataStream.next(dropLength)

    val dataBits = BitVector(binaryData)

    cachedValue = Some(RowExtractors.extractRecord(cobolSchema.getCobolSchema, dataBits, startOffset * 8, generateRecordId, policy, fileId, recordIndex) )

    recordIndex = recordIndex + 1
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
