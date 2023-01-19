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

class NumExprBuilderImpl(vars: Map[String, Int], expr: String) extends NumExprBuilder {
  val ops = new ListBuffer[String]
  val values = new ListBuffer[Int]

  override def openParen(pos: Int): Unit = ops += "("

  override def closeParen(pos: Int): Unit = {
    if (ops.isEmpty) {
      if (values.size != 1) {
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

  override def addVariable(name: String, pos: Int): Unit = {
    if (!vars.contains(name)) {
      throw new ExprSyntaxError(s"Unset variable '$name' used.")
    } else {
      values += vars(name)
    }
  }

  override def addFunction(name: String, pos: Int): Unit = {
    ops += name
  }

  override def addNumLiteral(num: Int, pos: Int): Unit = {
    values += num
  }

  def getResult: Int = {
    while (ops.nonEmpty) {
      eval()
    }
    if (values.isEmpty) {
      throw new ExprSyntaxError(s"Empty expressions are not supported in '$expr'.")
    } else if (values.size > 1) {
      throw new ExprSyntaxError(s"Malformed expression: '$expr'.")
    } else {
      values.head match {
        case n: Int => n
        case n => throw new ExprSyntaxError(s"Unexpected type of '$n' which is the result of: '$expr'.")
      }
    }
  }

  @tailrec
  private def eval(): Unit = {
    val op = ops.last
    ops.remove(ops.size - 1)

    op match {
      case "(" => if (ops.nonEmpty && ops.last != "(") eval()
      case "+" =>
        expectArguments(2)
        val b = getInt
        val a = getInt
        values += a + b
      case "-" =>
        expectArguments(2)
        val b = getInt
        val a = getInt
        values += a - b
      case "*" =>
        expectArguments(2)
        val b = getInt
        val a = getInt
        values += a * b
      case "/" =>
        expectArguments(2)
        val b = getInt
        val a = getInt
        values += a / b
      case f => throw new ExprSyntaxError(s"Unsupported function '$f' in '$expr'.")
    }
  }

  private def expectArguments(n: Int): Unit = {
    if (values.size < n)
      throw new ExprSyntaxError(s"Expected more arguments in '$expr'.")
  }

  private def getInt: Int = {
    val a = values.last
    values.remove(values.size - 1)
    a match {
      case n: Int => n
      case x => throw new ExprSyntaxError(s"Expected a number, got $x in '$expr'.")
    }
  }
}
