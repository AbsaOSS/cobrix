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

package za.co.absa.cobrix.cobol.parser.encoding

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointDecoders

import java.nio.{ByteBuffer, ByteOrder}

class FloatingPointEncodersSuite extends AnyWordSpec {
  private def floatToBigEndianBytes(f: Float): Array[Byte] = {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    buf.putFloat(f)
    buf.array()
  }

  private def floatToLittleEndianBytes(f: Float): Array[Byte] = {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    buf.putFloat(f)
    buf.array()
  }

  private def doubleToBigEndianBytes(d: Double): Array[Byte] = {
    val buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
    buf.putDouble(d)
    buf.array()
  }

  private def doubleToLittleEndianBytes(d: Double): Array[Byte] = {
    val buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
    buf.putDouble(d)
    buf.array()
  }

  "encodeIeee754SingleBigEndian" should {
    "encode 0.0f" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(0.0f)
      assert(result.length == 4)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode 1.0f" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(1.0f)
      assert(result.sameElements(Array(0x3F.toByte, 0x80.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode -1.0f" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(-1.0f)
      assert(result.sameElements(Array(0xBF.toByte, 0x80.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode positive infinity" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(Float.PositiveInfinity)
      assert(result.sameElements(Array(0x7F.toByte, 0x80.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode negative infinity" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(Float.NegativeInfinity)
      assert(result.sameElements(Array(0xFF.toByte, 0x80.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode NaN" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(Float.NaN)
      assert(result.sameElements(floatToBigEndianBytes(Float.NaN)))
    }

    "round-trip with decode" in {
      val original = 3.14f
      val encoded = FloatingPointEncoders.encodeIeee754SingleBigEndian(original)
      val decoded = FloatingPointDecoders.decodeFloatB(encoded)
      assert(decoded == original)
    }

    "encode Float.MaxValue" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(Float.MaxValue)
      assert(result.sameElements(floatToBigEndianBytes(Float.MaxValue)))
    }

    "encode Float.MinPositiveValue" in {
      val result = FloatingPointEncoders.encodeIeee754SingleBigEndian(Float.MinPositiveValue)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x01.toByte)))
    }
  }

  "encodeIeee754SingleLittleEndian" should {
    "encode 0.0f" in {
      val result = FloatingPointEncoders.encodeIeee754SingleLittleEndian(0.0f)
      assert(result.length == 4)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode 1.0f" in {
      val result = FloatingPointEncoders.encodeIeee754SingleLittleEndian(1.0f)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x80.toByte, 0x3F.toByte)))
    }

    "encode -1.0f" in {
      val result = FloatingPointEncoders.encodeIeee754SingleLittleEndian(-1.0f)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x80.toByte, 0xBF.toByte)))
    }

    "round-trip with decode" in {
      val original = -2.718f
      val encoded = FloatingPointEncoders.encodeIeee754SingleLittleEndian(original)
      val decoded = FloatingPointDecoders.decodeFloatL(encoded)
      assert(decoded == original)
    }

    "encode positive infinity" in {
      val result = FloatingPointEncoders.encodeIeee754SingleLittleEndian(Float.PositiveInfinity)
      assert(result.sameElements(floatToLittleEndianBytes(Float.PositiveInfinity)))
    }

    "encode negative infinity" in {
      val result = FloatingPointEncoders.encodeIeee754SingleLittleEndian(Float.NegativeInfinity)
      assert(result.sameElements(floatToLittleEndianBytes(Float.NegativeInfinity)))
    }
  }

  "encodeIeee754DoubleBigEndian" should {
    "encode 0.0" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(0.0)
      assert(result.length == 8)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode 1.0" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(1.0)
      assert(result.sameElements(Array(0x3F.toByte, 0xF0.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode -1.0" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(-1.0)
      assert(result.sameElements(Array(0xBF.toByte, 0xF0.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode positive infinity" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(Double.PositiveInfinity)
      assert(result.sameElements(Array(0x7F.toByte, 0xF0.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode negative infinity" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(Double.NegativeInfinity)
      assert(result.sameElements(Array(0xFF.toByte, 0xF0.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode NaN" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(Double.NaN)
      assert(result.sameElements(doubleToBigEndianBytes(Double.NaN)))
    }

    "round-trip with decode" in {
      val original = 3.141592653589793
      val encoded = FloatingPointEncoders.encodeIeee754DoubleBigEndian(original)
      val decoded = FloatingPointDecoders.decodeDoubleB(encoded)
      assert(decoded == original)
    }

    "encode Double.MaxValue" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(Double.MaxValue)
      assert(result.sameElements(doubleToBigEndianBytes(Double.MaxValue)))
    }

    "encode Double.MinPositiveValue" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleBigEndian(Double.MinPositiveValue)
      assert(result.sameElements(doubleToBigEndianBytes(Double.MinPositiveValue)))
    }
  }

  "encodeIeee754DoubleLittleEndian" should {
    "encode 0.0" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleLittleEndian(0.0)
      assert(result.length == 8)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)))
    }

    "encode 1.0" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleLittleEndian(1.0)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0xF0.toByte, 0x3F.toByte)))
    }

    "encode -1.0" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleLittleEndian(-1.0)
      assert(result.sameElements(Array(0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0xF0.toByte, 0xBF.toByte)))
    }

    "round-trip with decode" in {
      val original = -2.718281828459045
      val encoded = FloatingPointEncoders.encodeIeee754DoubleLittleEndian(original)
      val decoded = FloatingPointDecoders.decodeDoubleL(encoded)
      assert(decoded == original)
    }

    "encode positive infinity" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleLittleEndian(Double.PositiveInfinity)
      assert(result.sameElements(doubleToLittleEndianBytes(Double.PositiveInfinity)))
    }

    "encode negative infinity" in {
      val result = FloatingPointEncoders.encodeIeee754DoubleLittleEndian(Double.NegativeInfinity)
      assert(result.sameElements(doubleToLittleEndianBytes(Double.NegativeInfinity)))
    }
  }

