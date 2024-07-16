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

import org.scalatest.wordspec.AnyWordSpec

class ParameterParsingUtilsSuite extends AnyWordSpec {
  "splitSegmentIds()" should {
    "split input segment ids" in {
      val segmentIds = Seq("A,B,C", "D,E,F")

      val actual = ParameterParsingUtils.splitSegmentIds(segmentIds)

      assert(actual.length == 2)
      assert(actual(0).sameElements(Array("A", "B", "C")))
      assert(actual(1).sameElements(Array("D", "E", "F")))
    }

    "trim if split with spaces" in {
      val segmentIds = Seq("A, B, C", "D, E, F")

      val actual = ParameterParsingUtils.splitSegmentIds(segmentIds)

      assert(actual.length == 2)
      assert(actual(0).sameElements(Array("A", "B", "C")))
      assert(actual(1).sameElements(Array("D", "E", "F")))
    }

    "handle empty strings" in {
      val segmentIds = Seq("", "")

      val actual = ParameterParsingUtils.splitSegmentIds(segmentIds)

      assert(actual.length == 2)
      assert(actual(0).head == "")
      assert(actual(1).head == "")
    }

  }


  "validateSegmentIds()" should {
    "validate segment ids" in {
      val segmentIds = Array(
        Array("A", "B", "C"),
        Array("D", "E", "F")
      )

      ParameterParsingUtils.validateSegmentIds(segmentIds)
    }

    "throw an exception if '_' is used on the wrong level" in {
      val segmentIds = Array(
        Array("_"),
        Array("A", "B", "C")
      )

      val ex = intercept[IllegalArgumentException] {
        ParameterParsingUtils.validateSegmentIds(segmentIds)
      }

      assert(ex.getMessage.contains("The '_' as a segment id can only be used on the leaf level (segment_id_level1), found at 'segment_id_level0'"))
    }

    "throw an exception if '*' is used on the wrong level" in {
      val segmentIds = Array(
        Array("A"),
        Array("B"),
        Array("*"),
        Array("C")
      )

      val ex = intercept[IllegalArgumentException] {
        ParameterParsingUtils.validateSegmentIds(segmentIds)
      }

      assert(ex.getMessage.contains("The '*' as a segment id can only be used on the leaf level (segment_id_level3), found at 'segment_id_level2'"))
    }

    "throw an exception if '*' or '_' is used with other ids" in {
      val segmentIds = Array(
        Array("A", "B", "C"),
        Array("D", "*", "F", "G")
      )

      val ex = intercept[IllegalArgumentException] {
        ParameterParsingUtils.validateSegmentIds(segmentIds)
      }

      assert(ex.getMessage.contains("'*' or '_' as a segment id cannot be used with other ids"))
    }
  }

}
