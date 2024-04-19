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

package za.co.absa.cobrix.cobol.reader

import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat.{AsciiText, FixedLength}
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordHandler
import za.co.absa.cobrix.cobol.reader.iterator.FixedLenNestedRowIterator
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema

import scala.reflect.ClassTag

/**
  * The Cobol data reader that produces nested structure schema
  *
  * @param copyBookContents      A copybook contents.
  */
class FixedLenNestedReader[T: ClassTag](copyBookContents: Seq[String],
                                        readerProperties: ReaderParameters,
                                        handler: RecordHandler[T]) extends FixedLenReader with Serializable {

  protected val cobolSchema: CobolSchema = loadCopyBook(copyBookContents)

  override def getCobolSchema: CobolSchema = cobolSchema

  override def getRecordSize: Int = {
    val recordInternalsSize = readerProperties.recordLength.getOrElse(cobolSchema.getRecordSize)
    recordInternalsSize + readerProperties.startOffset + readerProperties.endOffset
  }

  @throws(classOf[Exception])
  override def getRecordIterator(binaryData: Array[Byte]): Iterator[Seq[Any]] = {
    checkBinaryDataValidity(binaryData)
    val singleRecordIterator = readerProperties.recordFormat == AsciiText || readerProperties.recordFormat == FixedLength
    new FixedLenNestedRowIterator(binaryData, cobolSchema, readerProperties, readerProperties.startOffset, readerProperties.endOffset, singleRecordIterator, handler)
  }

  def checkBinaryDataValidity(binaryData: Array[Byte]): Unit = {
    if (readerProperties.startOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record start offset = ${readerProperties.startOffset}. A record start offset cannot be negative.")
    }
    if (readerProperties.endOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record end offset = ${readerProperties.endOffset}. A record end offset cannot be negative.")
    }
    readerProperties.recordLength match {
      case Some(len) =>
        if (len < 1) {
          throw new IllegalArgumentException(s"The specified record size $len cannot be used. The record length should be greater then zero.")
        }
      case None      =>
        if (binaryData.length < getExpectedLength) {
          throw new IllegalArgumentException(s"Binary record too small. Expected binary record size = $getExpectedLength, got ${binaryData.length} ")
        }
        if (binaryData.length % getExpectedLength > 0) {
          throw new IllegalArgumentException(s"Binary record size $getExpectedLength does not divide data size ${binaryData.length}.")
        }
    }
  }

  private def getExpectedLength: Int = {
    cobolSchema.getRecordSize + readerProperties.startOffset + readerProperties.endOffset
  }

  private def loadCopyBook(copyBookContents: Seq[String]): CobolSchema = {
    CobolSchema.fromReaderParameters(copyBookContents, readerProperties)
  }
}