  "encodeIbmSingleBigEndian" should {
    "encode 0.0f as all zeros" in {
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(0.0f)
      assert(result.length == 4)
      assert(result.sameElements(Array[Byte](0, 0, 0, 0)))
    }

    "encode NaN as all 0xFF bytes" in {
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(Float.NaN)
      assert(result.sameElements(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)))
    }

    "encode positive infinity as 0x7FFFFFFF" in {
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(Float.PositiveInfinity)
      assert(result.sameElements(Array[Byte](0x7F.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)))
    }

    "encode negative infinity as all 0xFF bytes" in {
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(Float.NegativeInfinity)
      assert(result.sameElements(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)))
    }

    "encode 1.0f correctly" in {
      // 1.0 in IBM single: sign=0, exp=65 (0x41), fraction = 0.1 hex = 0x100000
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(1.0f)
      assert(result(0) == 0x41.toByte)
      assert(result(1) == 0x10.toByte)
      assert(result(2) == 0x00.toByte)
      assert(result(3) == 0x00.toByte)
    }

    "encode -1.0f with sign bit set" in {
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(-1.0f)
      assert((result(0) & 0x80) != 0) // sign bit set
      assert((result(0) & 0x7F) == 0x41) // exponent same as positive
    }

    "encode 0.5f correctly" in {
      // 0.5 in IBM single: sign=0, exp=64 (0x40), fraction = 0.8 hex = 0x800000
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(0.5f)
      assert(result(0) == 0x40.toByte)
      assert(result(1) == 0x80.toByte)
      assert(result(2) == 0x00.toByte)
      assert(result(3) == 0x00.toByte)
    }

    "encode 16.0f correctly" in {
      // 16.0 in IBM single: sign=0, exp=66 (0x42), fraction = 0.1 hex = 0x100000
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(16.0f)
      assert(result(0) == 0x42.toByte)
      assert(result(1) == 0x10.toByte)
      assert(result(2) == 0x00.toByte)
      assert(result(3) == 0x00.toByte)
    }

    "produce 4 bytes output" in {
      val result = FloatingPointEncoders.encodeIbmSingleBigEndian(42.0f)
      assert(result.length == 4)
    }
  }

  "encodeIbmDoubleBigEndian" should {
    "encode 0.0 as all zeros" in {
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(0.0)
      assert(result.length == 8)
      assert(result.sameElements(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0)))
    }

    "encode NaN as all 0xFF bytes" in {
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(Double.NaN)
      assert(result.sameElements(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)))
    }

    "encode positive infinity as 0x7FFFFFFFFFFFFFFF" in {
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(Double.PositiveInfinity)
      assert(result.sameElements(Array[Byte](0x7F.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)))
    }

    "encode negative infinity as all 0xFF bytes" in {
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(Double.NegativeInfinity)
      assert(result.sameElements(Array[Byte](0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)))
    }

    "encode 1.0 correctly" in {
      // 1.0 in IBM double: sign=0, exp=65 (0x41), fraction starts with 0x10...
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(1.0)
      assert(result(0) == 0x41.toByte)
      assert(result(1) == 0x10.toByte)
      assert(result(2) == 0x00.toByte)
      assert(result(3) == 0x00.toByte)
      assert(result(4) == 0x00.toByte)
      assert(result(5) == 0x00.toByte)
      assert(result(6) == 0x00.toByte)
      assert(result(7) == 0x00.toByte)
    }

    "encode -1.0 with sign bit set" in {
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(-1.0)
      assert((result(0) & 0x80) != 0) // sign bit set
      assert((result(0) & 0x7F) == 0x41) // exponent same as positive
    }

    "encode 0.5 correctly" in {
      // 0.5 in IBM double: sign=0, exp=64 (0x40), fraction = 0x80...
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(0.5)
      assert(result(0) == 0x40.toByte)
      assert(result(1) == 0x80.toByte)
      // remaining bytes should be 0
      assert(result(2) == 0x00.toByte)
      assert(result(3) == 0x00.toByte)
      assert(result(4) == 0x00.toByte)
      assert(result(5) == 0x00.toByte)
      assert(result(6) == 0x00.toByte)
      assert(result(7) == 0x00.toByte)
    }

    "produce 8 bytes output" in {
      val result = FloatingPointEncoders.encodeIbmDoubleBigEndian(42.0)
      assert(result.length == 8)
    }
  }

  "encodeIbmSingleLittleEndian" should {
    "encode 0.0f as all zeros" in {
      val result = FloatingPointEncoders.encodeIbmSingleLittleEndian(0.0f)
      assert(result.sameElements(Array[Byte](0, 0, 0, 0)))
    }

    "be the reverse of big-endian encoding" in {
      val bigEndian = FloatingPointEncoders.encodeIbmSingleBigEndian(1.0f)
      val littleEndian = FloatingPointEncoders.encodeIbmSingleLittleEndian(1.0f)
      assert(littleEndian.sameElements(bigEndian.reverse))
    }

    "be the reverse of big-endian encoding for negative values" in {
      val bigEndian = FloatingPointEncoders.encodeIbmSingleBigEndian(-3.14f)
      val littleEndian = FloatingPointEncoders.encodeIbmSingleLittleEndian(-3.14f)
      assert(littleEndian.sameElements(bigEndian.reverse))
    }

    "produce 4 bytes output" in {
      val result = FloatingPointEncoders.encodeIbmSingleLittleEndian(42.0f)
      assert(result.length == 4)
    }
  }

  "encodeIbmDoubleLittleEndian" should {
    "encode 0.0 as all zeros" in {
      val result = FloatingPointEncoders.encodeIbmDoubleLittleEndian(0.0)
      assert(result.sameElements(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0)))
    }

    "be the reverse of big-endian encoding" in {
      val bigEndian = FloatingPointEncoders.encodeIbmDoubleBigEndian(1.0)
      val littleEndian = FloatingPointEncoders.encodeIbmDoubleLittleEndian(1.0)
      assert(littleEndian.sameElements(bigEndian.reverse))
    }

    "be the reverse of big-endian encoding for negative values" in {
      val bigEndian = FloatingPointEncoders.encodeIbmDoubleBigEndian(-3.141592653589793)
      val littleEndian = FloatingPointEncoders.encodeIbmDoubleLittleEndian(-3.141592653589793)
      assert(littleEndian.sameElements(bigEndian.reverse))
    }

    "produce 8 bytes output" in {
      val result = FloatingPointEncoders.encodeIbmDoubleLittleEndian(42.0)
      assert(result.length == 8)
    }
  }
}
