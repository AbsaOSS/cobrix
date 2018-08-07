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

package za.co.absa.cobrix.spark.cobol.source

import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.reader.{NestedReader, Reader, ReaderFactory}
import za.co.absa.cobrix.spark.cobol.source.copybook.CopybookContentLoader
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._
import za.co.absa.cobrix.spark.cobol.source.parameters.{CobolParameters, CobolParametersParser, CobolParametersValidator}
import za.co.absa.cobrix.spark.cobol.streamreader.{NestedStreamReader, StreamReader}

class DefaultSource
  extends RelationProvider
    with SchemaRelationProvider
    with DataSourceRegister
    with ReaderFactory {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def shortName(): String = SHORT_NAME

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    CobolParametersValidator.validateOrThrow(parameters, sqlContext.sparkSession.sparkContext.hadoopConfiguration)
    new CobolRelation(parameters(PARAM_SOURCE_PATH), buildEitherReader(sqlContext.sparkSession, parameters))(sqlContext)
  }

  override def buildReader(spark: SparkSession, parameters: Map[String, String]): Reader = null

  private def buildEitherReader(spark: SparkSession, parameters: Map[String, String]): Either[Reader,StreamReader] = {

    val cobolParameters = CobolParametersParser.parse(parameters)
    CobolParametersValidator.checkSanity(cobolParameters)

    if (cobolParameters.variableLengthParams.isEmpty) {
      Left(createFixedLengthReader(cobolParameters, spark))
    }
    else {
      Right(createVariableLengthReader(cobolParameters, spark))
    }
  }

  private def createFixedLengthReader(parameters: CobolParameters, spark: SparkSession): Reader = {

    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)
    new NestedReader(copybookContent)
  }

  private def createVariableLengthReader(parameters: CobolParameters, spark: SparkSession): StreamReader = {

    if (!parameters.variableLengthParams.isDefined) {
      throw new IllegalArgumentException("Trying to create StreamReader by parameters for variable-length records are missing.")
    }

    val variableLengthParameters = parameters.variableLengthParams.get
    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)

    new NestedStreamReader(
      copybookContent,
      variableLengthParameters.recordLengthField,
      variableLengthParameters.recordStartOffset,
      variableLengthParameters.recordEndOffset
    )
  }
}