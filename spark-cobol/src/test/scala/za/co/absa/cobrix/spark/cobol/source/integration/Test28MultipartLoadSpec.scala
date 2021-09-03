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
import org.scalatest.WordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test28MultipartLoadSpec extends WordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test28 (multipart load)"

  private val copybook =
    """      01  R.
                03 A        PIC X(2).
      """
  private val data1 = "010203040506070809"
  private val data2 = "101112131415161718"

  "Multipart path spec" should {
    "load avv available copybooks" in {
      val expected = """[{"A":"01"},{"A":"02"},{"A":"03"},{"A":"04"},{"A":"05"},{"A":"06"},{"A":"07"},{"A":"08"},{"A":"09"},{"A":"10"},{"A":"11"},{"A":"12"},{"A":"13"},{"A":"14"},{"A":"15"},{"A":"16"},{"A":"17"},{"A":"18"}]"""

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
  }

  private def getDataFrame(inputPaths: Seq[String], extraOptions: Map[String, String] = Map.empty[String, String]): DataFrame = {
    spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("encoding", "ascii")
      .option("schema_retention_policy", "collapse_root")
      .option("paths", inputPaths.mkString(","))
      .options(extraOptions)
      .load()
  }


}
