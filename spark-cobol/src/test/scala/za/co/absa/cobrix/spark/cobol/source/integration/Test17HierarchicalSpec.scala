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

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, SparkUtils}

import scala.collection.JavaConversions._

//noinspection NameBooleanParameters
class Test17HierarchicalSpec extends WordSpec with SparkTestBase {

  "Multisegment hierarchical file" when {
    val exampleName = "Test17 (hierarchical)"
    val inputCopybookPath = "file://../data/test17_hierarchical.cob"
    val inputCopybookFSPath = "../data/test17_hierarchical.cob"
    val inputDataPath = "../data/test17/HIERARCHICAL.DATA.RDW.dat"


    "read as a flat file by specifying segment redefines" should {
      val expectedLayoutPath = "../data/test17_expected/test17a_layout.txt"
      val actualLayoutPath = "../data/test17_expected/test17a_layout_actual.txt"
      val expectedSchemaPath = "../data/test17_expected/test17a_schema.json"
      val actualSchemaPath = "../data/test17_expected/test17a_schema_actual.json"
      val expectedResultsPath = "../data/test17_expected/test17a.txt"
      val actualResultsPath = "../data/test17_expected/test17a_actual.txt"

      "return a flat data frame" in {
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
          .take(300)

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

    "read as a flat file with segment redefines and id generation" should {
      val expectedSchemaPath = "../data/test17_expected/test17b_schema.json"
      val actualSchemaPath = "../data/test17_expected/test17b_schema_actual.json"
      val expectedResultsPath = "../data/test17_expected/test17b.txt"
      val actualResultsPath = "../data/test17_expected/test17b_actual.txt"

      "return a dataframe with ids generated" in {
        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("pedantic", "true")
          .option("is_record_sequence", "true")
          .option("generate_record_id", "true")
          .option("schema_retention_policy", "collapse_root")
          .option("segment_field", "SEGMENT_ID")
          .option("segment_id_level0", "1")
          .option("segment_id_level1", "2,5")
          .option("segment_id_level2", "3,4,6,7")
          .option("segment_id_prefix", "A")
          .option("redefine_segment_id_map:1", "COMPANY => 1")
          .option("redefine-segment-id-map:2", "DEPT => 2")
          .option("redefine-segment-id-map:3", "EMPLOYEE => 3")
          .option("redefine-segment-id-map:4", "OFFICE => 4")
          .option("redefine-segment-id-map:5", "CUSTOMER => 5")
          .option("redefine-segment-id-map:6", "CONTACT => 6")
          .option("redefine-segment-id-map:7", "CONTRACT => 7")

          .load(inputDataPath)

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
          .take(300)

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

    "read as a hierarchical file with parent child relationships defined" should {
      val expectedSchemaPath = "../data/test17_expected/test17c_schema.json"
      val actualSchemaPath = "../data/test17_expected/test17c_schema_actual.json"
      val expectedResultsPath = "../data/test17_expected/test17c.txt"
      val actualResultsPath = "../data/test17_expected/test17c_actual.txt"

      "return a hierarchically structured dataframe" in {
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
          .option("segment-children:1", "COMPANY => DEPT,CUSTOMER")
          .option("segment-children:2", "DEPT => EMPLOYEE,OFFICE")
          .option("segment-children:3", "CUSTOMER => CONTACT,CONTRACT")

          .load(inputDataPath)

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
          .collect

        assert(df.count == 50)

        FileUtils.writeStringsToFile(actualDf, actualResultsPath)

        // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
        val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
        val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

        val firstLineActual = SparkUtils.prettyJSON(actual.head)
        val firstLineExpected = SparkUtils.prettyJSON(expected.head)

        assert(firstLineActual == firstLineExpected)

        if (!actual.sameElements(expected)) {
          assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
            s"$actualResultsPath for details.")
        }
        Files.delete(Paths.get(actualResultsPath))
      }
    }
  }

  "Multisegment single parent-child hierarchy example file" when {
    val exampleName = "Test4(multisegment,ascii)"
    val inputCopybookPath = "file://../data/test4_copybook.cob"
    val inpudDataPath = "../data/test4_data"

    "read as a hierarchical file with parent child relationships defined" should {
      "return a hierarchically structured dataframe with record id generation and collapse root policy" in {
        val expectedSchemaPath = "../data/test17_expected/test17d_schema.json"
        val actualSchemaPath = "../data/test17_expected/test17d_schema_actual.json"
        val expectedResultsPath = "../data/test17_expected/test17d.txt"
        val actualResultsPath = "../data/test17_expected/test17d_actual.txt"

        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("encoding", "ascii")
          .option("is_record_sequence", "true")
          .option("segment_field", "SEGMENT_ID")
          .option("redefine_segment_id_map:1", "STATIC-DETAILS => C")
          .option("redefine-segment-id-map:2", "CONTACTS => P")
          .option("segment-children:1", "STATIC-DETAILS => CONTACTS")
          .option("generate_record_id", "true")
          .option("schema_retention_policy", "collapse_root")
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

        // toList is used to convert the Java list to Scala list. If it is skipped the resulting type will be Array[AnyRef] instead of Array[String]
        val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toList.toArray
        val actual = Files.readAllLines(Paths.get(actualResultsPath), StandardCharsets.ISO_8859_1).toList.toArray

        if (!actual.sameElements(expected)) {
          assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
            s"$actualResultsPath for details.")
        }
        Files.delete(Paths.get(actualResultsPath))
      }

      "return a hierarchically structured dataframe without id generation and default schema retention policy" in {
        val expectedSchemaPath = "../data/test17_expected/test17e_schema.json"
        val actualSchemaPath = "../data/test17_expected/test17e_schema_actual.json"
        val expectedResultsPath = "../data/test17_expected/test17e.txt"
        val actualResultsPath = "../data/test17_expected/test17e_actual.txt"

        val df = spark
          .read
          .format("cobol")
          .option("copybook", inputCopybookPath)
          .option("encoding", "ascii")
          .option("is_record_sequence", "true")
          .option("segment_field", "SEGMENT_ID")
          .option("redefine_segment_id_map:1", "STATIC-DETAILS => C")
          .option("redefine-segment-id-map:2", "CONTACTS => P")
          .option("segment-children:1", "STATIC-DETAILS => CONTACTS")
          .load(inpudDataPath)

        val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
        val actualSchema = SparkUtils.prettyJSON(df.schema.json)

        if (actualSchema != expectedSchema) {
          FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
          assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
            s"$actualSchemaPath for details.")
        }

        val actualDf = df
          .orderBy("COMPANY_DETAILS.COMPANY_ID")
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

      "throw an exception if root segment id is specified (ID fields generation is requested)" in {
        val ex = intercept[IllegalArgumentException] {
          spark
            .read
            .format("cobol")
            .option("copybook", inputCopybookPath)
            .option("encoding", "ascii")
            .option("is_record_sequence", "true")
            .option("segment_field", "SEGMENT_ID")
            .option("redefine_segment_id_map:1", "STATIC-DETAILS => C")
            .option("redefine-segment-id-map:2", "CONTACTS => P")
            .option("segment-children:1", "STATIC-DETAILS => CONTACTS")
            .option("segment_id_root", "1")
            .load(inpudDataPath)
        }

        assert(ex.getMessage.contains("ID fields generation is not supported for hierarchical records reader"))
      }

      "throw an exception if ID fields generation is also requested" in {
        val ex = intercept[IllegalArgumentException] {
          spark
            .read
            .format("cobol")
            .option("copybook", inputCopybookPath)
            .option("encoding", "ascii")
            .option("is_record_sequence", "true")
            .option("segment_field", "SEGMENT_ID")
            .option("redefine_segment_id_map:1", "STATIC-DETAILS => C")
            .option("redefine-segment-id-map:2", "CONTACTS => P")
            .option("segment-children:1", "STATIC-DETAILS => CONTACTS")
            .option("segment_id_level0", "1")
            .load(inpudDataPath)
        }

        assert(ex.getMessage.contains("ID fields generation is not supported for hierarchical records reader"))
      }
    }
  }
}