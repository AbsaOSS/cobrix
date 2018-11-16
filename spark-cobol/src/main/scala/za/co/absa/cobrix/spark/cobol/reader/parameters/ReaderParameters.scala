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

package za.co.absa.cobrix.spark.cobol.reader.parameters

import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * These are properties for customizing mainframe binary data reader.
  *
  * @param lengthFieldName   A name of a field that contains record length. Optional. If not set the copybook record length will be used.
  * @param startOffset       An offset to the start of the record in each binary data block.
  * @param endOffset         An offset from the end of the record to the end of the binary data block.
  * @param generateRecordId  If true, a record id field will be prepended to each record.
  * @param policy            Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  */
case class ReaderParameters(
                             lengthFieldName: Option[String] = None,
                             isXCOM: Boolean = false,
                             isIndexGenerationNeeded: Boolean = false,
                             minRecordsPerPartition: Option[Int] = None,
                             startOffset: Int = 0,
                             endOffset: Int = 0,
                             generateRecordId: Boolean = false,
                             policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
                             multisegment: Option[MultisegmentParameters]
                           )
