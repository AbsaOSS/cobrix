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

import org.apache.spark.sql.DataFrame
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

import java.nio.charset.StandardCharsets

class Test43RecordLimitSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  private val fixedLengthCopybook =
    """      01  R.
      |          03 A PIC X(2).
      |""".stripMargin

  private val fixedLengthValues = Seq("00", "01", "02", "03", "10", "11", "12", "13")

  private val rdwData = Array(
    0x00, 0x02, 0x00, 0x00, 0x30, 0x31,
    0x00, 0x02, 0x00, 0x00, 0x32, 0x33,
    0x00, 0x02, 0x00, 0x00, 0x34, 0x35,
    0x00, 0x02, 0x00, 0x00, 0x36, 0x37,
    0x00, 0x02, 0x00, 0x00, 0x38, 0x39,
    0x00, 0x02, 0x00, 0x00, 0x3A, 0x3B
  ).map(_.toByte)

  "record_limit" should {
    "apply globally across multiple fixed-length input paths" in {
      withTempBinFile("record_limit_fixed_1", ".dat", "00010203".getBytes(StandardCharsets.US_ASCII)) { firstPath =>
        withTempBinFile("record_limit_fixed_2", ".dat", "10111213".getBytes(StandardCharsets.US_ASCII)) { secondPath =>
          val actual = values(fixedLengthDataFrame(Seq(firstPath, secondPath), Map("record_limit" -> "3")))

          assert(actual.length == 3)
          assert(actual.distinct.length == actual.length)
          assert(actual.forall(fixedLengthValues.contains))
        }
      }
    }

    "return no rows when set to zero" in {
      withTempBinFile("record_limit_zero", ".dat", "00010203".getBytes(StandardCharsets.US_ASCII)) { path =>
        assert(fixedLengthDataFrame(Seq(path), Map("record_limit" -> "0")).count() == 0)
      }
    }

    "leave all rows available when absent" in {
      withTempBinFile("record_limit_absent_1", ".dat", "00010203".getBytes(StandardCharsets.US_ASCII)) { firstPath =>
        withTempBinFile("record_limit_absent_2", ".dat", "10111213".getBytes(StandardCharsets.US_ASCII)) { secondPath =>
          val actual = values(fixedLengthDataFrame(Seq(firstPath, secondPath)))

          assert(actual.length == fixedLengthValues.length)
          assert(actual.toSet == fixedLengthValues.toSet)
        }
      }
    }

    "limit UTF-8 text records in D format" in {
      withTempTextFile("record_limit_text", ".txt", StandardCharsets.UTF_8, "aa\nbb\ncc") { path =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", fixedLengthCopybook)
          .option("record_format", "D")
          .option("ascii_charset", "UTF-8")
          .option("record_limit", "2")
          .load(path)

        assert(values(df) == Seq("aa", "bb"))
      }
    }

    "limit variable-length RDW records" in {
      withTempBinFile("record_limit_rdw", ".dat", rdwData) { path =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", fixedLengthCopybook)
          .option("encoding", "ascii")
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("record_limit", "2")
          .load(path)

        assert(values(df) == Seq("01", "23"))
      }
    }

    "limit variable-length RDW records which allows indexing" in {
      withTempBinFile("record_limit_rdw", ".dat", rdwData) { path =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", fixedLengthCopybook)
          .option("encoding", "ascii")
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("record_limit", "1000000")
          .load(path)

        assert(df.count() == 6)
      }
    }

    "be accepted in pedantic mode" in {
      withTempBinFile("record_limit_pedantic", ".dat", "00010203".getBytes(StandardCharsets.US_ASCII)) { path =>
        val df = fixedLengthDataFrame(Seq(path), Map("pedantic" -> "true", "record_limit" -> "1"))

        assert(values(df) == Seq("00"))
      }
    }
  }

  private def fixedLengthDataFrame(paths: Seq[String], options: Map[String, String] = Map.empty): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", fixedLengthCopybook)
      .option("encoding", "ascii")
      .option("data_paths", paths.mkString(","))
      .options(options)
      .load()
  }

  private def values(df: DataFrame): Seq[String] = {
    df.select("A").collect().map(_.getString(0)).toSeq
  }
}
