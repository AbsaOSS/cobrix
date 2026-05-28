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
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.exceptions.RuleExpressionParsingException
import za.co.absa.cobrix.cobol.parser.expression.ExpressionEvaluator

import scala.collection.mutable

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
    // Collect all field names from the schema
    val allFieldNames = collectAllFieldNames(ast)

    // Validate that all target fields exist in the schema
    val invalidTargets = redefineRuleExpressions.keys.toSet.diff(allFieldNames).toSeq.sorted
    if (invalidTargets.nonEmpty) {
      throw new RuleExpressionParsingException(
        None,
        msg = s"Target field(s) not found in schema: ${invalidTargets.mkString(", ")}"
      )
    }

    // Validate that all variables used in rule expressions exist in the schema
    val allVariables = redefineRuleExpressions.values.flatMap(_.getVariables).toSet
    val invalidVariables = allVariables.diff(allFieldNames).toSeq.sorted
    if (invalidVariables.nonEmpty) {
      val field = redefineRuleExpressions.find { case (_, expr) =>
        expr.getVariables.exists(invalidVariables.contains)
      }.map(_._1)
      throw new RuleExpressionParsingException(
        fieldOpt = field,
        msg = s"Rule expression variable(s) not found in schema: ${invalidVariables.mkString(", ")}"
      )
    }

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

  /**
    * Collects all field names from the AST recursively
    *
    * @param group The AST group to traverse
    * @return Set of all field names in the schema
    */
  private def collectAllFieldNames(group: CopybookAST): Set[String] = {
    val names = mutable.Set[String]()

    def collectNames(statement: Statement): Unit = {
      statement match {
        case grp: Group =>
          names += grp.name
          grp.children.foreach(collectNames)
        case primitive: Primitive =>
          names += primitive.name
      }
    }

    group.children.foreach(collectNames)
    names.toSet
  }
}

object RuleExpressionSetter {
  def apply(redefineRuleExpressions: Map[String, ExpressionEvaluator]): RuleExpressionSetter = new RuleExpressionSetter(redefineRuleExpressions)
}
