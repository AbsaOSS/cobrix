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
import za.co.absa.cobrix.cobol.parser.expression.lexer.Token
import za.co.absa.cobrix.cobol.parser.expression.lexer.Token._

import scala.collection.mutable.ListBuffer

class Parser(tokens: List[Token], builder: NumExprBuilder) {
  def parse(): Unit = {
    val STATE0 = 0
    val STATE1 = 1
    val STATE_VARIABLE = 2
    val MINUS_NUM = 3

    var state = STATE0

    val paranPos = new ListBuffer[Int]

    for (token <- tokens) {
      if (state == STATE0) {
        token match {
          case VAR_PREFIX(_) =>
            state = STATE_VARIABLE
          case COMMA(pos) =>
            throw new ExprSyntaxError(s"Unexpected ',' at pos $pos")
          case OPEN_PARAN(pos) =>
            paranPos += pos
            builder.openParen(pos)
          case CLOSE_PARAN(pos) =>
            if (paranPos.isEmpty) {
              throw new ExprSyntaxError(s"Unmatched ')' at pos $pos")
            }
            paranPos.remove(paranPos.size - 1)
            builder.closeParen(pos)
          case PLUS(pos) =>
            throw new ExprSyntaxError(s"Unexpected '+' at pos $pos")
          case MINUS(_) =>
            state = MINUS_NUM
          case NAME(pos, s) =>
            builder.addFunction(s, pos)
          case NUM_LITERAL(pos, s) =>
            builder.addNumLiteral(s.toInt, pos)
            state = STATE1
          case _ => new ExprSyntaxError(s"Unexpected '$token' at pos ${token.pos}")
        }
      } else if (state == STATE1) {
        token match {
          case VAR_PREFIX(pos) =>
            throw new ExprSyntaxError(s"Unexpected variable at pos $pos")
          case COMMA(_) =>
            state = STATE0
          case OPEN_PARAN(pos) =>
            paranPos += pos
            builder.openParen(pos)
            state = STATE0
          case CLOSE_PARAN(pos) =>
            if (paranPos.isEmpty) {
              throw new ExprSyntaxError(s"Unmatched ')' at pos $pos")
            }
            paranPos.remove(paranPos.size - 1)
            builder.closeParen(pos)
            state = STATE1
          case PLUS(pos) =>
            builder.addOperationPlus(pos)
            state = STATE0
          case MINUS(pos) =>
            builder.addOperationMinus(pos)
            state = STATE0
          case MULT(pos) =>
            builder.addOperationMultiply(pos)
            state = STATE0
          case DIV(pos) =>
            builder.addOperationDivide(pos)
            state = STATE0
          case NAME(pos, s) =>
            builder.addFunction(s, pos)
          case NUM_LITERAL(pos, s) =>
            builder.addNumLiteral(s.toInt, pos)
          case _ => new ExprSyntaxError(s"Unexpected '$token' at pos ${token.pos}")
        }
      } else if (state == STATE_VARIABLE) {
        token match {
          case NAME(pos, s) =>
            builder.addVariable(s, pos)
            state = STATE1
          case _ => new ExprSyntaxError(s"Unexpected '$token' at pos ${token.pos}")
        }
      } else if (state == MINUS_NUM) {
        token match {
          case OPEN_PARAN(pos) =>
            paranPos += pos
            builder.addOperationMinus(pos)
            builder.openParen(pos)
            state = STATE0
          case NAME(pos, s) =>
            builder.addOperationMinus(pos)
            builder.addFunction(s, pos)
            state = STATE0
          case NUM_LITERAL(pos, s) =>
            builder.addNumLiteral(-s.toInt, pos)
            state = STATE1
          case _ => new ExprSyntaxError(s"Unexpected '$token' at pos ${token.pos}")
        }
      }
    }

    if (paranPos.nonEmpty) {
      throw new ExprSyntaxError(s"Unmatched '(' at pos ${paranPos.head}")
    }
  }
}
