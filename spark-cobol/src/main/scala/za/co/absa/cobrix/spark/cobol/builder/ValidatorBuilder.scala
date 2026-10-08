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

package za.co.absa.cobrix.spark.cobol.builder

import org.apache.spark.sql.SparkSession
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParameters, CobolParametersParser, Parameters}
import za.co.absa.cobrix.spark.cobol.source.DefaultSource.buildEitherReader
import za.co.absa.cobrix.spark.cobol.source.parameters.LocalityParameters
import za.co.absa.cobrix.spark.cobol.source.{CobolRelation, DefaultSource}

import scala.collection.mutable

class ValidatorBuilder(implicit spark: SparkSession) {
  private val caseInsensitiveOptions = new mutable.HashMap[String, String]()

  def option(key: String, value: String): ValidatorBuilder = {
    caseInsensitiveOptions += (key.toLowerCase -> value)
    this
  }

  def options(options: Map[String, String]): ValidatorBuilder = {
    caseInsensitiveOptions ++= options.map(kv => (kv._1.toLowerCase(), kv._2))
    this
  }


  /**
    * Validates the list of data files against spark-cobol options.
    * File paths can contain wildcards, e.g. "/somedur/test*"
    */
  def validate(filePaths: String*): Unit = {
    val sqlContext = spark.sqlContext

    val cobolParameters: CobolParameters = CobolParametersParser.parse(new Parameters(caseInsensitiveOptions.toMap))
      .copy(sourcePaths = filePaths)
    val isRecursiveRetrieval = DefaultSource.isRecursiveRetrieval(sqlContext)
    val filesList = CobolRelation.getListFilesWithOrder(filePaths, sqlContext, isRecursiveRetrieval)
    val hasGpg = cobolParameters.gpgPrivateKey.isDefined
    val hasCompressedFiles = hasGpg || filesList.exists(_.isCompressed)

    val relation = new CobolRelation(cobolParameters.sourcePaths,
      filesList,
      buildEitherReader(sqlContext.sparkSession, cobolParameters, hasCompressedFiles),
      LocalityParameters.extract(cobolParameters),
      cobolParameters.debugIgnoreFileSize,
      cobolParameters.recordLimit)(sqlContext)

    relation.buildScan()
  }
}
