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

package za.co.absa.cobrix.spark.cobol.source.copybook

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParameters
import za.co.absa.cobrix.spark.cobol.utils.FileNameUtils

import scala.collection.JavaConverters._

object CopybookContentLoader {

  def load(parameters: CobolParameters, hadoopConf: Configuration): Seq[String] = {

    val copyBookContents = parameters.copybookContent
    val copyBookPathFileName = parameters.copybookPath

    (copyBookContents, copyBookPathFileName) match {
      case (Some(contents), _) => Seq(contents)
      case (None, Some(_)) =>
        val (isLocalFS, copyBookFileName) = FileNameUtils.getCopyBookFileName(copyBookPathFileName.get)
        Seq(
          if (isLocalFS) {
            loadCopybookFromLocalFS(copyBookFileName)
          } else {
            loadCopybookFromHDFS(hadoopConf, copyBookFileName)
          }
        )
      case (None, None) => parameters.multiCopybookPath.map(
        fileName => {
          val (isLocalFS, copyBookFileName) = FileNameUtils.getCopyBookFileName(fileName)
          if (isLocalFS) {
            loadCopybookFromLocalFS(copyBookFileName)
          } else {
            loadCopybookFromHDFS(hadoopConf, copyBookFileName)
          }
        }
      )
    }
  }

  private def loadCopybookFromLocalFS(copyBookLocalPath: String): String = {
    Files.readAllLines(Paths.get(copyBookLocalPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
  }

  private def loadCopybookFromHDFS(hadoopConfiguration: Configuration, copyBookHDFSPath: String): String = {
    val hdfs = FileSystem.get(hadoopConfiguration)
    val stream = hdfs.open(new Path(copyBookHDFSPath))
    try IOUtils.readLines(stream).asScala.mkString("\n") finally stream.close()
  }
}
