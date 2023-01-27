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

package za.co.absa.cobrix.spark.cobol.schema

import org.apache.spark.sql.types._
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast._
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, COMP1, COMP2, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.RAW
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.schema.{CobolSchema => CobolReaderSchema}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


/**
  * This class provides a view on a COBOL schema from the perspective of Spark. When provided with a parsed copybook the class
  * provides the corresponding Spark schema and also other properties for the Spark data source.
  *
  * @param copybook                A parsed copybook.
  * @param policy                  Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param generateRecordId        If true, a record id field will be prepended to the beginning of the schema.
  * @param inputFileNameField      If non-empty, a source file name will be prepended to the beginning of the schema.
  * @param generateSegIdFieldsCnt  A number of segment ID levels to generate
  * @param segmentIdProvidedPrefix A prefix for each segment id levels to make segment ids globally unique (by default the current timestamp will be used)
  * @param detailedMetadata        If true, Spark schema will be generated with additional metadata (e.g. PICs, USAGE, etc.)
  */
class CobolSchema(copybook: Copybook,
                  policy: SchemaRetentionPolicy,
                  inputFileNameField: String,
                  generateRecordId: Boolean,
                  generateSegIdFieldsCnt: Int = 0,
                  segmentIdProvidedPrefix: String = "",
                  detailedMetadata: Boolean = false)
  extends CobolReaderSchema(
    copybook, policy, inputFileNameField, generateRecordId,
    generateSegIdFieldsCnt, segmentIdProvidedPrefix
    ) with Logging with Serializable {

  @throws(classOf[IllegalStateException])
  private[this] lazy val sparkSchema = createSparkSchema()

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

  @throws(classOf[IllegalStateException])
  private def createSparkSchema(): StructType = {
    logger.info("Layout positions:\n" + copybook.generateRecordLayoutPositions())

    val records = for (record <- copybook.getRootRecords) yield {
      val group = record.asInstanceOf[Group]
      val redefines = copybook.getAllSegmentRedefines
      parseGroup(group, redefines)
    }
    val expandRecords = if (policy == SchemaRetentionPolicy.CollapseRoot || copybook.isFlatCopybook) {
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

    val recordsWithFileName = if (inputFileNameField.nonEmpty) {
      StructField(inputFileNameField, StringType, nullable = true) +: recordsWithSegmentFields
    } else {
      recordsWithSegmentFields
    }

    val recordsWithRecordId = if (generateRecordId) {
      StructField(Constants.fileIdField, IntegerType, nullable = false) +:
        StructField(Constants.recordIdField, LongType, nullable = false) +:
        StructField(Constants.recordByteLength, IntegerType, nullable = false) +: recordsWithFileName
    } else {
      recordsWithFileName
    }

    StructType(recordsWithRecordId)
  }

  @throws(classOf[IllegalStateException])
  private def parseGroup(group: Group, segmentRedefines: List[Group]): StructField = {
    val fields = group.children.flatMap(field => {
      if (field.isFiller) {
        // Skipping fillers
        Nil
      } else {
        field match {
          case group: Group =>
            if (group.parentSegment.isEmpty) {
              parseGroup(group, segmentRedefines) :: Nil
            } else {
              // Skipping child segments on this level
              Nil
            }
          case p: Primitive =>
            parsePrimitive(p) :: Nil
        }
      }
    })

    val fieldsWithChildrenSegments = fields ++ getChildSegments(group, segmentRedefines)
    val metadata = new MetadataBuilder()

    if (detailedMetadata)
      addDetailedMetadata(metadata, group)

    if (group.isArray) {
      addArrayMetadata(metadata, group)
      StructField(group.name, ArrayType(StructType(fieldsWithChildrenSegments.toArray)), nullable = true, metadata.build())
    } else {
      StructField(group.name, StructType(fieldsWithChildrenSegments.toArray), nullable = true, metadata.build())
    }
  }

  @throws(classOf[IllegalStateException])
  private def parsePrimitive(p: Primitive): StructField = {
    val metadata = new MetadataBuilder()
    val dataType: DataType = p.dataType match {
      case d: Decimal      =>
        d.compact match {
          case Some(COMP1()) => FloatType
          case Some(COMP2()) => DoubleType
          case _             => DecimalType(d.getEffectivePrecision, d.getEffectiveScale)
        }
      case a: AlphaNumeric =>
        addAlphaNumericMetadata(metadata, a)
        a.enc match {
          case Some(RAW) => BinaryType
          case _         => StringType
        }
      case dt: Integral    =>
        if (dt.precision > Constants.maxLongPrecision) {
          DecimalType(precision = dt.precision, scale = 0)
        } else if (dt.precision > Constants.maxIntegerPrecision) {
          LongType
        }
        else {
          IntegerType
        }
      case _               => throw new IllegalStateException("Unknown AST object")
    }

    if (detailedMetadata)
      addDetailedMetadata(metadata, p)

    if (p.isArray) {
      addArrayMetadata(metadata, p)
      StructField(p.name, ArrayType(dataType), nullable = true, metadata.build())
    } else {
      StructField(p.name, dataType, nullable = true, metadata.build())
    }
  }

  private def addArrayMetadata(metadataBuilder: MetadataBuilder, st: Statement): MetadataBuilder = {
    metadataBuilder.putLong("minElements", st.arrayMinSize)
    metadataBuilder.putLong("maxElements", st.arrayMaxSize)
  }

  private def addAlphaNumericMetadata(metadataBuilder: MetadataBuilder, a: AlphaNumeric): MetadataBuilder = {
    metadataBuilder.putLong("maxLength", a.length)
  }

  private def addDetailedMetadata(metadataBuilder: MetadataBuilder, s: Statement): MetadataBuilder = {
    metadataBuilder.putLong("level", s.level)
    if (s.originalName.nonEmpty && s.originalName != s.name)
      metadataBuilder.putString("originalName", s.originalName)
    s.redefines.foreach(redefines => metadataBuilder.putString("redefines", redefines))
    s.dependingOn.foreach(dependingOn => metadataBuilder.putString("depending_on", dependingOn))

    s match {
      case p: Primitive => addDetailedPrimitiveMetadata(metadataBuilder, p)
      case g: Group     => addDetailedGroupMetadata(metadataBuilder, g)
    }

    metadataBuilder
  }

  private def addDetailedPrimitiveMetadata(metadataBuilder: MetadataBuilder, p: Primitive): MetadataBuilder = {
    metadataBuilder.putString("pic", p.dataType.originalPic.getOrElse(p.dataType.pic))
    p.dataType match {
      case a: Integral =>
        a.compact.foreach(usage => metadataBuilder.putString("usage", usage.toString))
      case a: Decimal  =>
        a.compact.foreach(usage => metadataBuilder.putString("usage", usage.toString))
      case _           =>
    }
    metadataBuilder
  }

  private def addDetailedGroupMetadata(metadataBuilder: MetadataBuilder, g: Group): MetadataBuilder = {
    g.groupUsage.foreach(usage => metadataBuilder.putString("usage", usage.toString))
    metadataBuilder
  }

  private def getChildSegments(group: Group, segmentRedefines: List[Group]): ArrayBuffer[StructField] = {
    val childSegments = new mutable.ArrayBuffer[StructField]()

    segmentRedefines.foreach(segment => {
      segment.parentSegment.foreach(parent => {
        if (parent.name.equalsIgnoreCase(group.name)) {
          val child = parseGroup(segment, segmentRedefines)
          val fields = child.dataType.asInstanceOf[StructType].fields
          childSegments += StructField(segment.name, ArrayType(StructType(fields)), nullable = true)
        }
      })
    })
    childSegments
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
            case d: Decimal      =>
              DecimalType(d.getEffectivePrecision, d.getEffectiveScale)
            case a: AlphaNumeric =>
              a.enc match {
                case Some(RAW) => BinaryType
                case _         => StringType
              }
            case dt: Integral    =>
              if (dt.precision > Constants.maxIntegerPrecision) {
                LongType
              }
              else {
                IntegerType
              }
            case _               => throw new IllegalStateException("Unknown AST object")
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
}

object CobolSchema {
  def fromBaseReader(schema: CobolReaderSchema): CobolSchema = {
    new CobolSchema(
      schema.copybook,
      schema.policy,
      schema.inputFileNameField,
      schema.generateRecordId,
      schema.generateSegIdFieldsCnt,
      schema.segmentIdPrefix,
      schema.detailedMetadata
      )
  }
}