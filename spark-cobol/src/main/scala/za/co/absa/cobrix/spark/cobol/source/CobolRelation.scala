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

package za.co.absa.cobrix.spark.cobol.source

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenReader
import za.co.absa.cobrix.spark.cobol.reader.{Constants, Reader}
import za.co.absa.cobrix.spark.cobol.reader.varlen.VarLenReader
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, HDFSUtils}
import java.io.{IOException, ObjectInputStream, ObjectOutputStream}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.FileInputFormat
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SimpleIndexEntry
import za.co.absa.cobrix.spark.cobol.source.scanners.CobolScanner
import za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder

import scala.util.control.NonFatal

class SerializableConfiguration(@transient var value: Configuration) extends Serializable {
  private def writeObject(out: ObjectOutputStream): Unit = 
    try {
       out.defaultWriteObject()
       value.write(out)
    } catch {
      case NonFatal(e) =>
        throw new IOException(e)
    }

  private def readObject(in: ObjectInputStream): Unit = 
    try {
    value = new Configuration(false)
    value.readFields(in)
    } catch {
      case NonFatal(e) =>
        throw new IOException(e)
    }
}

/**
  * This class implements an actual Spark relation.
  *
  * It currently supports both, fixed and variable-length records.
  *
  * Its constructor is expected to change after the hierarchy of [[za.co.absa.cobrix.spark.cobol.reader.Reader]] is put in place.
  */
class CobolRelation(sourceDir: String, cobolReader: Reader)(@transient val sqlContext: SQLContext)
  extends BaseRelation
  with Serializable
  with TableScan {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val filesList = getListFilesWithOrder(sourceDir)

  private lazy val indexes: RDD[SimpleIndexEntry] = buildIndex(filesList)

  override def schema: StructType = {
    cobolReader.getSparkSchema
  }

  override def buildScan(): RDD[Row] = {

    cobolReader match {
      case blockReader: FixedLenReader => CobolScanner.buildScanForFixedLength(blockReader, sourceDir, parseRecords, sqlContext)
      case streamReader: VarLenReader if streamReader.isIndexGenerationNeeded => CobolScanner.buildScanForVarLenIndex(streamReader, indexes, filesList, sqlContext)
      case streamReader: VarLenReader => CobolScanner.buildScanForVariableLength(streamReader, filesList, sqlContext)
      case _ => throw new IllegalStateException("Invalid reader object $cobolReader.")
    }
  }





  /**
    * Retrieves a list containing the files contained in the directory to be processed attached to numbers which serve
    * as their order.
    *
    * The List contains [[za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder]] instances.
    */
  private def getListFilesWithOrder(sourceDir: String): Array[FileWithOrder] = {

    FileUtils
      .getFiles(sourceDir, sqlContext.sparkContext.hadoopConfiguration, isRecursiveRetrieval)
      .zipWithIndex
      .map(file => FileWithOrder(file._1, file._2))
      .toArray
  }

  /**
    * Checks if the recursive file retrieval flag is set
    */
  private def isRecursiveRetrieval: Boolean = {
    val hadoopConf = sqlContext.sparkContext.hadoopConfiguration
    hadoopConf.getBoolean(FileInputFormat.INPUT_DIR_RECURSIVE, false)
  }

  private[source] def parseRecords(reader: FixedLenReader, records: RDD[Array[Byte]]) = {
    records.flatMap(record => {
      val it = reader.getRowIterator(record)
      for (parsedRecord <- it) yield {
        parsedRecord
      }
    })
  }

  def buildIndex(filesList: Array[FileWithOrder]): RDD[SimpleIndexEntry] = {
    cobolReader match {
      case reader: VarLenReader if reader.isIndexGenerationNeeded => buildIndexForVarLenReaderWithFullLocality(filesList, reader)
      case _ => null
    }
  }

  def buildIndexForVarLenReader(filesList: Array[FileWithOrder], reader: VarLenReader): RDD[SimpleIndexEntry] = {
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

  def buildIndexForVarLenReaderWithFullLocality(filesList: Array[FileWithOrder], reader: VarLenReader): RDD[SimpleIndexEntry] = {

    val conf = sqlContext.sparkContext.hadoopConfiguration

    val filesRDD = toRDDWithLocality(filesList, conf)

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

    logger.info(s"Creating RDD for ${offsetsLocations.size} located indexes.")
    sqlContext.sparkContext.makeRDD(offsetsLocations)
  }

  /**
    * Converts the list of files into an RDD with preferred locations for the partitions.
    */
  private def toRDDWithLocality(filesList: Array[FileWithOrder], conf: Configuration): RDD[FileWithOrder] = {
    val fileSystem = FileSystem.get(conf)

    val filesWithPreferredLocations = filesList.map(file => {
      (file, HDFSUtils.getBlocksLocations(new Path(file.filePath), fileSystem))
    }).toSeq

    filesWithPreferredLocations.foreach(println)

    sqlContext.sparkContext.makeRDD(filesWithPreferredLocations)
  }
}