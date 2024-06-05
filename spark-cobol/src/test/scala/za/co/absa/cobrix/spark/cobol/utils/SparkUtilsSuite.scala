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

import org.apache.spark.sql.types._
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}
import za.co.absa.cobrix.spark.cobol.utils.TestUtils._

import java.nio.charset.StandardCharsets
import scala.util.Properties

class SparkUtilsSuite extends AnyFunSuite with SparkTestBase with BinaryFileFixture with TextComparisonFixture {

  import spark.implicits._

  private val logger = LoggerFactory.getLogger(this.getClass)

  val nestedSampleData: List[String] =
    """[{"id":1,"legs":[{"legid":100,"conditions":[{"checks":[{"checkNums":["1","2","3b","4","5c","6"]}],"amount":100}]}]}]""" ::
      """[{"id":2,"legs":[{"legid":200,"conditions":[{"checks":[{"checkNums":["3","4","5b","6","7c","8"]}],"amount":200}]}]}]""" ::
      """[{"id":3,"legs":[{"legid":300,"conditions":[{"checks":[{"checkNums":["6","7","8b","9","0c","1"]}],"amount":300}]}]}]""" ::
      """[{"id":4,"legs":[]}]""" ::
      """[{"id":5,"legs":null}]""" :: Nil

  test("Test schema flattening of multiple nested structure") {
    val expectedOrigSchema =
      """root
        | |-- id: long (nullable = true)
        | |-- legs: array (nullable = true)
        | |    |-- element: struct (containsNull = true)
        | |    |    |-- conditions: array (nullable = true)
        | |    |    |    |-- element: struct (containsNull = true)
        | |    |    |    |    |-- amount: long (nullable = true)
        | |    |    |    |    |-- checks: array (nullable = true)
        | |    |    |    |    |    |-- element: struct (containsNull = true)
        | |    |    |    |    |    |    |-- checkNums: array (nullable = true)
        | |    |    |    |    |    |    |    |-- element: string (containsNull = true)
        | |    |    |-- legid: long (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")
    val expectedOrigData =
      """{"id":1,"legs":[{"conditions":[{"amount":100,"checks":[{"checkNums":["1","2","3b","4","5c","6"]}]}],"legid":100}]}
        |{"id":2,"legs":[{"conditions":[{"amount":200,"checks":[{"checkNums":["3","4","5b","6","7c","8"]}]}],"legid":200}]}
        |{"id":3,"legs":[{"conditions":[{"amount":300,"checks":[{"checkNums":["6","7","8b","9","0c","1"]}]}],"legid":300}]}
        |{"id":4,"legs":[]}
        |{"id":5}""".stripMargin.replace("\r\n", "\n")

    val expectedFlatSchema =
      """root
        | |-- id: long (nullable = true)
        | |-- legs_0_conditions_0_amount: long (nullable = true)
        | |-- legs_0_conditions_0_checks_0_checkNums_0: string (nullable = true)
        | |-- legs_0_conditions_0_checks_0_checkNums_1: string (nullable = true)
        | |-- legs_0_conditions_0_checks_0_checkNums_2: string (nullable = true)
        | |-- legs_0_conditions_0_checks_0_checkNums_3: string (nullable = true)
        | |-- legs_0_conditions_0_checks_0_checkNums_4: string (nullable = true)
        | |-- legs_0_conditions_0_checks_0_checkNums_5: string (nullable = true)
        | |-- legs_0_legid: long (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")
    val expectedFlatData =
      """+---+--------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+------------+
        ||id |legs_0_conditions_0_amount|legs_0_conditions_0_checks_0_checkNums_0|legs_0_conditions_0_checks_0_checkNums_1|legs_0_conditions_0_checks_0_checkNums_2|legs_0_conditions_0_checks_0_checkNums_3|legs_0_conditions_0_checks_0_checkNums_4|legs_0_conditions_0_checks_0_checkNums_5|legs_0_legid|
        |+---+--------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+------------+
        ||1  |100                       |1                                       |2                                       |3b                                      |4                                       |5c                                      |6                                       |100         |
        ||2  |200                       |3                                       |4                                       |5b                                      |6                                       |7c                                      |8                                       |200         |
        ||3  |300                       |6                                       |7                                       |8b                                      |9                                       |0c                                      |1                                       |300         |
        ||4  |null                      |null                                    |null                                    |null                                    |null                                    |null                                    |null                                    |null        |
        ||5  |null                      |null                                    |null                                    |null                                    |null                                    |null                                    |null                                    |null        |
        |+---+--------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+------------+
        |
        |""".stripMargin.replace("\r\n", "\n")

    val df = spark.read.json(nestedSampleData.toDS)
    val dfFlattened = SparkUtils.flattenSchema(df)

    val originalSchema = df.schema.treeString
    val originalData = df.toJSON.collect().mkString("\n")

    val flatSchema = dfFlattened.schema.treeString
    val flatData = showString(dfFlattened)

    assertSchema(originalSchema, expectedOrigSchema)
    assertResults(originalData, expectedOrigData)

    assertSchema(flatSchema, expectedFlatSchema)
    assertResults(flatData, expectedFlatData)
  }

  test("Test metadata is retained") {
    val metadata1 = new MetadataBuilder().putLong("test_metadata1", 123).build()
    val metadata2 = new MetadataBuilder().putLong("test_metadata2", 456).build()
    val metadata3 = new MetadataBuilder().putLong("test_metadata3", 789).build()

    val schema = StructType(Array(
      StructField("id", LongType, nullable = true, metadata = metadata1),
      StructField("legs", ArrayType(StructType(List(
        StructField("conditions", ArrayType(StructType(List(
          StructField("amount", LongType, nullable = true),
          StructField("checks", ArrayType(StructType(List(
            StructField("checkNums", ArrayType(StringType, containsNull = true), nullable = true, metadata = metadata3)
          )), containsNull = true), nullable = true))), containsNull = true), nullable = true),
        StructField("legid", LongType, nullable = true, metadata = metadata2))), containsNull = true), nullable = true)))

    val df = spark.read.schema(schema).json(nestedSampleData.toDS)
    val dfFlattened = SparkUtils.flattenSchema(df)

    assert(dfFlattened.schema.fields(0).metadata.getLong("test_metadata1") == 123)
    assert(dfFlattened.schema.fields.find(_.name == "id").get.metadata.getLong("test_metadata1") == 123)
    assert(dfFlattened.schema.fields.find(_.name == "legs_0_legid").get.metadata.getLong("test_metadata2") == 456)
    assert(dfFlattened.schema.fields.find(_.name == "legs_0_conditions_0_checks_0_checkNums_1").get.metadata.getLong("test_metadata3") == 789)
    assert(dfFlattened.schema.fields.find(_.name == "legs_0_conditions_0_checks_0_checkNums_2").get.metadata.getLong("test_metadata3") == 789)
    assert(dfFlattened.schema.fields.find(_.name == "legs_0_conditions_0_checks_0_checkNums_3").get.metadata.getLong("test_metadata3") == 789)
    assert(dfFlattened.schema.fields.find(_.name == "legs_0_conditions_0_checks_0_checkNums_4").get.metadata.getLong("test_metadata3") == 789)
    assert(dfFlattened.schema.fields.find(_.name == "legs_0_conditions_0_checks_0_checkNums_5").get.metadata.getLong("test_metadata3") == 789)
  }

  test("Test schema flattening when short names are used") {
    val expectedFlatSchema =
      """root
        | |-- id: long (nullable = true)
        | |-- conditions_0_amount: long (nullable = true)
        | |-- checkNums_0: string (nullable = true)
        | |-- checkNums_1: string (nullable = true)
        | |-- checkNums_2: string (nullable = true)
        | |-- checkNums_3: string (nullable = true)
        | |-- checkNums_4: string (nullable = true)
        | |-- checkNums_5: string (nullable = true)
        | |-- legs_0_legid: long (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")
    val expectedFlatData =
      """+---+-------------------+-----------+-----------+-----------+-----------+-----------+-----------+------------+
        ||id |conditions_0_amount|checkNums_0|checkNums_1|checkNums_2|checkNums_3|checkNums_4|checkNums_5|legs_0_legid|
        |+---+-------------------+-----------+-----------+-----------+-----------+-----------+-----------+------------+
        ||1  |100                |1          |2          |3b         |4          |5c         |6          |100         |
        ||2  |200                |3          |4          |5b         |6          |7c         |8          |200         |
        ||3  |300                |6          |7          |8b         |9          |0c         |1          |300         |
        ||4  |null               |null       |null       |null       |null       |null       |null       |null        |
        ||5  |null               |null       |null       |null       |null       |null       |null       |null        |
        |+---+-------------------+-----------+-----------+-----------+-----------+-----------+-----------+------------+
        |
        |""".stripMargin.replace("\r\n", "\n")

    val df = spark.read.json(nestedSampleData.toDS)
    val dfFlattened = SparkUtils.flattenSchema(df, useShortFieldNames = true)

    val flatSchema = dfFlattened.schema.treeString
    val flatData = showString(dfFlattened)

    assertSchema(flatSchema, expectedFlatSchema)
    assertResults(flatData, expectedFlatData)
  }

  test("Test schema flattening of a matrix") {
    val f = List(
      List(
        List(1, 2, 3, 4, 5, 6),
        List(7, 8, 9, 10, 11, 12, 13)
      ), List(
        List(201, 202, 203, 204, 205, 206),
        List(207, 208, 209, 210, 211, 212, 213)
      ), List(
        List(201, 202, 203, 204, 205, 206),
        List(207, 208, 209, 210, 211, 212, 213)
      ), List(
        List(201, 202, 203, 204, 205, 206),
        List(207, 208, 209, 210, 211, 212, 213)
      )
    )

    val expectedOrigSchema =
      """root
        | |-- value: array (nullable = _)
        | |    |-- element: array (containsNull = _)
        | |    |    |-- element: integer (containsNull = _)
        |""".stripMargin.replace("\r\n", "\n")

    val expectedOrigData =
      """{"value":[[1,2,3,4,5,6],[7,8,9,10,11,12,13]]}
        |{"value":[[201,202,203,204,205,206],[207,208,209,210,211,212,213]]}
        |{"value":[[201,202,203,204,205,206],[207,208,209,210,211,212,213]]}
        |{"value":[[201,202,203,204,205,206],[207,208,209,210,211,212,213]]}""".stripMargin.replace("\r\n", "\n")

    val expectedFlatSchema =
      """root
        | |-- value_0_0: integer (nullable = true)
        | |-- value_0_1: integer (nullable = true)
        | |-- value_0_2: integer (nullable = true)
        | |-- value_0_3: integer (nullable = true)
        | |-- value_0_4: integer (nullable = true)
        | |-- value_0_5: integer (nullable = true)
        | |-- value_1_0: integer (nullable = true)
        | |-- value_1_1: integer (nullable = true)
        | |-- value_1_2: integer (nullable = true)
        | |-- value_1_3: integer (nullable = true)
        | |-- value_1_4: integer (nullable = true)
        | |-- value_1_5: integer (nullable = true)
        | |-- value_1_6: integer (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")
    val expectedFlatData =
      """+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+
        ||value_0_0|value_0_1|value_0_2|value_0_3|value_0_4|value_0_5|value_1_0|value_1_1|value_1_2|value_1_3|value_1_4|value_1_5|value_1_6|
        |+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+
        ||1        |2        |3        |4        |5        |6        |7        |8        |9        |10       |11       |12       |13       |
        ||201      |202      |203      |204      |205      |206      |207      |208      |209      |210      |211      |212      |213      |
        ||201      |202      |203      |204      |205      |206      |207      |208      |209      |210      |211      |212      |213      |
        ||201      |202      |203      |204      |205      |206      |207      |208      |209      |210      |211      |212      |213      |
        |+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+
        |
        |""".stripMargin.replace("\r\n", "\n")

    val df = f.toDF()

    val dfFlattened1 = SparkUtils.flattenSchema(df)
    val dfFlattened2 = SparkUtils.flattenSchema(df, useShortFieldNames = true)

    val originalSchema = df.schema.treeString
      .replace("true", "_")
      .replace("false", "_")

    val originalData = df.toJSON.collect().mkString("\n")

    val flatSchema1 = dfFlattened1.schema.treeString
    val flatData1 = showString(dfFlattened1)

    val flatSchema2 = dfFlattened2.schema.treeString
    val flatData2 = showString(dfFlattened2)

    assertSchema(originalSchema, expectedOrigSchema)
    assertResults(originalData, expectedOrigData)

    assertSchema(flatSchema1, expectedFlatSchema)
    assertResults(flatData1, expectedFlatData)

    assertSchema(flatSchema2, expectedFlatSchema)
    assertResults(flatData2, expectedFlatData)
  }

  test("The expected metadata should be present if the data is array of array") {
    val copyBook: String =
      """       01  RECORD.
        |         05  FIELD1                  PIC X(2).
        |         05  ARRAY1        PIC X(1) OCCURS 2 TO 5 TIMES.
        |         05  STRUCT1.
        |           10  ARRAY2                OCCURS 3.
        |             15  STRUCT1.
        |               20  IntValue          PIC 9(1).
        |""".stripMargin

    val data = "AABBBBB123\nCCDDDDD456\n"

    val expectedFlatSchema =
      """root
        | |-- FIELD1: string (nullable = true)
        | |-- ARRAY1_0: string (nullable = true)
        | |-- ARRAY1_1: string (nullable = true)
        | |-- ARRAY1_2: string (nullable = true)
        | |-- ARRAY1_3: string (nullable = true)
        | |-- ARRAY1_4: string (nullable = true)
        | |-- STRUCT1_ARRAY2_0_STRUCT1_IntValue: integer (nullable = true)
        | |-- STRUCT1_ARRAY2_1_STRUCT1_IntValue: integer (nullable = true)
        | |-- STRUCT1_ARRAY2_2_STRUCT1_IntValue: integer (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")

    withTempTextFile("fletten", "test", StandardCharsets.UTF_8, data) { filePath =>

      val df = spark.read
        .format("cobol")
        .option("copybook_contents", copyBook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .load(filePath)

      val metadataPrimitive = df.schema.fields(1).metadata
      val metadataStruct = df.schema.fields(2).dataType.asInstanceOf[StructType].fields.head.metadata

      assert(metadataPrimitive.contains("minElements"))
      assert(metadataStruct.contains("minElements"))
      assert(metadataPrimitive.contains("maxElements"))
      assert(metadataStruct.contains("maxElements"))

      assert(metadataPrimitive.getLong("minElements") == 2)
      assert(metadataStruct.getLong("minElements") == 0)
      assert(metadataPrimitive.getLong("maxElements") == 5)
      assert(metadataStruct.getLong("maxElements") == 3)

      val dfFlattened1 = SparkUtils.flattenSchema(df)
      val flatSchema1 = dfFlattened1.schema.treeString

      assertSchema(flatSchema1, expectedFlatSchema)
    }
  }

  test("Empty dataframe still has proper schema") {
    val copyBook: String =
      """       01  RECORD.
        |         05  FIELD1                  PIC X(2).
        |         05  ARRAY1        PIC X(1) OCCURS 2 TO 5 TIMES.
        |         05  STRUCT1.
        |           10  ARRAY2                OCCURS 3.
        |             15  STRUCT1.
        |               20  IntValue          PIC 9(1).
        |""".stripMargin

    val expectedFlatSchema =
      """root
        | |-- FIELD1: string (nullable = true)
        | |-- ARRAY1_0: string (nullable = true)
        | |-- ARRAY1_1: string (nullable = true)
        | |-- ARRAY1_2: string (nullable = true)
        | |-- ARRAY1_3: string (nullable = true)
        | |-- ARRAY1_4: string (nullable = true)
        | |-- STRUCT1_IntValue: integer (nullable = true)
        | |-- STRUCT1_IntValue1: integer (nullable = true)
        | |-- STRUCT1_IntValue2: integer (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")

    withTempTextFile("fletten", "test", StandardCharsets.UTF_8, "") { filePath =>
      val df = spark.read
        .format("cobol")
        .option("copybook_contents", copyBook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .load(filePath)

      val metadataPrimitive = df.schema.fields(1).metadata
      val metadataStruct = df.schema.fields(2).dataType.asInstanceOf[StructType].fields.head.metadata

      assert(metadataPrimitive.contains("minElements"))
      assert(metadataStruct.contains("minElements"))
      assert(metadataPrimitive.contains("maxElements"))
      assert(metadataStruct.contains("maxElements"))

      assert(metadataPrimitive.getLong("minElements") == 2)
      assert(metadataStruct.getLong("minElements") == 0)
      assert(metadataPrimitive.getLong("maxElements") == 5)
      assert(metadataStruct.getLong("maxElements") == 3)

      val dfFlattened1 = SparkUtils.flattenSchema(df, useShortFieldNames = true)
      val flatSchema1 = dfFlattened1.schema.treeString

      assertSchema(flatSchema1, expectedFlatSchema)
      assert(dfFlattened1.count() == 0)
    }
  }

  test("Empty non-cobrix dataframe could be flattened as well") {
    val expectedSchema =
      """root
        | |-- value_0_0: integer (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")

    val f: Seq[List[List[Int]]] = Nil

    val df = f.toDF()

    val dfFlattened = SparkUtils.flattenSchema(df)
    val flatSchema = dfFlattened.schema.treeString

    assertSchema(flatSchema, expectedSchema)

    assert(dfFlattened.count() == 0)
  }

  test("Schema with multiple OCCURS should properly determine array sizes") {
    val copyBook: String =
      """       01 RECORD.
        |          02 COUNT PIC 9(1).
        |          02 GROUP OCCURS 2 TIMES.
        |             03 INNER-COUNT PIC 9(1).
        |             03 INNER-GROUP OCCURS 3 TIMES.
        |                04 FIELD PIC X.
        |""".stripMargin

    val expectedFlatSchema =
      """root
        | |-- COUNT: integer (nullable = true)
        | |-- GROUP_0_INNER_COUNT: integer (nullable = true)
        | |-- INNER_GROUP_0_FIELD: string (nullable = true)
        | |-- INNER_GROUP_1_FIELD: string (nullable = true)
        | |-- INNER_GROUP_2_FIELD: string (nullable = true)
        | |-- GROUP_1_INNER_COUNT: integer (nullable = true)
        | |-- INNER_GROUP_0_FIELD1: string (nullable = true)
        | |-- INNER_GROUP_1_FIELD1: string (nullable = true)
        | |-- INNER_GROUP_2_FIELD1: string (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")

    withTempTextFile("flatten", "test", StandardCharsets.UTF_8, "") { filePath =>
      val df = spark.read
        .format("cobol")
        .option("copybook_contents", copyBook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .load(filePath)

      val metadataStruct1 = df.schema.fields(1).metadata
      val metadataInnerStruct = df.schema.fields(1).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields(1).metadata

      assert(metadataStruct1.contains("minElements"))
      assert(metadataInnerStruct.contains("minElements"))
      assert(metadataStruct1.contains("maxElements"))
      assert(metadataInnerStruct.contains("maxElements"))

      assert(metadataStruct1.getLong("minElements") == 0)
      assert(metadataInnerStruct.getLong("minElements") == 0)
      assert(metadataStruct1.getLong("maxElements") == 2)
      assert(metadataInnerStruct.getLong("maxElements") == 3)

      val dfFlattened1 = SparkUtils.flattenSchema(df, useShortFieldNames = true)
      val flatSchema1 = dfFlattened1.schema.treeString

      assertSchema(flatSchema1, expectedFlatSchema)
      assert(dfFlattened1.count() == 0)
    }
  }

  test("unstructDataFrame() and unstructSchema() should flatten a schema and the dataframe with short names") {
    val copyBook: String =
      """       01 RECORD.
        |          02 COUNT PIC 9(1).
        |          02 GROUP1.
        |             03 INNER-COUNT PIC S9(1).
        |             03 INNER-GROUP OCCURS 3 TIMES.
        |                04 FIELD PIC 9.
        |          02 GROUP2.
        |             03 INNER-COUNT PIC S9(1).
        |             03 INNER-NUM   PIC 9 OCCURS 3 TIMES.
        |""".stripMargin

    val expectedSchema =
      """|root
         | |-- COUNT: integer (nullable = true)
         | |-- INNER_COUNT: integer (nullable = true)
         | |-- INNER_GROUP: array (nullable = true)
         | |    |-- element: struct (containsNull = true)
         | |    |    |-- FIELD: integer (nullable = true)
         | |-- INNER_COUNT: integer (nullable = true)
         | |-- INNER_NUM: array (nullable = true)
         | |    |-- element: integer (containsNull = true)
         |""".stripMargin

    val expectedData =
      """[ {
        |  "COUNT" : 2,
        |  "INNER_COUNT" : 1,
        |  "INNER_GROUP" : [ {
        |    "FIELD" : 4
        |  }, {
        |    "FIELD" : 5
        |  }, {
        |    "FIELD" : 6
        |  } ],
        |  "INNER_NUM" : [ 7, 8, 9 ]
        |}, {
        |  "COUNT" : 3,
        |  "INNER_COUNT" : 2,
        |  "INNER_GROUP" : [ {
        |    "FIELD" : 7
        |  }, {
        |    "FIELD" : 8
        |  }, {
        |    "FIELD" : 9
        |  } ],
        |  "INNER_NUM" : [ 4, 5, 6 ]
        |} ]
        |""".stripMargin

    withTempTextFile("flatten", "test", StandardCharsets.UTF_8, "224561789\n347892456\n") { filePath =>
      val df = spark.read
        .format("cobol")
        .option("copybook_contents", copyBook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("metadata", "extended")
        .load(filePath)

      val actualDf = SparkUtils.unstructDataFrame(df, useShortFieldNames = true)
      val actualSchema = actualDf.schema.treeString
      val actualSchemaOnly = SparkUtils.unstructSchema(df.schema, useShortFieldNames = true)
      val actualSchema2 = actualSchemaOnly.treeString

      compareText(actualSchema, expectedSchema)
      compareText(actualSchema2, expectedSchema)

      val actualData = SparkUtils.prettyJSON(actualDf.orderBy("COUNT").toJSON.collect().mkString("[", ", ", "]"))

      compareText(actualData, expectedData)

      assert(actualDf.schema.fields.head.metadata.json.nonEmpty)
      assert(actualDf.schema.fields(1).metadata.json.nonEmpty)
      assert(actualDf.schema.fields(2).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields.head.metadata.json.nonEmpty)
      assert(actualDf.schema.fields(3).metadata.json.nonEmpty)
      assert(actualDf.schema.fields(4).metadata.json.nonEmpty)

      assert(actualSchemaOnly.fields.head.metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(1).metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(2).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields.head.metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(3).metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(4).metadata.json.nonEmpty)
    }
  }

  test("unstructDataFrame() and unstructSchema() should flatten a schema and the dataframe with long names") {
    val copyBook: String =
      """       01 RECORD.
        |          02 COUNT PIC 9(1).
        |          02 GROUP1.
        |             03 INNER-COUNT PIC S9(1).
        |             03 INNER-GROUP OCCURS 3 TIMES.
        |                04 FIELD PIC 9.
        |          02 GROUP2.
        |             03 INNER-COUNT PIC S9(1).
        |             03 INNER-NUM   PIC 9 OCCURS 3 TIMES.
        |""".stripMargin

    val expectedSchema =
      """|root
         | |-- COUNT: integer (nullable = true)
         | |-- GROUP1_INNER_COUNT: integer (nullable = true)
         | |-- GROUP1_INNER_GROUP: array (nullable = true)
         | |    |-- element: struct (containsNull = true)
         | |    |    |-- FIELD: integer (nullable = true)
         | |-- GROUP2_INNER_COUNT: integer (nullable = true)
         | |-- GROUP2_INNER_NUM: array (nullable = true)
         | |    |-- element: integer (containsNull = true)
         |""".stripMargin

    val expectedData =
      """[ {
        |  "COUNT" : 2,
        |  "GROUP1_INNER_COUNT" : 2,
        |  "GROUP1_INNER_GROUP" : [ {
        |    "FIELD" : 4
        |  }, {
        |    "FIELD" : 5
        |  }, {
        |    "FIELD" : 6
        |  } ],
        |  "GROUP2_INNER_COUNT" : 1,
        |  "GROUP2_INNER_NUM" : [ 7, 8, 9 ]
        |}, {
        |  "COUNT" : 3,
        |  "GROUP1_INNER_COUNT" : 4,
        |  "GROUP1_INNER_GROUP" : [ {
        |    "FIELD" : 7
        |  }, {
        |    "FIELD" : 8
        |  }, {
        |    "FIELD" : 9
        |  } ],
        |  "GROUP2_INNER_COUNT" : 2,
        |  "GROUP2_INNER_NUM" : [ 4, 5, 6 ]
        |} ]
        |""".stripMargin

    withTempTextFile("flatten", "test", StandardCharsets.UTF_8, "224561789\n347892456\n") { filePath =>
      val df = spark.read
        .format("cobol")
        .option("copybook_contents", copyBook)
        .option("pedantic", "true")
        .option("record_format", "D")
        .option("metadata", "extended")
        .load(filePath)

      val actualDf = SparkUtils.unstructDataFrame(df)
      val actualSchema = actualDf.schema.treeString
      val actualSchemaOnly = SparkUtils.unstructSchema(df.schema)
      val actualSchema2 = actualSchemaOnly.treeString

      compareText(actualSchema, expectedSchema)
      compareText(actualSchema2, expectedSchema)

      val actualData = SparkUtils.prettyJSON(actualDf.orderBy("COUNT").toJSON.collect().mkString("[", ", ", "]"))

      compareText(actualData, expectedData)

      assert(actualDf.schema.fields.head.metadata.json.nonEmpty)
      assert(actualDf.schema.fields(1).metadata.json.nonEmpty)
      assert(actualDf.schema.fields(2).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields.head.metadata.json.nonEmpty)
      assert(actualDf.schema.fields(3).metadata.json.nonEmpty)
      assert(actualDf.schema.fields(4).metadata.json.nonEmpty)

      assert(actualSchemaOnly.fields.head.metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(1).metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(2).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields.head.metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(3).metadata.json.nonEmpty)
      assert(actualSchemaOnly.fields(4).metadata.json.nonEmpty)
    }
  }

  test("Integral to decimal conversion for complex schema") {
    val expectedSchema =
      """|root
         | |-- COUNT: decimal(1,0) (nullable = true)
         | |-- GROUP: array (nullable = true)
         | |    |-- element: struct (containsNull = false)
         | |    |    |-- INNER_COUNT: decimal(1,0) (nullable = true)
         | |    |    |-- INNER_GROUP: array (nullable = true)
         | |    |    |    |-- element: struct (containsNull = false)
         | |    |    |    |    |-- FIELD: decimal(1,0) (nullable = true)
         |""".stripMargin

    val copyBook: String =
      """       01 RECORD.
        |          02 COUNT PIC 9(1).
        |          02 GROUP OCCURS 2 TIMES.
        |             03 INNER-COUNT PIC S9(1).
        |             03 INNER-GROUP OCCURS 3 TIMES.
        |                04 FIELD PIC 9.
        |""".stripMargin

    if (!Properties.versionNumberString.startsWith("2.11.")) {
      withTempTextFile("flatten", "test", StandardCharsets.UTF_8, "") { filePath =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copyBook)
          .option("pedantic", "true")
          .option("record_format", "D")
          .option("metadata", "extended")
          .load(filePath)

        // This method only works with Scala 2.12+ and Spark 3.0+
        val actualDf = SparkUtils.covertIntegralToDecimal(df)
        val actualSchema = actualDf.schema.treeString

        assert(actualDf.schema.fields.head.metadata.json.nonEmpty)
        assert(actualDf.schema.fields(1).metadata.json.nonEmpty)
        assert(actualDf.schema.fields(1).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields.head.metadata.json.nonEmpty)
        assert(actualDf.schema.fields(1).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields(1).metadata.json.nonEmpty)
        assert(actualDf.schema.fields(1).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields(1).dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields.head.metadata.json.nonEmpty)

        compareText(actualSchema, expectedSchema)
      }
    }
  }

  private def assertSchema(actualSchema: String, expectedSchema: String): Unit = {
    if (actualSchema != expectedSchema) {
      logger.error(s"EXPECTED:\n$expectedSchema")
      logger.error(s"ACTUAL:\n$actualSchema")
      fail("Actual schema does not match the expected schema (see above).")
    }
  }

  private def assertResults(actualResults: String, expectedResults: String): Unit = {
    if (actualResults.toLowerCase != expectedResults.toLowerCase) {
      logger.error(s"EXPECTED:\n$expectedResults")
      logger.error(s"ACTUAL:\n$actualResults")
      fail("Actual dataset data does not match the expected data (see above).")
    }
  }

}
