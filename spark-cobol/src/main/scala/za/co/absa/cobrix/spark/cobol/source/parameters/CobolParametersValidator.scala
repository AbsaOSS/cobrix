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

package za.co.absa.cobrix.spark.cobol.source.parameters

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import za.co.absa.cobrix.cobol.reader.parameters.CobolParameters
import za.co.absa.cobrix.spark.cobol.parameters.CobolParametersParser._
import za.co.absa.cobrix.spark.cobol.utils.FileNameUtils

/**
  * This class provides methods for checking the Spark job options after parsed.
  */
object CobolParametersValidator {

  def checkSanity(params: CobolParameters) = {
    if (params.sourcePaths.isEmpty) {
      throw new IllegalArgumentException("Data source path must be specified.")
    }

    if (params.copybookPath.isEmpty && params.copybookContent.isEmpty && params.multiCopybookPath.isEmpty) {
      throw new IllegalArgumentException("Either, copybook path or copybook content must be specified.")
    }
  }

  def validateOrThrow(sparkConf: SparkConf, hadoopConf: Configuration): Unit = {
    val parameters = Map[String, String](PARAM_COPYBOOK_PATH -> sparkConf.get(PARAM_COPYBOOK_PATH), PARAM_SOURCE_PATH -> sparkConf.get
    (PARAM_SOURCE_PATH))
    validateOrThrow(parameters, hadoopConf)
  }

  def validateOrThrow(parameters: Map[String, String], hadoopConf: Configuration): Unit = {
    val copyBookContents = parameters.get(PARAM_COPYBOOK_CONTENTS)
    val copyBookPathFileName = parameters.get(PARAM_COPYBOOK_PATH)
    val copyBookMultiPathFileNames = parameters.get(PARAM_MULTI_COPYBOOK_PATH)

    if (!parameters.isDefinedAt(PARAM_SOURCE_PATH) && !parameters.isDefinedAt(PARAM_SOURCE_PATHS) && !parameters.isDefinedAt(PARAM_SOURCE_PATHS_LEGACY)) {
      throw new IllegalStateException(s"Cannot define path to data files: missing " +
        s"parameter: '$PARAM_SOURCE_PATH' or '$PARAM_SOURCE_PATHS'. `path` is automatically set when you invoke .load()")
    }

    if (parameters.isDefinedAt(PARAM_SOURCE_PATHS) && parameters.isDefinedAt(PARAM_SOURCE_PATHS_LEGACY)) {
      throw new IllegalStateException(s"Only one of '$PARAM_SOURCE_PATHS' or '$PARAM_SOURCE_PATHS_LEGACY' [deprecated] should be defined.")
    }

    if (parameters.isDefinedAt(PARAM_SOURCE_PATH) && parameters.isDefinedAt(PARAM_SOURCE_PATHS)) {
      throw new IllegalStateException(s"Only one of '$PARAM_SOURCE_PATH' or '$PARAM_SOURCE_PATHS' should be defined.")
    }

    def validatePath(fileName: String): Unit = {
      val (isLocalFS, copyBookFileName) = FileNameUtils.getCopyBookFileName(fileName)
      if (isLocalFS) {
        if (!Files.exists(Paths.get(copyBookFileName))) {
          throw new FileNotFoundException(s"Copybook not found at $copyBookFileName")
        }
        if (!Files.isRegularFile(Paths.get(copyBookFileName))) {
          throw new IllegalArgumentException(s"The copybook path '$copyBookFileName' is not a file.")
        }
        if (!Files.isReadable(Paths.get(copyBookFileName))) {
          throw new IllegalArgumentException(s"The copybook path '$copyBookFileName' is not readable.")
        }
      } else {
        val fs = new Path(fileName).getFileSystem(hadoopConf)
        if (!fs.exists(new Path(copyBookFileName))) {
          throw new FileNotFoundException(s"Copybook not found at $copyBookFileName")
        }
        if (!fs.getFileStatus(new Path(copyBookFileName)).isFile) {
          throw new IllegalArgumentException(s"The copybook path '$copyBookFileName' is not a file.")
        }
      }
    }

    (copyBookContents, copyBookPathFileName, copyBookMultiPathFileNames) match {
      case (Some(_), Some(_), _) =>
        throw new IllegalStateException(s"Both '$PARAM_COPYBOOK_PATH' and '$PARAM_COPYBOOK_CONTENTS' options cannot be specified at the same time")
      case (Some(_), _, Some(_)) =>
        throw new IllegalStateException(s"Both '$PARAM_MULTI_COPYBOOK_PATH' and '$PARAM_COPYBOOK_CONTENTS' options cannot be specified at the same time")
      case (_, Some(_), Some(_)) =>
        throw new IllegalStateException(s"Both '$PARAM_COPYBOOK_PATH' and '$PARAM_MULTI_COPYBOOK_PATH' options cannot be specified at the same time")
      case (None, None, None) =>
        throw new IllegalStateException("COPYBOOK is not provided. Please, provide one of the options: " +
          s"'$PARAM_COPYBOOK_PATH', '$PARAM_COPYBOOK_CONTENTS', '$PARAM_MULTI_COPYBOOK_PATH'.")
      case (Some(_), None, None) =>
        // Nothing to validate
      case (None, Some(fileName), None) =>
        validatePath(fileName)
      case (None, None, Some(fileNames)) =>
        for(fileName <-fileNames.split(","))
          validatePath(fileName)
    }
  }
}