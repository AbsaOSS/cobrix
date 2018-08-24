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

object FileUtils {

  def getAllFilesInDirectory(dir: String, hadoopConf: Configuration): List[String] = {
    getAllFilesInDirectory(dir, FileSystem.get(hadoopConf))
  }

  def getAllFilesInDirectory(dir: String, fs: FileSystem): List[String] = {
    val dirPath = new Path(dir)
    val files: Array[FileStatus] = fs.listStatus(dirPath)
    val allFiles = for (file <- files if fs.isFile(file.getPath)) yield file
    allFiles.map(_.getPath.toUri.getRawPath).toList
  }
}