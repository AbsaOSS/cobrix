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
import za.co.absa.cobrix.cobol.reader.iterator.{VarLenHierarchicalIterator, VarLenNestedIterator}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream
import za.co.absa.cobrix.cobol.reader.{VarLenNestedReader => ReaderVarLenNestedReader}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema


/**
  *  The Cobol data reader for variable length records that gets input binary data as a stream and produces nested structure schema
  *
  * @param copybookContents      The contents of a copybook.
  * @param readerProperties      Additional properties for customizing the reader.
  */
@throws(classOf[IllegalArgumentException])
final class VarLenNestedReader(copybookContents: Seq[String],
                         readerProperties: ReaderParameters
) extends ReaderVarLenNestedReader[GenericRow](copybookContents, readerProperties, new RowHandler())
  with VarLenReader {

  class RowIterator(private val iterator: Iterator[Seq[Any]]) extends Iterator[Row] {
    override def hasNext: Boolean = iterator.hasNext

    @throws(classOf[IllegalStateException])
    override def next(): Row = Row.fromSeq(iterator.next())
  }


  override def getCobolSchema: CobolSchema = CobolSchema.fromBaseReader(cobolSchema)

  override def getSparkSchema: StructType = getCobolSchema.getSparkSchema


  override def getRowIterator(binaryData: SimpleStream,
                              startingFileOffset: Long,
                              fileNumber: Int,
                              startingRecordIndex: Long): Iterator[Row] =
    if (cobolSchema.copybook.isHierarchical) {
      new RowIterator(
        new VarLenHierarchicalIterator(cobolSchema.copybook, binaryData, readerProperties, recordHeaderParser,
          recordExtractor(binaryData, cobolSchema.copybook),
          fileNumber, startingRecordIndex, startingFileOffset, new RowHandler())
      )
    } else {
      new RowIterator(
        new VarLenNestedIterator(cobolSchema.copybook, binaryData, readerProperties, recordHeaderParser,
          recordExtractor(binaryData, cobolSchema.copybook),
          fileNumber, startingRecordIndex, startingFileOffset, cobolSchema.segmentIdPrefix, new RowHandler())
      )
    }
}
