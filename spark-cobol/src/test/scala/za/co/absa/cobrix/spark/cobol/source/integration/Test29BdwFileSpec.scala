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
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col
import org.scalatest.WordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test29BdwFileSpec extends WordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test29 (VB record format RDW+BDW"

  private val copybook =
    """      01  R.
                03 A        PIC X(1).
      """

  val expected2Records = """[{"A":"0"},{"A":"1"}]"""
  val expected4Records = """[{"A":"0"},{"A":"1"},{"A":"2"},{"A":"3"}]"""

  "VB record (little-endian BDW, little-endian RDW)" should {
    "load data without adjustments" in {
      testVbRecordLoad(false, false, 0, 0, 2, 1, expected2Records)
    }
    "load data with adjustments" in {
      testVbRecordLoad(false, false, -1, 1, 2, 1, expected2Records)
    }
  }

  "VB record (big-endian BDW, little-endian RDW)" should {
    "load data without adjustments" in {
      testVbRecordLoad(true, false, 0, 0, 1, 2, expected2Records)
    }
    "load data with adjustments" in {
      testVbRecordLoad(true, false, -1, 1, 1, 2, expected2Records)
    }
  }

  "VB record (little-endian BDW, big-endian RDW)" should {
    "load data without adjustments" in {
      testVbRecordLoad(false, true, 0, 0, 1, 2, expected2Records)
    }
    "load data with adjustments" in {
      testVbRecordLoad(false, true, -1, 1, 1, 2, expected2Records)
    }
  }

  "VB record (big-endian BDW, big-endian RDW)" should {
    "load data without adjustments" in {
      testVbRecordLoad(true, true, 0, 0, 2, 2, expected4Records)
    }
    "load data with adjustments" in {
      testVbRecordLoad(true, true, -1, 1, 2, 2, expected4Records)
    }
  }

  "in case of failures" should {
    "thrown an exception if there is only BDW, but nor RDW" in {
      val record: Seq[Byte] = getHeader(5, false, 0) ++ Seq(0xF0.toByte)

      withTempBinFile("rec", ".dat", record.toArray) { tmpFileName1 =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "VB")
          .load(tmpFileName1)

        intercept[SparkException] {
          df.orderBy(col("A"))
            .toJSON
            .collect()
        }
      }
    }
  }

  private def testVbRecordLoad(bdwBigEndian: Boolean,
                               rdwBigEndian: Boolean,
                               bdwAdjustment: Int,
                               rdwAdjustment: Int,
                               blocks: Int,
                               records: Int,
                               expected: String): Unit = {
    val record: Seq[Byte] = Range(0, blocks).flatMap(blockNum => {
      getHeader(records * 5, bdwBigEndian, bdwAdjustment) ++
      Range(0, records).flatMap(recordNum => {
        val idx = (blockNum * records + recordNum) % 10
        getHeader(1, rdwBigEndian, rdwAdjustment) ++ Seq((0xF0 + idx).toByte)
      })
    })

    withTempBinFile("rec", ".dat", record.toArray) { tmpFileName1 =>
      val df =     spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("record_format", "VB")
        .option("is_bdw_big_endian", bdwBigEndian)
        .option("is_rdw_big_endian", rdwBigEndian)
        .option("bdw_adjustment", -bdwAdjustment)
        .option("rdw_adjustment", -rdwAdjustment)
        .load(tmpFileName1)

      val actual = df
        .orderBy(col("A"))
        .toJSON
        .collect()
        .mkString("[", ",", "]")

      assert(df.count() == blocks * records)
      assert(actual == expected)
    }
  }

  private def getHeader(payloadSize: Int, bigEndian: Boolean, adjustment: Int): Seq[Byte] = {
    val byte0 = ((payloadSize + adjustment) % 256).toByte
    val byte1 = ((payloadSize + adjustment) / 256).toByte
    if (bigEndian) {
      Seq(byte1, byte0, 0, 0)
    } else {
      Seq(0, 0, byte0, byte1)
    }
  }


}
