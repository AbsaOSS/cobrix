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

package za.co.absa.cobrix.spark.cobol.reader.parameters

import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * These are properties for customizing mainframe binary data reader.
  *
  * @param isEbcdic                 If true the input data file encoding is EBCDIC, otherwise it is ASCII
  * @param lengthFieldName          A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param isRecordSequence         Does input files have 4 byte record length headers
  * @param isRdwBigEndian           Is RDW big endian? It may depend on flavor of mainframe and/or mainframe to PC transfer method
  * @param isIndexGenerationNeeded  Is indexing input file before processing is requested
  * @param inputSplitRecords        The number of records to include in each partition. Notice mainframe records may have variable size, inputSplitMB is the recommended option
  * @param inputSplitSizeMB         A partition size to target. In certain circumstances this size may not be exactly that, but the library will do the best effort to target that size
  * @param hdfsDefaultBlockSize     Default HDFS block size for the HDFS filesystem used. This value is used as the default split size if inputSplitSizeMB is not specified
  * @param startOffset              An offset to the start of the record in each binary data block.
  * @param endOffset                An offset from the end of the record to the end of the binary data block.
  * @param generateRecordId         If true, a record id field will be prepended to each record.
  * @param policy                   Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param multisegment             Parameters specific to reading multisegment files
  * @param dropGroupFillers         If true the parser will drop all FILLER fields, even GROUP FILLERS that have non-FILLER nested fields
  */
case class ReaderParameters(
                             isEbcdic: Boolean = true,
                             lengthFieldName: Option[String] = None,
                             isRecordSequence: Boolean = false,
                             isRdwBigEndian: Boolean = false,
                             isIndexGenerationNeeded: Boolean = false,
                             inputSplitRecords: Option[Int] = None,
                             inputSplitSizeMB: Option[Int] = None,
                             hdfsDefaultBlockSize: Option[Int] = None,
                             startOffset: Int = 0,
                             endOffset: Int = 0,
                             generateRecordId: Boolean = false,
                             policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
                             multisegment: Option[MultisegmentParameters],
                             dropGroupFillers: Boolean
                           )
