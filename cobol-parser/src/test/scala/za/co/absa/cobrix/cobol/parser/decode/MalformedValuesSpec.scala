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

package za.co.absa.cobrix.cobol.parser.decode

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}

class MalformedValuesSpec extends FunSuite {

  test("Test out of bounds integer handling") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD           PIC 9(7)  COMP.
        |""".stripMargin

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val primitive = firstRecord.children.head.asInstanceOf[Primitive]

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

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val primitive = firstRecord.children.head.asInstanceOf[Primitive]

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

  test("Test processing malformed unsigned numbers") {
    val copyBookContents: String =
      """        01  RECORD.
        |           10  FIELD1           PIC 9(2).
        |           10  FIELD2           PIC 9(6).
        |           10  FIELD3           PIC 9(10).
        |           10  FIELD4           PIC 9(5)V9(5).
        |           10  FIELD5           PIC S9(2).
        |           10  FIELD6           PIC S9(6).
        |           10  FIELD7           PIC S9(10).
        |           10  FIELD8           PIC S9(5)V9(5).
        |""".stripMargin

    val copybook = CopybookParser.parseTree(copyBookContents)
    val firstRecord = copybook.ast.children.head.asInstanceOf[Group]
    val field1 = firstRecord.children.head.asInstanceOf[Primitive]
    val field2 = firstRecord.children(1).asInstanceOf[Primitive]
    val field3 = firstRecord.children(2).asInstanceOf[Primitive]
    val field4 = firstRecord.children(3).asInstanceOf[Primitive]
    val field5 = firstRecord.children(4).asInstanceOf[Primitive]
    val field6 = firstRecord.children(5).asInstanceOf[Primitive]
    val field7 = firstRecord.children(6).asInstanceOf[Primitive]
    val field8 = firstRecord.children(7).asInstanceOf[Primitive]

    // Encoded 12 is OK since it is positive
    assert(field1.decodeTypeValue(0, Array(0xF1.toByte, 0xF2.toByte)) == 12)

    // Encoded -2 is incorrect since it is a negative number for an unsigned pattern
    assert(field1.decodeTypeValue(0, Array(0x60.toByte, 0xF2.toByte)) == null)

    // Encoded 123456 is OK since it is positive
    assert(field2.decodeTypeValue(0,
      Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte)) == 123456)

    // Encoded -23456 is incorrect since it is a negative number for an unsigned pattern
    assert(field2.decodeTypeValue(0,
      Array(0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte)) == null)

    // Encoded 1234567890 is OK since it is positive
    assert(field3.decodeTypeValue(0,
      Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == 1234567890)

    // Encoded -234567890 is incorrect since it is a negative number for an unsigned pattern
    assert(field3.decodeTypeValue(0,
      Array(0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == null)

    // Encoded 12345.67890 is OK since it is positive
    assert(field4.decodeTypeValue(0,
      Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == 12345.6789)

    // Encoded -2345.67890 is incorrect since it is a negative number for an unsigned pattern
    assert(field4.decodeTypeValue(0,
      Array(0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == null)

    // Encoded 12 is OK since it is positive
    assert(field5.decodeTypeValue(0, Array(0xF1.toByte, 0xF2.toByte)) == 12)

    // Encoded -2 is OK since the field is signed
    assert(field5.decodeTypeValue(0, Array(0x60.toByte, 0xF2.toByte)) == -2)

    // Encoded 123456 is OK
    assert(field6.decodeTypeValue(0,
      Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte)) == 123456)

    // Encoded -23456 is OK since the field is signed
    assert(field6.decodeTypeValue(0,
      Array(0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte)) == -23456)

    // Encoded 1234567890 is OK
    assert(field7.decodeTypeValue(0,
      Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == 1234567890)

    // Encoded -234567890 is OK since the field is signed
    assert(field7.decodeTypeValue(0,
      Array(0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == -234567890)

    // Encoded 12345.67890 is OK
    assert(field8.decodeTypeValue(0,
      Array(0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == 12345.6789)

    // Encoded -2345.67890 is OK since the field is signed
    assert(field8.decodeTypeValue(0,
      Array(0x60.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte,
        0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0xF0.toByte)) == -2345.6789)

  }

}
