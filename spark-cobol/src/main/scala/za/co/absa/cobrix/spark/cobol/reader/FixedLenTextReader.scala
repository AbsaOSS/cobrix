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

package za.co.absa.cobrix.spark.cobol.reader

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.{FixedLenNestedReader => ReaderFixedLenNestedReader}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema


/**
  *  The Cobol data reader from text files that produces nested structure schema
  *
  * @param copyBookContents    A copybook contents.
  * @param readerProperties    Properties reflecting parsing copybooks and decoding data.
  */
final class FixedLenTextReader(copyBookContents: Seq[String], readerProperties: ReaderParameters)
  extends ReaderFixedLenNestedReader[GenericRow](copyBookContents, readerProperties, new RowHandler()) with FixedLenReader with Serializable {

  class RowIterator(iterator: Iterator[Seq[Any]]) extends Iterator[Row] {
    override def hasNext: Boolean = iterator.hasNext

    @throws(classOf[IllegalStateException])
    override def next(): Row = Row.fromSeq(iterator.next())
  }

  override def getReaderProperties: ReaderParameters = readerProperties

  override def getCobolSchema: CobolSchema = CobolSchema.fromBaseReader(cobolSchema)

  override def getSparkSchema: StructType = getCobolSchema.getSparkSchema

  @throws(classOf[Exception])
  override def getRowIterator(binaryData: Array[Byte]): Iterator[Row] = {
    checkBinaryDataValidity(binaryData)
    new RowIterator(getRecordIterator(binaryData))
  }

  override def checkBinaryDataValidity(binaryData: Array[Byte]): Unit = {
    if (readerProperties.startOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record start offset = ${readerProperties.startOffset}. A record start offset cannot be negative.")
    }
    if (readerProperties.endOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record end offset = ${readerProperties.endOffset}. A record end offset cannot be negative.")
    }
  }
}
