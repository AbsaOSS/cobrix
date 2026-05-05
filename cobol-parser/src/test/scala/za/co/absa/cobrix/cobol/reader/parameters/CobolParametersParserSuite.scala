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
