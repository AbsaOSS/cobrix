package za.co.absa.cobrix.cobol.parser.antlr

import org.antlr.v4.runtime.{BailErrorStrategy, CharStreams, CommonTokenStream}
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage

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
    parser.setErrorHandler(new BailErrorStrategy())

    visitor.visitMain(parser.main())
    visitor.ast
  }
}