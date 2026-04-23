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

package za.co.absa.cobrix.spark.cobol.writer

import org.apache.hadoop.fs.Path
import org.apache.spark.sql.SaveMode
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}

class NestedWriterSuite extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {
  private val copybookWithOccurs =
    """      01 RECORD.
      |         05  ID               PIC 9(2).
      |         05  FILLER           PIC 9(1).
      |         05  NUMBERS          PIC 9(2) OCCURS 0 TO 5.
      |         05  PLACE.
      |            10  COUNTRY-CODE  PIC X(2).
      |            10  CITY          PIC X(10).
      |         05  PEOPLE                    OCCURS 0 TO 3.
      |            10 NAME           PIC X(14).
      |            10 FILLER         PIC X(1).
      |            10 PHONE-NUMBER   PIC X(12).
      |""".stripMargin

  private val copybookWithDependingOn =
    """      01 RECORD.
      |         05  ID               PIC 9(2).
      |         05  FILLER           PIC 9(1).
      |         05  CNT1             PIC 9(1).
      |         05  NUMBERS          PIC 9(2)
      |                 OCCURS 0 TO 5 DEPENDING ON CNT1.
      |         05  PLACE.
      |            10  COUNTRY-CODE  PIC X(2).
      |            10  CITY          PIC X(10).
      |         05  cnt-2             PIC 9(1).
      |         05  PEOPLE
      |                 OCCURS 0 TO 3 DEPENDING ON cnt-2.
      |            10 NAME           PIC X(14).
      |            10 FILLER         PIC X(1).
      |            10 PHONE-NUMBER   PIC X(12).
      |""".stripMargin

  "getFieldDefinition" should {
    "support alphanumeric PIC" in {
      val copybookContents =
        """       01  RECORD.
            05  NAME       PIC X(10).
        """

      val copybook = CopybookParser.parse(copybookContents)
      val nameField = copybook.getFieldByName("NAME").asInstanceOf[Primitive]

      val actual = NestedRecordCombiner.getFieldDefinition(nameField)

      assert(actual == "X(10)")
    }

    "support integral with COMP" in {
      val copybookContents =
        """       01  RECORD.
            05  NAME       PIC 9(10) USAGE IS COMP.
        """

      val copybook = CopybookParser.parse(copybookContents)
      val nameField = copybook.getFieldByName("NAME").asInstanceOf[Primitive]

      val actual = NestedRecordCombiner.getFieldDefinition(nameField)

      assert(actual == "9(10) COMP-4")
    }

    "support integral DISPLAY" in {
      val copybookContents =
        """       01  RECORD.
            05  NAME       PIC 9(10).
        """

      val copybook = CopybookParser.parse(copybookContents)
      val nameField = copybook.getFieldByName("NAME").asInstanceOf[Primitive]

      val actual = NestedRecordCombiner.getFieldDefinition(nameField)

      assert(actual == "9(10) USAGE IS DISPLAY")
    }

    "support decimal with COMP" in {
      val copybookContents =
        """       01  RECORD.
            05  NAME       PIC S9(5)V99 USAGE IS COMP.
        """

      val copybook = CopybookParser.parse(copybookContents)
      val nameField = copybook.getFieldByName("NAME").asInstanceOf[Primitive]

      val actual = NestedRecordCombiner.getFieldDefinition(nameField)

      assert(actual == "S9(5)V99 COMP-4")
    }

    "support decimal DISPLAY" in {
      val copybookContents =
        """       01  RECORD.
            05  NAME       PIC S9(5)V99 USAGE IS DISPLAY.
        """

      val copybook = CopybookParser.parse(copybookContents)
      val nameField = copybook.getFieldByName("NAME").asInstanceOf[Primitive]

      val actual = NestedRecordCombiner.getFieldDefinition(nameField)

      assert(actual == "S9(5)V99 USAGE IS DISPLAY")
    }
  }

