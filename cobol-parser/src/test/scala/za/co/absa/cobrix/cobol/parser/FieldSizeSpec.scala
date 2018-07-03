package za.co.absa.cobrix.cobol.parser

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Statement}
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC


class FieldSizeSpec extends FunSuite {
  val copyBookContents: String =
    """        01  RECORD.
      |           10  NUM1               PIC S9(2) USAGE COMP.
      |           10  DATE1              PIC X(10).
      |           10  DECIMAL-AMT        PIC S9(7)V9(2) USAGE COMP-3.
      |           10  DATE-TIME          PIC S9(4)V9(2) USAGE COMP-3.
      |           10  DECIMAL-NUM        PIC S9(15)V USAGE COMP-3.
      |""".stripMargin

  def fieldsize(index: Int, cpy: Copybook): Int = {
    val item = cpy.ast.head.children(index)
    val sizebits = item match {
      case grp: Group => grp.binaryProperties.actualSize
      case stat: Statement => stat.binaryProperties.actualSize
    }
    sizebits
  }

  test("Test field sizes are correctly calculated") {
    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val size_s9_2_comp = fieldsize(0, copybook)
    val size_x10 = fieldsize(1, copybook)
    val size_s9_7_v_9_2_comp3 = fieldsize(2, copybook)
    val size_s9_4_v_9_2_comp3 = fieldsize(3, copybook)
    val size_s9_15_v_comp3 = fieldsize(4, copybook)

    assert(size_s9_2_comp == 2 * 8)
    assert(size_x10 == 10 * 8)
    assert(size_s9_7_v_9_2_comp3 == 5 * 8)
    assert(size_s9_4_v_9_2_comp3 == 4 * 8)
    assert(size_s9_15_v_comp3 == 8 * 8)
  }
}