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

package za.co.absa.cobrix.spark.cobol.reader.fixedlen.iterator

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

import scala.collection.mutable.ListBuffer

/**
  * This is an iterator traversing contents of Cobol binary data
  * and returning the data as a map of field names to values
  *
  * @param binaryData  A binary data to traverse
  * @param cobolSchema A Cobol schema obtained by parsing a copybook
  */
@deprecated("This iterator was built for testing purposes only. Please use BinaryDataFlatRowIterator instead.")
class FixedLenMapIterator(val binaryData: Array[Byte], val cobolSchema: CobolSchema) extends Iterator[Map[String, Option[String]]] {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val recordSize = cobolSchema.getRecordSize
  private var byteIndex = 0
  private var currentRecord: Seq[(String, Option[String])] = _

  override def hasNext: Boolean = byteIndex + recordSize <= binaryData.length

  override def next(): Map[String, Option[String]] = {
    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    def extractArray(field: Statement, useOffset: Int, path: String="", isNullPath: Boolean=false): Seq[Tuple2[String, Option[String]]] = {
      val fields = new ListBuffer[Tuple2[String, Option[String]]]()
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
          val grpPath = s"$path${grp.name}_"
          for (i <- Range(from, arraySize)){
            val useNullPath = isNullPath || i >= actualSize
            val values = getGroupValues(offset, grp, s"$grpPath${i+1}_", useNullPath)
            offset += grp.binaryProperties.dataSize
            fields ++= values
          }
        case s: Primitive =>
          for (i <- Range(from, arraySize)) yield {
            val useNullPath = isNullPath || i >= actualSize
            val value: Option[String] = if (useNullPath) None else {
              val v = s.decodeTypeValue(offset, binaryData)
              if (v==null) None else Some(v.toString)
            }
            val tpl = (s"$path${i+1}", value)
            fields += tpl
            offset += s.binaryProperties.dataSize
          }
      }
      fields
    }

    def extractValue(field: Statement, useOffset: Int, path: String="", isNullPath: Boolean=false): Seq[Tuple2[String, Option[String]]] = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp, s"$path${grp.name}_", isNullPath)
        case st: Primitive =>
          val value = st.decodeTypeValue(useOffset, binaryData)
          if (value != null && st.isDependee) {
            val intVal: Int = value match {
              case v: Int => v
              case v: Number => v.intValue()
              case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
            }
            dependFields += st.name -> intVal
          }
          if (isNullPath || value == null)
            Seq((s"$path${field.name}", None))
          else
            Seq((s"$path${field.name}", Some(value.toString)))
      }
    }

    def getGroupValues(offset: Int, group: Group, path: String = "", isNullPath: Boolean=false): Seq[Tuple2[String, Option[String]]] = {
      var bitOffset = offset
      val fields = new ListBuffer[Tuple2[String, Option[String]]]()

      for (field <- group.children) {
        val vals = if (field.isArray) {
          extractArray(field, bitOffset, path, isNullPath)
        } else {
          extractValue(field, bitOffset, path, isNullPath)
          }

        if (!field.isFiller) {
          fields ++= vals
        }

        if (!field.isRedefined) {
          bitOffset += field.binaryProperties.actualSize
        }
      }
      fields
    }

    if (!hasNext) {
      throw new NoSuchElementException()
    }

    var offset = byteIndex
    val records = cobolSchema.getCobolSchema.ast.children.flatMap( record => {
      val values = getGroupValues(offset, record.asInstanceOf[Group], s"${record.name}_")
      offset += record.binaryProperties.actualSize
      values
    })

    // Advance byte index to the next record
    val lastRecord = cobolSchema.getCobolSchema.ast.children.last
    val lastRecordActualSize = lastRecord.binaryProperties.offset + lastRecord.binaryProperties.actualSize
    byteIndex += lastRecordActualSize

    currentRecord = records

    //for ((k,v) <- currentRecord) println(s"$k => $v")

    currentRecord.toMap
  }

  /** This routine is used for debugging by showing contents of the current record of Cobol reader */
  def showCurrentRecord(): Unit = {
    println(generateRecordString())
  }

  /** This routine is used fortesting by generating contents of the current record of Cobol reader */
  def generateRecordString(): String = {
    if (currentRecord == null || currentRecord.isEmpty) {
      throw new IllegalStateException("No record has been fetched. Please, use iterator.nex first.")
    }
    val strings = for (field <- currentRecord) yield {
      field match {
        case (name, value) => s"$name = $value"
      }
    }
    strings.mkString("\n")
  }

  def generateDebugCSVHeaders: String = {
    val schema = cobolSchema.getSparkFlatSchema
    val recordStrings = for (field <- schema.fields) yield {
      field.name
    }
    recordStrings.mkString(",")
  }

  def generateDebugCSVCurrentRow: String = {
    val recordStrings = for (field <- currentRecord) yield {
      field match {
        case (name, value) => value.getOrElse("")
      }
    }
    recordStrings.mkString(",")
  }


  private def getGroupValues(group: Group, path: String): Map[String, Option[String]] = {
    val fields = for (field <- group.children if !field.isFiller) yield {
      field match {
        case group: Group => getGroupValues(group, s"$path${group.name}_")
        case s: Primitive =>
          val name = s"$path${s.name}"
          val valueAny = s.decodeTypeValue(byteIndex + s.binaryProperties.offset, binaryData)
          val value = if (valueAny == null) None else Some(valueAny.toString)
          Map[String, Option[String]](name -> value)
      }
    }
    fields.flatten.toMap
  }

}
