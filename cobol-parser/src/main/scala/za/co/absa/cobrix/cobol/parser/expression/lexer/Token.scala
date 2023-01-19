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

sealed trait Token {
  def pos: Int
}

object Token {
  case class VAR_PREFIX(pos: Int) extends Token
  {
    override def toString = "@"
  }

  case class COMMA(pos: Int) extends Token
  {
    override def toString = ","
  }

  case class OPEN_PARAN(pos: Int) extends Token
  {
    override def toString = "("
  }

  case class CLOSE_PARAN(pos: Int) extends Token
  {
    override def toString = ")"
  }

  case class PLUS(pos: Int) extends Token
  {
    override def toString = "+"
  }

  case class MINUS(pos: Int) extends Token
  {
    override def toString = "-"
  }

  case class MULT(pos: Int) extends Token {
    override def toString = "*"
  }

  case class DIV(pos: Int) extends Token {
    override def toString = "/"
  }

  case class NAME(pos: Int, s: String) extends Token
  {
    override def toString: String = s
  }

  case class NUM_LITERAL(pos: Int, s: String) extends Token
  {
    override def toString: String = s
  }
}
