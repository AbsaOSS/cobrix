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

/**
  * These parameters specify segment fields for reassembling multi-segment data.
  * For example, data like this:
  * {{{
  *  file header record
  *  segment header record
  *    main segment record
  *       child1 segment record
  *       child2 segment record
  *       ...
  *  segment footer record
  *  segment header record
  *    main segment record
  *       child1 segment record
  *       child2 segment record
  *       ...
  *  segment footer record
  *  file footer record
  * }}}
  *
  * Produces:
  * {{{
  *   file header | segment header | main segment | child1 segment | child2 segment | segment footer | file footer
  *   file header | segment header | main segment | child1 segment | child2 segment | segment footer | file footer
  *   file header | segment header | main segment | child1 segment | child2 segment | segment footer | file footer
  * }}}
  *
  * Here:
  *  - headerSegmentFields = Seq("file header", "segment header")
  *  - mainSegmentField = "main segment"
  *  - childSegmentFields = Seq("child1 segment", "child2 segment")
  *  - headerSegmentFields = Seq("segment footer", "file footer")
  * */
case class ReassembleParameters(
                                 headerSegmentFields: Seq[String],
                                 mainSegmentField: String,
                                 childSegmentFields: Seq[String],
                                 footerSegmentFields: Seq[String]
                               )
