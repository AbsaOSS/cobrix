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

package za.co.absa.cobrix.cobol.parser.MemoryStream

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.stream.{SimpleMemoryStream, SimpleStream}

class MemoryStreamSpec extends FunSuite {

  test("Test memory stream reading capabilities") {

    val testSimpleStream: SimpleStream = new TestStringStream
    val testMemoryStream = new SimpleMemoryStream(testSimpleStream, 10)

    val arr1 = new Array[Byte](10)
    val num1 = testMemoryStream.getBytes(arr1, 0, 2)

    assert(num1 == 3)
    assert(new String(arr1.map(_.toChar)).startsWith("012"))

    val arr2 = new Array[Byte](10)
    val num2 = testMemoryStream.getBytes(arr2, 1, 5)

    assert(num2 == 5)
    assert(new String(arr2.map(_.toChar)).startsWith("12345"))

    val arr3 = new Array[Byte](10)
    val num3 = testMemoryStream.getBytes(arr3, 6, 12)

    assert(num3 == 7)
    assert(new String(arr3.map(_.toChar)).startsWith("6789ABC"))

    val arr4 = new Array[Byte](10)
    val num4 = testMemoryStream.getBytes(arr4, 8, 17)

    assert(num4 == 10)
    assert(new String(arr4.map(_.toChar)) == "89ABCDEFGH")

    val arr5 = new Array[Byte](10)
    val num5 = testMemoryStream.getBytes(arr5, 9, 18)

    assert(num5 == 10)
    assert(new String(arr5.map(_.toChar)) == "9ABCDEFGHI")

    val arr6 = new Array[Byte](10)
    val num6 = testMemoryStream.getBytes(arr6, 19, 28)

    assert(num6 == 10)
    assert(new String(arr6.map(_.toChar)) == "JKLMNOPQRS")

    val arr7 = new Array[Byte](10)
    val num7 = testMemoryStream.getBytes(arr7, 29, 38)

    assert(num7 == 7)
    assert(new String(arr7.map(_.toChar)).startsWith("TUVWXYZ"))

    val arr8 = new Array[Byte](10)
    val num8 = testMemoryStream.getBytes(arr8, 36, 38)

    assert(num8 == 0)
  }

  test("Test memory stream search capabilities") {
    val testSimpleStream: SimpleStream = new TestStringStream
    val testMemoryStream = new SimpleMemoryStream(testSimpleStream, 10)

    val searchBytes1 = "BCD".toCharArray.map(_.toByte)
    val searchBytes2 = "89".toCharArray.map(_.toByte)
    val ind1 = testMemoryStream.search(searchBytes1, 0)
    assert(ind1 == 11)
    val ind2 = testMemoryStream.search(searchBytes2, 7)
    assert(ind2 == 8)
  }

}
