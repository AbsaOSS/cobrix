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
class Test19DisplayNumParsingSpec extends WordSpec with SparkTestBase with CobolTestBase {

  "A copybook and a binary file contains special character in pathname" when {
    val dataPath = "test19_display_num"
    val expectedPath = "test19_display_num_expected"
    val inputCopybookPath = "file://../data/test19_display_num.cob"
    val inputCopybookFSPath = "../data/test19_display_num.cob"
    val inputDataPath = s"../data/$dataPath/data.dat"


    "read normally" should {
      val expectedLayoutPath = s"../data/$expectedPath/test19_layout.txt"
      val actualLayoutPath = s"../data/$expectedPath/test19_layout_actual.txt"
      val expectedSchemaPath = s"../data/$expectedPath/test19_schema.json"
      val actualSchemaPath = s"../data/$expectedPath/test19_schema_actual.json"
      val expectedResultsPath = s"../data/$expectedPath/test19.txt"
      val actualResultsPath = s"../data/$expectedPath/test19_actual.txt"

        "return a data frame" in {
        // Comparing layout
        val copybookContents = readCopybook(inputCopybookFSPath)

        //testLaoyout(copybookContents, actualLayoutPath, expectedLayoutPath)

        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("pedantic", "true")
          .option("generate_record_id", "true")
          .option("schema_retention_policy", "collapse_root")
          .load(inputDataPath)

        //testSchema(df, actualSchemaPath, expectedSchemaPath)

        val actualDf = df
          .orderBy("File_Id", "Record_Id")
          .toJSON
          .take(300)

        testData(actualDf, actualResultsPath, expectedResultsPath)
      }
    }

  }

}
