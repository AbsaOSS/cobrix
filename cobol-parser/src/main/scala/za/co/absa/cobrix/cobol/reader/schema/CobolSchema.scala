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

package za.co.absa.cobrix.cobol.reader.schema

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy


/**
  * This class provides a view on a COBOL schema from the perspective of Spark. When provided with a parsed copybook the class
  * provides the corresponding Spark schema and also other properties for the Spark data source.
  *
  * @param copybook                A parsed copybook.
  * @param policy                  Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param generateRecordId        If true, a record id field will be prepended to the beginning of the schema.
  * @param inputFileNameField      If non-empty, a source file name will be prepended to the beginning of the schema.
  * @param generateSegIdFieldsCnt  A number of segment ID levels to generate
  * @param segmentIdProvidedPrefix A prefix for each segment id levels to make segment ids globally unique (by default the current timestamp will be used)
  */
class CobolSchema(val copybook: Copybook,
                  val policy: SchemaRetentionPolicy,
                  val inputFileNameField: String,
                  val generateRecordId: Boolean,
                  val generateSegIdFieldsCnt: Int = 0,
                  segmentIdProvidedPrefix: String = "") extends Serializable {

  @transient protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val segmentIdPrefix: String = if (segmentIdProvidedPrefix.isEmpty) getDefaultSegmentIdPrefix else segmentIdProvidedPrefix

  def getCobolSchema: Copybook = copybook

  lazy val getRecordSize: Int = copybook.getRecordSize

  def isRecordFixedSize: Boolean = copybook.isRecordFixedSize

  private def getDefaultSegmentIdPrefix: String = {
    val timestampFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val now = ZonedDateTime.now()
    timestampFormat.format(now)
  }
}
