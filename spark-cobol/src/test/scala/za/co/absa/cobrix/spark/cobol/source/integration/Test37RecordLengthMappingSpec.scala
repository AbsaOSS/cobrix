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

import org.apache.spark.SparkException
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test37RecordLengthMappingSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  private val copybook =
    """      01  R.
                03 SEG-ID        PIC X(1).
                03 TEXT          PIC X(7).
      """

  val dataSimple: Array[Byte] = Array(
    0xC1, 0xF1, 0xF2, 0xF3,                                       // record 0 'A123'
    0xC2, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6,                     // record 1 'B123456'
    0xC3, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7).map(_.toByte) // record 2 'C1234567890'

  val dataNumeric: Array[Byte] = Array(
    0xF1, 0xF1, 0xF2, 0xF3,                                       // record 0 '1123'
    0xF2, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6,                     // record 1 '1123456'
    0xF3, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7).map(_.toByte) // record 2 '11234567890'

  val dataWithFileOffsets: Array[Byte] = Array(
    0x00,                                                         // header
    0xC1, 0xF1, 0xF2, 0xF3,                                       // record 0 'A123'
    0xC2, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6,                     // record 1 'B123456'
    0xC3, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7,               // record 2 'C1234567890'
    0x00, 0x00).map(_.toByte)                                     // footer

  "data with record length mapping" should {
    "work for simple mappings" in {
      withTempBinFile("record_length_mapping", ".tmp", dataSimple) { tempFile =>
        val expected = """{"SEG_ID":"A","TEXT":"123"},{"SEG_ID":"B","TEXT":"123456"},{"SEG_ID":"C","TEXT":"1234567"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("input_split_records", "2")
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":7,"C":8}""")
          .load(tempFile)

        val actual = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "work for numeric mappings" in {
      withTempBinFile("record_length_mapping", ".tmp", dataNumeric) { tempFile =>
        val expected = """{"SEG_ID":"1","TEXT":"123"},{"SEG_ID":"2","TEXT":"123456"},{"SEG_ID":"3","TEXT":"1234567"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("input_split_records", "2")
          .option("pedantic", "true")
          .option("record_length_map", """{"1":4,"2":7,"3":8}""")
          .load(tempFile)

        val actual = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "work for data with offsets" in {
      withTempBinFile("record_length_mapping", ".tmp", dataWithFileOffsets) { tempFile =>
        val expected = """{"SEG_ID":"A","TEXT":"123"},{"SEG_ID":"B","TEXT":"123456"},{"SEG_ID":"C","TEXT":"1234567"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("file_start_offset", 1)
          .option("file_end_offset", 2)
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":7,"C":8}""")
          .load(tempFile)

        val actual = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "work for data with offsets and indexes" in {
      withTempBinFile("record_length_mapping", ".tmp", dataWithFileOffsets) { tempFile =>
        val expected = """{"SEG_ID":"A","TEXT":"123"},{"SEG_ID":"B","TEXT":"123456"},{"SEG_ID":"C","TEXT":"1234567"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("file_start_offset", 1)
          .option("file_end_offset", 2)
          .option("input_split_records", "2")
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":7,"C":8}""")
          .load(tempFile)

        val actual = df.orderBy("SEG_ID").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "throw an exception for unknown mapping" in {
      withTempBinFile("record_length_mapping", ".tmp", dataSimple) { tempFile =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("record_length_map", """{"A":4,"B":7}""")
          .option("pedantic", "true")
          .load(tempFile)

        val ex = intercept[SparkException] {
          df.count()
        }

        assert(ex.getMessage.contains("Record length value 'C' is not mapped to a record length"))
      }
    }

    "throw an exception for null fields" in {
      withTempBinFile("rdw_test", ".tmp", dataWithFileOffsets) { tempFile =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "SEG-ID")
          .option("pedantic", "true")
          .option("record_length_map", """{"A":4,"B":7,"C":8}""")
          .load(tempFile)

        val ex = intercept[SparkException] {
          df.count()
        }

        assert(ex.getMessage.contains("Null encountered as a record length field (offset: 1, raw value: 00)"))
      }
    }
  }
}
