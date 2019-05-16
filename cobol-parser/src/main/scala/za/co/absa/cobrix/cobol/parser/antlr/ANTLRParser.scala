package za.co.absa.cobrix.cobol.parser.antlr

import org.antlr.v4.runtime.{BailErrorStrategy, CharStreams, CommonTokenStream, DefaultErrorStrategy, InputMismatchException, Parser, RecognitionException, Token}
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
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
            ebcdicCodePage: CodePage): CopybookAST = {
    val visitor = new ParserVisitor(enc, stringTrimmingPolicy, ebcdicCodePage)

    val charStream = CharStreams.fromString(copyBookContents)
    val lexer = new copybookLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new copybookParser(tokens)
    parser.setErrorHandler(new ThrowErrorStrategy())

    visitor.visitMain(parser.main())
    visitor.ast
  }
}