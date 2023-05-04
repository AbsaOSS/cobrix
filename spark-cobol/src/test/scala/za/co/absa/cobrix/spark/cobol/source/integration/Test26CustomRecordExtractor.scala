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
        assert(CustomRecordExtractorMock.catchContext.headerStream != CustomRecordExtractorMock.catchContext.dataStream)
      }
    }

    "filter out records that are bigger than the specified size" in {
      val expected = """[{"A":"BBB"},{"A":"DDD"},{"A":"FFF"}]"""

      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map("minimum_record_length" -> "3"))

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(actual == expected)
        assert(CustomRecordExtractorMock.additionalInfo == "re info")
        assert(CustomRecordExtractorMock.catchContext.headerStream != CustomRecordExtractorMock.catchContext.dataStream)
      }
    }

    "filter out records that are smaller than the specified size" in {
      val expected = """[{"A":"AA"},{"A":"CC"},{"A":"EE"}]"""

      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map("maximum_record_length" -> "2"))

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(actual == expected)
        assert(CustomRecordExtractorMock.additionalInfo == "re info")
        assert(CustomRecordExtractorMock.catchContext.headerStream != CustomRecordExtractorMock.catchContext.dataStream)
      }
    }

    "support file headers" in {
      val expected = """[{"A":"012"},{"A":"345"},{"A":"6"}]"""

      val binData = Array(0x03, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6).map(_.toByte)

      withTempBinFile("custom_re", ".dat", binData) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("file_start_offset", 1)
          .option("record_extractor", "za.co.absa.cobrix.spark.cobol.mocks.CustomRecordExtractorWithFileHeaderMock")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(actual == expected)
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
