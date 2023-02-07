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

package za.co.absa.cobrix.spark.cobol

import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class CobrixSpec extends AnyWordSpec with SparkTestBase with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  "fromRdd" should {
    val copybook: String =
      """       01  RECORD.
        |         05  FIELD1                  PIC X(2).
        |         05  FIELD2                  PIC X(1).
        |""".stripMargin

    "read an RDD of strings" in {
      val expected =
        """[ {
          |  "RECORD" : {
          |    "FIELD1" : "Š",
          |    "FIELD2" : "B"
          |  }
          |}, {
          |  "RECORD" : {
          |    "FIELD1" : "DE",
          |    "FIELD2" : "F"
          |  }
          |}, {
          |  "RECORD" : {
          |    "FIELD1" : "GH",
          |    "FIELD2" : "I"
          |  }
          |} ]
          |""".stripMargin

      val rdd = spark.sparkContext.parallelize(Seq("ŠBC", "DEF", "GHI"))

      val df = Cobrix.fromRdd
        .copybookContents(copybook)
        .option("schema_retention_policy", "keep_original")
        .loadText(rdd)

      val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

      assertEqualsMultiline(actual, expected)
    }

    "read an RDD of byte arrays" in {
      val expected =
        """[ {
          |  "FIELD1" : "AB",
          |  "FIELD2" : "C"
          |}, {
          |  "FIELD1" : "DE",
          |  "FIELD2" : "F"
          |}, {
          |  "FIELD1" : "GH",
          |  "FIELD2" : "I"
          |} ]
          |""".stripMargin

      val data = Seq(
        Array(0xC1, 0xC2, 0xC3),
        Array(0xC4, 0xC5, 0xC6),
        Array(0xC7, 0xC8, 0xC9)
      ).map(_.map(_.toByte))

      val rdd = spark.sparkContext.parallelize(data)

      val df = Cobrix.fromRdd
        .copybookContents(copybook)
        .option("encoding", "ebcdic")
        .load(rdd)

      val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

      assertEqualsMultiline(actual, expected)
    }
  }
}
