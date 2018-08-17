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

package za.co.absa.cobrix.spark.cobol.reader.varlen

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.varlen.iterator.VarLenNestedIterator
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema


/**
  *  The Cobol data reader for variable length records that gets input binary data as a stream and produces nested structure schema
  *
  * @param copybookContents      The contents of a copybook.
  * @param lengthFieldName       A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param startOffset           An offset to the start of the record in each binary data block.
  * @param endOffset             An offset from the end of the record to the end of the binary data block.
  * @param generateRecordId      If true, a record id field will be prepended to each record.
  * @param startRecordId         A starting record id value
  * @param recordIdFileIncrement An increment for record id when switching between binary files
  */
@throws(classOf[IllegalArgumentException])
class VarLenNestedReader(copybookContents: String,
                         lengthFieldName: Option[String],
                         startOffset: Int = 0,
                         endOffset: Int = 0,
                         generateRecordId: Boolean = false,
                         startRecordId: Long = 1,
                         recordIdFileIncrement: Long = 1000000) extends VarLenReader {

  private val cobolSchema: CobolSchema = loadCopyBook(copybookContents)

  checkInputArgumentsValidity()

  override def getCobolSchema: CobolSchema = cobolSchema

  override def getSparkSchema: StructType = cobolSchema.getSparkSchema

  override def getRowIterator(binaryData: SimpleStream, fileNumber: Int): Iterator[Row] =
    new VarLenNestedIterator(cobolSchema.copybook, binaryData, lengthFieldName, startOffset, endOffset, generateRecordId, startRecordId +
      recordIdFileIncrement * fileNumber)

  private def loadCopyBook(copyBookContents: String): CobolSchema = {
    val schema = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    new CobolSchema(schema, generateRecordId)
  }

  override def getRecordStartOffset: Int = startOffset

  override def getRecordEndOffset: Int = endOffset

  @throws(classOf[IllegalArgumentException])
  private def checkInputArgumentsValidity(): Unit = {
    if (startOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record start offset = $startOffset. A record start offset cannot be negative.")
    }
    if (endOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record end offset = $endOffset. A record end offset cannot be negative.")
    }
  }

}
