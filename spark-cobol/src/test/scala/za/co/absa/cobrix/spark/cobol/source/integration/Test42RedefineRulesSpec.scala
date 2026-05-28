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
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import java.nio.charset.StandardCharsets

class Test42RedefineRulesSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {
  private val copybook =
    """      01  R.
                03 ID      PIC 9(1).
                03 G1.
                   04 F1   PIC S9(2).
                03 G2 REDEFINES G1.
                   04 F2   PIC X(2).
                03 G3 REDEFINES G1.
                   04 F3   PIC 9(1).
      """

  "Files with redefine rules" should {
    "extract data according to the rules" when {
      val dataAscii = "111\n222\n33\n"
      withTempTextFile("redefine_rules1", ".dat", StandardCharsets.UTF_8, dataAscii) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("redefine-rule:1", "G1 => ID = 1")
          .option("redefine-rule:2", "G2 => ID = 2")
          .option("redefine-rule:3", "G3 => ID = 3")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualSchema = df.schema.treeString
        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "schema should match" in {
          val expectedSchema =
            """root
              | |-- ID: integer (nullable = true)
              | |-- G1: struct (nullable = true)
              | |    |-- F1: integer (nullable = true)
              | |-- G2: struct (nullable = true)
              | |    |-- F2: string (nullable = true)
              | |-- G3: struct (nullable = true)
              | |    |-- F3: integer (nullable = true)
              |""".stripMargin


          compareTextVertical(actualSchema, expectedSchema)
        }

        "data should match" in {
          val expectedData =
            """[ {
              |  "ID" : 1,
              |  "G1" : {
              |    "F1" : 11
              |  }
              |}, {
              |  "ID" : 2,
              |  "G2" : {
              |    "F2" : "22"
              |  }
              |}, {
              |  "ID" : 3,
              |  "G3" : {
              |    "F3" : 3
              |  }
              |} ]""".stripMargin

          compareTextVertical(actualData, expectedData)
        }
      }
    }

    "extract data according to the rules with null input fields" when {
      val data = Array(
        0xF1, 0xF1, 0xF1,
        0xF2, 0xF2, 0xF2,
        0x00, 0xF3, 0xF3
      ).map(_.toByte)

      withTempBinFile("redefine_rules2", ".dat", data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("redefine-rule:1", "G1 => ID = 1")
          .option("redefine-rule:2", "G2 => ID = 2")
          .option("redefine-rule:3", "G3 => ID = 3")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "data should match" in {
          val expectedData =
            """[ {
              |  "ID" : 1,
              |  "G1" : {
              |    "F1" : 11
              |  }
              |}, {
              |  "ID" : 2,
              |  "G2" : {
              |    "F2" : "22"
              |  }
              |}, { } ]""".stripMargin

          compareTextVertical(actualData, expectedData)
        }
      }
    }

    "extract data according to the rules with allowed null values" when {
      val data = Array(
        0xF1, 0xF1, 0xF1,
        0xF2, 0xF2, 0xF2,
        0x00, 0xF3, 0xF3
      ).map(_.toByte)

      withTempBinFile("redefine_rules3", ".dat", data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("redefine-rule:1", "G1 => in(ID, 1, null)")
          .option("redefine-rule:2", "G2 => ID = 2 || ID = null")
          .option("redefine-rule:3", "G3 => ID = 3 || ID = null")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "data should match" in {
          val expectedData =
            """[ {
              |  "ID" : 1,
              |  "G1" : {
              |    "F1" : 11
              |  }
              |}, {
              |  "ID" : 2,
              |  "G2" : {
              |    "F2" : "22"
              |  }
              |}, {
              |  "G1" : {
              |    "F1" : 33
              |  },
              |  "G2" : {
              |    "F2" : "33"
              |  },
              |  "G3" : {
              |    "F3" : 3
              |  }
              |} ]""".stripMargin

          compareTextVertical(actualData, expectedData)
        }
      }
    }

    "extract data according to the rules with string values" when {
      val copybook =
        """      01  R.
                03 ID      PIC X(1).
                03 G1.
                   04 F1   PIC S9(2).
                03 G2 REDEFINES G1.
                   04 F2   PIC X(2).
                03 G3 REDEFINES G1.
                   04 F3   PIC 9(1).
      """

      val data = Array(
        0xC1, 0xF1, 0xF1,
        0xC2, 0xF2, 0xF2,
        0x00, 0xF3, 0xF3
      ).map(_.toByte)

      withTempBinFile("redefine_rules3", ".dat", data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("redefine-rule:1", "G1 => in(ID, 'A', null)")
          .option("redefine-rule:2", "G2 => ID='B' && true")
          .option("redefine-rule:3", "G3 => ID = 'C' || ID = null")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "data should match" in {
          val expectedData =
            """[ {
              |  "ID" : "A",
              |  "G1" : {
              |    "F1" : 11
              |  }
              |}, {
              |  "ID" : "B",
              |  "G2" : {
              |    "F2" : "22"
              |  }
              |}, {
              |  "G1" : {
              |    "F1" : 33
              |  },
              |  "G3" : {
              |    "F3" : 3
              |  }
              |} ]""".stripMargin

          compareTextVertical(actualData, expectedData)
        }
      }
    }

    "extract data according to the rules with decimal values" when {
      val copybook =
        """      01  R.
                03 ID      PIC 9V9.
                03 G1.
                   04 F1   PIC S9(2).
                03 G2 REDEFINES G1.
                   04 F2   PIC X(2).
                03 G3 REDEFINES G1.
                   04 F3   PIC 9(1).
      """

      val data = Array(
        0xF1, 0xF1, 0xF1, 0xF1,
        0xF2, 0xF2, 0xF2, 0xF2,
        0xF3, 0xF0, 0xF3, 0xF3,
        0x00, 0x00, 0xF4, 0xF4
      ).map(_.toByte)

      withTempBinFile("redefine_rules3", ".dat", data) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("redefine-rule:1", "G1 => in(ID, 1, null)")
          .option("redefine-rule:2", "G2 => ID=2 && true")
          .option("redefine-rule:3", "G3 => ID = 3 || ID = null")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actualData = SparkUtils.prettyJSON(df.toJSON.collect().mkString("[", ",", "]"))

        "data should match" in {
          val expectedData =
            """[ {
              |  "ID" : 1.1,
              |  "G1" : {
              |    "F1" : 11
              |  }
              |}, {
              |  "ID" : 2.2,
              |  "G2" : {
              |    "F2" : "22"
              |  }
              |}, {
              |  "ID" : 3.0,
              |  "G3" : {
              |    "F3" : 3
              |  }
              |}, {
              |  "G1" : {
              |    "F1" : 44
              |  },
              |  "G3" : {
              |    "F3" : 4
              |  }
              |} ]""".stripMargin

          compareTextVertical(actualData, expectedData)
        }
      }
    }
  }
}
