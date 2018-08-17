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
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenReader
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.reader.varlen.VarLenReader
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

/**
  * This class implements an actual Spark relation.
  *
  * It currently supports both, fixed and variable-length records.
  *
  * Its constructor is expected to change after the hierarchy of [[Reader]] is put in place.
  */
class CobolRelation(sourceDir: String, cobolReader: Reader)(@transient val sqlContext: SQLContext)
  extends BaseRelation
  with Serializable
  with TableScan {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def schema: StructType = {
    cobolReader.getSparkSchema
  }

  override def buildScan(): RDD[Row] = {

    cobolReader match {
      case blockReader: FixedLenReader => buildScanForFixedLength(blockReader)
      case streamReader: VarLenReader => buildScanForVariableLength(streamReader)
      case _ => throw new IllegalStateException("Invalid reader object $cobolReader.")
    }
  }

  private def buildScanForVariableLength(reader: VarLenReader): RDD[Row] = {
    val filesDF = getParallelizedFiles(sourceDir)
    implicit val rowEncoder = RowEncoder.apply(reader.getSparkSchema)

    filesDF.mapPartitions(
      partition =>
      {
        val conf = SparkSession.builder.getOrCreate().sparkContext.hadoopConfiguration
        val fileSystem = FileSystem.get(conf)

        partition.flatMap(row =>
        {
          val file = row.getString(0)
          logger.info(s"Going to parse file: $file")
          reader.getRowIterator(new FileStreamer(file, fileSystem), fileNumber = 0 /*ToDo add a file sequence here is possible*/)
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

  private def buildScanForFixedLength(reader: FixedLenReader): RDD[Row] = {
    // This reads whole text files as RDD[String]
    // Todo For Cobol files need to use
    // binaryRecords() for fixed size records
    // binaryFiles() for varying size records
    // https://spark.apache.org/docs/2.1.1/api/java/org/apache/spark/SparkContext.html#binaryFiles(java.lang.String,%20int)

    val recordSize = reader.getCobolSchema.getRecordSize + reader.getRecordStartOffset + reader.getRecordEndOffset
    val schema = reader.getSparkSchema

    val records = sqlContext.sparkContext.binaryRecords(sourceDir, recordSize, sqlContext.sparkContext.hadoopConfiguration)
    parseRecords(reader, records)
  }

  private[source] def parseRecords(reader: FixedLenReader, records: RDD[Array[Byte]]) = {
    records.flatMap(record => {
      val it = reader.getRowIterator(record)
      for (parsedRecord <- it) yield {
        parsedRecord
      }
    })
  }
}