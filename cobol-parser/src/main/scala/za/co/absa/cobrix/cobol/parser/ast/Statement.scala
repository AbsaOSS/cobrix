/*
 * Copyright 2018 Barclays Africa Group Limited
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

package za.co.absa.cobrix.cobol.parser.ast

import scodec.Codec
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.common.BinaryUtils._
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Decimal, Integer}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC

/** An abstraction of the leaves in the Cobol copybook
  *
  * @param level       A level for the statement
  * @param name        An identifier
  * @param lineNumber  An line number in the copybook
  * @param redefines   A name of a field which is redefined by this one
  * @param occurs      The number of elements in an fixed size array / minimum items in variable-sized array
  * @param to          The maximum number of items in a variable size array
  * @param dependingOn A field which specifies size of the array in a record
  * @param parent      A parent node
  */
case class Statement(
                      level: Int,
                      name: String,
                      lineNumber: Int,
                      dataType: CobolType,
                      redefines: Option[String] = None,
                      isRedefined: Boolean = false,
                      occurs: Option[Int] = None,
                      to: Option[Int] = None,
                      dependingOn: Option[String] = None,
                      isDependee: Boolean = false,
                      binaryProperties: BinaryProperties = BinaryProperties(0, 0, 0)
                    )
                    (val parent: Option[Group] = None)
  extends CBTree {

  /** Returns a string representation of the field */
  override def toString: String = {
    s"${" " * 2 * level}$camelCased ${camelCase(redefines.getOrElse(""))} $dataType"
  }

  /** Returns the original field with updated binary properties */
  def withUpdatedBinaryProperties(newBinaryProperties: BinaryProperties): Statement = {
    copy(binaryProperties = newBinaryProperties)(parent)
  }

  /** Returns the original field with updated `isRedefined` flag */
  def withUpdatedIsRedefined(newIsRedefined: Boolean): Statement = {
    copy(isRedefined = newIsRedefined)(parent)
  }

  /** Returns the original field with updated `isDependee` flag */
  def withUpdatedIsDependee(newIsDependee: Boolean): Statement = {
    copy(isDependee = newIsDependee)(parent)
  }

  /** Returns the binary size in bits for the field */
  def getBinarySizeBits: Int = {
    val size = dataType match {
      case a: AlphaNumeric =>
        // Each character is represented by a byte
        val codec = a.enc.getOrElse(EBCDIC()).codec(None, a.length, None)
        getBitCount(codec, None, a.length) //count of entire word
      case d: Decimal =>
        val codec = d.enc.getOrElse(EBCDIC()).codec(d.compact, d.scale, d.signPosition)
        // Support explicit decimal point (aka REAL DECIMAL, PIC 999.99.)
        val precision = if (d.compact.isEmpty && d.explicitDecimal) d.precision + 1 else d.precision
        getBitCount(codec, d.compact, precision)
      case i: Integer =>
        val codec = i.enc.getOrElse(EBCDIC()).codec(i.compact, i.precision, i.signPosition)
        // Hack around byte-alignment
        getBitCount(codec, i.compact, i.precision)
    }
    // round size up to next byte
    ((size + 7)/8)*8
  }

  /** Returns the string representation of a field biven a binary data.
    * Returns None if the contents of the field is empty.
    *
    * @param bitOffset An offset of the field inside the binary data
    * @param record    A record in a binary format represented as a vector of bits
    */
  def decodeValue2(bitOffset: Long, record: BitVector): Option[String] = {
    val bitCount = binaryProperties.dataSize
    val idx = bitOffset
    val bytes = record.slice(idx, idx + bitCount).toByteArray

    val value = dataType match {
      case AlphaNumeric(length, wordAlligned, enc) =>
        val encoding = enc.getOrElse(EBCDIC())
        Some(decodeString(encoding, bytes, length))
      case Decimal(scale, precision, explicitDecimal, signPosition, wordAlligned, compact, enc) =>
        val encoding = enc.getOrElse(EBCDIC())
        decodeCobolNumber(encoding, bytes, compact, precision, scale, explicitDecimal, signPosition.nonEmpty)
      case Integer(precision, signPosition, wordAlligned, compact, enc) =>
        val encoding = enc.getOrElse(EBCDIC())
        decodeCobolNumber(encoding, bytes, compact, precision, 0, explicitDecimal = false, signPosition.nonEmpty)
      case _ => throw new IllegalStateException("Unknown AST object")
    }
    value
  }

  /** Returns a value of a field biven a binary data.
    * The return data type depends on the data type of the field
    *
    * @param itOffset An offset of the field inside the binary data
    * @param record   A record in a binary format represented as a vector of bits
    */
  def decodeTypeValue(itOffset: Long, record: BitVector): Any = {
    val str = decodeValue2(itOffset, record)
    str match {
      case None => null
      case Some(strValue) =>
        val value = dataType match {
          case _: AlphaNumeric =>
            strValue
          case _: Decimal =>
            BigDecimal(strValue)
          case dt: Integer =>
            // Here the explicit converters to boxed types are used.
            // This is because Scala tries to generalize output and will
            // produce java.lang.Long for both str.get.toLong and str.get.toInt
            if (dt.precision > Constants.maxIntegerPrecision) {
              val longValue: java.lang.Long = strValue.toLong
              longValue
            }
            else {
              val intValue: java.lang.Integer = strValue.toInt
              intValue
            }
          case _ => throw new IllegalStateException("Unknown AST object")
        }
        value
    }
  }


  /** Returns the number of bits an integral value occupies given an encoding and a binary representation format.
    *
    * @param codec     A type of encoding (EBCDIC / ASCII)
    * @param comp      A type of binary number representation format
    * @param precision A precision that is the number of digits in a number
    *
    */
  private def getBitCount(codec: Codec[_ <: AnyVal], comp: Option[Int], precision: Int): Int = {
    comp match {
      case Some(x) =>
        x match {
          case a if a == 3 =>
            (precision + 1) * codec.sizeBound.lowerBound.toInt //bcd
          case _ => codec.sizeBound.lowerBound.toInt // bin/float/floatL
        }
      case None => precision * codec.sizeBound.lowerBound.toInt
    }
  }

}
