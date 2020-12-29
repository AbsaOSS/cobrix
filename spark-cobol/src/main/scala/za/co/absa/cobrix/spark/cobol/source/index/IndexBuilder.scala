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
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
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
private[source] object IndexBuilder {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def buildIndex(filesList: Array[FileWithOrder], cobolReader: Reader, sqlContext: SQLContext)(localityParams: LocalityParameters): RDD[SparseIndexEntry] = {
    val fs = new Path(filesList.head.filePath).getFileSystem(sqlContext.sparkSession.sparkContext.hadoopConfiguration)
    val isIndexSupported = isFileRandomAccessSupported(fs)

    cobolReader match {
      case reader: VarLenReader if isIndexSupported => {
        if (reader.isIndexGenerationNeeded && localityParams.improveLocality && isDataLocalitySupported(fs)) {
          buildIndexForVarLenReaderWithFullLocality(filesList, reader, sqlContext)(localityParams.optimizeAllocation)
        }
        else {
          buildIndexForVarLenReader(filesList, reader, sqlContext)
        }
      }
      case reader: VarLenReader =>
        buildIndexForFullFiles(filesList, reader, sqlContext)
      case _ => null
    }
  }

  /**
    * Builds the indexes by querying HDFS about the records locations and then asking Spark to assign local executors
    * to those records in those locations.
    */
  private def buildIndexForVarLenReaderWithFullLocality(filesList: Array[FileWithOrder], reader: VarLenReader, sqlContext: SQLContext)
                                                       (optimizeAllocation: Boolean): RDD[SparseIndexEntry] = {

    val conf = sqlContext.sparkContext.hadoopConfiguration

    val filesRDD = toRDDWithLocality(filesList, conf, sqlContext)

    val sconf = new SerializableConfiguration(conf)

    val indexes = filesRDD.mapPartitions(
      partition => {
        partition.flatMap(row => {
          val index = generateIndexEntry(row, sconf.value, reader)

          val filePath = row.filePath
          val fileSystem = new Path(filePath).getFileSystem(sconf.value)

          index.map(entry => {
            val offset = if (entry.offsetFrom >= 0) entry.offsetFrom else 0
            val length = getBlockLengthByIndexEntry(entry)
            (entry, HDFSUtils.getBlocksLocations(new Path(filePath), offset, length, fileSystem))
          })
        }
        )
      })

    logger.info("Going to collect located indexes into driver.")
    val offsetsLocations: Seq[(SparseIndexEntry, Seq[String])] = if (optimizeAllocation) {
      optimizeDistribution(indexes.collect(), sqlContext.sparkContext)
    }
    else {
      indexes.collect()
    }

    logger.info(s"Creating RDD for ${offsetsLocations.length} located indexes.")

    if (logger.isDebugEnabled) {
      logger.debug("Preferred locations per index entry")
      offsetsLocations.foreach(allocation => logger.debug(allocation.toString()))
    }

    val indexRDD = sqlContext.sparkContext.makeRDD(offsetsLocations)

    repartitionIndexes(indexRDD)
  }

  /**
    * Builds records indexes. Does not take locality into account. Might be removed in further releases.
    */
  def buildIndexForVarLenReader(filesList: Array[FileWithOrder], reader: VarLenReader, sqlContext: SQLContext): RDD[SparseIndexEntry] = {
    val filesRDD = sqlContext.sparkContext.parallelize(filesList, filesList.length)
    val conf = sqlContext.sparkContext.hadoopConfiguration
    val sconf = new SerializableConfiguration(conf)

    val indexRDD = filesRDD.mapPartitions(
      partition => {
        partition.flatMap(row => {
          generateIndexEntry(row, sconf.value, reader)
        })
      }).cache

    repartitionIndexes(indexRDD)
  }

  /**
    * Builds records indexes for filesystems that do not support fetching from the middle.
    */
  def buildIndexForFullFiles(filesList: Array[FileWithOrder], reader: VarLenReader, sqlContext: SQLContext): RDD[SparseIndexEntry] = {
    val filesRDD = sqlContext.sparkContext.parallelize(filesList, filesList.length)

    val indexRDD = filesRDD.mapPartitions(
      partition => {
        partition.flatMap(row => {
          val fileId = row.order

          val element = SparseIndexEntry(0, -1, fileId, 0L)
          ArrayBuffer[SparseIndexEntry](element)
        })
      }).cache

    repartitionIndexes(indexRDD)
  }

  private def generateIndexEntry(fileWithOrder: FileWithOrder, config: Configuration, reader: VarLenReader): ArrayBuffer[SparseIndexEntry] = {
    val filePath = fileWithOrder.filePath
    val path = new Path(filePath)
    val fileOrder = fileWithOrder.order
    val fileSystem = path.getFileSystem(config)

    logger.info(s"Going to generate index for the file: $filePath")
    val index = reader.generateIndex(new FileStreamer(filePath, fileSystem, 0, 0),
      fileOrder, reader.isRdwBigEndian)
    index
  }


  private def getBlockLengthByIndexEntry(entry: SparseIndexEntry): Long = {
    val indexedLength = if (entry.offsetTo > 0) entry.offsetTo else Long.MaxValue

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
  private def optimizeDistribution(allocation: Seq[(SparseIndexEntry, Seq[String])], sc: SparkContext): Seq[(SparseIndexEntry, Seq[String])] = {
    val availableExecutors = SparkUtils.currentActiveExecutors(sc)
    logger.info(s"Trying to balance ${allocation.size} partitions among all available executors ($availableExecutors)")
    LocationBalancer.balance(allocation, availableExecutors)
  }

  /**
    * Converts the list of files into an RDD with preferred locations for the partitions.
    */
  private def toRDDWithLocality(filesList: Array[FileWithOrder], conf: Configuration, sqlContext: SQLContext): RDD[FileWithOrder] = {
    val fileSystem = FileSystem.get(conf)

    val filesWithPreferredLocations = filesList.map(file => {
      (file, HDFSUtils.getBlocksLocations(new Path(file.filePath), fileSystem))
    }).toSeq

    if (logger.isDebugEnabled()) {
      filesWithPreferredLocations.foreach(a => logger.debug(a.toString()))
    }

    sqlContext.sparkContext.makeRDD(filesWithPreferredLocations)
  }

  private def repartitionIndexes(indexRDD: RDD[SparseIndexEntry]): RDD[SparseIndexEntry] = {
    val indexCount = indexRDD.count()
    val numPartitions = Math.min(indexCount, Constants.maxNumPartitions).toInt
    logger.warn(s"Index elements count: $indexCount, number of partitions = $numPartitions")
    indexRDD.repartition(numPartitions).cache()
  }

  def isFileRandomAccessSupported(fs: FileSystem): Boolean = {
    import za.co.absa.cobrix.spark.cobol.parameters.CobolParametersParser._

    val isSupportedFx =
    fs.isInstanceOf[DistributedFileSystem] ||
      fs.isInstanceOf[RawLocalFileSystem] ||
      fs.isInstanceOf[FilterFileSystem] ||
      fs.isInstanceOf[LocalFileSystem] ||
      fs.isInstanceOf[ChecksumFileSystem]
    if (!isSupportedFx) {
      val q = "\""
      logger.warn(s"Filesystem '${fs.getScheme}://' might not support random file access. Please, disable indexes if the job fails. " +
      s" You can do this using '.option($q$PARAM_ENABLE_INDEXES$q, false)' ")
    }
    true
  }

  def isDataLocalitySupported(fs: FileSystem): Boolean = {
    fs.isInstanceOf[DistributedFileSystem]
  }

}