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

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordExtractors
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.reader.RowHandler
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

import scala.collection.mutable

class SparkCobolOptionsBuilder(copybookContent: String)(implicit spark: SparkSession) {
  private val caseInsensitiveOptions = new mutable.HashMap[String, String]()

  def option(key: String, value: String): SparkCobolOptionsBuilder = {
    caseInsensitiveOptions += (key.toLowerCase -> value)
    this
  }

  def options(options: Map[String, String]): SparkCobolOptionsBuilder = {
    caseInsensitiveOptions ++= options.map(kv => (kv._1.toLowerCase(), kv._2))
    this
  }

  def load(rdd: RDD[Array[Byte]]): DataFrame = {
    val params = RddReaderParams.forBinary(caseInsensitiveOptions.toMap)

    getDataFrame(rdd, params)
  }

  def loadText(rddText: RDD[String]): DataFrame = {
    val params = RddReaderParams.forText(caseInsensitiveOptions.toMap)

    getDataFrame(rddText.map(_.getBytes()), params)
  }

  private [cobol] def getDataFrame(rdd: RDD[Array[Byte]], params: RddReaderParams): DataFrame = {
    val parsedCopybook = CopybookParser.parse(copybookContent, dataEncoding = params.encoding, asciiCharset = params.asciiCharset)

    val cobolSchema = new CobolSchema(parsedCopybook, params.schemaRetentionPolicy, "", false)
    val sparkSchema = cobolSchema.getSparkSchema

    val recordHandler = new RowHandler()

    val schemaRetentionPolicy = params.schemaRetentionPolicy

    val rddRow = rdd
      .filter(array => array.nonEmpty)
      .map(array => {
        val record = RecordExtractors.extractRecord[GenericRow](parsedCopybook.ast,
                                                                array,
                                                                0,
                                                                schemaRetentionPolicy, handler = recordHandler)
        Row.fromSeq(record)
      })

    spark.createDataFrame(rddRow, sparkSchema)
  }
}
