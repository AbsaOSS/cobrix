/*
 * Copyright 2018-2019 ABSA Group Limited
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
import org.apache.spark.sql.functions._
import org.scalatest.WordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase

//noinspection NameBooleanParameters
class Test20InputFileNameSpec extends WordSpec with SparkTestBase {

  "input_file_name()" when {
    "a fixed-record length file is used" should {
      val inputCopybookPath = "file://../data/test1_copybook.cob"
      val inputDataPath = "../data/test1_data"

      "return the input file name if a file is specified" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("schema_retention_policy", "collapse_root")
          .load(inputDataPath)

        val fileName = getInputFileName(df)

        assert(fileName == "example.bin")
      }

      "return the input file name if a directory with a single file is specified" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("schema_retention_policy", "collapse_root")
          .load(inputDataPath + "/example.bin")

        val fileName = getInputFileName(df)

        assert(fileName == "example.bin")
      }
    }

    "a fixed-record length directory is used" should {
      val inputCopybookPath = "file://../data/test1_copybook.cob"
      val inputDataPath = "../data/test2_data"

      "return file name corresponding to each record" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("schema_retention_policy", "collapse_root")
          .load(inputDataPath)

        val fileName1 = getInputFileName(df.filter(col("ID") === 1))
        val fileName2 = getInputFileName(df.filter(col("ID") === 20))

        assert(fileName1 == "example.bin")
        assert(fileName2 == "example2.bin")
      }

      "throw an exception if 'with_input_file_name_col' is used" in {
        val ex = intercept[IllegalArgumentException] {
          spark
            .read
            .format("cobol")
            .option("copybook", inputCopybookPath)
            .option("with_input_file_name_col", "file_name")
            .load(inputDataPath)
        }

        assert(ex.getMessage.contains("'with_input_file_name_col' is supported only when 'is_record_sequence' = true"))
      }
    }

    "a variable-record length file is used" should {
      val inputCopybookPath = "file://../data/test4_copybook.cob"
      val inputDataPath = "../data/test4_data"

      "return the input file name if a file is specified" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("is_record_sequence", "true")
          .option("with_input_file_name_col", "file_name")
          .load(inputDataPath + "/COMP.DETAILS.SEP30.DATA.dat")

        val fileName = getInputFileName(df)

        // ToDo #221
        //assert(fileName == "COMP.DETAILS.SEP30.DATA.dat")
      }

      "return the input file name if a directory with one file is specified" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("is_record_sequence", "true")
          .option("with_input_file_name_col", "file_name")
          .load(inputDataPath)

        val fileName = getInputFileName(df)

        // ToDo #221
        //assert(fileName == "COMP.DETAILS.SEP30.DATA.dat")
      }
    }

  }

  private def getInputFileName(df: DataFrame): String = {
    val fileFullPath = df.select(input_file_name().as("F"))
      .take(1)(0)(0)
      .toString
    extractFileName(fileFullPath)
  }

  private def extractFileName(pathName: String): String = pathName.split("[\\/\\\\]").last

}
