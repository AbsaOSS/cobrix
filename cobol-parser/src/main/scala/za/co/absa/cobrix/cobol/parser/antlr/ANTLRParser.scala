package za.co.absa.cobrix.cobol.parser.antlr

import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST

object ANTLRParser {
  def parse(copyBookContents: String): CopybookAST = {
    val visitor = new ParserVisitor()

    val charStream = CharStreams.fromString(copyBookContents)
    val lexer = new copybook_lexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new copybook_parser(tokens)

    visitor.visitMain(parser.main())
    visitor.ast
  }
}