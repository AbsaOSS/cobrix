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
  *   val expr = new NumberExprEvaluator
  *   expr.setValue("record_length", 500)
  *   expr.setValue("offset", 50)
  *
  *   assert(expr.evalDate("@record_length + @offset - 1") == 549)
  *   }}}
  */
class NumberExprEvaluator {
  private val vars = mutable.HashMap[String, Int]()

  def setValue(varName: String, value: Int): Unit = {
    vars += varName -> value
  }

  def getVariables(expression: String): Seq[String] = {
    val tokens = new Lexer(expression).lex()

    val exprBuilder = new ExtractVariablesBuilder(expression)
    new Parser(tokens, exprBuilder).parse()

    exprBuilder.getResult
  }

  def eval(expression: String): Int = {
    val tokens = new Lexer(expression).lex()

    val exprBuilder = new NumExprBuilderImpl(vars.toMap, expression)
    new Parser(tokens, exprBuilder).parse()

    exprBuilder.getResult
  }
}
