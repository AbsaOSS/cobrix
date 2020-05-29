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

package za.co.absa.cobrix.spark.cobol.source.feature

import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test01DisplayPicAsStrings extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 N     PIC 9(4).
                03 D1    PIC 99V99.
                03 D2    PIC 99.99.
    """

  val binFileContents: Array[Byte] = "0001011012.342010002200.01 300   1  .02".getBytes()

  test("Test a numeric fields having DISPLAY format are parsed as numbers") {
    withTempBinFile("num_display1", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("pedantic", "true")
        .option("encoding", "ascii")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      val expectedSchema =
      """root
        | |-- N: integer (nullable = true)
        | |-- D1: decimal(4,2) (nullable = true)
        | |-- D2: decimal(4,2) (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")

      val expectedData =
      """[ {
        |  "N" : 1,
        |  "D1" : 1.1,
        |  "D2" : 12.34
        |}, {
        |  "N" : 2010,
        |  "D1" : 0.22,
        |  "D2" : 0.01
        |}, {
        |  "N" : 300,
        |  "D1" : 0.01,
        |  "D2" : 0.02
        |} ]""".stripMargin.replace("\r\n", "\n")

      val actualSchema = df.schema.treeString
      val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      assertEqualsMultiline(actualSchema, expectedSchema)
      assertEqualsMultiline(actualData, expectedData)
    }
  }

}
