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

package za.co.absa.cobrix.spark.cobol.reader.rules.language

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import za.co.absa.cobrix.spark.cobol.reader.rules.RuleExpression

class TokensSpec extends FlatSpec with BeforeAndAfter with Matchers {

  behavior of Tokens.getClass.getName

  it should "parse fields" in {
    val expectedFields = Seq[String]("a", "b", "c", "d")
    val expression = "field( a) + field( b ) + field(c )     +  field(d)"

    assert(expectedFields == Tokens.getFieldsFromExpression(expression).toList)
  }

  it should "clean fields" in {
    val expectedResult = "(a) + (b) + (c)"
    val expression = "field(a) + field(b) + field(c)"

    assert(expectedResult == Tokens.cleanExpressionFields(expression))
  }

  it should "clean parentheses" in {
    val expectedResult = "abcdef"
    val expression = s"($expectedResult)"

    assert(Tokens.cleanParentheses(expression) == expectedResult)
  }

  it should "get content inside parentheses" in {
    val expectedResult = "(abcedf)"
    val expression = s"field${expectedResult}"

    assert(Tokens.getParenthesesContent(expression) == expectedResult)
  }

  it should "extract the rules in order" in {
    val testData = Map(
      "rule10: whatever" -> "first rule here",
      "this is not a rule" -> "any value",
      "rule   9   : some rule here " -> "rule with messed up spaces",
      "rule 2 : " -> "no rule, but don't break the others",
      "rule 1 : field(a) == field(b)" -> "decent rule spec"
    )

    val expected = Seq(
      RuleExpression("field(a) == field(b)", "decent rule spec"),
      RuleExpression("some rule here", "rule with messed up spaces"),
      RuleExpression("whatever", "first rule here")
    )

    assert(expected == Tokens.extractSortedRules(testData))
  }
}