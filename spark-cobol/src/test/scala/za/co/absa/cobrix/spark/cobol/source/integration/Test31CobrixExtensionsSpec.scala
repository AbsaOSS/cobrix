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
import org.apache.spark.sql.types.{BinaryType, DecimalType, IntegerType}
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test31CobrixExtensionsSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  "COMP-3U" should {
    "be a interpreted as unsigned packed with PIC X" when {
      val copybook =
        """      01  R.
                    03 A        PIC X(4) COMP-3U.
        """

      val data = Array(0x12.toByte, 0x34.toByte, 0x56.toByte, 0x78.toByte)

      withTempBinFile("comp3u", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        val actual = df.select("A").collect().map(_.getInt(0)).mkString

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == IntegerType)
        }

        "data as expected" in {
          val expected = "12345678"

          assert(actual == expected)
        }
      }
    }

    "be a interpreted as unsigned packed with PIC 9" when {
      val copybook =
        """      01  R.
              03 A        PIC 9(4) COMPUTATIONAL-3U.
        """

      val data = Array(0x12.toByte, 0x34.toByte)

      withTempBinFile("comp3u", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        val actual = df.select("A").collect().map(_.getInt(0)).mkString

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == IntegerType)
        }

        "data as expected" in {
          val expected = "1234"

          assert(actual == expected)
        }
      }
    }

    "be able to parse decimals" when {
      val copybook =
        """      01  R.
        03 A        PIC 9(4)V99 USAGE IS COMP-3U.
        """

      val data = Array(0x12.toByte, 0x34.toByte, 0x56.toByte)

      withTempBinFile("comp3u", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        val actual = df.select("A").collect().map(_.getDecimal(0)).mkString

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == DecimalType(6, 2))
        }

        "data as expected" in {
          val expected = "1234.56"

          assert(actual == expected)
        }
      }
    }

    "be able to parse odd number of digits" when {
      val copybook =
        """      01  R.
        03 A        PIC 9(5) COMP-3U.
        """

      val data = Array(0x12.toByte, 0x34.toByte, 0x56.toByte)

      withTempBinFile("comp3u", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        val actual = df.select("A").collect().map(_.getInt(0)).mkString

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == IntegerType)
        }

        "data as expected" in {
          val expected = "123456"

          assert(actual == expected)
        }
      }
    }

    "be able to parse decimals with odd number of digits" when {
      val copybook =
        """      01  R.
        03 A        PIC 9(4)V9 COMP-3U.
        """

      val data = Array(0x01.toByte, 0x23.toByte, 0x45.toByte)

      withTempBinFile("comp3u", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        val actual = df.select("A").collect().map(_.getDecimal(0)).mkString

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == DecimalType(5, 1))
        }

        "data as expected" in {
          val expected = "1234.5"

          assert(actual == expected)
        }
      }
    }

    "fail to parse data with a sign nibble" when {
      val copybook =
        """      01  R.
              03 A        PIC X(2) COMP-3U.
        """

      val data = Array(0x12.toByte, 0x3C.toByte)

      withTempBinFile("comp3u", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        val actual = df.select("A").collect()(0)(0)

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == IntegerType)
        }

        "data as expected" in {
          assert(actual == null)
        }
      }
    }
  }

  "COMP with a alpha-numeric field" should {
    "be a interpreted as bytes with PIC X" when {
      val copybook =
        """      01  R.
                    03 A        PIC X(4) COMP.
        """

      val data = Array(0x12.toByte, 0x34.toByte, 0x56.toByte, 0x78.toByte)

      withTempBinFile("compstring", ".bin", data) { fileName =>
        val df = getDf(copybook, fileName)

        "schema as expected" in {
          val field = df.schema.fields.head

          assert(field.name == "A")
          assert(field.dataType == BinaryType)
        }

        val actual = df.select("A").collect().map(_.get(0).asInstanceOf[Array[Byte]]).head

        "data as expected" in {
          assert(actual sameElements data)
        }
      }
    }
  }

    def getDf(copybook: String, fileName: String): DataFrame = {
    spark.read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("record_format", "F")
      .load(fileName)
  }

}
