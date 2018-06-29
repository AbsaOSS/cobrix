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

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.sources.BaseRelation
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.sources.RelationProvider
import org.apache.spark.sql.sources.SchemaRelationProvider
import org.apache.spark.sql.types.StructType
import org.slf4j.LoggerFactory

import za.co.absa.cobrix.spark.cobol.reader.HDFSReader
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.reader.ReaderFactory
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParameters

class DefaultSource
  extends RelationProvider
  with SchemaRelationProvider
  with DataSourceRegister
  with ReaderFactory {

  import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParameters._

  private val logger = LoggerFactory.getLogger(this.getClass)
  
  override def shortName(): String = SHORT_NAME

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    createRelation(sqlContext, parameters, null)
  }

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    CobolParameters.validateOrThrow(parameters, sqlContext.sparkSession.sparkContext.hadoopConfiguration)
    new CobolRelation(parameters(PARAM_SOURCE_PATH), buildReader(sqlContext.sparkSession, parameters))(sqlContext)
  }
  
  override def buildReader(spark: SparkSession, parameters: Map[String,String]): Reader = {
    new HDFSReader(spark.sparkContext.hadoopConfiguration, parameters(PARAM_COPYBOOK_PATH))
  } 
}
