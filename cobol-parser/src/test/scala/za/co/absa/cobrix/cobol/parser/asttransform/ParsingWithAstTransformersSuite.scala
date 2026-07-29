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
import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.parser.exceptions.RuleExpressionParsingException
import za.co.absa.cobrix.cobol.parser.expression.ExpressionEvaluator

class ParsingWithAstTransformersSuite extends AnyWordSpec {
  "parse" should {
    "parse the copybook with a custom transformer" in {
      val copybook =
        """       01  RECORD.
          |           05  FIELD-A           PIC 9(5).
          |           05  FIELD-B           PIC 9(5).
          |           05  FIELD-C           PIC X(10).
          |""".stripMargin

      val transformers = Seq(new AstTransformerSpy)
      val schema = CopybookParser.parseTree(copybook, customAstTransformers = transformers)

      val fields = schema.ast.children.head.asInstanceOf[Group].children
      assert(fields.head.name == "FIELD_A_transformed")
      assert(fields(1).name == "FIELD_B_transformed")
      assert(fields(2).name == "FIELD_C_transformed")

      assert(transformers.head.transformCaller == 1)
    }
  }
}
