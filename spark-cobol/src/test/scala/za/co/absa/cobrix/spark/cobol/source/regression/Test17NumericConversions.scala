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

package za.co.absa.cobrix.spark.cobol.source.regression

import org.scalatest.WordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import java.nio.charset.StandardCharsets

/**
  * This suite checks if numerics are converted properly, even if there are leading zeros
  */
class Test17NumericConversions extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  "COMP-9" should {
    val copybook1 =
      """         01  R.
           05  N    PIC S9(4).
    """

    "non-sign punched numbers should be parsed correctly" when {
      val asciiContents = "1234\n0234\n0034\n0030\n0004\n0000\n"

      withTempTextFile("num_conversion", ".dat", StandardCharsets.UTF_8, asciiContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook1)
          .option("record_format", "D")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString
        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "schema should match" in {
          val expectedSchema =
            """root
              | |-- N: integer (nullable = true)
              |""".stripMargin

          assertEqualsMultiline(actualSchema, expectedSchema)
        }

        "data should match" in {
          val expectedData =
            """[ {
              |  "N" : 1234
              |}, {
              |  "N" : 234
              |}, {
              |  "N" : 34
              |}, {
              |  "N" : 30
              |}, {
              |  "N" : 4
              |}, {
              |  "N" : 0
              |} ]""".stripMargin

          assertEqualsMultiline(actualData, expectedData)
        }
      }
    }
  }

}
