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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.{FixedLenReader, FixedLenTextReader, VarLenReader}
import za.co.absa.cobrix.spark.cobol.source.SerializableConfiguration
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

import java.nio.charset.{Charset, StandardCharsets}

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
      val headerStream = new FileStreamer(filePathName, fileSystem)
      reader.getRowIterator(dataStream, headerStream, indexEntry.offsetFrom, indexEntry.fileId, indexEntry.recordIndex)
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
          val startFileOffset = reader.getReaderProperties.fileStartOffset
          val maximumFileBytes = if (reader.getReaderProperties.fileEndOffset == 0) {
            0
          } else {
            fileSystem.getFileStatus(path).getLen - reader.getReaderProperties.fileEndOffset - startFileOffset
          }
          val dataStream = new FileStreamer(filePath, fileSystem, startFileOffset, maximumFileBytes)
          val headerStream = new FileStreamer(filePath, fileSystem, startFileOffset)
          reader.getRowIterator(dataStream, headerStream, startFileOffset, fileOrder, 0L)
        })
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
    // Take a deep copy of Configuration for conf isolation
    val conf = new Configuration(sqlContext.sparkContext.hadoopConfiguration)
    val recordSize = reader.getRecordSize

    sourceDirs.foreach(sourceDir => {
      if (!debugIgnoreFileSize) {
        val nonDivisibleFiles = getNonDivisibleFiles(sourceDir, conf, recordSize)

        if (nonDivisibleFiles.nonEmpty) {
          nonDivisibleFiles.head match {
            case (name, size) =>
              if (nonDivisibleFiles.length > 1) {
                throw new IllegalArgumentException(s"Multiple file sizes are NOT DIVISIBLE by the RECORD SIZE calculated from the copybook ($recordSize bytes per record). Example file: $name size ($size bytes).")
              } else {
                throw new IllegalArgumentException(s"File $name size ($size bytes) is NOT DIVISIBLE by the RECORD SIZE calculated from the copybook ($recordSize bytes per record).")
              }
          }
        }
      }
    })

    val records = sourceDirs.map(sourceDir => sqlContext.sparkContext.binaryRecords(sourceDir, recordSize, conf))
      .reduce((a ,b) => a.union(b))
    recordParser(reader, records)
  }

  private[source] def buildScanForTextFiles(reader: FixedLenTextReader,
                                            sourceDirs: Seq[String],
                                            recordParser: (FixedLenTextReader, RDD[Array[Byte]]) => RDD[Row],
                                            sqlContext: SQLContext): RDD[Row] = {
    val utf8 = StandardCharsets.UTF_8
    reader.getReaderProperties.asciiCharset
      .map(Charset.forName) match {
      case None                             => buildScanForUtf8TextFiles(reader, sourceDirs, recordParser, sqlContext)
      case Some(charset) if charset == utf8 => buildScanForUtf8TextFiles(reader, sourceDirs, recordParser, sqlContext)
      case Some(_)                          => buildScanForAnyTextFiles(reader, sourceDirs, recordParser, sqlContext)
    }
  }

  private[source] def buildScanForUtf8TextFiles(reader: FixedLenTextReader,
                                                sourceDirs: Seq[String],
                                                recordParser: (FixedLenTextReader, RDD[Array[Byte]]) => RDD[Row],
                                                sqlContext: SQLContext): RDD[Row] = {
    val minimumRecordLength = reader.getReaderProperties.minimumRecordLength
    val maximumRecordLength = reader.getReaderProperties.maximumRecordLength

    val rddText = sourceDirs.map(sourceDir => sqlContext.sparkContext.textFile(sourceDir))
      .reduce((a, b) => a.union(b))

    val records = rddText
      .filter(str => str.nonEmpty && str.length >= minimumRecordLength && str.length <= maximumRecordLength)
      .map(str => {
        str.getBytes(StandardCharsets.UTF_8)
      })
    recordParser(reader, records)
  }

  private[source] def buildScanForAnyTextFiles(reader: FixedLenTextReader,
                                               sourceDirs: Seq[String],
                                               recordParser: (FixedLenTextReader, RDD[Array[Byte]]) => RDD[Row],
                                               sqlContext: SQLContext): RDD[Row] = {
    val minimumRecordLength = reader.getReaderProperties.minimumRecordLength
    val maximumRecordLength = reader.getReaderProperties.maximumRecordLength

    // The ides for the implementation is taken from the following Spark PR:
    // https://github.com/apache/spark/pull/21287/files
    val rddText = sourceDirs.map(sourceDir => sqlContext
      .sparkContext.hadoopFile(sourceDir, classOf[TextInputFormat], classOf[LongWritable], classOf[Text]))
      .reduce((a, b) => a.union(b))
      .map(pair => pair._2)
      .map { text =>
        // Text.getBytes returns the internal memory buffer which size usually larger than the
        // actual  bytes count of the string content for performance reasons. We should be
        // careful to limit the index range correctly within [0, Text.getLength)
        val ar = new Array[Byte](text.getLength)
        System.arraycopy(text.getBytes, 0, ar, 0, text.getLength)
        ar
      }

    val records = rddText
      .filter(str => str.nonEmpty && str.length >= minimumRecordLength && str.length <= maximumRecordLength)

    recordParser(reader, records)
  }

  private def getNonDivisibleFiles(sourceDir: String, hadoopConfiguration: Configuration, divisor: Int): Seq[(String, Long)] = {
    val fileSystem = new Path(sourceDir).getFileSystem(hadoopConfiguration)

    if (FileUtils.getNumberOfFilesInDir(sourceDir, fileSystem) < FileUtils.THRESHOLD_DIR_LENGTH_FOR_SINGLE_FILE_CHECK) {
      FileUtils.findAndLogAllNonDivisibleFiles(sourceDir, divisor, fileSystem)
    }
    else {
      FileUtils.findAndLogFirstNonDivisibleFile(sourceDir, divisor, fileSystem).toSeq
    }
  }
}
