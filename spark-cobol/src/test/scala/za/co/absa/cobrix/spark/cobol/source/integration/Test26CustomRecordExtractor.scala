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
import za.co.absa.cobrix.spark.cobol.mocks.CustomRecordExtractorMock
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test26CustomRecordExtractor extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test26 (custom record extractor)"

  private val copybook =
    """      01  R.
                03 A        PIC X(3).
      """
  private val data = "AABBBCCDDDEEFFF"

  "Custom record extractor" should {
    "apply the extractor to a binary data" in {
        val expected = """[{"A":"AA"},{"A":"BBB"},{"A":"CC"},{"A":"DDD"},{"A":"EE"},{"A":"FFF"}]"""

        withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
          val df = getDataFrame(tmpFileName)

          val actual = df.toJSON.collect().mkString("[", ",", "]")

          assert(actual == expected)
          assert(CustomRecordExtractorMock.additionalInfo == "re info")
        }
      }
    }

  "Custom record extractor options are not compatible with" when {
    "record_length" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("record_length" -> "2"))
      }
    }
    "is_record_sequence" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_record_sequence" -> "true"))
      }
    }
    "is_xcom" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_xcom" -> "true"))
      }
    }
    "is_rdw_big_endian" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_rdw_big_endian" -> "true"))
      }
    }
    "is_rdw_part_of_record_length" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("is_rdw_part_of_record_length" -> "true"))
      }
    }
    "rdw_adjustment" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("rdw_adjustment" -> "-1"))
      }
    }
    "record_length_field" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("record_length_field" -> "A"))
      }
    }
    "record_header_parser" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("record_header_parser" -> "com.example.parser"))
      }
    }
    "rhp_additional_info" in {
      intercept[IllegalArgumentException] {
        getDataFrame("/dummy", Map("rhp_additional_info" -> "Additional info"))
      }
    }
  }

  private def getDataFrame(inputPath: String, extraOptions: Map[String, String] = Map.empty[String, String]): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("encoding", "ascii")
      .option("schema_retention_policy", "collapse_root")
      .option("record_extractor", "za.co.absa.cobrix.spark.cobol.mocks.CustomRecordExtractorMock")
      .option("re_additional_info", "re info")
      .options(extraOptions)
      .load(inputPath)
  }

}
