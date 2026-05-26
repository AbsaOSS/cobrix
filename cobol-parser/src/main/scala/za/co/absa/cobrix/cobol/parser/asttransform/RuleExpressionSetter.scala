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

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.expression.ExpressionEvaluator

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class RuleExpressionSetter(
                            redefineRuleExpressions: Map[String, ExpressionEvaluator]
                          ) extends AstTransformer {
  private val log = LoggerFactory.getLogger(this.getClass)

  /**
    * Sets isDependee attribute for fields in the schema which are used by other fields in DEPENDING ON clause
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    val ruleDrivenRedefines = redefineRuleExpressions.keys.toSet
    val dependeeFields = redefineRuleExpressions.values.flatMap { expr =>
      expr.getVariables
    }.toSet

    def markRuleForGroup(group: Group): Group = {
      val newChildren = markRuleFields(group)
      val newGroup = if (redefineRuleExpressions contains group.name) {
        group.copy(children = newChildren.children, ruleExpression = redefineRuleExpressions.get(group.name))(group.parent)
      } else {
        group.copy(children = newChildren.children)(group.parent)
      }
      newGroup
    }

    def markRuleFields(group: CopybookAST): CopybookAST = {
      val newChildren = for (field <- group.children) yield {
        val newField: Statement = field match {
          case grp: Group           => markRuleForGroup(grp)
          case primitive: Primitive =>
            val newPrimitive1 = if (redefineRuleExpressions contains primitive.name) {
              primitive.withUpdatedRuleExpression(redefineRuleExpressions.get(primitive.name))
            } else {
              primitive
            }
            val newPrimitive2 = if (dependeeFields contains primitive.name) {
              newPrimitive1.withUpdatedIsUsedInRules(newIsUsedInRules = true)
            } else {
              newPrimitive1
            }
            newPrimitive2
        }
        newField
      }
      group.copy(children = newChildren)(group.parent)
    }

    markRuleFields(ast)
  }
}

object RuleExpressionSetter {
  def apply(redefineRuleExpressions: Map[String, ExpressionEvaluator]): RuleExpressionSetter = new RuleExpressionSetter(redefineRuleExpressions)
}
