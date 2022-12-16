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

package za.co.absa.cobrix.spark.cobol.source.regression

import org.scalatest.WordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import java.nio.charset.StandardCharsets

class Test21VariableOccursForTextFiles extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01 RECORD.
        02 COUNT PIC 9(1).
        02 GROUP OCCURS 0 TO 2 TIMES DEPENDING ON COUNT.
           03 INNER-COUNT PIC 9(1).
           03 INNER-GROUP OCCURS 0 TO 3 TIMES
                              DEPENDING ON INNER-COUNT.
              04 FIELD PIC X.
    """

  "ASCII files" should {

    val data =
      """0
        |01
        |10
        |11A
        |12AB
        |13ABC
        |21AB
        |22ABC
        |23ABCD
        |""".stripMargin

    val expectedSchema =
      """root
        | |-- COUNT: integer (nullable = true)
        | |-- GROUP: array (nullable = true)
        | |    |-- element: struct (containsNull = true)
        | |    |    |-- INNER_COUNT: integer (nullable = true)
        | |    |    |-- INNER_GROUP: array (nullable = true)
        | |    |    |    |-- element: struct (containsNull = true)
        | |    |    |    |    |-- FIELD: string (nullable = true)
        |""".stripMargin

    val expectedData =
      """[ {
        |  "COUNT" : 0,
        |  "GROUP" : [ ]
        |}, {
        |  "COUNT" : 0,
        |  "GROUP" : [ ]
        |}, {
        |  "COUNT" : 1,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 0,
        |    "INNER_GROUP" : [ ]
        |  } ]
        |}, {
        |  "COUNT" : 1,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 1,
        |    "INNER_GROUP" : [ {
        |      "FIELD" : "A"
        |    } ]
        |  } ]
        |}, {
        |  "COUNT" : 1,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 2,
        |    "INNER_GROUP" : [ {
        |      "FIELD" : "A"
        |    }, {
        |      "FIELD" : "B"
        |    } ]
        |  } ]
        |}, {
        |  "COUNT" : 1,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 3,
        |    "INNER_GROUP" : [ {
        |      "FIELD" : "A"
        |    }, {
        |      "FIELD" : "B"
        |    }, {
        |      "FIELD" : "C"
        |    } ]
        |  } ]
        |}, {
        |  "COUNT" : 2,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 1,
        |    "INNER_GROUP" : [ {
        |      "FIELD" : "A"
        |    } ]
        |  }, {
        |    "INNER_GROUP" : [ { } ]
        |  } ]
        |}, {
        |  "COUNT" : 2,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 2,
        |    "INNER_GROUP" : [ {
        |      "FIELD" : "A"
        |    }, {
        |      "FIELD" : "B"
        |    } ]
        |  }, {
        |    "INNER_GROUP" : [ { }, { } ]
        |  } ]
        |}, {
        |  "COUNT" : 2,
        |  "GROUP" : [ {
        |    "INNER_COUNT" : 3,
        |    "INNER_GROUP" : [ {
        |      "FIELD" : "A"
        |    }, {
        |      "FIELD" : "B"
        |    }, {
        |      "FIELD" : "C"
        |    } ]
        |  }, {
        |    "INNER_GROUP" : [ { }, { }, { } ]
        |  } ]
        |} ]
        |""".stripMargin.replace("\r\n", "\n")

    "correctly keep occurs for basic ASCII" in {
      withTempTextFile("variable_occurs_ascii", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D2")
          .option("variable_size_occurs", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString

        assertEqualsMultiline(actualSchema, expectedSchema)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actualData, expectedData)
      }
    }

    "correctly keep occurs for Cobrix ASCII" in {
      withTempTextFile("variable_occurs_ascii", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("variable_size_occurs", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString

        assertEqualsMultiline(actualSchema, expectedSchema)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actualData, expectedData)
      }
    }

    "correctly keep occurs for Cobrix ASCII with variable length extractor" in {
      withTempTextFile("variable_occurs_ascii", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("ascii_charset", "utf8")
          .option("variable_size_occurs", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString

        assertEqualsMultiline(actualSchema, expectedSchema)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actualData, expectedData)
      }
    }
  }
}
