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
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

import scala.collection.JavaConverters._

//noinspection NameBooleanParameters
class Test14RdwAdjustmentsSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test4(multisegment,ascii)"
  private val inputCopybookPath = "file://../data/test14_copybook.cob"
  private val inpudDataPath = "../data/test14_data"
  private val expectedSchemaPath = "../data/test14_expected/test14_schema.json"
  private val actualSchemaPath = "../data/test14_expected/test14_schema_actual.json"
  private val expectedResultsPath = "../data/test14_expected/test14.txt"
  private val actualResultsPath = "../data/test14_expected/test14_actual.txt"

  test(s"Integration test on $exampleName - RDWs counts headers in record length") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_record_sequence", "true")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_level0", "C")
      .option("segment_id_level1", "P")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .option("segment_id_prefix", "A")
      .option("redefine_segment_id_map:0", "STATIC-DETAILS => C,D")
      .option("redefine-segment-id-map:1", "CONTACTS => P")
      .option("is_rdw_part_of_record_length", "true")
      .load(inpudDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    FileUtils.writeStringsToFile(actualDf, actualResultsPath)

    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))
  }

  test(s"Integration test on $exampleName - RDWs with adjustments") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_record_sequence", "true")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_level0", "C")
      .option("segment_id_level1", "P")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .option("segment_id_prefix", "A")
      .option("redefine_segment_id_map:0", "STATIC-DETAILS => C,D")
      .option("redefine-segment-id-map:1", "CONTACTS => P")
      .option("rdw_adjustment", "-4")
      .load(inpudDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = SparkUtils.prettyJSON(df.schema.json)

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    FileUtils.writeStringsToFile(actualDf, actualResultsPath)

    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).asScala.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))
  }
}
