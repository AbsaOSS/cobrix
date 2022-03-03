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

package za.co.absa.cobrix.cobol.parser.antlr
import org.antlr.v4.runtime.{BailErrorStrategy, CharStreams, CommonTokenStream}
import za.co.absa.cobrix.cobol.internal.Logging

import scala.collection.JavaConverters._


class ParserJson extends Logging {

  def parse(text: String): Any = {
    val visitor = new ParserJsonVisitor()
    val charStream = CharStreams.fromString(text)
    val lexer = new jsonLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(new LogErrorListener(logger))

    val tokens = new CommonTokenStream(lexer)
    val parser = new jsonParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(new LogErrorListener(logger))
    parser.setErrorHandler(new BailErrorStrategy())

    visitor.visit(parser.json())
  }

  def parseMap(text: String): Map[String, Any] = {
    this.parse(text).asInstanceOf[Map[String, Any]]
  }
}


class ParserJsonVisitor extends jsonBaseVisitor[Any] {
  /**
   * Visit a parse tree produced by {@link jsonParser#pair}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitPair(ctx: jsonParser.PairContext): Any = {
    val key = ctx.STRING().getText
    val value = this.visit(ctx.value)
    (key.substring(1, key.length - 1), value)
  }

  /**
   * Visit a parse tree produced by the {@code value_string}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_string(ctx: jsonParser.Value_stringContext): Any = {
    val text = ctx.getText
    text.substring(1, text.length - 1)
  }

  /**
   * Visit a parse tree produced by the {@code value_number}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_number(ctx: jsonParser.Value_numberContext): Any = {
    val text = ctx.getText
    try { text.toInt } catch { case _: Throwable => text.toDouble }
  }

  /**
   * Visit a parse tree produced by the {@code value_obj}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_obj(ctx: jsonParser.Value_objContext): Any = {
    ctx.obj().pair().asScala.map(x => this.visit(x).asInstanceOf[(String, Any)]).toMap
  }

  /**
   * Visit a parse tree produced by the {@code value_array}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_array(ctx: jsonParser.Value_arrayContext): Any = {
    ctx.arr.value.asScala.map(x => this.visit(x)).toArray
  }

  /**
   * Visit a parse tree produced by the {@code value_true}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_true(ctx: jsonParser.Value_trueContext): Any = true

  /**
   * Visit a parse tree produced by the {@code value_false}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_false(ctx: jsonParser.Value_falseContext): Any = false

  /**
   * Visit a parse tree produced by the {@code value_null}
   * labeled alternative in {@link jsonParser#value}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  override def visitValue_null(ctx: jsonParser.Value_nullContext): Any = null
}
