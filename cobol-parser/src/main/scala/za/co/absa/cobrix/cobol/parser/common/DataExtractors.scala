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

package za.co.absa.cobrix.cobol.parser.common

import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Statement, Group, Primitive}

import scala.collection.mutable.ArrayBuffer

object DataExtractors {


  @throws(classOf[IllegalStateException])
  def extractValues(ast: CopybookAST, bytes: Array[Byte], offset: Int = 0): Seq[Any] = {

    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    def extractArray(field: Statement, useOffset: Int): IndexedSeq[Any] = {
      val from = 0
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
          val groupValues = for (_ <- Range(from, actualSize)) yield {
            val value = getGroupValues(offset, grp)
            offset += grp.binaryProperties.dataSize
            value
          }
          groupValues
        case s: Primitive =>
          val values = for (_ <- Range(from, actualSize)) yield {
            val value = s.decodeTypeValue(offset, bytes)
            offset += s.binaryProperties.dataSize
            value
          }
          values
      }
    }

    def extractValue(field: Statement, useOffset: Int): Any = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp)
        case st: Primitive =>
          val value = st.decodeTypeValue(useOffset, bytes)
          if (value != null && st.isDependee) {
            val intVal: Int = value match {
              case v: Int => v
              case v: Number => v.intValue()
              case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
            }
            dependFields += st.name -> intVal
          }
          value
      }
    }

    def getGroupValues(offset: Int, group: Group): Seq[Any] = {
      var bitOffset = offset
      val fields = new ArrayBuffer[Any]()

      for (field <- group.children) {
        val fieldValue = if (field.isArray) {
          extractArray(field, bitOffset)
        } else {
          extractValue(field, bitOffset)
        }
        if (!field.isRedefined) {
          bitOffset += field.binaryProperties.actualSize
        }
        if (!field.isFiller) {
          fields += fieldValue
        }
      }
      fields
    }

    var nextOffset = offset
    val records = for (record <- ast.children) yield {
      val values = getGroupValues(nextOffset, record.asInstanceOf[Group])
      nextOffset += record.binaryProperties.actualSize
      values
    }
    records
  }

}
