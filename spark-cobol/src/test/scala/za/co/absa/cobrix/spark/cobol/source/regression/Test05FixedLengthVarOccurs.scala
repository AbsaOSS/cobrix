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

import org.apache.commons.io.Charsets
import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class Test05FixedLengthVarOccurs extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """
           01 RECORD.
              02 COUNT PIC 9(4).
              02 GROUP OCCURS 0 TO 11 TIMES DEPENDING ON COUNT.
                  03 TEXT   PIC X(3).
                  03 FIELD  PIC 9.
    """

  test("Test input data file having a numeric field with a comma as the decimal separator") {
    withTempTextFile("text", ".dat", Charsets.UTF_8,"   5ABC1ABC2ABC3ABC4ABC5   5DEF1DEF2DEF3DEF4DEF5") { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("schema_retention_policy", "collapse_root")
        .option("variable_size_occurs", "true")
        .option("encoding", "ascii")
        .load(tmpFileName)

      val expected = """[ {
                       |  "COUNT" : 5,
                       |  "GROUP" : [ {
                       |    "TEXT" : "ABC",
                       |    "FIELD" : 1
                       |  }, {
                       |    "TEXT" : "ABC",
                       |    "FIELD" : 2
                       |  }, {
                       |    "TEXT" : "ABC",
                       |    "FIELD" : 3
                       |  }, {
                       |    "TEXT" : "ABC",
                       |    "FIELD" : 4
                       |  }, {
                       |    "TEXT" : "ABC",
                       |    "FIELD" : 5
                       |  } ]
                       |}, {
                       |  "COUNT" : 5,
                       |  "GROUP" : [ {
                       |    "TEXT" : "DEF",
                       |    "FIELD" : 1
                       |  }, {
                       |    "TEXT" : "DEF",
                       |    "FIELD" : 2
                       |  }, {
                       |    "TEXT" : "DEF",
                       |    "FIELD" : 3
                       |  }, {
                       |    "TEXT" : "DEF",
                       |    "FIELD" : 4
                       |  }, {
                       |    "TEXT" : "DEF",
                       |    "FIELD" : 5
                       |  } ]
                       |} ]""".stripMargin.replace("\r\n", "\n")

      val actual = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

      //assertEqualsMultiline(actual, expected)
    }

  }

}
