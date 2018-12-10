/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SimpleIndexEntry
import za.co.absa.cobrix.spark.cobol.reader.varlen.VarLenReader
import za.co.absa.cobrix.spark.cobol.source.SerializableConfiguration
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

  def buildIndex(filesList: Array[FileWithOrder], cobolReader: Reader, sqlContext: SQLContext): RDD[SimpleIndexEntry] = {
    cobolReader match {
      case reader: VarLenReader if reader.isIndexGenerationNeeded => buildIndexForVarLenReaderWithFullLocality(filesList, reader, sqlContext)
      case _ => null
    }
  }

  /**
    * Builds the indexes by querying HDFS about the records locations and then asking Spark to assign local executors
    * to those records in those locations.
    */
  def buildIndexForVarLenReaderWithFullLocality(filesList: Array[FileWithOrder], reader: VarLenReader, sqlContext: SQLContext): RDD[SimpleIndexEntry] = {

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
          val index = reader.generateIndex(new FileStreamer(filePath, fileSystem, 0, 0), fileOrder)

          index.map(entry => {
            val offset = if (entry.offsetFrom >= 0) entry.offsetFrom else 0
            val length = if (entry.offsetTo > 0) entry.offsetTo else Long.MaxValue
            (entry, HDFSUtils.getBlocksLocations(new Path(filePath), offset, length, fileSystem))
          })
        }
        )
      })

    logger.info("Going to collect located indexes into driver.")
    val offsetsLocations = indexes.collect()

    val optimizedAllocation = optimizeDistribution(offsetsLocations, sqlContext.sparkContext)

    logger.info(s"Creating RDD for ${offsetsLocations.length} located indexes.")
    sqlContext.sparkContext.makeRDD(optimizedAllocation)
  }

  /**
    * Tries to balance the allocation among unused executors.
    */
  private def optimizeDistribution(allocation: Seq[(SimpleIndexEntry,Seq[String])], sc: SparkContext): Seq[(SimpleIndexEntry,Seq[String])] = {
    val availableExecutors = SparkUtils.currentActiveExecutors(sc)

    logger.info(s"Trying to balance ${allocation.size} partitions among all available executors (${availableExecutors})")

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

    filesWithPreferredLocations.foreach(println)

    sqlContext.sparkContext.makeRDD(filesWithPreferredLocations)
  }

  /**
    * Builds records indexes. Does not take locality into account. Might be removed in further releases.
    */
  @Deprecated
  def buildIndexForVarLenReader(filesList: Array[FileWithOrder], reader: VarLenReader, sqlContext: SQLContext): RDD[SimpleIndexEntry] = {
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
          val index = reader.generateIndex(new FileStreamer(filePath, fileSystem, 0, 0), fileOrder)
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