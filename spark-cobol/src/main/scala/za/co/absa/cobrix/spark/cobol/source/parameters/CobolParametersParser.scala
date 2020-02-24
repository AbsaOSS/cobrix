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

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.policies.{CommentPolicy, StringTrimmingPolicy}
import za.co.absa.cobrix.spark.cobol.reader.parameters.MultisegmentParameters
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.utils.Parameters

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * This class provides methods for parsing the parameters set as Spark options.
  */
object CobolParametersParser {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val SHORT_NAME                      = "cobol"
  val PARAM_COPYBOOK_PATH             = "copybook"
  val PARAM_MULTI_COPYBOOK_PATH       = "copybooks"
  val PARAM_COPYBOOK_CONTENTS         = "copybook_contents"
  val PARAM_SOURCE_PATH               = "path"
  val PARAM_ENCODING                  = "encoding"
  val PARAM_PEDANTIC                  = "pedantic"
  val PARAM_RECORD_LENGTH             = "record_length_field"
  val PARAM_RECORD_START_OFFSET       = "record_start_offset"
  val PARAM_RECORD_END_OFFSET         = "record_end_offset"
  val PARAM_FILE_START_OFFSET         = "file_start_offset"
  val PARAM_FILE_END_OFFSET           = "file_end_offset"

  // Schema transformation parameters
  val PARAM_GENERATE_RECORD_ID        = "generate_record_id"
  val PARAM_SCHEMA_RETENTION_POLICY   = "schema_retention_policy"
  val PARAM_GROUP_FILLERS             = "drop_group_fillers"
  val PARAM_GROUP_NOT_TERMINALS       = "non_terminals"

  // General parsing parameters
  val PARAM_TRUNCATE_COMMENTS         = "truncate_comments"
  val PARAM_COMMENTS_LBOUND           = "comments_lbound"
  val PARAM_COMMENTS_UBOUND           = "comments_ubound"

  // Data parsing parameters
  val PARAM_STRING_TRIMMING_POLICY    = "string_trimming_policy"
  val PARAM_EBCDIC_CODE_PAGE          = "ebcdic_code_page"
  val PARAM_EBCDIC_CODE_PAGE_CLASS    = "ebcdic_code_page_class"
  val PARAM_ASCII_CHARSET             = "ascii_charset"
  val PARAM_FLOATING_POINT_FORMAT     = "floating_point_format"
  val PARAM_VARIABLE_SIZE_OCCURS      = "variable_size_occurs"

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
  val PARAM_RHP_ADDITIONAL_INFO       = "rhp_additional_info"
  val PARAM_INPUT_FILE_COLUMN         = "with_input_file_name_col"

  // Indexed multisegment file processing
  val PARAM_ALLOW_INDEXING            = "allow_indexing"
  val PARAM_INPUT_SPLIT_RECORDS       = "input_split_records"
  val PARAM_INPUT_SPLIT_SIZE_MB       = "input_split_size_mb"
  val PARAM_SEGMENT_ID_PREFIX         = "segment_id_prefix"
  val PARAM_OPTIMIZE_ALLOCATION       = "optimize_allocation"
  val PARAM_IMPROVE_LOCALITY          = "improve_locality"

  // Parameters for debugging
  val PARAM_DEBUG_IGNORE_FILE_SIZE    = "debug_ignore_file_size"

  private def getSchemaRetentionPolicy(params: Parameters): SchemaRetentionPolicy = {
    val schemaRetentionPolicyName = params.getOrElse(PARAM_SCHEMA_RETENTION_POLICY, "keep_original")
    val schemaRetentionPolicy = SchemaRetentionPolicy.withNameOpt(schemaRetentionPolicyName)

    schemaRetentionPolicy match {
      case Some(p) =>
        p
      case None =>
        throw new IllegalArgumentException(s"Invalid value '$schemaRetentionPolicyName' for '$PARAM_SCHEMA_RETENTION_POLICY' option.")
    }
  }

  private def getStringTrimmingPolicy(params: Parameters): StringTrimmingPolicy = {
    val stringTrimmingPolicyName = params.getOrElse(PARAM_STRING_TRIMMING_POLICY, "both")
    val stringTrimmingPolicy = StringTrimmingPolicy.withNameOpt(stringTrimmingPolicyName)

    stringTrimmingPolicy match {
      case Some(p) =>
        p
      case None =>
        throw new IllegalArgumentException(s"Invalid value '$stringTrimmingPolicy' for '$PARAM_STRING_TRIMMING_POLICY' option.")
    }
  }

