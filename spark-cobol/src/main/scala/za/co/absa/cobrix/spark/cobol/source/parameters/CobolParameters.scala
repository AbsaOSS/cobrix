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

import za.co.absa.cobrix.cobol.parser.decoders.EbcdicCodePage.EbcdicCodePage
import za.co.absa.cobrix.spark.cobol.reader.parameters.MultisegmentParameters
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy

/**
  * This class holds parameters for the job.
  *
  * @param copybookPath           String containing the path to the copybook in a given file system.
  * @param copybookContent        String containing the actual content of the copybook. Either this or the copybookPath parameter must be specified.
  * @param sourcePath             String containing the path to the Cobol file to be parsed.
  * @param isEbcdic               If true the input data file encoding is EBCDIC, otherwise it is ASCII
  * @param ebcdicCodePage         Specifies what code page to use for EBCDIC to ASCII/Unicode conversions
  * @param isRecordSequence       Does input files have 4 byte record length headers
  * @param isRdwBigEndian         Is RDW big endian? It may depend on flavor of mainframe and/or mainframe to PC transfer method
  * @param isUsingIndex           Is indexing input file before processing is requested
  * @param inputSplitSizeMB       A partition size to target. In certain circumstances this size may not be exactly that, but the library will do the best effort to target that size
  * @param inputSplitRecords      The number of records to include in each partition. Notice mainframe records may have variable size, inputSplitMB is the recommended option
  * @param recordStartOffset      A number of bytes to skip at the beginning of the record before parsing a record according to a copybook
  * @param recordEndOffset        A number of bytes to skip at the end of each record
  * @param variableLengthParams   VariableLengthParameters containing the specifications for the consumption of variable-length Cobol records.
  *                               If None, the records will be assumed to be fixed-length.
  * @param generateRecordId       Generate a sequential record number for each record to be able to retain the order of the original data
  * @param schemaRetentionPolicy  A copybook usually has a root group struct element that acts like a rowtag in XML. This can be retained in Spark schema or can be collapsed
  *                               so that the resulting Spark schema will consist of child elements of that group
  * @param stringTrimmingPolicy   Specify if and how strings should be trimmed when parsed
  * @param multisegmentParams     Parameters for reading multisegment mainframe files
  * @param improveLocality        Tries to improve locality by extracting preferred locations for variable-length records
  * @param optimizeAllocation     Optimizes cluster usage in case of optimization for locality in the presence of new nodes (nodes that do not contain any blocks of the files being processed)
  * @param dropGroupFillers       If true the parser will drop all FILLER fields, even GROUP FILLERS that have non-FILLER nested fields
  */
case class CobolParameters(
                            copybookPath:          Option[String],
                            copybookContent:       Option[String],
                            sourcePath:            Option[String],
                            isEbcdic:              Boolean,
                            ebcdicCodePage:        EbcdicCodePage,
                            isRecordSequence:      Boolean,
                            isRdwBigEndian:        Boolean,
                            isUsingIndex:          Boolean,
                            inputSplitRecords:     Option[Int],
                            inputSplitSizeMB:      Option[Int],
                            recordStartOffset:     Int,
                            recordEndOffset:       Int,
                            variableLengthParams:  Option[VariableLengthParameters],
                            generateRecordId:      Boolean,
                            schemaRetentionPolicy: SchemaRetentionPolicy,
                            stringTrimmingPolicy:  StringTrimmingPolicy,
                            searchSignatureField:  Option[String],
                            searchSignatureValue:  Option[String],
                            multisegmentParams:    Option[MultisegmentParameters],
                            improveLocality:       Boolean,
                            optimizeAllocation:    Boolean,
                            dropGroupFillers:      Boolean
                          )