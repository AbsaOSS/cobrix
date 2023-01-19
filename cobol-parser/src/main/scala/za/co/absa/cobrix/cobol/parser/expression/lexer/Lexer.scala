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

package za.co.absa.cobrix.cobol.parser.expression.lexer

import za.co.absa.cobrix.cobol.parser.expression.exception.ExprSyntaxError
import za.co.absa.cobrix.cobol.parser.expression.lexer.Token._

import scala.collection.mutable.ListBuffer

class Lexer(expression: String) {
  private val digits = "0123456789"
  private val nameStartChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_"
  private val nameMidChars = nameStartChars + digits
  private val whitespaces = " \t"

  private var pos = 0
  private val tokens = new ListBuffer[Token]

  def lex(): Array[Token] = {
    pos = 0
    tokens.clear()

    while (pos < expression.length) {
      val ok = findOneCharTokens() || findWhiteSpace() || findName() || findNumLiteral()
      if (!ok) {
        throw new ExprSyntaxError(s"Unexpected character '${expression(pos)}' at position: $pos")
      }
    }
    tokens.toArray
  }

  def findOneCharTokens(): Boolean = {
    val c = expression(pos)

    val found: Option[Token] = c match {
      case ',' => Some(COMMA(pos))
      case '(' => Some(OPEN_PARAN(pos))
      case ')' => Some(CLOSE_PARAN(pos))
      case '+' => Some(PLUS(pos))
      case '-' => Some(MINUS(pos))
      case '*' => Some(MULT(pos))
      case '/' => Some(DIV(pos))
      case _ => None
    }

    found match {
      case Some(t) =>
        tokens += t
        pos += 1
        true
      case None =>
        false
    }
  }

  def findWhiteSpace(): Boolean = {
    var pos2 = pos
    while (pos2 < expression.length && whitespaces.contains(expression(pos2))) {
      pos2 += 1
    }
    if (pos == pos2) {
      false
    } else {
      pos = pos2
      true
    }
  }

  def findName(): Boolean = {
    if (!nameStartChars.contains(expression(pos))) {
      false
    } else {
      var pos2 = pos
      while (pos2 < expression.length && nameMidChars.contains(expression(pos2))) {
        pos2 += 1
      }
      val token = NAME(pos, expression.substring(pos, pos2))
      tokens += token
      pos = pos2
      true
    }
  }

  def findNumLiteral(): Boolean = {
    var pos2 = pos
    while (pos2 < expression.length && digits.contains(expression(pos2))) {
      pos2 += 1
    }
    if (pos == pos2) {
      false
    } else {
      val token = NUM_LITERAL(pos, expression.substring(pos, pos2))
      tokens += token
      pos = pos2
      true
    }
  }
}
