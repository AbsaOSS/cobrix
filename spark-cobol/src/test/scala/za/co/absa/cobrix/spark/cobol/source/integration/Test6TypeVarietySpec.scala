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
class Test6TypeVarietySpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test6(type variety)"
  private val inputCopybookPath = "file://../data/test6_copybook.cob"
  private val inputCopybookFSPath = "../data/test6_copybook.cob"
  private val inpudDataPath = "../data/test6_data"

  test(s"Integration test on $exampleName - segment ids") {

    val expectedSchemaPath = "../data/test6_expected/test6_schema.json"
    val expectedLayoutPath = "../data/test6_expected/test6_layout.txt"
    val actualSchemaPath = "../data/test6_expected/test6_schema_actual.json"
    val actualLayoutPath = "../data/test6_expected/test6_layout_actual.txt"
    //val expectedResultsPath = "../data/test6_expected/test6.csv"
    val expectedResultsPath = "../data/test6_expected/test6.txt"
    //val actualResultsPath1 = "../data/test6_expected/test6"
    val actualResultsPath = "../data/test6_expected/test6_actual.txt"
    //val actualResultsPathCrc = "../data/test6_expected/.test6_actual.csv.crc"

    // Comparing layout
    val copybookContents = Files.readAllLines(Paths.get(inputCopybookFSPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val cobolSchema = CopybookParser.parseTree(copybookContents)
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
      .option("floating_point_format", "IEEE754")
      .load(inpudDataPath)

    df.printSchema()

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    // Uncomment this to compare CSV instead of json
    /*
    df
      //.repartition(1)
      //.orderBy("ID")
      .write.format("csv")
      .option("header", "true")
      .mode(SaveMode.Overwrite).save(actualResultsPath1)

    merge(actualResultsPath1, actualResultsPath)
    Files.delete(Paths.get(actualResultsPathCrc))
    */

    // Fill nulls with zeros so by lokking at json you can tell a field is missing. Otherwise json won't contain null fields.
    val actualDf = df.orderBy("ID").na.fill(0).toJSON.take(100)
    FileUtils.writeStringsToFile(actualDf, actualResultsPath)
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
