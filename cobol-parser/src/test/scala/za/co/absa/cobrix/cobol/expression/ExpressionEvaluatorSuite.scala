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

package za.co.absa.cobrix.cobol.expression

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.expression.NumberExprEvaluator

class ExpressionEvaluatorSuite extends AnyWordSpec {
  "getVariables()" should {
    "return the list of variables in expressions" in {
      val expr = "@d + @b * @c - @a / @d"
      val vars = new NumberExprEvaluator(expr).getVariables
      assert(vars == Seq("a", "b", "c", "d"))
    }
  }

  "eval()" should {
    "evaluate simple expressions" when {
      val exprs = Seq(
        ("2 + 4", 6),
        ("2 - 2", 0),
        ("10 * 4", 40),
        ("40 / 5", 8),
        ("(10 * 2) + (5 * 3)", 35),
        ("10 * (2 + 5) * 3", 210),
        ("10 * 2 + 5 * 3", 35),
        ("10 * 2 + 6 / 3", 22),
        ("10 * 2 / (6 / 3)", 10),
        ("(2 + 3) * (7 + 3)", 50),
        ("(10 * 2 + 5 * 3) / 5", 7))

      exprs.foreach {
        case (expr, expectedResult) =>
          s"$expr" in {
            val actualResult = new NumberExprEvaluator(expr).eval()
            assert(actualResult == expectedResult)
          }
      }
    }

    "evaluate expressions with variables" in {
      val expr = "10 * (@a1 + 5) * @bcd"
      val evaluator = new NumberExprEvaluator(expr)
      evaluator.setValue("a1", 2)
      evaluator.setValue("bcd", 3)

      val actualResult = evaluator.eval()
      assert(actualResult == 210)
    }
  }
}
