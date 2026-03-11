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

package za.co.absa.cobrix.spark.cobol.source

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{BytesWritable, NullWritable}
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser._
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParameters, CobolParametersParser, Parameters, VariableLengthParameters}
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema
import za.co.absa.cobrix.spark.cobol.reader._
import za.co.absa.cobrix.spark.cobol.source.copybook.CopybookContentLoader
import za.co.absa.cobrix.spark.cobol.source.parameters._
import za.co.absa.cobrix.spark.cobol.utils.{BuildProperties, SparkUtils}
import za.co.absa.cobrix.spark.cobol.writer.{RawBinaryOutputFormat, RecordCombinerSelector}

/**
  * This class represents a Cobol data source.
  */
class DefaultSource
  extends RelationProvider
    with SchemaRelationProvider
    with CreatableRelationProvider
    with DataSourceRegister
    with ReaderFactory
    with Logging {
  import DefaultSource._

  override def shortName(): String = SHORT_NAME

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  /** Reader relation */
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    CobolParametersValidator.validateOrThrow(parameters, sqlContext.sparkSession.sparkContext.hadoopConfiguration)

    logger.info(s"Cobrix 'spark-cobol' build ${BuildProperties.buildVersion} (${BuildProperties.buildTimestamp}) ")

    val cobolParameters = CobolParametersParser.parse(new Parameters(parameters))
    CobolParametersValidator.checkSanity(cobolParameters)

    val filesList = CobolRelation.getListFilesWithOrder(cobolParameters.sourcePaths, sqlContext, isRecursiveRetrieval(sqlContext))

    val hasCompressedFiles = filesList.exists(_.isCompressed)

    if (hasCompressedFiles) {
      logger.info(s"Compressed files found. Binary parallelism and indexes will be adjusted accordingly.")
    }

    new CobolRelation(cobolParameters.sourcePaths,
      filesList,
      buildEitherReader(sqlContext.sparkSession, cobolParameters, hasCompressedFiles),
      LocalityParameters.extract(cobolParameters),
      cobolParameters.debugIgnoreFileSize)(sqlContext)
  }

  /** Writer relation */
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    val outSqlContext = sqlContext
    val path = parameters.getOrElse("path",
      throw new IllegalArgumentException("Path is required for this data source."))

    val cobolParameters = CobolParametersParser.parse(new Parameters(parameters))
    CobolParametersValidator.checkSanity(cobolParameters)

    val readerParameters = CobolParametersParser.getReaderProperties(cobolParameters, None)
    CobolParametersValidator.validateParametersForWriting(readerParameters)

    val outputPath = new Path(path)
    val hadoopConf = sqlContext.sparkContext.hadoopConfiguration
    val fs = outputPath.getFileSystem(hadoopConf)

    mode match {
      case SaveMode.Overwrite =>
        if (fs.exists(outputPath)) {
          fs.delete(outputPath, true)
        }
      case SaveMode.Append =>
      // In append mode, no action is needed. Tasks will write to different files.
      case SaveMode.ErrorIfExists =>
        if (fs.exists(outputPath)) {
          throw new IllegalArgumentException(
            s"Path '$path' already exists; SaveMode.ErrorIfExists prevents overwriting."
          )
        }
      case SaveMode.Ignore =>
        if (fs.exists(outputPath)) {
          // Skip the write entirely
          return new BaseRelation {
            override val sqlContext: SQLContext = outSqlContext
            override def schema: StructType = data.schema
          }
        }
      case _ =>
    }

    val copybookContent = CopybookContentLoader.load(cobolParameters, sqlContext.sparkContext.hadoopConfiguration)
    val cobolSchema = CobolSchema.fromReaderParameters(copybookContent, readerParameters)
    val combiner = RecordCombinerSelector.selectCombiner(cobolSchema, readerParameters)
    val rdd = combiner.combine(data, cobolSchema, readerParameters)

    rdd.map(bytes => (NullWritable.get(), new BytesWritable(bytes)))
      .saveAsNewAPIHadoopFile(
        path,
        classOf[NullWritable],
        classOf[BytesWritable],
        classOf[RawBinaryOutputFormat]
      )

    new BaseRelation {
      override def sqlContext: SQLContext = outSqlContext
      override def schema: StructType = data.schema
    }
  }


  //TODO fix with the correct implementation once the correct Reader hierarchy is put in place.
  override def buildReader(spark: SparkSession, parameters: Map[String, String]): FixedLenReader = null

  /**
    * Checks if the recursive file retrieval flag is set
    */
  private def isRecursiveRetrieval(sqlContext: SQLContext): Boolean = {
    val hadoopConf = sqlContext.sparkContext.hadoopConfiguration
    hadoopConf.getBoolean(FileInputFormat.INPUT_DIR_RECURSIVE, false)
  }
}

object DefaultSource {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
    * Builds one of two Readers, depending on the parameters.
    *
    * This method will probably be removed once the correct hierarchy for [[FixedLenReader]] is put in place.
    */
  def buildEitherReader(spark: SparkSession, cobolParameters: CobolParameters, hasCompressedFiles: Boolean): Reader = {
    val resolvedParameters = resolveHeaderTrailerOffsets(cobolParameters, spark)

    val reader = if (resolvedParameters.isText && resolvedParameters.variableLengthParams.isEmpty) {
      createTextReader(resolvedParameters, spark)
    } else if (resolvedParameters.variableLengthParams.isEmpty && !hasCompressedFiles) {
      createFixedLengthReader(resolvedParameters, spark)
    }
    else {
      createVariableLengthReader(resolvedParameters, spark)
    }

    if (resolvedParameters.debugLayoutPositions)
      logger.info(s"Layout positions:\n${reader.getCobolSchema.copybook.generateRecordLayoutPositions()}")
    reader
  }

