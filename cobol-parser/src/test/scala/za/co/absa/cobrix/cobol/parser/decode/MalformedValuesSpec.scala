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

package za.co.absa.cobrix.cobol.parser.decode

import org.scalatest.FunSuite
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.datatype.{CobolType, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Primitive}
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC

class MalformedValuesSpec extends FunSuite {

  test("Test out of bounds integer handling") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(7)  COMP.
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents, dropGroupFillers = false)
    val primitive = copybook.ast.head.children.head.asInstanceOf[Primitive]

    // Encoded 8405184 is OK for Int32 and PIC 9(7)
    val data1 = Array(0x00.toByte, 0x80.toByte, 0x40.toByte, 0xC0.toByte)
    val decodedValue1 = primitive.decodeTypeValue(0, data1)
    assert(decodedValue1.asInstanceOf[Int] == 8405184)

    // Encoded 3263185088 is bigger than Int32 and it is invalid 9(7) since the number of digits is 10, not 7. Should return null
    val data2 = Array(0xC2.toByte, 0x80.toByte, 0x40.toByte, 0xC0.toByte)
    val decodedValue2 = primitive.decodeTypeValue(0, data2)
    assert(decodedValue2 == null)
 }

  test("Test malformed decimal handling") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(5)V9(5).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(EBCDIC(), copyBookContents, dropGroupFillers = false)
    val primitive = copybook.ast.head.children.head.asInstanceOf[Primitive]

    // Encoded 12345.12345 is OK for Decimal and PIC 9(5)V9(5)
    val data1 = Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte)
    val decodedValue1 = primitive.decodeTypeValue(0, data1)
    assert(decodedValue1 == 12345.12345)

    // Encoded 12345.1234k is not a valid Decimal, should return null
    val data2 = Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0x93.toByte)
    val decodedValue2 = primitive.decodeTypeValue(0, data2)
    assert(decodedValue2 == null)

    // Not enough data for PIC 9(5)V9(5), should return null
    val data3 = Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte)
    val decodedValue3 = primitive.decodeTypeValue(0, data3)
    assert(decodedValue3 == null)
  }
}
