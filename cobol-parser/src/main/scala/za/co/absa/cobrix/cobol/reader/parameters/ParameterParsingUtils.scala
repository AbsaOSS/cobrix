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

object ParameterParsingUtils {
  /** Splits segment ids defined in spark-cobol options for hierarchical id generation. */
  def splitSegmentIds(segmentIdsToSplit: scala.collection.Seq[String]): Array[Array[String]] = {
    segmentIdsToSplit.toArray
      .map{ ids =>
        ids.split(',')
          .map(_.trim())
          .map(id => if (id == "*") "_" else id)
      }
  }

  /** Validates segment ids for hierarchical record id generation. */
  def validateSegmentIds(segmentIds: Array[Array[String]]): Unit = {
    val maxLevel = segmentIds.length - 1
    segmentIds.zipWithIndex.foreach {
      case (ids, level) =>
        if (ids.contains("_") && level < maxLevel)
          throw new IllegalArgumentException(s"The '_' as a segment id can only be used on the leaf level (segment_id_level$maxLevel), found at 'segment_id_level$level'")
        if (ids.contains("*") && level < maxLevel)
          throw new IllegalArgumentException(s"The '*' as a segment id can only be used on the leaf level (segment_id_level$maxLevel), found at 'segment_id_level$level'")
        if ((ids.contains("*") || ids.contains("_")) && ids.length > 1)
          throw new IllegalArgumentException(s"The '*' or '_' as a segment id cannot be used with other ids 'segment_id_level$level = ${ids.mkString(",")}' is incorrect.")
    }

  }
}
