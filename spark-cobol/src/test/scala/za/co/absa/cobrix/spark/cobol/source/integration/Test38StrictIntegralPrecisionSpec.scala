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

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}

class Test38StrictIntegralPrecisionSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {
  private val copybook =
    """      01  R.
                03 SEG-ID        PIC X(1).
                03 SEG1.
                  05 NUM1        PIC 9(2).
                03 SEG2 REDEFINES SEG1.
                  05 NUM2        PIC S9(9).
                03 SEG3 REDEFINES SEG1.
                  05 NUM3        PIC S9(15).
      """

  val dataSimple: Array[Byte] = Array(
    0xC1, 0xF1, 0xF2, 0xF3,                                       // record 0 'A12'
    0xC2, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9,   // record 1 'B123456789'
    0xC3, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9,
    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5).map(_.toByte)             // record 2 'C123456789012345'

  "non-strict decimals" should {
    "be decoded normally" in {
      withTempBinFile("strict_integral_precision", ".tmp", dataSimple) { tempFile =>
        val expectedSchema =
          """root
            | |-- SEG_ID: string (nullable = true)
            | |-- SEG1: struct (nullable = true)
            | |    |-- NUM1: integer (nullable = true)
            | |-- SEG2: struct (nullable = true)
            | |    |-- NUM2: integer (nullable = true)
            | |-- SEG3: struct (nullable = true)
            | |    |-- NUM3: long (nullable = true)""".stripMargin

        val expectedData = """{"SEG_ID":"A","SEG1":{"NUM1":12}},{"SEG_ID":"B","SEG2":{"NUM2":123456789}},{"SEG_ID":"C","SEG3":{"NUM3":123456789012345}}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("segment_field", "SEG-ID")
          .option("input_split_records", "2")
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":10,"C":16}""")
          .option("redefine_segment_id_map:0", "SEG1 => A")
          .option("redefine_segment_id_map:1", "SEG2 => B")
          .option("redefine_segment_id_map:2", "SEG3 => C")
        .load(tempFile)

        val actualSchema = df.schema.treeString
        val actualData = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        compareText(actualSchema, expectedSchema)
        assert(actualData == expectedData)
      }
    }
  }

  "strict decimals" should {
    "be decoded as decimals for normal files" in {
      withTempBinFile("strict_integral_precision", ".tmp", dataSimple) { tempFile =>
        val expectedSchema =
          """root
            | |-- SEG_ID: string (nullable = true)
            | |-- SEG1: struct (nullable = true)
            | |    |-- NUM1: decimal(2,0) (nullable = true)
            | |-- SEG2: struct (nullable = true)
            | |    |-- NUM2: decimal(9,0) (nullable = true)
            | |-- SEG3: struct (nullable = true)
            | |    |-- NUM3: decimal(15,0) (nullable = true)""".stripMargin

        val expectedData = """{"SEG_ID":"A","SEG1":{"NUM1":12}},{"SEG_ID":"B","SEG2":{"NUM2":123456789}},{"SEG_ID":"C","SEG3":{"NUM3":123456789012345}}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("segment_field", "SEG-ID")
          .option("input_split_records", "2")
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":10,"C":16}""")
          .option("redefine_segment_id_map:0", "SEG1 => A")
          .option("redefine_segment_id_map:1", "SEG2 => B")
          .option("redefine_segment_id_map:2", "SEG3 => C")
          .option("strict_integral_precision", "true")
          .load(tempFile)

        val actualSchema = df.schema.treeString
        val actualData = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        compareText(actualSchema, expectedSchema)
        assert(actualData == expectedData)
      }
    }

    "be decoded as decimals for hierarchical files" in {
      withTempBinFile("strict_integral_precision", ".tmp", dataSimple) { tempFile =>
        val expectedSchema =
          """root
            | |-- SEG_ID: string (nullable = true)
            | |-- SEG1: struct (nullable = true)
            | |    |-- NUM1: decimal(2,0) (nullable = true)
            | |    |-- SEG2: array (nullable = true)
            | |    |    |-- element: struct (containsNull = true)
            | |    |    |    |-- NUM2: decimal(9,0) (nullable = true)
            | |    |-- SEG3: array (nullable = true)
            | |    |    |-- element: struct (containsNull = true)
            | |    |    |    |-- NUM3: decimal(15,0) (nullable = true)""".stripMargin

        val expectedData = """{"SEG_ID":"A","SEG1":{"NUM1":12,"SEG2":[{"NUM2":123456789}],"SEG3":[{"NUM3":123456789012345}]}}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("segment_field", "SEG-ID")
          .option("input_split_records", "2")
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":10,"C":16}""")
          .option("redefine_segment_id_map:0", "SEG1 => A")
          .option("redefine_segment_id_map:1", "SEG2 => B")
          .option("redefine_segment_id_map:2", "SEG3 => C")
          .option("segment-children:1", "SEG1 => SEG2,SEG3")

          .option("strict_integral_precision", "true")
          .load(tempFile)

        val actualSchema = df.schema.treeString
        val actualData = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        compareText(actualSchema, expectedSchema)
        assert(actualData == expectedData)
      }
    }
  }
}
