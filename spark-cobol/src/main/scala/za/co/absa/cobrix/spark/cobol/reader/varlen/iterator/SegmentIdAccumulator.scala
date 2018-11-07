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

package za.co.absa.cobrix.spark.cobol.reader.varlen.iterator

class SegmentIdAccumulator (segmentIds: Seq[String], startingId: Long = 0) {
  private val segmentIdsArr = segmentIds.toArray
  private val segmentIdCount = segmentIds.size
  private val segmentIdAccumulator = new Array[Long](segmentIdCount + 1)
  private var currentLevel = -1

  segmentIdAccumulator.map(v => 0)

  /**
    * This method should be called every time for every new record. It will update segment level ids based on the segment id.
    * higher than the current level
    *
    * @param segmentId The segment id of the record
    */

  def acquiredSegmentId(segmentId: String): Unit = {
    if (segmentIdCount == 0) return
    val level = getLevel(segmentId)
    level match {
      case Some(l) =>
        currentLevel = l
        segmentIdAccumulator(currentLevel) += 1
      case None => // do nothing
    }
  }

  /**
    * Get the value for the specific level id. Any is returned deliberately to support null for hierarchy levels
    * higher than the current level
    *
    * @param level A level for which a value is requested
    * @return A Seg_Id value for the level
    */
  def getSegmentLevelId(level: Int): Any = {
    if (level>=0 && level<=currentLevel) {
      segmentIdAccumulator(level)
    } else
      null
  }

  /** Gets the segment level by a segment id  */
  private def getLevel(id: String): Option[Int] = {
    var level: Option[Int] = None
    var i = 0
    while (level.isEmpty && i<segmentIdCount) {
      if (id == segmentIdsArr(i))
        level = Some(i)
      i += 1
    }
    level
  }
}
