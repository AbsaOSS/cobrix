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

package za.co.absa.cobrix.spark.cobol.utils

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.ReservedWords
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

import scala.collection.mutable.ArrayBuffer

object RowExtractors {

  /**
    * This method extracts a record from the specified bit vector. The copybook for the record needs to be already parsed.
    *
    * @param ast              The parsed copybook
    * @param dataBits         The data bits containing the record
    * @param offsetBits       The offset to the beginning of the record (in bits)
    * @param generateRecordId If true a record id field will be added as the first field of the record.
    * @param recordId         The record id to be saved to the record id field
    *
    * @return                 A Spark [[Row]] object corresponding to the record schema
    */
  @throws(classOf[IllegalStateException])
  def extractRecord(ast: CopybookAST,
                    dataBits: BitVector,
                    offsetBits: Long = 0,
                    generateRecordId: Boolean = false,
                    policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
                    fileId: Int = 0,
                    recordId: Long = 0): Row = {
    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    def extractArray(field: Statement, useOffset: Long): IndexedSeq[Any] = {
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
            val value = s.decodeTypeValue(offset, dataBits)
            offset += s.binaryProperties.dataSize
            value
          }
          values
      }
    }

    def extractValue(field: Statement, useOffset: Long): Any = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp)
        case st: Primitive =>
          val value = st.decodeTypeValue(useOffset, dataBits)
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
        if (field.name.toUpperCase != ReservedWords.FILLER) {
          fields += fieldValue
        }
      }
      Row.fromSeq(fields)
    }

    var nextOffset = offsetBits
    val records = for (record <- ast) yield {
      val values = getGroupValues(nextOffset, record)
      nextOffset += record.binaryProperties.actualSize
      values
    }

    applyRowPostProcessing(ast, records, generateRecordId, policy, fileId, recordId)
  }

  /**
    * <p>This method applies additional postprocessing to the schema obtained from a copybook to make it easier to use as a Spark Schema.</p>
    *
    * <p>The following transofmations will currently be applied:
    * <ul>
    *   <li>If `generateRecordId == true` the record id field will be prepended to the row.</li>
    *   <li>If the schema has only one root StructType element, the element will be expanded. The the resulting schema will contain only the children fields of
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
                                     generateRecordId: Boolean,
                                     policy: SchemaRetentionPolicy,
                                     fileId: Int,
                                     recordId: Long): Row = {
    if (generateRecordId) {
      if (policy == SchemaRetentionPolicy.CollapseRoot) {
        // If the policy for schema retention is root collapsing, expand root fields
        // and add fileId and recordId
        val expandedRows = records.flatMap( record => record.toSeq )
        Row.fromSeq(fileId +: recordId +: expandedRows)
      } else {
        // Add recordId as the first field
        Row.fromSeq(fileId +: recordId +: records)
      }
    } else {
      // Addition of record index is not required
      if (policy == SchemaRetentionPolicy.CollapseRoot) {
        // If the policy for schema retention is root collapsing, expand root fields
        Row.fromSeq(records.flatMap( record => record.toSeq ))
      } else {
        // Return rows as the original sequence of groups
        Row.fromSeq(records)
      }
    }
  }
}
