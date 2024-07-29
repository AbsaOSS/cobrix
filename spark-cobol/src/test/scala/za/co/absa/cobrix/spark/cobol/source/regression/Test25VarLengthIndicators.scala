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

import java.nio.charset.StandardCharsets

class Test25VarLengthIndicators extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """
      01 R.
           10 IND.
               15  D1-PRESENT                   PIC 9(1).
               15  D2-PRESENT                   PIC 9(1).
               15  D3-PRESENT                   PIC 9(1).
        05 D1.
           10 P1 OCCURS 0 TO 1 TIMES
                   DEPENDING ON D1-PRESENT.
               15  COL1                         PIC X(3).
        05 D2.
           10 P2 OCCURS 0 TO 1 TIMES
                   DEPENDING ON D2-PRESENT.
               15  COL2                         PIC X(5).
        05 D3.
           10 P3 OCCURS 0 TO 1 TIMES
                   DEPENDING ON D3-PRESENT.
               15 COL3                         PIC X(7).
    """

  val binFileContents: Array[Byte] = Array(
    // D1
    0xF1, 0xF0, 0xF0, 0xF1, 0xF2, 0xF3,
    // D2
    0xF0, 0xF1, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5,
    // D3
    0xF0, 0xF0, 0xF1, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7,
    // D1+D2
    0xF1, 0xF1, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8,
    // D1+D3
    0xF1, 0xF0, 0xF1, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0,
    // D2+D3
    0xF0, 0xF1, 0xF1, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0, 0xF1, 0xF2,
    // D1+D2+D3
    0xF1, 0xF1, 0xF1, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5
  ).map(_.toByte)

  val expected: String =
    """[ {
      |  "IND" : {
      |    "D1_PRESENT" : 1,
      |    "D2_PRESENT" : 0,
      |    "D3_PRESENT" : 0
      |  },
      |  "D1" : {
      |    "P1" : [ {
      |      "COL1" : "123"
      |    } ]
      |  },
      |  "D2" : {
      |    "P2" : [ ]
      |  },
      |  "D3" : {
      |    "P3" : [ ]
      |  }
      |}, {
      |  "IND" : {
      |    "D1_PRESENT" : 0,
      |    "D2_PRESENT" : 1,
      |    "D3_PRESENT" : 0
      |  },
      |  "D1" : {
      |    "P1" : [ ]
      |  },
      |  "D2" : {
      |    "P2" : [ {
      |      "COL2" : "12345"
      |    } ]
      |  },
      |  "D3" : {
      |    "P3" : [ ]
      |  }
      |}, {
      |  "IND" : {
      |    "D1_PRESENT" : 0,
      |    "D2_PRESENT" : 0,
      |    "D3_PRESENT" : 1
      |  },
      |  "D1" : {
      |    "P1" : [ ]
      |  },
      |  "D2" : {
      |    "P2" : [ ]
      |  },
      |  "D3" : {
      |    "P3" : [ {
      |      "COL3" : "1234567"
      |    } ]
      |  }
      |}, {
      |  "IND" : {
      |    "D1_PRESENT" : 1,
      |    "D2_PRESENT" : 1,
      |    "D3_PRESENT" : 0
      |  },
      |  "D1" : {
      |    "P1" : [ {
      |      "COL1" : "123"
      |    } ]
      |  },
      |  "D2" : {
      |    "P2" : [ {
      |      "COL2" : "45678"
      |    } ]
      |  },
      |  "D3" : {
      |    "P3" : [ ]
      |  }
      |}, {
      |  "IND" : {
      |    "D1_PRESENT" : 1,
      |    "D2_PRESENT" : 0,
      |    "D3_PRESENT" : 1
      |  },
      |  "D1" : {
      |    "P1" : [ {
      |      "COL1" : "123"
      |    } ]
      |  },
      |  "D2" : {
      |    "P2" : [ ]
      |  },
      |  "D3" : {
      |    "P3" : [ {
      |      "COL3" : "4567890"
      |    } ]
      |  }
      |}, {
      |  "IND" : {
      |    "D1_PRESENT" : 0,
      |    "D2_PRESENT" : 1,
      |    "D3_PRESENT" : 1
      |  },
      |  "D1" : {
      |    "P1" : [ ]
      |  },
      |  "D2" : {
      |    "P2" : [ {
      |      "COL2" : "12345"
      |    } ]
      |  },
      |  "D3" : {
      |    "P3" : [ {
      |      "COL3" : "6789012"
      |    } ]
      |  }
      |}, {
      |  "IND" : {
      |    "D1_PRESENT" : 1,
      |    "D2_PRESENT" : 1,
      |    "D3_PRESENT" : 1
      |  },
      |  "D1" : {
      |    "P1" : [ {
      |      "COL1" : "123"
      |    } ]
      |  },
      |  "D2" : {
      |    "P2" : [ {
      |      "COL2" : "45678"
      |    } ]
      |  },
      |  "D3" : {
      |    "P3" : [ {
      |      "COL3" : "9012345"
      |    } ]
      |  }
      |} ]""".stripMargin

  "Variable length by indicators" should {
    "work as expected" in {
      withTempBinFile("var_len_ind", ".dat", binFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("variable_size_occurs", "true")
          .option("pedantic", "true")
          .load(tmpFileName)

        val actual = SparkUtils.convertDataFrameToPrettyJSON(df)

        assertEqualsMultiline(actual, expected)
      }
    }
  }
}
