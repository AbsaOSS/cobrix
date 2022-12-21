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

import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

/**
  * This suite checks if Spark is able to read numbers from ASCII files that overflow the expected data type.
  */
class Test16BinaryLittleEndian extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val binFileContents: Array[Byte] = Array[Byte](
    // Record 0
    16, 0, 0, 0,
    32, 0,
    // Record 1
    0, 0, 1, 0,
    0, 2
  )

  "COMP-9" should {
    val copybook1 =
      """         01  R.
           05  N1    PIC 9(8)  COMP-9.
           05  N2    PIC 9(3)  COMP-9.
    """

    "be interpreted as little-endian binary" when {
      withTempBinFile("num_overflow", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook1)
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString
        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "schema should match" in {
          val expectedSchema =
            """root
              | |-- N1: integer (nullable = true)
              | |-- N2: integer (nullable = true)
              |""".stripMargin

          assertEqualsMultiline(actualSchema, expectedSchema)
        }

        "data should match" in {
          val expectedData =
            """[ {
              |  "N1" : 16,
              |  "N2" : 32
              |}, {
              |  "N1" : 65536,
              |  "N2" : 512
              |} ]""".stripMargin

          assertEqualsMultiline(actualData, expectedData)
        }
      }
    }
  }

}
