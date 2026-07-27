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

class CobolParametersParserSuite extends AnyWordSpec {
  "parse" should {
    "leave record_limit undefined when it is absent" in {
      val parsedParams = CobolParametersParser.parse(new Parameters(Map.empty[String, String]))

      assert(parsedParams.recordLimit.isEmpty)
    }

    "parse a zero record_limit" in {
      val parsedParams = CobolParametersParser.parse(new Parameters(Map("record_limit" -> "0")))

      assert(parsedParams.recordLimit.contains(0))
    }

    "parse a positive record_limit" in {
      val parsedParams = CobolParametersParser.parse(new Parameters(Map("record_limit" -> "100")))

      assert(parsedParams.recordLimit.contains(100))
    }

    "reject a negative record_limit" in {
      val exception = intercept[IllegalArgumentException] {
        CobolParametersParser.parse(new Parameters(Map("record_limit" -> "-1")))
      }

      assert(exception.getMessage.contains("record_limit"))
    }

    "reject a malformed record_limit" in {
      val exception = intercept[IllegalArgumentException] {
        CobolParametersParser.parse(new Parameters(Map("record_limit" -> "invalid")))
      }

      assert(exception.getMessage.contains("record_limit"))
    }

    "reject an overflowing record_limit" in {
      val exception = intercept[IllegalArgumentException] {
        CobolParametersParser.parse(new Parameters(Map("record_limit" -> "2147483648")))
      }

      assert(exception.getMessage.contains("record_limit"))
    }

    "recognize record_limit in pedantic mode" in {
      val parsedParams = CobolParametersParser.parse(new Parameters(Map(
        "record_limit" -> "1",
        "pedantic" -> "true"
      )))

      assert(parsedParams.recordLimit.contains(1))
    }

    "parse writer parameters" in {
      val params =  new Parameters(Map(
        "write_null_strings_as_spaces" -> "false",
        "write_null_display_numbers_as_zeros" -> "true",
        "write_null_comp3_numbers_as_zeros" -> "true",
        "pedantic" -> "true"
      ))

      val parsedParams = CobolParametersParser.parse(params, isWriter = true)
      assert(parsedParams.writerParameters.get == WriterParameters(
        nullStringsAsSpaces = false,
        nullDisplayNumbersAsZeros = true,
        nullComp3NumbersAsZeros = true
      ))
    }
  }

}
