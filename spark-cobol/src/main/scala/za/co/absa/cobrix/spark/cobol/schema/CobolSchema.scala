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
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, COMP1, COMP2, COMP4, COMP5, COMP9, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.RAW
import za.co.absa.cobrix.cobol.parser.policies.MetadataPolicy
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser.getReaderProperties
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParametersParser, Parameters}
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.schema.{CobolSchema => CobolReaderSchema}
import za.co.absa.cobrix.spark.cobol.parameters.MetadataFields.{MAX_ELEMENTS, MAX_LENGTH, MIN_ELEMENTS}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


/**
  * This class provides a view on a COBOL schema from the perspective of Spark. When provided with a parsed copybook the class
  * provides the corresponding Spark schema and also other properties for the Spark data source.
  *
  * @param copybook                A parsed copybook.
  * @param schemaRetentionPolicy   Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param strictIntegralPrecision If true, Cobrix will not generate short/integer/long Spark data types, and always use decimal(n) with the exact precision that matches the copybook.
  * @param generateRecordId        If true, a record id field will be prepended to the beginning of the schema.
  * @param generateRecordBytes     If true, a record bytes field will be appended to the beginning of the schema.
  * @param generateCorruptFields   If true, a corrupt fields field will be appended to the end of the schema.
  * @param inputFileNameField      If non-empty, a source file name will be prepended to the beginning of the schema.
  * @param generateSegIdFieldsCnt  A number of segment ID levels to generate
  * @param segmentIdProvidedPrefix A prefix for each segment id levels to make segment ids globally unique (by default the current timestamp will be used)
  * @param metadataPolicy          Specifies a policy to generate metadata fields.
  */
