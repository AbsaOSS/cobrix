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

import java.nio.charset.Charset

import org.antlr.v4.runtime._
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.policies.CommentPolicy
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy


class ThrowErrorStrategy() extends DefaultErrorStrategy {
  override def recover(recognizer: Parser, e: RecognitionException): Unit = {
    throw new SyntaxErrorException(
      e.getOffendingToken.getLine,
      "",
      "Invalid input " + getTokenErrorDisplay(e.getOffendingToken) + " at position " + e.getOffendingToken.getLine
      + ":" + (e.getOffendingToken.getCharPositionInLine + 6)
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
            commentPolicy: CommentPolicy,
            ebcdicCodePage: CodePage,
            asciiCharset: Charset,
            floatingPointFormat: FloatingPointFormat): CopybookAST = {
    val visitor = new ParserVisitor(enc, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)

    val strippedContents = copyBookContents.split("\\r?\\n").map(
      line =>
        truncateComments(line, commentPolicy)
    ).mkString("\n")

    val charStream = CharStreams.fromString(strippedContents)
    val lexer = new copybookLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new copybookParser(tokens)
    parser.setErrorHandler(new ThrowErrorStrategy())

    visitor.visitMain(parser.main())
    visitor.ast
  }

  /**
    * Truncate all columns after configured (72th by default) one and
    * first configured (6 by default) columns (historically for line numbers)
    */
  private def truncateComments(copybookLine: String, commentPolicy: CommentPolicy): String = {
    if (commentPolicy.truncateComments) {
      if (commentPolicy.commentsUpToChar >= 0 && commentPolicy.commentsAfterChar >= 0) {
        copybookLine.slice(commentPolicy.commentsUpToChar, commentPolicy.commentsAfterChar)
      } else {
        if (commentPolicy.commentsUpToChar >= 0) {
          copybookLine.drop(commentPolicy.commentsUpToChar)
        } else {
          copybookLine.dropRight(commentPolicy.commentsAfterChar)
        }
      }
    } else {
      copybookLine
    }
  }
}