/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.cobrix.spark.cobol.source.index

import za.co.absa.cobrix.spark.cobol.reader.index.entry.SimpleIndexEntry

/**
  * This class provides methods to rebalance partitions in case we have idle executors.
  *
  * For instance, assume there were only 4 nodes (n1 ... n4) in the cluster when the files to be processed were stored.
  *
  * Later on, two new nodes were added (n5 and n6).
  *
  * When trying to achieve record-level locality the new nodes might be idle since they don't have any HDFS blocks related
  * to the files being processed.
  *
  * This class provides methods to send some work for those new nodes.
  *
  * Although some shuffling will happen, the overall benefit of having more workers should outweigh it.
  */
object LocationBalancer {

  /**
    * Distributes the partitions among all executors available.
    */
  def balance(currentDistribution: Seq[(SimpleIndexEntry, Seq[String])], availableExecutors: Seq[String]): Seq[(SimpleIndexEntry,Seq[String])] = {

    val executorSet = availableExecutors.toSet
    val allocationByExecutor = toDistributionByExecutor(currentDistribution)

    // if there are unused executors, re-balancing is necessary
    if (executorSet.nonEmpty && !executorSet.subsetOf(allocationByExecutor.keySet)) {
      val executorsToAssign = executorSet.diff(allocationByExecutor.keySet)
      val busiestExecutors = findKBusiestExecutors(allocationByExecutor, executorsToAssign.size)

      assert(executorsToAssign.size == busiestExecutors.size)

      val reallocations = for (executor <- busiestExecutors zip executorsToAssign)
        yield {
        // assigns the first entry from busy executor to first idle executor
        val relocatedEntry = allocationByExecutor(executor._1).head
        val currentAllocation = allocationByExecutor(executor._1)

        // if more than one entry, relocate, otherwise keep as is - otherwise locality might be damaged
        if (currentAllocation.size > 1) {
          allocationByExecutor(executor._1) = allocationByExecutor(executor._1).filter(_ != relocatedEntry)
          (executor._2, Seq(relocatedEntry))
        }
        else {
          (executor._1, currentAllocation)
        }
      }

      toItemsWithLocations(allocationByExecutor ++ reallocations)
    }
    else {
      currentDistribution
    }
  }

  private def toDistributionByExecutor(currentDistribution: Seq[(SimpleIndexEntry, Seq[String])]): collection.mutable.Map[String,List[SimpleIndexEntry]] = {
    collection.mutable.Map(
      currentDistribution
      .flatMap(distribution => distribution._2.map((_,distribution._1)))
      .groupBy(_._1)
      .map(group => (group._1, group._2.map(_._2).toList))
      .toSeq:_*
    )
  }

  private def findKBusiestExecutors(entriesByExecutor: collection.mutable.Map[String,List[SimpleIndexEntry]], k: Int): Seq[String] = {
    entriesByExecutor
      .toSeq
      .sortWith(_._2.size > _._2.size)
      .map(_._1)
      .take(k)
  }

  private def toItemsWithLocations(entriesByExecutors: collection.mutable.Map[String,Seq[SimpleIndexEntry]]): Seq[(SimpleIndexEntry,Seq[String])] = {
    entriesByExecutors
      .toSeq
      .flatMap(entry => entry._2.map((_, entry._1)))
      .groupBy(_._1)
      .map(group => (group._1, group._2.map(_._2).toList))
      .toList
  }
}