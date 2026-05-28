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

package za.co.absa.cobrix.cobol.parser.asttransform

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.exceptions.{RuleExpressionParsingException, SyntaxErrorException}
import za.co.absa.cobrix.cobol.parser.expression.ExpressionEvaluator

class RuleExpressionSetterSuite extends AnyWordSpec {
  "RuleExpressionSetter" should {
    "accept valid rule expressions with existing fields" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |           05  FIELD-C           PIC X(10).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "FIELD_C" -> new ExpressionEvaluator("FIELD_A > 100")
      )

      val setter = RuleExpressionSetter(rules)
      // Should not throw exception
      val result = setter.transform(schema.ast)
      assert(result != null)
    }

    "accept rule expressions with multiple variables" in {
      val copybook =
        """       01  RECORD.
          |           05  STATUS            PIC X(2).
          |           05  AMOUNT            PIC 9(5).
          |           05  PRIORITY          PIC 9(1).
          |           05  RESULT            PIC X(10).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "RESULT" -> new ExpressionEvaluator("STATUS = 'OK' && AMOUNT > 100")
      )

      val setter = RuleExpressionSetter(rules)
      // Should not throw exception
      val result = setter.transform(schema.ast)
      assert(result != null)
    }

    "throw exception when target field does not exist" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "FIELD-C" -> new ExpressionEvaluator("FIELD-A > 100")
      )

      val setter = RuleExpressionSetter(rules)
      val ex = intercept[RuleExpressionParsingException] {
        setter.transform(schema.ast)
      }
      assert(ex.getMessage.contains("Target field(s) not found in schema"))
      assert(ex.getMessage.contains("FIELD-C"))
    }

    "throw exception when variable in expression does not exist" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "FIELD_B" -> new ExpressionEvaluator("FIELD_X > 100")
      )

      val setter = RuleExpressionSetter(rules)
      val ex = intercept[RuleExpressionParsingException] {
        setter.transform(schema.ast)
      }
      assert(ex.getMessage.contains("variable(s) not found in schema"))
      assert(ex.getMessage.contains("FIELD_X"))
    }

    "throw exception for multiple invalid variables" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "FIELD_B" -> new ExpressionEvaluator("FIELD_X > 100 && FIELD_Y < 50")
      )

      val setter = RuleExpressionSetter(rules)
      val ex = intercept[RuleExpressionParsingException] {
        setter.transform(schema.ast)
      }
      assert(ex.getMessage.contains("variable(s) not found in schema"))
      assert(ex.getMessage.contains("FIELD_X"))
      assert(ex.getMessage.contains("FIELD_Y"))
    }

    "throw exception for multiple invalid target fields" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "FIELD-X" -> new ExpressionEvaluator("FIELD-A > 100"),
        "FIELD-Y" -> new ExpressionEvaluator("FIELD-B < 50")
      )

      val setter = RuleExpressionSetter(rules)
      val ex = intercept[RuleExpressionParsingException] {
        setter.transform(schema.ast)
      }
      assert(ex.getMessage.contains("Target field(s) not found in schema"))
      // Should contain both invalid targets
      assert(ex.getMessage.contains("FIELD-X"))
      assert(ex.getMessage.contains("FIELD-Y"))
    }

    "accept rules for nested fields" in {
      val copybook =
        """       01  RECORD.
          |           05  HEADER.
          |               10  STATUS        PIC X(2).
          |               10  CODE          PIC 9(3).
          |           05  BODY.
          |               10  AMOUNT        PIC 9(5).
          |               10  RESULT        PIC X(10).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "RESULT" -> new ExpressionEvaluator("STATUS = 'OK' && AMOUNT > 100")
      )

      val setter = RuleExpressionSetter(rules)
      // Should not throw exception
      val result = setter.transform(schema.ast)
      assert(result != null)
    }

    "accept rules with in() and if() functions" in {
      val copybook =
        """       01  RECORD.
          |           05  STATUS            PIC X(2).
          |           05  PRIORITY          PIC 9(1).
          |           05  RESULT            PIC X(10).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "RESULT" -> new ExpressionEvaluator("if(in(STATUS, 'OK', 'AC'), PRIORITY > 5, false)")
      )

      val setter = RuleExpressionSetter(rules)
      // Should not throw exception
      val result = setter.transform(schema.ast)
      assert(result != null)
    }

    "accept empty rule map" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map.empty[String, ExpressionEvaluator]

      val setter = RuleExpressionSetter(rules)
      // Should not throw exception
      val result = setter.transform(schema.ast)
      assert(result != null)
    }

    "accept rules with literals that don't reference variables" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC X(10).
          |""".stripMargin

      val schema = CopybookParser.parseTree(copybook)

      val rules = Map(
        "FIELD_B" -> new ExpressionEvaluator("100 > 50")
      )

      val setter = RuleExpressionSetter(rules)
      // Should not throw exception (no variables, just literals)
      val result = setter.transform(schema.ast)
      assert(result != null)
    }
  }
}
