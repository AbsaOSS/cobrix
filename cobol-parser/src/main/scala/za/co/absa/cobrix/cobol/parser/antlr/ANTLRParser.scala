/*
 * Copyright 2018-2019 ABSA Group Limited
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

import org.antlr.v4.runtime._
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException


class ThrowErrorStrategy() extends DefaultErrorStrategy {
  override def recover(recognizer: Parser, e: RecognitionException): Unit = {
    throw new SyntaxErrorException(
      e.getOffendingToken.getLine,
      "",
      "Invalid input " + getTokenErrorDisplay(e.getOffendingToken) + " at position " + e.getOffendingToken.getLine
      + ":" + e.getOffendingToken.getCharPositionInLine
    )
  }

  @throws[RecognitionException]
  override def recoverInline(recognizer: Parser): Token = {
    throw new InputMismatchException(recognizer)
  }

  override def sync(recognizer: Parser) = {}

}


object ANTLRParser {
  def parse(copyBookContents: String,
            enc: Encoding,
            stringTrimmingPolicy: StringTrimmingPolicy,
            ebcdicCodePage: CodePage,
            floatingPointFormat: FloatingPointFormat): CopybookAST = {
    val visitor = new ParserVisitor(enc, stringTrimmingPolicy, ebcdicCodePage, floatingPointFormat)

    val charStream = CharStreams.fromString(copyBookContents)
    val lexer = new copybookLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new copybookParser(tokens)
    parser.setErrorHandler(new ThrowErrorStrategy())

    visitor.visitMain(parser.main())
    visitor.ast
  }
}