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

package za.co.absa.cobrix.spark.cobol.reader.varlen

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.Constants
import za.co.absa.cobrix.spark.cobol.reader.index.IndexGenerator
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SimpleIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.validator.ReaderParametersValidator
import za.co.absa.cobrix.spark.cobol.reader.varlen.iterator.VarLenNestedIterator
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

import scala.collection.mutable.ArrayBuffer


/**
  *  The Cobol data reader for variable length records that gets input binary data as a stream and produces nested structure schema
  *
  * @param copybookContents      The contents of a copybook.
  * @param readerProperties      Additional properties for customizing the reader.
  */
@throws(classOf[IllegalArgumentException])
final class VarLenNestedReader(copybookContents: String,
                         readerProperties: ReaderParameters) extends VarLenReader {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val cobolSchema: CobolSchema = loadCopyBook(copybookContents)

  checkInputArgumentsValidity()

  override def getCobolSchema: CobolSchema = cobolSchema

  override def getSparkSchema: StructType = cobolSchema.getSparkSchema

  override def isIndexGenerationNeeded: Boolean = readerProperties.isXCOM && readerProperties.isIndexGenerationNeeded

  override def getRowIterator(binaryData: SimpleStream, startingFileOffset: Long, fileNumber: Int, startingRecordIndex: Long): Iterator[Row] =
    new VarLenNestedIterator(cobolSchema.copybook, binaryData, readerProperties, fileNumber, startingRecordIndex, startingFileOffset, cobolSchema.segmentIdPrefix)

  /**
    * Traverses the data sequentially as fast as possible to generate record index.
    * This index will be used to distribute workload of the conversion.
    *
    * @param binaryData A stream of input binary data
    * @param fileNumber A file number uniquely identified a particular file of the data set
    * @return An index of the file
    *
    */
  override def generateIndex(binaryData: SimpleStream, fileNumber: Int): ArrayBuffer[SimpleIndexEntry] = {
    var recordSize = cobolSchema.getRecordSize
    val recordsPerIndexEntry: Option[Int] = readerProperties.recordsPerPartition
    val indexEntryMinSize: Option[Int] = readerProperties.partitionSizeMB

    if (recordsPerIndexEntry.isDefined) {
      if (recordsPerIndexEntry.get < 1 || recordsPerIndexEntry.get > 1000000000) {
        throw new IllegalArgumentException(s"Invalid number of records per partition of ${recordsPerIndexEntry.get} MB.")
      }
      logger.warn(s"Minimum records per partition = ${recordsPerIndexEntry.get} MB")
    } else {
      if (indexEntryMinSize.nonEmpty) {
        if (indexEntryMinSize.get < 1 || indexEntryMinSize.get > 2000) {
          throw new IllegalArgumentException(s"Invalid requested partition size of ${indexEntryMinSize.get} MB.")
        }
        logger.warn(s"Minimum size per partition = ${indexEntryMinSize.get} MB")
      }
    }

    val copybook = cobolSchema.copybook
    val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, copybook)

    segmentIdField match {
      case Some(field) => IndexGenerator.simpleIndexGenerator(fileNumber, binaryData, recordsPerIndexEntry, indexEntryMinSize, Some(copybook), Some(field))
      case None => IndexGenerator.simpleIndexGenerator(fileNumber, binaryData, recordsPerIndexEntry, indexEntryMinSize)
    }
  }


  private def loadCopyBook(copyBookContents: String): CobolSchema = {
    val schema = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val segIdFieldCount = readerProperties.multisegment.map(p => p.segmentLevelIds.size).getOrElse(0)
    val segmentIdPrefix = readerProperties.multisegment.map(p => p.segmentIdPrefix).getOrElse("")
    new CobolSchema(schema, readerProperties.policy, readerProperties.generateRecordId, segIdFieldCount, segmentIdPrefix)
  }

  override def getRecordStartOffset: Int = readerProperties.startOffset

  override def getRecordEndOffset: Int = readerProperties.endOffset

  @throws(classOf[IllegalArgumentException])
  private def checkInputArgumentsValidity(): Unit = {
    if (readerProperties.startOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record start offset = ${readerProperties.startOffset}. A record start offset cannot be negative.")
    }
    if (readerProperties.endOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record end offset = ${readerProperties.endOffset}. A record end offset cannot be negative.")
    }
  }
}
