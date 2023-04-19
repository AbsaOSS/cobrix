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
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test36RdwBdwSimpleSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  private val copybook =
    """      01  R.
                03 A        PIC X(3).
      """

  val dataRdw: Array[Byte] = Array(
    0x00, 0x02, 0x00, 0x00, 0xF1, 0xF2,                            // record 0 '12'
    0x00, 0x03, 0x00, 0x00, 0xF3, 0xF4, 0xF5,                      // record 1 '345'
    0x00, 0x04, 0x00, 0x00, 0xF6, 0xF7, 0xF8, 0xF9).map(_.toByte)  // record 1 '6789'

  val dataBdwRdw: Array[Byte] = Array(
    0x00, 0x00, 0x0D, 0x00,                                       // BDW = 13
    0x00, 0x02, 0x00, 0x00, 0xF1, 0xF2,                           // record 0 '12'
    0x00, 0x03, 0x00, 0x00, 0xF3, 0xF4, 0xF5,                     // record 1 '345'
    0x00, 0x00, 0x08, 0x00,                                         // BDW = 8
    0x00, 0x04, 0x00, 0x00, 0xF6, 0xF7, 0xF8, 0xF9).map(_.toByte) // record 1 '6789'

  "minimum_record_length" should {
    "work for files with RDW" in {
      withTempBinFile("rdw_test", ".tmp", dataRdw) { tempFile =>
        val expected = """{"A":"345"},{"A":"678"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("minimum_record_length", "3")
          .load(tempFile)

        val actual = df.orderBy("A").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "work for files with BDW + RDW" in {
      withTempBinFile("rdw_test", ".tmp", dataBdwRdw) { tempFile =>
        val expected = """{"A":"345"},{"A":"678"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "VB")
          .option("is_rdw_big_endian", "true")
          .option("minimum_record_length", "3")
          .load(tempFile)

        val actual = df.orderBy("A").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }
  }

  "maximum_record_length" should {
    "work for files with RDW" in {
      withTempBinFile("rdw_test", ".tmp", dataRdw) { tempFile =>
        val expected = """{"A":"12"},{"A":"345"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("maximum_record_length", "3")
          .load(tempFile)

        val actual = df.orderBy("A").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "work for files with BDW + RDW" in {
      withTempBinFile("rdw_test", ".tmp", dataBdwRdw) { tempFile =>
        val expected = """{"A":"12"},{"A":"345"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "VB")
          .option("is_rdw_big_endian", "true")
          .option("maximum_record_length", "3")
          .load(tempFile)

        val actual = df.orderBy("A").toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }
  }

  "minimum_record_length + maximum_record_length" should {
    "work for files with RDW" in {
      withTempBinFile("rdw_test", ".tmp", dataRdw) { tempFile =>
        val expected = """{"A":"345"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("minimum_record_length", "3")
          .option("maximum_record_length", "3")
          .load(tempFile)

        val actual = df.toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }

    "work for files with BDW + RDW" in {
      withTempBinFile("rdw_test", ".tmp", dataBdwRdw) { tempFile =>
        val expected = """{"A":"345"}"""

        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "VB")
          .option("is_rdw_big_endian", "true")
          .option("minimum_record_length", "3")
          .option("maximum_record_length", "3")
          .load(tempFile)

        val actual = df.toJSON.collect().mkString(",")

        assert(actual == expected)
      }
    }
  }
}
