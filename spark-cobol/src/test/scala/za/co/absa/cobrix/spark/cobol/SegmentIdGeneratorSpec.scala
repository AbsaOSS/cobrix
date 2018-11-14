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

package za.co.absa.cobrix.spark.cobol

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.reader.varlen.iterator.SegmentIdAccumulator

class SegmentIdGeneratorSpec extends FunSuite {

  // Here we test segment id generator for hierarchical databases
  // Each hierarchy level has a record id unique for that

  test("Test example segment id generator sequence") {

    // Initialize the generator with 4 levels of hierarchy. Each level has a specific segment id
    val acc = new SegmentIdAccumulator(Seq("AAA", "BBB", "CCC", "DDD"), "Z", 0)

    // Simulate root record incoming
    acc.acquiredSegmentId("AAA", 1)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String]== "Z_0_1")
    assert (acc.getSegmentLevelId(1) == null)
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)
    assert (acc.getSegmentLevelId(4) == null)

    // Simulate root record incoming
    acc.acquiredSegmentId("AAA", 2)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_2")
    assert (acc.getSegmentLevelId(1) == null)
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)

    // Simulate child record incoming
    acc.acquiredSegmentId("BBB", 3)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_2")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_2_L1_1")
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)

    // Simulate child record incoming
    acc.acquiredSegmentId("BBB", 4)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_2")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_2_L1_2")
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)

    // Simulate child record incoming
    acc.acquiredSegmentId("BBB", 5)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_2")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_2_L1_3")
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)

    // Simulate child of a child record incoming
    acc.acquiredSegmentId("CCC", 6)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_2")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_2_L1_3")
    assert (acc.getSegmentLevelId(2).asInstanceOf[String] == "Z_0_2_L2_1")
    assert (acc.getSegmentLevelId(3) == null)

    // Simulate root record incoming
    acc.acquiredSegmentId("AAA", 7)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_7")
    assert (acc.getSegmentLevelId(1) == null)
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)

    acc.acquiredSegmentId("BBB", 8)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_7")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_7_L1_1")
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)

    acc.acquiredSegmentId("CCC", 9)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_7")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_7_L1_1")
    assert (acc.getSegmentLevelId(2).asInstanceOf[String] == "Z_0_7_L2_1")
    assert (acc.getSegmentLevelId(3) == null)

    acc.acquiredSegmentId("DDD", 10)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_7")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_7_L1_1")
    assert (acc.getSegmentLevelId(2).asInstanceOf[String] == "Z_0_7_L2_1")
    assert (acc.getSegmentLevelId(3).asInstanceOf[String] == "Z_0_7_L3_1")

    acc.acquiredSegmentId("DDD", 11)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_7")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_7_L1_1")
    assert (acc.getSegmentLevelId(2).asInstanceOf[String] == "Z_0_7_L2_1")
    assert (acc.getSegmentLevelId(3).asInstanceOf[String] == "Z_0_7_L3_2")

    // Unknown segment ids should not change the segment ids
    acc.acquiredSegmentId("DDD1", 12)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_7")
    assert (acc.getSegmentLevelId(1).asInstanceOf[String] == "Z_0_7_L1_1")
    assert (acc.getSegmentLevelId(2).asInstanceOf[String] == "Z_0_7_L2_1")
    assert (acc.getSegmentLevelId(3).asInstanceOf[String] == "Z_0_7_L3_2")

    acc.acquiredSegmentId("AAA", 13)
    assert (acc.getSegmentLevelId(0).asInstanceOf[String] == "Z_0_13")
    assert (acc.getSegmentLevelId(1) == null)
    assert (acc.getSegmentLevelId(2) == null)
    assert (acc.getSegmentLevelId(3) == null)
  }

}
