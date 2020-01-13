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

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

//noinspection NameBooleanParameters
class Test9CodePages extends FunSuite with SparkTestBase {

  private val exampleName = "Test9"
  private val inputCopybookPath = "file://../data/test9_copybook.cob"
  private val inputDataPath = "../data/test9_data"

  private val expectedSchemaPath = "../data/test9_expected/test9_schema.json"
  private val actualSchemaPath = "../data/test9_expected/test9_schema_actual.json"

  def runTest(expectPrefix: String, options: Seq[(String, String)]): Unit = {
    val expectedResultsPath = s"../data/test9_expected/test9$expectPrefix.txt"
    val actualResultsPath = s"../data/test9_expected/test9${expectPrefix}_actual.txt"

    val df = {
      val loadDf = spark
        .read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("schema_retention_policy", "collapse_root")

      val loadDfWithOptions = options.foldLeft(loadDf)((a, b) => {
        a.option(b._1, b._2)
      })

      loadDfWithOptions.load(inputDataPath)
    }

    val expectedSchema = FileUtils.readAllFileStringUtf8(expectedSchemaPath)
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToUtf8File(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = FileUtils.readAllFileLinesUtf8(expectedResultsPath)

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToUtf8File(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }

  test(s"Integration test on $exampleName code page CP037") {
    runTest("_cp037",
      "ebcdic_code_page" -> "cp037" :: Nil)
  }

  test(s"Integration test on $exampleName code page CP037 extended") {
    runTest("_cp037_ext",
      "ebcdic_code_page" -> "cp037_extended" ::
        "string_trimming_policy" -> "none" :: Nil)
  }

  test(s"Integration test on $exampleName custom code page") {
    runTest("_cp_custom",
      "ebcdic_code_page_class" -> "za.co.absa.cobrix.spark.cobol.source.utils.CustomCodePage" ::
      "string_trimming_policy" -> "none" :: Nil)
  }

}
