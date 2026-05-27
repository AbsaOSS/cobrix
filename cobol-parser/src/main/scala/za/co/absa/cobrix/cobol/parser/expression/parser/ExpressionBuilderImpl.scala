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

class ExpressionBuilderImpl(vars: Map[String, Int], stringVars: Map[String, String], nullVars: Set[String], expr: String) extends ExpressionBuilder {
  sealed trait ValueType
  case object IntType extends ValueType
  case object BoolType extends ValueType
  case object StringType extends ValueType
  case object NullType extends ValueType

  val ops = new ListBuffer[String]
  val valuesInt = new ListBuffer[Int]
  val valuesBool = new ListBuffer[Boolean]
  val valuesString = new ListBuffer[String]
  val valueTypes = new ListBuffer[ValueType]
  val argCounts = new ListBuffer[Int]  // Track argument counts for functions

  override def openParen(pos: Int): Unit = {
    ops += "("
    argCounts += 1  // Start counting arguments (at least 1 if not empty)
  }

  override def closeParen(pos: Int): Unit = {
    if (ops.isEmpty) {
      if (valuesInt.size != 1) {
        throw new ExprSyntaxError(s"Empty expression at $pos in '$expr'.")
      }
    } else {
      while (ops.last != "(") {
        eval()
      }
      ops.remove(ops.size - 1)  // Remove "("

      val currentArgCount = if (argCounts.nonEmpty) {
        val count = argCounts.last
        argCounts.remove(argCounts.size - 1)
        count
      } else 0

      // Check if there's a function to evaluate
      if (ops.nonEmpty && !isOperator(ops.last)) {
        evalFunction(ops.last, currentArgCount)
        ops.remove(ops.size - 1)
      }
    }
  }

  private def isOperator(s: String): Boolean = {
    s match {
      case "(" | "+" | "-" | "*" | "/" | "=" | "!=" | ">" | "<" | ">=" | "<=" | "&&" | "||" | "!" => true
      case _ => false
    }
  }

  override def addComma(pos: Int): Unit = {
    // Evaluate pending operators within the current parentheses
    while (ops.nonEmpty && ops.last != "(") {
      eval()
    }
    // Increment argument count for current function call
    if (argCounts.nonEmpty) {
      argCounts(argCounts.size - 1) += 1
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
    while (ops.nonEmpty && "+-*/=!><".contains(ops.last.head)) {
      eval()
    }
    ops += "="
  }

  override def addOperationGreaterThan(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=!><".contains(ops.last.head)) {
      eval()
    }
    ops += ">"
  }

