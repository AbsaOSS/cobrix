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

/**
  * This class holds the parameters currently used for parsing variable-length records.
  */
case class MultisegmentParameters(
                                   segmentIdField: String,
                                   segmentIdFilter: Option[Seq[String]],      // The list of segment ids to read
                                   segmentLevelIds: Seq[String],              // The list of segment id on corresponding levels
                                   segmentIdPrefix: String,                   // A prefix that will be added to all segment id fields
                                   segmentIdRedefineMap: Map[String, String], // Key = a segment id, Value = a redefined field
                                   fieldParentMap: Map[String, String]        // Key = a segment redefined field, Value = a parent field
                                 )
