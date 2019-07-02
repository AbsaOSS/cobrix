/*
 * Copyright 2018-2019 ABSA Group Limited
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

import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy
import za.co.absa.cobrix.spark.cobol.reader.parameters.MultisegmentParameters
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

import scala.collection.mutable.ListBuffer

/**
  * This class provides methods for parsing the parameters set as Spark options.
  */
object CobolParametersParser {

  val SHORT_NAME                      = "cobol"
  val PARAM_COPYBOOK_PATH             = "copybook"
  val PARAM_MULTI_COPYBOOK_PATH       = "copybooks"
  val PARAM_COPYBOOK_CONTENTS         = "copybook_contents"
  val PARAM_SOURCE_PATH               = "path"
  val PARAM_ENCODING                  = "encoding"
  val PARAM_RECORD_LENGTH             = "record_length_field"
  val PARAM_RECORD_LENGTH_MIN         = "record_length_min"
  val PARAM_RECORD_LENGTH_MAX         = "record_length_max"
  val PARAM_RECORD_START_OFFSET       = "record_start_offset"
  val PARAM_RECORD_END_OFFSET         = "record_end_offset"
  val PARAM_FILE_START_OFFSET         = "file_start_offset"
  val PARAM_FILE_END_OFFSET           = "file_end_offset"

  // Schema transformation parameters
  val PARAM_GENERATE_RECORD_ID        = "generate_record_id"
  val PARAM_SCHEMA_RETENTION_POLICY   = "schema_retention_policy"
  val PARAM_GROUP_FILLERS             = "drop_group_fillers"
  val PARAM_GROUP_NOT_TERMINALS       = "non_terminals"

  // Data parsing parameters
  val PARAM_STRING_TRIMMING_POLICY    = "string_trimming_policy"
  val PARAM_EBCDIC_CODE_PAGE          = "ebcdic_code_page"
  val PARAM_EBCDIC_CODE_PAGE_CLASS    = "ebcdic_code_page_class"

  // Parameters for multisegment variable length files
  val PARAM_IS_XCOM                   = "is_xcom"
  val PARAM_IS_RECORD_SEQUENCE        = "is_record_sequence"
  val PARAM_IS_RDW_BIG_ENDIAN         = "is_rdw_big_endian"
  val PARAM_IS_RDW_PART_REC_LENGTH    = "is_rdw_part_of_record_length"
  val PARAM_RDW_ADJUSTMENT            = "rdw_adjustment"
  val PARAM_SEGMENT_FIELD             = "segment_field"
  val PARAM_SEGMENT_ID_ROOT           = "segment_id_root"
  val PARAM_SEGMENT_FILTER            = "segment_filter"
  val PARAM_SEGMENT_ID_LEVEL_PREFIX   = "segment_id_level"
  val PARAM_RECORD_HEADER_PARSER      = "record_header_parser"

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

  // Parameters for debugging
  val PARAM_DEBUG_IGNORE_FILE_SIZE    = "debug_ignore_file_size"

  private def getSchemaRetentionPolicy(params: Map[String,String]): SchemaRetentionPolicy = {
    val schemaRetentionPolicyName = params.getOrElse(PARAM_SCHEMA_RETENTION_POLICY, "keep_original")
    val schemaRetentionPolicy = SchemaRetentionPolicy.withNameOpt(schemaRetentionPolicyName)

    schemaRetentionPolicy match {
      case Some(p) =>
        p
      case None =>
        throw new IllegalArgumentException(s"Invalid value '$schemaRetentionPolicyName' for '$PARAM_SCHEMA_RETENTION_POLICY' option.")
    }
  }

  private def getStringTrimmingPolicy(params: Map[String,String]): StringTrimmingPolicy = {
    val stringTrimmingPolicyName = params.getOrElse(PARAM_STRING_TRIMMING_POLICY, "both")
    val stringTrimmingPolicy = StringTrimmingPolicy.withNameOpt(stringTrimmingPolicyName)

    stringTrimmingPolicy match {
      case Some(p) =>
        p
      case None =>
        throw new IllegalArgumentException(s"Invalid value '$stringTrimmingPolicy' for '$PARAM_STRING_TRIMMING_POLICY' option.")
    }
  }

