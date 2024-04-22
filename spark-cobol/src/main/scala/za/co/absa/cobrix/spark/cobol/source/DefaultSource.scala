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

import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{SQLContext, SparkSession}
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.reader.parameters.CobolParameters
import za.co.absa.cobrix.spark.cobol.parameters.CobolParametersParser._
import za.co.absa.cobrix.spark.cobol.parameters.{CobolParametersParser, Parameters}
import za.co.absa.cobrix.spark.cobol.reader._
import za.co.absa.cobrix.spark.cobol.source.copybook.CopybookContentLoader
import za.co.absa.cobrix.spark.cobol.source.parameters._
import za.co.absa.cobrix.spark.cobol.utils.{BuildProperties, SparkUtils}

/**
  * This class represents a Cobol data source.
  */
class DefaultSource
  extends RelationProvider
    with SchemaRelationProvider
    with DataSourceRegister
    with ReaderFactory
    with Logging {

  override def shortName(): String = SHORT_NAME

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

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

  //TODO fix with the correct implementation once the correct Reader hierarchy is put in place.
  override def buildReader(spark: SparkSession, parameters: Map[String, String]): FixedLenReader = null

  /**
    * Builds one of two Readers, depending on the parameters.
    *
    * This method will probably be removed once the correct hierarchy for [[FixedLenReader]] is put in place.
    */
  private def buildEitherReader(spark: SparkSession, cobolParameters: CobolParameters): Reader = {
    if (cobolParameters.isText && cobolParameters.variableLengthParams.isEmpty) {
      createTextReader(cobolParameters, spark)
    } else if (cobolParameters.variableLengthParams.isEmpty) {
      createFixedLengthReader(cobolParameters, spark)
    }
    else {
      createVariableLengthReader(cobolParameters, spark)
    }
  }

  /**
    * Creates a Reader that knows how to consume text Cobol records.
    */
  private def createTextReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {
    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultHdfsBlockSize(spark)
    new FixedLenTextReader(copybookContent,  getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }

  /**
    * Creates a Reader that knows how to consume fixed-length Cobol records.
    */
  private def createFixedLengthReader(parameters: CobolParameters, spark: SparkSession): FixedLenReader = {

    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    val defaultHdfsBlockSize = SparkUtils.getDefaultHdfsBlockSize(spark)
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
    val defaultHdfsBlockSize = SparkUtils.getDefaultHdfsBlockSize(spark)
    new VarLenNestedReader(
      copybookContent, getReaderProperties(parameters, defaultHdfsBlockSize)
    )
  }
}
