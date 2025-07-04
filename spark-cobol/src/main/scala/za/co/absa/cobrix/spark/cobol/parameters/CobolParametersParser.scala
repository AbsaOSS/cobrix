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

package za.co.absa.cobrix.spark.cobol.parameters

import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.antlr.ParserJson
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy.DebugFieldsPolicy
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.policies._
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat._
import za.co.absa.cobrix.cobol.reader.parameters._
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

/**
  * This class provides methods for parsing the parameters set as Spark options.
  */
object CobolParametersParser extends Logging {

  val SHORT_NAME                      = "cobol"
  val PARAM_COPYBOOK_PATH             = "copybook"
  val PARAM_MULTI_COPYBOOK_PATH       = "copybooks"
  val PARAM_COPYBOOK_CONTENTS         = "copybook_contents"
  val PARAM_SOURCE_PATH               = "path"
  val PARAM_SOURCE_PATHS              = "data_paths"
  val PARAM_SOURCE_PATHS_LEGACY       = "paths"
  val PARAM_ENCODING                  = "encoding"
  val PARAM_PEDANTIC                  = "pedantic"

  // Record format options
  val PARAM_RECORD_FORMAT             = "record_format"
  val PARAM_RECORD_LENGTH             = "record_length"
  val PARAM_MINIMUM_RECORD_LENGTH     = "minimum_record_length"
  val PARAM_MAXIMUM_RECORD_LENGTH     = "maximum_record_length"
  val PARAM_IS_RECORD_SEQUENCE        = "is_record_sequence"
  val PARAM_RECORD_LENGTH_FIELD       = "record_length_field"
  val PARAM_RECORD_LENGTH_MAP         = "record_length_map"
  val PARAM_RECORD_START_OFFSET       = "record_start_offset"
  val PARAM_RECORD_END_OFFSET         = "record_end_offset"
  val PARAM_FILE_START_OFFSET         = "file_start_offset"
  val PARAM_FILE_END_OFFSET           = "file_end_offset"
  val PARAM_IS_XCOM                   = "is_xcom"
  val PARAM_IS_TEXT                   = "is_text"

  // Schema transformation parameters
  val PARAM_GENERATE_RECORD_ID        = "generate_record_id"
  val PARAM_GENERATE_RECORD_BYTES     = "generate_record_bytes"
  val PARAM_SCHEMA_RETENTION_POLICY   = "schema_retention_policy"
  val PARAM_GROUP_FILLERS             = "drop_group_fillers"
  val PARAM_VALUE_FILLERS             = "drop_value_fillers"
  val PARAM_FILLER_NAMING_POLICY      = "filler_naming_policy"
  val PARAM_STRICT_INTEGRAL_PRECISION = "strict_integral_precision"
  val PARAM_DISPLAY_PIC_ALWAYS_STRING = "display_pic_always_string"

  val PARAM_GROUP_NOT_TERMINALS       = "non_terminals"
  val PARAM_OCCURS_MAPPINGS           = "occurs_mappings"
  val PARAM_DEBUG                     = "debug"
  val PARAM_METADATA                  = "metadata"

  // General parsing parameters
  val PARAM_TRUNCATE_COMMENTS         = "truncate_comments"
  val PARAM_COMMENTS_LBOUND           = "comments_lbound"
  val PARAM_COMMENTS_UBOUND           = "comments_ubound"

  // Data parsing parameters
  val PARAM_STRING_TRIMMING_POLICY    = "string_trimming_policy"
  val PARAM_EBCDIC_CODE_PAGE          = "ebcdic_code_page"
  val PARAM_EBCDIC_CODE_PAGE_CLASS    = "ebcdic_code_page_class"
  val PARAM_ASCII_CHARSET             = "ascii_charset"
  val PARAM_IS_UTF16_BIG_ENDIAN       = "is_utf16_big_endian"
  val PARAM_FLOATING_POINT_FORMAT     = "floating_point_format"
  val PARAM_VARIABLE_SIZE_OCCURS      = "variable_size_occurs"
  val PARAM_STRICT_SIGN_OVERPUNCHING  = "strict_sign_overpunching"
  val PARAM_IMPROVED_NULL_DETECTION   = "improved_null_detection"
  val PARAM_BINARY_AS_HEX             = "binary_as_hex"
  val PARAM_ALLOW_PARTIAL_RECORDS     = "allow_partial_records"
  val PARAM_FIELD_CODE_PAGE_PREFIX    = "field_code_page:"

