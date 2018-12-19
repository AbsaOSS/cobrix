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

import za.co.absa.cobrix.spark.cobol.reader.Constants
import za.co.absa.cobrix.spark.cobol.reader.parameters.MultisegmentParameters
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.source.parameters

import scala.collection.mutable.ListBuffer

/**
  * This class provides methods for parsing the parameters set as Spark options.
  */
object CobolParametersParser {

  val SHORT_NAME                      = "cobol"
  val PARAM_COPYBOOK_PATH             = "copybook"
  val PARAM_COPYBOOK_CONTENTS         = "copybook_contents"
  val PARAM_SOURCE_PATH               = "path"
  val PARAM_RECORD_LENGTH             = "record_length_field"
  val PARAM_RECORD_LENGTH_MIN         = "record_length_min"
  val PARAM_RECORD_LENGTH_MAX         = "record_length_max"
  val PARAM_RECORD_START_OFFSET       = "record_start_offset"
  val PARAM_RECORD_END_OFFSET         = "record_end_offset"

  // Schema transformation parameters
  val PARAM_GENERATE_RECORD_ID        = "generate_record_id"
  val PARAM_SCHEMA_RETENTION_POLICY   = "schema_retention_policy"
  val PARAM_GROUP_FILLERS             = "drop_group_fillers"

  // Parameters for multisegment variable length files
  val PARAM_IS_XCOM                   = "is_xcom"
  val PARAM_IS_RECORD_SEQUENCE        = "is_record_sequence"
  val PARAM_SEGMENT_FIELD             = "segment_field"
  val PARAM_SEGMENT_ID_ROOT           = "segment_id_root"
  val PARAM_SEGMENT_FILTER            = "segment_filter"
  val PARAM_SEGMENT_ID_LEVEL_PREFIX   = "segment_id_level"

  // Parameters for signature search reader
  val PARAM_SEARCH_SIGNATURE_FIELD    = "search_field_name"
  val PARAM_SEARCH_SIGNATURE_VALUE    = "search_field_value"

  // Indexed multisegment file processing
  val PARAM_ALLOW_INDEXING            = "allow_indexing"
  val PARAM_INPUT_SPLIT_RECORDS       = "input_split_records"
  val PARAM_INPUT_SPLIT_SIZE_MB       = "input_split_size_mb"
  val PARAM_SEGMENT_ID_PREFIX         = "segment_id_prefix"
  val PARAM_OPTIMIZE_ALLOCATION       = "optimize_allocation"
  val PARAM_IMPROVE_LOCALITY          = "improve_locality"

  def parse(params: Map[String,String]): CobolParameters = {

    val policyName = params.getOrElse(PARAM_SCHEMA_RETENTION_POLICY, "keep_original")
    val policy = SchemaRetentionPolicy.withNameOpt(policyName)

    if (policy.isEmpty) {
      throw new IllegalArgumentException(s"Invalid value '$policyName' for '$PARAM_SCHEMA_RETENTION_POLICY' option.")
    }

    CobolParameters(
      getParameter(PARAM_COPYBOOK_PATH, params),
      getParameter(PARAM_COPYBOOK_CONTENTS, params),
      getParameter(PARAM_SOURCE_PATH, params),
      params.getOrElse(PARAM_IS_XCOM, params.getOrElse(PARAM_IS_RECORD_SEQUENCE, "false")).toBoolean,
      params.getOrElse(PARAM_ALLOW_INDEXING, "true").toBoolean,
      params.get(PARAM_INPUT_SPLIT_RECORDS).map(v => v.toInt),
      params.get(PARAM_INPUT_SPLIT_SIZE_MB).map(v => v.toInt),
      params.getOrElse(PARAM_RECORD_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_RECORD_END_OFFSET, "0").toInt,
      parseVariableLengthParameters(params),
      params.getOrElse(PARAM_GENERATE_RECORD_ID, "false").toBoolean,
      policy.get,
      getParameter(PARAM_SEARCH_SIGNATURE_FIELD, params),
      getParameter(PARAM_SEARCH_SIGNATURE_VALUE, params),
      parseMultisegmentParameters(params),
      params.getOrElse(PARAM_IMPROVE_LOCALITY, "true").toBoolean,
      params.getOrElse(PARAM_OPTIMIZE_ALLOCATION, "false").toBoolean,
      params.getOrElse(PARAM_GROUP_FILLERS, "false").toBoolean
    )
  }

  private def parseVariableLengthParameters(params: Map[String,String]): Option[VariableLengthParameters] = {
    if (params.contains(PARAM_RECORD_LENGTH)) {
      Some(VariableLengthParameters
      (
        params(PARAM_RECORD_LENGTH),
        params.get(PARAM_RECORD_LENGTH_MIN).map(_.toInt),
        params.get(PARAM_RECORD_LENGTH_MAX).map(_.toInt)
      ))
    }
    else {
      None
    }
  }

  /**
    * Parses parameters for reading multisegment mainframe files
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns a multisegment reader parameters
    */
  private def parseMultisegmentParameters(params: Map[String,String]): Option[MultisegmentParameters] = {
    if (params.contains(PARAM_SEGMENT_FIELD)) {
      val levels = parseSegmentLevels(params)
      Some(MultisegmentParameters
      (
        params(PARAM_SEGMENT_FIELD),
        params.get(PARAM_SEGMENT_FILTER).map(_.split(',')),
        levels,
        params.getOrElse(PARAM_SEGMENT_ID_PREFIX, "")
      ))
    }
    else {
      None
    }
  }

  /**
    * Parses the list of segment levels and it's corresponding segment ids.
    *
    * Example:
    * For
    * {{{
    *   sprak.read
    *     .option("segment_id_level0", "SEGID-ROOT")
    *     .option("segment_id_level1", "SEGID-CHD1")
    *     .option("segment_id_level2", "SEGID-CHD2")
    * }}}
    *
    * The corresponding sequence will be like this:
    *
    * {{{
    *    0 -> "SEGID-ROOT"
    *    1 -> "SEGID-CHD1"
    *    2 -> "SEGID-CHD2"
    * }}}
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns a sequence of segment ids on the order of hierarchy levels
    */
  private def parseSegmentLevels(params: Map[String,String]): Seq[String] = {
    val levels = new ListBuffer[String]
    var i = 0
    while (true) {
      val name = s"$PARAM_SEGMENT_ID_LEVEL_PREFIX$i"
      if (params.contains(name)) {
        levels += params(name)
      } else if (i==0 && params.contains(PARAM_SEGMENT_ID_ROOT)){
        levels += params(PARAM_SEGMENT_ID_ROOT)
      } else {
        return levels
      }
      i = i + 1
    }
    levels
  }

  private def getParameter(key: String, params: Map[String,String]): Option[String] = {
    if (params.contains(key)) {
      Some(params(key))
    }
    else {
      None
    }
  }
}