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

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, DataFrameReader}
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

        assert(ex.getMessage.contains("'with_input_file_name_col' is supported only when one of this holds"))
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
          .option("encoding", "ascii")
          .option("with_input_file_name_col", "F")
          .load(inputDataPath + "/COMP.DETAILS.SEP30.DATA.dat")

        assert(df.schema.fields.head.name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "COMP.DETAILS.SEP30.DATA.dat")
      }

      "return the input file name if a file is specified (when collapse root is used)" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("is_record_sequence", "true")
          .option("schema_retention_policy", "collapse_root")
          .option("encoding", "ascii")
          .option("with_input_file_name_col", "F")
          .load(inputDataPath + "/COMP.DETAILS.SEP30.DATA.dat")

        assert(df.schema.fields.head.name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "COMP.DETAILS.SEP30.DATA.dat")
      }

      "return the input file name if a directory with one file is specified" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("is_record_sequence", "true")
          .option("encoding", "ascii")
          .option("with_input_file_name_col", "F")
          .option("generate_record_id", "true")
          .load(inputDataPath)

        assert(df.schema.fields(2).name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "COMP.DETAILS.SEP30.DATA.dat")
      }

      "return the input file name if a directory with one file is specified (when collapse root is used)" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("is_record_sequence", "true")
          .option("schema_retention_policy", "collapse_root")
          .option("encoding", "ascii")
          .option("with_input_file_name_col", "F")
          .option("generate_record_id", "true")
          .load(inputDataPath)

        assert(df.schema.fields(2).name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "COMP.DETAILS.SEP30.DATA.dat")
      }
    }

    "a variable-record length file is used" should {
      val inputCopybookPath = "file://../data/test17_hierarchical.cob"
      val inputDataPath = "../data/test17/HIERARCHICAL.DATA.RDW.dat"

      def getHierarchicalDf: DataFrameReader = {
        spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("pedantic", "true")
          .option("is_record_sequence", "true")
          .option("segment_field", "SEGMENT_ID")
          .option("redefine_segment_id_map:1", "COMPANY => 1")
          .option("redefine-segment-id-map:2", "DEPT => 2")
          .option("redefine-segment-id-map:3", "EMPLOYEE => 3")
          .option("redefine-segment-id-map:4", "OFFICE => 4")
          .option("redefine-segment-id-map:5", "CUSTOMER => 5")
          .option("redefine-segment-id-map:6", "CONTACT => 6")
          .option("redefine-segment-id-map:7", "CONTRACT => 7")
          .option("segment-children:1", "COMPANY => DEPT,CUSTOMER")
          .option("segment-children:2", "DEPT => EMPLOYEE,OFFICE")
          .option("segment-children:3", "CUSTOMER => CONTACT,CONTRACT")
          .option("input_split_records", 5)
          .option("with_input_file_name_col", "F")
      }

      "return a hierarchical dataframe for a hierarchical file" in {
        val df = getDataFrameReader(getHierarchicalDf, false, false)
          .load(inputDataPath)

        assert(df.schema.head.name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "HIERARCHICAL.DATA.RDW.dat")
      }

      "return a hierarchical dataframe for a hierarchical file (+collapseRoot)" in {
        val df = getDataFrameReader(getHierarchicalDf, false, true)
          .load(inputDataPath)

        assert(df.schema.head.name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "HIERARCHICAL.DATA.RDW.dat")
      }

      "return a hierarchical dataframe for a hierarchical file (+genId)" in {
        val df = getDataFrameReader(getHierarchicalDf, true, false)
          .load(inputDataPath)

        assert(df.schema.fields(2).name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "HIERARCHICAL.DATA.RDW.dat")
      }

      "return a hierarchical dataframe for a hierarchical file (+genId +collapseRoot)" in {
        val df = getDataFrameReader(getHierarchicalDf, true, true)
          .load(inputDataPath)

        assert(df.schema.fields(2).name == "F")

        val fileName = getInputFileNameVarLen(df)

        assert(fileName == "HIERARCHICAL.DATA.RDW.dat")
      }
    }
  }

  private def getDataFrameReader(dfr: DataFrameReader, generateRecordIds: Boolean, collapseRoot: Boolean): DataFrameReader = {
    (generateRecordIds, collapseRoot) match {
      case (false, false) => dfr
      case (false, true)  => dfr.option("schema_retention_policy", "collapse_root")
      case (true, false)  => dfr.option("generate_record_id", "true")
      case (true, true)   => dfr.option("generate_record_id", "true").option("schema_retention_policy", "collapse_root")
    }
  }

  private def getInputFileName(df: DataFrame): String = {
    val fileFullPath = df.select(input_file_name().as("F"))
      .take(1)(0)(0)
      .toString
    extractFileName(fileFullPath)
  }

  private def getInputFileNameVarLen(df: DataFrame): String = {
    val fileFullPath = df.select(col("F"))
      .take(1)(0)(0)
      .toString
    extractFileName(fileFullPath)
  }

  private def extractFileName(pathName: String): String = pathName.split("[\\/\\\\]").last

}
