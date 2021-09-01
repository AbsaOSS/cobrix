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

package za.co.absa.cobrix.cobol.reader.recordheader

/** Parameters for parsing record headers of mainframe files (RDW and BDW) */
case class RecordHeaderParameters(
                                   isBigEndian: Boolean,

                                   /* Sometime the size includes only payload, and sometimes it includes headers themselves.
                                   * This allows flexible adjustments. */
                                   adjustment: Int
                                 )
