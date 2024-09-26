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

import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.policies.{CommentPolicy, FillerNamingPolicy, MetadataPolicy}
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy.DebugFieldsPolicy
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * This class holds parameters for the job.
  *
  * @param copybookPath            String containing the path to the copybook in a given file system.
  * @param multiCopybookPath       Sequence containing the paths to the copybooks.
  * @param copybookContent         String containing the actual content of the copybook. Either this, the copybookPath, or multiCopybookPath parameter must be specified.
  * @param sourcePaths             The list of source file paths.
  * @param recordFormat            The record format (F, V, VB, D)
  * @param isText                  [deprecated by recordFormat] If true the input data consists of text files where records are separated by a line ending character
  * @param isEbcdic                If true the input data file encoding is EBCDIC, otherwise it is ASCII
  * @param ebcdicCodePage          Specifies what code page to use for EBCDIC to ASCII/Unicode conversions
  * @param ebcdicCodePageClass     An optional custom code page conversion class provided by a user
  * @param asciiCharset            A charset for ASCII data
  * @param fieldCodePage           Specifies a mapping between a field name and the code page
  * @param isUtf16BigEndian        If true UTF-16 is considered big-endian.
  * @param floatingPointFormat     A format of floating-point numbers
  * @param recordStartOffset       A number of bytes to skip at the beginning of the record before parsing a record according to a copybook
  * @param recordEndOffset         A number of bytes to skip at the end of each record
  * @param recordLength            Specifies the length of the record disregarding the copybook record size. Implied the file has fixed record length.
  * @param minimumRecordLength     Minium record length for which the record is considered valid.
  * @param maximumRecordLength     Maximum record length for which the record is considered valid.
  * @param variableLengthParams    VariableLengthParameters containing the specifications for the consumption of variable-length Cobol records.
  * @param variableSizeOccurs      If true, OCCURS DEPENDING ON data size will depend on the number of elements
  * @param generateRecordBytes     Generate 'record_bytes' field containing raw bytes of the original record
  * @param schemaRetentionPolicy   A copybook usually has a root group struct element that acts like a rowtag in XML. This can be retained in Spark schema or can be collapsed
  * @param stringTrimmingPolicy    Specify if and how strings should be trimmed when parsed
  * @param allowPartialRecords     If true, partial ASCII records can be parsed (in cases when LF character is missing for example)
  * @param multisegmentParams      Parameters for reading multisegment mainframe files
  * @param improvedNullDetection   If true, string values that contain only zero bytes (0x0) will be considered null.
  * @param strictIntegralPrecision If true, Cobrix will not generate short/integer/long Spark data types, and always use decimal(n) with the exact precision that matches the copybook.
  * @param decodeBinaryAsHex       Decode binary fields as HEX strings
  * @param commentPolicy           A comment truncation policy
  * @param dropGroupFillers        If true the parser will drop all FILLER fields, even GROUP FILLERS that have non-FILLER nested fields
  * @param dropValueFillers        If true the parser will drop all value FILLER fields
  * @param nonTerminals            A list of non-terminals (GROUPS) to combine and parse as primitive fields
  * @param debugFieldsPolicy       Specifies if debugging fields need to be added and what should they contain (false, hex, raw).
  * @param debugIgnoreFileSize     If true the fixed length file reader won't check file size divisibility. Useful for debugging binary file / copybook mismatches.
  * @param debugLayoutPositions    If true, layout positions for input files will be logged (false by default)
  * @param metadataPolicy          Specifies the policy of metadat fields to be added to the Spark schema
  */
case class CobolParameters(
                            copybookPath:            Option[String],
                            multiCopybookPath:       Seq[String],
                            copybookContent:         Option[String],
                            sourcePaths:             Seq[String],
                            recordFormat:            RecordFormat,
                            isText:                  Boolean,
                            isEbcdic:                Boolean,
                            ebcdicCodePage:          String,
                            ebcdicCodePageClass:     Option[String],
                            asciiCharset:            Option[String],
                            fieldCodePage:           Map[String, String],
                            isUtf16BigEndian:        Boolean,
                            floatingPointFormat:     FloatingPointFormat,
                            recordStartOffset:       Int,
                            recordEndOffset:         Int,
                            recordLength:            Option[Int],
                            minimumRecordLength:     Option[Int],
                            maximumRecordLength:     Option[Int],
                            variableLengthParams:    Option[VariableLengthParameters],
                            variableSizeOccurs:      Boolean,
                            generateRecordBytes:     Boolean,
                            schemaRetentionPolicy:   SchemaRetentionPolicy,
                            stringTrimmingPolicy:    StringTrimmingPolicy,
                            allowPartialRecords:     Boolean,
                            multisegmentParams:      Option[MultisegmentParameters],
                            commentPolicy:           CommentPolicy,
                            strictSignOverpunch:     Boolean,
                            improvedNullDetection:   Boolean,
                            strictIntegralPrecision: Boolean,
                            decodeBinaryAsHex:       Boolean,
                            dropGroupFillers:        Boolean,
                            dropValueFillers:        Boolean,
                            fillerNamingPolicy:      FillerNamingPolicy,
                            nonTerminals:            Seq[String],
                            occursMappings:          Map[String, Map[String, Int]],
                            debugFieldsPolicy:       DebugFieldsPolicy,
                            debugIgnoreFileSize:     Boolean,
                            debugLayoutPositions:    Boolean,
                            metadataPolicy:          MetadataPolicy
                          )
