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

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.utils.TestUtils._

class SparkUtilsSuite extends FunSuite with SparkTestBase {

  import spark.implicits._

  private val logger = LoggerFactory.getLogger(this.getClass)

  val nestedSampleData: List[String] =
    """[{"id":1,"legs":[{"legid":100,"conditions":[{"checks":[{"checkNums":["1","2","3b","4","5c","6"]}],"amount":100}]}]}]""" ::
      """[{"id":2,"legs":[{"legid":200,"conditions":[{"checks":[{"checkNums":["3","4","5b","6","7c","8"]}],"amount":200}]}]}]""" ::
      """[{"id":3,"legs":[{"legid":300,"conditions":[{"checks":[{"checkNums":["6","7","8b","9","0c","1"]}],"amount":300}]}]}]""" ::
      """[{"id":4,"legs":[]}]""" ::
      """[{"id":5,"legs":null}]""" :: Nil

  test("Test schema flattening of multiple nested structure") {
    val expectedOrigSchema = """root
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
    val expectedOrigData = """{"id":1,"legs":[{"conditions":[{"amount":100,"checks":[{"checkNums":["1","2","3b","4","5c","6"]}]}],"legid":100}]}
                             |{"id":2,"legs":[{"conditions":[{"amount":200,"checks":[{"checkNums":["3","4","5b","6","7c","8"]}]}],"legid":200}]}
                             |{"id":3,"legs":[{"conditions":[{"amount":300,"checks":[{"checkNums":["6","7","8b","9","0c","1"]}]}],"legid":300}]}
                             |{"id":4,"legs":[]}
                             |{"id":5}""".stripMargin.replace("\r\n", "\n")

    val expectedFlatSchema = """root
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
    val expectedFlatData = """+---+--------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+----------------------------------------+------------+
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

  test("Test schema flattening when short names are used") {
    val expectedFlatSchema = """root
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
    val expectedFlatData = """+---+-------------------+-----------+-----------+-----------+-----------+-----------+-----------+------------+
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

    val expectedOrigSchema = """root
                               | |-- value: array (nullable = false)
                               | |    |-- element: array (containsNull = true)
                               | |    |    |-- element: integer (containsNull = false)
                               |""".stripMargin.replace("\r\n", "\n")
    val expectedOrigData = """{"value":[[1,2,3,4,5,6],[7,8,9,10,11,12,13]]}
                             |{"value":[[201,202,203,204,205,206],[207,208,209,210,211,212,213]]}
                             |{"value":[[201,202,203,204,205,206],[207,208,209,210,211,212,213]]}
                             |{"value":[[201,202,203,204,205,206],[207,208,209,210,211,212,213]]}""".stripMargin.replace("\r\n", "\n")

    val expectedFlatSchema = """root
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
    val expectedFlatData = """+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+---------+
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

  private def assertSchema(actualSchema: String, expectedSchema: String): Unit = {
    if (actualSchema != expectedSchema) {
      logger.error(s"EXPECTED:\n$expectedSchema")
      logger.error(s"ACTUAL:\n$actualSchema")
      fail("Actual schema does not match the expected schema (see above).")
    }
  }

  private def assertResults(actualResults: String, expectedResults: String): Unit = {
    if (actualResults != expectedResults) {
      logger.error(s"EXPECTED:\n$expectedResults")
      logger.error(s"ACTUAL:\n$actualResults")
      fail("Actual dataset data does not match the expected data (see above).")
    }
  }

}
