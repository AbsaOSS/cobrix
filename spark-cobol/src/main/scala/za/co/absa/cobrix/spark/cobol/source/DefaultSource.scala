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
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode, SparkSession}
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser._
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParameters, CobolParametersParser, Parameters}
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

    new CobolRelation(cobolParameters.sourcePaths,
      buildEitherReader(sqlContext.sparkSession, cobolParameters),
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
    * Builds one of two Readers, depending on the parameters.
    *
    * This method will probably be removed once the correct hierarchy for [[FixedLenReader]] is put in place.
    */
  private def buildEitherReader(spark: SparkSession, cobolParameters: CobolParameters): Reader = {
    val reader = if (cobolParameters.isText && cobolParameters.variableLengthParams.isEmpty) {
      createTextReader(cobolParameters, spark)
    } else if (cobolParameters.variableLengthParams.isEmpty) {
      createFixedLengthReader(cobolParameters, spark)
    }
    else {
      createVariableLengthReader(cobolParameters, spark)
    }

    if (cobolParameters.debugLayoutPositions)
      logger.info(s"Layout positions:\n${reader.getCobolSchema.copybook.generateRecordLayoutPositions()}")
    reader
  }

  /**
    * Creates a Reader that knows how to consume text Cobol records.
    */
  private def createTextReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {
    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultHdfsBlockSize(spark, parameters.sourcePaths.headOption)
    new FixedLenTextReader(copybookContent,  getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }

  /**
    * Creates a Reader that knows how to consume fixed-length Cobol records.
    */
  private def createFixedLengthReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {

    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultHdfsBlockSize(spark, parameters.sourcePaths.headOption)
    new FixedLenNestedReader(copybookContent, getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }

  /**
    * Creates a Reader that is capable of reading variable-length Cobol records.
    *
    * The variable-length reading process is approached as if reading from a stream.
    */
  private def createVariableLengthReader(parameters: CobolParameters, spark: SparkSession): VarLenReader = {


    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultHdfsBlockSize(spark, parameters.sourcePaths.headOption)
    new VarLenNestedReader(
      copybookContent, getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }
}
