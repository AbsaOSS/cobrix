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
import org.scalatest.WordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

//noinspection NameBooleanParameters
class Test28MultipartLoadSpec extends WordSpec with SparkTestBase with BinaryFileFixture {

  private val exampleName = "Test26 (custom record extractor)"

  private val copybook =
    """      01  R.
                03 A        PIC X(2).
                03 B        PIC X(1).
      """
  private val data1 = "AABBBCCDDDEEFFFZYY"
  private val data2 = "BAABBBCCDDDEEFFFZY"

  "Multipart path spec" should {
    "load avv available copybooks" in {
      val expected = """"""

      withTempBinFile("rec_len1", ".dat", data1.getBytes) { tmpFileName1 =>
        withTempBinFile("rec_len2", ".dat", data2.getBytes) { tmpFileName2 =>


          intercept[IllegalStateException] {
            val df = getDataFrame(Seq(tmpFileName1, tmpFileName1))

            val actual = df.toJSON.collect().mkString("[", ",", "]")
          }

          //assert(df.count() == 12)
          //assert(actual == expected)
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
      .option("record_length", "2")
      .option("schema_retention_policy", "collapse_root")
      .options(extraOptions)
      .load(inputPaths: _*)
  }


}
