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
  "binary fields handling" should {
    "support full integer and ling decoding" in {
      val copybook =
        """      01  R.
                05 NUM1        PIC 9(9) COMP.
                05 NUM2        PIC 9(9) COMP-9.
                05 NUM3        PIC S9(9) COMP-5.
                05 NUM4        PIC S9(9) COMP-9.
                05 STRUCT.
                  10 NUM5        PIC 9(9) COMP.
                  10 NUM6        PIC 9(9) COMP-9.
                  10 NUM7        PIC S9(9) COMP-5.
                  10 NUM8        PIC S9(9) COMP-9.
      """

      val integerDataSample: Array[Byte] = Array(
        0xFF, 0xFF, 0xFF, 0xFE, // 4294967294
        0xFF, 0xFF, 0xFF, 0xFE, // 4278190079
        0xFF, 0xFF, 0xFF, 0xFE, // -2
        0xFF, 0xFF, 0xFF, 0xFE, // -16777217
        0xF0, 0x00, 0x01, 0xF0, // 4026532336
        0xF0, 0x00, 0x01, 0xF0, // 4026597616
        0x84, 0xF0, 0x21, 0x67, // -2064637593
        0x67, 0x21, 0xF0, 0x84  // -2064637593
      ).map(_.toByte)

      withTempBinFile("strict_bin_fields", ".tmp", integerDataSample) { tempFile =>
        val expectedSchema =
          """root
            | |-- NUM1: long (nullable = true)
            | |-- NUM2: long (nullable = true)
            | |-- NUM3: integer (nullable = true)
            | |-- NUM4: integer (nullable = true)
            | |-- STRUCT: struct (nullable = true)
            | |    |-- NUM5: long (nullable = true)
            | |    |-- NUM6: long (nullable = true)
            | |    |-- NUM7: integer (nullable = true)
            | |    |-- NUM8: integer (nullable = true)
            |""".stripMargin

        val expectedData = """{"NUM1":4294967294,"NUM2":4278190079,"NUM3":-2,"NUM4":-16777217,"STRUCT":{"NUM5":4026532336,"NUM6":4026597616,"NUM7":-2064637593,"NUM8":-2064637593}}"""

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

    "support full long and int decimal decoding" in {
      val copybook =
        """      01  R.
                05 NUM1        PIC 9(18) COMP.
                05 NUM2        PIC 9(18) COMP-9.
                05 NUM3        PIC S9(18) COMP-5.
                05 NUM4        PIC S9(18) COMP-9.
                05 STRUCT.
                  10 NUM5        PIC 9(18) COMP.
                  10 NUM6        PIC 9(18) COMP-9.
                  10 NUM7        PIC S9(18) COMP-5.
                  10 NUM8        PIC S9(18) COMP-9.
      """

      val integerDataSample: Array[Byte] = Array(
        0xFF, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFE, // 18446462598732906494
        0xFF, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFE, // 18374405004694978559
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, // -2
        0xFF, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFE, // -72339069014573057
        0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0xF0, // 17293822569102705136
        0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0xF0, // 17294104044079415536
        0x84, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x21, 0x67, // -8867587666292498073
        0x67, 0x21, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x84  // -8867587666292498073
      ).map(_.toByte)

      withTempBinFile("strict_bin_fields", ".tmp", integerDataSample) { tempFile =>
        val expectedSchema =
          """root
            | |-- NUM1: decimal(20,0) (nullable = true)
            | |-- NUM2: decimal(20,0) (nullable = true)
            | |-- NUM3: long (nullable = true)
            | |-- NUM4: long (nullable = true)
            | |-- STRUCT: struct (nullable = true)
            | |    |-- NUM5: decimal(20,0) (nullable = true)
            | |    |-- NUM6: decimal(20,0) (nullable = true)
            | |    |-- NUM7: long (nullable = true)
            | |    |-- NUM8: long (nullable = true)""".stripMargin

        val expectedData = """{"NUM1":18446462598732906494,"NUM2":18374405004694978559,"NUM3":-2,"NUM4":-72339069014573057,"STRUCT":{"NUM5":17293822569102705136,"NUM6":17294104044079415536,"NUM7":-8867587666292498073,"NUM8":-8867587666292498073}}"""

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
