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

package za.co.absa.cobrix.spark.cobol.reader.rules.parsing.impl

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import za.co.absa.cobrix.spark.cobol.reader.rules.RuleExpression

class NashornRulesParserSpec extends FlatSpec with BeforeAndAfter with Matchers {

  private val parser = new NashornRuleParser()
  private val expectedResult = "just_expected"
  private val fieldsValues = Map[String,Any](
    "a" -> 2.4,
    "b" -> 1.8,
    "c" -> "\"value\"",
    "d" -> "\"value\""
  )

  behavior of parser.getClass.getName

  it should "throw on invalid input" in {
    val nullException = intercept[IllegalArgumentException](
      parser.parse(null)
    )
    assert(nullException.getMessage.contains("null"))
  }

  it should "return empty list if no expression is passed" in {
    assert(parser.parse(Seq()).isEmpty)
  }

  it should "throw if missing values" in {

    val fieldsWithMissingValues = Map[String,Any](
      "a" -> 1,
      "b" -> 2,
      "c" -> 3
      // "d" -> 4 -> field "d" will be missing
    )

    val expressions = Seq(new RuleExpression("field(a) < field(b) || field(c) == field(d) || field(a) == 2.8", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      val exception = intercept[java.util.NoSuchElementException]{
        rule.eval(fieldsWithMissingValues)
      }

      assert(exception.getMessage.contains("key not found: d"))
    })
  }

  it should "throw if expressions are unbalanced" in {

    val expressions = Seq(new RuleExpression("(field(a) < field(b) || field(a) == 2 || (field(c) == field(d))", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      val exception = intercept[javax.script.ScriptException]{
        rule.eval(fieldsValues)
      }

      assert(exception.getMessage.contains("Expected ) but found eof"))
    })
  }

  it should "throw if values are malformed" in {

    val expressions = Seq(new RuleExpression("field(a) < field(b) || field(a) == error || field(c) == field(d)", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      val exception = intercept[javax.script.ScriptException]{
        (rule.eval(fieldsValues))
      }

      assert(exception.getMessage.contains("\"error\" is not defined"))
    })
  }

  it should "throw if fields are malformed" in {

    val expressions = Seq(new RuleExpression("field(a) < field(b || field(c) == field(d) || a == 2.8", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      val exception = intercept[java.util.NoSuchElementException]{
        rule.eval(fieldsValues)
      }
      assert(exception.getMessage.contains("key not found"))
    })
  }

  it should "support simple rules" in {

    val expressions = Seq(new RuleExpression("field(a) < field(b) || field(c) == field(d) || a == 2.8", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      assert(rule.eval(fieldsValues))
    })
  }

  it should "support nested rules" in {

    val expressions = Seq(new RuleExpression("(field(a) < field(b) || (field(a) * field(b) < 10)) && (field(c) == field(d) || field(c) == \"value\")", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      assert(rule.eval(fieldsValues))
    })
  }

  it should "create rules that return correct fields" in {

    val expressions = Seq(new RuleExpression("(field(a) < field(b) || (field(a) * field(b) < 10)) && (field(c) == field(d) || field(c) == \"value\")", expectedResult))
    val expectedFields = Seq[String]("a", "b", "c", "d")

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      assert(rule.getFields() == expectedFields)
    })
  }

  it should "create rules that return the correct value" in {

    val expressions = Seq(new RuleExpression("(field(a) < field(b) || (field(a) * field(b) < 10)) && (field(c) == field(d) || field(c) == \"value\")", expectedResult))

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      assert(rule.getResult() == expectedResult)
    })
  }

  it should "support multiple rules" in {

    val expressions = Seq(
      new RuleExpression("field(a) < field(b) || field(c) == field(d) || a == 2.8", expectedResult),
      new RuleExpression("field(b) == 1 || field(c) == field(d) || a == 2.8", expectedResult)
    )

    val rules = parser.parse(expressions)

    rules.foreach(rule => {
      assert(rule.eval(fieldsValues))
    })
  }
}