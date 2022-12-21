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

import org.apache.spark.SparkException
import org.apache.spark.sql.functions.col
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test30FbFileSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test30 (FB record format)"

  private val copybook =
    """      01  R.
                03 A        PIC X(2).
      """

  val expected2Records = """[{"A":"00"},{"A":"11"}]"""
  val expected4Records = """[{"A":"00"},{"A":"11"},{"A":"22"},{"A":"33"}]"""

  "FB record loader" should {
    "load data when record length and block length are specified" in {
      testVbRecordLoad(1, 2, Map[String, String]("block_length" -> "4", "record_length" -> "2"), expected2Records)
    }

    "load data when block length is specified" in {
      testVbRecordLoad(1, 2, Map[String, String]("block_length" -> "2"), expected2Records)
    }

    "load data when the number records per block is specified" in {
      testVbRecordLoad(2, 2, Map[String, String]("records_per_block" -> "2"), expected4Records)
    }

    "load data when smaller records are provided" in {
      testVbRecordLoad(1, 2,
        Map[String, String]("block_length" -> "4", "record_length" -> "1"),
        """[{"A":"0"},{"A":"0"},{"A":"1"},{"A":"1"}]""",
        ignoreCount = true)
    }

    "load data with BDWs" in {
      testVbRecordLoad(2, 2,
        Map[String, String](),
        expected4Records,
        hasBDW = true)
    }

    "load empty data with BDWs" in {
      testVbRecordLoad(0, 0,
        Map[String, String](),
        "[]",
        hasBDW = true)
    }
  }

  "FB record failures should happen" when {
    "Block length is negative" in {
      val ex = intercept[SparkException] {
        testVbRecordLoad(1, 2, Map[String, String]("block_length" -> "-1"), "")
      }

      assert(ex.getCause.getMessage.contains("Block length should be positive. Got -1."))
    }

    "Records per block is negative" in {
      val ex = intercept[SparkException] {
        testVbRecordLoad(1, 2, Map[String, String]("records_per_block" -> "-1"), "")
      }

      assert(ex.getCause.getMessage.contains("Records per block should be positive. Got -1."))
    }

    "Both block length and records per block are specified" in {
      val ex = intercept[IllegalArgumentException] {
        testVbRecordLoad(1, 2, Map[String, String]("block_length" -> "2", "records_per_block" -> "2"), "")
      }

      assert(ex.getMessage.contains("Options 'block_length' and 'records_per_block' cannot be used together."))
    }
  }

  private def testVbRecordLoad(blocks: Int,
                               records: Int,
                               options: Map[String, String],
                               expected: String,
                               ignoreCount: Boolean = false,
                               hasBDW: Boolean = false): Unit = {
    val record: Seq[Byte] = Range(0, blocks).flatMap(blockNum => {
      val bdw: Seq[Byte] = if (hasBDW) {
        val byte0 = ((records * 2) % 256).toByte
        val byte1 = ((records * 2) / 256).toByte
        Seq(0, 0, byte0, byte1)
      } else {
        Nil
      }
      bdw ++ Range(0, records).flatMap(recordNum => {
        val idx = (blockNum * records + recordNum) % 10
        val v = (0xF0 + idx).toByte
        Seq(v, v)
      })
    })

    withTempBinFile("rec", ".dat", record.toArray) { tmpFileName1 =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("record_format", "FB")
        .options(options)
        .load(tmpFileName1)

      val actual = df
        .orderBy(col("A"))
        .toJSON
        .collect()
        .mkString("[", ",", "]")

      if (!ignoreCount) {
        assert(df.count() == blocks * records)
      }
      assert(actual == expected)
    }
  }

}
