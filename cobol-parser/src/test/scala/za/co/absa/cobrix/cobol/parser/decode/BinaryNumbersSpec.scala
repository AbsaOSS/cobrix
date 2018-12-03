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

import org.scalatest.{BeforeAndAfter, FunSuite}
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.decoders.BinaryUtils

import scala.util.Random

class BinaryNumbersSpec extends FunSuite with BeforeAndAfter {
  val numberOfTests = 10000
  val rand = new Random()

  before {
    rand.setSeed(100100100)
  }

  test("Test 1 byte binary numbers signed converter") {
    val arr = new Array[Byte](1)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = true, 0)
      val v2 = scodec.codecs.int8.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 1 byte binary numbers unsigned converter") {
    val arr = new Array[Byte](1)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = false, 0)
      val v2 = scodec.codecs.uint8.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 2 byte binary numbers signed big endian converter") {
    val arr0 = Array[Byte](-128, 0)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = true, signed = true, 0)
    val c1v0 = scodec.codecs.int16.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == Short.MinValue.toString)

    val arr1 = Array[Byte](127, -1)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = true, signed = true, 0)
    val c1v1 = scodec.codecs.int16.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Short.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = true, signed = true, 0)
    val c1v2 = scodec.codecs.int16.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "-1")

    val arr = new Array[Byte](2)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = true, 0)
      val v2 = scodec.codecs.int16.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 4 byte binary numbers signed big endian converter") {
    val arr0 = Array[Byte](-128, 0, 0, 0)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = true, signed = true, 0)
    val c1v0 = scodec.codecs.int32.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == Int.MinValue.toString)

    val arr1 = Array[Byte](127, -1, -1, -1)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = true, signed = true, 0)
    val c1v1 = scodec.codecs.int32.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Int.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1, -1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = true, signed = true, 0)
    val c1v2 = scodec.codecs.int32.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "-1")

    val arr = new Array[Byte](4)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = true, 0)
      val v2 = scodec.codecs.int32.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 8 byte binary numbers signed big endian converter") {
    val arr0 = Array[Byte](-128, 0, 0, 0, 0, 0, 0, 0)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = true, signed = true, 0)
    val c1v0 = scodec.codecs.int64.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == Long.MinValue.toString)

    val arr1 = Array[Byte](127, -1, -1, -1, -1, -1, -1, -1)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = true, signed = true, 0)
    val c1v1 = scodec.codecs.int64.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Long.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1, -1, -1, -1, -1, -1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = true, signed = true, 0)
    val c1v2 = scodec.codecs.int64.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "-1")

    val arr = new Array[Byte](8)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = true, 0)
      val v2 = scodec.codecs.int64.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 2 byte binary numbers signed little endian converter") {
    val arr0 = Array[Byte](0, -128)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = false, signed = true, 0)
    val c1v0 = scodec.codecs.int16L.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == Short.MinValue.toString)

    val arr1 = Array[Byte](-1, 127)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = false, signed = true, 0)
    val c1v1 = scodec.codecs.int16L.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Short.MaxValue.toString)

    val arr = new Array[Byte](2)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = false, signed = true, 0)
      val v2 = scodec.codecs.int16L.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 4 byte binary numbers signed little endian converter") {
    val arr0 = Array[Byte](0, 0, 0, -128)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = false, signed = true, 0)
    val c1v0 = scodec.codecs.int32L.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == Int.MinValue.toString)

    val arr1 = Array[Byte](-1, -1, -1, 127)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = false, signed = true, 0)
    val c1v1 = scodec.codecs.int32L.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Int.MaxValue.toString)

    val arr = new Array[Byte](4)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = false, signed = true, 0)
      val v2 = scodec.codecs.int32L.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 8 byte binary numbers signed little endian converter") {
    val arr0 = Array[Byte](0, 0, 0, 0, 0, 0, 0, -128)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = false, signed = true, 0)
    val c1v0 = scodec.codecs.int64L.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == Long.MinValue.toString)

    val arr1 = Array[Byte](-1, -1, -1, -1, -1, -1, -1, 127)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = false, signed = true, 0)
    val c1v1 = scodec.codecs.int64L.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Long.MaxValue.toString)

    val arr = new Array[Byte](8)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = false, signed = true, 0)
      val v2 = scodec.codecs.int64L.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 2 byte binary numbers unsigned big endian converter") {
    val arr0 = Array[Byte](-128, 0)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = true, signed = false, 0)
    val c1v0 = scodec.codecs.uint16.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == (-Short.MinValue.toLong).toString)

    val arr1 = Array[Byte](127, -1)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = true, signed = false, 0)
    val c1v1 = scodec.codecs.uint16.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Short.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = true, signed = false, 0)
    val c1v2 = scodec.codecs.uint16.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "65535")

    val arr = new Array[Byte](2)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = false, 0)
      val v2 = scodec.codecs.uint16.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 4 byte binary numbers unsigned big endian converter") {
    val arr0 = Array[Byte](-128, 0, 0, 0)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = true, signed = false, 0)
    val c1v0 = scodec.codecs.uint32.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == (-Int.MinValue.toLong).toString)

    val arr1 = Array[Byte](127, -1, -1, -1)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = true, signed = false, 0)
    val c1v1 = scodec.codecs.uint32.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Int.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1, -1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = true, signed = false, 0)
    val c1v2 = scodec.codecs.uint32.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "4294967295")

    val arr = new Array[Byte](4)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = true, signed = false, 0)
      val v2 = scodec.codecs.uint32.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 2 byte binary numbers unsigned little endian converter") {
    val arr0 = Array[Byte](0, -128)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = false, signed = false, 0)
    val c1v0 = scodec.codecs.uint16L.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == (-Short.MinValue.toLong).toString)

    val arr1 = Array[Byte](-1, 127)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = false, signed = false, 0)
    val c1v1 = scodec.codecs.uint16L.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Short.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = false, signed = false, 0)
    val c1v2 = scodec.codecs.uint16L.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "65535")

    val arr = new Array[Byte](2)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = false, signed = false, 0)
      val v2 = scodec.codecs.uint16L.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }

  test("Test 4 byte binary numbers unsigned little endian converter") {
    val arr0 = Array[Byte](0, 0, 0, -128)
    val c0v0 = BinaryUtils.decodeBinaryNumber(arr0, bigEndian = false, signed = false, 0)
    val c1v0 = scodec.codecs.uint32L.decode(BitVector(arr0)).require.value.toString
    assert(c0v0 == c1v0)
    assert(c0v0 == (-Int.MinValue.toLong).toString)

    val arr1 = Array[Byte](-1, -1, -1, 127)
    val c0v1 = BinaryUtils.decodeBinaryNumber(arr1, bigEndian = false, signed = false, 0)
    val c1v1 = scodec.codecs.uint32L.decode(BitVector(arr1)).require.value.toString
    assert(c0v1 == c1v1)
    assert(c0v1 == Int.MaxValue.toString)

    val arr2 = Array[Byte](-1, -1, -1, -1)
    val c0v2 = BinaryUtils.decodeBinaryNumber(arr2, bigEndian = false, signed = false, 0)
    val c1v2 = scodec.codecs.uint32L.decode(BitVector(arr2)).require.value.toString
    assert(c0v2 == c1v2)
    assert(c0v2 == "4294967295")

    val arr = new Array[Byte](4)
    var i = 0
    while (i < numberOfTests) {
      rand.nextBytes(arr)
      val v1 = BinaryUtils.decodeBinaryNumber(arr, bigEndian = false, signed = false, 0)
      val v2 = scodec.codecs.uint32L.decode(BitVector(arr)).require.value.toString
      assert(v1 == v2)
      i += 1
    }
  }
}
