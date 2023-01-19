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

class Test32RecordLengthExprssionSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  "record_length_fiel" should {
    "recognize field names" in {
      val copybook =
        """      01  R.
                    03 LEN      PIC 9(1).
                    03 A        PIC X(10).
        """

      val data = Array(0xF2.toByte, 0xF0.toByte,
                       0xF3.toByte, 0xF9.toByte, 0xF8.toByte,
                       0xF4.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte)

      withTempBinFile("record_length", ".bin", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "LEN")
          .load(fileName)

        val expected = "0,98,123"
        val actual = df.select("A").collect().map(_.getString(0)).mkString(",")

        assert(actual == expected)
      }
    }

    "recognize expressions names (format=F)" in {
      val copybook =
        """      01  R.
                03 A-1      PIC 9(1).
                03 A-2      PIC 9(1).
                03 X       PIC X(10).
    """

      val data = Array(0xF1.toByte, 0xF4.toByte, 0xF0.toByte, 0xF1.toByte,
                       0xF2.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
                       0xF2.toByte, 0xF2.toByte, 0xF9.toByte)

      withTempBinFile("record_length", ".bin", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("record_length_field", "A_1 + A_2 - 1")
          .load(fileName)

        val expected = "01,1234,9"
        val actual = df.select("X").collect().map(_.getString(0)).mkString(",")

        assert(actual == expected)
      }
    }

    "recognize expressions names (format=V)" in {
      val copybook =
        """      01  R.
            03 A-1      PIC 9(1).
            03 A-2      PIC 9(1).
            03 X       PIC X(10).
"""

      val data = Array(0xF1.toByte, 0xF4.toByte, 0xF0.toByte, 0xF1.toByte,
                       0xF2.toByte, 0xF5.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
                       0xF2.toByte, 0xF2.toByte, 0xF9.toByte)

      withTempBinFile("record_length", ".bin", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("record_length_field", "A_1 + A_2 - 1")
          .load(fileName)

        val expected = "01,1234,9"
        val actual = df.select("X").collect().map(_.getString(0)).mkString(",")

        assert(actual == expected)
      }
    }
  }
}
