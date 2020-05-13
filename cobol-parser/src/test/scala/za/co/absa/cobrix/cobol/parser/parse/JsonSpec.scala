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

package za.co.absa.cobrix.cobol.parser.parse

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.antlr.{ParserJson}


class JsonSpec extends FunSuite {
  val parser = new ParserJson()
  def parse(text: String) = parser.parse(text)


  test("Test JSON") {
    // literals
    assert(parse("null") == null)
    assert(parse("false") == false)
    assert(parse("true") == true)
    // numbers
    assert(parse("1.2") == 1.2)
    assert(parse("5") == 5)
    assert(parse("5").isInstanceOf[Int])
    assert(parse("5.0").isInstanceOf[Double])
    // strings
    assert(parse("\"ABC\"") == "ABC")
    assert(parse("\"1.2\"") == "1.2")
    assert(parse("\"5\"") == "5")
    // arrays
    assert(parse("[\"ABC\", 1, 2]").asInstanceOf[Array[Any]].deep == Array("ABC", 1.0, 2.0).deep)
    // maps
    assert(parse("{\"ABC\": 1.2}") == Map("ABC" -> 1.2))
    // as map
    assert(parser.parseMap("{\"ABC\": 1.2}") == Map("ABC" -> 1.2))
  }

  test("JSON Fails") {
    // syntax
    intercept[ParseCancellationException] { parse("\"ABC") }
    intercept[ParseCancellationException] { parse("{\"ABC\": abc}") }
    intercept[ParseCancellationException] { parse("{\"ABC\"= 1}") }
    // cast to map
    intercept[ClassCastException] { parser.parseMap("1.2") }
  }
}
