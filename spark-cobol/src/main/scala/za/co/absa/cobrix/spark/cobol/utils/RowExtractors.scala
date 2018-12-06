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

package za.co.absa.cobrix.spark.cobol.utils

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRow
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

import scala.collection.mutable.ArrayBuffer

object RowExtractors {

  /**
    * This method extracts a record from the specified bit vector. The copybook for the record needs to be already parsed.
    *
    * @param ast              The parsed copybook
    * @param data             The data bits containing the record
    * @param offsetBits       The offset to the beginning of the record (in bits)
    * @param generateRecordId If true a record id field will be added as the first field of the record.
    * @param recordId         The record id to be saved to the record id field
    *
    * @return                 A Spark [[org.apache.spark.sql.Row]] object corresponding to the record schema
    */
  @throws(classOf[IllegalStateException])
  def extractRecord(ast: CopybookAST,
                    data: Array[Byte],
                    offsetBits: Long = 0,
                    policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
                    generateRecordId: Boolean = false,
                    segmentLevelIds: Seq[Any] = Nil,
                    fileId: Int = 0,
                    recordId: Long = 0): Row = {
    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    def extractArray(field: Statement, useOffset: Long): Array[Any] = {
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
          val groupValues = new Array[Any](actualSize-from)
          var i = from
          var j = 0
          while (i < actualSize) {
            val value = getGroupValues(offset, grp)
            offset += grp.binaryProperties.dataSize
            groupValues(j) = value
            i += 1
            j += 1
          }
          groupValues
        case s: Primitive =>
          val values = new Array[Any](actualSize-from)
          var i = from
          var j = 0
          while (i < actualSize) {
            val value = s.decodeTypeValue(offset, data)
            offset += s.binaryProperties.dataSize
            values(j) = value
            i += 1
            j += 1
          }
          values
      }
    }

    def extractValue(field: Statement, useOffset: Long): Any = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp)
        case st: Primitive =>
          val value = st.decodeTypeValue(useOffset, data)
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

    def getGroupValues(offset: Long, group: Group): Row = {
      var bitOffset = offset

      val fields = new Array[Any](group.nonFillerSize)

      var j = 0
      var i = 0
      while (i < group.children.length) {
        val field = group.children(i)
        val fieldValue = if (field.isArray) {
          extractArray(field, bitOffset)
        } else {
          extractValue(field, bitOffset)
        }
        if (!field.isRedefined) {
          bitOffset += field.binaryProperties.actualSize
        }
        if (!field.isFiller) {
          fields(j) = fieldValue
          j += 1
        }
        i += 1
      }
      new GenericRow(fields)
    }

    var nextOffset = offsetBits

    val records = for (record <- ast) yield {
      val values = getGroupValues(nextOffset, record)
      nextOffset += record.binaryProperties.actualSize
      values
    }

    applyRowPostProcessing(ast, records, policy, generateRecordId, segmentLevelIds, fileId, recordId)
  }

  /**
    * <p>This method applies additional postprocessing to the schema obtained from a copybook to make it easier to use as a Spark Schema.</p>
    *
    * <p>The following transofmations will currently be applied:
    * <ul>
    *   <li>If `generateRecordId == true` the record id field will be prepended to the row.</li>
    *   <li>If the schema has only one root StructType element, the element will be expanded. The resulting schema will contain only the children fields of
    *   the element.</li>
    * </ul>
    * Combinations of the listed transformations are supported.
    * </p>
    *
    * @param ast              The parsed copybook
    * @param records          The array of [[Row]] object for each Group of the copybook
    * @param generateRecordId If true a record id field will be added as the first field of the record.
    * @param fileId           The file id to be saved to the file id field
    * @param recordId         The record id to be saved to the record id field
    *
    * @return                 A Spark [[Row]] object corresponding to the record schema
    */
  private def applyRowPostProcessing(ast: CopybookAST,
                                     records: Seq[Row],
                                     policy: SchemaRetentionPolicy,
                                     generateRecordId: Boolean,
                                     segmentLevelIds: Seq[Any],
                                     fileId: Int,
                                     recordId: Long): Row = {
    if (generateRecordId) {
      if (policy == SchemaRetentionPolicy.CollapseRoot) {
        // If the policy for schema retention is root collapsing, expand root fields
        // and add fileId and recordId
        val expandedRows = records.flatMap( record => record.toSeq )
        Row.fromSeq(fileId +: recordId +: (segmentLevelIds ++ expandedRows))
      } else {
        // Add recordId as the first field
        Row.fromSeq(fileId +: recordId +: (segmentLevelIds ++ records))
      }
    } else {
      // Addition of record index is not required
      if (policy == SchemaRetentionPolicy.CollapseRoot) {
        // If the policy for schema retention is root collapsing, expand root fields
        Row.fromSeq(segmentLevelIds ++ records.flatMap( record => record.toSeq ))
      } else {
        // Return rows as the original sequence of groups
        Row.fromSeq(segmentLevelIds ++ records)
      }
    }
  }
}