  override def addOperationLessThan(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=!><".contains(ops.last.head)) {
      eval()
    }
    ops += "<"
  }

  override def addOperationGreaterThanOrEqual(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=!><".contains(ops.last.head)) {
      eval()
    }
    ops += ">="
  }

  override def addOperationLessThanOrEqual(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=!><".contains(ops.last.head)) {
      eval()
    }
    ops += "<="
  }

  override def addOperationNotEqual(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=!><".contains(ops.last.head)) {
      eval()
    }
    ops += "!="
  }

  override def addOperationAnd(pos: Int): Unit = {
    while (ops.nonEmpty && "+-*/=!><&".contains(ops.last.head)) {
      eval()
    }
    ops += "&&"
  }

  override def addOperationOr(pos: Int): Unit = {
    while (ops.nonEmpty && ops.last != "||" && ops.last != "(") {
      eval()
    }
    ops += "||"
  }

  override def addOperationNot(pos: Int): Unit = {
    ops += "!"
  }

  override def addVariable(name: String, pos: Int): Unit = {
    if (nullVars.contains(name)) {
      valueTypes += NullType
    } else if (vars.contains(name)) {
      valuesInt += vars(name)
      valueTypes += IntType
    } else if (stringVars.contains(name)) {
      valuesString += stringVars(name)
      valueTypes += StringType
    } else {
      throw new ExprSyntaxError(s"Unset variable '$name' used.")
    }
  }

  override def addFunction(name: String, pos: Int): Unit = {
    ops += name
  }

  override def addNumLiteral(num: Int, pos: Int): Unit = {
    valuesInt += num
    valueTypes += IntType
  }

  override def addStringLiteral(s: String, pos: Int): Unit = {
    valuesString += s
    valueTypes += StringType
  }

  override def addNullLiteral(pos: Int): Unit = {
    valueTypes += NullType
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
    if (valuesInt.isEmpty && valuesBool.isEmpty && valuesString.isEmpty) {
      throw new ExprSyntaxError(s"Empty expressions are not supported in '$expr'.")
    } else if (valuesBool.isEmpty) {
      throw new ExprSyntaxError(s"The expression does not return a boolean in '$expr'.")
    } else if (valuesBool.size > 1 || (valuesInt.nonEmpty && valuesBool.nonEmpty) || (valuesString.nonEmpty && valuesBool.nonEmpty)) {
      throw new ExprSyntaxError(s"Malformed expression: '$expr'.")
    } else {
      valuesBool.head
    }
  }

  def getStringResult: String = {
    while (ops.nonEmpty) {
      eval()
    }
    if (valuesInt.isEmpty && valuesBool.isEmpty && valuesString.isEmpty) {
      throw new ExprSyntaxError(s"Empty expressions are not supported in '$expr'.")
    } else if (valuesString.isEmpty) {
      throw new ExprSyntaxError(s"The expression does not return a string in '$expr'.")
    } else if (valuesString.size > 1 || (valuesInt.nonEmpty && valuesString.nonEmpty) || (valuesBool.nonEmpty && valuesString.nonEmpty)) {
      throw new ExprSyntaxError(s"Malformed expression: '$expr'.")
    } else {
      valuesString.head
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
        valueTypes.remove(valueTypes.size - 1)
        valueTypes.remove(valueTypes.size - 1)
        val b = getInt
        val a = getInt
        valuesInt += a + b
        valueTypes += IntType
      case "-" =>
        expectIntArguments(2)
        valueTypes.remove(valueTypes.size - 1)
        valueTypes.remove(valueTypes.size - 1)
        val b = getInt
        val a = getInt
        valuesInt += a - b
        valueTypes += IntType
      case "*" =>
        expectIntArguments(2)
        valueTypes.remove(valueTypes.size - 1)
        valueTypes.remove(valueTypes.size - 1)
        val b = getInt
        val a = getInt
        valuesInt += a * b
        valueTypes += IntType
      case "/" =>
        expectIntArguments(2)
        valueTypes.remove(valueTypes.size - 1)
        valueTypes.remove(valueTypes.size - 1)
        val b = getInt
        val a = getInt
        valuesInt += a / b
        valueTypes += IntType
      case "=" =>
        val (b, a, vType) = getTwoComparableValues()
        valuesBool += a == b
        valueTypes += BoolType
      case "!=" =>
        val (b, a, vType) = getTwoComparableValues()
        valuesBool += a != b
        valueTypes += BoolType
      case ">" =>
        val (b, a, vType) = getTwoComparableValues()
        valuesBool += (vType match {
          case IntType => a.asInstanceOf[Int] > b.asInstanceOf[Int]
          case StringType => a.asInstanceOf[String] > b.asInstanceOf[String]
          case _ => throw new ExprSyntaxError(s"Cannot use > operator with $vType in '$expr'.")
        })
        valueTypes += BoolType
      case "<" =>
        val (b, a, vType) = getTwoComparableValues()
        valuesBool += (vType match {
          case IntType => a.asInstanceOf[Int] < b.asInstanceOf[Int]
          case StringType => a.asInstanceOf[String] < b.asInstanceOf[String]
          case _ => throw new ExprSyntaxError(s"Cannot use < operator with $vType in '$expr'.")
        })
        valueTypes += BoolType
      case ">=" =>
        val (b, a, vType) = getTwoComparableValues()
        valuesBool += (vType match {
          case IntType => a.asInstanceOf[Int] >= b.asInstanceOf[Int]
          case StringType => a.asInstanceOf[String] >= b.asInstanceOf[String]
          case _ => throw new ExprSyntaxError(s"Cannot use >= operator with $vType in '$expr'.")
        })
        valueTypes += BoolType
      case "<=" =>
        val (b, a, vType) = getTwoComparableValues()
        valuesBool += (vType match {
          case IntType => a.asInstanceOf[Int] <= b.asInstanceOf[Int]
          case StringType => a.asInstanceOf[String] <= b.asInstanceOf[String]
          case _ => throw new ExprSyntaxError(s"Cannot use <= operator with $vType in '$expr'.")
        })
        valueTypes += BoolType
      case "&&" =>
        expectBoolArguments(2)
        valueTypes.remove(valueTypes.size - 1)
        valueTypes.remove(valueTypes.size - 1)
        val b = getBool
        val a = getBool
        valuesBool += a && b
        valueTypes += BoolType
      case "||" =>
        expectBoolArguments(2)
        valueTypes.remove(valueTypes.size - 1)
        valueTypes.remove(valueTypes.size - 1)
        val b = getBool
        val a = getBool
        valuesBool += a || b
        valueTypes += BoolType
      case "!" =>
        expectBoolArguments(1)
        valueTypes.remove(valueTypes.size - 1)
        val a = getBool
        valuesBool += !a
        valueTypes += BoolType
      case f => throw new ExprSyntaxError(s"Unsupported function '$f' in '$expr'.")
    }
  }

  private def evalFunction(funcName: String, argCount: Int): Unit = {
    funcName.toLowerCase match {
      case "in" => evalInFunction(argCount)
      case "if" => evalIfFunction(argCount)
      case f => throw new ExprSyntaxError(s"Unsupported function '$f' in '$expr'.")
    }
  }

  private def evalInFunction(argCount: Int): Unit = {
    if (argCount < 2)
      throw new ExprSyntaxError(s"Function 'in' requires at least 2 arguments in '$expr'.")

    // Get the options (all arguments except the first one)
    val optionCount = argCount - 1
    val options = new ListBuffer[(Any, ValueType)]

    for (_ <- 0 until optionCount) {
      val vType = valueTypes.last
      valueTypes.remove(valueTypes.size - 1)
      val value: Any = vType match {
        case IntType => getInt
        case StringType => getString
        case NullType => null
        case BoolType => getBool
      }
      options.prepend((value, vType))
    }

    // Get the value to check
    val checkType = valueTypes.last
    valueTypes.remove(valueTypes.size - 1)
    val checkValue: Any = checkType match {
      case IntType => getInt
      case StringType => getString
      case NullType => null
      case BoolType => getBool
    }

    // Check if checkValue is in options
    val result = options.exists { case (optValue, optType) =>
      // Allow null comparisons
      if (checkType == NullType || optType == NullType) {
        checkValue == optValue
      } else if (checkType == optType) {
        checkValue == optValue
      } else {
        false
      }
    }

    valuesBool += result
    valueTypes += BoolType
  }

  private def evalIfFunction(argCount: Int): Unit = {
    if (argCount != 3)
      throw new ExprSyntaxError(s"Function 'if' requires exactly 3 arguments in '$expr'.")

    // Get false value (third argument)
    val falseType = valueTypes.last
    valueTypes.remove(valueTypes.size - 1)
    val falseValue: Any = falseType match {
      case IntType => getInt
      case StringType => getString
      case NullType => null
      case BoolType => getBool
    }

    // Get true value (second argument)
    val trueType = valueTypes.last
    valueTypes.remove(valueTypes.size - 1)
    val trueValue: Any = trueType match {
      case IntType => getInt
      case StringType => getString
      case NullType => null
      case BoolType => getBool
    }

    // Get condition (first argument)
    val condType = valueTypes.last
    valueTypes.remove(valueTypes.size - 1)
    if (condType != BoolType)
      throw new ExprSyntaxError(s"First argument of 'if' must be a boolean expression in '$expr'.")
    val condition = getBool

    // Return the appropriate value based on condition
    val resultType = if (condition) trueType else falseType
    val resultValue = if (condition) trueValue else falseValue

    resultType match {
      case IntType =>
        valuesInt += resultValue.asInstanceOf[Int]
        valueTypes += IntType
      case StringType =>
        valuesString += resultValue.asInstanceOf[String]
        valueTypes += StringType
      case BoolType =>
        valuesBool += resultValue.asInstanceOf[Boolean]
        valueTypes += BoolType
      case NullType =>
        valueTypes += NullType
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

  private def getString: String = {
    val a = valuesString.last
    valuesString.remove(valuesString.size - 1)
    a
  }

  private def expectBoolArguments(n: Int): Unit = {
    if (valuesBool.size < n)
      throw new ExprSyntaxError(s"Expected boolean arguments in '$expr'.")
  }

  private def getTwoComparableValues(): (Any, Any, ValueType) = {
    if (valueTypes.size < 2)
      throw new ExprSyntaxError(s"Expected more arguments in '$expr'.")

    val type2 = valueTypes.last
    valueTypes.remove(valueTypes.size - 1)
    val type1 = valueTypes.last
    valueTypes.remove(valueTypes.size - 1)

    // Handle null comparisons
    if (type1 == NullType || type2 == NullType) {
      val val1: Any = type1 match {
        case NullType => null
        case IntType => getInt
        case StringType => getString
        case BoolType => getBool
      }
      val val2: Any = type2 match {
        case NullType => null
        case IntType => getInt
        case StringType => getString
        case BoolType => getBool
      }
      return (val2, val1, NullType)
    }

    if (type1 != type2)
      throw new ExprSyntaxError(s"Cannot compare $type1 with $type2 in '$expr'.")

    type1 match {
      case IntType => (getInt, getInt, IntType)
      case StringType => (getString, getString, StringType)
      case BoolType => throw new ExprSyntaxError(s"Cannot compare boolean values in '$expr'.")
      case NullType => (null, null, NullType) // both nulls
    }
  }
}
