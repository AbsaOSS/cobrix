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

package za.co.absa.cobrix.spark.cobol.source

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser
import za.co.absa.cobrix.spark.cobol.utils.Parameters

import scala.collection.immutable.HashMap

class ParametersParsingSpec extends FunSuite {
  test("Test segment id - redefine mapping") {
    val config = HashMap[String,String] ("is_record_sequence"-> "true",
      "redefine-segment-id-map:0" -> "COMPANY => C,D",
      "redefine-segment-id-map:1" -> "CONTACT => P")

    val segmentIdMapping = CobolParametersParser.getSegmentIdRedefineMapping(new Parameters(config))

    assert(segmentIdMapping("C") == "COMPANY")
    assert(segmentIdMapping("D") == "COMPANY")
    assert(segmentIdMapping("P") == "CONTACT")
    assert(segmentIdMapping.get("Q").isEmpty)
  }

  test("Test field - parent field mapping") {
    val config = HashMap[String,String] ("is_record_sequence"-> "true",
      "segment-children:1" -> "COMPANY => DEPT,CUSTOMER",
      "segment-children:2" -> "DEPT => EMPLOYEE,OFFICE",
      "segment-children:3" -> "CUSTOMER => CONTACT,CONTRACT")

    val fieldParents = CobolParametersParser.getSegmentRedefineParents(new Parameters(config))

    assert(fieldParents("DEPT") == "COMPANY")
    assert(fieldParents("CUSTOMER") == "COMPANY")
    assert(fieldParents("EMPLOYEE") == "DEPT")
    assert(fieldParents("OFFICE") == "DEPT")
    assert(fieldParents("CONTACT") == "CUSTOMER")
    assert(fieldParents("CONTRACT") == "CUSTOMER")
    assert(fieldParents.get("COMPANY").isEmpty)
  }

  test("Test field - parent field mapping (split)") {
    val config = HashMap[String,String] ("is_record_sequence"-> "true",
      "segment-children:1" -> "COMPANY => DEPT",
      "segment-children:2" -> "COMPANY => DEPT,CUSTOMER",
      "segment-children:3" -> "DEPT => EMPLOYEE",
      "segment-children:4" -> "DEPT => OFFICE",
      "segment-children:5" -> "CUSTOMER => CONTACT",
      "segment-children:6" -> "CUSTOMER => CONTRACT")

    val fieldParents = CobolParametersParser.getSegmentRedefineParents(new Parameters(config))

    assert(fieldParents("DEPT") == "COMPANY")
    assert(fieldParents("CUSTOMER") == "COMPANY")
    assert(fieldParents("EMPLOYEE") == "DEPT")
    assert(fieldParents("OFFICE") == "DEPT")
    assert(fieldParents("CONTACT") == "CUSTOMER")
    assert(fieldParents("CONTRACT") == "CUSTOMER")
    assert(fieldParents.get("COMPANY").isEmpty)
  }

  test("Test field - parent field mapping (duplicate child)") {
    val config = HashMap[String,String] ("is_record_sequence"-> "true",
      "segment-children:1" -> "COMPANY-ROOT => DEPT-ROOT,CUSTOMER,EMPLOYEE",
      "segment-children:2" -> "DEPT-ROOT => EMPLOYEE,OFFICE",
      "segment-children:3" -> "CUSTOMER => CONTACT,CONTRACT")

    val ex = intercept[IllegalArgumentException] {
      CobolParametersParser.getSegmentRedefineParents(new Parameters(config))
    }
    assert(ex.getMessage == "Duplicate child 'EMPLOYEE' for parents DEPT_ROOT and COMPANY_ROOT specified for 'segment-children' option.")
  }

}
