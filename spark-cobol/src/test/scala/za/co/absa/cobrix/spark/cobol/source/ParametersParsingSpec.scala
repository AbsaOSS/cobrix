/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.cobrix.spark.cobol.source

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser

import scala.collection.immutable.HashMap

class ParametersParsingSpec extends FunSuite {
  test("Test segment id - redefine mapping") {
    val config = HashMap[String,String] ("is_record_sequence"-> "true",
      "redefine-segment-id-map:0" -> "COMPANY => C,D",
      "redefine-segment-id-map:1" -> "CONTACT => P")

    val segmentIdMapping = CobolParametersParser.getSegmentIdRedefineMapping(config)

    assert(segmentIdMapping("C") == "COMPANY")
    assert(segmentIdMapping("D") == "COMPANY")
    assert(segmentIdMapping("P") == "CONTACT")
    assert(segmentIdMapping.get("Q").isEmpty)
  }

}
