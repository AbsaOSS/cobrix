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

import org.apache.hadoop.conf.Configuration
import za.co.absa.cobrix.cobol.reader.parameters.CobolParameters
import za.co.absa.cobrix.spark.cobol.utils.FsType.LocalFs
import za.co.absa.cobrix.spark.cobol.utils.{FileNameUtils, FsType, HDFSUtils, ResourceUtils}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object CopybookContentLoader {

  def load(parameters: CobolParameters, hadoopConf: Configuration): Seq[String] = {

    val copyBookContents = parameters.copybookContent
    val copyBookPathFileName = parameters.copybookPath

    (copyBookContents, copyBookPathFileName) match {
      case (Some(contents), _) => Seq(contents)
      case (None, Some(_)) =>
        val copybookContent = loadCopybook(copyBookPathFileName.get, hadoopConf)
        Seq(copybookContent)
      case (None, None) =>
        parameters.multiCopybookPath.map(fileName => loadCopybook(fileName, hadoopConf))
    }
  }

  private def loadCopybook(pathToCopybook: String, hadoopConf: Configuration): String = {
    val (fsType, copyBookFileName) = FileNameUtils.getCopyBookFileName(pathToCopybook)
    fsType match {
      case FsType.LocalFs  => loadCopybookFromLocalFS(copyBookFileName)
      case FsType.JarFs    => loadCopybookFromJarResources(copyBookFileName)
      case FsType.HadoopFs => HDFSUtils.loadTextFileFromHadoop(hadoopConf, copyBookFileName)
    }
  }

  private def loadCopybookFromLocalFS(copyBookLocalPath: String): String = {
    Files.readAllLines(Paths.get(copyBookLocalPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
  }

  private def loadCopybookFromJarResources(copyBookJarPath: String): String = {
    ResourceUtils.readResourceAsString(copyBookJarPath)
  }
}
