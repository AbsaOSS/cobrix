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

package za.co.absa.cobrix.cobol.parser.decode

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.decoders.StringTools

class StringToolsSpec extends FunSuite {
  import StringTools._

  test("Test trimLeft() works as expected") {
    assert(trimLeft("") == "")
    assert(trimLeft("a") == "a")
    assert(trimLeft("  aabb") == "aabb")
    assert(trimLeft("aabb  ") == "aabb  ")
    assert(trimLeft("  aabb  ") == "aabb  ")
  }

  test("Test trimRight) works as expected") {
    assert(trimRight("") == "")
    assert(trimRight("a") == "a")
    assert(trimRight("  aabb") == "  aabb")
    assert(trimRight("aabb  ") == "aabb")
    assert(trimRight("  aabb  ") == "  aabb")
  }

}
