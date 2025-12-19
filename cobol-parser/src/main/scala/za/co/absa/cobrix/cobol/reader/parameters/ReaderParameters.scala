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

package za.co.absa.cobrix.cobol.reader.parameters

import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy.DebugFieldsPolicy
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.policies._
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat.FixedLength
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * These are properties for customizing mainframe binary data reader.
  * @param recordFormat            Record format
  * @param isEbcdic                If true the input data file encoding is EBCDIC, otherwise it is ASCII
  * @param isText                  If true line ending characters will be used (LF / CRLF) as the record separator
  * @param ebcdicCodePage          Specifies what code page to use for EBCDIC to ASCII/Unicode conversions
  * @param ebcdicCodePageClass     An optional custom code page conversion class provided by a user
  * @param asciiCharset            A charset for ASCII data
  * @param fieldCodePage           Specifies a mapping between a field name and the code page
  * @param isUtf16BigEndian        If true UTF-16 strings are considered big-endian.
  * @param floatingPointFormat     A format of floating-point numbers
  * @param variableSizeOccurs      If true, OCCURS DEPENDING ON data size will depend on the number of elements
  * @param recordLength            Specifies the length of the record disregarding the copybook record size. Implied the file has fixed record length.
  * @param minimumRecordLength     Minium record length for which the record is considered valid.
  * @param maximumRecordLength     Maximum record length for which the record is considered valid.
  * @param lengthFieldExpression   A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param lengthFieldMap          Mapping between record length field values to actual length. The field name should be specified in lengthFieldExpression.
  * @param isRecordSequence        Does input files have 4 byte record length headers
  * @param bdw                     Block descriptor word (if specified), for FB and VB record formats
  * @param isRdwPartRecLength      Does RDW count itself as part of record length itself
  * @param rdwAdjustment           Controls a mismatch between RDW and record length
  * @param isIndexGenerationNeeded Is indexing input file before processing is requested
  * @param inputSplitRecords       The number of records to include in each partition. Notice mainframe records may have variable size, inputSplitMB is the recommended option
  * @param inputSplitSizeMB        A partition size to target. In certain circumstances this size may not be exactly that, but the library will do the best effort to target that size
  * @param inputSplitSizeCompressedMB A partition size to target for compressed files.
  * @param hdfsDefaultBlockSize    Default HDFS block size for the HDFS filesystem used. This value is used as the default split size if inputSplitSizeMB is not specified
  * @param startOffset             An offset to the start of the record in each binary data block.
  * @param endOffset               An offset from the end of the record to the end of the binary data block.
  * @param fileStartOffset         A number of bytes to skip at the beginning of each file
  * @param fileEndOffset           A number of bytes to skip at the end of each file
  * @param generateRecordId        If true, a record id field will be prepended to each record.
  * @param generateRecordBytes     Generate 'record_bytes' field containing raw bytes of the original record
  * @param schemaPolicy            Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param stringTrimmingPolicy    Specifies if and how strings should be trimmed when parsed.
  * @param isDisplayAlwaysString   If true, all fields having DISPLAY format will remain strings and won't be converted to numbers.
  * @param allowPartialRecords     If true, partial ASCII records can be parsed (in cases when LF character is missing for example)
  * @param multisegment            Parameters specific to reading multisegment files
  * @param commentPolicy           A comment truncation policy
  * @param improvedNullDetection   If true, string values that contain only zero bytes (0x0) will be considered null.
  * @param strictIntegralPrecision If true, Cobrix will not generate short/integer/long Spark data types, and always use decimal(n) with the exact precision that matches the copybook.
  * @param decodeBinaryAsHex       Decode binary fields as HEX strings
  * @param dropGroupFillers        If true the parser will drop all FILLER fields, even GROUP FILLERS that have non-FILLER nested fields
  * @param dropValueFillers        If true the parser will drop all value FILLER fields
  * @param fillerNamingPolicy      Specifies the strategy of renaming FILLER names to make them unique
  * @param nonTerminals            A list of non-terminals (GROUPS) to combine and parse as primitive fields
  * @param debugFieldsPolicy       Specifies if debugging fields need to be added and what should they contain (false, hex, raw).
  * @param recordHeaderParser      A parser used to parse data field record headers
  * @param rhpAdditionalInfo       An optional additional option string passed to a custom record header parser
  * @param inputFileNameColumn     A column name to add to the dataframe. The column will contain input file name for each record similar to 'input_file_name()' function
  * @param metadataPolicy          Specifies the policy of metadat fields to be added to the Spark schema
  * @param options                 Options passed to spark-cobol
  */
