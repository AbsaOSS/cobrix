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

/**
  * This class holds the parameters currently used for parsing variable-length records.
  *
  * @param isRecordSequence       Does input files have 4 byte record length headers
  * @param isRdwBigEndian         Is RDW big endian? It may depend on flavor of mainframe and/or mainframe to PC transfer method
  * @param isRdwPartRecLength     Does RDW count itself as part of record length itself
  * @param rdwAdjustment          Controls a mismatch between RDW and record length
  * @param recordHeaderParser     An optional custom record header parser for non-standard RDWs
  * @param rhpAdditionalInfo      An optional additional option string passed to a custom record header parser
  * @param recordLengthField      A field that stores record length
  * @param fileStartOffset        A number of bytes to skip at the beginning of each file
  * @param fileEndOffset          A number of bytes to skip at the end of each file
  * @param variableSizeOccurs     If true, OCCURS DEPENDING ON data size will depend on the number of elements
  * @param generateRecordId       Generate a sequential record number for each record to be able to retain the order of the original data
  * @param isUsingIndex           Is indexing input file before processing is requested
  * @param inputSplitSizeMB       A partition size to target. In certain circumstances this size may not be exactly that, but the library will do the best effort to target that size
  * @param inputSplitRecords      The number of records to include in each partition. Notice mainframe records may have variable size, inputSplitMB is the recommended option
  * @param improveLocality        Tries to improve locality by extracting preferred locations for variable-length records
  * @param optimizeAllocation     Optimizes cluster usage in case of optimization for locality in the presence of new nodes (nodes that do not contain any blocks of the files being processed)
  * @param inputFileNameColumn    A column name to add to the dataframe. The column will contain input file name for each record similar to 'input_file_name()' function
  */
case class VariableLengthParameters(
                                     isRecordSequence:      Boolean,
                                     isRdwBigEndian:        Boolean,
                                     isRdwPartRecLength:    Boolean,
                                     rdwAdjustment:         Int,
                                     recordHeaderParser:    Option[String],
                                     rhpAdditionalInfo:     Option[String],
                                     recordLengthField:     String,
                                     fileStartOffset:       Int,
                                     fileEndOffset:         Int,
                                     variableSizeOccurs:    Boolean,
                                     generateRecordId:      Boolean,
                                     isUsingIndex:          Boolean,
                                     inputSplitRecords:     Option[Int],
                                     inputSplitSizeMB:      Option[Int],
                                     improveLocality:       Boolean,
                                     optimizeAllocation:    Boolean,
                                     inputFileNameColumn:   String
                                   )