  def parse(params: Map[String,String]): CobolParameters = {
    val schemaRetentionPolicy = getSchemaRetentionPolicy(params)
    val stringTrimmingPolicy = getStringTrimmingPolicy(params)
    val ebcdicCodePageName = params.getOrElse(PARAM_EBCDIC_CODE_PAGE, "common")
    val ebcdicCodePageClass = params.get(PARAM_EBCDIC_CODE_PAGE_CLASS)

    val encoding = params.getOrElse(PARAM_ENCODING, "")
    val isEbcdic = {
      if (encoding.isEmpty || encoding.compareToIgnoreCase("ebcdic") == 0) {
        true
      } else {
        if (encoding.compareToIgnoreCase("ascii") == 0) {
          false
        } else {
          throw new IllegalArgumentException(s"Invalid value '$encoding' for '$PARAM_ENCODING' option. Should be either 'EBCDIC' or 'ASCII'.")
        }
      }
    }

    CobolParameters(
      getParameter(PARAM_COPYBOOK_PATH, params),
      params.getOrElse(PARAM_MULTI_COPYBOOK_PATH, "").split(','),
      getParameter(PARAM_COPYBOOK_CONTENTS, params),
      getParameter(PARAM_SOURCE_PATH, params),
      isEbcdic,
      ebcdicCodePageName,
      ebcdicCodePageClass,
      params.getOrElse(PARAM_IS_XCOM, params.getOrElse(PARAM_IS_RECORD_SEQUENCE, "false")).toBoolean,
      params.getOrElse(PARAM_IS_RDW_BIG_ENDIAN, "false").toBoolean,
      params.getOrElse(PARAM_IS_RDW_PART_REC_LENGTH, "false").toBoolean,
      params.getOrElse(PARAM_RDW_ADJUSTMENT, "0").toInt,
      params.getOrElse(PARAM_ALLOW_INDEXING, "true").toBoolean,
      params.get(PARAM_INPUT_SPLIT_RECORDS).map(v => v.toInt),
      params.get(PARAM_INPUT_SPLIT_SIZE_MB).map(v => v.toInt),
      params.getOrElse(PARAM_RECORD_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_RECORD_END_OFFSET, "0").toInt,
      params.getOrElse(PARAM_FILE_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_FILE_END_OFFSET, "0").toInt,
      parseVariableLengthParameters(params),
      params.getOrElse(PARAM_GENERATE_RECORD_ID, "false").toBoolean,
      schemaRetentionPolicy,
      stringTrimmingPolicy,
      getParameter(PARAM_SEARCH_SIGNATURE_FIELD, params),
      getParameter(PARAM_SEARCH_SIGNATURE_VALUE, params),
      parseMultisegmentParameters(params),
      params.getOrElse(PARAM_IMPROVE_LOCALITY, "true").toBoolean,
      params.getOrElse(PARAM_OPTIMIZE_ALLOCATION, "false").toBoolean,
      params.getOrElse(PARAM_GROUP_FILLERS, "false").toBoolean,
      params.getOrElse(PARAM_GROUP_NOT_TERMINALS, "").split(','),
      params.get(PARAM_RECORD_HEADER_PARSER),
      params.getOrElse(PARAM_DEBUG_IGNORE_FILE_SIZE, "false").toBoolean
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
        params.getOrElse(PARAM_SEGMENT_ID_PREFIX, ""),
        getSegmentIdRedefineMapping(params)
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

  // key - segment id, value - redefine field name
  /**
    * Parses the list of redefines and their corresponding segment ids.
    *
    * Example:
    * For
    * {{{
    *   sprak.read
    *     .option("redefine-segment-id-map:0", "COMPANY => C,D")
    *     .option("redefine-segment-id-map:1", "CONTACT => P")
    * }}}
    *
    * The corresponding mapping will be:
    *
    * {{{
    *    "C" -> "COMPANY"
    *    "D" -> "COMPANY"
    *    "P" -> "PERSON"
    * }}}
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns a sequence of segment ids on the order of hierarchy levels
    */
  def getSegmentIdRedefineMapping(params: Map[String, String]): Map[String, String] = {
    params.flatMap {
      case (k, v) =>
        val keyNoCase = k.toLowerCase
        if (keyNoCase.startsWith("redefine-segment-id-map") ||
          keyNoCase.startsWith("redefine_segment_id_map")) {
          val splitVal = v.split("\\=\\>")
          if (splitVal.lengthCompare(2) !=0) {
            throw new IllegalArgumentException(s"Illegal argument for the 'redefine_segment_id_map' option: '$v'.")
          }
          val redefine = splitVal(0).trim
          val segmentIds = splitVal(1).split(',').map(_.trim)
          segmentIds.map(segmentId => (segmentId, CopybookParser.transformIdentifier(redefine)))
        } else {
          Nil
        }
    }
  }


}