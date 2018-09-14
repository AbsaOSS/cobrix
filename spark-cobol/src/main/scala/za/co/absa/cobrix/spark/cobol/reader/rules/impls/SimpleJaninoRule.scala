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

private[rules] class SimpleJaninoRule(fields: Seq[String], expression: RuleExpression, evaluator: RuleEvaluator) extends Rule {

  override def getFields(): Seq[String] = fields

  override def getResult(): String = expression.result

  override def eval(fieldsValues: Map[String,Any]): Boolean = ???
}
