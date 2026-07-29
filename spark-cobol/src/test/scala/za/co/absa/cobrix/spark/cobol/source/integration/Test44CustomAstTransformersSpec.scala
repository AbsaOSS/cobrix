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

class Test44CustomAstTransformersSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybookContent =
    """      01  R.
      |          03 A PIC X(1).
      |""".stripMargin

  private val rdwData = Array(
    0x00, 0x02, 0x00, 0x00, 0xF0, 0xF1,
    0x00, 0x02, 0x00, 0x00, 0xF2, 0xF3,
    0x00, 0x02, 0x00, 0x00, 0xF4, 0xF5
  ).map(_.toByte)

  "ast_transformers" should {
    "transform the ast before processing in Spark" in {
      val expectedData =
        """[ {
          |  "A_transformed" : "0",
          |  "EXTRA" : "1"
          |}, {
          |  "A_transformed" : "2",
          |  "EXTRA" : "3"
          |}, {
          |  "A_transformed" : "4",
          |  "EXTRA" : "5"
          |} ]""".stripMargin

      withTempBinFile("record_limit_rdw", ".dat", rdwData) { path =>
        AstTransformerSpy.transformCaller = 0
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookContent)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("ast_transformers", "za.co.absa.cobrix.spark.cobol.mocks.AstTransformerSpy")
          .load(path)

        assert(AstTransformerSpy.transformCaller == 1)

        val schema = df.schema

        assert(schema.fieldNames.contains("A_transformed"))
        assert(schema.fieldNames.contains("EXTRA"))

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        assertEqualsMultiline(actualData, expectedData)
      }
    }
  }
}
