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
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test29BdwFileSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test29 (VB record format RDW+BDW)"

  private val copybook =
    """      01  R.
                03 A        PIC X(2).
      """

  val expected2Records = """[{"A":"00"},{"A":"01"}]"""
  val expected4Records = """[{"A":"00"},{"A":"01"},{"A":"02"},{"A":"03"}]"""
  val expected20Records = """[{"A":"00"},{"A":"01"},{"A":"02"},{"A":"03"},{"A":"04"},{"A":"05"},{"A":"06"},{"A":"07"},{"A":"08"},{"A":"09"},{"A":"10"},{"A":"11"},{"A":"12"},{"A":"13"},{"A":"14"},{"A":"15"},{"A":"16"},{"A":"17"},{"A":"18"},{"A":"19"}]"""
  val expected20RecordsWithIds = """[{"R":0,"A":"00"},{"R":1,"A":"01"},{"R":2,"A":"02"},{"R":3,"A":"03"},{"R":4,"A":"04"},{"R":5,"A":"05"},{"R":6,"A":"06"},{"R":7,"A":"07"},{"R":8,"A":"08"}]"""

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
    "VB indexing" in {
      testVbRecordLoad(true, true, 0, 0, 5, 4, expected20Records,
        Map("input_split_records" -> "3"), Some(5))
    }
    "VB indexing with record id generator" in {
      testVbRecordLoad(true, true, 0, 0, 3, 3, expected20RecordsWithIds,
        Map("input_split_records" -> "4", "generate_record_id" -> "true"), Some(2))
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
                               expected: String,
                               options: Map[String, String] = Map.empty[String, String],
                               expectedPartitions: Option[Int] = None): Unit = {
    val record: Seq[Byte] = Range(0, blocks).flatMap(blockNum => {
      getHeader(records * 6, bdwBigEndian, bdwAdjustment) ++
      Range(0, records).flatMap(recordNum => {
        val idx0 = (blockNum * records + recordNum) / 10
        val idx1 = (blockNum * records + recordNum) % 10
        getHeader(2, rdwBigEndian, rdwAdjustment) ++ Seq((0xF0 + idx0).toByte, (0xF0.toByte + idx1).toByte)
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
        .options(options)
        .load(tmpFileName1)

      val df2 = if (df.schema.fields.length > 1) {
        df.drop("File_Id")
          .drop("Record_Byte_Length")
          .withColumnRenamed("Record_Id", "R")
      } else {
        df
      }

      val actual = df2
        .orderBy(col("A"))
        .toJSON
        .collect()
        .mkString("[", ",", "]")

      assert(df.count() == blocks * records)
      assert(actual == expected)
      expectedPartitions.foreach(expectedPartitions => {
        assert(df.rdd.getNumPartitions == expectedPartitions)
      })
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
