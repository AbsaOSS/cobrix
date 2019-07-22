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

package za.co.absa.cobrix.cobol.parser.decoders

import scodec.Codec
import scodec.bits.BitVector

import scala.util.control.NonFatal

object FloatingPointDecoders {
  private val floatB: Codec[Float] = scodec.codecs.float
  private val floatL: Codec[Float] = scodec.codecs.floatL
  private val doubleB: Codec[Double] = scodec.codecs.double
  private val doubleL: Codec[Double] = scodec.codecs.doubleL

  /** Decode IEEE754 single precision big endian encoded number. */
  def decodeIeee754SingleBigEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      floatB.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IEEE754 double precision big endian encoded number. */
  def decodeIeee754DoubleBigEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      doubleB.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IEEE754 single precision little endian encoded number. */
  def decodeIeee754SingleLittleEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      floatL.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IEEE754 double precision little endian encoded number. */
  def decodeIeee754DoubleLittleEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      doubleL.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IBM single precision big endian encoded number. */
  def decodeIbmSingleBigEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      floatB.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IBM double precision big endian encoded number. */
  def decodeIbmDoubleBigEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      doubleB.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IBM single precision little endian encoded number. */
  def decodeIbmSingleLittleEndian(bytes: Array[Byte]): java.lang.Float = {
    try {
      floatL.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

  /** Decode IBM double precision little endian encoded number. */
  def decodeIbmDoubleLittleEndian(bytes: Array[Byte]): java.lang.Double = {
    try {
      doubleL.decode(BitVector(bytes)).require.value
    } catch {
      case NonFatal(_) => null
    }
  }

}
