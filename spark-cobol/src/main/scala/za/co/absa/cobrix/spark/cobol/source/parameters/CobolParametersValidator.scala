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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser._
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParameters, ReaderParameters}
import za.co.absa.cobrix.spark.cobol.utils.{FileNameUtils, FsType}

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}
import scala.collection.mutable.ListBuffer

/**
  * This class provides methods for checking the Spark job options after parsed.
  */
object CobolParametersValidator {
  private val log = LoggerFactory.getLogger(this.getClass)

  /**
    * Validates the provided COBOL processing parameters and throws an exception
    * if any inconsistency or missing configuration is detected.
    *
    * This method checks only if the copybook is defined and not ambiguous.
    *
    * @param params the COBOL processing parameters to be validated
    * @return this method does not return any value. It throws an IllegalArgumentException if
    *         the validation fails.
    */
  def checkSanity(params: CobolParameters): Unit = {
    if (params.sourcePaths.isEmpty) {
      throw new IllegalArgumentException("Data source path must be specified.")
    }

    if (params.copybookPath.isEmpty && params.copybookContent.isEmpty && params.multiCopybookPath.isEmpty) {
      throw new IllegalArgumentException("Either, copybook path or copybook content must be specified.")
    }
  }

  /**
    * Validates the provided Spark and Hadoop configuration objects and throws an exception
    * if any inconsistency or issue is detected in the configurations.
    *
    * The method extracts essential parameters from the Spark configuration, validates these
    * parameters alongside the Hadoop configuration object, and ensures that all required
    * settings are present and correct. Conflicting or missing configurations are identified
    * and result in exceptions being thrown.
    *
    * @param sparkConf  the Spark configuration object containing parameters necessary for
    *                   this validation step.
    * @param hadoopConf the Hadoop configuration object used to perform validation tasks
    *                   related to Hadoop filesystem operations.
    * @return this method does not return any value. It throws an exception if any validation fails.
    */
  def validateOrThrow(sparkConf: SparkConf, hadoopConf: Configuration): Unit = {
    val parameters = Map[String, String](PARAM_COPYBOOK_PATH -> sparkConf.get(PARAM_COPYBOOK_PATH), PARAM_SOURCE_PATH -> sparkConf.get
    (PARAM_SOURCE_PATH))
    validateOrThrow(parameters, hadoopConf)
  }

  /**
    * Validates the provided parameters for processing COBOL data and throws an exception
    * if any inconsistency or issue is detected in the configurations.
    *
    * The method checks for the presence and validity of parameters required for data input
    * and ensures that conflicting or missing configurations are handled correctly. It also
    * verifies the validity and accessibility of copybook files when specified.
    *
    * @param parameters a map of configuration options to be validated.
    * @param hadoopConf the Hadoop configuration object used to support filesystem operations
    *                   when validating copybook paths in distributed storage systems.
    * @return this method does not return any value. It throws an exception if any validation fails.
    */
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
      val (fsType, copyBookFileName) = FileNameUtils.getCopyBookFileName(fileName)
      if (fsType == FsType.LocalFs) {
        if (!Files.exists(Paths.get(copyBookFileName))) {
          throw new FileNotFoundException(s"Copybook not found at $copyBookFileName")
        }
        if (!Files.isRegularFile(Paths.get(copyBookFileName))) {
          throw new IllegalArgumentException(s"The copybook path '$copyBookFileName' is not a file.")
        }
        if (!Files.isReadable(Paths.get(copyBookFileName))) {
          throw new IllegalArgumentException(s"The copybook path '$copyBookFileName' is not readable.")
        }
      } else if (fsType == FsType.JarFs) {
        if (getClass.getResourceAsStream(copyBookFileName) == null) {
          if (!Files.exists(Paths.get(copyBookFileName))) {
            throw new FileNotFoundException(s"Copybook not found at the JAR resource path: $copyBookFileName")
          }
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

  /**
    * Validates the parameters provided for writing operations in COBOL data processing.
    * Verifies that the specified configurations are compatible with the writer and throws
    * an exception in case of violations.
    *
    * @param readerParameters the configuration containing options to be validated for writing operations.
    * @return this method does not return any value but throws an IllegalArgumentException if the validation fails.
    */
  def validateParametersForWriting(readerParameters: ReaderParameters): Unit = {
    val issues = new ListBuffer[String]

    if (readerParameters.recordFormat != RecordFormat.FixedLength && readerParameters.recordFormat != RecordFormat.VariableLength) {
      issues += s"Only '${RecordFormat.FixedLength}' and '${RecordFormat.VariableLength}' values for 'record_format' are supported for writing, " +
        s"provided value: '${readerParameters.recordFormat}'"
    }

    if (readerParameters.variableSizeOccurs &&
      readerParameters.recordFormat == RecordFormat.FixedLength) {
      log.warn("Option 'variable_size_occurs=true' is used with 'record_format=F' which means records can have variable length. It is highly recommended to use 'record_format=V' instead.")
    }

    if (readerParameters.occursMappings.nonEmpty) {
      issues += "OCCURS mapping option ('occurs_mappings') is not supported for writing"
    }

    if (readerParameters.startOffset != 0 || readerParameters.endOffset != 0) {
      issues += "'record_start_offset' and 'record_end_offset' are not supported for writing"
    }

    if (readerParameters.fileStartOffset != 0 || readerParameters.fileEndOffset != 0) {
      issues += "'file_start_offset' and 'file_end_offset' are not supported for writing"
    }

    if (readerParameters.multisegment.isDefined) {
      issues += "Multi-segment options ('segment_field', 'segment_filter', etc) are not supported for writing"
    }

    if (issues.nonEmpty) {
      throw new IllegalArgumentException(s"Writer validation issues: ${issues.mkString("; ")}")
    }
  }
}
