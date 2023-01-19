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

package za.co.absa.cobrix.cobol.parser.expression

import za.co.absa.cobrix.cobol.parser.expression.lexer.Lexer
import za.co.absa.cobrix.cobol.parser.expression.parser.{ExtractVariablesBuilder, NumExprBuilderImpl, Parser}

import scala.collection.mutable

/**
  * This class evaluates numeric expressions.
  * Expressions can use variables that have '@' prefix.
  *
  * Example:
  *   {{{
  *   val evaluator = new NumberExprEvaluator("record_length + offset - 1")
  *   evaluator.setValue("record_length", 500)
  *   evaluator.setValue("offset", 50)
  *
  *   assert(evaluator.eval() == 549)
  *   }}}
  */
class NumberExprEvaluator(expr: String) {
  private val tokens = new Lexer(expr).lex()

  private val vars = mutable.HashMap[String, Int]()

  def setValue(varName: String, value: Int): Unit = {
    vars += varName -> value
  }

  def getVariables: Seq[String] = {
    val exprBuilder = new ExtractVariablesBuilder(expr)
    Parser.parse(tokens, exprBuilder)

    exprBuilder.getResult
  }

  def eval(): Int = {
    val exprBuilder = new NumExprBuilderImpl(vars.toMap, expr)
    Parser.parse(tokens, exprBuilder)

    exprBuilder.getResult
  }
}
