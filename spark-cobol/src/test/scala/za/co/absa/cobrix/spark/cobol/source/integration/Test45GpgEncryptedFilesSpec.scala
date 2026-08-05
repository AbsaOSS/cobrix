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
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, ResourceUtils}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

class Test45GpgEncryptedFilesSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val exampleName = "Test41"
  private val inputCopybookPath = "file://../data/test41_copybook.cob"
  private val inputDataPath = "../data/test41_data"
  private val expectedResultsAPath = "../data/test41_expected/test41a.txt"
  private val actualResultsAPath = "../data/test41_expected/test41a_actual.txt"
  private val expectedResultsBPath = "../data/test41_expected/test41b.txt"
  private val actualResultsBPath = "../data/test41_expected/test41b_actual.txt"
  private val expectedResultsCPath = "../data/test41_expected/test41c.txt"
  private val actualResultsCPath = "../data/test41_expected/test41c_actual.txt"

  "gpg encrypted files" should {
    "load normally a fixed-record-length file" in {
      val gpgPrivateKey = ResourceUtils.readResourceAsString("/test/test_gpg_key.asc")
      val df = spark.read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("gpg_private_key", gpgPrivateKey)
        .option("pedantic", "true")
        .load(inputDataPath)

      val actual = df.toJSON.take(60)
      val expected = Files.readAllLines(Paths.get(expectedResultsAPath), StandardCharsets.ISO_8859_1).toArray

      if (!actual.sameElements(expected)) {
        FileUtils.writeStringsToFile(actual, actualResultsAPath)
        assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsAPath to $actualResultsAPath for details.")
      }
    }

    "load normally a fixed-record-length file without indexes and with record ids" in {
      val gpgPrivateKey = ResourceUtils.readResourceAsString("/test/test_gpg_key.asc")
      val df = spark.read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("gpg_private_key", gpgPrivateKey)
        .option("pedantic", "true")
        .option("generate_record_id", "true")
        .option("enable_indexes", "false")
        .load(inputDataPath)

      val actual = df.toJSON.take(60)
      val expected = Files.readAllLines(Paths.get(expectedResultsBPath), StandardCharsets.ISO_8859_1).toArray

      if (!actual.sameElements(expected)) {
        FileUtils.writeStringsToFile(actual, actualResultsBPath)
        assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsBPath to $actualResultsBPath for details.")
      }
    }

    "load normally a variable-record-length file with indexes" in {
      val gpgPrivateKey = ResourceUtils.readResourceAsString("/test/test_gpg_key.asc")
      val df = spark.read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("gpg_private_key", gpgPrivateKey)
        .option("generate_record_id", "true")
        .option("input_split_records", 1)
        .option("pedantic", "true")
        .load(inputDataPath)

      val actual = df.toJSON.take(60)
      val expected = Files.readAllLines(Paths.get(expectedResultsCPath), StandardCharsets.ISO_8859_1).toArray

      if (!actual.sameElements(expected)) {
        FileUtils.writeStringsToFile(actual, actualResultsCPath)
        assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsCPath to $actualResultsCPath for details.")
      }
    }
  }
}