case class ReaderParameters(
                             recordFormat:            RecordFormat = FixedLength,
                             isEbcdic:                Boolean = true,
                             isText:                  Boolean = false,
                             ebcdicCodePage:          String = "common",
                             ebcdicCodePageClass:     Option[String] = None,
                             asciiCharset:            Option[String] = None,
                             fieldCodePage:           Map[String, String] = Map.empty[String, String],
                             isUtf16BigEndian:        Boolean = true,
                             floatingPointFormat:     FloatingPointFormat = FloatingPointFormat.IBM,
                             variableSizeOccurs:      Boolean = false,
                             recordLength:            Option[Int] = None,
                             minimumRecordLength:     Int = 1,
                             maximumRecordLength:     Int = Int.MaxValue,
                             lengthFieldExpression:   Option[String] = None,
                             lengthFieldMap:          Map[String, Int] = Map.empty,
                             isRecordSequence:        Boolean = false,
                             bdw:                     Option[Bdw] = None,
                             isRdwBigEndian:          Boolean = false,
                             isRdwPartRecLength:      Boolean = false,
                             rdwAdjustment:           Int = 0,
                             isIndexGenerationNeeded: Boolean = false,
                             isIndexCachingAllowed:   Boolean = false,
                             inputSplitRecords:       Option[Int] = None,
                             inputSplitSizeMB:        Option[Int] = None,
                             inputSplitSizeCompressedMB: Option[Int] = None,
                             hdfsDefaultBlockSize:    Option[Int] = None,
                             startOffset:             Int = 0,
                             endOffset:               Int = 0,
                             fileStartOffset:         Int = 0,
                             fileEndOffset:           Int = 0,
                             generateRecordId:        Boolean = false,
                             generateRecordBytes:     Boolean = false,
                             schemaPolicy:            SchemaRetentionPolicy = SchemaRetentionPolicy.CollapseRoot,
                             stringTrimmingPolicy:    StringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
                             isDisplayAlwaysString:   Boolean = false,
                             allowPartialRecords:     Boolean = false,
                             multisegment:            Option[MultisegmentParameters] = None,
                             commentPolicy:           CommentPolicy = CommentPolicy(),
                             strictSignOverpunch:     Boolean = true,
                             improvedNullDetection:   Boolean = false,
                             strictIntegralPrecision: Boolean = false,
                             decodeBinaryAsHex:       Boolean = false,
                             dropGroupFillers:        Boolean = false,
                             dropValueFillers:        Boolean = true,
                             enableSelfChecks:        Boolean = true,
                             fillerNamingPolicy:      FillerNamingPolicy = FillerNamingPolicy.SequenceNumbers,
                             nonTerminals:            Seq[String] = Nil,
                             occursMappings:          Map[String, Map[String, Int]] = Map(),
                             debugFieldsPolicy:       DebugFieldsPolicy = DebugFieldsPolicy.NoDebug,
                             recordHeaderParser:      Option[String] = None,
                             recordExtractor:         Option[String] = None,
                             rhpAdditionalInfo:       Option[String] = None,
                             reAdditionalInfo:        String = "",
                             inputFileNameColumn:     String = "",
                             metadataPolicy:          MetadataPolicy = MetadataPolicy.Basic,
                             options:                 Map[String, String] = Map.empty
                           )
