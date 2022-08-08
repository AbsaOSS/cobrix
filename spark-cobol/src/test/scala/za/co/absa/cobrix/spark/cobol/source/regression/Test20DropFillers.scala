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

import java.nio.charset.StandardCharsets

class Test20DropFillers extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01  R.
           05  F.
              07 FILLER OCCURS 3 TIMES.
                 10 A1   PIC X.
                 10 A2   PIC X.
           05  B         PIC X(1).
    """

  "ASCII files" should {
    val data =
      """1234567
        |8901234
        |""".stripMargin

    "correctly retain all fillers" in {
      withTempTextFile("drop_fillers", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("drop_group_fillers", "false")
          .option("drop_value_fillers", "false")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expectedSchema = """root
                               | |-- F: struct (nullable = true)
                               | |    |-- FILLER_1: array (nullable = true)
                               | |    |    |-- element: struct (containsNull = true)
                               | |    |    |    |-- A1: string (nullable = true)
                               | |    |    |    |-- A2: string (nullable = true)
                               | |-- B: string (nullable = true)
                               |""".stripMargin

        val expectedData = """[{"F":{"FILLER_1":[{"A1":"1","A2":"2"},{"A1":"3","A2":"4"},{"A1":"5","A2":"6"}]},"B":"7"},{"F":{"FILLER_1":[{"A1":"8","A2":"9"},{"A1":"0","A2":"1"},{"A1":"2","A2":"3"}]},"B":"4"}]"""

        val actualSchema = df.schema.treeString

        assertEqualsMultiline(actualSchema, expectedSchema)

        val actualData = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actualData, expectedData)
      }
    }

    "correctly drop all fillers" in {
      withTempTextFile("drop_fillers", ".dat", StandardCharsets.US_ASCII, data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("drop_group_fillers", "true")
          .option("drop_value_fillers", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val expectedSchema = """root
                               | |-- B: string (nullable = true)
                               |""".stripMargin

        val expectedData = """[{"B":"7"},{"B":"4"}]"""

        val actualSchema = df.schema.treeString

        assertEqualsMultiline(actualSchema, expectedSchema)

        val actualData = df.toJSON.collect().mkString("[", ",", "]")

        assertEqualsMultiline(actualData, expectedData)
      }
    }
  }
}
