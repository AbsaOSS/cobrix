package za.co.absa.cobrix.cobol.parser.decode

import org.scalatest.FunSuite
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.{Group, Statement}
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException

class UsageInheritanceSpec extends FunSuite {

  test("Test nested fields inherit group usage fields") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMP-3.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val statement = copybook.ast.head.children.head.asInstanceOf[Group].children.head.asInstanceOf[Statement]

    val dataType = statement.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integer].compact

    assert(compact.isDefined)
    assert(compact.get == 3)
  }

  test("Test alternative COMP type definition") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMPUTATIONAL-3.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val statement = copybook.ast.head.children.head.asInstanceOf[Group].children.head.asInstanceOf[Statement]

    val dataType = statement.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integer].compact

    assert(compact.isDefined)
    assert(compact.get == 3)
  }

  test("Test group COMPUTATIONAL USAGE") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMPUTATIONAL.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val statement = copybook.ast.head.children.head.asInstanceOf[Group].children.head.asInstanceOf[Statement]

    val dataType = statement.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integer].compact

    assert(compact.isDefined)
    assert(compact.get == 4)
  }

  test("Test default field USAGE") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP.
        |              15  FLD       PIC 9(7).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val statement = copybook.ast.head.children.head.asInstanceOf[Group].children.head.asInstanceOf[Statement]

    val dataType = statement.dataType
    val compact = dataType.asInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integer].compact

    assert(compact.isEmpty)
  }

  test("Test conflict between group's COMP and field's COMP values") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  GRP        COMP-3.
        |              15  FLD       PIC 9(7)  COMP.
        |""".stripMargin

    val syntaxErrorException = intercept[SyntaxErrorException] {
      CopybookParser.parseTree(EBCDIC(), copyBookContents)
    }

    assert(syntaxErrorException.lineNumber == 3)
    assert(syntaxErrorException.msg.contains("Field USAGE (COMP-4) doesn't match group's USAGE (COMP-3)"))
  }

}
