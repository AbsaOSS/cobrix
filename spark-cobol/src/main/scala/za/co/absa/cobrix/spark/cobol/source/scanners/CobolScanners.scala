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

package za.co.absa.cobrix.spark.cobol.source.scanners

import java.nio.charset.StandardCharsets
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.{FixedLenReader, VarLenReader}
import za.co.absa.cobrix.spark.cobol.source.SerializableConfiguration
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

private[source] object CobolScanners extends Logging {

  private[source] def buildScanForVarLenIndex(reader: VarLenReader, indexes: RDD[SparseIndexEntry], filesList: Array[FileWithOrder], sqlContext: SQLContext): RDD[Row] = {
    val filesMap = filesList.map(fileWithOrder => (fileWithOrder.order, fileWithOrder.filePath)).toMap
    val conf = sqlContext.sparkContext.hadoopConfiguration
    val sconf = new SerializableConfiguration(conf)

    indexes.flatMap(indexEntry => {
      val filePathName = filesMap(indexEntry.fileId)
      val path = new Path(filePathName)
      val fileSystem = path.getFileSystem(sconf.value)
      val fileName = path.getName
      val numOfBytes = if (indexEntry.offsetTo > 0L) indexEntry.offsetTo - indexEntry.offsetFrom else 0L
      val numOfBytesMsg = if (numOfBytes > 0) s"${numOfBytes / Constants.megabyte} MB" else "until the end"

      logger.info(s"Going to process offsets ${indexEntry.offsetFrom}...${indexEntry.offsetTo} ($numOfBytesMsg) of $fileName")
      val dataStream = new FileStreamer(filePathName, fileSystem, indexEntry.offsetFrom, numOfBytes)
      reader.getRowIterator(dataStream, indexEntry.offsetFrom, indexEntry.fileId, indexEntry.recordIndex)
    })
  }

  private[source] def buildScanForVariableLength(reader: VarLenReader, filesList: Array[FileWithOrder], sqlContext: SQLContext): RDD[Row] = {
    val filesRDD = sqlContext.sparkContext.parallelize(filesList, filesList.length)

    val conf = sqlContext.sparkContext.hadoopConfiguration
    val sconf = new SerializableConfiguration(conf)
    filesRDD.mapPartitions(
      partition => {
        partition.flatMap(row => {
          val filePath = row.filePath
          val fileOrder = row.order
          val path = new Path(filePath)
          val fileSystem = path.getFileSystem(sconf.value)

          logger.info(s"Going to parse file: $filePath")
          reader.getRowIterator(new FileStreamer(filePath, fileSystem), 0L, fileOrder, 0L)
        }
        )
      })
  }

  private[source] def buildScanForFixedLength(reader: FixedLenReader, sourceDirs: Seq[String],
                                              recordParser: (FixedLenReader, RDD[Array[Byte]]) => RDD[Row],
                                              debugIgnoreFileSize: Boolean,
                                              sqlContext: SQLContext): RDD[Row] = {
    // This reads whole text files as RDD[String]
    // binaryRecords() for fixed size records
    // binaryFiles() for varying size records
    // https://spark.apache.org/docs/2.1.1/api/java/org/apache/spark/SparkContext.html#binaryFiles(java.lang.String,%20int)

    val recordSize = reader.getRecordSize

    sourceDirs.foreach(sourceDir => {
      if (!debugIgnoreFileSize && areThereNonDivisibleFiles(sourceDir, sqlContext.sparkContext.hadoopConfiguration, recordSize)) {
        throw new IllegalArgumentException(s"There are some files in $sourceDir that are NOT DIVISIBLE by the RECORD SIZE calculated from the copybook ($recordSize bytes per record). Check the logs for the names of the files.")
      }
    })

    val records = sourceDirs.map(sourceDir => sqlContext.sparkContext.binaryRecords(sourceDir, recordSize, sqlContext.sparkContext.hadoopConfiguration))
      .reduce((a ,b) => a.union(b))
    recordParser(reader, records)
  }

  private[source] def buildScanForTextFiles(reader: FixedLenReader, sourceDirs: Seq[String],
                                            recordParser: (FixedLenReader, RDD[Array[Byte]]) => RDD[Row],
                                            sqlContext: SQLContext): RDD[Row] = {
    sqlContext.read.text()

    val rddText = sourceDirs.map(sourceDir => sqlContext.sparkContext.textFile(sourceDir))
      .reduce((a,b) => a.union(b))

    val records = rddText
      .filter(str => str.nonEmpty)
      .map(str => {
        str.getBytes(StandardCharsets.UTF_8)
      })
    recordParser(reader, records)
  }

  private def areThereNonDivisibleFiles(sourceDir: String, hadoopConfiguration: Configuration, divisor: Int): Boolean = {

    val fileSystem = new Path(sourceDir).getFileSystem(hadoopConfiguration)

    if (FileUtils.getNumberOfFilesInDir(sourceDir, fileSystem) < FileUtils.THRESHOLD_DIR_LENGTH_FOR_SINGLE_FILE_CHECK) {
      FileUtils.findAndLogAllNonDivisibleFiles(sourceDir, divisor, fileSystem) > 0
    }
    else {
      FileUtils.findAndLogFirstNonDivisibleFile(sourceDir, divisor, fileSystem)
    }
  }
}