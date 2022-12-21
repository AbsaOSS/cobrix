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
    "decode IBM single precision / big-endian FP numbers" in {
      val bytes = Array[Byte](
        0x43.toByte, 0x14.toByte, 0x2E.toByte, 0xFC.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleBigEndian(bytes), 5.045883f)
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
    "decode IBM single precision / little-endian FP numbers" in {
      val bytes = Array[Byte](
        0xFC.toByte, 0x2E.toByte, 0x14.toByte, 0x43.toByte)
      assertFloatEqual(FloatingPointDecoders.decodeIbmSingleLittleEndian(bytes), 5.045883f)
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