  /**
    * Resolves record_header_name / record_trailer_name options into file_start_offset / file_end_offset
    * by parsing the copybook and looking up the named root-level records.
    */
  private def resolveHeaderTrailerOffsets(parameters: CobolParameters, spark: SparkSession): CobolParameters = {
    if (parameters.recordHeaderName.isEmpty && parameters.recordTrailerName.isEmpty) {
      return parameters
    }

    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val copybook = if (copybookContent.size == 1)
      CopybookParser.parseTree(copybookContent.head)
    else
      za.co.absa.cobrix.cobol.parser.Copybook.merge(copybookContent.map(CopybookParser.parseTree(_)))

    val rootRecords = copybook.ast.children

    var fileStartOffset = 0
    var fileEndOffset = 0

    parameters.recordHeaderName.foreach { headerName =>
      val transformedHeaderName = CopybookParser.transformIdentifier(headerName)
      val headerRecord = rootRecords.find(_.name.equalsIgnoreCase(transformedHeaderName))
        .getOrElse(throw new IllegalArgumentException(
          s"Record '$headerName' specified in '${PARAM_RECORD_HEADER_NAME}' is not found among the root-level (01) records of the copybook. " +
            s"Available root records: ${rootRecords.map(_.name).mkString(", ")}"
        ))

      headerRecord match {
        case _: Group => // OK
        case _ => throw new IllegalArgumentException(
          s"Record '$headerName' specified in '${PARAM_RECORD_HEADER_NAME}' is a primitive field, not a group record. " +
            "The copybook might be flat (no 01-level groups)."
        )
      }

      fileStartOffset = headerRecord.binaryProperties.offset + headerRecord.binaryProperties.actualSize
    }

    parameters.recordTrailerName.foreach { trailerName =>
      val transformedTrailerName = CopybookParser.transformIdentifier(trailerName)
      val trailerRecord = rootRecords.find(_.name.equalsIgnoreCase(transformedTrailerName))
        .getOrElse(throw new IllegalArgumentException(
          s"Record '$trailerName' specified in '${PARAM_RECORD_TRAILER_NAME}' is not found among the root-level (01) records of the copybook. " +
            s"Available root records: ${rootRecords.map(_.name).mkString(", ")}"
        ))

      trailerRecord match {
        case _: Group => // OK
        case _ => throw new IllegalArgumentException(
          s"Record '$trailerName' specified in '${PARAM_RECORD_TRAILER_NAME}' is a primitive field, not a group record. " +
            "The copybook might be flat (no 01-level groups)."
        )
      }

      fileEndOffset = trailerRecord.binaryProperties.actualSize
    }

    // Compute the data-only record length by subtracting excluded records from total
    val excludedNames = (parameters.recordHeaderName.map(n => CopybookParser.transformIdentifier(n).toUpperCase).toSet ++
      parameters.recordTrailerName.map(n => CopybookParser.transformIdentifier(n).toUpperCase).toSet)
    val totalSize = copybook.getRecordSize
    val excludedSize = rootRecords
      .filter(r => excludedNames.contains(r.name.toUpperCase))
      .map(_.binaryProperties.actualSize)
      .sum
    val dataRecordLength = totalSize - excludedSize
    val updatedRecordLength = if (parameters.recordLength.isEmpty && dataRecordLength > 0 && dataRecordLength < totalSize)
      Some(dataRecordLength)
    else
      parameters.recordLength

    val existingVarLen = parameters.variableLengthParams
    val updatedVarLen = existingVarLen match {
      case Some(vlp) =>
        Some(vlp.copy(
          fileStartOffset = if (parameters.recordHeaderName.isDefined) fileStartOffset else vlp.fileStartOffset,
          fileEndOffset = if (parameters.recordTrailerName.isDefined) fileEndOffset else vlp.fileEndOffset
        ))
      case None if fileStartOffset > 0 || fileEndOffset > 0 =>
        Some(VariableLengthParameters(
          isRecordSequence = false,
          bdw = None,
          isRdwBigEndian = false,
          isRdwPartRecLength = false,
          rdwAdjustment = 0,
          recordHeaderParser = None,
          recordExtractor = None,
          rhpAdditionalInfo = None,
          reAdditionalInfo = "",
          recordLengthField = "",
          recordLengthMap = Map.empty,
          fileStartOffset = fileStartOffset,
          fileEndOffset = fileEndOffset,
          generateRecordId = false,
          isUsingIndex = false,
          isIndexCachingAllowed = false,
          inputSplitRecords = None,
          inputSplitSizeMB = None,
          inputSplitSizeCompressedMB = None,
          improveLocality = false,
          optimizeAllocation = false,
          inputFileNameColumn = "",
          occursMappings = parameters.occursMappings
        ))
      case other => other
    }

    parameters.copy(variableLengthParams = updatedVarLen, recordLength = updatedRecordLength)
  }

  /**
    * Creates a Reader that knows how to consume text Cobol records.
    */
  def createTextReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {
    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultFsBlockSize(spark, parameters.sourcePaths.headOption)
    new FixedLenTextReader(copybookContent,  getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }

  /**
    * Creates a Reader that knows how to consume fixed-length Cobol records.
    */
  def createFixedLengthReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {

    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultFsBlockSize(spark, parameters.sourcePaths.headOption)
    new FixedLenNestedReader(copybookContent, getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }

  /**
    * Creates a Reader that is capable of reading variable-length Cobol records.
    *
    * The variable-length reading process is approached as if reading from a stream.
    */
  def createVariableLengthReader(parameters: CobolParameters, spark: SparkSession): VarLenReader = {


    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultFsBlockSize(spark, parameters.sourcePaths.headOption)
    new VarLenNestedReader(
      copybookContent, getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }
}
