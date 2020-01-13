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

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.schema.{CobolSchema, SchemaRetentionPolicy}

class CobolSchemaSpec extends FunSuite {

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

  test("Test simple Spark schema devivation from a Copybook") {
    val expectedSchema: String = "StructType(StructField(BIN_INT,IntegerType,true), StructField(STRUCT_FLD,StructType" +
      "(StructField(STR_FLD,StringType,true))," +
      "true), StructField(DATA_STRUCT,StructType(StructField(EXAMPLE_INT_FLD,IntegerType,true), StructField(EXAMPLE_STR_FLD,StringType,true)),true))"

    val parsedSchema = CopybookParser.parseTree(copyBookContents)
    val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false)
    val actualSchema = cobolSchema.getSparkSchema.toString()

    assert(actualSchema == expectedSchema)
  }

  test("Test generation of record id field") {
    val expectedSchema: String = "StructType(StructField(File_Id,IntegerType,false), StructField(Record_Id,LongType,false), StructField(BIN_INT," +
      "IntegerType,true), StructField(STRUCT_FLD," +
      "StructType" +
      "(StructField(STR_FLD,StringType,true))," +
      "true), StructField(DATA_STRUCT,StructType(StructField(EXAMPLE_INT_FLD,IntegerType,true), StructField(EXAMPLE_STR_FLD,StringType,true)),true))"

    val parsedSchema = CopybookParser.parseTree(copyBookContents)
    val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true)
    val actualSchema = cobolSchema.getSparkSchema.toString()

    assert(actualSchema == expectedSchema)
  }

  test("Test struct case of record id generation") {
    val copyBook: String =
      """       01  STRUCT1.
        |           05  IntValue            PIC 9(6)  COMP.
        |       01  STRUCT2.
        |           10  STR-FLD             PIC X(10).
        |""".stripMargin

    val expectedSchemaWithRecordId: String =
      "StructType(StructField(File_Id,IntegerType,false), StructField(Record_Id,LongType,false), StructField(STRUCT1,StructType(StructField(IntValue," +
        "IntegerType,true)),true), StructField" +
        "(STRUCT2,StructType(StructField(STR_FLD,StringType,true)),true))"
    val expectedSchemaWithoutRecordId: String =
      "StructType(StructField(STRUCT1,StructType(StructField(IntValue,IntegerType,true)),true), StructField(STRUCT2,StructType(StructField(STR_FLD," +
        "StringType,true)),true))"

    val expectedCollapsedSchemaWithRecordId: String =
      "StructType(StructField(File_Id,IntegerType,false), StructField(Record_Id,LongType,false), StructField(IntValue,IntegerType,true), StructField" +
        "(STR_FLD,StringType,true))"
    val expectedCollapsedSchemaWithoutRecordId: String =
      "StructType(StructField(IntValue,IntegerType,true), StructField(STR_FLD,StringType,true))"

    val parsedSchema = CopybookParser.parseTree(copyBook)
    val cobolSchema1 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", true)
    val actualSchema1 = cobolSchema1.getSparkSchema.toString()

    assert(actualSchema1 == expectedSchemaWithRecordId)

    val cobolSchema2 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", false)
    val actualSchema2 = cobolSchema2.getSparkSchema.toString()

    assert(actualSchema2 == expectedSchemaWithoutRecordId)

    val cobolSchema3 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true)
    val actualSchema3 = cobolSchema3.getSparkSchema.toString()

    assert(actualSchema3 == expectedCollapsedSchemaWithRecordId)

    val cobolSchema4 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false)
    val actualSchema4 = cobolSchema4.getSparkSchema.toString()

    assert(actualSchema4 == expectedCollapsedSchemaWithoutRecordId)
  }

  test("Test struct case of segment ids generation") {
    val copyBook: String =
      """       01  STRUCT1.
        |           05  IntValue            PIC 9(6)  COMP.
        |       01  STRUCT2.
        |           10  STR-FLD             PIC X(10).
        |""".stripMargin

    val expectedSchemaWithRecordId: String =
      "StructType(StructField(File_Id,IntegerType,false), StructField(Record_Id,LongType,false), " +
        "StructField(Seg_Id0,StringType,true), StructField(Seg_Id1,StringType,true), " +
        "StructField(STRUCT1,StructType(StructField(IntValue," +
        "IntegerType,true)),true), StructField" +
        "(STRUCT2,StructType(StructField(STR_FLD,StringType,true)),true))"

    val expectedSchemaWithoutRecordId: String =
      "StructType(" +
        "StructField(Seg_Id0,StringType,true), StructField(Seg_Id1,StringType,true), " +
        "StructField(STRUCT1,StructType(StructField(IntValue,IntegerType,true)),true), StructField(STRUCT2,StructType(StructField(STR_FLD," +
        "StringType,true)),true))"

    val expectedCollapsedSchemaWithRecordId: String =
      "StructType(StructField(File_Id,IntegerType,false), StructField(Record_Id,LongType,false), " +
        "StructField(Seg_Id0,StringType,true), StructField(Seg_Id1,StringType,true), " +
        "StructField(IntValue,IntegerType,true), StructField" +
        "(STR_FLD,StringType,true))"
    val expectedCollapsedSchemaWithoutRecordId: String =
      "StructType(" +
        "StructField(Seg_Id0,StringType,true), StructField(Seg_Id1,StringType,true), " +
        "StructField(IntValue,IntegerType,true), StructField(STR_FLD,StringType,true))"


    val parsedSchema = CopybookParser.parseTree(copyBook)
    val cobolSchema1 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", true, 2)
    val actualSchema1 = cobolSchema1.getSparkSchema.toString()

    assert(actualSchema1 == expectedSchemaWithRecordId)

    val cobolSchema2 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.KeepOriginal, "", false, 2)
    val actualSchema2 = cobolSchema2.getSparkSchema.toString()

    assert(actualSchema2 == expectedSchemaWithoutRecordId)

    val cobolSchema3 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", true, 2)
    val actualSchema3 = cobolSchema3.getSparkSchema.toString()

    assert(actualSchema3 == expectedCollapsedSchemaWithRecordId)

    val cobolSchema4 = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "", false, 2)
    val actualSchema4 = cobolSchema4.getSparkSchema.toString()

    assert(actualSchema4 == expectedCollapsedSchemaWithoutRecordId)
  }


}
