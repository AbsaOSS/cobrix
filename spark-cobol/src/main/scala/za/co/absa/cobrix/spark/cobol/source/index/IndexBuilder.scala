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

package za.co.absa.cobrix.spark.cobol.source.index

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.hadoop.hdfs.DistributedFileSystem
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream
import za.co.absa.cobrix.cobol.reader.{VarLenNestedReader => ReaderVarLenNestedReader}
import za.co.absa.cobrix.spark.cobol.reader.{Reader, VarLenReader}
import za.co.absa.cobrix.spark.cobol.source.SerializableConfiguration
import za.co.absa.cobrix.spark.cobol.source.parameters.LocalityParameters
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder
import za.co.absa.cobrix.spark.cobol.utils.{HDFSUtils, SparkUtils}

import scala.collection.mutable.ArrayBuffer

/**
  * Builds offsets indexes for distributed processing of variable-length records.
  *
  * The indexes creation tries to optimize locality by finding the HDFS blocks containing the records and instructing
  * Spark to create the RDD partitions according to those locations.
  *
  * In a nutshell, ideally, there will be as many partitions as are there are indexes.
  */
private[source] object IndexBuilder extends Logging {
  def buildIndex(filesList: Array[FileWithOrder],
                 cobolReader: Reader,
                 sqlContext: SQLContext)
                (localityParams: LocalityParameters): RDD[SparseIndexEntry] = {
    val fs = new Path(filesList.head.filePath).getFileSystem(sqlContext.sparkSession.sparkContext.hadoopConfiguration)

    cobolReader match {
      case reader: VarLenReader if reader.isIndexGenerationNeeded && localityParams.improveLocality && isDataLocalitySupported(fs) =>
        buildIndexForVarLenReaderWithFullLocality(filesList, reader, sqlContext, localityParams.optimizeAllocation)
      case reader: VarLenReader                                                                                                    =>
        buildIndexForVarLenReader(filesList, reader, sqlContext)
      case _                                                                                                                       =>
        buildIndexForFullFiles(filesList, sqlContext)
    }
  }

  /**
    * Builds the indexes by querying HDFS about the records locations and then asking Spark to assign local executors
    * to those records in those locations.
    */
  private[cobol] def buildIndexForVarLenReaderWithFullLocality(filesList: Array[FileWithOrder],
                                                               reader: VarLenReader,
                                                               sqlContext: SQLContext,
                                                               optimizeAllocation: Boolean): RDD[SparseIndexEntry] = {
    val conf = sqlContext.sparkContext.hadoopConfiguration
    val filesRDD = toRDDWithLocality(filesList, conf, sqlContext)
    val sconf = new SerializableConfiguration(conf)

    val indexes = filesRDD.mapPartitions { partition =>
      partition.flatMap { row =>
        val index = generateIndexEntry(row, sconf.value, reader)

        val filePath = row.filePath
        val fileSystem = new Path(filePath).getFileSystem(sconf.value)

        index.map { entry =>
          val offset = if (entry.offsetFrom >= 0) entry.offsetFrom else 0
          val length = getBlockLengthByIndexEntry(entry)
          (entry, HDFSUtils.getBlocksLocations(new Path(filePath), offset, length, fileSystem))
        }
      }
    }

    logger.info("Going to collect located indexes into driver.")
    val offsetsLocations: Seq[(SparseIndexEntry, Seq[String])] = if (optimizeAllocation) {
      optimizeDistribution(indexes.collect(), sqlContext.sparkContext)
    }
    else {
      indexes.collect()
    }

    logger.info(s"Creating RDD for ${offsetsLocations.length} located indexes.")

    logDebug("Preferred locations per index entry")
    offsetsLocations.foreach(allocation => logDebug(allocation.toString()))

    val indexRDD = sqlContext.sparkContext.makeRDD(offsetsLocations)

    repartitionIndexes(indexRDD)
  }

  /**
    * Builds records indexes. Does not take locality into account. Used for file systems that
    * do not support data locality (local, S3, etc).
    */
  private[cobol] def buildIndexForVarLenReader(filesList: Array[FileWithOrder],
                                               reader: VarLenReader,
                                               sqlContext: SQLContext): RDD[SparseIndexEntry] = {
    val conf = sqlContext.sparkContext.hadoopConfiguration
    val sconf = new SerializableConfiguration(conf)

    if (reader.getReaderProperties.enableSelfChecks && filesList.nonEmpty) {
      selfCheckForIndexCompatibility(reader, filesList.head.filePath, conf)
    }

    val filesRDD = sqlContext.sparkContext.parallelize(filesList, filesList.length)

    val indexRDD = filesRDD.mapPartitions(
      partition => {
        partition.flatMap(row => {
          generateIndexEntry(row, sconf.value, reader)
        })
      }).cache()

    repartitionIndexes(indexRDD)
  }

  /**
    * Builds records indexes for filesystems that do not support fetching from the middle.
    */
  private[cobol] def buildIndexForFullFiles(filesList: Array[FileWithOrder],
                                            sqlContext: SQLContext): RDD[SparseIndexEntry] = {
    val filesRDD = sqlContext.sparkContext.parallelize(filesList, filesList.length)

    val indexRDD = filesRDD.mapPartitions(
      partition => {
        partition.flatMap(row => {
          val fileId = row.order

          val element = SparseIndexEntry(0, -1, fileId, 0L)
          ArrayBuffer[SparseIndexEntry](element)
        })
      }).cache()

    repartitionIndexes(indexRDD)
  }

  private[cobol] def generateIndexEntry(fileWithOrder: FileWithOrder,
                                        config: Configuration,
                                        reader: VarLenReader): ArrayBuffer[SparseIndexEntry] = {
    val filePath = fileWithOrder.filePath
    val fileOrder = fileWithOrder.order
    val startOffset = reader.getReaderProperties.fileStartOffset
    val endOffset = reader.getReaderProperties.fileEndOffset

    logger.info(s"Going to generate index for the file: $filePath")

    val (inputStream, headerStream, maximumBytes) = getStreams(filePath, startOffset, endOffset, config)
    val index = reader.generateIndex(inputStream, headerStream, fileOrder, reader.isRdwBigEndian)

    val indexWithEndOffset = if (maximumBytes > 0 ){
      index.map(entry => if (entry.offsetTo == -1) entry.copy(offsetTo = startOffset + maximumBytes) else entry)
    } else {
      index
    }

    indexWithEndOffset
  }

  private[cobol] def getStreams(filePath: String,
                                fileStartOffset: Long,
                                fileEndOffset: Long,
                                config: Configuration): (SimpleStream, SimpleStream, Long) = {
    val path = new Path(filePath)
    val fileSystem = path.getFileSystem(config)

    val startOffset = fileStartOffset
    val maximumBytes = if (fileEndOffset == 0) {
      0
    } else {
      val bytesToRead = fileSystem.getContentSummary(path).getLength - fileEndOffset - startOffset
      if (bytesToRead < 0)
        0
      else
        bytesToRead
    }

    val inputStream = new FileStreamer(filePath, fileSystem, startOffset, maximumBytes)
    val headerStream = new FileStreamer(filePath, fileSystem)

    (inputStream, headerStream, maximumBytes)
  }

  private[cobol] def selfCheckForIndexCompatibility(reader: VarLenReader, filePath: String, config: Configuration): Unit = {
    if (!reader.isInstanceOf[ReaderVarLenNestedReader[_]])
      return

    val readerProperties = reader.getReaderProperties

    val startOffset = readerProperties.fileStartOffset
    val endOffset = readerProperties.fileEndOffset

    readerProperties.recordExtractor.foreach { recordExtractorClass =>
      val (dataStream, headerStream, _) = getStreams(filePath, startOffset, endOffset, config)

      val extractorOpt = reader.asInstanceOf[ReaderVarLenNestedReader[_]].recordExtractor(0, dataStream, headerStream)

      var offset = -1L
      var record = Array[Byte]()

      extractorOpt.foreach { extractor =>
        if (extractor.hasNext) {
          // Getting the first record, if available
          extractor.next()
          offset = extractor.offset // Saving offset to jump to later

          if (extractor.hasNext) {
            // Getting the second record, if available
            record = extractor.next() // Saving the record to check later

            dataStream.close()
            headerStream.close()

            // Getting new streams and record extractor that points directly to the second record
            val (dataStream2, headerStream2, _) = getStreams(filePath, offset, endOffset, config)
            val extractorOpt2 = reader.asInstanceOf[ReaderVarLenNestedReader[_]].recordExtractor(1, dataStream2, headerStream2)

            extractorOpt2.foreach { extractor2 =>
              if (!extractor2.hasNext) {
                // If the extractor refuses to return the second record, it is obviously faulty in terms of indexing support.
                throw new RuntimeException(
                  s"Record extractor self-check failed. When reading from a non-zero offset the extractor returned hasNext()=false. " +
                    "Please, use 'enable_indexes = false'. " +
                    s"File: $filePath, offset: $offset"
                )
              }

              // Getting the second record from the extractor pointing to the second record offset at the start.
              val expectedRecord = extractor2.next()

              if (!expectedRecord.sameElements(record)) {
                // Records should match. If they don't, the record extractor is faulty in terms of indexing support..
                throw new RuntimeException(
                  s"Record extractor self-check failed. The record extractor returned wrong record when started from non-zero offset. " +
                    "Please, use 'enable_indexes = false'. " +
                    s"File: $filePath, offset: $offset"
                )
              } else {
                logger.info(s"Record extractor self-check passed. File: $filePath, offset: $offset")
              }
              dataStream2.close()
              headerStream2.close()
            }
          }
        }
      }
    }
  }

  private[cobol] def getBlockLengthByIndexEntry(entry: SparseIndexEntry): Long = {
    val indexedLength = if (entry.offsetTo - entry.offsetFrom > 0)
      entry.offsetTo - entry.offsetFrom
    else
      Constants.megabyte

    // Each entry of a sparse index can be slightly bigger than the default HDFS block size.
    // The exact size depends on record size and root level boundaries between records.
    // But overwhelming majority of these additional bytes will be less than 1 MB due to
    // limitations on mainframe record sizes.
    // We subtract 1 MB from indexed length to get locality nodes for the significant part
    // of the block.
    // In other words we don't care if the last megabyte is not node local as long as
    // most of the split chunk is node local.
    val significantLength = if (indexedLength < 10L * Constants.megabyte) {
      indexedLength
    } else {
      indexedLength - Constants.megabyte
    }
    significantLength
  }

  /**
    * Tries to balance the allocation among unused executors.
    */
  private[cobol] def optimizeDistribution(allocation: Seq[(SparseIndexEntry, Seq[String])],
                                          sc: SparkContext): Seq[(SparseIndexEntry, Seq[String])] = {
    val availableExecutors = SparkUtils.currentActiveExecutors(sc)
    logger.info(s"Trying to balance ${allocation.size} partitions among all available executors ($availableExecutors)")
    LocationBalancer.balance(allocation, availableExecutors)
  }

  /**
    * Converts the list of files into an RDD with preferred locations for the partitions.
    */
  private[cobol] def toRDDWithLocality(filesList: Array[FileWithOrder],
                                       conf: Configuration,
                                       sqlContext: SQLContext): RDD[FileWithOrder] = {
    val fileSystem = FileSystem.get(conf)

    val filesWithPreferredLocations = filesList.map(file => {
      (file, HDFSUtils.getBlocksLocations(new Path(file.filePath), fileSystem))
    }).toSeq

    filesWithPreferredLocations.foreach(a => logDebug(a.toString()))

    sqlContext.sparkContext.makeRDD(filesWithPreferredLocations)
  }

  private[cobol] def isDataLocalitySupported(fs: FileSystem): Boolean = {
    fs.isInstanceOf[DistributedFileSystem]
  }

  private def repartitionIndexes(indexRDD: RDD[SparseIndexEntry]): RDD[SparseIndexEntry] = {
    val indexCount = indexRDD.count()
    val numPartitions = Math.min(indexCount, Constants.maxNumPartitions).toInt
    logger.info(s"Index elements count: $indexCount, number of partitions = $numPartitions")
    indexRDD.repartition(numPartitions).cache()
  }
}
