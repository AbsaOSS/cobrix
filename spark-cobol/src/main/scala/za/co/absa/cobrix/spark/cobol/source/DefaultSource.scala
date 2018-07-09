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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import scala.collection.JavaConverters.asScalaBufferConverter
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.sources.BaseRelation
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.sources.RelationProvider
import org.apache.spark.sql.sources.SchemaRelationProvider
import org.apache.spark.sql.types.StructType
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.reader.NestedReader
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.reader.ReaderFactory
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParameters
import za.co.absa.cobrix.spark.cobol.utils.FileNameUtils

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

  override def buildReader(spark: SparkSession, parameters: Map[String, String]): Reader = {
    val copyBookContents = parameters.get(PARAM_COPYBOOK_CONTENTS)
    val copyBookPathFileName = parameters.get(PARAM_COPYBOOK_PATH)

    copyBookContents match {
      case Some(contents) =>
        new NestedReader(contents)
      case None =>
        val (isLocalFS, copyBookFileName) = FileNameUtils.getCopyBookFileName(parameters(PARAM_COPYBOOK_PATH))
        val copyBookContents = if (isLocalFS) {
          loadCopybookFromLocalFS(copyBookFileName)
        } else {
          loadCopybookFromHDFS(spark.sparkContext.hadoopConfiguration, copyBookFileName)
        }
        new NestedReader(copyBookContents)
    }
  }

  private def loadCopybookFromLocalFS(copyBookLocalPath: String): String = {
    Files.readAllLines(Paths.get(copyBookLocalPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
  }

  private def loadCopybookFromHDFS(hadoopConfiguration: Configuration, copyBookHDFSPath: String): String = {
    val hdfs = FileSystem.get(hadoopConfiguration)
    val stream = hdfs.open(new Path(copyBookHDFSPath))
    try IOUtils.readLines(stream).asScala.mkString("\n") finally stream.close()
  }

}
