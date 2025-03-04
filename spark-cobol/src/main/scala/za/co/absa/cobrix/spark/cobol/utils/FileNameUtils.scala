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

object FileNameUtils {
  private val LOCALFS_PREFIX = "file://"
  private val JAR_PREFIX = "jar://"

  def getCopyBookFileName(fileNameURI: String): (FsType, String) = {
    val isLocalFS = fileNameURI.toLowerCase.startsWith(LOCALFS_PREFIX)
    val isJar = fileNameURI.toLowerCase.startsWith(JAR_PREFIX)
    if (isLocalFS)
      (FsType.LocalFs, fileNameURI.drop(LOCALFS_PREFIX.length))
    else if (isJar) {
      val fileCandidate = fileNameURI.drop(JAR_PREFIX.length)
      val filePath = if (fileCandidate.startsWith("/")) fileCandidate else s"/$fileCandidate"
      (FsType.JarFs, filePath)
    }
    else
      (FsType.HadoopFs, fileNameURI)
  }

}
