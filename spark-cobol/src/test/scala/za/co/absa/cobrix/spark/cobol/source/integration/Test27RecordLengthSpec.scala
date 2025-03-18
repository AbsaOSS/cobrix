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
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test27RecordLengthSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test27 (record length option)"

  private val copybook =
    """      01  R.
                03 A        PIC X(2).
                03 B        PIC X(1).
      """
  private val data = "AABBBCCDDDEEFFFZYY"

  "Fixed-length reader" should {
    "be used if the record length is forced to be smaller than the copybook size" in {
      val expected = """[{"A":"AA","B":""},{"A":"BB","B":""},{"A":"BC","B":""},{"A":"CD","B":""},{"A":"DD","B":""},{"A":"EE","B":""},{"A":"FF","B":""},{"A":"FZ","B":""},{"A":"YY","B":""}]"""

      withTempBinFile("rec_len1", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map("record_length" -> "2"))

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(df.count() == 9)
        assert(actual == expected)
      }
    }

    "be used if the record length is forced to be the same as the copybook size" in {
      val expected = """[{"A":"AA","B":"B"},{"A":"BB","B":"C"},{"A":"CD","B":"D"},{"A":"DE","B":"E"},{"A":"FF","B":"F"},{"A":"ZY","B":"Y"}]"""

      withTempBinFile("rec_len2", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map("record_length" -> "3"))

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(df.count() == 6)
        assert(actual == expected)
      }
    }

    "be used if the record length is forced to be bigger than the copybook size" in {
      val expected = """[{"A":"AA","B":"B"},{"A":"CD","B":"D"},{"A":"FF","B":"F"}]"""

      withTempBinFile("rec_len3", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map("record_length" -> "6"))

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(df.count() == 3)
        assert(actual == expected)
      }
    }

    "throw an exception when record length does not divide record size" in {
      withTempBinFile("rec_len4", ".dat", data.getBytes) { tmpFileName =>
        val ex = intercept[IllegalArgumentException] {
          getDataFrame(tmpFileName, Map("record_length" -> "7")).collect()
        }
        assert(ex.getMessage.contains("NOT DIVISIBLE by the RECORD SIZE"))
      }
    }
  }

  "Record length option is not compatible with" when {
    "is_record_sequence" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_record_sequence" -> "true"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'is_record_sequence' cannot be used together."))
    }
    "is_xcom" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_xcom" -> "true"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'is_xcom' cannot be used together."))
    }
    "minimum_record_length" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("minimum_record_length" -> "10"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'minimum_record_length' cannot be used together."))
    }
    "maximum_record_length" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("maximum_record_length" -> "10"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'maximum_record_length' cannot be used together."))
    }
    "is_rdw_big_endian" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_rdw_big_endian" -> "true"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'is_rdw_big_endian' cannot be used together."))
    }
    "is_rdw_part_of_record_length" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_rdw_part_of_record_length" -> "true"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'is_rdw_part_of_record_length' cannot be used together."))
    }
    "rdw_adjustment" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("rdw_adjustment" -> "-1"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'rdw_adjustment' cannot be used together."))
    }
    "record_length_field" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("record_length_field" -> "A"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'record_length_field' cannot be used together."))
    }
    "record_header_parser" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("record_header_parser" -> "com.example.parser"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'record_header_parser' cannot be used together."))
    }
    "rhp_additional_info" in {
      val ex = intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("rhp_additional_info" -> "Additional info"))
      }
      assert(ex.getMessage.contains("Option 'record_length' and 'rhp_additional_info' cannot be used together."))
    }
  }

  "record length should be at least 1" when {
    "minimum_record_length" in {
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("minimum_record_length", 0)
          .load("/dummy")
      }
      assert(ex.getMessage.contains("The option 'minimum_record_length' should be at least 1."))
    }
    "minimum_record_length should not be bigger than maximum_record_length" in {
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("minimum_record_length", 10)
          .option("maximum_record_length", 8)
          .load("/dummy")
      }
      assert(ex.getMessage.contains("'minimum_record_length' (10) should be >= 'maximum_record_length' (8)."))
    }
  }

  private def getDataFrame(inputPath: String, extraOptions: Map[String, String] = Map.empty[String, String]): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("encoding", "ascii")
      .option("record_length", "2")
      .option("schema_retention_policy", "collapse_root")
      .option("improved_null_detection", "false")
      .options(extraOptions)
      .load(inputPath)
  }


}
