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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.hadoop.fs.PathFilter
import org.apache.hadoop.mapred.FileInputFormat

/**
  * Retrieves files from a given file system.
  *
  * Supports glob patterns and recursive retrieval.
  *
  * Applies the same filter as Hadoop's FileInputFormat, which excludes files starting with '.' or '_'.
  */
object FileUtils {

  private val hiddenFileFilter = new PathFilter() {
    def accept(p: Path): Boolean = {
      val name = p.getName
      !name.startsWith("_") && !name.startsWith(".")
    }
  }

  /**
    * Retrieves files from a directory, recursively or not.
    *
    * The directory may be informed through a glob pattern.
    */
  def getFiles(dir: String, hadoopConf: Configuration, recursive: Boolean = false): List[String] = {
    getFiles(dir, FileSystem.get(hadoopConf), recursive)
  }

  /**
    * Retrieves files from a directory, recursively or not.
    *
    * The directory may be informed through a glob pattern.
    */
  def getFiles(dir: String, fileSystem: FileSystem, recursive: Boolean): List[String] = {

    val dirPath = new Path(dir)
    val stats: Array[FileStatus] = fileSystem.globStatus(dirPath, hiddenFileFilter)

    if (stats == null) {
      throw new IllegalArgumentException(s"Input path does not exist: $dir")
    }

    val allFiles = stats.flatMap(stat => {
      if (stat.isDirectory) {
        if (recursive) {
          getAllFiles(stat.getPath, fileSystem)
        }
        else {
          fileSystem.listStatus(stat.getPath, hiddenFileFilter).filter(!_.isDirectory)
        }
      }
      else {
        List(stat)
      }
    })

    allFiles.map(_.getPath.toUri.getRawPath).toList
  }

  /**
    * Recursively retrieves all files from the directory tree.
    */
  private def getAllFiles(dir: Path, fileSystem: FileSystem): Seq[FileStatus] = {
    fileSystem.listStatus(dir, hiddenFileFilter).flatMap(stat => {
      if (stat.isDirectory) {
        getAllFiles(stat.getPath, fileSystem)
      }
      else {
        Seq(stat)
      }
    })
  }
}