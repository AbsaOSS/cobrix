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

package za.co.absa.cobrix.cobol.parser.recordextractors

import java.util

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream

/**
  * This implementation of a record extractor
  * A raw record is an array of bytes.
  *
  * Record extractors are used for in situations where the size of records in a file is not fixed and cannot be
  * determined neither from the copybook nor from record headers.
  */
class VarOccursRecordExtractor(inputStream: SimpleStream, copybook: Copybook) extends RawRecordExtractor {
  private val maxRecordSize = copybook.getRecordSize
  private val ast = copybook.ast
  private val hasVarSizeOccurs = copybookHasVarSizedOccurs
  private val bytes = new Array[Byte](maxRecordSize)
  private var bytesSize = 0

  override def hasNext: Boolean = inputStream.offset < inputStream.size

  override def next(): Array[Byte] = {
    if (hasVarSizeOccurs) {
      bytesSize = 0
      util.Arrays.fill(bytes, 0.toByte)
      extractVarOccursRecordBytes()
    } else {
      inputStream.next(maxRecordSize)
    }
  }

  private def extractVarOccursRecordBytes(): Array[Byte] = {
    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    def extractArray(field: Statement, useOffset: Int): Int = {
      val arraySize = field.arrayMaxSize
      val actualSize = field.dependingOn match {
        case None => arraySize
        case Some(dependingOn) =>
          val dependValue = dependFields.getOrElse(dependingOn, arraySize)
          if (dependValue >= field.arrayMinSize && dependValue <= arraySize)
            dependValue
          else
            arraySize
      }

      var offset = useOffset
      field match {
        case grp: Group =>
          var i = 0
          while (i < actualSize) {
            offset += extractGroup(offset, grp)
            i += 1
          }
        case s: Primitive =>
          offset += s.binaryProperties.dataSize * actualSize
      }
      offset - useOffset
    }

    def extractValue(field: Statement, useOffset: Int): Int = {
      field match {
        case grp: Group =>
          extractGroup(useOffset, grp)
        case st: Primitive =>
          if (st.isDependee) {
            ensureBytesRead(useOffset + field.binaryProperties.actualSize)
            val value = st.decodeTypeValue(useOffset, bytes)
            if (value != null) {
              val intVal: Int = value match {
                case v: Int => v
                case v: Number => v.intValue()
                case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
              }
              dependFields += st.name -> intVal
            }
          }
          field.binaryProperties.actualSize
      }
    }

    def extractGroup(useOffset: Int, group: Group): Int = {
      var offset = useOffset
      var j = 0
      var i = 0
      while (i < group.children.length) {
        val field = group.children(i)
        if (field.isArray) {
          val size = extractArray(field, offset)
          if (!field.isRedefined) {
            offset += size
          }
        } else {
          val size = extractValue(field, offset)
          if (!field.isRedefined) {
            offset += size
          }
        }
        if (!field.isFiller) {
          j += 1
        }
        i += 1
      }
      offset - useOffset
    }

    var nextOffset = 0
    for (record <- ast.children) {
      nextOffset += extractGroup(nextOffset, record.asInstanceOf[Group])
    }
    ensureBytesRead(nextOffset)
    bytes.take(nextOffset)
  }

  private def ensureBytesRead(numOfBytes: Int): Unit = {
    val bytesToRead = numOfBytes - bytesSize
    if (bytesToRead > 0) {
      val newBytes = inputStream.next(bytesToRead)
      if (newBytes.length > 0) {
        System.arraycopy(newBytes, 0, bytes, bytesSize, newBytes.length)
        bytesSize = numOfBytes
      }
    }
  }

  private def copybookHasVarSizedOccurs: Boolean = {
    var varSizedOccursExist = false
    copybook.visitPrimitive(field => varSizedOccursExist |= field.isDependee)
    varSizedOccursExist
  }
}