  "writer" should {
    "write the dataframe with OCCURS" in {
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}""",
        """{"ID":2,"NUMBERS":[],"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer1") { tempDir =>
        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithOccurs)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("is_rdw_part_of_record_length", "false")
          .save(path.toString)

        //        val df2 = spark.read.format("cobol")
        //          .option("copybook_contents", copybookWithOccurs)
        //          .option("record_format", "V")
        //          .option("is_rdw_big_endian", "true")
        //          .option("is_rdw_part_of_record_length", "false")
        //          .load(path.toString)
        //
        //        println(SparkUtils.convertDataFrameToPrettyJSON(df2))

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data
        val expected = Array(
          0x00, 0x6A, 0x00, 0x00, // RDW record 0
          0xF0, 0xF1, 0x00, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0x00, 0x00, 0x00, 0x00, 0xE4, 0xE2, 0xD5, 0x85,
          0xA6, 0x40, 0xE8, 0x96, 0x99, 0x92, 0x40, 0x40, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40,
          0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF4, 0x40, 0x40, 0x40, 0x40,
          0xD1, 0x81, 0x95, 0x85, 0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5,
          0xCA, 0xF5, 0xF6, 0xF7, 0xF8, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x6A, 0x00, 0x00, // RDW record 1
          0xF0, 0xF2, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xE9, 0xC1, 0xC3, 0x81,
          0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xE3, 0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99,
          0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS without strict schema check" in {
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe"},{"NAME": "Jane Smith"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer1") { tempDir =>
        val path = new Path(tempDir, "writer1")

        val ex = intercept[IllegalArgumentException] {
          df.write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookWithOccurs)
            .save(path.toString)
        }

        assert(ex.getMessage == "Field 'PEOPLE.PHONE_NUMBER' is not found in Spark schema.")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithOccurs)
          .option("record_format", "F")
          .option("strict_schema", "false")
          .option("variable_size_occurs", "true")
          .save(path.toString)

