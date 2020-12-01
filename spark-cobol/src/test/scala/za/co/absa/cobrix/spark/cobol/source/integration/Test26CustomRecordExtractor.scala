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

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.mocks.CustomRecordExtractorMock
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test26CustomRecordExtractor extends FunSuite with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test26 (custom record extractor)"

  private val copybook =
    """      01  R.
                03 A        PIC X(3).
      """
  private val data = "AABBBCCDDDEEFFF"

  test(s"Integration test on $exampleName - custom record extractor") {
    val expected = """[{"A":"AA"},{"A":"BBB"},{"A":"CC"},{"A":"DDD"},{"A":"EE"},{"A":"FFF"}]"""

    withTempBinFile("custom_re", ".dat", data.getBytes) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        //.option("is_record_sequence", "true")
        .option("encoding", "ascii")
        .option("schema_retention_policy", "collapse_root")
        .option("record_extractor", "za.co.absa.cobrix.spark.cobol.mocks.CustomRecordExtractorMock")
        .option("re_additional_info", "re info")
        .load(tmpFileName)

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(actual == expected)
      assert(CustomRecordExtractorMock.additionalInfo == "re info")
    }
  }
}
