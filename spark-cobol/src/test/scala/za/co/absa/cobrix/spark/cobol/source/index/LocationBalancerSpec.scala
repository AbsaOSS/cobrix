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

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry

import scala.collection.mutable.ArrayBuffer

class LocationBalancerSpec extends FlatSpec with BeforeAndAfterAll {

  behavior of LocationBalancer.getClass.getName

  it should "return empty list if input list is also empty" in {
    val currentDistribution = Seq[(SparseIndexEntry,Seq[String])]()
    assert(LocationBalancer.balance(currentDistribution, Seq[String]()).isEmpty)
  }

  it should "keep current distribution if empty list of executors" in {
    val currentDistribution = Seq[(SparseIndexEntry,Seq[String])](
      (SparseIndexEntry(0l, 2l, 1, 1l), Seq("exec1", "exec2")),
      (SparseIndexEntry(2l, 4l, 1, 2l), Seq("exec1", "exec3"))
    )
    assert(currentDistribution == LocationBalancer.balance(currentDistribution, Seq[String]()))
  }

  it should "keep current distribution if fewer executors in the list but all in the current distribution" in {
    val currentDistribution = Seq[(SparseIndexEntry,Seq[String])](
      (SparseIndexEntry(0l, 2l, 1, 1l), Seq("exec1", "exec2")),
      (SparseIndexEntry(2l, 4l, 1, 2l), Seq("exec1", "exec3"))
    )
    val availableExecutors = Seq("exec2", "exec3")
    assert(currentDistribution == LocationBalancer.balance(currentDistribution, availableExecutors))

  }

  it should "rebalance if fewer executors in the list but any of them not in the current distribution" in {
    val currentDistribution = List[(SparseIndexEntry,Seq[String])](
      (SparseIndexEntry(0l, 2l, 1, 1l), List("exec1", "exec2")),
      (SparseIndexEntry(2l, 4l, 1, 2l), List("exec1", "exec3"))
    )

    val availableExecutors = Seq("exec4")

    val actual = LocationBalancer.balance(currentDistribution, availableExecutors)

    val expected = List[(Any,Seq[String])](
      (SparseIndexEntry(2l, 4l, 1, 2l), List("exec3", "exec1")),
      (SparseIndexEntry(0l, 2l, 1, 1l), List("exec2", "exec4"))
    )

    assert(actual == expected)
  }

  it should "not rebalance if locality is damaged for nothing" in {
    val currentDistribution = List[(SparseIndexEntry,Seq[String])](
      (SparseIndexEntry(0l, 2l, 1, 1l), List("exec1", "exec2")),
      (SparseIndexEntry(2l, 4l, 1, 2l), List("exec1", "exec3"))
    )

    val availableExecutors = Seq("exec2", "exec3", "exec4", "exec5")

    val actual = LocationBalancer.balance(currentDistribution, availableExecutors)

    val expected = List[(Any,Seq[String])](
      (SparseIndexEntry(2l, 4l, 1, 2l), List("exec3", "exec1")),
      (SparseIndexEntry(0l, 2l, 1, 1l), List("exec2", "exec4"))
    )

    assert(actual == expected)
  }

  it should "accept more idle than allocated executors and rebalance correctly" in {
    val currentDistribution = List[(SparseIndexEntry,Seq[String])](
      (SparseIndexEntry(0l, 2l, 1, 1l), List("exec1")),
      (SparseIndexEntry(2l, 4l, 1, 2l), List("exec1"))
    )

    val availableExecutors = Seq("exec2", "exec3", "exec4", "exec5")

    val actual = LocationBalancer.balance(currentDistribution, availableExecutors)

    val expected = List[(Any,Seq[String])](
      (SparseIndexEntry(2l, 4l, 1, 2l), List("exec1")),
      (SparseIndexEntry(0l, 2l, 1, 1l), List("exec2"))
    )

    assert(actual == expected)
  }
}