class CobolSchema(copybook: Copybook,
                  schemaRetentionPolicy: SchemaRetentionPolicy,
                  isDisplayAlwaysString: Boolean,
                  strictIntegralPrecision: Boolean,
                  inputFileNameField: String,
                  generateRecordId: Boolean,
                  generateRecordBytes: Boolean,
                  generateCorruptFields: Boolean,
                  generateSegIdFieldsCnt: Int,
                  segmentIdProvidedPrefix: String,
                  metadataPolicy: MetadataPolicy)
  extends CobolReaderSchema(copybook,
    schemaRetentionPolicy,
    isDisplayAlwaysString,
    strictIntegralPrecision,
    inputFileNameField,
    generateRecordId,
    generateRecordBytes,
    generateCorruptFields,
    generateSegIdFieldsCnt,
    segmentIdProvidedPrefix
  ) with Logging with Serializable {

  @throws(classOf[IllegalStateException])
  private[this] lazy val sparkSchema = createSparkSchema()

  def getSparkSchema: StructType = {
    sparkSchema
  }

  @throws(classOf[IllegalStateException])
  private def createSparkSchema(): StructType = {
    val records = for (record <- copybook.getRootRecords) yield {
      val group = record.asInstanceOf[Group]
      val redefines = copybook.getAllSegmentRedefines
      parseGroup(group, redefines)
    }
    val expandRecords = if (schemaRetentionPolicy == SchemaRetentionPolicy.CollapseRoot || copybook.isFlatCopybook) {
      // Expand root group fields
      records.toArray.flatMap(group => group.dataType.asInstanceOf[StructType].fields)
    } else {
      records.toArray
    }

    val recordsWithSegmentFields = if (generateSegIdFieldsCnt > 0) {
      val newFields = for (level <- Range(0, generateSegIdFieldsCnt))
        yield {
          val maxPrefixLength = getMaximumSegmentIdLength(segmentIdProvidedPrefix)
          val segFieldMetadata = new MetadataBuilder()
          segFieldMetadata.putLong(MAX_LENGTH, maxPrefixLength.toLong)

          StructField(s"${Constants.segmentIdField}$level", StringType, nullable = true, metadata = segFieldMetadata.build())
        }

      newFields.toArray ++ expandRecords
    } else {
      expandRecords
    }

    val recordsWithFileName = if (inputFileNameField.nonEmpty) {
      StructField(inputFileNameField, StringType, nullable = true) +: recordsWithSegmentFields
    } else {
      recordsWithSegmentFields
    }

    val recordsWithRecordBytes = if (generateRecordBytes) {
      StructField(Constants.recordBytes, BinaryType, nullable = false) +: recordsWithFileName
    } else {
      recordsWithFileName
    }

    val recordsWithRecordId = if (generateRecordId) {
      StructField(Constants.fileIdField, IntegerType, nullable = false) +:
        StructField(Constants.recordIdField, LongType, nullable = false) +:
        StructField(Constants.recordByteLength, IntegerType, nullable = false) +: recordsWithRecordBytes
    } else {
      recordsWithRecordBytes
    }

    val recordsWithCorruptFields = if (generateCorruptFields) {
      recordsWithRecordId :+ StructField(Constants.corruptFieldsField, ArrayType(StructType(
        Seq(
          StructField(Constants.fieldNameColumn, StringType, nullable = false),
          StructField(Constants.rawValueColumn, BinaryType, nullable = false)
        )
      ), containsNull = false), nullable = true)
    } else {
      recordsWithRecordId
    }

    StructType(recordsWithCorruptFields)
  }

  private [cobrix] def getMaximumSegmentIdLength(segmentIdProvidedPrefix: String): Int = {
    val DATETIME_PREFIX_LENGTH = 15
    val SEGMENT_ID_MAX_GENERATED_LENGTH = 50

    val prefixLength = if (segmentIdProvidedPrefix.isEmpty) DATETIME_PREFIX_LENGTH else segmentIdProvidedPrefix.length

    prefixLength + SEGMENT_ID_MAX_GENERATED_LENGTH
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

    if (metadataPolicy == MetadataPolicy.Extended)
      addExtendedMetadata(metadata, group)

    if (group.isArray) {
      if (metadataPolicy != MetadataPolicy.NoMetadata)
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
        if (metadataPolicy != MetadataPolicy.NoMetadata)
          addAlphaNumericMetadata(metadata, a)
        a.enc match {
          case Some(RAW) => BinaryType
          case _         => StringType
        }
      case dt: Integral  if isDisplayAlwaysString  =>
        if (metadataPolicy != MetadataPolicy.NoMetadata)
          addIntegralStringMetadata(metadata, dt)
        StringType
      case dt: Integral  if strictIntegralPrecision  =>
        DecimalType(precision = dt.precision, scale = 0)
      case dt: Integral  =>
        val isBinary = dt.compact.exists(c => c == COMP4() || c == COMP5() || c == COMP9())
        if (dt.precision > Constants.maxLongPrecision) {
          DecimalType(precision = dt.precision, scale = 0)
        } else if (dt.precision == Constants.maxLongPrecision && isBinary && dt.signPosition.isEmpty) {  // promoting unsigned int to long to be able to fit any value
          DecimalType(precision = dt.precision + 2, scale = 0)
        } else if (dt.precision > Constants.maxIntegerPrecision) {
          LongType
        } else if (dt.precision == Constants.maxIntegerPrecision && isBinary && dt.signPosition.isEmpty) { // promoting unsigned long to decimal(20) to be able to fit any value
          LongType
        } else {
          IntegerType
        }
      case _               => throw new IllegalStateException("Unknown AST object")
    }

    if (metadataPolicy == MetadataPolicy.Extended)
      addExtendedMetadata(metadata, p)

    if (p.isArray) {
      if (metadataPolicy != MetadataPolicy.NoMetadata)
        addArrayMetadata(metadata, p)
      StructField(p.name, ArrayType(dataType), nullable = true, metadata.build())
    } else {
      StructField(p.name, dataType, nullable = true, metadata.build())
    }
  }

  private def addArrayMetadata(metadataBuilder: MetadataBuilder, st: Statement): MetadataBuilder = {
    metadataBuilder.putLong(MIN_ELEMENTS, st.arrayMinSize)
    metadataBuilder.putLong(MAX_ELEMENTS, st.arrayMaxSize)
  }

  private def addAlphaNumericMetadata(metadataBuilder: MetadataBuilder, a: AlphaNumeric): MetadataBuilder = {
    metadataBuilder.putLong(MAX_LENGTH, a.length)
  }

  private def addIntegralStringMetadata(metadataBuilder: MetadataBuilder, i: Integral): MetadataBuilder = {
    val maxLength = if (i.signPosition.isDefined) i.precision + 1 else i.precision
    metadataBuilder.putLong(MAX_LENGTH, maxLength)
  }

  private def addExtendedMetadata(metadataBuilder: MetadataBuilder, s: Statement): MetadataBuilder = {
    metadataBuilder.putLong("level", s.level)
    if (s.originalName.nonEmpty && s.originalName != s.name)
      metadataBuilder.putString("originalName", s.originalName)
    s.redefines.foreach(redefines => metadataBuilder.putString("redefines", redefines))
    s.dependingOn.foreach(dependingOn => metadataBuilder.putString("depending_on", dependingOn))
    metadataBuilder.putLong("offset", s.binaryProperties.offset)
    metadataBuilder.putLong("byte_size", s.binaryProperties.dataSize)

    s match {
      case p: Primitive => addExtendedPrimitiveMetadata(metadataBuilder, p)
      case g: Group     => addExtendedGroupMetadata(metadataBuilder, g)
    }

    metadataBuilder
  }

  private def addExtendedPrimitiveMetadata(metadataBuilder: MetadataBuilder, p: Primitive): MetadataBuilder = {
    metadataBuilder.putString("pic", p.dataType.originalPic.getOrElse(p.dataType.pic))
    p.dataType match {
      case a: Integral =>
        a.compact.foreach(usage => metadataBuilder.putString("usage", usage.toString))
        metadataBuilder.putLong("precision", a.precision)
        metadataBuilder.putBoolean("signed", a.signPosition.nonEmpty)
        metadataBuilder.putBoolean("sign_separate", a.isSignSeparate)
      case a: Decimal  =>
        a.compact.foreach(usage => metadataBuilder.putString("usage", usage.toString))
        metadataBuilder.putLong("precision", a.precision)
        metadataBuilder.putLong("scale", a.scale)
        if (a.scaleFactor != 0)
          metadataBuilder.putLong("scale_factor", a.scaleFactor)
        metadataBuilder.putBoolean("signed", a.signPosition.nonEmpty)
        metadataBuilder.putBoolean("sign_separate", a.isSignSeparate)
        metadataBuilder.putBoolean("implied_decimal", !a.explicitDecimal)
      case _           =>
    }
    metadataBuilder
  }

  private def addExtendedGroupMetadata(metadataBuilder: MetadataBuilder, g: Group): MetadataBuilder = {
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
}

object CobolSchema {
  def fromBaseReader(schema: CobolReaderSchema): CobolSchema = {
    new CobolSchema(
      schema.copybook,
      schema.policy,
      schema.isDisplayAlwaysString,
      schema.strictIntegralPrecision,
      schema.inputFileNameField,
      schema.generateRecordId,
      schema.generateRecordBytes,
      schema.generateCorruptFields,
      schema.generateSegIdFieldsCnt,
      schema.segmentIdPrefix,
      schema.metadataPolicy
      )
  }

  def fromSparkOptions(copyBookContents: Seq[String], sparkReaderOptions: Map[String, String]): CobolSchema = {
    val lowercaseOptions = sparkReaderOptions.map { case (k, v) => (k.toLowerCase, v) }
    val cobolParameters = CobolParametersParser.parse(new Parameters(lowercaseOptions))
    val readerParameters = getReaderProperties(cobolParameters, None)

    CobolSchema.fromBaseReader(CobolReaderSchema.fromReaderParameters(copyBookContents, readerParameters))
  }

  def builder(copybook: Copybook): CobolSchemaBuilder = new CobolSchemaBuilder(copybook)

  class CobolSchemaBuilder(copybook: Copybook) {
    private var schemaRetentionPolicy: SchemaRetentionPolicy = SchemaRetentionPolicy.CollapseRoot
    private var isDisplayAlwaysString: Boolean = false
    private var strictIntegralPrecision: Boolean = false
    private var inputFileNameField: String = ""
    private var generateRecordId: Boolean = false
    private var generateRecordBytes: Boolean = false
    private var generateCorruptFields: Boolean = false
    private var generateSegIdFieldsCnt: Int = 0
    private var segmentIdProvidedPrefix: String = ""
    private var metadataPolicy: MetadataPolicy = MetadataPolicy.Basic

    def withSchemaRetentionPolicy(schemaRetentionPolicy: SchemaRetentionPolicy): CobolSchemaBuilder = {
      this.schemaRetentionPolicy = schemaRetentionPolicy
      this
    }

    def withIsDisplayAlwaysString(isDisplayAlwaysString: Boolean): CobolSchemaBuilder = {
      this.isDisplayAlwaysString = isDisplayAlwaysString
      this
    }

    def withStrictIntegralPrecision(strictIntegralPrecision: Boolean): CobolSchemaBuilder = {
      this.strictIntegralPrecision = strictIntegralPrecision
      this
    }

    def withInputFileNameField(inputFileNameField: String): CobolSchemaBuilder = {
      this.inputFileNameField = inputFileNameField
      this
    }

    def withGenerateRecordId(generateRecordId: Boolean): CobolSchemaBuilder = {
      this.generateRecordId = generateRecordId
      this
    }

    def withGenerateRecordBytes(generateRecordBytes: Boolean): CobolSchemaBuilder = {
      this.generateRecordBytes = generateRecordBytes
      this
    }

    def withGenerateCorruptFields(generateCorruptFields: Boolean): CobolSchemaBuilder = {
      this.generateCorruptFields = generateCorruptFields
      this
    }

    def withGenerateSegIdFieldsCnt(generateSegIdFieldsCnt: Int): CobolSchemaBuilder = {
      this.generateSegIdFieldsCnt = generateSegIdFieldsCnt
      this
    }

    def withSegmentIdProvidedPrefix(segmentIdProvidedPrefix: String): CobolSchemaBuilder = {
      this.segmentIdProvidedPrefix = segmentIdProvidedPrefix
      this
    }

    def withMetadataPolicy(metadataPolicy: MetadataPolicy): CobolSchemaBuilder = {
      this.metadataPolicy = metadataPolicy
      this
    }

    def build(): CobolSchema = {
      new CobolSchema(
        copybook,
        schemaRetentionPolicy,
        isDisplayAlwaysString,
        strictIntegralPrecision,
        inputFileNameField,
        generateRecordId,
        generateRecordBytes,
        generateCorruptFields,
        generateSegIdFieldsCnt,
        segmentIdProvidedPrefix,
        metadataPolicy
      )
    }
  }
}
