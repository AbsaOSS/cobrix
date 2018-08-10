/*
 * Copyright 2018 Barclays Africa Group Limited
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
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

@throws(classOf[IllegalStateException])
class VarLenNestedIterator(cobolSchema: Copybook,
                           dataStream: SimpleStream,
                           lengthFieldName: String,
                           startOffset: Int = 0,
                           endOffset: Int = 0) extends Iterator[Row] {

  private val copyBookRecordSize = cobolSchema.getRecordSize
  private var byteIndex = 0L
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

    val recordLength = cobolSchema.extractPrimitiveField(lengthField, binaryData, startOffset) match {
      case i: Int => i
      case l: Long => l.toInt
      case s: String => s.toInt
      case _=> throw new IllegalStateException(s"Record length value of the field $lengthFieldName must be an integral type.")
    }

    // Drop out of the record values
    val dropLength = recordLength - copyBookRecordSize + endOffset
    if (dropLength > 0)
      dataStream.next(dropLength)

    val dataBits = BitVector(binaryData)

    cachedValue = Some(RowExtractors.extractRecord(cobolSchema.getCobolSchema, dataBits, startOffset * 8))
  }

  @throws(classOf[IllegalStateException])
  private def getLengthField: Primitive = {
    val field = cobolSchema.getFieldByName(lengthFieldName)
    field match {
      case s: Primitive =>
        if (!s.dataType.isInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral]) {
          throw new IllegalStateException(s"The record length field $lengthFieldName must be an integral type.")
        }
        s
      case _ =>
        throw new IllegalStateException(s"The record length field $lengthFieldName must be an primitive integral type.")
    }
  }
}