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

import org.apache.hadoop.fs.FileSystem
import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.{FixedLenNestedReader, FixedLenReader, FixedLenReaderFactory}
import za.co.absa.cobrix.spark.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.varlen.{VarLenNestedReader, VarLenReader}
import za.co.absa.cobrix.spark.cobol.source.copybook.CopybookContentLoader
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._
import za.co.absa.cobrix.spark.cobol.source.parameters._
import za.co.absa.cobrix.spark.cobol.utils.{BuildProperties, HDFSUtils, Parameters}

/**
  * This class represents a Cobol data source.
  */
class DefaultSource
  extends RelationProvider
    with SchemaRelationProvider
    with DataSourceRegister
    with FixedLenReaderFactory {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def shortName(): String = SHORT_NAME

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    CobolParametersValidator.validateOrThrow(parameters, sqlContext.sparkSession.sparkContext.hadoopConfiguration)

    logger.info(s"Cobrix 'spark-cobol' build ${BuildProperties.buildVersion} (${BuildProperties.buildTimestamp}) ")

    val cobolParameters = CobolParametersParser.parse(new Parameters(parameters))
    CobolParametersValidator.checkSanity(cobolParameters)

    new CobolRelation(parameters(PARAM_SOURCE_PATH),
      buildEitherReader(sqlContext.sparkSession, cobolParameters),
      LocalityParameters.extract(cobolParameters),
      cobolParameters.debugIgnoreFileSize)(sqlContext)
  }

  //TODO fix with the correct implementation once the correct Reader hierarchy is put in place.
  override def buildReader(spark: SparkSession, parameters: Map[String, String]): FixedLenReader = null

  /**
    * Builds one of two Readers, depending on the parameters.
    *
    * This method will probably be removed once the correct hierarchy for [[FixedLenReader]] is put in place.
    */
  private def buildEitherReader(spark: SparkSession, cobolParameters: CobolParameters): Reader = {
    if (cobolParameters.variableLengthParams.isEmpty) {
      createFixedLengthReader(cobolParameters, spark)
    }
    else {
      createVariableLengthReader(cobolParameters, spark)
    }
  }

  /**
    * Creates a Reader that knows how to consume fixed-length Cobol records.
    */
  private def createFixedLengthReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {

    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    new FixedLenNestedReader(copybookContent,
      parameters.isEbcdic,
      getCodePage(parameters.ebcdicCodePage, parameters.ebcdicCodePageClass),
      parameters.floatingPointFormat,
      parameters.recordStartOffset,
      parameters.recordEndOffset,
      parameters.schemaRetentionPolicy,
      parameters.stringTrimmingPolicy,
      parameters.dropGroupFillers,
      parameters.nonTerminals,
      getReaderProperties(parameters, spark)
    )
  }

  /**
    * Creates a Reader that is capable of reading variable-length Cobol records.
    *
    * The variable-length reading process is approached as if reading from a stream.
    */
  private def createVariableLengthReader(parameters: CobolParameters, spark: SparkSession): VarLenReader = {


    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    new VarLenNestedReader(
      copybookContent, getReaderProperties(parameters, spark)

    )
  }

  private def getReaderProperties(parameters: CobolParameters, spark: SparkSession): ReaderParameters = {
    val varLenParams: VariableLengthParameters = parameters.variableLengthParams
      .getOrElse(
        VariableLengthParameters(isRecordSequence = false,
          isRdwBigEndian = false,
          isRdwPartRecLength = false,
          rdwAdjustment = 0,
          recordHeaderParser = None,
          rhpAdditionalInfo = None,
          recordLengthField = "",
          fileStartOffset = 0,
          fileEndOffset = 0,
          variableSizeOccurs = false,
          generateRecordId = false,
          isUsingIndex = false,
          inputSplitRecords = None,
          inputSplitSizeMB = None,
          improveLocality = false,
          optimizeAllocation = false,
          inputFileNameColumn = "")
      )

    val recordLengthField = if (varLenParams.recordLengthField.nonEmpty)
      Some(varLenParams.recordLengthField)
    else
      None

    ReaderParameters(isEbcdic = parameters.isEbcdic,
      ebcdicCodePage = parameters.ebcdicCodePage,
      ebcdicCodePageClass = parameters.ebcdicCodePageClass,
      asciiCharset = parameters.asciiCharset,
      floatingPointFormat = parameters.floatingPointFormat,
      variableSizeOccurs = varLenParams.variableSizeOccurs,
      lengthFieldName = recordLengthField,
      isRecordSequence = varLenParams.isRecordSequence,
      isRdwBigEndian = varLenParams.isRdwBigEndian,
      isRdwPartRecLength = varLenParams.isRdwPartRecLength,
      rdwAdjustment = varLenParams.rdwAdjustment,
      isIndexGenerationNeeded = varLenParams.isUsingIndex,
      inputSplitRecords = varLenParams.inputSplitRecords,
      inputSplitSizeMB = varLenParams.inputSplitSizeMB,
      hdfsDefaultBlockSize = getDefaultHdfsBlockSize(spark),
      startOffset = parameters.recordStartOffset,
      endOffset = parameters.recordEndOffset,
      fileStartOffset = varLenParams.fileStartOffset,
      fileEndOffset = varLenParams.fileEndOffset,
      generateRecordId = varLenParams.generateRecordId,
      schemaPolicy = parameters.schemaRetentionPolicy,
      stringTrimmingPolicy = parameters.stringTrimmingPolicy,
      parameters.multisegmentParams,
      parameters.commentPolicy,
      parameters.dropGroupFillers,
      parameters.nonTerminals,
      varLenParams.recordHeaderParser,
      varLenParams.rhpAdditionalInfo,
      varLenParams.inputFileNameColumn
    )
  }

  private def getDefaultHdfsBlockSize(spark: SparkSession): Option[Int] = {
    val conf = spark.sparkContext.hadoopConfiguration
    val fileSystem = FileSystem.get(conf)
    val hdfsBlockSize = HDFSUtils.getHDFSDefaultBlockSizeMB(fileSystem)
    hdfsBlockSize match {
      case None => logger.info(s"Unable to get HDFS default block size.")
      case Some(size) => logger.info(s"HDFS default block size = $size MB.")
    }

    hdfsBlockSize
  }

  private def getCodePage(codePageName: String, codePageClass: Option[String]): CodePage = {
    codePageClass match {
      case Some(c) => CodePage.getCodePageByClass(c)
      case None => CodePage.getCodePageByName(codePageName)
    }
  }

}