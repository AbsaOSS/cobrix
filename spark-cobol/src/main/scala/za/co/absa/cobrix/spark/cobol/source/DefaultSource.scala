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
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.{FixedLenNestedReader, FixedLenReader, FixedLenReaderFactory}
import za.co.absa.cobrix.spark.cobol.reader.varlen.{VarLenNestedReader, VarLenReader}
import za.co.absa.cobrix.spark.cobol.source.copybook.CopybookContentLoader
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._
import za.co.absa.cobrix.spark.cobol.source.parameters.{CobolParameters, CobolParametersParser, CobolParametersValidator}

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
    new CobolRelation(parameters(PARAM_SOURCE_PATH), buildEitherReader(sqlContext.sparkSession, parameters))(sqlContext)
  }

  //TODO fix with the correct implementation once the correct Reader hierarchy is put in place.
  override def buildReader(spark: SparkSession, parameters: Map[String, String]): FixedLenReader = null

  /**
    * Builds one of two Readers, depending on the parameters.
    *
    * This method will probably be removed once the correct hierarchy for [[FixedLenReader]] is put in place.
    */
  private def buildEitherReader(spark: SparkSession, parameters: Map[String, String]): Reader = {

    val cobolParameters = CobolParametersParser.parse(parameters)
    CobolParametersValidator.checkSanity(cobolParameters)

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
    new FixedLenNestedReader(copybookContent, parameters.recordStartOffset, parameters.recordEndOffset
    )
  }

  /**
    * Creates a Reader that is capable of reading variable-length Cobol records.
    *
    * The variable-length reading process is approached as if reading from a stream.
    */
  private def createVariableLengthReader(parameters: CobolParameters, spark: SparkSession): VarLenReader = {

    if (!parameters.variableLengthParams.isDefined) {
      throw new IllegalArgumentException("Trying to create StreamReader by parameters for variable-length records are missing.")
    }

    val variableLengthParameters = parameters.variableLengthParams.get
    val copybookContent = CopybookContentLoader.load(parameters, spark.sparkContext.hadoopConfiguration)

    new VarLenNestedReader(
      copybookContent,
      variableLengthParameters.recordLengthField,
      parameters.recordStartOffset,
      parameters.recordEndOffset
    )
  }
}