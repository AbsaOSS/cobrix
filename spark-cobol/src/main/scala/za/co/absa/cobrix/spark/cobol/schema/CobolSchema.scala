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

package za.co.absa.cobrix.spark.cobol.schema

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast._
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.common.{Constants, ReservedWords}
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

import scala.collection.mutable.ArrayBuffer

/**
  * This class provides a view on a COBOL schema from the perspective of Spark. When provided with a parsed copybook the class
  * provides the corresponding Spark schema and also other properties for the Spark data source.
  *
  * @param copybook                A parsed copybook.
  * @param policy                  Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param generateRecordId        If true, a record id field will be prepended to to the begginning of the schema.
  * @param generateSegIdFieldsCnt  A number of segment ID levels to generate
  * @param segmentIdProvidedPrefix A prefix for each segment id levels to make segment ids globally unique (by default the current timestamp will be used)
  */
class CobolSchema(val copybook: Copybook,
                  policy: SchemaRetentionPolicy,
                  generateRecordId: Boolean,
                  generateSegIdFieldsCnt: Int = 0,
                  segmentIdProvidedPrefix: String = "") extends Serializable {

  private val logger = LoggerFactory.getLogger(this.getClass)

  val segmentIdPrefix: String = if (segmentIdProvidedPrefix.isEmpty) getDefaultSegmentIdPrefix else segmentIdProvidedPrefix

  def getCobolSchema: Copybook = copybook

  @throws(classOf[IllegalStateException])
  private[this] lazy val sparkSchema = {
    logger.info("Layout positions:\n" + copybook.generateRecordLayoutPositions())
    val records = for (record <- copybook.ast.children) yield {
      parseGroup(record.asInstanceOf[Group])
    }
    val expandRecords = if (policy == SchemaRetentionPolicy.CollapseRoot) {
      // Expand root group fields
      records.toArray.flatMap(group => group.dataType.asInstanceOf[StructType].fields)
    } else {
      records.toArray
    }

    val recordsWithSegmentFields = if (generateSegIdFieldsCnt > 0) {
      val newFields = for (level <- Range(0, generateSegIdFieldsCnt))
        yield StructField(s"${Constants.segmentIdField}$level", StringType, nullable = true)
      newFields.toArray ++ expandRecords
    } else {
      expandRecords
    }

    val recordsWithRecordId = if (generateRecordId) {
      StructField(Constants.fileIdField, IntegerType, nullable = false) +:
        StructField(Constants.recordIdField, LongType, nullable = false) +: recordsWithSegmentFields
    } else {
      recordsWithSegmentFields
    }
    StructType(recordsWithRecordId)
  }

  @throws(classOf[IllegalStateException])
  private[this] lazy val sparkFlatSchema = {
    logger.info("Layout positions:\n" + copybook.generateRecordLayoutPositions())
    val arraySchema = copybook.ast.children.toArray
    val records = arraySchema.flatMap(record => {
      parseGroupFlat(record.asInstanceOf[Group], s"${record.name}_")
    })
    StructType(records)
  }

  def getSparkSchema: StructType = {
    sparkSchema
  }

  def getSparkFlatSchema: StructType = {
    sparkFlatSchema
  }

  lazy val getRecordSize: Int = copybook.getRecordSize

  def isRecordFixedSize: Boolean = copybook.isRecordFixedSize

  @throws(classOf[IllegalStateException])
  private def parseGroup(group: Group): StructField = {
    val fields = for (field <- group.children if !field.isFiller) yield {
      field match {
        case group: Group =>
          parseGroup(group)
        case s: Primitive =>
          val dataType: DataType = s.dataType match {
            case d: Decimal =>
              val computation = d.compact.getOrElse(-1)
              if (computation == 1) {
                FloatType
              } else if (computation == 2) {
                DoubleType
              } else {
                DecimalType(d.getEffectivePrecision, d.getEffectiveScale)
              }
            case _: AlphaNumeric => StringType
            case dt: Integral =>
              if (dt.precision > Constants.maxLongPrecision) {
                DecimalType(precision = dt.precision, scale = 0)
              } else if (dt.precision > Constants.maxIntegerPrecision) {
                LongType
              }
              else {
                IntegerType
              }
            case _ => throw new IllegalStateException("Unknown AST object")
          }
          if (s.isArray) {
            StructField(s.name, ArrayType(dataType), nullable = true)
          } else {
            StructField(s.name, dataType, nullable = true)
          }
      }

    }
    if (group.isArray) {
      StructField(group.name, ArrayType(StructType(fields.toArray)), nullable = true)
    } else {
      StructField(group.name, StructType(fields.toArray), nullable = true)
    }

  }

  @throws(classOf[IllegalStateException])
  private def parseGroupFlat(group: Group, structPath: String = ""): ArrayBuffer[StructField] = {
    val fields = new ArrayBuffer[StructField]()
    for (field <- group.children if !field.isFiller) {
      field match {
        case group: Group =>
          if (group.isArray) {
            for (i <- Range(1, group.arrayMaxSize + 1)) {
              val path = s"$structPath${group.name}_${i}_"
              fields ++= parseGroupFlat(group, path)
            }
          } else {
            val path = s"$structPath${group.name}_"
            fields ++= parseGroupFlat(group, path)
          }
        case s: Primitive =>
          val dataType: DataType = s.dataType match {
            case d: Decimal =>
              DecimalType(d.getEffectivePrecision, d.getEffectiveScale)
            case _: AlphaNumeric => StringType
            case dt: Integral =>
              if (dt.precision > Constants.maxIntegerPrecision) {
                LongType
              }
              else {
                IntegerType
              }
            case _ => throw new IllegalStateException("Unknown AST object")
          }
          val path = s"$structPath" //${group.name}_"
          if (s.isArray) {
            for (i <- Range(1, s.arrayMaxSize + 1)) {
              fields += StructField(s"$path{s.name}_$i", ArrayType(dataType), nullable = true)
            }
          } else {
            fields += StructField(s"$path${s.name}", dataType, nullable = true)
          }
      }
    }

    fields
  }

  private def getDefaultSegmentIdPrefix: String = {
    val timestampFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val now = ZonedDateTime.now()
    timestampFormat.format(now)
  }

}
