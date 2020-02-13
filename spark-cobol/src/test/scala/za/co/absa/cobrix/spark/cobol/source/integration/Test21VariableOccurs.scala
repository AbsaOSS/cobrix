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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

class Test21VariableOccurs extends FunSuite with SparkTestBase {

  private val exampleName = "Test21"
  private val inputCopybookPath = "file://../data/test21_copybook.cob"
  private val inputDataPath = "../data/test21_data"

  private val expectedResultsPath = "../data/test21_expected/test21.txt"
  private val actualResultsPath = "../data/test21_expected/test21_actual.txt"
  private val expectedSchemaPath = "../data/test21_expected/test21_schema.json"
  private val actualSchemaPath = "../data/test21_expected/test21_schema_actual.json"

  test(s"Integration test on $exampleName data") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("encoding", "ascii")
      .load(inputDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

//    if (!actual.sameElements(expected)) {
//      FileUtils.writeStringsToFile(actual, actualResultsPath)
//      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
//    }
  }

}
