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
import za.co.absa.cobrix.spark.cobol.mocks.AstTransformerSpy
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test45PgpEncryptedFilesSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val exampleName = "Test41"
  private val inputCopybookPath = "file://../data/test41_copybook.cob"
  private val inputDataPath = "../data/test41_data"


  "gpg encrypted files" should {
    "load normally" in {
      val df = spark.read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("generate_record_id", "true")
        .option("enable_indexes", "false")
        //.option("debug_ignore_file_size", "true")
        .load(inputDataPath)

      println(df.count())
      df.show(false)
    }
  }
}