  /**
    * Parses comment truncation parameters
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns an instance of omment truncation parameters
    */
  @throws(classOf[IllegalArgumentException])
  private def parseCommentTruncationPolicy(params: Parameters): CommentPolicy = {
    var commentParams = CommentPolicy()

    if (params.contains(PARAM_TRUNCATE_COMMENTS)) {
      val truncateComments = params(PARAM_TRUNCATE_COMMENTS).toBoolean
      commentParams = commentParams.copy(truncateComments = truncateComments)

      if (!truncateComments) {
        if (params.contains(PARAM_COMMENTS_LBOUND) || params.contains(PARAM_COMMENTS_UBOUND)) {
          throw new IllegalArgumentException(s"When '$PARAM_TRUNCATE_COMMENTS=false' the following parameters cannot " +
            s"be used: '$PARAM_COMMENTS_LBOUND', '$PARAM_COMMENTS_UBOUND'."
          )
        }
      }
    }

    if (params.contains(PARAM_COMMENTS_LBOUND)) {
      val lbound = params(PARAM_COMMENTS_LBOUND).toInt
      commentParams = commentParams.copy(commentsUpToChar = lbound)
    }

    if (params.contains(PARAM_COMMENTS_UBOUND)) {
      val ubound = params(PARAM_COMMENTS_UBOUND).toInt
      commentParams = commentParams.copy(commentsAfterChar = ubound)
    }

    commentParams
  }

  private def getFloatingPointFormat(params: Parameters): FloatingPointFormat = {
    val floatingPointFormatName = params.getOrElse(PARAM_FLOATING_POINT_FORMAT, "IBM")
    val floatingPointFormat = FloatingPointFormat.withNameOpt(floatingPointFormatName)

    floatingPointFormat match {
      case Some(p) =>
        p
      case None =>
        throw new IllegalArgumentException(s"Invalid value '$floatingPointFormat' for '$PARAM_FLOATING_POINT_FORMAT' option.")
    }
  }

  def parse(params: Parameters): CobolParameters = {
    val schemaRetentionPolicy = getSchemaRetentionPolicy(params)
    val stringTrimmingPolicy = getStringTrimmingPolicy(params)
    val ebcdicCodePageName = params.getOrElse(PARAM_EBCDIC_CODE_PAGE, "common")
    val ebcdicCodePageClass = params.get(PARAM_EBCDIC_CODE_PAGE_CLASS)
    val asciiCharset = params.getOrElse(PARAM_ASCII_CHARSET, "")

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

    val cobolParameters = CobolParameters(
      getParameter(PARAM_COPYBOOK_PATH, params),
      params.getOrElse(PARAM_MULTI_COPYBOOK_PATH, "").split(','),
      getParameter(PARAM_COPYBOOK_CONTENTS, params),
      getParameter(PARAM_SOURCE_PATH, params),
      isEbcdic,
      ebcdicCodePageName,
      ebcdicCodePageClass,
      asciiCharset,
      getFloatingPointFormat(params),
      params.getOrElse(PARAM_RECORD_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_RECORD_END_OFFSET, "0").toInt,
      parseVariableLengthParameters(params),
      schemaRetentionPolicy,
      stringTrimmingPolicy,
      parseMultisegmentParameters(params),
      parseCommentTruncationPolicy(params),
      params.getOrElse(PARAM_GROUP_FILLERS, "false").toBoolean,
      params.getOrElse(PARAM_GROUP_NOT_TERMINALS, "").split(','),
      params.getOrElse(PARAM_DEBUG_IGNORE_FILE_SIZE, "false").toBoolean
    )
    validateSparkCobolOptions(params)
    cobolParameters
  }

