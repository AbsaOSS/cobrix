package za.co.absa.cobrix.cobol.parser.decode

import org.scalatest.FunSuite
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.datatype.{CobolType, Integer}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Statement}
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC

class MalformedValuesSpec extends FunSuite {

  test("Test out of bounds integer handling") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(7)  COMP.
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val statement = copybook.ast.head.children.head.asInstanceOf[Statement]

    // Encoded 8405184 is OK for Int32 and PIC 9(7)
    val data1 = BitVector(Array(0x00.toByte, 0x80.toByte, 0x40.toByte, 0xC0.toByte))
    val decodedValue1 = statement.decodeTypeValue(0, data1)
    assert(decodedValue1 == 8405184)

    // Encoded 3263185088 is bigger than Int32 and PIC 9(7), should return null
    val data2 = BitVector(Array(0xC2.toByte, 0x80.toByte, 0x40.toByte, 0xC0.toByte))
    val decodedValue2 = statement.decodeTypeValue(0, data2)
    assert(decodedValue2 == null)
 }

  test("Test malformed decimal handling") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(5)V9(5).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val statement = copybook.ast.head.children.head.asInstanceOf[Statement]

    // Encoded 12345.12345 is OK for Decimal and PIC 9(5)V9(5)
    val data1 = BitVector(Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte))
    val decodedValue1 = statement.decodeTypeValue(0, data1)
    assert(decodedValue1 == 12345.12345)

    // Encoded 12345.1234k is not a valid Decimal, should return null
    val data2 = BitVector(Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0x93.toByte))
    val decodedValue2 = statement.decodeTypeValue(0, data2)
    assert(decodedValue2 == null)
  }
}