  // Parameters for multisegment variable length files
  val PARAM_IS_RDW_BIG_ENDIAN         = "is_rdw_big_endian"
  val PARAM_IS_BDW_BIG_ENDIAN         = "is_bdw_big_endian"
  val PARAM_IS_RDW_PART_REC_LENGTH    = "is_rdw_part_of_record_length"
  val PARAM_RDW_ADJUSTMENT            = "rdw_adjustment"
  val PARAM_BDW_ADJUSTMENT            = "bdw_adjustment"
  val PARAM_BLOCK_LENGTH              = "block_length"
  val PARAM_RECORDS_PER_BLOCK         = "records_per_block"
  val PARAM_SEGMENT_FIELD             = "segment_field"
  val PARAM_SEGMENT_ID_ROOT           = "segment_id_root"
  val PARAM_SEGMENT_FILTER            = "segment_filter"
  val PARAM_SEGMENT_ID_LEVEL_PREFIX   = "segment_id_level"
  val PARAM_RECORD_HEADER_PARSER      = "record_header_parser"
  val PARAM_RECORD_EXTRACTOR          = "record_extractor"
  val PARAM_RHP_ADDITIONAL_INFO       = "rhp_additional_info"
  val PARAM_RE_ADDITIONAL_INFO        = "re_additional_info"
  val PARAM_INPUT_FILE_COLUMN         = "with_input_file_name_col"
  val PARAM_SEGMENT_REDEFINE_PREFIX   = "redefine_segment_id_map"
  val PARAM_SEGMENT_REDEFINE_PREFIX_ALT = "redefine-segment-id-map"

  // Indexed multisegment file processing
  val PARAM_ENABLE_INDEXES            = "enable_indexes"
  val PARAM_INPUT_SPLIT_RECORDS       = "input_split_records"
  val PARAM_INPUT_SPLIT_SIZE_MB       = "input_split_size_mb"
  val PARAM_SEGMENT_ID_PREFIX         = "segment_id_prefix"
  val PARAM_OPTIMIZE_ALLOCATION       = "optimize_allocation"
  val PARAM_IMPROVE_LOCALITY          = "improve_locality"

  // Parameters for debugging
  val PARAM_DEBUG_LAYOUT_POSITIONS    = "debug_layout_positions"
  val PARAM_DEBUG_IGNORE_FILE_SIZE    = "debug_ignore_file_size"
  val PARAM_ENABLE_SELF_CHECKS        = "enable_self_checks"

