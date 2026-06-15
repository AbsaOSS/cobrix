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

package za.co.absa.cobrix.cobol.parser.decoders

import org.scalatest.wordspec.AnyWordSpec

class FloatingPointDecodersSpec extends AnyWordSpec {

  def assertFloatEqual(a: Float, b: Float): Unit = {
    assert(Math.abs(a - b) < 0.00001, s"($a != $b)")
  }

  def assertDoubleEqual(a: Double, b: Double): Unit = {
    assert(Math.abs(a - b) < 0.0000000001, s"($a != $b)")
  }

  "decodeIbmSingleBigEndian()" should {
    "decode IBM single precision / big-endian FP number 1.0" in {
      val bytes = Array[Byte](
        0x41.toByte, 0x10.toByte, 0x00.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), 1.0f)
    }

    "decode IBM single precision / big-endian FP number 1234.0" in {
      val bytes = Array[Byte](
        0x43.toByte, 0x4D.toByte, 0x20.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), 1234.0f)
    }

    "decode IBM single precision / big-endian FP number -1234.0" in {
      val bytes = Array[Byte](
        0xC3.toByte, 0x4D.toByte, 0x20.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), -1234.0f)
    }

    "decode IBM single precision / big-endian FP number 4.5" in {
      val bytes = Array[Byte](
        0x41.toByte, 0x48.toByte, 0x00.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), 4.5f)
    }

    "decode IBM single precision / big-endian FP number -3.75" in {
      val bytes = Array[Byte](
        0xC1.toByte, 0x3C.toByte, 0x00.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), -3.75f)
    }

    "decode IBM single precision / big-endian FP number 2.5" in {
      val bytes = Array[Byte](
        0x41.toByte, 0x28.toByte, 0x00.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), 2.5f)
    }

    "decode IBM single precision / big-endian FP number 0" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), 0f)
    }

    "decode IBM single precision / big-endian FP number infinity" in {
      val bytes = Array[Byte](
        0x7F.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)
      assert(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes).isInfinite)
    }
  }

  "decodeIbmDoubleBigEndian()" should {
    "decode IBM double precision / big-endian FP numbers" in {
      val bytes = Array[Byte](
        0x43.toByte, 0x14.toByte, 0x2E.toByte, 0xFC.toByte,
        0xCA.toByte, 0xF7.toByte, 0x09.toByte, 0xB7.toByte)
      assertDoubleEqual(FloatingPointDecoders.decodeIbmDoubleBigEndian(bytes), 322.936717)
    }

    "decode IBM double precision / big-endian FP vary small numbers" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte,
        0xCA.toByte, 0xF7.toByte, 0x09.toByte, 0xB7.toByte)
      assertDoubleEqual(FloatingPointDecoders.decodeIbmDoubleBigEndian(bytes), 4.08114837E-85)
    }
  }

  "decodeIbmSingleLittleEndian()" should {
    "decode IBM single precision / little-endian FP number 1.0" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x00.toByte, 0x10.toByte, 0x41.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleLittleEndian(bytes), 1.0f)
    }

    "decode IBM single precision / little-endian FP number 1234.0" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x20.toByte, 0x4D.toByte, 0x43.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleLittleEndian(bytes), 1234.0f)
    }

    "decode IBM single precision / little-endian FP number -1234.0" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x20.toByte, 0x4D.toByte, 0xC3.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleLittleEndian(bytes), -1234.0f)
    }

    "decode IBM single precision / little-endian FP number 4.5" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x00.toByte, 0x48.toByte, 0x41.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleLittleEndian(bytes), 4.5f)
    }

    "decode IBM single precision / little-endian FP number -3.75" in {
      val bytes = Array[Byte](
        0x00.toByte, 0x00.toByte, 0x3C.toByte, 0xC1.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleLittleEndian(bytes), -3.75f)
    }

    "decode IBM single precision / big-endian FP number infinity" in {
      val bytes = Array[Byte](
        0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0x7F.toByte)
      assert(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes).isInfinite)
    }
  }

  "decodeIbmDoubleLittleEndian()" should {
    "decode IBM double precision / little-endian FP numbers" in {
      val bytes = Array[Byte](
        0xB7.toByte, 0x09.toByte, 0xF7.toByte, 0xCA.toByte,
        0xFC.toByte, 0x2E.toByte, 0x14.toByte, 0x43.toByte)
      assertDoubleEqual(FloatingPointDecoders.decodeIbmDoubleLittleEndian(bytes), 322.936717)
    }
  }

  "decodeIeee754SingleBigEndian()" should {
    "decode IEEE754 single precision / big-endian FP numbers" in {
      val bytes = Array[Byte](0x40.toByte, 0x49.toByte, 0x0F.toByte, 0xDA.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIeee754SingleBigEndian(bytes), 3.1415925f)
    }
  }

  "decodeIeee754DoubleBigEndian()" should {
    "decode IEEE754 double precision / big-endian FP numbers" in {
      val bytes = Array[Byte](
        0x40.toByte, 0x09.toByte, 0x21.toByte, 0xFB.toByte,
        0x54.toByte, 0x44.toByte, 0x2E.toByte, 0xEA.toByte)
      assertDoubleEqual(FloatingPointDecoders.decodeIeee754DoubleBigEndian(bytes), 3.14159265359)
    }
  }

  "decodeIeee754SingleLittleEndian()" should {
    "decode IEEE754 single precision / little-endian FP numbers" in {
      val bytes = Array[Byte](0xDA.toByte, 0x0F.toByte, 0x49.toByte, 0x40.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIeee754SingleLittleEndian(bytes), 3.1415925f)
    }
  }

  "decodeIeee754DoubleLittleEndian()" should {
    "decode IEEE754 double precision / little-endian FP numbers" in {
      val bytes = Array[Byte](
        0xEA.toByte, 0x2E.toByte, 0x44.toByte, 0x54.toByte,
        0xFB.toByte, 0x21.toByte, 0x09.toByte, 0x40.toByte)
      assertDoubleEqual(FloatingPointDecoders.decodeIeee754DoubleLittleEndian(bytes), 3.14159265359)
    }
  }

}
