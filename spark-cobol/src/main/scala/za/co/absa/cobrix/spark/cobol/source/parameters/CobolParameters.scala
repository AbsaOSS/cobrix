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

import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.policies.CommentPolicy
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.spark.cobol.reader.parameters.MultisegmentParameters
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * This class holds parameters for the job.
  *
  * @param copybookPath           String containing the path to the copybook in a given file system.
  * @param multiCopybookPath      Sequence containing the paths to the copybooks.
  * @param copybookContent        String containing the actual content of the copybook. Either this, the copybookPath, or multiCopybookPath parameter must be specified.
  * @param sourcePath             String containing the path to the Cobol file to be parsed.
  * @param isEbcdic               If true the input data file encoding is EBCDIC, otherwise it is ASCII
  * @param ebcdicCodePage         Specifies what code page to use for EBCDIC to ASCII/Unicode conversions
  * @param ebcdicCodePageClass    An optional custom code page conversion class provided by a user
  * @param asciiCharset           A charset for ASCII data
  * @param floatingPointFormat    A format of floating-point numbers
  * @param recordStartOffset      A number of bytes to skip at the beginning of the record before parsing a record according to a copybook
  * @param recordEndOffset        A number of bytes to skip at the end of each record
  * @param variableLengthParams   VariableLengthParameters containing the specifications for the consumption of variable-length Cobol records.
  * @param schemaRetentionPolicy  A copybook usually has a root group struct element that acts like a rowtag in XML. This can be retained in Spark schema or can be collapsed
  * @param stringTrimmingPolicy   Specify if and how strings should be trimmed when parsed
  * @param multisegmentParams     Parameters for reading multisegment mainframe files
  * @param commentPolicy          A comment truncation policy
  * @param dropGroupFillers       If true the parser will drop all FILLER fields, even GROUP FILLERS that have non-FILLER nested fields
  * @param nonTerminals           A list of non-terminals (GROUPS) to combine and parse as primitive fields
  * @param debugIgnoreFileSize    If true the fixed length file reader won't check file size divisibility. Useful for debugging binary file / copybook mismatches.
  */
case class CobolParameters(
                            copybookPath:          Option[String],
                            multiCopybookPath:     Seq[String],
                            copybookContent:       Option[String],
                            sourcePath:            Option[String],
                            isEbcdic:              Boolean,
                            ebcdicCodePage:        String,
                            ebcdicCodePageClass:   Option[String],
                            asciiCharset:          String,
                            floatingPointFormat:   FloatingPointFormat,
                            recordStartOffset:     Int,
                            recordEndOffset:       Int,
                            variableLengthParams:  Option[VariableLengthParameters],
                            schemaRetentionPolicy: SchemaRetentionPolicy,
                            stringTrimmingPolicy:  StringTrimmingPolicy,
                            multisegmentParams:    Option[MultisegmentParameters],
                            commentPolicy:         CommentPolicy,
                            dropGroupFillers:      Boolean,
                            nonTerminals:          Seq[String],
                            debugIgnoreFileSize:   Boolean
                          )
