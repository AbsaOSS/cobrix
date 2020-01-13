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

import org.scalatest.WordSpec
import za.co.absa.cobrix.spark.cobol.source.base.{CobolTestBase, SparkTestBase}

//noinspection NameBooleanParameters
class Test18PathSpecialCharSpec extends WordSpec with SparkTestBase with CobolTestBase {

  "A copybook and a binary file contains special character in pathname" when {
    val dataPath = "test18 special_char"
    val expectedPath = "test18 special_char_expected"
    val inputCopybookPath = "file://../data/test18 special_char.cob"
    val inputCopybookFSPath = "../data/test18 special_char.cob"
    val inputDataPath = s"../data/$dataPath/HIERARCHICAL.DATA.RDW.dat"


    "read normally" should {
      val expectedLayoutPath = s"../data/$expectedPath/test18a_layout.txt"
      val actualLayoutPath = s"../data/$expectedPath/test18a_layout_actual.txt"
      val expectedSchemaPath = s"../data/$expectedPath/test18a_schema.json"
      val actualSchemaPath = s"../data/$expectedPath/test18a_schema_actual.json"
      val expectedResultsPath = s"../data/$expectedPath/test18a.txt"
      val actualResultsPath = s"../data/$expectedPath/test18a_actual.txt"

        "return a data frame" in {
        // Comparing layout
        val copybookContents = readCopybook(inputCopybookFSPath)

        testLaoyout(copybookContents, actualLayoutPath, expectedLayoutPath)

        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("pedantic", "true")
          .option("is_record_sequence", "true")
          .option("generate_record_id", "true")
          .option("schema_retention_policy", "collapse_root")
          .option("segment_field", "SEGMENT_ID")
          .option("redefine_segment_id_map:1", "COMPANY => 1")
          .option("redefine-segment-id-map:2", "DEPT => 2")
          .option("redefine-segment-id-map:3", "EMPLOYEE => 3")
          .option("redefine-segment-id-map:4", "OFFICE => 4")
          .option("redefine-segment-id-map:5", "CUSTOMER => 5")
          .option("redefine-segment-id-map:6", "CONTACT => 6")
          .option("redefine-segment-id-map:7", "CONTRACT => 7")
          .load(inputDataPath)

        testSchema(df, actualSchemaPath, expectedSchemaPath)

        val actualDf = df
          .orderBy("File_Id", "Record_Id")
          .toJSON
          .take(300)

        testData(actualDf, actualResultsPath, expectedResultsPath)
      }
    }

  }

}
