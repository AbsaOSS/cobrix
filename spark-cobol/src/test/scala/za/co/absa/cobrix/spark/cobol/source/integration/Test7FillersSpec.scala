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
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

import scala.collection.JavaConverters._

//noinspection NameBooleanParameters
class Test7FillersSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test7 (fillers)"
  private val inputCopybookPath = "file://../data/test7_fillers.cob"
  private val inputCopybookFSPath = "../data/test7_fillers.cob"
  private val inpudDataPath = "../data/test7_data"

  test(s"Integration test on $exampleName - drop group fillers handling") {
    runTest("test7", dropGroupFillers = true)  }

  test(s"Integration test on $exampleName - retain group fillers handling") {
    runTest("test7a", dropGroupFillers = false)
  }

  private def runTest(namePrefix: String, dropGroupFillers: Boolean): Unit = {

    val expectedSchemaPath = s"../data/test7_expected/${namePrefix}_schema.json"
    val expectedLayoutPath = s"../data/test7_expected/${namePrefix}_layout.txt"
    val actualSchemaPath = s"../data/test7_expected/${namePrefix}_schema_actual.json"
    val actualLayoutPath = s"../data/test7_expected/${namePrefix}_layout_actual.txt"
    val expectedResultsPath = s"../data/test7_expected/$namePrefix.txt"
    val actualResultsPath = s"../data/test7_expected/${namePrefix}_actual.txt"

    // Comparing layout
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookFSPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val cobolSchema = CopybookParser.parseTree(copybookContents, dropGroupFillers)
    val actualLayout = cobolSchema.generateRecordLayoutPositions()
    val expectedLayout = Files.readAllLines(Paths.get(expectedLayoutPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    if (actualLayout != expectedLayout) {
      FileUtils.writeStringToFile(actualLayout, actualLayoutPath)
      assert(false, s"The actual layout doesn't match what is expected for $exampleName example. Please compare contents of $expectedLayoutPath to " +
        s"$actualLayoutPath for details.")
    }

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .option("drop_group_fillers", dropGroupFillers)
      .load(inpudDataPath)

    df.printSchema()

    // This is to print the actual output
    println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    // Fill nulls with zeros so by lokking at json you can tell a field is missing. Otherwise json won't contain null fields.
    val actualDf =  SparkUtils.convertDataFrameToPrettyJSON(df.orderBy("AMOUNT"), 100)
    FileUtils.writeStringToFile(actualDf, actualResultsPath)
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))
  }

}
