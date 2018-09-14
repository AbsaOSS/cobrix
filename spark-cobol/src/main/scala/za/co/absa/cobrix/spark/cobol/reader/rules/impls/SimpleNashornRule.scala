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

package za.co.absa.cobrix.spark.cobol.reader.rules.impls

import za.co.absa.cobrix.spark.cobol.reader.rules.{Rule, RuleExpression}
import za.co.absa.cobrix.spark.cobol.reader.rules.evaluation.RuleEvaluator

/**
  * This class represents an expression evaluator that uses Oracle's Nashorn as its validation engine.
  *
  * The expressions are parsed as JavaScript.
  *
  * @param fields Seq[String] containing the fields present in the expression.
  * @param expression [[RuleExpression]] containing the full expression and the expected result in case the expression is true.
  * @param evaluator [[RuleEvaluator]] wrapping the evaluation engine.
  */
private[rules] class SimpleNashornRule(fields: Seq[String], expression: RuleExpression, evaluator: RuleEvaluator) extends Rule {

  override def getFields(): Seq[String] = fields

  override def getResult(): String = expression.result

  override def eval(fieldsValues: Map[String,Any]): Boolean = {
    val resolvedExpression = replaceFields(expression.expression, fields, fieldsValues)
    evaluator.eval(resolvedExpression)
  }

  private def replaceFields(expression: String, fields: Seq[String], fieldsValues: Map[String,Any]): String = {
    fields.length match {
      case c if c == 0 => expression
      case _ => {
        val replacement = fieldsValues(fields.head).toString
        replaceFields(expression.replaceAll(fields.head, replacement), fields.tail, fieldsValues)
      }
    }
  }
}