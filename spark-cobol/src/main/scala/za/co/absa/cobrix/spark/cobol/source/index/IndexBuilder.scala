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
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.reader.{Constants, Reader}
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.varlen.VarLenReader
import za.co.absa.cobrix.spark.cobol.source.SerializableConfiguration
import za.co.absa.cobrix.spark.cobol.source.parameters.LocalityParameters
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder
import za.co.absa.cobrix.spark.cobol.utils.{HDFSUtils, SparkUtils}

/**
  * Builds offsets indexes for distributed processing of variable-length records.
  *
  * The indexes creation tries to optimize locality by finding the HDFS blocks containing the records and instructing
  * Spark to create the RDD partitions according to those locations.
  *
  * In a nutshell, ideally, there will be as many partitions as are there are indexes.
  */
private [source] object IndexBuilder {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def buildIndex(filesList: Array[FileWithOrder], cobolReader: Reader, sqlContext: SQLContext)(localityParams: LocalityParameters): RDD[SparseIndexEntry] = {
    cobolReader match {
      case reader: VarLenReader => {
        if (reader.isIndexGenerationNeeded && localityParams.improveLocality){
          buildIndexForVarLenReaderWithFullLocality(filesList, reader, sqlContext)(localityParams.optimizeAllocation)
        }
        else {
          buildIndexForVarLenReader(filesList, reader, sqlContext)
        }
      }
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
        val fileSystem = FileSystem.get(sconf.value)
        partition.flatMap(row => {
          val filePath = row.filePath
          val fileOrder = row.order

          logger.info(s"Going to generate index for the file: $filePath")
          val index = reader.generateIndex(new FileStreamer(filePath, fileSystem, 0, 0),
            fileOrder, reader.isRdwBigEndian)

          index.map(entry => {
            val offset = if (entry.offsetFrom >= 0) entry.offsetFrom else 0
            val length = getBlockLengthByIndexEntry(entry)
            (entry, HDFSUtils.getBlocksLocations(new Path(filePath), offset, length, fileSystem))
          })
        }
        )
      })

    logger.info("Going to collect located indexes into driver.")
    val offsetsLocations: Seq[(SparseIndexEntry,Seq[String])] = if (optimizeAllocation) {
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

    sqlContext.sparkContext.makeRDD(offsetsLocations)
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
    val significantLength = if (indexedLength < 10L*Constants.megabyte) {
      indexedLength
    } else {
      indexedLength - Constants.megabyte
    }
    significantLength
  }

  /**
    * Tries to balance the allocation among unused executors.
    */
  private def optimizeDistribution(allocation: Seq[(SparseIndexEntry,Seq[String])], sc: SparkContext): Seq[(SparseIndexEntry,Seq[String])] = {
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

    filesWithPreferredLocations.foreach(a => logger.debug(a.toString()))

    sqlContext.sparkContext.makeRDD(filesWithPreferredLocations)
  }

  /**
    * Builds records indexes. Does not take locality into account. Might be removed in further releases.
    */
  def buildIndexForVarLenReader(filesList: Array[FileWithOrder], reader: VarLenReader, sqlContext: SQLContext): RDD[SparseIndexEntry] = {
    val filesRDD = sqlContext.sparkContext.parallelize(filesList, filesList.length)
    val conf = sqlContext.sparkContext.hadoopConfiguration
    val sconf = new SerializableConfiguration(conf)

    val indexes = filesRDD.mapPartitions(
      partition => {
        val fileSystem = FileSystem.get(sconf.value)
        partition.flatMap(row => {
          val filePath = row.filePath
          val fileOrder = row.order

          logger.info(s"Going to generate index for the file: $filePath")
          val index = reader.generateIndex(new FileStreamer(filePath, fileSystem, 0, 0),
            fileOrder, reader.isRdwBigEndian)
          index
        }
        )
      }).cache
    val indexCount = indexes.count()
    val numPartitions = Math.min(indexCount, Constants.maxNumPartitions).toInt
    logger.warn(s"Index elements count: $indexCount, number of partitions = $numPartitions")
    indexes.repartition(numPartitions).cache()
  }
}