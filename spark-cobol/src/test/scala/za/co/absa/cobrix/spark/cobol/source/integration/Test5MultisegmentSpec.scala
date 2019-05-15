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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParserFactory
import za.co.absa.cobrix.spark.cobol.reader.index.IndexGenerator
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

import scala.collection.JavaConversions._

//noinspection NameBooleanParameters
class Test5MultisegmentSpec extends FunSuite with SparkTestBase {

  private val exampleName = "Test5(multisegment,ebcdic)"
  private val inputCopybookPath = "file://../data/test5_copybook.cob"
  private val inputDataPathLittleEndian = "../data/test5_data"
  private val inputDataPathBigEndian = "../data/test5b_data"

  test(s"Integration test on $exampleName - segment ids, ebcdic") {
    import spark.implicits._

    val expectedSchemaPath = "../data/test5_expected/test5_schema.json"
    val actualSchemaPath = "../data/test5_expected/test5_schema_actual.json"
    val expectedResultsPath = "../data/test5_expected/test5.txt"
    val actualResultsPath = "../data/test5_expected/test5_actual.txt"

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
      .load(inputDataPathLittleEndian)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

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

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))
  }

  test(s"Integration test on $exampleName - root segment id") {
    import spark.implicits._

    // In this test we check that if only one segment id is specified the root segment ids should
    // still be correctly generated.

    val expectedSchemaPath = "../data/test5_expected/test5a_schema.json"
    val actualSchemaPath = "../data/test5_expected/test5a_schema_actual.json"
    val expectedResultsPath = "../data/test5_expected/test5a.txt"
    val actualResultsPath = "../data/test5_expected/test5a_actual.txt"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_record_sequence", "true")
      .option("input_split_records", "100")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_root", "C")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .option("segment_id_prefix", "B")
      .load(inputDataPathLittleEndian)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    //df.show(200)
    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    FileUtils.writeStringsToFile(actualDf, actualResultsPath)

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))
  }

  test(s"Integration test on $exampleName - auto resolve segment redefines") {
    import spark.implicits._

    val expectedSchemaPath = "../data/test5_expected/test5c_schema.json"
    val actualSchemaPath = "../data/test5_expected/test5c_schema_actual.json"
    val expectedResultsPath = "../data/test5_expected/test5c.txt"
    val actualResultsPath = "../data/test5_expected/test5c_actual.txt"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_record_sequence", "true")
      .option("input_split_records", "100")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_root", "C")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .option("segment_id_prefix", "B")
      .option("redefine_segment_id_map:0", "STATIC-DETAILS => C,D")
      .option("redefine-segment-id-map:1", "CONTACTS => P")

      .load(inputDataPathLittleEndian)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    //df.show(200)
    val actualDf = df
      .orderBy("File_Id", "Record_Id")
      .toJSON
      .take(60)

    FileUtils.writeStringsToFile(actualDf, actualResultsPath)

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))
  }

  test(s"Index generator test on $exampleName - root segment id") {
    val copybookContents = Files.readAllLines(Paths.get("../data/test5_copybook.cob"), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val copybook = CopybookParser.parseTree(copybookContents)
    val segmentIdField = copybook.getFieldByName("SEGMENT_ID").asInstanceOf[Primitive]
    val segmentIdRootValue = "C"

    val stream = new FileStreamer("../data/test5_data/COMP.DETAILS.SEP30.DATA.dat", FileSystem.get(new Configuration()))

    val recordHeaderParser = RecordHeaderParserFactory.createRecordHeaderParser(Constants.RhRdwLittleEndian)
    val indexes = IndexGenerator.sparseIndexGenerator(0, stream, isRdwBigEndian = false,
      recordHeaderParser = recordHeaderParser, recordsPerIndexEntry = Some(10),  sizePerIndexEntryMB = None,
      copybook = Some(copybook), segmentField = Some(segmentIdField), rootSegmentId = segmentIdRootValue)
    assert(indexes.length == 88)
  }

  test(s"Integration test on $exampleName - big endian RDW") {
    import spark.implicits._

    val expectedSchemaPath = "../data/test5_expected/test5b_schema.json"
    val actualSchemaPath = "../data/test5_expected/test5b_schema_actual.json"
    val expectedResultsPath = "../data/test5_expected/test5b.txt"
    val actualResultsPath = "../data/test5_expected/test5b_actual.txt"

    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("is_record_sequence", "true")
      .option("is_rdw_big_endian", "true")
      .option("segment_field", "SEGMENT_ID")
      .option("segment_id_level0", "C")
      .option("segment_id_level1", "P")
      .option("generate_record_id", "true")
      .option("schema_retention_policy", "collapse_root")
      .option("segment_id_prefix", "A")
      .load(inputDataPathBigEndian)

    // This is to print the actual output
    //println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

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

    // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
    val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

    if (!actual.sameElements(expected)) {
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
    Files.delete(Paths.get(actualResultsPath))

  }


}