        //        val df2 = spark.read.format("cobol")
        //          .option("copybook_contents", copybookWithOccurs)
        //          .option("variable_size_occurs", "true")
        //          .load(path.toString)
        //
        //        println(SparkUtils.convertDataFrameToPrettyJSON(df2))

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data
        val expected = Array(
          0xF0, 0xF1, 0x00, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0xE4, 0xE2, 0xD5, 0x85, 0xA6, 0x40, 0xE8, 0x96,
          0x99, 0x92, 0x40, 0x40, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40, 0x40, 0x40, 0x40, 0x40,
          0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xD1, 0x81, 0x95,
          0x85, 0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS DEPENDING ON" in {
      //val parsedCopybook = CopybookParser.parse(copybookWithDependingOn)
      //println(parsedCopybook.generateRecordLayoutPositions())

      val exampleJsons = Seq(
        """{"ID":1,"cnt1":3,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}""",
        """{"ID":2,"cnt1":0,"NUMBERS":[],"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "cnt1", "NUMBERS", "PLACE", "PEOPLE")

      // df.printSchema()
      // df.show()
      // val ast = NestedRecordCombiner.constructWriterAst(parsedCopybook, df.schema)
      // println(ast)
      // Apply the UDF to the full record by packing all columns into a struct
      //val dfWithDump = df.withColumn(
      //  "record_dump",
      //  printRowUdf(struct(df.columns.map(col): _*))
      //)
      //dfWithDump.select("record_dump").show(truncate = false)

      withTempDirectory("cobol_writer2") { tempDir =>
        val path = new Path(tempDir, "writer2")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "false")
          .option("is_rdw_part_of_record_length", "true")
          .option("variable_size_occurs", "max_size")
          .save(path.toString)

        //val df2 = spark.read.format("cobol")
        //  .option("copybook_contents", copybookWithDependingOn)
        //  .option("record_format", "V")
        //  .option("is_rdw_big_endian", "false")
        //  .option("is_rdw_part_of_record_length", "true")
        //  .load(path.toString)
        //println(SparkUtils.convertDataFrameToPrettyJSON(df2))

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data
        val expected = Array(
          0x70, 0x00, 0x00, 0x00, // RDW record 0
          0xF0, 0xF1, 0x00, 0xF3, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0x00, 0x00, 0x00, 0x00, 0xE4, 0xE2, 0xD5, 0x85,
          0xA6, 0x40, 0xE8, 0x96, 0x99, 0x92, 0x40, 0x40, 0xF2, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40,
          0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF4, 0x40, 0x40, 0x40, 0x40,
          0xD1, 0x81, 0x95, 0x85, 0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5,
          0xCA, 0xF5, 0xF6, 0xF7, 0xF8, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x70, 0x00, 0x00, 0x00, // RDW record 1
          0xF0, 0xF2, 0x00, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xE9, 0xC1, 0xC3, 0x81,
          0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xF1, 0xE3, 0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99,
          0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS DEPENDING ON, fixed record length with record padding" in {
      val exampleJsons = Seq(
        """{"ID":1,"cnt1":3,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}""",
        """{"ID":2,"cnt1":0,"NUMBERS":[],"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "cnt1", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer2") { tempDir =>
        val path = new Path(tempDir, "writer2")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "F")
          .option("variable_size_occurs", "pad_record")
          .save(path.toString)

/*        val df2 = spark.read.format("cobol")
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "F")
          .option("variable_size_occurs", "pad_record")
          .load(path.toString)
        println(SparkUtils.convertDataFrameToPrettyJSON(df2))*/

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data (no RDW headers)
        val expected = Array(
          // Record 0
          0xF0, 0xF1, 0x00, 0xF3, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0xE4, 0xE2, 0xD5, 0x85, 0xA6, 0x40, 0xE8, 0x96,
          0x99, 0x92, 0x40, 0x40, 0xF2, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40, 0x40, 0x40, 0x40, 0x40,
          0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF4, 0x40, 0x40, 0x40, 0x40, 0xD1, 0x81, 0x95, 0x85,
          0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF5, 0xF6, 0xF7,
          0xF8, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          // Record 1
          0xF0, 0xF2, 0x00, 0xF0, 0xE9, 0xC1, 0xC3, 0x81, 0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xF1, 0xE3,
          0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99, 0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA,
          0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS DEPENDING ON, fixed record length without record padding" in {
      val exampleJsons = Seq(
        """{"ID":1,"cnt1":3,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}""",
        """{"ID":2,"cnt1":0,"NUMBERS":[],"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "cnt1", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer2") { tempDir =>
        val path = new Path(tempDir, "writer2")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "F")
          .option("variable_size_occurs", "shift_record")
          .save(path.toString)

        /*        val df2 = spark.read.format("cobol")
                  .option("copybook_contents", copybookWithDependingOn)
                  .option("record_format", "F")
                  .option("variable_size_occurs", "shift_record")
                  .load(path.toString)
                println(SparkUtils.convertDataFrameToPrettyJSON(df2))*/

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data (no RDW headers)
        val expected = Array(
          // Record 0
          0xF0, 0xF1, 0x00, 0xF3, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0xE4, 0xE2, 0xD5, 0x85, 0xA6, 0x40, 0xE8, 0x96,
          0x99, 0x92, 0x40, 0x40, 0xF2, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40, 0x40, 0x40, 0x40, 0x40,
          0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF4, 0x40, 0x40, 0x40, 0x40, 0xD1, 0x81, 0x95, 0x85,
          0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF5, 0xF6, 0xF7,
          0xF8, 0x40, 0x40, 0x40, 0x40,
          // Record 1
          0xF0, 0xF2, 0x00, 0xF0, 0xE9, 0xC1, 0xC3, 0x81, 0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xF1, 0xE3,
          0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99, 0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA,
          0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS DEPENDING ON and variable length occurs" in {
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}""",
        """{"ID":2,"NUMBERS":[],"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer3") { tempDir =>
        val path = new Path(tempDir, "writer3")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "false")
          .option("is_rdw_part_of_record_length", "true")
          .option("variable_size_occurs", "shift_record")
          .save(path.toString)

        //        val df2 = spark.read.format("cobol")
        //          .option("copybook_contents", copybookWithDependingOn)
        //          .option("record_format", "V")
        //          .option("is_rdw_big_endian", "false")
        //          .option("is_rdw_part_of_record_length", "true")
        //          .option("variable_size_occurs", "true")
        //          .load(path.toString)
        //        println(SparkUtils.convertDataFrameToPrettyJSON(df2))

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data
        val expected = Array(
          0x51, 0x00, 0x00, 0x00, // RDW record 0
          0xF0, 0xF1, 0x00, 0xF3, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0xE4, 0xE2, 0xD5, 0x85, 0xA6, 0x40, 0xE8, 0x96,
          0x99, 0x92, 0x40, 0x40, 0xF2, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40, 0x40, 0x40, 0x40, 0x40,
          0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF4, 0x40, 0x40, 0x40, 0x40, 0xD1, 0x81, 0x95, 0x85,
          0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF5, 0xF6, 0xF7,
          0xF8, 0x40, 0x40, 0x40, 0x40,
          0x30, 0x00, 0x00, 0x00, // RDW record 1
          0xF0, 0xF2, 0x00, 0xF0, 0xE9, 0xC1, 0xC3, 0x81, 0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xF1, 0xE3,
          0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99, 0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA,
          0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS DEPENDING ON and variable length occurs with record padding" in {
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}""",
        """{"ID":2,"NUMBERS":[],"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer3") { tempDir =>
        val path = new Path(tempDir, "writer3")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "false")
          .option("is_rdw_part_of_record_length", "true")
          .option("variable_size_occurs", "pad_record")
          .save(path.toString)

/*
        val parsedCopybook = CopybookParser.parseTree(copybookWithDependingOn)
        val layout = parsedCopybook.generateRecordLayoutPositions()
        println(layout)
*/

        /*
                val df2 = spark.read.format("cobol")
                  .option("copybook_contents", copybookWithDependingOn)
                  .option("record_format", "V")
                  .option("is_rdw_big_endian", "false")
                  .option("is_rdw_part_of_record_length", "true")
                  .option("variable_size_occurs", "pad_record")
                  .load(path.toString)
                println(SparkUtils.convertDataFrameToPrettyJSON(df2))
        */

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data
        val expected = Array(
          0x70, 0x00, 0x00, 0x00, // RDW record 0
          0xF0, 0xF1, 0x00, 0xF3, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0xE4, 0xE2, 0xD5, 0x85, 0xA6, 0x40, 0xE8, 0x96,
          0x99, 0x92, 0x40, 0x40, 0xF2, 0xD1, 0x96, 0x88, 0x95, 0x40, 0xC4, 0x96, 0x85, 0x40, 0x40, 0x40, 0x40, 0x40,
          0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF1, 0xF2, 0xF3, 0xF4, 0x40, 0x40, 0x40, 0x40, 0xD1, 0x81, 0x95, 0x85,
          0x40, 0xE2, 0x94, 0x89, 0xA3, 0x88, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA, 0xF5, 0xF6, 0xF7,
          0xF8, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x70, 0x00, 0x00, 0x00, // RDW record 1
          0xF0, 0xF2, 0x00, 0xF0, 0xE9, 0xC1, 0xC3, 0x81, 0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xF1, 0xE3,
          0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99, 0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA,
          0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }

    "write the dataframe with OCCURS DEPENDING ON and variable length occurs and null values" in {
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"}}""",
        """{"ID":2,"PLACE":{"COUNTRY_CODE":"ZA","CITY":"Cape Town"},"PEOPLE":[{"NAME":"Test User","PHONE_NUMBER":"555-1235"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      withTempDirectory("cobol_writer3") { tempDir =>
        val path = new Path(tempDir, "writer3")

        df.coalesce(1)
          .orderBy("id")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithDependingOn)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "false")
          .option("is_rdw_part_of_record_length", "true")
          .option("variable_size_occurs", "true")
          .save(path.toString)

//        val df2 = spark.read.format("cobol")
//          .option("copybook_contents", copybookWithDependingOn)
//          .option("record_format", "V")
//          .option("is_rdw_big_endian", "false")
//          .option("is_rdw_part_of_record_length", "true")
//          .option("variable_size_occurs", "true")
//          .load(path.toString)
//        println(SparkUtils.convertDataFrameToPrettyJSON(df2))

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")

        val partFile = files.head.getPath
        val data = fs.open(partFile)
        val bytes = new Array[Byte](files.head.getLen.toInt)
        data.readFully(bytes)
        data.close()

        // Expected EBCDIC data for sample test data
        val expected = Array(
          0x1B, 0x00, 0x00, 0x00, // RDW record 0
          0xF0, 0xF1, 0x00, 0xF3, 0xF1, 0xF0, 0xF2, 0xF0, 0xF3, 0xF0, 0xE4, 0xE2, 0xD5, 0x85, 0xA6, 0x40, 0xE8, 0x96,
          0x99, 0x92, 0x40, 0x40, 0xF0,
          0x30, 0x00, 0x00, 0x00,
          0xF0, 0xF2, 0x00, 0xF0, 0xE9, 0xC1, 0xC3, 0x81, 0x97, 0x85, 0x40, 0xE3, 0x96, 0xA6, 0x95, 0x40, 0xF1, 0xE3,
          0x85, 0xA2, 0xA3, 0x40, 0xE4, 0xA2, 0x85, 0x99, 0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0xF5, 0xF5, 0xF5, 0xCA,
          0xF1, 0xF2, 0xF3, 0xF5, 0x40, 0x40, 0x40, 0x40
        ).map(_.toByte)

        compareBinary(bytes, expected, "Written data should match expected EBCDIC encoding")
      }
    }
  }

  "constructWriterAst" should {
    "fail on duplicate depending on fields" in {
      val copybook =
        """      01 RECORD.
          |         05  ID               PIC 9(2).
          |         05  FILLER           PIC 9(1).
          |         05  CNT1             PIC 9(1).
          |         05  NUMBERS          PIC 9(2)
          |                 OCCURS 0 TO 5 DEPENDING ON CNT1.
          |         05  PLACE.
          |            10  COUNTRY-CODE  PIC X(2).
          |            10  CITY          PIC X(10).
          |         05  CNT1             PIC 9(1).
          |         05  PEOPLE
          |                 OCCURS 0 TO 3 DEPENDING ON CNT1.
          |            10 NAME           PIC X(14).
          |            10 FILLER         PIC X(1).
          |            10 PHONE-NUMBER   PIC X(12).
          |""".stripMargin
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe","PHONE_NUMBER":"555-1234"},{"NAME": "Jane Smith","PHONE_NUMBER":"555-5678"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      val parsedCopybook: Copybook = CopybookParser.parse(copybook)
      val ast = parsedCopybook.ast
      val children = ast.children.head.asInstanceOf[Group].children
      val cnt2 = children(5).asInstanceOf[Primitive].withUpdatedIsDependee(true)
      children(5) = cnt2

      val ex = intercept[IllegalArgumentException] {
        NestedRecordCombiner.constructWriterAst(parsedCopybook, df.schema, strictSchema = false)
      }

      assert(ex.getMessage == "Duplicate field name 'CNT1' found in copybook. Field names must be unique (case-insensitive) when OCCURS DEPENDING ON is used. Already found a dependee field with the same name at line 4, current field line number: 10.")
    }

    "fail when a field in the copybook does not exits in Spark schema" in {
      val copybook =
        """      01 RECORD.
          |         05  ID               PIC 9(2).
          |         05  FILLER           PIC 9(1).
          |         05  CNT1             PIC 9(1).
          |         05  NUMBERS          PIC 9(2)
          |                 OCCURS 0 TO 5 DEPENDING ON CNT1.
          |         05  PLACE.
          |            10  COUNTRY-CODE  PIC X(2).
          |            10  CITY          PIC X(10).
          |         05  CNT2             PIC 9(1).
          |         05  PEOPLE
          |                 OCCURS 0 TO 3 DEPENDING ON CNT1.
          |            10 NAME           PIC X(14).
          |            10 FILLER         PIC X(1).
          |            10 PHONE-NUMBER   PIC X(12).
          |""".stripMargin
      val exampleJsons = Seq(
        """{"ID":1,"NUMBERS":[10,20,30],"PLACE":{"COUNTRY_CODE":"US","CITY":"New York"},"PEOPLE":[{"NAME":"John Doe"},{"NAME": "Jane Smith"}]}"""
      )

      import spark.implicits._

      val df = spark.read.json(exampleJsons.toDS())
        .select("ID", "NUMBERS", "PLACE", "PEOPLE")

      val parsedCopybook: Copybook = CopybookParser.parse(copybook)
      val ast = parsedCopybook.ast
      val children = ast.children.head.asInstanceOf[Group].children
      val cnt2 = children(5).asInstanceOf[Primitive].withUpdatedIsDependee(true)
      children(5) = cnt2

      // This should not throw
      NestedRecordCombiner.constructWriterAst(parsedCopybook, df.schema, strictSchema = false)

      val ex = intercept[IllegalArgumentException] {
        NestedRecordCombiner.constructWriterAst(parsedCopybook, df.schema, strictSchema = true)
      }

      assert(ex.getMessage == "Field 'PEOPLE.PHONE_NUMBER' is not found in Spark schema.")
    }
  }

}