  private def parseVariableLengthParameters(params: Parameters): Option[VariableLengthParameters] = {
    val recordLengthFieldOpt = params.get(PARAM_RECORD_LENGTH)
    val isRecordSequence = params.getOrElse(PARAM_IS_XCOM, params.getOrElse(PARAM_IS_RECORD_SEQUENCE, "false")).toBoolean
    val isRecordIdGenerationEnabled = params.getOrElse(PARAM_GENERATE_RECORD_ID, "false").toBoolean
    val fileStartOffset = params.getOrElse(PARAM_FILE_START_OFFSET, "0").toInt
    val fileEndOffset = params.getOrElse(PARAM_FILE_END_OFFSET, "0").toInt
    val varLenOccursEnabled = params.getOrElse(PARAM_VARIABLE_SIZE_OCCURS, "false").toBoolean

    if (params.contains(PARAM_RECORD_LENGTH) &&
      (params.contains(PARAM_IS_RECORD_SEQUENCE) || params.contains(PARAM_IS_XCOM) )) {
      throw new IllegalArgumentException(s"Option '$PARAM_RECORD_LENGTH' cannot be used together with '$PARAM_IS_RECORD_SEQUENCE' or '$PARAM_IS_XCOM'.")
    }

    if (recordLengthFieldOpt.isDefined ||
      isRecordSequence ||
      isRecordIdGenerationEnabled ||
      fileStartOffset > 0 ||
      fileEndOffset > 0 ||
      varLenOccursEnabled
    ) {
      Some(VariableLengthParameters
      (
        isRecordSequence,
        params.getOrElse(PARAM_IS_RDW_BIG_ENDIAN, "false").toBoolean,
        params.getOrElse(PARAM_IS_RDW_PART_REC_LENGTH, "false").toBoolean,
        params.getOrElse(PARAM_RDW_ADJUSTMENT, "0").toInt,
        params.get(PARAM_RECORD_HEADER_PARSER),
        params.get(PARAM_RHP_ADDITIONAL_INFO),
        recordLengthFieldOpt.getOrElse(""),
        fileStartOffset,
        fileEndOffset,
        varLenOccursEnabled,
        isRecordIdGenerationEnabled,
        params.getOrElse(PARAM_ALLOW_INDEXING, "true").toBoolean,
        params.get(PARAM_INPUT_SPLIT_RECORDS).map(v => v.toInt),
        params.get(PARAM_INPUT_SPLIT_SIZE_MB).map(v => v.toInt),
        params.getOrElse(PARAM_IMPROVE_LOCALITY, "true").toBoolean,
        params.getOrElse(PARAM_OPTIMIZE_ALLOCATION, "false").toBoolean,
        params.getOrElse(PARAM_INPUT_FILE_COLUMN, "")
      ))
    } else {
      None
    }
  }