  private def getSchemaRetentionPolicy(params: Parameters): SchemaRetentionPolicy = {
    val schemaRetentionPolicyName = params.getOrElse(PARAM_SCHEMA_RETENTION_POLICY, "collapse_root")
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
        throw new IllegalArgumentException(s"Invalid value '$stringTrimmingPolicyName' for '$PARAM_STRING_TRIMMING_POLICY' option.")
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
        throw new IllegalArgumentException(s"Invalid value '$floatingPointFormatName' for '$PARAM_FLOATING_POINT_FORMAT' option.")
    }
  }

  private def getDebuggingFieldsPolicy(recordFormat: RecordFormat, params: Parameters): DebugFieldsPolicy = {
    val debugFieldsPolicyName = params.getOrElse(PARAM_DEBUG, "false")
    val isText = params.getOrElse(PARAM_IS_TEXT, "false").toBoolean
    val debugFieldsPolicy = DebugFieldsPolicy.withNameOpt(debugFieldsPolicyName)

    val policy = debugFieldsPolicy match {
      case Some(p) =>
        p
      case None =>
        throw new IllegalArgumentException(s"Invalid value '$debugFieldsPolicyName' for '$PARAM_DEBUG' option. " +
          "Allowed one of: 'true' = 'hex', 'raw', 'binary', 'string' (ASCII only), 'false' = 'none'. ")
    }
    if (policy == DebugFieldsPolicy.StringValue && recordFormat != RecordFormat.AsciiText && recordFormat != RecordFormat.CobrixAsciiText && !isText) {
      throw new IllegalArgumentException(s"Invalid value '$debugFieldsPolicyName' for '$PARAM_DEBUG' option. " +
        "Allowed only for record_format = 'D' or 'D2'.")
    }

    policy
  }

  def parse(params: Parameters): CobolParameters = {
    val schemaRetentionPolicy = getSchemaRetentionPolicy(params)
    val stringTrimmingPolicy = getStringTrimmingPolicy(params)
    val ebcdicCodePageName = params.getOrElse(PARAM_EBCDIC_CODE_PAGE, "common")
    val ebcdicCodePageClass = params.get(PARAM_EBCDIC_CODE_PAGE_CLASS)
    val asciiCharset = params.get(PARAM_ASCII_CHARSET)

    val recordFormatDefined = getRecordFormat(params)

    val isEbcdic = getIsEbcdic(params, recordFormatDefined)

    val pathsParam = getParameter(PARAM_SOURCE_PATHS, params).orElse(getParameter(PARAM_SOURCE_PATHS_LEGACY, params))

    val paths = pathsParam.map(_.split(',')).getOrElse(Array(getParameter(PARAM_SOURCE_PATH, params).getOrElse("")))

    val variableLengthParams = parseVariableLengthParameters(params, recordFormatDefined)

    val recordFormat = if (recordFormatDefined == AsciiText && variableLengthParams.nonEmpty) {
      logger.info("According to options passed, the custom ASCII parser (record_format = D2) is used.")
      CobrixAsciiText
    } else {
      recordFormatDefined
    }

    val cobolParameters = CobolParameters(
      getParameter(PARAM_COPYBOOK_PATH, params),
      params.getOrElse(PARAM_MULTI_COPYBOOK_PATH, "").split(','),
      getParameter(PARAM_COPYBOOK_CONTENTS, params),
      paths,
      recordFormat,
      recordFormat == CobrixAsciiText || recordFormat == AsciiText || params.getOrElse(PARAM_IS_TEXT, "false").toBoolean,
      isEbcdic,
      ebcdicCodePageName,
      ebcdicCodePageClass,
      asciiCharset,
      getFieldCodepageMap(params),
      params.getOrElse(PARAM_IS_UTF16_BIG_ENDIAN, "true").toBoolean,
      getFloatingPointFormat(params),
      params.getOrElse(PARAM_RECORD_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_RECORD_END_OFFSET, "0").toInt,
      params.get(PARAM_RECORD_LENGTH).map(_.toInt),
      params.get(PARAM_MINIMUM_RECORD_LENGTH).map(_.toInt),
      params.get(PARAM_MAXIMUM_RECORD_LENGTH).map(_.toInt),
      variableLengthParams,
      params.getOrElse(PARAM_VARIABLE_SIZE_OCCURS, "false").toBoolean,
      params.getOrElse(PARAM_GENERATE_RECORD_BYTES, "false").toBoolean,
      schemaRetentionPolicy,
      stringTrimmingPolicy,
      params.getOrElse(PARAM_DISPLAY_PIC_ALWAYS_STRING, "false").toBoolean,
      params.getOrElse(PARAM_ALLOW_PARTIAL_RECORDS, "false").toBoolean,
      parseMultisegmentParameters(params),
      parseCommentTruncationPolicy(params),
      params.getOrElse(PARAM_STRICT_SIGN_OVERPUNCHING, "true").toBoolean,
      params.getOrElse(PARAM_IMPROVED_NULL_DETECTION, "true").toBoolean,
      params.getOrElse(PARAM_STRICT_INTEGRAL_PRECISION, "false").toBoolean,
      params.getOrElse(PARAM_BINARY_AS_HEX, "false").toBoolean,
      params.getOrElse(PARAM_GROUP_FILLERS, "false").toBoolean,
      params.getOrElse(PARAM_VALUE_FILLERS, "true").toBoolean,
      FillerNamingPolicy(params.getOrElse(PARAM_FILLER_NAMING_POLICY, "sequence_numbers")),
      params.getOrElse(PARAM_GROUP_NOT_TERMINALS, "").split(','),
      getOccursMappings(params.getOrElse(PARAM_OCCURS_MAPPINGS, "{}")),
      getDebuggingFieldsPolicy(recordFormat, params),
      params.getOrElse(PARAM_DEBUG_IGNORE_FILE_SIZE, "false").toBoolean,
      params.getOrElse(PARAM_DEBUG_LAYOUT_POSITIONS, "false").toBoolean,
      params.getOrElse(PARAM_ENABLE_SELF_CHECKS, "false").toBoolean,
      MetadataPolicy(params.getOrElse(PARAM_METADATA, "basic"))
      )
    validateSparkCobolOptions(params, recordFormat)
    cobolParameters
  }

  def getIsEbcdic(params: Parameters, recordFormat: RecordFormat): Boolean = {
    val encoding = params.getOrElse(PARAM_ENCODING, "")
    val isEbcdic = {
      if (encoding.isEmpty) {
        if (recordFormat == AsciiText || recordFormat == CobrixAsciiText) {
          false
        } else {
          true
        }
      } else if (encoding.compareToIgnoreCase("ebcdic") == 0) {
        if (recordFormat == AsciiText || recordFormat == CobrixAsciiText) {
          logger.warn(s"$PARAM_RECORD_FORMAT = D/D2/T and $PARAM_ENCODING = $encoding are used together. Most of the time the encoding should be ASCII for text files.")
        }
        true
      } else {
        if (encoding.compareToIgnoreCase("ascii") == 0) {
          false
        } else {
          throw new IllegalArgumentException(s"Invalid value '$encoding' for '$PARAM_ENCODING' option. Should be either 'EBCDIC' or 'ASCII'.")
        }
      }
    }
    isEbcdic
  }

  def getFieldCodepageMap(parameters: Parameters): Map[String, String] = {
    val entries = parameters
      .getMap
      .keys
      .filter(_.startsWith(PARAM_FIELD_CODE_PAGE_PREFIX))

    entries.flatMap { key =>
      val idx = key.indexOf(':')
      if (idx >= 0) {
        val codePage = key.substring(idx + 1).trim
        val fieldsStr = parameters.get(key).get

        if (codePage.isEmpty) {
          logger.warn(s"Incorrect code page name for the option '$key' -> '$fieldsStr'.")
          Array.empty[(String, String)]
        } else {
          val fields = fieldsStr.split(',').map(fld =>
                                                  CopybookParser.transformIdentifier(fld.trim.toLowerCase)
                                                ).filter(_.nonEmpty)

          fields.map(field => (field, codePage))
        }
      } else {
        Array.empty[(String, String)]
      }
    }.toMap
  }

  def getReaderProperties(parameters: CobolParameters, defaultBlockSize: Option[Int]): ReaderParameters = {
    val varLenParams: VariableLengthParameters = parameters.variableLengthParams
      .getOrElse(
        VariableLengthParameters(isRecordSequence = false,
                                 None,
                                 isRdwBigEndian = false,
                                 isRdwPartRecLength = false,
                                 rdwAdjustment = 0,
                                 recordHeaderParser = None,
                                 recordExtractor = None,
                                 rhpAdditionalInfo = None,
                                 reAdditionalInfo = "",
                                 recordLengthField = "",
                                 Map.empty,
                                 fileStartOffset = 0,
                                 fileEndOffset = 0,
                                 generateRecordId = false,
                                 isUsingIndex = false,
                                 inputSplitRecords = None,
                                 inputSplitSizeMB = None,
                                 improveLocality = false,
                                 optimizeAllocation = false,
                                 inputFileNameColumn = "",
                                 parameters.occursMappings)
        )

    val recordLengthField = if (varLenParams.recordLengthField.nonEmpty)
      Some(varLenParams.recordLengthField)
    else
      None

    ReaderParameters(
      recordFormat = parameters.recordFormat,
      isEbcdic = parameters.isEbcdic,
      isText = parameters.isText,
      ebcdicCodePage = parameters.ebcdicCodePage,
      ebcdicCodePageClass = parameters.ebcdicCodePageClass,
      asciiCharset = parameters.asciiCharset,
      fieldCodePage = parameters.fieldCodePage,
      isUtf16BigEndian = parameters.isUtf16BigEndian,
      floatingPointFormat = parameters.floatingPointFormat,
      variableSizeOccurs = parameters.variableSizeOccurs,
      recordLength = parameters.recordLength,
      minimumRecordLength = parameters.minimumRecordLength.getOrElse(1),
      maximumRecordLength = parameters.maximumRecordLength.getOrElse(Int.MaxValue),
      lengthFieldExpression = recordLengthField,
      lengthFieldMap = varLenParams.recordLengthMap,
      isRecordSequence = varLenParams.isRecordSequence,
      bdw = varLenParams.bdw,
      isRdwBigEndian = varLenParams.isRdwBigEndian,
      isRdwPartRecLength = varLenParams.isRdwPartRecLength,
      rdwAdjustment = varLenParams.rdwAdjustment,
      isIndexGenerationNeeded = varLenParams.isUsingIndex,
      inputSplitRecords = varLenParams.inputSplitRecords,
      inputSplitSizeMB = varLenParams.inputSplitSizeMB,
      hdfsDefaultBlockSize = defaultBlockSize,
      startOffset = parameters.recordStartOffset,
      endOffset = parameters.recordEndOffset,
      fileStartOffset = varLenParams.fileStartOffset,
      fileEndOffset = varLenParams.fileEndOffset,
      generateRecordId = varLenParams.generateRecordId,
      generateRecordBytes = parameters.generateRecordBytes,
      schemaPolicy = parameters.schemaRetentionPolicy,
      stringTrimmingPolicy = parameters.stringTrimmingPolicy,
      isDisplayAlwaysString = parameters.isDisplayAlwaysString,
      allowPartialRecords = parameters.allowPartialRecords,
      parameters.multisegmentParams,
      parameters.commentPolicy,
      parameters.strictSignOverpunch,
      parameters.improvedNullDetection,
      parameters.strictIntegralPrecision,
      parameters.decodeBinaryAsHex,
      parameters.dropGroupFillers,
      parameters.dropValueFillers,
      parameters.enableSelfChecks,
      parameters.fillerNamingPolicy,
      parameters.nonTerminals,
      parameters.occursMappings,
      parameters.debugFieldsPolicy,
      varLenParams.recordHeaderParser,
      varLenParams.recordExtractor,
      varLenParams.rhpAdditionalInfo,
      varLenParams.reAdditionalInfo,
      varLenParams.inputFileNameColumn,
      parameters.metadataPolicy
      )
  }

  private def parseVariableLengthParameters(params: Parameters, recordFormat: RecordFormat): Option[VariableLengthParameters] = {
    val recordLengthFieldOpt = params.get(PARAM_RECORD_LENGTH_FIELD)
    val isRecordSequence = Seq(FixedBlock, VariableLength, VariableBlock).contains(recordFormat)
    val isRecordIdGenerationEnabled = params.getOrElse(PARAM_GENERATE_RECORD_ID, "false").toBoolean
    val isSegmentIdGenerationEnabled = params.contains(PARAM_SEGMENT_ID_ROOT) || params.contains(s"${PARAM_SEGMENT_ID_LEVEL_PREFIX}0")
    val fileStartOffset = params.getOrElse(PARAM_FILE_START_OFFSET, "0").toInt
    val fileEndOffset = params.getOrElse(PARAM_FILE_END_OFFSET, "0").toInt
    val varLenOccursEnabled = params.getOrElse(PARAM_VARIABLE_SIZE_OCCURS, "false").toBoolean
    val hasRecordExtractor = params.contains(PARAM_RECORD_EXTRACTOR)
    val isHierarchical = params.getMap.keys.exists(k => k.startsWith("segment-children"))
    val allowPartialRecords = params.getOrElse(PARAM_ALLOW_PARTIAL_RECORDS, "false").toBoolean

    // Fallback to basic Ascii when the format is 'D' or 'T', but no special Cobrix features are enabled
    val basicAscii = !allowPartialRecords && recordFormat == AsciiText
    val variableLengthAscii = (recordFormat == AsciiText && !basicAscii) || recordFormat == CobrixAsciiText
    val nonTextVariableLengthOccurs = varLenOccursEnabled && !basicAscii

    if (params.contains(PARAM_RECORD_LENGTH_FIELD) &&
      (params.contains(PARAM_IS_RECORD_SEQUENCE) || params.contains(PARAM_IS_XCOM))) {
      throw new IllegalArgumentException(s"Option '$PARAM_RECORD_LENGTH_FIELD' cannot be used together with '$PARAM_IS_RECORD_SEQUENCE' or '$PARAM_IS_XCOM'.")
    }

    if (recordLengthFieldOpt.isDefined ||
      isRecordSequence ||
      isRecordIdGenerationEnabled ||
      isSegmentIdGenerationEnabled ||
      fileStartOffset > 0 ||
      fileEndOffset > 0 ||
      hasRecordExtractor ||
      nonTextVariableLengthOccurs ||
      variableLengthAscii ||
      isHierarchical
    ) {
      Some(VariableLengthParameters
      (
        isRecordSequence,
        parseBdw(params, recordFormat),
        params.getOrElse(PARAM_IS_RDW_BIG_ENDIAN, "false").toBoolean,
        params.getOrElse(PARAM_IS_RDW_PART_REC_LENGTH, "false").toBoolean,
        params.getOrElse(PARAM_RDW_ADJUSTMENT, "0").toInt,
        params.get(PARAM_RECORD_HEADER_PARSER),
        params.get(PARAM_RECORD_EXTRACTOR),
        params.get(PARAM_RHP_ADDITIONAL_INFO),
        params.get(PARAM_RE_ADDITIONAL_INFO).getOrElse(""),
        recordLengthFieldOpt.getOrElse(""),
        getRecordLengthMappings(params.getOrElse(PARAM_RECORD_LENGTH_MAP, "{}")),
        fileStartOffset,
        fileEndOffset,
        isRecordIdGenerationEnabled,
        params.getOrElse(PARAM_ENABLE_INDEXES, "true").toBoolean,
        params.get(PARAM_INPUT_SPLIT_RECORDS).map(v => v.toInt),
        params.get(PARAM_INPUT_SPLIT_SIZE_MB).map(v => v.toInt),
        params.getOrElse(PARAM_IMPROVE_LOCALITY, "true").toBoolean,
        params.getOrElse(PARAM_OPTIMIZE_ALLOCATION, "false").toBoolean,
        params.getOrElse(PARAM_INPUT_FILE_COLUMN, ""),
        getOccursMappings(params.getOrElse(PARAM_OCCURS_MAPPINGS, "{}"))
      ))
    } else {
      None
    }
  }

  private def parseBdw(params: Parameters, recordFormat: RecordFormat): Option[Bdw] = {
    if (recordFormat == FixedBlock || recordFormat == VariableBlock) {
      val bdw = Bdw(
        params.getOrElse(PARAM_IS_BDW_BIG_ENDIAN, "false").toBoolean,
        params.getOrElse(PARAM_BDW_ADJUSTMENT, "0").toInt,
        params.get(PARAM_BLOCK_LENGTH).map(_.toInt),
        params.get(PARAM_RECORDS_PER_BLOCK).map(_.toInt)
      )
      if (bdw.blockLength.nonEmpty && bdw.recordsPerBlock.nonEmpty) {
        throw new IllegalArgumentException(s"Options '$PARAM_BLOCK_LENGTH' and '$PARAM_RECORDS_PER_BLOCK' cannot be used together.")
      }
      if (recordFormat == VariableBlock && bdw.blockLength.nonEmpty) {
        logger.warn(s"Option '$PARAM_BLOCK_LENGTH' is ignored for record format: VB")
      }
      if (recordFormat == FixedBlock && bdw.recordsPerBlock.nonEmpty) {
        logger.warn(s"Option '$PARAM_RECORDS_PER_BLOCK' is ignored for record format: F")
      }
      Some(bdw)
    } else {
      None
    }
  }

  private def getRecordFormat(params: Parameters): RecordFormat = {
    if (params.contains(PARAM_RECORD_FORMAT)) {
      val recordFormatStr = params(PARAM_RECORD_FORMAT)

      RecordFormat.withNameOpt(recordFormatStr).getOrElse(throw new IllegalArgumentException(s"Unknown record format: $recordFormatStr"))
    } else {
      val hasRdw = params.getOrElse(PARAM_IS_XCOM, params.getOrElse(PARAM_IS_RECORD_SEQUENCE, "false")).toBoolean

      val q = "\""

      if (params.contains(PARAM_IS_XCOM)) {
        logger.warn(s"Option '$PARAM_IS_XCOM' is deprecated. Use .option($q$PARAM_RECORD_FORMAT$q, ${q}V$q)")
      }

      if (params.contains(PARAM_IS_RECORD_SEQUENCE)) {
        logger.warn(s"Option '$PARAM_IS_RECORD_SEQUENCE' is deprecated. Use .option($q$PARAM_RECORD_FORMAT$q, ${q}V$q)")
      }

      if (hasRdw) {
        VariableLength
      } else {
        if (params.getOrElse(PARAM_IS_TEXT, "false").toBoolean) {
          AsciiText
        } else {
          FixedLength
        }
      }
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
      val multiseg = MultisegmentParameters(
        params(PARAM_SEGMENT_FIELD),
        params.get(PARAM_SEGMENT_FILTER).map(_.split(',')),
        levels,
        params.getOrElse(PARAM_SEGMENT_ID_PREFIX, ""),
        getSegmentIdRedefineMapping(params),
        getSegmentRedefineParents(params)
      )

      val segmentIds = ParameterParsingUtils.splitSegmentIds(multiseg.segmentLevelIds)
      ParameterParsingUtils.validateSegmentIds(segmentIds)

      Some(multiseg)
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
      } else if (i == 0 && params.contains(PARAM_SEGMENT_ID_ROOT)) {
        levels += params(PARAM_SEGMENT_ID_ROOT)
      } else {
        return levels.toList
      }
      i = i + 1
    }
    levels.toList
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
        if (keyNoCase.startsWith(PARAM_SEGMENT_REDEFINE_PREFIX) ||
          keyNoCase.startsWith(PARAM_SEGMENT_REDEFINE_PREFIX_ALT)) {
          params.markUsed(k)
          val splitVal = v.split("\\=\\>")
          if (splitVal.lengthCompare(2) != 0) {
            throw new IllegalArgumentException(s"Illegal argument for the '$PARAM_SEGMENT_REDEFINE_PREFIX_ALT' option: '$v'.")
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
    *   spark.read
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
  private def validateSparkCobolOptions(params: Parameters, recordFormat: RecordFormat): Unit = {
    val isRecordSequence = params.getOrElse(PARAM_IS_XCOM, "false").toBoolean ||
      params.getOrElse(PARAM_IS_RECORD_SEQUENCE, "false").toBoolean ||
      params.contains(PARAM_FILE_START_OFFSET) ||
      params.contains(PARAM_FILE_END_OFFSET) ||
      params.contains(PARAM_RECORD_LENGTH_FIELD) ||
      recordFormat == VariableLength ||
      recordFormat == VariableBlock

    if (params.contains(PARAM_RECORD_FORMAT)) {
      if (params.contains(PARAM_IS_XCOM)) {
        throw new IllegalArgumentException(s"Option '$PARAM_RECORD_FORMAT' and '$PARAM_IS_XCOM' cannot be used together. The use of $PARAM_RECORD_FORMAT is preferable.")
      }

      if (params.contains(PARAM_IS_RECORD_SEQUENCE)) {
        throw new IllegalArgumentException(s"Option '$PARAM_RECORD_FORMAT' and '$PARAM_IS_RECORD_SEQUENCE' cannot be used together. The use of $PARAM_RECORD_FORMAT is preferable.")
      }

      if (params.contains(PARAM_IS_TEXT)) {
        throw new IllegalArgumentException(s"Option '$PARAM_RECORD_FORMAT' and '$PARAM_IS_TEXT' cannot be used together. The use of $PARAM_RECORD_FORMAT is preferable.")
      }
    }

    val hasRecordExtractor = params.contains(PARAM_RECORD_EXTRACTOR)

    val isText = params.getOrElse(PARAM_IS_TEXT, "false").toBoolean

    val isPedantic = params.getOrElse(PARAM_PEDANTIC, "false").toBoolean
    val keysPassed = params.getMap.keys.toSeq
    val unusedKeys = keysPassed.flatMap(key => {
      if (params.isKeyUsed(key)) {
        None
      } else {
        Some(key)
      }
    })

    if (hasRecordExtractor) {
      val incorrectParameters = new ListBuffer[String]
      if (isText) {
        incorrectParameters += PARAM_IS_TEXT
      }
      if (params.contains(PARAM_RECORD_LENGTH)) {
        incorrectParameters += PARAM_RECORD_LENGTH
      }
      if (params.contains(PARAM_IS_RECORD_SEQUENCE)) {
        incorrectParameters += PARAM_IS_RECORD_SEQUENCE
      }
      if (params.contains(PARAM_IS_XCOM)) {
        incorrectParameters += PARAM_IS_XCOM
      }
      if (params.contains(PARAM_IS_RDW_BIG_ENDIAN)) {
        incorrectParameters += PARAM_IS_RDW_BIG_ENDIAN
      }
      if (params.contains(PARAM_IS_RDW_PART_REC_LENGTH)) {
        incorrectParameters += PARAM_IS_RDW_PART_REC_LENGTH
      }
      if (params.contains(PARAM_RDW_ADJUSTMENT)) {
        incorrectParameters += PARAM_RDW_ADJUSTMENT
      }
      if (params.contains(PARAM_RECORD_LENGTH_FIELD)) {
        incorrectParameters += PARAM_RECORD_LENGTH_FIELD
      }
      if (params.contains(PARAM_RECORD_HEADER_PARSER)) {
        incorrectParameters += PARAM_RECORD_HEADER_PARSER
      }
      if (params.contains(PARAM_RHP_ADDITIONAL_INFO)) {
        incorrectParameters += PARAM_RHP_ADDITIONAL_INFO
      }

      if (incorrectParameters.nonEmpty) {
        throw new IllegalArgumentException(s"Option '$PARAM_RECORD_EXTRACTOR' and '${incorrectParameters.mkString(", ")}' cannot be used together.")
      }
    }

    if (params.contains(PARAM_RECORD_LENGTH)) {
      val incorrectParameters = new ListBuffer[String]
      if (isText) {
        incorrectParameters += PARAM_IS_TEXT
      }
      if (params.contains(PARAM_IS_RECORD_SEQUENCE)) {
        incorrectParameters += PARAM_IS_RECORD_SEQUENCE
      }
      if (params.contains(PARAM_IS_XCOM)) {
        incorrectParameters += PARAM_IS_XCOM
      }
      if (params.contains(PARAM_MINIMUM_RECORD_LENGTH)) {
        incorrectParameters += PARAM_MINIMUM_RECORD_LENGTH
      }
      if (params.contains(PARAM_MAXIMUM_RECORD_LENGTH)) {
        incorrectParameters += PARAM_MAXIMUM_RECORD_LENGTH
      }
      if (params.contains(PARAM_IS_RDW_BIG_ENDIAN)) {
        incorrectParameters += PARAM_IS_RDW_BIG_ENDIAN
      }
      if (params.contains(PARAM_IS_RDW_PART_REC_LENGTH)) {
        incorrectParameters += PARAM_IS_RDW_PART_REC_LENGTH
      }
      if (params.contains(PARAM_RDW_ADJUSTMENT)) {
        incorrectParameters += PARAM_RDW_ADJUSTMENT
      }
      if (params.contains(PARAM_RECORD_LENGTH_FIELD)) {
        incorrectParameters += PARAM_RECORD_LENGTH_FIELD
      }
      if (params.contains(PARAM_RECORD_HEADER_PARSER)) {
        incorrectParameters += PARAM_RECORD_HEADER_PARSER
      }
      if (params.contains(PARAM_RHP_ADDITIONAL_INFO)) {
        incorrectParameters += PARAM_RHP_ADDITIONAL_INFO
      }

      if (incorrectParameters.nonEmpty) {
        throw new IllegalArgumentException(s"Option '$PARAM_RECORD_LENGTH' and '${incorrectParameters.mkString(", ")}' cannot be used together.")
      }
    }

    val segmentRedefineParents = getSegmentRedefineParents(params)
    if (segmentRedefineParents.nonEmpty) {
      val segmentIdLevels = parseSegmentLevels(params)
      if (segmentIdLevels.nonEmpty) {
        throw new IllegalArgumentException(s"Options 'segment-children:*' cannot be used with 'segment_id_level*' or 'segment_id_root' " +
          "since ID fields generation is not supported for hierarchical records reader.")
      }
      if (params.contains(PARAM_GENERATE_RECORD_BYTES)) {
        throw new IllegalArgumentException(s"Option '$PARAM_GENERATE_RECORD_BYTES' cannot be used with 'segment-children:*' " +
          "since hierarchical records are composites of more than one raw record.")
      }
    }
    if (!isRecordSequence && recordFormat != AsciiText && recordFormat != CobrixAsciiText && params.contains(PARAM_INPUT_FILE_COLUMN)) {
      val recordSequenceCondition = s"one of this holds: '$PARAM_RECORD_FORMAT' = V or '$PARAM_RECORD_FORMAT' = VB or '$PARAM_IS_RECORD_SEQUENCE' = true" +
        s" or one of these options is set: '$PARAM_RECORD_LENGTH_FIELD', '$PARAM_FILE_START_OFFSET', '$PARAM_FILE_END_OFFSET' or " +
        "a custom record extractor is specified. If you see the error, please use Spark standard function 'input_file_name()' instead."
      throw new IllegalArgumentException(s"Option '$PARAM_INPUT_FILE_COLUMN' is supported only when $recordSequenceCondition")
    }

    if (isText) {
      val incorrectParameters = new ListBuffer[String]
      if (params.contains(PARAM_IS_RDW_BIG_ENDIAN)) {
        incorrectParameters += PARAM_IS_RDW_BIG_ENDIAN
      }
      if (params.contains(PARAM_IS_RDW_PART_REC_LENGTH)) {
        incorrectParameters += PARAM_IS_RDW_PART_REC_LENGTH
      }
      if (params.contains(PARAM_RDW_ADJUSTMENT)) {
        incorrectParameters += PARAM_RDW_ADJUSTMENT
      }
      if (params.contains(PARAM_IS_XCOM)) {
        incorrectParameters += PARAM_IS_XCOM
      }
      if (params.contains(PARAM_RECORD_LENGTH)) {
        incorrectParameters += PARAM_RECORD_LENGTH
      }
      if (params.contains(PARAM_RECORD_HEADER_PARSER)) {
        incorrectParameters += PARAM_RECORD_HEADER_PARSER
      }
      if (params.contains(PARAM_RHP_ADDITIONAL_INFO)) {
        incorrectParameters += PARAM_RHP_ADDITIONAL_INFO
      }

      if (incorrectParameters.nonEmpty) {
        throw new IllegalArgumentException(s"Option '$PARAM_IS_TEXT' and ${incorrectParameters.mkString(", ")} cannot be used together.")
      }
    }

    if (params.contains(PARAM_ENCODING) && params(PARAM_ENCODING).toLowerCase() == "ebcdic") {
      if (params.contains(PARAM_ASCII_CHARSET)) {
        throw new IllegalArgumentException(s"Option '$PARAM_ASCII_CHARSET' cannot be used when '$PARAM_ENCODING = ebcdic'.")
      }
    }

    if (params.contains(PARAM_ENCODING) && params(PARAM_ENCODING).toLowerCase() == "ascii") {
      if (params.contains(PARAM_EBCDIC_CODE_PAGE)) {
        throw new IllegalArgumentException(s"Option '$PARAM_EBCDIC_CODE_PAGE' cannot be used when '$PARAM_ENCODING = ascii'.")
      }
    }

    if (params.contains(PARAM_MINIMUM_RECORD_LENGTH) && params(PARAM_MINIMUM_RECORD_LENGTH).toInt < 1)
      throw new IllegalArgumentException(s"The option '$PARAM_MINIMUM_RECORD_LENGTH' should be at least 1.")

    if (params.contains(PARAM_MAXIMUM_RECORD_LENGTH) && params(PARAM_MAXIMUM_RECORD_LENGTH).toInt < 1)
      throw new IllegalArgumentException(s"The option '$PARAM_MAXIMUM_RECORD_LENGTH' should be at least 1.")

    if (params.contains(PARAM_MINIMUM_RECORD_LENGTH) && params.contains(PARAM_MAXIMUM_RECORD_LENGTH) &&
      params(PARAM_MINIMUM_RECORD_LENGTH).toInt > params(PARAM_MAXIMUM_RECORD_LENGTH).toInt) {
      val min = params(PARAM_MINIMUM_RECORD_LENGTH).toInt
      val max = params(PARAM_MAXIMUM_RECORD_LENGTH).toInt
      throw new IllegalArgumentException(s"'$PARAM_MINIMUM_RECORD_LENGTH' ($min) should be >= '$PARAM_MAXIMUM_RECORD_LENGTH' ($max).")
    }

    if (params.contains(PARAM_DISPLAY_PIC_ALWAYS_STRING) && params(PARAM_DISPLAY_PIC_ALWAYS_STRING).toBoolean &&
      params.contains(PARAM_STRICT_INTEGRAL_PRECISION) && params(PARAM_STRICT_INTEGRAL_PRECISION).toBoolean)
      throw new IllegalArgumentException(s"Options '$PARAM_DISPLAY_PIC_ALWAYS_STRING' and '$PARAM_STRICT_INTEGRAL_PRECISION' cannot be used together.")

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

  /**
    * Parses the options for the record length mappings.
    *
    * @param recordLengthMapJson Parameters provided by spark.read.option(...)
    * @return Returns a mapping from the record length field values to the actual record length
    */
  @throws(classOf[IllegalArgumentException])
  def getRecordLengthMappings(recordLengthMapJson: String): Map[String, Int] = {
    val parser = new ParserJson()
    val json = try {
      parser.parseMap(recordLengthMapJson)
    } catch {
      case NonFatal(ex) => throw new IllegalArgumentException(s"Unable to parse record length mapping JSON.", ex)
    }

    json.toSeq // Converting to a non-lazy sequence first. If .mapValues() is used the map stays lazy and errors pop up later
      .map { case (k, v) =>
        val vInt = v match {
          case num: Int => num
          case num: Long => num.toInt
          case str: String =>
            try {
              str.toInt
            } catch {
              case NonFatal(ex) => throw new IllegalArgumentException(s"Unsupported record length value: '$str'. Please, use numeric values only.", ex)
            }
          case any => throw new IllegalArgumentException(s"Unsupported record length value: '$any'. Please, use numeric values only.")
        }
        (k, vInt)
      }.toMap[String, Int]
  }

  /**
    * Parses the options for the occurs mappings.
    *
    * @param params Parameters provided by spark.read.option(...)
    * @return Returns a mapping for OCCURS fields
    */
  @throws(classOf[IllegalArgumentException])
  def getOccursMappings(params: String): Map[String, Map[String, Int]] = {
    val parser = new ParserJson()
    val parsedParams = parser.parseMap(params)
    parsedParams.map(kv => kv._1 -> kv._2.asInstanceOf[Map[String, Any]].map(x => x._1 -> x._2.asInstanceOf[Int]))
  }
}
