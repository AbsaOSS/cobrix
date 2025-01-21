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

class Test38StrictBinaryTypeSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {
  private val copybook =
    """      01  R.
                05 NUM1        PIC 9(9) COMP.
                05 NUM2        PIC 9(9) COMP-9.
                05 NUM3        PIC S9(9) COMP-5.
                05 NUM4        PIC S9(9) COMP-9.
      """

  val dataSimple: Array[Byte] = Array(
    0xFF, 0xFF, 0xFF, 0xFE, //
    0xFF, 0xFF, 0xFF, 0xFE, //
    0xFF, 0xFF, 0xFF, 0xFE, // -2
    0xFF, 0xFF, 0xFF, 0xFE, // -16777217
    0xF0, 0x00, 0x01, 0xF0, //
    0xF0, 0x00, 0x01, 0xF0, //
    0x84, 0xF0, 0x21, 0x67, // -2064637593
    0x67, 0x21, 0xF0, 0x84  // -2064637593
  ).map(_.toByte)

  "binary fields handling" should {
    "be decoded with the default behavior" in {
      withTempBinFile("strict_bin_fields", ".tmp", dataSimple) { tempFile =>
        val expectedSchema =
          """root
            | |-- NUM1: integer (nullable = true)
            | |-- NUM2: integer (nullable = true)
            | |-- NUM3: integer (nullable = true)
            | |-- NUM4: integer (nullable = true)
            |""".stripMargin

        val expectedData = """{"NUM3":-2,"NUM4":-16777217},{"NUM3":-2064637593,"NUM4":-2064637593}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("pedantic", "true")
          .load(tempFile)

        val actualSchema = df.schema.treeString
        val actualData = df.toJSON.collect().mkString(",")

        compareText(actualSchema, expectedSchema)
        assert(actualData == expectedData)
      }
    }
  }
}
