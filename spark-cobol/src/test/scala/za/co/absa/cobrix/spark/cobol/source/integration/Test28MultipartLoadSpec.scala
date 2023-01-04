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
import org.apache.spark.sql.functions.col
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import java.io.File
import java.nio.charset.StandardCharsets

//noinspection NameBooleanParameters
class Test28MultipartLoadSpec extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test28 (multipart load)"

  private val copybook =
    """      01  R.
                03 A        PIC X(2).
      """
  private val data1 = "010203040506070809"
  private val data2 = "101112131415161718"

  "Multipart path spec" should {
    val expected = """[{"A":"01"},{"A":"02"},{"A":"03"},{"A":"04"},{"A":"05"},{"A":"06"},{"A":"07"},{"A":"08"},{"A":"09"},{"A":"10"},{"A":"11"},{"A":"12"},{"A":"13"},{"A":"14"},{"A":"15"},{"A":"16"},{"A":"17"},{"A":"18"}]"""

    "load all available files using data_paths option" in {
      withTempBinFile("rec_len1", ".dat", data1.getBytes) { tmpFileName1 =>
        withTempBinFile("rec_len2", ".dat", data2.getBytes) { tmpFileName2 =>
          val df = getDataFrame(Seq(tmpFileName1, tmpFileName2))

          val actual = df
            .orderBy(col("A"))
            .toJSON
            .collect()
            .mkString("[", ",", "]")

          assert(df.count() == 18)
          assert(actual == expected)
        }
      }
    }

    "load a single path using data_paths option" in {
      val expected = """[{"A":"01"},{"A":"02"},{"A":"03"},{"A":"04"},{"A":"05"},{"A":"06"},{"A":"07"},{"A":"08"},{"A":"09"}]"""

      withTempBinFile("rec_len1", ".dat", data1.getBytes) { tmpFileName1 =>
        val df = getDataFrame(Seq(tmpFileName1))

        val actual = df
          .orderBy(col("A"))
          .toJSON
          .collect()
          .mkString("[", ",", "]")

        assert(df.count() == 9)
        assert(actual == expected)
      }
    }

    "load all available files using legacy paths option" in {
      withTempBinFile("rec_len1", ".dat", data1.getBytes) { tmpFileName1 =>
        withTempBinFile("rec_len2", ".dat", data2.getBytes) { tmpFileName2 =>
          val df = getDataFrame(Seq(tmpFileName1, tmpFileName2), optionToUse = "paths")

          val actual = df
            .orderBy(col("A"))
            .toJSON
            .collect()
            .mkString("[", ",", "]")

          assert(df.count() == 18)
          assert(actual == expected)
        }
      }
    }

    "file offsets supported when multiple multi-partition files are read" in {
      val expected = """[ {
                       |  "A" : "10"
                       |}, {
                       |  "A" : "11"
                       |}, {
                       |  "A" : "12"
                       |}, {
                       |  "A" : "13"
                       |}, {
                       |  "A" : "14"
                       |}, {
                       |  "A" : "15"
                       |}, {
                       |  "A" : "16"
                       |}, {
                       |  "A" : "17"
                       |}, {
                       |  "A" : "18"
                       |}, {
                       |  "A" : "19"
                       |}, {
                       |  "A" : "20"
                       |}, {
                       |  "A" : "21"
                       |}, {
                       |  "A" : "22"
                       |}, {
                       |  "A" : "23"
                       |}, {
                       |  "A" : "24"
                       |}, {
                       |  "A" : "25"
                       |}, {
                       |  "A" : "26"
                       |}, {
                       |  "A" : "27"
                       |}, {
                       |  "A" : "28"
                       |}, {
                       |  "A" : "29"
                       |}, {
                       |  "A" : "30"
                       |}, {
                       |  "A" : "31"
                       |}, {
                       |  "A" : "32"
                       |}, {
                       |  "A" : "33"
                       |}, {
                       |  "A" : "34"
                       |}, {
                       |  "A" : "35"
                       |}, {
                       |  "A" : "36"
                       |}, {
                       |  "A" : "37"
                       |}, {
                       |  "A" : "38"
                       |}, {
                       |  "A" : "39"
                       |}, {
                       |  "A" : "BB"
                       |}, {
                       |  "A" : "FF"
                       |} ]""".stripMargin.replaceAll("\r", "")

      val data1 = "AA10111213141516171819" + "B" * 960 + "20212223242526272829CC"
      val data2 = "EE30313233343536373839" + "F" * 960 + "30313233343536373839GG"

      withTempDirectory("rec_len_multi") { tempDir =>
        val parentDirOpt = Option(new File(tempDir))
        createTempTextFile(parentDirOpt, "file1", ".txt", StandardCharsets.UTF_8, data1)
        createTempTextFile(parentDirOpt, "file2", ".txt", StandardCharsets.UTF_8, data2)

        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ascii")
          .option("input_split_records", 10)
          .option("file_start_offset", "2")
          .option("file_end_offset", "2")
          .load(tempDir)


        val count = df.count()

        val actual = SparkUtils.prettyJSON(df
          .distinct()
          .orderBy(col("A"))
          .toJSON
          .collect()
          .mkString("[", ",", "]"))

        assert(count == 1000)
        assert(actual == expected)
      }
    }
  }

  private def getDataFrame(inputPaths: Seq[String],
                           extraOptions: Map[String, String] = Map.empty[String, String],
                           optionToUse: String = "data_paths"): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("encoding", "ascii")
      .option("schema_retention_policy", "collapse_root")
      .option(optionToUse, inputPaths.mkString(","))
      .options(extraOptions)
      .load()
  }
}
