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
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.reader.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.reader.fixedlen.{FixedLenNestedReader => ReaderFixedLenNestedReader}
import za.co.absa.cobrix.cobol.reader.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.SparkCobolRowType.{GenericRowSparkCobol, rowCreator}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema


/**
  *  The Cobol data reader that produces nested structure schema
  *
  * @param copyBookContents    A copybook contents.
  * @param startOffset         Specifies the number of bytes at the beginning of each record that can be ignored.
  * @param endOffset           Specifies the number of bytes at the end of each record that can be ignored.
  * @param schemaRetentionPolicy              Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  */
final class FixedLenNestedReader(copyBookContents: Seq[String],
                                 isEbcdic: Boolean = true,
                                 ebcdicCodePage: CodePage,
                                 floatingPointFormat: FloatingPointFormat,
                                 startOffset: Int = 0,
                                 endOffset: Int = 0,
                                 schemaRetentionPolicy: SchemaRetentionPolicy,
                                 stringTrimmingPolicy: StringTrimmingPolicy,
                                 dropGroupFillers: Boolean,
                                 nonTerminals: Seq[String],
                                 readerProperties: ReaderParameters
                                 )
  extends ReaderFixedLenNestedReader[GenericRowSparkCobol](
    copyBookContents, isEbcdic, ebcdicCodePage, floatingPointFormat,
    startOffset, endOffset, schemaRetentionPolicy, stringTrimmingPolicy,
    dropGroupFillers, nonTerminals, readerProperties,
    rowCreator
  ) with FixedLenReader with Serializable {

  class RowIterator(private val iterator: Iterator[Seq[Any]]) extends Iterator[Row] {
    override def hasNext: Boolean = iterator.hasNext

    @throws(classOf[IllegalStateException])
    override def next(): Row = Row.fromSeq(iterator.next())
  }

  override def getCobolSchema: CobolSchema = CobolSchema.fromBaseReader(cobolSchema)

  override def getSparkSchema: StructType = getCobolSchema.getSparkSchema

  @throws(classOf[Exception])
  override def getRowIterator(binaryData: Array[Byte]): Iterator[Row] = {
    checkBinaryDataValidity(binaryData)
    new RowIterator(getRecordIterator(binaryData))
  }
}
