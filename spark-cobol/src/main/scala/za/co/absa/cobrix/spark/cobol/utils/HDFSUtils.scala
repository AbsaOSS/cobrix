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

package za.co.absa.cobrix.spark.cobol.utils

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._

/**
  * This object provides utility methods for interacting with HDFS internals.
  */
object HDFSUtils {
  final val bytesInMegabyte = 1048576

  /**
    * Retrieves distinct block locations for a given HDFS file.
    *
    * As now ofsets are informed, the locations for the whole file are returned.
    *
    * The locations are returned as hostnames.
    */
  def getBlocksLocations(path: Path, fileSystem: FileSystem): Seq[String] = {
    getBlocksLocations(path, 0, Long.MaxValue, fileSystem)
  }

  /**
    * Retrieves distinct block locations for the chunk between start and length.
    *
    * The locations are returned as hostnames.
    */
  def getBlocksLocations(path: Path, start: Long, length: Long, fileSystem: FileSystem): Seq[String] = {

    if (start < 0 || length <= 0) {
      throw new IllegalArgumentException(s"Invalid offset or length: offset = $start, length = $length")
    }

    if (fileSystem.getFileStatus(path).isDirectory) {
      throw new IllegalArgumentException(s"Should be a file, not a directory: ${path.getName}")
    }

    fileSystem.getFileBlockLocations(
      fileSystem.getFileStatus(path), start, length)
      .flatMap(_.getHosts)
      .distinct
  }

  /**
    * Returns the default block size of the HDFS filesystem in megabytes.
    *
    * @param fileSystem An HDFS filesystem
    * @param path       An optional path can be provided if the default block size is path-dependent.
    *                   The path should not have to exist.
    *
    * @return A block size in megabytes and None in case of an error
    */
  def getHDFSDefaultBlockSizeMB(fileSystem: FileSystem, path: Option[String] = None): Option[Int] = {
    val hdfsPath = new Path(path.getOrElse("/"))
    val blockSizeInBytes = fileSystem.getDefaultBlockSize(hdfsPath)
    if (blockSizeInBytes > 0) {
      val blockSizeInBM = (blockSizeInBytes / bytesInMegabyte).toInt
      if (blockSizeInBM>0) {
        Some (blockSizeInBM)
      } else {
        None
      }
    } else {
      None
    }
  }

  /** Reads a text File from Hafoop (HDFS, S3, etc) */
  def loadTextFileFromHadoop(hadoopConfiguration: Configuration, copyBookHadoopPath: String): String = {
    val copybookPath = new Path(copyBookHadoopPath)
    val hdfs = copybookPath.getFileSystem(hadoopConfiguration)
    val stream = hdfs.open(copybookPath)
    try IOUtils.readLines(stream, StandardCharsets.UTF_8).asScala.mkString("\n") finally stream.close()
  }
}