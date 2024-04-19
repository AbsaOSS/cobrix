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

import org.apache.spark.sql.types.{IntegerType, StringType, StructType}
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, false)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true, false)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, true)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true, true)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", true, false)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", false, false)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true, false)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, true)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true, true)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, false)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", true, false, 2)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", false, true, 2)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", true, true, 2)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", false, false, 2)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true, false, 2)
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
      val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, false, 2)
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

    val cobolSchema1 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", false, false)
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

    val cobolSchema1 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, false)
    val actualSparkSchema = cobolSchema1.getSparkSchema

    val metadataStr1 = actualSparkSchema.fields.head.metadata
    val metadataStr2 = actualSparkSchema.fields(1).metadata
    val metadataNum3 = actualSparkSchema.fields(2).metadata

    assert(metadataStr1.contains("maxLength"))
    assert(metadataStr2.contains("maxLength"))
    assert(!metadataNum3.contains("maxLength"))

    actualSparkSchema.fields(1).metadata.getLong("maxLength")

    assert(metadataStr1.getLong("maxLength") == 10)
    assert(metadataStr2.getLong("maxLength") == 7)
  }

  "fromSparkOptions" should {
    "return a schema for a copybook" in {
      val copyBook: String =
        """       01  RECORD.
          |         05  STR1                  PIC X(10).
          |         05  STR2                  PIC A(7).
          |         05  NUM3                  PIC 9(7).
          |""".stripMargin

      val cobolSchema = CobolSchema.fromSparkOptions(Seq(copyBook), Map.empty)

      val sparkSchema = cobolSchema.getSparkSchema

      assert(sparkSchema.fields.length == 3)
      assert(sparkSchema.fields.head.name == "STR1")
      assert(sparkSchema.fields.head.dataType == StringType)
      assert(sparkSchema.fields(1).name == "STR2")
      assert(sparkSchema.fields(1).dataType == StringType)
      assert(sparkSchema.fields(2).name == "NUM3")
      assert(sparkSchema.fields(2).dataType == IntegerType)
    }
  }

}
