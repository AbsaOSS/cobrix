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

package za.co.absa.cobrix.spark.cobol

import org.apache.spark.sql.types._
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.policies.MetadataPolicy
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.parameters.MetadataFields.MAX_LENGTH
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema
import za.co.absa.cobrix.spark.cobol.source.base.SimpleComparisonBase

class CobolSchemaSpec extends AnyWordSpec with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  "for simple copybooks" should {
    val copyBookContents: String =
      """       01  RECORD.
        |      ******************************************************************
        |      *             This is an example COBOL copybook
        |      ******************************************************************
        |           05  BIN-INT                  PIC S9(4)  COMP.
        |           05  STRUCT-FLD.
        |               10  STR-FLD
        |                   PIC X(10).
        |           05  DATA-STRUCT.
        |               10  EXAMPLE-INT-FLD      PIC 9(07) COMP-3.
        |               10  EXAMPLE-STR-FLD      PIC X(06).
        |""".stripMargin

    "Derive Spark schema from a Copybook" in {
      val expectedSchema =
        """root
          | |-- BIN_INT: integer (nullable = true)
          | |-- STRUCT_FLD: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          | |-- DATA_STRUCT: struct (nullable = true)
          | |    |-- EXAMPLE_INT_FLD: integer (nullable = true)
          | |    |-- EXAMPLE_STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBookContents)
      val cobolSchema = CobolSchema.builder(parsedSchema).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "Derive integral strict precision Spark schema from a Copybook" in {
      val expectedSchema =
        """root
          | |-- BIN_INT: decimal(4,0) (nullable = true)
          | |-- STRUCT_FLD: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          | |-- DATA_STRUCT: struct (nullable = true)
          | |    |-- EXAMPLE_INT_FLD: decimal(7,0) (nullable = true)
          | |    |-- EXAMPLE_STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBookContents)
      val cobolSchema = CobolSchema.builder(parsedSchema).withStrictIntegralPrecision(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "Generate record id field" in {
      val expectedSchema: String =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- BIN_INT: integer (nullable = true)
          | |-- STRUCT_FLD: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          | |-- DATA_STRUCT: struct (nullable = true)
          | |    |-- EXAMPLE_INT_FLD: integer (nullable = true)
          | |    |-- EXAMPLE_STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBookContents)
      val cobolSchema = CobolSchema.builder(parsedSchema).withGenerateRecordId(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "Generate record bytes field" in {
      val expectedSchema: String =
        """root
          | |-- Record_Bytes: binary (nullable = false)
          | |-- BIN_INT: integer (nullable = true)
          | |-- STRUCT_FLD: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          | |-- DATA_STRUCT: struct (nullable = true)
          | |    |-- EXAMPLE_INT_FLD: integer (nullable = true)
          | |    |-- EXAMPLE_STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBookContents)
      val cobolSchema = CobolSchema.builder(parsedSchema).withGenerateRecordBytes(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "Generate record id and byte fields" in {
      val expectedSchema: String =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- Record_Bytes: binary (nullable = false)
          | |-- BIN_INT: integer (nullable = true)
          | |-- STRUCT_FLD: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          | |-- DATA_STRUCT: struct (nullable = true)
          | |    |-- EXAMPLE_INT_FLD: integer (nullable = true)
          | |    |-- EXAMPLE_STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBookContents)
      val cobolSchema = CobolSchema.builder(parsedSchema).withGenerateRecordId(true).withGenerateRecordBytes(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }
  }

  "for copybook parsing with modifiers" should {
    val copyBook: String =
      """       01  STRUCT1.
        |           05  IntValue            PIC 9(6)  COMP.
        |       01  STRUCT2.
        |           10  STR-FLD             PIC X(10).
        |""".stripMargin

    "keep original + record id generation" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal).withGenerateRecordId(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "keep original and no record id generation" in {
      val expectedSchema =
        """root
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "collapse root + record id generation" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- IntValue: integer (nullable = true)
          | |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot).withGenerateRecordId(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "collapse root + record bytes generation" in {
      val expectedSchema =
        """root
          | |-- Record_Bytes: binary (nullable = false)
          | |-- IntValue: integer (nullable = true)
          | |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot).withGenerateRecordBytes(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "collapse root + record id and bytes generation" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- Record_Bytes: binary (nullable = false)
          | |-- IntValue: integer (nullable = true)
          | |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot).withGenerateRecordBytes(true).withGenerateRecordId(true).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "collapse root and no record id generation" in {
      val expectedSchema =
        """root
          | |-- IntValue: integer (nullable = true)
          | |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")

      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }
  }

  "for multi-segment copybook parsing with modifiers" should {
    val copyBook: String =
      """       01  STRUCT1.
        |           05  IntValue            PIC 9(6)  COMP.
        |       01  STRUCT2.
        |           10  STR-FLD             PIC X(10).
        |""".stripMargin

    "multi-segment keep-original with record id generation" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal).withGenerateRecordId(true).withGenerateSegIdFieldsCnt(2).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment keep-original with record bytes generation" in {
      val expectedSchema =
        """root
          | |-- Record_Bytes: binary (nullable = false)
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal).withGenerateRecordBytes(true).withGenerateSegIdFieldsCnt(2).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment keep-original with corrupt record generation" in {
      val expectedSchema =
        """root
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          | |-- _corrupt_fields: array (nullable = false)
          | |    |-- element: struct (containsNull = false)
          | |    |    |-- field_name: string (nullable = false)
          | |    |    |-- raw_value: binary (nullable = false)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal).withGenerateCorruptFields(true).withGenerateSegIdFieldsCnt(2).build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment keep-original with record id and bytes generation" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- Record_Bytes: binary (nullable = false)
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema)
        .withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal)
        .withGenerateRecordId(true)
        .withGenerateRecordBytes(true)
        .withGenerateSegIdFieldsCnt(2)
        .build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment keep-original with record id and bytes generation and strict integral precision" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- Record_Bytes: binary (nullable = false)
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: decimal(6,0) (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema)
        .withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal)
        .withStrictIntegralPrecision(true)
        .withGenerateRecordId(true)
        .withGenerateRecordBytes(true)
        .withGenerateSegIdFieldsCnt(2)
        .build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment keep-original without record id generation" in {
      val expectedSchema =
        """root
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- STRUCT1: struct (nullable = true)
          | |    |-- IntValue: integer (nullable = true)
          | |-- STRUCT2: struct (nullable = true)
          | |    |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema)
        .withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal)
        .withGenerateSegIdFieldsCnt(2)
        .build()

      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment collapse root with record id generation" in {
      val expectedSchema =
        """root
          | |-- File_Id: integer (nullable = false)
          | |-- Record_Id: long (nullable = false)
          | |-- Record_Byte_Length: integer (nullable = false)
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- IntValue: integer (nullable = true)
          | |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema)
        .withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot)
        .withGenerateRecordId(true)
        .withGenerateSegIdFieldsCnt(2)
        .build()
      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }

    "multi-segment collapse root without record id generation" in {
      val expectedSchema =
        """root
          | |-- Seg_Id0: string (nullable = true)
          | |-- Seg_Id1: string (nullable = true)
          | |-- IntValue: integer (nullable = true)
          | |-- STR_FLD: string (nullable = true)
          |""".stripMargin.replaceAll("[\\r\\n]", "\n")
      val parsedSchema = CopybookParser.parseTree(copyBook)
      val cobolSchema = CobolSchema.builder(parsedSchema)
        .withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot)
        .withGenerateSegIdFieldsCnt(2)
        .build()

      val actualSchema = cobolSchema.getSparkSchema.treeString

      assertEqualsMultiline(actualSchema, expectedSchema)
    }
  }

  "Metadata generation for OCCURS" in {
    val copyBook: String =
      """       01  RECORD.
        |         05  FIELD1                  PIC X(10).
        |         05  ARRAY1        PIC X(3) OCCURS 2 TO 5 TIMES.
        |         05  ARRAY2                 OCCURS 10.
        |           10  STRUCT1.
        |             20  IntValue        PIC 9(6)  COMP.
        |""".stripMargin

    val parsedSchema = CopybookParser.parseTree(copyBook)

    val cobolSchema1 = CobolSchema.builder(parsedSchema).withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal).build()

    val actualSparkSchema = cobolSchema1.getSparkSchema

    val rootField = actualSparkSchema.fields.head.dataType.asInstanceOf[StructType]

    val metadataPrimitive = rootField.fields(1).metadata
    val metadataStruct = rootField.fields(2).metadata

    assert(metadataPrimitive.contains("minElements"))
    assert(metadataStruct.contains("minElements"))
    assert(metadataPrimitive.contains("maxElements"))
    assert(metadataStruct.contains("maxElements"))

    assert(metadataPrimitive.getLong("minElements") == 2)
    assert(metadataStruct.getLong("minElements") == 0)
    assert(metadataPrimitive.getLong("maxElements") == 5)
    assert(metadataStruct.getLong("maxElements") == 10)
  }

  "Metadata generation for string types" in {
    val copyBook: String =
      """       01  RECORD.
        |         05  STR1                  PIC X(10).
        |         05  STR2                  PIC A(7).
        |         05  NUM3                  PIC 9(7).
        |""".stripMargin

    val parsedSchema = CopybookParser.parseTree(copyBook)

    val cobolSchema1 = CobolSchema.builder(parsedSchema)
      .withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot)
      .build()

    val actualSparkSchema = cobolSchema1.getSparkSchema

    val metadataStr1 = actualSparkSchema.fields.head.metadata
    val metadataStr2 = actualSparkSchema.fields(1).metadata
    val metadataNum3 = actualSparkSchema.fields(2).metadata

    assert(metadataStr1.contains(MAX_LENGTH))
    assert(metadataStr2.contains(MAX_LENGTH))
    assert(!metadataNum3.contains(MAX_LENGTH))

    actualSparkSchema.fields(1).metadata.getLong(MAX_LENGTH)

    assert(metadataStr1.getLong(MAX_LENGTH) == 10)
    assert(metadataStr2.getLong(MAX_LENGTH) == 7)
  }

  "String display types" in {
    val copyBook: String =
      """       01  RECORD.
        |         05  STR1                  PIC 9(10).
        |         05  STR2                  PIC S9(7).
        |         05  NUM3                  PIC 9V99(7).
        |""".stripMargin

    val expectedSchema =
      """root
        | |-- Seg_Id0: string (nullable = true)
        | |-- Seg_Id1: string (nullable = true)
        | |-- STR1: string (nullable = true)
        | |-- STR2: string (nullable = true)
        | |-- NUM3: decimal(9,8) (nullable = true)""".stripMargin

    val parsedSchema = CopybookParser.parseTree(copyBook)
    val cobolSchema = CobolSchema.builder(parsedSchema)
      .withSchemaRetentionPolicy(SchemaRetentionPolicy.CollapseRoot)
      .withIsDisplayAlwaysString(true)
      .withGenerateSegIdFieldsCnt(2)
      .build()

    val actualSchema = cobolSchema.getSparkSchema.treeString

    assertEqualsMultiline(actualSchema, expectedSchema)
  }

  "fromSparkOptions" should {
    "return a schema for a copybook" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |         05  STR2                  PIC A(7).
          |         05  NUM3                  PIC 9(7).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook), Map.empty)

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 3)
      assert(sparkSchema.fields.head.name == "STR1")
      assert(sparkSchema.fields.head.dataType == StringType)
      assert(sparkSchema.fields(1).name == "STR2")
      assert(sparkSchema.fields(1).dataType == StringType)
      assert(sparkSchema.fields(2).name == "NUM3")
      assert(sparkSchema.fields(2).dataType == IntegerType)
    }

    "return a schema for a copybook with strict decimal" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |         05  NUM2                  PIC S9(12).
          |         05  NUM3                  PIC 9(7).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook), Map("strict_integral_precision" -> "true"))

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 3)
      assert(sparkSchema.fields.head.name == "STR1")
      assert(sparkSchema.fields.head.dataType == StringType)
      assert(sparkSchema.fields(1).name == "NUM2")
      assert(sparkSchema.fields(1).dataType == DecimalType(12, 0))
      assert(sparkSchema.fields(2).name == "NUM3")
      assert(sparkSchema.fields(2).dataType == DecimalType(7, 0))
    }

    "return a schema for multiple copybooks" in {
      val copybook1: String =
        """       01  RECORD1.
          |         05  STR1                  PIC X(10).
          |         05  STR2                  PIC A(7).
          |         05  NUM3                  PIC 9(7).
          |""".stripMargin

      val copybook2: String =
        """       01  RECORD2.
          |         05  STR4                  PIC X(10).
          |         05  STR5                  PIC A(7).
          |         05  NUM6                  PIC 9(7).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook1, copybook2), Map("schema_retention_policy" -> "keep_original"))

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 2)
      assert(sparkSchema.fields.head.name == "RECORD1")
      assert(sparkSchema.fields.head.dataType.isInstanceOf[StructType])
      assert(sparkSchema.fields(1).name == "RECORD2")
      assert(sparkSchema.fields(1).dataType.isInstanceOf[StructType])
      assert(cobolSchema.getCobolSchema.ast.children.head.isRedefined)
      assert(cobolSchema.getCobolSchema.ast.children(1).redefines.contains("RECORD1"))
    }

    "return a schema for a hierarchical copybook" in {
      val copybook: String =
        """       01  RECORD.
          |         05  HEADER                PIC X(5).
          |         05  SEGMENT-ID            PIC X(2).
          |         05  SEG1.
          |           10  FIELD1              PIC 9(7).
          |         05  SEG2 REDEFINES SEG1.
          |           10  FIELD3              PIC X(7).
          |         05  SEG3 REDEFINES SEG1.
          |           10  FIELD4              PIC S9(7).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook),
        Map(
          "segment_field" -> "SEGMENT-ID",
          "redefine-segment-id-map:0" -> "SEG1 => 01",
          "redefine-segment-id-map:1" -> "SEG2 => 02",
          "redefine-segment-id-map:2" -> "SEG3 => 03,0A",
          "segment-children:1" -> "SEG1 => SEG2",
          "segment-children:2" -> "SEG1 => SEG3"
        )
      )

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 3)
      assert(sparkSchema.fields.head.name == "HEADER")
      assert(sparkSchema.fields.head.dataType == StringType)
      assert(sparkSchema.fields(1).name == "SEGMENT_ID")
      assert(sparkSchema.fields(1).dataType == StringType)
      assert(sparkSchema.fields(2).name == "SEG1")
      assert(sparkSchema.fields(2).dataType.isInstanceOf[StructType])

      val seg1 = sparkSchema.fields(2).dataType.asInstanceOf[StructType]
      assert(seg1.fields.length == 3)
      assert(seg1.fields.head.name == "FIELD1")
      assert(seg1.fields.head.dataType == IntegerType)
      assert(seg1.fields(1).name == "SEG2")
      assert(seg1.fields(1).dataType.isInstanceOf[ArrayType])
      assert(seg1.fields(2).name == "SEG3")
      assert(seg1.fields(2).dataType.isInstanceOf[ArrayType])
    }

    "return a schema for a multi-segment copybook" in {
      val copybook: String =
        """       01  RECORD.
          |         05  HEADER                PIC X(5).
          |         05  SEGMENT-ID            PIC X(2).
          |         05  SEG1.
          |           10  FIELD1              PIC 9(7).
          |         05  SEG2 REDEFINES SEG1.
          |           10  FIELD3              PIC X(7).
          |         05  SEG3 REDEFINES SEG1.
          |           10  FIELD4              PIC S9(7).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook),
        Map(
          "segment_field" -> "SEGMENT-ID",
          "redefine-segment-id-map:0" -> "SEG1 => 01",
          "redefine-segment-id-map:1" -> "SEG2 => 02",
          "redefine-segment-id-map:2" -> "SEG3 => 03",
          "segment_field" -> "SEGMENT-ID",
          "segment_id_level0" -> "TEST",
          "generate_record_id" -> "true"
        )
      )

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 9)
      assert(sparkSchema.fields.head.name == "File_Id")
      assert(sparkSchema.fields.head.dataType == IntegerType)
      assert(sparkSchema.fields(1).name == "Record_Id")
      assert(sparkSchema.fields(1).dataType == LongType)
      assert(sparkSchema.fields(2).name == "Record_Byte_Length")
      assert(sparkSchema.fields(2).dataType == IntegerType)
      assert(sparkSchema.fields(3).name == "Seg_Id0")
      assert(sparkSchema.fields(3).dataType == StringType)
      assert(sparkSchema.fields(4).name == "HEADER")
      assert(sparkSchema.fields(4).dataType == StringType)
      assert(sparkSchema.fields(5).name == "SEGMENT_ID")
      assert(sparkSchema.fields(5).dataType == StringType)
      assert(sparkSchema.fields(6).name == "SEG1")
      assert(sparkSchema.fields(6).dataType.isInstanceOf[StructType])
      assert(sparkSchema.fields(7).name == "SEG2")
      assert(sparkSchema.fields(7).dataType.isInstanceOf[StructType])
      assert(sparkSchema.fields(8).name == "SEG3")
      assert(sparkSchema.fields(8).dataType.isInstanceOf[StructType])
    }

    "work in the case insensitive way" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook),
        Map(
          "pedantic" -> "true",
          "generate_RECORD_id" -> "true"
        )
      )

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 4)
    }

    "fail on redundant options when pedantic mode is turned on" in {
      val copybook: String =
        """       01  RECORD.
          |         05  DATA                PIC X(5).
          |""".stripMargin

      val ex = intercept[IllegalArgumentException] {
        CobolSchema.fromSparkOptions(Seq(copybook),
          Map(
            "pedantic" -> "true",
            "dummy_option" -> "dummy_value"
          )
        )
      }

      assert(ex.getMessage == "Redundant or unrecognized option(s) to 'spark-cobol': dummy_option.")
    }
  }

  "getMaximumSegmentIdLength" should {
    val copybook: String =
      """       01  RECORD.
        |         05  STR1                  PIC X(10).
        |""".stripMargin

    val cobolSchema = CobolSchema.fromSparkOptions(Seq(copybook), Map.empty)

    "return proper size for autogenerated prefix" in {
      assert(cobolSchema.getMaximumSegmentIdLength("") == 65)
    }

    "return proper size for provided prefix" in {
      assert(cobolSchema.getMaximumSegmentIdLength("ID_") == 53)
    }
  }

  "builder" should {
    "create schema with default settings using builder" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |         05  NUM2                  PIC 9(7).
          |""".stripMargin

      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 2)
      assert(sparkSchema.fields.head.name == "STR1")
      assert(sparkSchema.fields.head.dataType == StringType)
      assert(sparkSchema.fields(1).name == "NUM2")
      assert(sparkSchema.fields(1).dataType == IntegerType)
    }

    "create schema with strict integral precision using builder" in {
      val copybook: String =
        """       01  RECORD.
          |         05  NUM1                  PIC 9(12).
          |         05  NUM2                  PIC S9(7).
          |""".stripMargin

      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withStrictIntegralPrecision(true)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 2)
      assert(sparkSchema.fields.head.name == "NUM1")
      assert(sparkSchema.fields.head.dataType == DecimalType(12, 0))
      assert(sparkSchema.fields(1).name == "NUM2")
      assert(sparkSchema.fields(1).dataType == DecimalType(7, 0))
    }

    "create schema with record id generation using builder" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |""".stripMargin

      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withGenerateRecordId(true)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 4)
      assert(sparkSchema.fields.head.name == "File_Id")
      assert(sparkSchema.fields.head.dataType == IntegerType)
      assert(sparkSchema.fields(1).name == "Record_Id")
      assert(sparkSchema.fields(1).dataType == LongType)
      assert(sparkSchema.fields(2).name == "Record_Byte_Length")
      assert(sparkSchema.fields(2).dataType == IntegerType)
      assert(sparkSchema.fields(3).name == "STR1")
    }

    "create schema with record bytes generation using builder" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |""".stripMargin
      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withGenerateRecordBytes(true)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 2)
      assert(sparkSchema.fields.head.name == "Record_Bytes")
      assert(sparkSchema.fields.head.dataType == BinaryType)
      assert(sparkSchema.fields(1).name == "STR1")
    }

    "create schema with both record id and bytes using builder" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |""".stripMargin

      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withGenerateRecordId(true)
        .withGenerateRecordBytes(true)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 5)
      assert(sparkSchema.fields.head.name == "File_Id")
      assert(sparkSchema.fields(1).name == "Record_Id")
      assert(sparkSchema.fields(2).name == "Record_Byte_Length")
      assert(sparkSchema.fields(3).name == "Record_Bytes")
      assert(sparkSchema.fields(3).dataType == BinaryType)
      assert(sparkSchema.fields(4).name == "STR1")
    }

    "create schema with schema retention policy using builder" in {
      val copybook: String =
        """       01  STRUCT1.
          |           05  IntValue            PIC 9(6)  COMP.
          |       01  STRUCT2.
          |           10  STR-FLD             PIC X(10).
          |""".stripMargin

      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withSchemaRetentionPolicy(SchemaRetentionPolicy.KeepOriginal)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 2)
      assert(sparkSchema.fields.head.name == "STRUCT1")
      assert(sparkSchema.fields.head.dataType.isInstanceOf[StructType])
      assert(sparkSchema.fields(1).name == "STRUCT2")
      assert(sparkSchema.fields(1).dataType.isInstanceOf[StructType])
    }

    "create schema with corrupt fields using builder" in {
      val copybook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |""".stripMargin
      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withGenerateCorruptFields(true)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 2)
      assert(sparkSchema.fields(1).name == "_corrupt_fields")
      assert(sparkSchema.fields(1).dataType.isInstanceOf[ArrayType])
    }

    "create schema with various options" in {
      val copybook: String =
        """       01  RECORD.
          |         05  NUM1                  PIC 9(10).
          |""".stripMargin
      val parsedCopybook = CopybookParser.parse(copybook)
      val cobolSchema = CobolSchema.builder(parsedCopybook)
        .withIsDisplayAlwaysString(true)
        .withInputFileNameField("file_name")
        .withGenerateSegIdFieldsCnt(1)
        .withSegmentIdProvidedPrefix("segid")
        .withMetadataPolicy(MetadataPolicy.Extended)
        .build()

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 3)
      assert(sparkSchema.fields(0).name == "file_name")
      assert(sparkSchema.fields(1).name == "Seg_Id0")
      assert(sparkSchema.fields(2).name == "NUM1")
      assert(sparkSchema.fields(2).dataType.isInstanceOf[StringType])
    }
  }
}
