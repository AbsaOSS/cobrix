/*
 * Copyright 2018-2019 ABSA Group Limited
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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParserFactory
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.index.IndexGenerator
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.varlen.iterator.VarLenSearchIterator
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.{CobolSchema, SchemaRetentionPolicy}

import scala.collection.mutable.ArrayBuffer


/**
  * The Cobol data reader for variable length records that gets input binary data as a stream and produces nested structure schema.
  * This reader fetches rows from binary data stream by searching the stream for the signature sequence of bytes.
  * This sequence of bytes we call signature. Once a signature is found in the stream a row is extracted.
  * As an additional check is a length field is specified the value of the field is checked against the expected range of values.
  *
  * Note. This is an EXPERIMENTAL reader and should be used with CAUTION.
  *
  * @param copybookContents The contents of a copybook.
  * @param signatureFieldName  The name of the field that contains the signature.
  * @param signatureFieldValue The value of the signature should match.
  * @param lengthFieldName  A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param minimumLength  The mininum possible value of the length field.
  * @param maximumLength  The maximum possible value of the length field.
  * @param startOffset  An offset to the start of the record in each binary data block.
  * @param endOffset  An offset from the end of the record to the end of the binary data block.
  * @param generateRecordId  If true, a record id field will be prepended to each record.
  * @param policy           Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the
  *                         copybook.
  */
@throws(classOf[IllegalArgumentException])
@deprecated("This class is deprecated. It will be removed in future versions")
final class VarLenSearchReader(copybookContents: String,
                         signatureFieldName: String,
                         signatureFieldValue: String,
                         lengthFieldName: Option[String],
                         minimumLength: Option[Int],
                         maximumLength: Option[Int],
                         startOffset: Int = 0,
                         endOffset: Int = 0,
                         generateRecordId: Boolean = false,
                         policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
                         dropGroupFillers: Boolean) extends VarLenReader {

  private val cobolSchema: CobolSchema = loadCopyBook(copybookContents)

  checkInputArgumentsValidity()

  override def getCobolSchema: CobolSchema = cobolSchema

  override def getSparkSchema: StructType = cobolSchema.getSparkSchema

  override def isIndexGenerationNeeded: Boolean = false

  override def isRdwBigEndian: Boolean = false

  override def getRowIterator(binaryData: SimpleStream, startingFileOffset: Long, fileNumber: Int, startingRecordIndex: Long): Iterator[Row] =
    new VarLenSearchIterator(cobolSchema.copybook, binaryData, signatureFieldName, signatureFieldValue, lengthFieldName, minimumLength,
      maximumLength, startOffset, endOffset, generateRecordId, fileNumber, policy)

  override def generateIndex(binaryData: SimpleStream, fileNumber: Int, isRdwBigEndian: Boolean): ArrayBuffer[SparseIndexEntry] = {
    val recordHeaderParser = RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwLittleEndian)
    IndexGenerator.sparseIndexGenerator(fileNumber, binaryData, isRdwBigEndian, recordHeaderParser)
  }
  private def loadCopyBook(copyBookContents: String): CobolSchema = {
    val schema = CopybookParser.parseTree(copyBookContents)
    new CobolSchema(schema, policy, generateRecordId)
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
    minimumLength.foreach(v => if (v<0) throw new IllegalArgumentException(s"Invalid minimum length value = $v. It cannot be negative.") )

    maximumLength.foreach(v => if (v<0) throw new IllegalArgumentException(s"Invalid maximum length value = $v. It cannot be negative.") )
  }
}
