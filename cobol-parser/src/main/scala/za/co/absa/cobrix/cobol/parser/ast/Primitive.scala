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

package za.co.absa.cobrix.cobol.parser.ast

import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.decoders.{BinaryUtils, DecoderSelector}

/** An abstraction of the statements describing fields of primitive data types in the COBOL copybook
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
case class Primitive(
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
                      isFiller: Boolean = false,
                      decode: DecoderSelector.Decoder,
                      binaryProperties: BinaryProperties = BinaryProperties(0, 0, 0)
                    )
                    (val parent: Option[Group] = None)
  extends Statement {

  /** This is cached value specifying if the field is a string */
  private val isString = dataType.isInstanceOf[AlphaNumeric]

  /** Returns a string representation of the field */
  override def toString: String = {
    s"${" " * 2 * level}$camelCased ${camelCase(redefines.getOrElse(""))} $dataType"
  }

  /** Returns true if the field is a child segment */
  def isChildSegment: Boolean = false

  /** Returns the original field with updated binary properties */
  def withUpdatedBinaryProperties(newBinaryProperties: BinaryProperties): Primitive = {
    copy(binaryProperties = newBinaryProperties)(parent)
  }

  /** Returns the original field with updated `isRedefined` flag */
  def withUpdatedIsRedefined(newIsRedefined: Boolean): Primitive = {
    copy(isRedefined = newIsRedefined)(parent)
  }

  /** Returns the original field with updated `isDependee` flag */
  def withUpdatedIsDependee(newIsDependee: Boolean): Primitive = {
    copy(isDependee = newIsDependee)(parent)
  }

  /** Returns the binary size in bits for the field */
  def getBinarySizeBytes: Int = {
    dataType match {
      case a: AlphaNumeric =>
        a.length
      case d: Decimal =>
        BinaryUtils.getBytesCount(d.compact, d.precision, d.signPosition.isDefined, d.explicitDecimal, d.isSignSeparate)
      case i: Integral =>
        BinaryUtils.getBytesCount(i.compact, i.precision, i.signPosition.isDefined, isExplicitDecimalPt = false, isSignSeparate = i.isSignSeparate)
    }
  }

  /** Returns a value of a field biven a binary data.
    * The return data type depends on the data type of the field
    *
    * @param itOffset An offset of the field inside the binary data
    * @param record   A record in a binary format represented as a vector of bits
    */
  @throws(classOf[Exception])
  def decodeTypeValue(itOffset: Int, record: Array[Byte]): Any = {
    val bytesCount = binaryProperties.dataSize
    val idx = itOffset

    if (isString) {
      // The length of a string can be smaller for varchar fields at the end of a record
      if (idx > record.length) {
        return null
      }
    } else {
      // Non-string field size should exactly fix the required bytes
      if (idx + bytesCount > record.length) {
        return null
      }
    }

    // Determine the actual number of bytes to copy based on the record size.
    // Varchar fields can be trimmed by the record size.
    val bytesToCopy = if (idx + bytesCount > record.length) {
      record.length - idx
    } else {
      bytesCount
    }
    val bytes = java.util.Arrays.copyOfRange(record, idx, idx + bytesToCopy)

    decode(bytes)
  }

}
