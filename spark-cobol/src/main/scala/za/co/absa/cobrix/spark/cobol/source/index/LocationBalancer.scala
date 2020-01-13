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

package za.co.absa.cobrix.spark.cobol.source.index

import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry

case class ExecutorPair(newExecutor: String, busyExecutor: String)

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
  def balance(currentDistribution: Seq[(SparseIndexEntry, Seq[String])], availableExecutors: Seq[String]): Seq[(SparseIndexEntry,Seq[String])] = {

    val executorSet = availableExecutors.toSet
    val allocationByExecutor = toDistributionByExecutor(currentDistribution)

    // if there are unused executors (executors that did not receive any partitions), re-balancing is necessary
    if (executorSet.nonEmpty && !executorSet.subsetOf(allocationByExecutor.keySet)) {

      val newExecutors: Set[String] = executorSet.diff(allocationByExecutor.keySet)
      val busiestExecutors: Seq[String] = findKBusiestExecutors(allocationByExecutor, newExecutors.size)

      val reallocations = for (executorsPair <- toExecutorPairs(newExecutors.toSeq, busiestExecutors)) yield {

        val entryToRelocate: SparseIndexEntry = allocationByExecutor(executorsPair.busyExecutor).head
        val currentBusyExecutorAllocation: List[SparseIndexEntry] = allocationByExecutor(executorsPair.busyExecutor)

        // if more than one entry, relocate, otherwise keep as is - otherwise locality might be damaged
        if (currentBusyExecutorAllocation.size > 1) {
          // removes the entry for the busy executor queue
          allocationByExecutor(executorsPair.busyExecutor) = allocationByExecutor(executorsPair.busyExecutor).filter(_ != entryToRelocate)
          // assigns the entry to a new idle executor
          (executorsPair.newExecutor, Seq(entryToRelocate))
        }
        else {
          // otherwise, keeps the allocation as it is
          (executorsPair.busyExecutor, currentBusyExecutorAllocation)
        }
      }

      // converts the reallocations to the same format they had before
      toItemsWithLocations(allocationByExecutor ++ reallocations)
    }
    else {
      currentDistribution
    }
  }

  private def toDistributionByExecutor(currentDistribution: Seq[(SparseIndexEntry, Seq[String])]): collection.mutable.Map[String,List[SparseIndexEntry]] = {
    collection.mutable.Map(
      currentDistribution
      .flatMap(distribution => distribution._2.map((_,distribution._1)))
      .groupBy(_._1)
      .map(group => (group._1, group._2.map(_._2).toList))
      .toSeq:_*
    )
  }

  private def findKBusiestExecutors(entriesByExecutor: collection.mutable.Map[String,List[SparseIndexEntry]], k: Int): Seq[String] = {
    entriesByExecutor
      .toSeq
      .sortWith(_._2.size > _._2.size) // sort executors by number of entries assigned to it, in decreasing order
      .map(_._1) // collect the executor addresses
      .take(k) // and takes the first k
  }

  private def toItemsWithLocations(entriesByExecutors: collection.mutable.Map[String,Seq[SparseIndexEntry]]): Seq[(SparseIndexEntry,Seq[String])] = {
    entriesByExecutors
      .toSeq
      .flatMap(entry => entry._2.map((_, entry._1))) // reverts the map(k,v): now, it will be map(v,k), or map(entry,executor)
      .groupBy(_._1) // group by entry
      .map(group => (group._1, group._2.map(_._2).toList)) // maps preferred executors to an entry
      .toList
  }

  /**
    * Performs a zip-like operation between differently sized sequences, wrapping the result into ExecutorPair instances.
    */
  private def toExecutorPairs(newExecutor: Seq[String], busiestExecutors: Seq[String]): Seq[ExecutorPair] = {
    Seq.range(0, Math.min(newExecutor.size, busiestExecutors.size))
      .map(i => ExecutorPair(newExecutor(i), busiestExecutors(i)))
  }
}