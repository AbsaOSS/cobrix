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

final class SegmentIdAccumulator (segmentIds: Seq[String], segmentIdPrefix: String, val fileId: Int) {
  private val segmentIdsArr = segmentIds.toArray.map(_.split(","))
  private val segmentIdCount = segmentIds.size
  private val segmentIdAccumulator = new Array[Long](segmentIdCount + 1)
  private var currentLevel = -1
  private var currentRootId: String = ""

  segmentIdAccumulator.map(v => 0)

  /**
    * This method should be called every time for every new record. It will update segment level ids based on the segment id.
    * higher than the current level
    *
    * @param segmentId The segment id of the record
    */
  def acquiredSegmentId(segmentId: String, recordIndex: Long): Unit = {
    if (segmentIdCount == 0) return
    val level = getLevel(segmentId)
    if (level.isDefined) {
      currentLevel = level.get
      if (currentLevel == 0) {
        updateRootSegment(recordIndex)
      } else {
        segmentIdAccumulator(currentLevel) += 1
      }
    }
  }

  /**
    * Get the value for the specific level id. Any is returned deliberately to support null for hierarchy levels
    * higher than the current level
    *
    * @param level A level for which a value is requested
    * @return A Seg_Id value for the level
    */
  def getSegmentLevelId(level: Int): String = {
    if (level>=0 && level<=currentLevel) {
      if (level==0) {
        currentRootId
      } else {
        val levelId = segmentIdAccumulator(level)
        s"${currentRootId}_L${level}_$levelId"
      }
    } else
      null
  }

  def updateRootSegment(recordIndex: Long): Unit = {
    currentRootId = s"${segmentIdPrefix}_${fileId}_$recordIndex"
    var i = 0
    while (i < segmentIdAccumulator.length) {
      segmentIdAccumulator(i) = 0
      i += 1
    }
  }

  /** Gets the segment level by a segment id  */
  private def getLevel(id: String): Option[Int] = {
    var level: Option[Int] = None
    var i = 0
    while (level.isEmpty && i<segmentIdCount) {
      if (segmentIdsArr(i).contains(id))
        level = Some(i)
      i += 1
    }
    level
  }
}
