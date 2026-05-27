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
import za.co.absa.cobrix.cobol.parser.expression.ExpressionEvaluator
import za.co.absa.cobrix.cobol.parser.expression.exception.ExprSyntaxError

class ExpressionEvaluatorSuite extends AnyWordSpec {
  "getVariables()" should {
    "return the list of variables in expressions" in {
      val expr = "d + b * c - a / d"
      val vars = new ExpressionEvaluator(expr).getVariables
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
            val actualResult = new ExpressionEvaluator(expr).evalInt()
            assert(actualResult == expectedResult)
          }
      }
    }

    "evaluate expressions with variables" in {
      val expr = "10 * (a1 + 5) * bcd"
      val evaluator = new ExpressionEvaluator(expr)
      evaluator.setValue("a1", 2)
      evaluator.setValue("bcd", 3)

      val actualResult = evaluator.evalInt()
      assert(actualResult == 210)
    }

    "evaluate boolean expressions" in {
      val expr = "a1*2 = 4"
      val evaluator = new ExpressionEvaluator(expr)
      evaluator.setValue("a1", 2)

      val actualResult = evaluator.evalBool()
      assert(actualResult)
    }

    "fail when int expected but boolean returned" in {
      val expr = "a1*2 = 4"
      val evaluator = new ExpressionEvaluator(expr)
      evaluator.setValue("a1", 2)

      val ex = intercept[ExprSyntaxError] {
        evaluator.evalInt()
      }

      assert(ex.getMessage == "The expression does not return a number in 'a1*2 = 4'.")
    }

    "fail when bool expected but int returned" in {
      val expr = "a1*2"
      val evaluator = new ExpressionEvaluator(expr)
      evaluator.setValue("a1", 2)

      val ex = intercept[ExprSyntaxError] {
        evaluator.evalBool()
      }

      assert(ex.getMessage == "The expression does not return a boolean in 'a1*2'.")
    }

    "evaluate string literal expressions" in {
      assert(new ExpressionEvaluator("'hello' = 'hello'").evalBool())
      assert(new ExpressionEvaluator("'abc' != 'def'").evalBool())
      assert(!new ExpressionEvaluator("'test' = 'Test'").evalBool())
    }

    "handle escaped quotes in strings" in {
      assert(new ExpressionEvaluator("'It''s' = 'It''s'").evalBool())
      val evaluator = new ExpressionEvaluator("s = 'It''s'")
      evaluator.setStringValue("s", "It's")
      assert(evaluator.evalBool())
    }

    "handle empty strings" in {
      assert(new ExpressionEvaluator("'' = ''").evalBool())
      assert(new ExpressionEvaluator("'' != 'a'").evalBool())
    }

    "evaluate comparison operators with integers" in {
      val e1 = new ExpressionEvaluator("a > 5")
      e1.setValue("a", 10)
      assert(e1.evalBool())

      val e2 = new ExpressionEvaluator("a > 5")
      e2.setValue("a", 3)
      assert(!e2.evalBool())

      assert(new ExpressionEvaluator("3 < 5").evalBool())
      assert(!new ExpressionEvaluator("5 < 3").evalBool())
      assert(new ExpressionEvaluator("5 >= 5").evalBool())
      assert(new ExpressionEvaluator("6 >= 5").evalBool())
      assert(!new ExpressionEvaluator("4 >= 5").evalBool())
      assert(new ExpressionEvaluator("5 <= 5").evalBool())
      assert(!new ExpressionEvaluator("5 <= 4").evalBool())
      assert(new ExpressionEvaluator("5 != 6").evalBool())
      assert(!new ExpressionEvaluator("5 != 5").evalBool())
    }

    "compare strings with operators" in {
      assert(new ExpressionEvaluator("'abc' < 'def'").evalBool())
      assert(new ExpressionEvaluator("'xyz' > 'abc'").evalBool())
      assert(new ExpressionEvaluator("'abc' <= 'abc'").evalBool())
      assert(new ExpressionEvaluator("'abc' >= 'abc'").evalBool())
    }

    "compare string variables" in {
      val evaluator = new ExpressionEvaluator("status = 'ACTIVE'")
      evaluator.setStringValue("status", "ACTIVE")
      assert(evaluator.evalBool())

      val evaluator2 = new ExpressionEvaluator("name > 'John'")
      evaluator2.setStringValue("name", "Mary")
      assert(evaluator2.evalBool())
    }

    "evaluate AND operations" in {
      val e1 = new ExpressionEvaluator("a > 5 && b < 10")
      e1.setValue("a", 7)
      e1.setValue("b", 8)
      assert(e1.evalBool())

      val e2 = new ExpressionEvaluator("a > 5 && b < 10")
      e2.setValue("a", 3)
      e2.setValue("b", 8)
      assert(!e2.evalBool())

      val e3 = new ExpressionEvaluator("a > 5 && b < 10")
      e3.setValue("a", 7)
      e3.setValue("b", 15)
      assert(!e3.evalBool())
    }

    "evaluate OR operations" in {
      val e1 = new ExpressionEvaluator("a > 100 || b = 5")
      e1.setValue("a", 10)
      e1.setValue("b", 5)
      assert(e1.evalBool())

      val e2 = new ExpressionEvaluator("a > 100 || b = 5")
      e2.setValue("a", 150)
      e2.setValue("b", 10)
      assert(e2.evalBool())

      val e3 = new ExpressionEvaluator("a > 100 || b = 5")
      e3.setValue("a", 10)
      e3.setValue("b", 10)
      assert(!e3.evalBool())
    }

    "evaluate NOT operations" in {
      val e1 = new ExpressionEvaluator("!(a = 5)")
      e1.setValue("a", 10)
      assert(e1.evalBool())

      val e2 = new ExpressionEvaluator("!(a = 5)")
      e2.setValue("a", 5)
      assert(!e2.evalBool())

      val e3 = new ExpressionEvaluator("!(a > 5)")
      e3.setValue("a", 3)
      assert(e3.evalBool())
    }

    "evaluate double NOT operations" in {
      val e1 = new ExpressionEvaluator("!!(a = 5)")
      e1.setValue("a", 5)
      assert(e1.evalBool())

      val e2 = new ExpressionEvaluator("!!(a = 5)")
      e2.setValue("a", 10)
      assert(!e2.evalBool())
    }

    "evaluate complex boolean expressions" in {
      val e1 = new ExpressionEvaluator("(a > 5 && b < 10) || c = 100")
      e1.setValue("a", 3)
      e1.setValue("b", 20)
      e1.setValue("c", 100)
      assert(e1.evalBool())

      val e2 = new ExpressionEvaluator("(a > 5 && b < 10) || c = 100")
      e2.setValue("a", 7)
      e2.setValue("b", 8)
      e2.setValue("c", 50)
      assert(e2.evalBool())

      val e3 = new ExpressionEvaluator("(a > 5 && b < 10) || c = 100")
      e3.setValue("a", 3)
      e3.setValue("b", 20)
      e3.setValue("c", 50)
      assert(!e3.evalBool())
    }

    "respect operator precedence" in {
      val e1 = new ExpressionEvaluator("a = 1 || b = 2 && c = 3")
      e1.setValue("a", 0)
      e1.setValue("b", 2)
      e1.setValue("c", 3)
      assert(e1.evalBool()) // (a=1) || ((b=2) && (c=3)) = false || (true && true) = true

      val e2 = new ExpressionEvaluator("a = 1 || b = 2 && c = 3")
      e2.setValue("a", 0)
      e2.setValue("b", 2)
      e2.setValue("c", 0)
      assert(!e2.evalBool()) // (a=1) || ((b=2) && (c=3)) = false || (true && false) = false
    }

    "combine string and numeric comparisons" in {
      val e = new ExpressionEvaluator("status = 'ACTIVE' && amount > 100")
      e.setStringValue("status", "ACTIVE")
      e.setValue("amount", 150)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("status = 'ACTIVE' && amount > 100")
      e2.setStringValue("status", "INACTIVE")
      e2.setValue("amount", 150)
      assert(!e2.evalBool())
    }

    "throw error on type mismatch in comparison" in {
      val e = new ExpressionEvaluator("a > 'string'")
      e.setValue("a", 5)
      val ex = intercept[ExprSyntaxError] {
        e.evalBool()
      }
      assert(ex.getMessage.contains("Cannot compare"))
    }

    "throw error on single & character" in {
      val ex = intercept[ExprSyntaxError] {
        new ExpressionEvaluator("a & b")
      }
      assert(ex.getMessage.contains("'&&'"))
    }

    "throw error on single | character" in {
      val ex = intercept[ExprSyntaxError] {
        new ExpressionEvaluator("a | b")
      }
      assert(ex.getMessage.contains("'||'"))
    }

    "throw error on unterminated string" in {
      val ex = intercept[ExprSyntaxError] {
        new ExpressionEvaluator("'unterminated")
      }
      assert(ex.getMessage.contains("Unterminated string"))
    }

    // Null literal tests
    "evaluate null comparisons" in {
      assert(new ExpressionEvaluator("null = null").evalBool())
      assert(!new ExpressionEvaluator("null != null").evalBool())
      assert(!new ExpressionEvaluator("'test' = null").evalBool())
      assert(new ExpressionEvaluator("'test' != null").evalBool())
      assert(!new ExpressionEvaluator("5 = null").evalBool())
      assert(new ExpressionEvaluator("5 != null").evalBool())
    }

    "evaluate null with variables" in {
      val e = new ExpressionEvaluator("a = 'test' || a = null")
      e.setStringValue("a", "test")
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("a = 'test' || a = null")
      e2.setStringValue("a", "other")
      assert(!e2.evalBool())
    }

    "allow setting variable to null via setStringValue" in {
      val e = new ExpressionEvaluator("a = null")
      e.setStringValue("a", null)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("a != null")
      e2.setStringValue("a", null)
      assert(!e2.evalBool())
    }

    "allow setting variable to null via setValue with Integer" in {
      val e = new ExpressionEvaluator("a = null")
      e.setValue("a", null: java.lang.Integer)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("a != null")
      e2.setValue("a", null: java.lang.Integer)
      assert(!e2.evalBool())
    }

    "allow setting variable to null via setNullValue" in {
      val e = new ExpressionEvaluator("a = null")
      e.setNullValue("a")
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("a = 'test' || a = null")
      e2.setNullValue("a")
      assert(e2.evalBool())

      val e3 = new ExpressionEvaluator("a = 'test' || a = null")
      e3.setStringValue("a", "test")
      assert(e3.evalBool())

      val e4 = new ExpressionEvaluator("a = 'test' || a = null")
      e4.setStringValue("a", "test1")
      assert(!e4.evalBool())
    }

    "allow overwriting variable value with null" in {
      val e = new ExpressionEvaluator("a = null")
      e.setStringValue("a", "test")
      assert(!e.evalBool())

      val e2 = new ExpressionEvaluator("a = null")
      e2.setStringValue("a", "test")
      e2.setStringValue("a", null)
      assert(e2.evalBool())
    }

    "use null variable in in() function" in {
      val e = new ExpressionEvaluator("in(a, 'X', null, 'Y')")
      e.setNullValue("a")
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("in(a, 'X', 'Y', 'Z')")
      e2.setNullValue("a")
      assert(!e2.evalBool())
    }

    "support NULL in any case" in {
      assert(new ExpressionEvaluator("NULL = null").evalBool())
      assert(new ExpressionEvaluator("Null = null").evalBool())
    }

    // 'in' function tests
    "evaluate in() function with strings" in {
      val e = new ExpressionEvaluator("in(a, 'A', 'B', 'F')")
      e.setStringValue("a", "A")
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("in(a, 'A', 'B', 'F')")
      e2.setStringValue("a", "B")
      assert(e2.evalBool())

      val e3 = new ExpressionEvaluator("in(a, 'A', 'B', 'F')")
      e3.setStringValue("a", "C")
      assert(!e3.evalBool())
    }

    "evaluate in() function with integers" in {
      val e = new ExpressionEvaluator("in(b, 1, 23, 55)")
      e.setValue("b", 23)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("in(b, 1, 23, 55)")
      e2.setValue("b", 100)
      assert(!e2.evalBool())
    }

    "evaluate in() function with literal value" in {
      assert(new ExpressionEvaluator("in('X', 'A', 'B', 'X')").evalBool())
      assert(!new ExpressionEvaluator("in('Y', 'A', 'B', 'X')").evalBool())
      assert(new ExpressionEvaluator("in(5, 1, 5, 10)").evalBool())
      assert(!new ExpressionEvaluator("in(7, 1, 5, 10)").evalBool())
    }

    "evaluate in() function with null" in {
      assert(new ExpressionEvaluator("in(null, 'A', null, 'B')").evalBool())
      assert(!new ExpressionEvaluator("in(null, 'A', 'B', 'C')").evalBool())
    }

    "throw error for in() with less than 2 arguments" in {
      val e = new ExpressionEvaluator("in(a)")
      e.setValue("a", 10)
      val ex = intercept[ExprSyntaxError] {
        e.evalBool()
      }
      assert(ex.getMessage.contains("at least 2 arguments"))
    }

    // 'if' function tests
    "evaluate if() function returning integer" in {
      val e = new ExpressionEvaluator("if(a > 2, 5, 1)")
      e.setValue("a", 10)
      assert(e.evalInt() == 5)

      val e2 = new ExpressionEvaluator("if(a > 2, 5, 1)")
      e2.setValue("a", 1)
      assert(e2.evalInt() == 1)
    }

    "evaluate if() function returning string" in {
      val e = new ExpressionEvaluator("if(a = 'yes', 'approved', 'rejected')")
      e.setStringValue("a", "yes")
      assert(e.evalString() == "approved")

      val e2 = new ExpressionEvaluator("if(a = 'yes', 'approved', 'rejected')")
      e2.setStringValue("a", "no")
      assert(e2.evalString() == "rejected")
    }

    "evaluate nested if() functions" in {
      val e = new ExpressionEvaluator("if(a > 10, 100, if(a > 5, 50, 10))")
      e.setValue("a", 15)
      assert(e.evalInt() == 100)

      val e2 = new ExpressionEvaluator("if(a > 10, 100, if(a > 5, 50, 10))")
      e2.setValue("a", 7)
      assert(e2.evalInt() == 50)

      val e3 = new ExpressionEvaluator("if(a > 10, 100, if(a > 5, 50, 10))")
      e3.setValue("a", 3)
      assert(e3.evalInt() == 10)
    }

    "evaluate if() with complex condition" in {
      val e = new ExpressionEvaluator("if(a > 5 && b < 10, 1, 0)")
      e.setValue("a", 7)
      e.setValue("b", 8)
      assert(e.evalInt() == 1)

      val e2 = new ExpressionEvaluator("if(a > 5 && b < 10, 1, 0)")
      e2.setValue("a", 3)
      e2.setValue("b", 8)
      assert(e2.evalInt() == 0)
    }

    "throw error for if() without exactly 3 arguments" in {
      val ex = intercept[ExprSyntaxError] {
        val e = new ExpressionEvaluator("if(a > 2, 5)")
        e.setValue("a", 3)
        e.evalInt()
      }
      assert(ex.getMessage.contains("exactly 3 arguments"))
    }

    // Combined tests
    "combine in() and if() functions" in {
      val e = new ExpressionEvaluator("if(in(status, 'A', 'B'), 100, 0)")
      e.setStringValue("status", "A")
      assert(e.evalInt() == 100)

      val e2 = new ExpressionEvaluator("if(in(status, 'A', 'B'), 100, 0)")
      e2.setStringValue("status", "C")
      assert(e2.evalInt() == 0)
    }

    // Boolean literal tests
    "evaluate true and false literals" in {
      assert(new ExpressionEvaluator("true").evalBool())
      assert(!new ExpressionEvaluator("false").evalBool())
      assert(new ExpressionEvaluator("TRUE").evalBool())
      assert(!new ExpressionEvaluator("FALSE").evalBool())
    }

    "use boolean literals in boolean operations" in {
      assert(new ExpressionEvaluator("true && true").evalBool())
      assert(!new ExpressionEvaluator("true && false").evalBool())
      assert(new ExpressionEvaluator("true || false").evalBool())
      assert(!new ExpressionEvaluator("false || false").evalBool())
    }

    "use NOT with boolean literals" in {
      assert(!new ExpressionEvaluator("!true").evalBool())
      assert(new ExpressionEvaluator("!false").evalBool())
      assert(new ExpressionEvaluator("!!true").evalBool())
    }

    "use boolean literals in if() function" in {
      val e = new ExpressionEvaluator("if(a > 10, true, false)")
      e.setValue("a", 15)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("if(a > 10, true, false)")
      e2.setValue("a", 5)
      assert(!e2.evalBool())
    }

    "use boolean literals with mixed expressions in if()" in {
      val e = new ExpressionEvaluator("if(a > 10, true, b < 100)")
      e.setValue("a", 5)
      e.setValue("b", 50)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("if(a > 10, true, b < 100)")
      e2.setValue("a", 5)
      e2.setValue("b", 200)
      assert(!e2.evalBool())

      val e3 = new ExpressionEvaluator("if(a > 10, true, b < 100)")
      e3.setValue("a", 15)
      e3.setValue("b", 200)
      assert(e3.evalBool())
    }

    "combine boolean literals with comparisons" in {
      val e = new ExpressionEvaluator("(a = 5 && true) || false")
      e.setValue("a", 5)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("(a = 5 && false) || false")
      e2.setValue("a", 5)
      assert(!e2.evalBool())
    }

    "return boolean literal from if() based on condition" in {
      val e = new ExpressionEvaluator("if(status = 'OK', true, false)")
      e.setStringValue("status", "OK")
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("if(status = 'OK', true, false)")
      e2.setStringValue("status", "ERROR")
      assert(!e2.evalBool())
    }

    "use boolean literals in nested if() functions" in {
      val e = new ExpressionEvaluator("if(a > 10, true, if(a > 5, false, true))")
      e.setValue("a", 15)
      assert(e.evalBool())

      val e2 = new ExpressionEvaluator("if(a > 10, true, if(a > 5, false, true))")
      e2.setValue("a", 7)
      assert(!e2.evalBool())

      val e3 = new ExpressionEvaluator("if(a > 10, true, if(a > 5, false, true))")
      e3.setValue("a", 3)
      assert(e3.evalBool())
    }

    "evaluate expressions with boolean logical operations" in {
      val e = new ExpressionEvaluator("(a > 5) && (b < 10)")
      e.setValue("a", 10)
      e.setValue("b", 5)
      assert(e.evalBool()) // both true, so true = true

      val e2 = new ExpressionEvaluator("(a > 5) || (b < 10)")
      e2.setValue("a", 10)
      e2.setValue("b", 15)
      assert(e2.evalBool()) // true || false => true
    }
  }
}
