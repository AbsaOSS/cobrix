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
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.ASCII
import za.co.absa.cobrix.cobol.reader.extractors.raw.{RawRecordContext, VarOccursRecordExtractor}
import za.co.absa.cobrix.cobol.reader.stream.FSStream
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

import scala.collection.mutable.ListBuffer


class Test25OccursMappings extends AnyFunSuite with SparkTestBase {

  private val exampleName = "Test25"
  private val inputCopybookPath = "file://../data/test25_copybook.cob"
  private val inputDataPath = "../data/test25_data"

  private val expectedResultsPath = "../data/test25_expected/test25.txt"
  private val actualResultsPath = "../data/test25_expected/test25_actual.txt"
  private val expectedSchemaPath = "../data/test25_expected/test25_schema.json"
  private val actualSchemaPath = "../data/test25_expected/test25_schema_actual.json"

  test("Test fail without Occurs Mappings") {
    val inputStream = new FSStream(s"$inputDataPath/data.dat")
    val copybookContents = Files.readAllLines(Paths.get("../data/test25_copybook.cob"), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    val exc = intercept[IllegalStateException] {
      CopybookParser.parseTree(copybookContents)
    }
    assert(exc.getMessage.contains("Field FIELD is a DEPENDING ON field of an OCCURS, should be integral"))
  }

  test("Test Occurs Mappings on VarOccursRecordExtractor") {
    val inputStream = new FSStream(s"$inputDataPath/data.dat")
    val copybookContents = Files.readAllLines(Paths.get("../data/test25_copybook.cob"), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    val occursMapping: Map[String, Map[String, Int]] = Map(
      "DETAIL1" -> Map(
        "A" -> 0,
        "B" -> 1
      ),
      "DETAIL2" -> Map(
        "A" -> 1,
        "B" -> 2
      )
    )
    val copybook = CopybookParser.parse(copybookContents, ASCII, occursHandlers = occursMapping)

    val recordExtractor = new VarOccursRecordExtractor(RawRecordContext(0L, inputStream, copybook, null, null, ""))

    val expectedRecords = ListBuffer(
      "1AX".getBytes,
      "2BXYZ".getBytes
    )

    val records = new ListBuffer[Array[Byte]]
    while (recordExtractor.hasNext) {
      records += recordExtractor.next
    }

    assert(records.length == 2)
    val sameData = records.zip(expectedRecords).forall{ case (a1, a2) => a1.sameElements(a2)}
    assert(sameData)
  }

  test("Integration test fail on bad occurs mappings") {
    val exc = intercept[ClassCastException] {
      spark
        .read
        .format("cobol")
        .option("copybook", inputCopybookPath)
        .option("encoding", "ascii")
        .option("occurs_mappings", "{\"DETAIL1\":{\"A\":0,\"B\":1.5},\"DETAIL2\":{\"A\":1,\"B\":\"Z\"}}")
        .option("variable_size_occurs", "true")
        .option("schema_retention_policy", "keep_original")
        .load(inputDataPath + "/data.dat")
    }
    assert(exc.getMessage.contains("java.lang.Double cannot be cast to java.lang.Integer"))
  }

  test(s"Integration test on $exampleName data for variable occurs") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("encoding", "ascii")
      .option("occurs_mappings", "{\"DETAIL1\":{\"A\":0,\"B\":1},\"DETAIL2\":{\"A\":1,\"B\":2}}")
      .option("variable_size_occurs", "true")
      .option("schema_retention_policy", "keep_original")
      .load(inputDataPath + "/data.dat")

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)
      .replace("\"maxElements\" : 2", "[redacted]") // different versions of Spark sort metadata columns differently
      .replace("\"minElements\" : 0", "[redacted]")


    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }

  test(s"Integration test on $exampleName data without variable occurs") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("encoding", "ascii")
      .option("occurs_mappings", "{\"DETAIL1\":{\"A\":0,\"B\":1},\"DETAIL2\":{\"A\":1,\"B\":2}}")
      .option("schema_retention_policy", "keep_original")
      .load(inputDataPath + "/data2.dat")

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)
      .replace("\"maxElements\" : 2", "[redacted]") // different versions of Spark sort metadata columns differently
      .replace("\"minElements\" : 0", "[redacted]")

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }
}