  /**
    * Parses parameters for reading multisegment mainframe files
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns a multisegment reader parameters
    */
  private def parseMultisegmentParameters(params: Parameters): Option[MultisegmentParameters] = {
    if (params.contains(PARAM_SEGMENT_FIELD)) {
      val levels = parseSegmentLevels(params)
      Some(MultisegmentParameters
      (
        params(PARAM_SEGMENT_FIELD),
        params.get(PARAM_SEGMENT_FILTER).map(_.split(',')),
        levels,
        params.getOrElse(PARAM_SEGMENT_ID_PREFIX, ""),
        getSegmentIdRedefineMapping(params),
        getSegmentRedefineParents(params)
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
  private def parseSegmentLevels(params: Parameters): Seq[String] = {
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

  private def getParameter(key: String, params: Parameters): Option[String] = {
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
  @throws(classOf[IllegalArgumentException])
  def getSegmentIdRedefineMapping(params: Parameters): Map[String, String] = {
    params.getMap.flatMap {
      case (k, v) =>
        val keyNoCase = k.toLowerCase
        if (keyNoCase.startsWith("redefine-segment-id-map") ||
          keyNoCase.startsWith("redefine_segment_id_map")) {
          params.markUsed(k)
          val splitVal = v.split("\\=\\>")
          if (splitVal.lengthCompare(2) !=0) {
            throw new IllegalArgumentException(s"Illegal argument for the 'redefine-segment-id-map' option: '$v'.")
          }
          val redefine = splitVal(0).trim
          val segmentIds = splitVal(1).split(',').map(_.trim)
          segmentIds.map(segmentId => (segmentId, CopybookParser.transformIdentifier(redefine)))
        } else {
          Nil
        }
    }
  }

  /**
    * Parses the list of sergent redefine fields and their children for a hierarchical data.
    * Produces a mapping between redefined fields and their parents.
    * Root level redefines are not added to the mapping since they don't have parents.
    *
    * Example:
    * For
    * {{{
    *   sprak.read
    *     .option("segment-children:0", "COMPANY => DEPT,CUSTOMER")
    *     .option("segment-children:1", "DEPT => EMPLOYEE,OFFICE")
    * }}}
    *
    * The corresponding mapping will be:
    *
    * {{{
    *    "DEPT" -> "COMPANY"
    *    "CUSTOMER" -> "COMPANY"
    *    "EMPLOYEE" -> "DEPT"
    *    "OFFICE" -> "DEPT"
    * }}}
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns a mapping between redefined fields and their parents
    */
  @throws(classOf[IllegalArgumentException])
  def getSegmentRedefineParents(params: Parameters): Map[String, String] = {
    val segmentRedefineParents = new mutable.HashMap[String, String]
    params.getMap.foreach({
      case (key, value) =>
        val keyNoCase = key.toLowerCase
        if (keyNoCase.startsWith("segment-children") ||
          keyNoCase.startsWith("segment_children")) {
          params.markUsed(key)
          val splitVal = value.split("\\=\\>")
          if (splitVal.lengthCompare(2) != 0) {
            throw new IllegalArgumentException(s"Illegal argument for the 'segment-children' option: '$value'.")
          }
          val redefine = CopybookParser.transformIdentifier(splitVal(0).trim)
          val children = splitVal(1).split(',').map(field => CopybookParser.transformIdentifier(field.trim))
          children.foreach(child => {
            val duplicateOpt = segmentRedefineParents.find {
              case (k, v) => k == child & v != redefine
            }
            duplicateOpt match {
              case Some((k, v)) =>
                throw new IllegalArgumentException(s"Duplicate child '$child' for parents $v and $redefine " +
                  s"specified for 'segment-children' option.")
              case _ =>
                segmentRedefineParents.put(child, redefine)
            }
          })
        } else {
          Nil
        }
    })
    segmentRedefineParents.toMap
  }

  /**
    * Validates if all options passed to 'spark-cobol' are recognized.
    *
    * @param params Parameters provided by spark.read.option(...)
    */
  private def validateSparkCobolOptions(params: Parameters): Unit = {
    val isRecordSequence = params.getOrElse(PARAM_IS_XCOM, "false").toBoolean ||
      params.getOrElse(PARAM_IS_RECORD_SEQUENCE, "false").toBoolean ||
      params.getOrElse(PARAM_VARIABLE_SIZE_OCCURS, "false").toBoolean ||
      params.contains(PARAM_FILE_START_OFFSET) ||
      params.contains(PARAM_FILE_END_OFFSET) ||
      params.contains(PARAM_RECORD_LENGTH)

    val isPedantic = params.getOrElse(PARAM_PEDANTIC, "false").toBoolean
    val keysPassed = params.getMap.keys.toSeq
    val unusedKeys = keysPassed.flatMap(key => {
      if (params.isKeyUsed(key)) {
        None
      } else {
        Some(key)
      }
    })
    val segmentRedefineParents = getSegmentRedefineParents(params)
    if (segmentRedefineParents.nonEmpty) {
      val segmentIdLevels = parseSegmentLevels(params)
      if (segmentIdLevels.nonEmpty) {
        throw new IllegalArgumentException(s"Options 'segment-children:*' cannot be used with 'segment_id_level*' or 'segment_id_root' " +
          "since ID fields generation is not supported for hierarchical records reader.")
      }
    }
    if (!isRecordSequence && params.contains(PARAM_INPUT_FILE_COLUMN)) {
      val recordSequenceCondition = s"one of this holds: '$PARAM_IS_RECORD_SEQUENCE' = true or '$PARAM_VARIABLE_SIZE_OCCURS' = true" +
        s" or one of these options is set: '$PARAM_RECORD_LENGTH', '$PARAM_FILE_START_OFFSET', '$PARAM_FILE_END_OFFSET'"
      throw new IllegalArgumentException(s"Option '$PARAM_INPUT_FILE_COLUMN' is supported only when $recordSequenceCondition")
    }
    if (unusedKeys.nonEmpty) {
      val unusedKeyStr = unusedKeys.mkString(",")
      val msg = s"Redundant or unrecognized option(s) to 'spark-cobol': $unusedKeyStr."
      if (isPedantic) {
        throw new IllegalArgumentException(msg)
      } else {
        logger.error(msg)
      }
    }
  }

}