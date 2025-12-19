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

import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.headerparsers.{RecordHeaderParser, RecordHeaderParserFactory}
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat.{FixedBlock, FixedLength, VariableBlock, VariableLength}
import za.co.absa.cobrix.cobol.reader.extractors.raw._
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordHandler
import za.co.absa.cobrix.cobol.reader.index.IndexGenerator
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.cobol.reader.iterator.{VarLenHierarchicalIterator, VarLenNestedIterator}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream
import za.co.absa.cobrix.cobol.reader.validator.ReaderParametersValidator

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
  * The Cobol data reader for variable length records that gets input binary data as a stream and produces nested structure schema
  *
  * @param copybookContents The contents of a copybook.
  * @param readerProperties Additional properties for customizing the reader.
  */
class VarLenNestedReader[T: ClassTag](copybookContents: Seq[String],
                                      readerProperties: ReaderParameters,
                                      handler: RecordHandler[T]) extends VarLenReader with Logging with Serializable {

  protected val cobolSchema: CobolSchema = loadCopyBook(copybookContents)

  val recordHeaderParser: RecordHeaderParser = {
    getRecordHeaderParser
  }

  checkInputArgumentsValidity()

  def recordExtractor(startingRecordNumber: Long,
                      dataStream: SimpleStream,
                      headerStream: SimpleStream): Option[RawRecordExtractor] = {
    val bdwOpt = readerProperties.bdw
    val reParams = RawRecordContext.builder(startingRecordNumber, dataStream, headerStream, cobolSchema.copybook)
      .withReaderParams(readerProperties)
      .build()

    readerProperties.recordExtractor match {
      case Some(recordExtractorClass) =>
        Some(RawRecordExtractorFactory.createRecordHeaderParser(recordExtractorClass, reParams))
      case None if readerProperties.isText && readerProperties.allowPartialRecords =>
        Some(new TextRecordExtractor(reParams))
      case None if readerProperties.isText =>
        Some(new TextFullRecordExtractor(reParams))
      case None if readerProperties.recordFormat == FixedLength && (readerProperties.lengthFieldExpression.nonEmpty || readerProperties.lengthFieldMap.nonEmpty) =>
        Some(new FixedWithRecordLengthExprRawRecordExtractor(reParams, readerProperties))
      case None if readerProperties.recordFormat == VariableLength && (readerProperties.lengthFieldExpression.nonEmpty || readerProperties.lengthFieldMap.nonEmpty) =>
        Some(new FixedWithRecordLengthExprRawRecordExtractor(reParams, readerProperties))
      case None if readerProperties.recordFormat == FixedBlock =>
        val fbParams = FixedBlockParameters(readerProperties.recordLength, bdwOpt.get.blockLength, bdwOpt.get.recordsPerBlock)
        FixedBlockParameters.validate(fbParams)
        Some(new FixedBlockRawRecordExtractor(reParams, fbParams))
      case None if readerProperties.recordFormat == VariableBlock =>
        Some(new VariableBlockVariableRecordExtractor(reParams))
      case None if readerProperties.variableSizeOccurs &&
        readerProperties.recordHeaderParser.isEmpty &&
        !readerProperties.isRecordSequence &&
        readerProperties.lengthFieldExpression.isEmpty            =>
        Some(new VarOccursRecordExtractor(reParams))
      case None if readerProperties.recordFormat == FixedLength   =>
        Some(new FixedRecordLengthRawRecordExtractor(reParams, readerProperties.recordLength))
      case None                                                   =>
        None
    }
  }

  override def getCobolSchema: CobolSchema = cobolSchema

  override def getRecordSize: Int = cobolSchema.copybook.getRecordSize

  override def isIndexGenerationNeeded: Boolean = readerProperties.isIndexGenerationNeeded

  override def isRdwBigEndian: Boolean = readerProperties.isRdwBigEndian

  override def getRecordIterator(dataStream: SimpleStream,
                                 headerStream: SimpleStream,
                                 startingFileOffset: Long,
                                 fileNumber: Int,
                                 startingRecordIndex: Long): Iterator[Seq[Any]] = {
    val recordExtractorOpt = recordExtractor(startingRecordIndex, dataStream, headerStream)
    if (recordExtractorOpt.isEmpty) {
      headerStream.close()
    }
    if (cobolSchema.copybook.isHierarchical) {
      new VarLenHierarchicalIterator(cobolSchema.copybook,
        dataStream,
        readerProperties,
        recordHeaderParser,
        recordExtractorOpt,
        fileNumber,
        startingRecordIndex,
        startingFileOffset,
        handler)
    } else {
      new VarLenNestedIterator(cobolSchema.copybook,
        dataStream,
        readerProperties,
        recordHeaderParser,
        recordExtractorOpt,
        fileNumber,
        startingRecordIndex,
        startingFileOffset,
        cobolSchema.segmentIdPrefix,
        handler)
    }
  }

  /**
    * Traverses the data sequentially as fast as possible to generate record index.
    * This index will be used to distribute workload of the conversion.
    *
    * @param dataStream   A stream of input binary data
    * @param headerStream A stream pointing to the beginning of the file, even if inputStream is pointing
    *                     to a record in the middle.
    * @param fileNumber   A file number uniquely identified a particular file of the data set
    * @return An index of the file
    */
  override def generateIndex(dataStream: SimpleStream,
                             headerStream: SimpleStream,
                             fileNumber: Int,
                             isRdwBigEndian: Boolean): ArrayBuffer[SparseIndexEntry] = {
    val inputSplitSizeRecords: Option[Int] = readerProperties.inputSplitRecords
    val inputSplitSizeMB: Option[Int] = getSplitSizeMB(dataStream.isCompressed)

    if (inputSplitSizeRecords.isDefined) {
      if (inputSplitSizeRecords.get < 1 || inputSplitSizeRecords.get > 1000000000) {
        throw new IllegalArgumentException(s"Invalid input split size. The requested number of records is ${inputSplitSizeRecords.get}.")
      }
      logger.info(s"Input split size = ${inputSplitSizeRecords.get} records")
    } else {
      if (inputSplitSizeMB.nonEmpty) {
        val maxSplitSizeMB = if (dataStream.isCompressed) 200000 else 2000
        if (inputSplitSizeMB.get < 1 || inputSplitSizeMB.get > maxSplitSizeMB) {
          throw new IllegalArgumentException(s"Invalid input split size of ${inputSplitSizeMB.get} MB (max allowed: $maxSplitSizeMB MB).")
        }
        logger.info(s"Input split size = ${inputSplitSizeMB.get} MB")
      }
    }

    val copybook = cobolSchema.copybook
    val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, copybook)

    val segmentIdValue = getRootSegmentId

    // It makes sense to parse data hierarchically only if hierarchical id generation is requested
    val isHierarchical = readerProperties.multisegment match {
      case Some(params) => params.segmentLevelIds.nonEmpty || params.fieldParentMap.nonEmpty
      case None => false
    }

    val recordExtractorOpt = recordExtractor(0L, dataStream, headerStream)
    if (recordExtractorOpt.isEmpty) {
      headerStream.close()
    }

    segmentIdField match {
      case Some(field) => IndexGenerator.sparseIndexGenerator(fileNumber,
        dataStream,
        readerProperties.fileStartOffset,
        recordHeaderParser,
        recordExtractorOpt,
        inputSplitSizeRecords,
        inputSplitSizeMB,
        Some(copybook),
        Some(field),
        isHierarchical,
        segmentIdValue)
      case None => IndexGenerator.sparseIndexGenerator(fileNumber,
        dataStream,
        readerProperties.fileStartOffset,
        recordHeaderParser,
        recordExtractorOpt,
        inputSplitSizeRecords,
        inputSplitSizeMB,
        None,
        None,
        isHierarchical)
    }
  }

  private def loadCopyBook(copyBookContents: Seq[String]): CobolSchema = {
    CobolSchema.fromReaderParameters(copyBookContents, readerProperties)
  }

  private def checkInputArgumentsValidity(): Unit = {
    if (readerProperties.startOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record start offset = ${readerProperties.startOffset}. A record start offset cannot be negative.")
    }
    if (readerProperties.endOffset < 0) {
      throw new IllegalArgumentException(s"Invalid record end offset = ${readerProperties.endOffset}. A record end offset cannot be negative.")
    }
  }

  private def getSplitSizeMB(isCompressed: Boolean): Option[Int] = {
    if (isCompressed) {
      readerProperties.inputSplitSizeCompressedMB match {
        case Some(size) => readerProperties.inputSplitSizeCompressedMB
        case None => Some(1024)
      }
    } else {
      if (readerProperties.inputSplitSizeMB.isDefined) {
        readerProperties.inputSplitSizeMB
      } else {
        readerProperties.hdfsDefaultBlockSize
      }
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
      val recordSize = readerProperties.recordLength.getOrElse(cobolSchema.getRecordSize)
      RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwFixedLength,
        recordSize,
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
