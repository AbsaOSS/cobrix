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

package za.co.absa.cobrix.cobol.parser.expression.parser

import za.co.absa.cobrix.cobol.parser.expression.exception.ExprSyntaxError

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class ExpressionBuilderImpl(vars: Map[String, Int], expr: String) extends NumExprBuilder {
  val ops = new ListBuffer[String]
  val valuesInt = new ListBuffer[Int]
  val valuesBool = new ListBuffer[Boolean]

  override def openParen(pos: Int): Unit = ops += "("

  override def closeParen(pos: Int): Unit = {
    if (ops.isEmpty) {
      if (valuesInt.size != 1) {
        throw new ExprSyntaxError(s"Empty expression at $pos in '$expr'.")
      }
    } else {
      while (ops.last != "(") {
        eval()
      }
      ops.remove(ops.size - 1)
    }
  }

  override def addOperationPlus(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/".contains(ops.last)) {
      eval()
    }
    ops += "+"
  }

  override def addOperationMinus(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/".contains(ops.last)) {
      eval()
    }
    ops += "-"
  }

  override def addOperationMultiply(pos: Int): Unit = {
    while (ops.nonEmpty && "*/".contains(ops.last)) {
      eval()
    }
    ops += "*"
  }

  override def addOperationDivide(pos: Int): Unit = {
    while (ops.nonEmpty && "*/".contains(ops.last)) {
      eval()
    }
    ops += "/"
  }

  override def addOperationEquals(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=".contains(ops.last)) {
      eval()
    }
    ops += "="
  }

  override def addVariable(name: String, pos: Int): Unit = {
    if (!vars.contains(name)) {
      throw new ExprSyntaxError(s"Unset variable '$name' used.")
    } else {
      valuesInt += vars(name)
    }
  }

  override def addFunction(name: String, pos: Int): Unit = {
    ops += name
  }

  override def addNumLiteral(num: Int, pos: Int): Unit = {
    valuesInt += num
  }

  def getIntResult: Int = {
    while (ops.nonEmpty) {
      eval()
    }
    if (valuesInt.isEmpty && valuesBool.isEmpty) {
      throw new ExprSyntaxError(s"Empty expressions are not supported in '$expr'.")
    } else if (valuesInt.isEmpty) {
      throw new ExprSyntaxError(s"The expression does not return a number in '$expr'.")
    } else if (valuesInt.size > 1 || (valuesInt.nonEmpty && valuesBool.nonEmpty)) {
      throw new ExprSyntaxError(s"Malformed expression: '$expr'.")
    } else {
      valuesInt.head
    }
  }

  def getBoolResult: Boolean = {
    while (ops.nonEmpty) {
      eval()
    }
    if (valuesInt.isEmpty && valuesBool.isEmpty) {
      throw new ExprSyntaxError(s"Empty expressions are not supported in '$expr'.")
    } else if (valuesBool.isEmpty) {
      throw new ExprSyntaxError(s"The expression does not return a boolean in '$expr'.")
    } else if (valuesBool.size > 1 || (valuesInt.nonEmpty && valuesBool.nonEmpty)) {
      throw new ExprSyntaxError(s"Malformed expression: '$expr'.")
    } else {
      valuesBool.head
    }
  }

  @tailrec
  private def eval(): Unit = {
    val op = ops.last
    ops.remove(ops.size - 1)

    op match {
      case "(" => if (ops.nonEmpty && ops.last != "(") eval()
      case "+" =>
        expectIntArguments(2)
        val b = getInt
        val a = getInt
        valuesInt += a + b
      case "-" =>
        expectIntArguments(2)
        val b = getInt
        val a = getInt
        valuesInt += a - b
      case "*" =>
        expectIntArguments(2)
        val b = getInt
        val a = getInt
        valuesInt += a * b
      case "/" =>
        expectIntArguments(2)
        val b = getInt
        val a = getInt
        valuesInt += a / b
      case "=" =>
        expectIntArguments(2)
        val b = getInt
        val a = getInt
        valuesBool += a == b
      case f => throw new ExprSyntaxError(s"Unsupported function '$f' in '$expr'.")
    }
  }

  private def expectIntArguments(n: Int): Unit = {
    if (valuesInt.size < n)
      throw new ExprSyntaxError(s"Expected more arguments in '$expr'.")
  }

  private def getInt: Int = {
    val a = valuesInt.last
    valuesInt.remove(valuesInt.size - 1)
    a
  }

  private def getBool: Boolean = {
    val a = valuesBool.last
    valuesBool.remove(valuesBool.size - 1)
    a
  }
}
