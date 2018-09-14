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

package za.co.absa.cobrix.spark.cobol.reader.rules.impl

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import za.co.absa.cobrix.spark.cobol.reader.rules.RuleExpression
import za.co.absa.cobrix.spark.cobol.reader.rules.evaluation.RuleEvaluatorFactory
import za.co.absa.cobrix.spark.cobol.reader.rules.impls.SimpleNashornRule

class SimpleNashornRuleSpec extends FlatSpec with BeforeAndAfter with Matchers {

  private val fieldsValues = Map("a" -> 1, "b" -> 2, "c" -> 3)
  private val fields = Seq("a", "b", "c")
  private val expression = new RuleExpression("a < b || b == c", "expected_result")
  private val evaluator = RuleEvaluatorFactory.buildNashornEvaluator()

  private val rule = new SimpleNashornRule(fields, expression, evaluator)

  behavior of rule.getClass.getName

  it should "return the received fields" in {
    assert(rule.getFields() == fields)
  }

  it should "return the received result" in {
    assert(rule.getResult() == expression.result)
  }

  it should "evaluate correct expressions" in {
    assert(rule.eval(fieldsValues))
  }

  it should "throw if invalid expressions" in {
    val exception = intercept[javax.script.ScriptException] {
      new SimpleNashornRule(fields, new RuleExpression("a = b || b < c", "any_result"), evaluator).eval(fieldsValues)
    }

    assert(exception.getMessage.contains("Invalid left hand side for assignment"))
  }
}