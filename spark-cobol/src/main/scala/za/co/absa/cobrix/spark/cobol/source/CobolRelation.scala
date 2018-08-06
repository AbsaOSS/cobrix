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

import org.apache.hadoop.fs.FileSystem
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.source.variable.VariableLengthSimpleStreamer
import za.co.absa.cobrix.spark.cobol.streamreader.StreamReader
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

class CobolRelation(sourceDir: String, cobolReader: Either[Reader,StreamReader])(@transient val sqlContext: SQLContext)
  extends BaseRelation
  with Serializable
  with TableScan {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def schema: StructType = {
    //TODO refactor when Reader hierarchy is changed
    if (cobolReader.isLeft) {
      cobolReader.left.get.getSparkSchema
    }
    else {
      cobolReader.right.get.getSparkSchema
    }
  }

  override def buildScan(): RDD[Row] = {
    if (cobolReader.isRight) {
      buildScanForVariableLength(cobolReader.right.get)
    }
    else {
      buildScanForFixedLength(cobolReader.left.get)
    }
  }

  private def buildScanForVariableLength(reader: StreamReader): RDD[Row] = {
    val filesDF = getParallelizedFiles(sourceDir)
    implicit val rowEncoder = RowEncoder.apply(reader.getSparkSchema)

    filesDF.mapPartitions(
      partition =>
      {
        val conf = SparkSession.builder.getOrCreate().sparkContext.hadoopConfiguration

        //val fileSystem = FileSystem.get(sqlContext.sparkContext.hadoopConfiguration)
        val fileSystem = FileSystem.get(conf)
        partition.flatMap(row =>
        {
          val file = row.getString(0)
          logger.info(s"Going to parse file: $file")
          reader.getRowIterator(new VariableLengthSimpleStreamer(file, fileSystem))
        }
        )
      })
      .rdd
  }

  private def getParallelizedFiles(sourceDir: String): DataFrame = {
    val files = FileUtils.getAllFilesInDirectory(sourceDir, sqlContext.sparkContext.hadoopConfiguration)

    import sqlContext.implicits._
    sqlContext.sparkContext.parallelize(files, files.size).toDF()
  }

  private def buildScanForFixedLength(reader: Reader): RDD[Row] = {
    // This reads whole text files as RDD[String]
    // Todo For Cobol files need to use
    // binaryRecords() for fixed size records
    // binaryFiles() for varying size records
    // https://spark.apache.org/docs/2.1.1/api/java/org/apache/spark/SparkContext.html#binaryFiles(java.lang.String,%20int)

    val recordSize = reader.getCobolSchema.getRecordSize
    val schema = reader.getSparkSchema

    val records = sqlContext.sparkContext.binaryRecords(sourceDir, recordSize, sqlContext.sparkContext.hadoopConfiguration)
    parseRecords(records)
  }

  private[source] def parseRecords(records: RDD[Array[Byte]]) = {
    records.flatMap(record => {
      val it = cobolReader.left.get.getRowIterator(record)
      for (parsedRecord <- it) yield {
        parsedRecord
      }
    })
  }
}