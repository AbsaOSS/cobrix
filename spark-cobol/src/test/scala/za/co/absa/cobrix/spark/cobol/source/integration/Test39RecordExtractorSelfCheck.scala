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

class Test39RecordExtractorSelfCheck extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  private val copybook =
    """      01  R.
                03 A        PIC X(2).
      """
  private val data = "AABBCCDDEEFF"

  "Record extractor supporting indexes" should {
    "should work with indexes" in {
      val expected = """[{"A":"AA"},{"A":"BB"},{"A":"CC"},{"A":"DD"},{"A":"EE"},{"A":"FF"}]"""

      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map(
          "enable_self_checks" -> "true",
          "record_extractor" -> "za.co.absa.cobrix.spark.cobol.mocks.FixedRecordExtractor",
          "input_split_records" -> "2")
        )

        val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

        assert(actual == expected)
      }
    }
  }

  "Record extractor not supporting indexes" should {
    "should fail self checks when offsets are not properly handled" in {
      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map(
          "enable_self_checks" -> "true",
          "record_extractor" -> "za.co.absa.cobrix.spark.cobol.mocks.FixedRecordExtractorNoIndex",
          "input_split_records" -> "2")
        )

        val ex = intercept[RuntimeException] {
          df.show(false)
          df.count()
        }

        assert(ex.getMessage.contains("Record extractor self-check failed. The record extractor returned wrong record when started from non-zero offset"))
        assert(ex.getMessage.contains("offset: 4"))
      }
    }

    "should fail self checks when the extractor returns hasNext=false unexpectedly" in {
      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map(
          "enable_self_checks" -> "true",
          "record_extractor" -> "za.co.absa.cobrix.spark.cobol.mocks.FixedRecordExtractorBroken",
          "input_split_records" -> "2")
        )

        val ex = intercept[RuntimeException] {
          df.show(false)
          df.count()
        }

        assert(ex.getMessage.contains("Record extractor self-check failed. When reading from a non-zero offset the extractor returned hasNext()=false"))
        assert(ex.getMessage.contains("offset: 2"))
      }
    }

    "should still work if self checks is turned off" in {
      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map(
          "enable_self_checks" -> "false",
          "record_extractor" -> "za.co.absa.cobrix.spark.cobol.mocks.FixedRecordExtractorNoIndex",
          "input_split_records" -> "2")
        )

        // No guarantees regarding the correct record count at this point
        assert(df.count() > 4)
      }
    }

    "should still work if there is just one record" in {
      withTempBinFile("custom_re", ".dat", "AA".getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map(
          "enable_self_checks" -> "true",
          "record_extractor" -> "za.co.absa.cobrix.spark.cobol.mocks.FixedRecordExtractorNoIndex")
        )

        assert(df.count() == 1)
      }
    }

    "should still work if indexes are disabled" in {
      val expected = """[{"A":"AA"},{"A":"BB"},{"A":"CC"},{"A":"DD"},{"A":"EE"},{"A":"FF"}]"""

      withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
        val df = getDataFrame(tmpFileName, Map(
          "record_extractor" -> "za.co.absa.cobrix.spark.cobol.mocks.FixedRecordExtractorNoIndex",
          "enable_indexes" -> "false")
        )

        val actual = df.orderBy("A").toJSON.collect().mkString("[", ",", "]")

        assert(actual == expected)
      }
    }
  }

  private def getDataFrame(inputPath: String, extraOptions: Map[String, String] = Map.empty[String, String]): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("encoding", "ascii")
      .options(extraOptions)
      .load(inputPath)
  }
}
