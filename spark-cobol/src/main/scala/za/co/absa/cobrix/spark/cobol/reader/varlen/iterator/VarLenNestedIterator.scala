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
  private val lengthField = ReaderParametersValidator.getLengthField(readerProperties, cobolSchema)

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

    val lengthFieldBlock = lengthField.get.binaryProperties.offset / 8 + lengthField.get.binaryProperties.actualSize / 8

    val binaryData = dataStream.next(readerProperties.startOffset + lengthFieldBlock)

    if (binaryData.length < readerProperties.startOffset + lengthFieldBlock) {
      cachedValue = None
      return
    }

    var recordLength = lengthField match {
      case Some(lengthAST) =>
        cobolSchema.extractPrimitiveField(lengthAST, binaryData, readerProperties.startOffset) match {
          case i: Int => i
          case l: Long => l.toInt
          case s: String => s.toInt
          case _ => throw new IllegalStateException(s"Record length value of the field ${lengthAST.name} must be an integral type.")
        }
      case None => copyBookRecordSize
    }

    val restOfDataLength = recordLength - lengthFieldBlock + readerProperties.endOffset

    val binaryDataFull = if (restOfDataLength > 0) {
      binaryData ++ dataStream.next(restOfDataLength)
    } else {
      binaryData
    }

    val dataBits = BitVector(binaryDataFull)

    cachedValue = Some(RowExtractors.extractRecord(cobolSchema.getCobolSchema, dataBits, readerProperties.startOffset * 8, readerProperties
      .generateRecordId, readerProperties.policy, fileId, recordIndex))

    byteIndex += binaryDataFull.length
    recordIndex = recordIndex + 1
  }
}
