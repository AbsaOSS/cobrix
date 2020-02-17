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

import java.nio.charset.{Charset, StandardCharsets}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC}
import za.co.absa.cobrix.cobol.parser.headerparsers.{RecordHeaderParser, RecordHeaderParserFactory}
import za.co.absa.cobrix.cobol.parser.recordextractors.{RawRecordExtractor, VarOccursRecordExtractor}
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.spark.cobol.reader.index.IndexGenerator
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.validator.ReaderParametersValidator
import za.co.absa.cobrix.spark.cobol.reader.varlen.iterator.{VarLenHierarchicalIterator, VarLenNestedIterator}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer


/**
  *  The Cobol data reader for variable length records that gets input binary data as a stream and produces nested structure schema
  *
  * @param copybookContents      The contents of a copybook.
  * @param readerProperties      Additional properties for customizing the reader.
  */
@throws(classOf[IllegalArgumentException])
final class VarLenNestedReader(copybookContents: Seq[String],
                         readerProperties: ReaderParameters) extends VarLenReader {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val cobolSchema: CobolSchema = loadCopyBook(copybookContents)

  private val recordHeaderParser: RecordHeaderParser = {
    getRecordHeaderParser
  }

  private def recordExtractor(binaryData: SimpleStream, copybook: Copybook): Option[RawRecordExtractor] = {
    if (readerProperties.variableSizeOccurs &&
      readerProperties.recordHeaderParser.isEmpty &&
      !readerProperties.isRecordSequence &&
      readerProperties.lengthFieldName.isEmpty) {
      Some(new VarOccursRecordExtractor(binaryData, copybook))
    } else {
      None
    }
  }

  checkInputArgumentsValidity()

  override def getCobolSchema: CobolSchema = cobolSchema

  override def getSparkSchema: StructType = cobolSchema.getSparkSchema

  override def isIndexGenerationNeeded: Boolean = (readerProperties.lengthFieldName.isEmpty || readerProperties.isRecordSequence) && readerProperties.isIndexGenerationNeeded

  override def isRdwBigEndian: Boolean = readerProperties.isRdwBigEndian

  override def getRowIterator(binaryData: SimpleStream,
                              startingFileOffset: Long,
                              fileNumber: Int,
                              startingRecordIndex: Long): Iterator[Row] =
    if (cobolSchema.copybook.isHierarchical) {
      new VarLenHierarchicalIterator(cobolSchema.copybook, binaryData, readerProperties, recordHeaderParser,
        fileNumber, startingRecordIndex, startingFileOffset)
    } else {
      new VarLenNestedIterator(cobolSchema.copybook, binaryData, readerProperties, recordHeaderParser, recordExtractor(binaryData, cobolSchema.copybook),
        fileNumber, startingRecordIndex, startingFileOffset, cobolSchema.segmentIdPrefix)
    }

  /**
    * Traverses the data sequentially as fast as possible to generate record index.
    * This index will be used to distribute workload of the conversion.
    *
    * @param binaryData A stream of input binary data
    * @param fileNumber A file number uniquely identified a particular file of the data set
    * @return An index of the file
    *
    */
  override def generateIndex(binaryData: SimpleStream, fileNumber: Int, isRdwBigEndian: Boolean): ArrayBuffer[SparseIndexEntry] = {
    var recordSize = cobolSchema.getRecordSize
    val inputSplitSizeRecords: Option[Int] = readerProperties.inputSplitRecords
    val inputSplitSizeMB: Option[Int] = getSplitSizeMB

    if (inputSplitSizeRecords.isDefined) {
      if (inputSplitSizeRecords.get < 1 || inputSplitSizeRecords.get > 1000000000) {
        throw new IllegalArgumentException(s"Invalid input split size. The requested number of records is ${inputSplitSizeRecords.get}.")
      }
      logger.info(s"Input split size = ${inputSplitSizeRecords.get} records")
    } else {
      if (inputSplitSizeMB.nonEmpty) {
        if (inputSplitSizeMB.get < 1 || inputSplitSizeMB.get > 2000) {
          throw new IllegalArgumentException(s"Invalid input split size of ${inputSplitSizeMB.get} MB.")
        }
        logger.info(s"Input split size = ${inputSplitSizeMB.get} MB")
      }
    }

    val copybook = cobolSchema.copybook
    val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, copybook)

    // TODO Add support for multiple Ids (https://github.com/AbsaOSS/cobrix/issues/197)
    val segmentIdValue = getRootSegmentId

    // It makes sense to parse data hierarchically only if hierarchical id generation is requested
    val isHierarchical = readerProperties.multisegment match {
      case Some(params) => params.segmentLevelIds.nonEmpty || params.fieldParentMap.nonEmpty
      case None => false
    }

    segmentIdField match {
      case Some(field) => IndexGenerator.sparseIndexGenerator(fileNumber, binaryData, isRdwBigEndian,
        recordHeaderParser, recordExtractor(binaryData, copybook), inputSplitSizeRecords, inputSplitSizeMB, Some(copybook), Some(field), isHierarchical, segmentIdValue)
      case None => IndexGenerator.sparseIndexGenerator(fileNumber, binaryData, isRdwBigEndian,
        recordHeaderParser, recordExtractor(binaryData, copybook), inputSplitSizeRecords, inputSplitSizeMB, None, None, isHierarchical)
    }
  }

  private def loadCopyBook(copyBookContents: Seq[String]): CobolSchema = {
    val encoding = if (readerProperties.isEbcdic) EBCDIC() else ASCII()
    val segmentRedefines = readerProperties.multisegment.map(r => r.segmentIdRedefineMap.values.toList.distinct).getOrElse(Nil)
    val fieldParentMap = readerProperties.multisegment.map(r => r.fieldParentMap).getOrElse(HashMap[String,String]())
    val codePage = getCodePage(readerProperties.ebcdicCodePage, readerProperties.ebcdicCodePageClass)
    val asciiCharset = if (readerProperties.asciiCharset.isEmpty) StandardCharsets.US_ASCII else Charset.forName(readerProperties.asciiCharset)

    val schema = if (copyBookContents.size == 1)
      CopybookParser.parseTree(encoding,
        copyBookContents.head,
        readerProperties.dropGroupFillers,
        segmentRedefines,
        fieldParentMap,
        readerProperties.stringTrimmingPolicy,
        readerProperties.commentPolicy,
        codePage,
        asciiCharset,
        readerProperties.floatingPointFormat,
        readerProperties.nonTerminals)
    else
      Copybook.merge(copyBookContents.map(
        CopybookParser.parseTree(encoding,
          _,
          readerProperties.dropGroupFillers,
          segmentRedefines,
          fieldParentMap,
          readerProperties.stringTrimmingPolicy,
          readerProperties.commentPolicy,
          codePage,
          asciiCharset,
          readerProperties.floatingPointFormat,
          nonTerminals = readerProperties.nonTerminals)
      ))
    val segIdFieldCount = readerProperties.multisegment.map(p => p.segmentLevelIds.size).getOrElse(0)
    val segmentIdPrefix = readerProperties.multisegment.map(p => p.segmentIdPrefix).getOrElse("")
    new CobolSchema(schema, readerProperties.schemaPolicy, readerProperties.inputFileNameColumn, readerProperties.generateRecordId, segIdFieldCount, segmentIdPrefix)
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

  private def getSplitSizeMB: Option[Int] = {
    if (readerProperties.inputSplitSizeMB.isDefined) {
      readerProperties.inputSplitSizeMB
    } else {
      readerProperties.hdfsDefaultBlockSize
    }
  }

  private def getCodePage(codePageName: String, codePageClass: Option[String]): CodePage = {
    codePageClass match {
      case Some(c) => CodePage.getCodePageByClass(c)
      case None => CodePage.getCodePageByName(codePageName)
    }
  }

  private def getRecordHeaderParser: RecordHeaderParser = {
    val adjustment1 = if (readerProperties.isRdwPartRecLength) -4 else 0
    val adjustment2 = readerProperties.rdwAdjustment
    val rhp = readerProperties.recordHeaderParser match {
      case Some(customRecordParser) => RecordHeaderParserFactory.createRecordHeaderParser(customRecordParser,
        cobolSchema.getRecordSize,
        readerProperties.fileStartOffset,
        readerProperties.fileEndOffset,
        adjustment1 + adjustment2)
      case None => getDefaultRecordHeaderParser
    }
    readerProperties.rhpAdditionalInfo.foreach(rhp.onReceiveAdditionalInfo)
    rhp
  }

  private def getDefaultRecordHeaderParser: RecordHeaderParser = {
    val adjustment1 = if (readerProperties.isRdwPartRecLength) -4 else 0
    val adjustment2 = readerProperties.rdwAdjustment
    if (readerProperties.isRecordSequence) {
      // RDW record parsers
      if (isRdwBigEndian) {
        RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwBigEndian,
          cobolSchema.getRecordSize,
          readerProperties.fileStartOffset,
          readerProperties.fileEndOffset,
          adjustment1 + adjustment2
        )
      } else {
        RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwLittleEndian,
          cobolSchema.getRecordSize,
          readerProperties.fileStartOffset,
          readerProperties.fileEndOffset,
          adjustment1 + adjustment2
        )
      }
    } else {
      // Fixed record length record parser
      RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwFixedLength,
        cobolSchema.getRecordSize,
        readerProperties.fileStartOffset,
        readerProperties.fileEndOffset,
        0
      )
    }
  }

  private def getRootSegmentId: String = {
    readerProperties.multisegment match {
      case Some(m) =>
        if (m.fieldParentMap.nonEmpty && m.segmentIdRedefineMap.nonEmpty) {
          cobolSchema.copybook.getRootSegmentIds(m.segmentIdRedefineMap, m.fieldParentMap).headOption.getOrElse("")
        } else {
          m.segmentLevelIds.headOption.getOrElse("")
        }
      case None =>
        ""
    }
  }
}
