
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

package za.co.absa.cobrix.spark.cobol.utils

import org.apache.hadoop.fs.{FileSystem, Path}

/**
  * This object provides utility methods for interacting with HDFS internals.
  */
object HDFSUtils {

  def getBlocksLocations(path: Path, fileSystem: FileSystem): Seq[String] = {
    getBlocksLocations(path, 0, Long.MaxValue, fileSystem)
  }

  def getBlocksLocations(path: Path, start: Long, length: Long, fileSystem: FileSystem): Seq[String] = {

    if (fileSystem.isDirectory(path)) {
      throw new IllegalArgumentException(s"Should be a file, not a directory: ${path.getName}")
    }

    fileSystem.getFileBlockLocations(
      fileSystem.getFileStatus(path), start, length)
      .flatMap(_.getHosts)
  }
}