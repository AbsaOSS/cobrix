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

import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * This class holds parameters for the job.
  *
  * @param copybookPath String containing the path to the copybook in a given file system.
  * @param copybookContent String containing the actual content of the copybook. Either this or the copybookPath parameter must be specified.
  * @param sourcePath String containing the path to the Cobol file to be parsed.
  * @param variableLengthParams VariableLengthParameters containing the specifications for the consumption of variable-length Cobol records.
  *                             If None, the records will be assumed to be fixed-length.
  */
case class CobolParameters(
                            copybookPath:          Option[String],
                            copybookContent:       Option[String],
                            sourcePath:            Option[String],
                            recordStartOffset:     Int,
                            recordEndOffset:       Int,
                            variableLengthParams:  Option[VariableLengthParameters],
                            generateRecordId:      Boolean,
                            schemaRetentionPolicy: SchemaRetentionPolicy
                          )