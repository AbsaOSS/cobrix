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

package za.co.absa.cobrix.spark.cobol.reader.fixedlen.iterator

import org.apache.spark.sql.Row
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.ast.{CBTree, Group, Statement}
import za.co.absa.cobrix.cobol.parser.common.ReservedWords
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

import scala.collection.mutable.ListBuffer

/**
  * This is an iterator traversing contents of Cobol binary data
  * for the flattened version of schema
  *
  * @param binaryData  A binary data to traverse
  * @param cobolSchema A Cobol schema obtained by parsing a copybook
  */
class FixedLenFlatRowIterator(val binaryData: Array[Byte], val cobolSchema: CobolSchema) extends Iterator[Row] {
  private val dataBits: BitVector = BitVector(binaryData)
  private val recordSize = cobolSchema.getRecordSize
  private var bitIndex = 0L

  override def hasNext: Boolean = bitIndex + recordSize <= dataBits.size

  override def next(): Row = {
    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    def extractArray(field: CBTree, useOffset: Long, isNullPath: Boolean = false): Seq[Any] = {
      val fields = new ListBuffer[Any]()
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
          val groupValues = for (i <- Range(from, arraySize)) yield {
            val useNullPath = isNullPath || i >= actualSize
            val value = getGroupValues(offset, grp, useNullPath)
            offset += grp.binaryProperties.dataSize
            fields ++= value
          }
          groupValues
        case s: Statement =>
          val values = for (i <- Range(from, arraySize)) yield {
            val useNullPath = isNullPath || i >= actualSize
            val value = s.decodeTypeValue(offset, dataBits)
            offset += s.binaryProperties.dataSize
            if (useNullPath) null else value
          }
          fields ++= values
      }
      fields
    }

    def extractValue(field: CBTree, useOffset: Long, isNullPath: Boolean = false): Seq[Any] = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp, isNullPath)
        case st: Statement =>
          val value = st.decodeTypeValue(useOffset, dataBits)
          if (value != null && st.isDependee) {
            val intVal: Int = value match {
              case v: Int => v
              case v: Number => v.intValue()
              case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
            }
            dependFields += st.name -> intVal
          }
          if (isNullPath) Seq(null) else Seq(value)
      }
    }

    def getGroupValues(offset: Long, group: Group, isNullPath: Boolean = false): Seq[Any] = {
      var bitOffset = offset
      val fields = new ListBuffer[Any]()

      for (field <- group.children) {
        val fieldValues = if (field.isArray) {
          extractArray(field, bitOffset, isNullPath)
        } else {
          extractValue(field, bitOffset, isNullPath)
        }
        if (!field.isRedefined) {
          bitOffset += field.binaryProperties.actualSize
        }
        if (field.name.toUpperCase != ReservedWords.FILLER) {
          fields ++= fieldValues
        }
      }
      fields
    }

    if (!hasNext) {
      throw new NoSuchElementException()
    }

    var offset = bitIndex
    val records = cobolSchema.getCobolSchema.ast.flatMap( record => {
      val values = getGroupValues(offset, record)
      offset += record.binaryProperties.actualSize
      values
    })

    // Advance bit index to the next record
    val lastRecord = cobolSchema.getCobolSchema.ast.last
    val lastRecordActualSize = lastRecord.binaryProperties.offset + lastRecord.binaryProperties.actualSize
    bitIndex += lastRecordActualSize

    Row.fromSeq(records)
  }

}
