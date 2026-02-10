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

package za.co.absa.cobrix.spark.cobol.source.integration

import org.apache.spark.sql.DataFrame
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser.PARAM_CORRUPTED_FIELDS
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test39CorruptedFieldsSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {
  private val copybook =
    """      01  R.
                03 ID      PIC 9(1).
                03 F1      PIC X(1).
                03 F2      PIC S9(1)  COMP-3.
                03 F3      PIC 9(3)   COMP.
                03 F4      PIC 9(1)   OCCURS 3.
      """

  private val data = Array(
    0xF1, 0x40, 0x5C, 0x00, 0x06, 0xF1, 0xF2, 0xF3, // Record OK, no errors
    0xF2, 0xF1, 0xD3, 0x00, 0x05, 0xF4, 0xF5, 0xF6, // COMP-3 error
    0xF3, 0x00, 0x3C, 0xF1, 0x06, 0xF7, 0xF8, 0xF9, // COMP invalid value (negative)
    0xF4, 0xC1, 0x4C, 0x00, 0xA0, 0xC1, 0xF5, 0xA3  // Errors in array
  ).map(_.toByte)

  "Corrupted fields record generation" should {
    "work when the option is turned on" in {
      val expectedSchema =
        """root
          | |-- ID: integer (nullable = true)
          | |-- F1: string (nullable = true)
          | |-- F2: integer (nullable = true)
          | |-- F3: integer (nullable = true)
          | |-- F4: array (nullable = true)
          | |    |-- element: integer (containsNull = true)
          | |-- _corrupted_fields: array (nullable = false)
          | |    |-- element: struct (containsNull = false)
          | |    |    |-- field_name: string (nullable = false)
          | |    |    |-- raw_value: binary (nullable = false)
          |""".stripMargin

      val expectedData =
        """[ {
          |  "ID" : 1,
          |  "F1" : "",
          |  "F2" : 5,
          |  "F3" : 6,
          |  "F4" : [ 1, 2, 3 ],
          |  "_corrupted_fields" : [ ]
          |}, {
          |  "ID" : 2,
          |  "F1" : "1",
          |  "F3" : 5,
          |  "F4" : [ 4, 5, 6 ],
          |  "_corrupted_fields" : [ {
          |    "field_name" : "F2",
          |    "raw_value" : "0w=="
          |  } ]
          |}, {
          |  "ID" : 3,
          |  "F2" : 3,
          |  "F3" : 61702,
          |  "F4" : [ 7, 8, 9 ],
          |  "_corrupted_fields" : [ ]
          |}, {
          |  "ID" : 4,
          |  "F1" : "A",
          |  "F2" : 4,
          |  "F3" : 160,
          |  "F4" : [ null, 5, null ],
          |  "_corrupted_fields" : [ {
          |    "field_name" : "F4[0]",
          |    "raw_value" : "wQ=="
          |  }, {
          |    "field_name" : "F4[2]",
          |    "raw_value" : "ow=="
          |  } ]
          |} ]
          |""".stripMargin

      withTempBinFile("corrupted_fields1", ".dat", data) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map("generate_corrupted_fields" -> "true"))

        val actualSchema = df.schema.treeString
        compareTextVertical(actualSchema, expectedSchema)

        val actualData = SparkUtils.convertDataFrameToPrettyJSON(df.orderBy("ID"), 10)

        compareTextVertical(actualData, expectedData)
      }
    }

    "throw an exception when working with a hierarchical data" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/tmp/dummy", Map("generate_corrupted_fields" -> "true", "segment-children:0" -> "COMPANY => DEPT,CUSTOMER"))
      }

      assert(ex.getMessage.contains(s"Option '$PARAM_CORRUPTED_FIELDS' cannot be used with 'segment-children:*'"))
    }
  }

  private def getDataFrame(inputPath: String, extraOptions: Map[String, String] = Map.empty[String, String]): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .options(extraOptions)
      .load(inputPath)
  }
}
