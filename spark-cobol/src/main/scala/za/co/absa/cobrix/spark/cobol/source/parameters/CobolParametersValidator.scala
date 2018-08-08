/*
 * Copyright 2018 Barclays Africa Group Limited
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
import java.security.InvalidParameterException

import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import za.co.absa.cobrix.spark.cobol.utils.FileNameUtils
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._

/**
  * This class provides methods for checking the Spark job options after parsed.
  */
object CobolParametersValidator {

  def checkSanity(params: CobolParameters) = {

    if (params.sourcePath.isEmpty) {
      throw new IllegalArgumentException("Data source path must be specified.")
    }

    if (params.copybookPath.isEmpty && params.copybookContent.isEmpty) {
      throw new IllegalArgumentException("Either, copybook path or copybook content must be specified.")
    }
  }

  def validateOrThrow(sparkConf: SparkConf, hadoopConf: Configuration): Unit = {
    val parameters = Map[String, String](PARAM_COPYBOOK_PATH -> sparkConf.get(PARAM_COPYBOOK_PATH), PARAM_SOURCE_PATH -> sparkConf.get
    (PARAM_SOURCE_PATH))
    validateOrThrow(parameters, hadoopConf)
  }

  def validateOrThrow(parameters: Map[String, String], hadoopConf: Configuration): Unit = {
    val hdfs = FileSystem.get(hadoopConf)
    val copyBookContents = parameters.get(PARAM_COPYBOOK_CONTENTS)
    val copyBookPathFileName = parameters.get(PARAM_COPYBOOK_PATH)

    parameters.getOrElse(PARAM_SOURCE_PATH, throw new IllegalStateException(s"Cannot define path to source files: missing " +
      s"parameter: '$PARAM_SOURCE_PATH'"))

    (copyBookContents, copyBookPathFileName) match {
      case (Some(contents), Some(fileName)) =>
        throw new IllegalStateException(s"Both '$PARAM_COPYBOOK_PATH' and '$PARAM_COPYBOOK_CONTENTS' options cannot be specified at the same time")
      case (Some(contents), None) =>
      // This is fine
      case (None, Some(fileName)) =>
        val (isLocalFS, copyBookFileName) = FileNameUtils.getCopyBookFileName(parameters(PARAM_COPYBOOK_PATH))
        if (isLocalFS) {
          if (!Files.exists(Paths.get(copyBookFileName))) {
            throw new FileNotFoundException(s"Copybook not found at $copyBookFileName")
          }
          if (!Files.isReadable(Paths.get(copyBookFileName))) {
            throw new InvalidParameterException(s"Value does not point at a valid Copybook file: $copyBookFileName")
          }
        } else {
          if (!hdfs.exists(new Path(copyBookFileName))) {
            throw new FileNotFoundException(s"Copybook not found at $copyBookFileName")
          }
          if (!hdfs.isFile(new Path(copyBookFileName))) {
            throw new InvalidParameterException(s"Please specify copybook file location via '$PARAM_COPYBOOK_PATH' option or provide copybook " +
              s"contents via '$PARAM_COPYBOOK_CONTENTS' option.")
          }
        }
      case (None, None) =>
        throw new IllegalStateException(s"Cannot define path to source files: missing parameter: '$PARAM_SOURCE_PATH'")
    }

    if (!hdfs.exists(new Path(parameters(PARAM_SOURCE_PATH)))) {
      throw new FileNotFoundException(s"Data source not found at ${parameters(PARAM_SOURCE_PATH)}")
    }
  }
}