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

package za.co.absa.cobrix.cobol.parser.reader.iterator

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.common.DataExtractors
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream

class VarLenIterator(cobolSchema: Copybook,
                     dataStream: SimpleStream,
                     lengthFieldName: String,
                     startOffset: Int = 0,
                     endOffset: Int = 0) extends Iterator[Seq[Any]] {

  private val copyBookRecordSize = cobolSchema.getRecordSize
  private var byteIndex = 0L
  private var cachedValue: Option[Seq[Any]] = _
  private val lengthField = getLengthField

  fetchNext()

  override def hasNext: Boolean = cachedValue.nonEmpty

  override def next(): Seq[Any] = {
    cachedValue match {
      case None => throw new NoSuchElementException
      case Some(value) =>
        fetchNext()
        value
    }
  }

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

    cachedValue = Some(DataExtractors.extractValues(cobolSchema.getCobolSchema, binaryData, startOffset))
  }

  private def getLengthField: Primitive = {
    val field = cobolSchema.getFieldByName(lengthFieldName)
    field match {
      case s: Primitive =>
        s.dataType match {
          case _: za.co.absa.cobrix.cobol.parser.ast.datatype.Integral => //ok
          case _ => throw new IllegalStateException(s"The record length field $lengthFieldName must be an integral type.")
        }
        s
      case _ =>
        throw new IllegalStateException(s"The record length field $lengthFieldName must be an primitive integral type.")
    }
  }

}
