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

package za.co.absa.cobrix.spark.cobol.reader.rules

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class RulesFacadeSpec extends FlatSpec with BeforeAndAfter with Matchers {

  behavior of RulesFacade.getClass.getName

  it should "return empty sequence of rules if no expressions" in {
   assert(RulesFacade.getRules(Seq()).isEmpty)
  }

  it should "throw if received null list of expressions" in {
    val exception = intercept[IllegalArgumentException]{
      RulesFacade.getRules(null)
    }
    assert(exception.getMessage.contains("Received null"))
  }

  it should "parse rules from expressions" in {
    val expressions = Seq(
      RuleExpression("field(b) < field(a) && field(a) > field(c)", "result_1"),
      RuleExpression("field(a) > field(b) || field(a) > field(c)", "result_2")
    )

    val fieldsValues = Map(
      "a" -> 3,
      "b" -> 2,
      "c" -> 1
    )

    val rules = RulesFacade.getRules(expressions)

    rules.foreach(rule => assert(rule.eval(fieldsValues)))
  }

  it should "extract rules from parameters" in {

    val testData = Map(
      "rule10: whatever" -> "first rule here",
      "copybook" -> "/home/me/copybook.cb",
      "rule   9   : some rule here " -> "rule with messed up spaces",
      "rule 2 : " -> "no rule, but don't break the others",
      "rule 1 : field(a) == field(b)" -> "decent rule spec",
      "path" -> "/etc/repo/data"
    )

    val expected = Seq(
      RuleExpression("field(a) == field(b)", "decent rule spec"),
      RuleExpression("some rule here", "rule with messed up spaces"),
      RuleExpression("whatever", "first rule here")
    )

    assert(expected == RulesFacade.extractRules(testData))
  }
}