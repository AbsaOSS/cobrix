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
import za.co.absa.cobrix.cobol.parser.expression.parser.{ExpressionBuilderImpl, ExtractVariablesBuilder, Parser}

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
class ExpressionEvaluator(val expr: String) extends Serializable {
  private val tokens = new Lexer(expr).lex()

  private val vars = mutable.HashMap[String, Int]()
  private val stringVars = mutable.HashMap[String, String]()
  private val nullVars = mutable.HashSet[String]()

  def setValue(varName: String, value: Int): Unit = {
    nullVars -= varName
    stringVars -= varName
    vars += varName -> value
  }

  def setValue(varName: String, value: java.lang.Integer): Unit = {
    if (value == null) {
      setNullValue(varName)
    } else {
      setValue(varName, value.intValue())
    }
  }

  def setStringValue(varName: String, value: String): Unit = {
    if (value == null) {
      setNullValue(varName)
    } else {
      nullVars -= varName
      vars -= varName
      stringVars += varName -> value
    }
  }

  def setNullValue(varName: String): Unit = {
    vars -= varName
    stringVars -= varName
    nullVars += varName
  }

  def getVariables: Seq[String] = {
    val exprBuilder = new ExtractVariablesBuilder(expr)
    Parser.parse(tokens, exprBuilder)

    exprBuilder.getResult
  }

  def evalInt(): Int = {
    val exprBuilder = new ExpressionBuilderImpl(vars.toMap, stringVars.toMap, nullVars.toSet, expr)
    Parser.parse(tokens, exprBuilder)

    val i = exprBuilder.getIntResult
    clearValues()
    i
  }

  def evalBool(): Boolean = {
    val exprBuilder = new ExpressionBuilderImpl(vars.toMap, stringVars.toMap, nullVars.toSet, expr)
    Parser.parse(tokens, exprBuilder)

    val b = exprBuilder.getBoolResult
    clearValues()
    b
  }

  def evalString(): String = {
    val exprBuilder = new ExpressionBuilderImpl(vars.toMap, stringVars.toMap, nullVars.toSet, expr)
    Parser.parse(tokens, exprBuilder)

    val s = exprBuilder.getStringResult
    clearValues()
    s
  }

  private def clearValues(): Unit = {
    vars.clear()
    stringVars.clear()
    nullVars.clear()
  }
}
