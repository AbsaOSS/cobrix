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
import org.scalatest.Assertion
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.Cobrix
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

import scala.annotation.tailrec

class FixedLengthEbcdicWriterSuite extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {

  import spark.implicits._

  private val copybookContents =
    """       01  RECORD.
           05  A       PIC X(1).
           05  B       PIC X(5).
    """

  "cobol writer" should {
    "write simple fixed-record-length EBCDIC data files" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer1")

        Cobrix.validateOptions
          .option("copybook_contents", copybookContents)
          .validateSave(df)

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

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
        val expected = Array[Byte](
          0xC1.toByte, 0xC6.toByte, 0x89.toByte, 0x99.toByte, 0xa2.toByte, 0xa3.toByte, // A,First
          0xC2.toByte, 0xE2.toByte, 0x83.toByte, 0x95.toByte, 0x84.toByte, 0x40.toByte, // B,Scnd_
          0xC3.toByte, 0xD3.toByte, 0x81.toByte, 0xa2.toByte, 0xa3.toByte, 0x40.toByte  // C,Last_
        )

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }


    "write simple fixed-record-length EBCDIC data files using code page 1144" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(("A", "F|rst"), ("B", "S€nd"), ("C", "Last]")).toDF("A", "B")

        val path = new Path(tempDir, "writer1")

        Cobrix.validateOptions
          .option("copybook_contents", copybookContents)
          .option("ebcdic_code_page", "cp1144")
          .validateSave(df)

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContents)
          .option("ebcdic_code_page", "cp1144")
          .save(path.toString)

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
        val expected = Array[Byte](
          0xC1.toByte, 0xC6.toByte, 0xBB.toByte, 0x99.toByte, 0xa2.toByte, 0xa3.toByte, // A,F|rst
          0xC2.toByte, 0xE2.toByte, 0x9F.toByte, 0x95.toByte, 0x84.toByte, 0x40.toByte, // B,S€nd_
          0xC3.toByte, 0xD3.toByte, 0x81.toByte, 0xa2.toByte, 0xa3.toByte, 0x51.toByte  // C,Last]
        )

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC1144 encoding")
        }
      }
    }


    "write data frames with different field order and null values" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List((1, "First", "A"), (2, "Scnd", "B"), (3, null, "C")).toDF("C", "B", "A")

        val path = new Path(tempDir, "writer1")

        val copybookContentsWithFilers =
          """       01  RECORD.
           05  A       PIC X(1).
           05  FILLER  PIC X(1).
           05  B       PIC X(5).
    """

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContentsWithFilers)
          .save(path.toString)

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
        val expected = Array[Byte](
          0xC1.toByte, 0x00.toByte, 0xC6.toByte, 0x89.toByte, 0x99.toByte, 0xa2.toByte, 0xa3.toByte, // A,First
          0xC2.toByte, 0x00.toByte, 0xE2.toByte, 0x83.toByte, 0x95.toByte, 0x84.toByte, 0x40.toByte, // B,Scnd_
          0xC3.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte  // C,Last_
        )

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "write data frames with COMP-3 fields" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(
          (1, 100.5, new java.math.BigDecimal(10.23), 1, 100.5, new java.math.BigDecimal(10.12)),
          (2, 800.4, new java.math.BigDecimal(30), 2, 800.4, new java.math.BigDecimal(30)),
          (3, 22.33, new java.math.BigDecimal(-20), 3, 22.33, new java.math.BigDecimal(-20))
        ).toDF("A", "B", "C", "D", "E", "F")

        val path = new Path(tempDir, "writer1")

        val copybookContentsWithFilers =
          """       01  RECORD.
           05  A       PIC S9(1)      COMP-3.
           05  B       PIC 9(4)V9(2)  COMP-3.
           05  C       PIC S9(2)V9(2) COMP-3.
           05  D       PIC 9(1)       COMP-3U.
           05  E       PIC 9(4)V9(2)  COMP-3U.
           05  F       PIC 9(2)V9(2)  COMP-3U.
    """

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContentsWithFilers)
          .save(path.toString)

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
          0x1C, 0x00, 0x10, 0x05, 0x0F, 0x01, 0x02, 0x3C, 0x01, 0x01, 0x00, 0x50, 0x10, 0x12,
          0x2C, 0x00, 0x80, 0x04, 0x0F, 0x03, 0x00, 0x0C, 0x02, 0x08, 0x00, 0x40, 0x30, 0x00,
          0x3C, 0x00, 0x02, 0x23, 0x3F, 0x02, 0x00, 0x0D, 0x03, 0x00, 0x22, 0x33, 0x00, 0x00
        ).map(_.toByte)

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "write data frames with COMP fields" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(
          (1, 100.5, new java.math.BigDecimal(10.23), 1, 10050, new java.math.BigDecimal(10.12)),
          (2, 800.4, new java.math.BigDecimal(30), 2, 80040, new java.math.BigDecimal(30)),
          (3, 22.33, new java.math.BigDecimal(-20), 3, -2233, new java.math.BigDecimal(-20))
        ).toDF("A", "B", "C", "D", "E", "F")

        val path = new Path(tempDir, "writer1")

        val copybookContentsWithBinFields =
          """       01  RECORD.
           05  A       PIC S9(1)      COMP.
           05  B       PIC 9(4)V9(2)  COMP-4.
           05  C       PIC S9(2)V9(2) BINARY.
           05  D       PIC 9(1)       COMP-9.
           05  E       PIC S9(6)      COMP-9.
           05  F       PIC 9(2)V9(2)  COMP-9.
    """

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContentsWithBinFields)
          .save(path.toString)

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
          0x00, 0x01,              // 1 (short, big-endian)
          0x00, 0x00, 0x27, 0x42,  // 100.5 -> 10050(int, big-endian)
          0x03, 0xFF,              // 10.23 -> 1023(short, big-endian)
          0x01,                    // 1 (byte)
          0x42, 0x27, 0x00, 0x00,  // 10050(int, little-endian)
          0xF4, 0x03,              // 10.12 -> 1012(short, little-endian)

          0x00, 0x02,              // 2 (short, big-endian)
          0x00, 0x01, 0x38, 0xA8,  // 800.4 -> 80040(int, big-endian)
          0x0B, 0xB8,              // 30 -> 3000(short, big-endian)
          0x02,                    // 2 (byte)
          0xA8, 0x38, 0x01, 0x00,  // 80040(int, little-endian)
          0xB8, 0x0B,              // 30 -> 3000(short, little-endian)

          0x00, 0x03,              // 3 (short, big-endian)
          0x00, 0x00, 0x08, 0xB9,  // 22.33 -> 2233(int, big-endian)
          0xF8, 0x30,              // -20 -> -2000(short, big-endian)
          0x03,                    // 3 (byte)
          0x47, 0xF7, 0xFF, 0xFF,  // -2233(int, little-endian)
          0x00, 0x00               // null, because -20 cannot fix the unsigned type
        ).map(_.toByte)

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "write data frames with DISPLAY fields" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val bigDecimalNull = null: java.math.BigDecimal
        val df = List(
          (-1, 100.5, new java.math.BigDecimal(10.23), 1, 10050, new java.math.BigDecimal(10.12)),
          (2, 800.4, new java.math.BigDecimal(30), 2, 80040, new java.math.BigDecimal(30)),
          (3, 22.33, new java.math.BigDecimal(-20), -3, -2233, new java.math.BigDecimal(-20.456)),
          (4, -1.0, bigDecimalNull, 400, 1000000, bigDecimalNull)
        ).toDF("A", "B", "C", "D", "E", "F")

        val path = new Path(tempDir, "writer1")

        val copybookContentsWithDisplayFields =
          """       01  RECORD.
           05  A       PIC S9(1).
           05  B       PIC 9(4)V9(2).
           05  C       PIC S9(2).9(2).
           05  C1      PIC X(5)       REDEFINES C.
           05  D       PIC 9(1).
           05  E       PIC S9(6)      SIGN IS LEADING SEPARATE.
           05  F       PIC S9(2).9(2) SIGN IS TRAILING SEPARATE.
    """

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContentsWithDisplayFields)
          .save(path.toString)

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
          0xD1,                                      // -1     PIC S9(1).
          0xF0, 0xF1, 0xF0, 0xF0, 0xF5, 0xF0,        // 100.5  PIC 9(4)V9(2)
          0xF1, 0xF0, 0x4B, 0xF2, 0xC3,              // 10.23  PIC S9(2).9(2)
          0xF1,                                      // 1      9(1)
          0x4E, 0xF0, 0xF1, 0xF0, 0xF0, 0xF5, 0xF0,  // 10050  S9(6)      SIGN IS LEADING SEPARATE.
          0xF1, 0xF0, 0x4B, 0xF1, 0xF2, 0x4E,        // 10.12  S9(2).9(2) SIGN IS TRAILING SEPARATE

          0xC2,                                      // 2      PIC S9(1).
          0xF0, 0xF8, 0xF0, 0xF0, 0xF4, 0xF0,        // 800.4  PIC 9(4)V9(2)
          0xF3, 0xF0, 0x4B, 0xF0, 0xC0,              // 30     PIC S9(2).9(2)
          0xF2,                                      // 2      9(1)
          0x4E, 0xF0, 0xF8, 0xF0, 0xF0, 0xF4, 0xF0,  // 80040  S9(6)      SIGN IS LEADING SEPARATE.
          0xF3, 0xF0, 0x4B, 0xF0, 0xF0, 0x4E,        // 30     S9(2).9(2) SIGN IS TRAILING SEPARATE

          0xC3,                                      // 3      PIC S9(1).
          0xF0, 0xF0, 0xF2, 0xF2, 0xF3, 0xF3,        // 22.33  PIC 9(4)V9(2)
          0xF2, 0xF0, 0x4B, 0xF0, 0xD0,              // -20    PIC S9(2).9(2)
          0x00,                                      // null   PIC 9(1) (because a negative value cannot be converted to this PIC)
          0x60, 0xF0, 0xF0, 0xF2, 0xF2, 0xF3, 0xF3,  // -2233  S9(6)      SIGN IS LEADING SEPARATE.
          0xF2, 0xF0, 0x4B, 0xF4, 0xF6, 0x60,        // -20    S9(2).9(2) SIGN IS TRAILING SEPARATE

          0xC4,                                      // 4      PIC S9(1).
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00,        // nulls
          0x00, 0x00, 0x00, 0x00, 0x00,
          0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map(_.toByte)

        assertArraysEqual(bytes, expected)

        val df2 = spark.read.format("cobol")
          .option("copybook_contents", copybookContentsWithDisplayFields)
          .load(path.toString)
          .orderBy("A")

        val expectedJson =
          """[ {
            |  "A" : -1,
            |  "B" : 100.5,
            |  "C" : 10.23,
            |  "C1" : "10.2C",
            |  "D" : 1,
            |  "E" : 10050,
            |  "F" : 10.12
            |}, {
            |  "A" : 2,
            |  "B" : 800.4,
            |  "C" : 30.0,
            |  "C1" : "30.0{",
            |  "D" : 2,
            |  "E" : 80040,
            |  "F" : 30.0
            |}, {
            |  "A" : 3,
            |  "B" : 22.33,
            |  "C" : -20.0,
            |  "C1" : "20.0}",
            |  "E" : -2233,
            |  "F" : -20.46
            |}, {
            |  "A" : 4
            |} ]""".stripMargin

        val actualJson = SparkUtils.convertDataFrameToPrettyJSON(df2)

        compareText(actualJson, expectedJson)
      }
    }

    "write should successfully append" in {
      withTempDirectory("cobol_writer3") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer2")

        df.write
          .format("cobol")
          .mode(SaveMode.Append)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        df.write
          .format("cobol")
          .mode(SaveMode.Append)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))

        assert(files.length > 1)
      }
    }

    "write should fail with save mode fail if exists and the path exists" in {
      withTempDirectory("cobol_writer3") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer2")

        df.write
          .format("cobol")
          .mode(SaveMode.ErrorIfExists)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        assertThrows[IllegalArgumentException] {
          Cobrix.validateOptions
            .validateSave(df)
        }

        assertThrows[IllegalArgumentException] {
          df.write
            .format("cobol")
            .mode(SaveMode.ErrorIfExists)
            .option("copybook_contents", copybookContents)
            .save(path.toString)
        }
      }
    }

    "write should be ignored when save mode is ignore" in {
      withTempDirectory("cobol_writer3") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer2")

        df.write
          .format("cobol")
          .mode(SaveMode.Ignore)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        df.write
          .format("cobol")
          .mode(SaveMode.Ignore)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)
        assert(fs.exists(path), "Output directory should exist")
      }
    }

    "write data frames using REDEFINES fields" should {
      val copybookContentsWithRedefines =
        """       01  RECORD.
             05  A       PIC X(1).
             05  B1      PIC 9(5).
             05  B2      PIC X(5)       REDEFINES B1.
        """

      "write using only the base field of a REDEFINES group" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(("X", 12345)).toDF("A", "B1")

          val path = new Path(tempDir, "writer_redefines_base")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Expected EBCDIC data: A='X', B=12345 (DISPLAY digits)
          val expected = Array[Byte](
            0xE7.toByte,
            0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "write using only the redefining field of a REDEFINES group" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(("X", "ABCDE")).toDF("A", "B2")

          val path = new Path(tempDir, "writer_redefines_alt")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Expected EBCDIC data: A='X', B1="ABCDE"
          val expected = Array[Byte](
            0xE7.toByte,
            0xC1.toByte, 0xC2.toByte, 0xC3.toByte, 0xC4.toByte, 0xC5.toByte
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "fail fast when both the base and the redefining fields are populated on the same row" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(("X", 12345, "ABCDE")).toDF("A", "B1", "B2")

          val path = new Path(tempDir, "writer_redefines_conflict")

          Cobrix.validateOptions
            .option("copybook_contents", copybookContentsWithRedefines)
            .option("write_strict_redefines", "true")
            .option("pedantic", "true")
            .validateSave(df)

          val thrown = intercept[Throwable] {
            df.coalesce(1)
              .write
              .format("cobol")
              .mode(SaveMode.Overwrite)
              .option("copybook_contents", copybookContentsWithRedefines)
              .option("write_strict_redefines", "true")
              .save(path.toString)
          }

          val messages = causeChainMessages(thrown)
          assert(messages.exists(m => m.contains("B") && m.contains("B1")),
            s"Expected an error mentioning both conflicting REDEFINES fields 'B1' and 'B2', but got: ${messages.mkString(" | ")}")
        }
      }

      "write the first alternative when multiple REDEFINES fields are populated and strict is disabled" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(("X", 12345, "ABCDE")).toDF("A", "B1", "B2")

          val path = new Path(tempDir, "writer_redefines_first_wins")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // A='X' (0xE7), then B=12345 as EBCDIC DISPLAY digits (0xF1..0xF5). B1 ("ABCDE") is ignored.
          val expected = Array[Byte](
            0xE7.toByte,
            0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "write a data frame with a mix of rows using the base field and rows using the redefining field" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(
            ("1", Some(11111), None),
            ("2", None, Some("AAAAA")),
            ("3", Some(33333), None),
            ("4", None, Some("BBBBB"))
          ).toDF("A", "B1", "B2")

          val path = new Path(tempDir, "writer_redefines_mixed_rows")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Expected EBCDIC data per row: A=<digit>, then either B (DISPLAY digits) or B1 (alpha) depending on which is populated
          val expected = Array[Byte](
            0xF1.toByte, 0xF1.toByte, 0xF1.toByte, 0xF1.toByte, 0xF1.toByte, 0xF1.toByte,
            0xF2.toByte, 0xC1.toByte, 0xC1.toByte, 0xC1.toByte, 0xC1.toByte, 0xC1.toByte,
            0xF3.toByte, 0xF3.toByte, 0xF3.toByte, 0xF3.toByte, 0xF3.toByte, 0xF3.toByte,
            0xF4.toByte, 0xC2.toByte, 0xC2.toByte, 0xC2.toByte, 0xC2.toByte, 0xC2.toByte
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "write using the third alternative of a three-way REDEFINES chain" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val copybookContentsWithThreeWayRedefines =
            """       01  RECORD.
                 05  A       PIC X(1).
                 05  B       PIC 9(5).
                 05  B1      PIC X(5)       REDEFINES B.
                 05  B2      PIC 9(3)V99    REDEFINES B.
            """

          val df = List(("X", new java.math.BigDecimal("123.45"))).toDF("A", "B2")

          val path = new Path(tempDir, "writer_redefines_third")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithThreeWayRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Expected EBCDIC data: A='X', B2=123.45 (DISPLAY digits, implied decimal point)
          val expected = Array[Byte](
            0xE7.toByte,
            0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "write supporting 2 views on the same field" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val copybookContentsWith2Views =
            """       01  RECORD.
                 05  A          PIC X(1).
                 05  ACCOUNT    PIC 9(7).
                 05  DETAIL      REDEFINES ACCOUNT.
                    10  PREFIX  PIC 9(3).
                    10  SUFFIX  PIC 9(4).
            """

          // Each row uses one of the 2 views on the same 7 bytes:
          //   * the flat view    - ACCOUNT
          //   * the split view   - DETAIL (PREFIX + SUFFIX)
          //   * both are present
          val exampleJsons = Seq(
            """{"A":"X","ACCOUNT":1234567}""",
            """{"A":"Y","DETAIL":{"PREFIX":234,"SUFFIX":5678}}""",
            """{"A":"Z","ACCOUNT":3456789,"DETAIL":{"PREFIX":456,"SUFFIX":7890}}"""
          )

          val df = spark.read.json(exampleJsons.toDS())
            .select("A", "ACCOUNT", "DETAIL")

          val path = new Path(tempDir, "writer_redefines_2_views")

          df.coalesce(1)
            .orderBy("A")
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWith2Views)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Row 1: A='X', ACCOUNT=1234567 (DISPLAY digits), DETAIL absent
          val row1 = Array[Byte](
            0xE7.toByte,
            0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte
          )

          // Row 2: A='Y', DETAIL populated (PREFIX=123, SUFFIX=4567), ACCOUNT absent
          val row2 = Array[Byte](
            0xE8.toByte,
            0xF2.toByte, 0xF3.toByte, 0xF4.toByte,
            0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte
          )

          // Row 3: A='Z', ACCOUNT=3456789, DETAIL populated (PREFIX=456, SUFFIX=7890)
          val row3 = Array[Byte](
            0xE9.toByte,
            0xF3.toByte, 0xF4.toByte, 0xF5.toByte,
            0xF6.toByte, 0xF7.toByte, 0xF8.toByte, 0xF9.toByte
          )

          assertArraysEqual(bytes, row1 ++ row2 ++ row3)
        }
      }

      "fail fast when two non-adjacent alternatives of a three-way REDEFINES chain are both populated" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val copybookContentsWithThreeWayRedefines =
            """       01  RECORD.
                 05  A       PIC X(1).
                 05  B1      PIC 9(5).
                 05  B2      PIC X(5)       REDEFINES B1.
                 05  B3      PIC 9(3)V99    REDEFINES B1.
            """

          val df = List(("X", 12345, new java.math.BigDecimal("123.45"))).toDF("A", "B1", "B3")

          val path = new Path(tempDir, "writer_redefines_third_conflict")

          val thrown = intercept[Throwable] {
            df.coalesce(1)
              .write
              .format("cobol")
              .mode(SaveMode.Overwrite)
              .option("copybook_contents", copybookContentsWithThreeWayRedefines)
              .option("write_strict_redefines", "true")
              .save(path.toString)
          }

          val messages = causeChainMessages(thrown)
          assert(messages.exists(m => m.contains("B1") && m.contains("B3")),
            s"Expected an error mentioning both conflicting REDEFINES fields 'B1' and 'B3', but got: ${messages.mkString(" | ")}")
        }
      }

      "write the first alternative of a three-way REDEFINES chain when multiple are populated and strict is disabled" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val copybookContentsWithThreeWayRedefines =
            """       01  RECORD.
                 05  A       PIC X(1).
                 05  B1      PIC 9(5).
                 05  B2      PIC X(5)       REDEFINES B1.
                 05  B3      PIC 9(3)V99    REDEFINES B1.
            """

          val df = List(("X", 12345, new java.math.BigDecimal("123.45"))).toDF("A", "B1", "B3")

          val path = new Path(tempDir, "writer_redefines_three_first_wins")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithThreeWayRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // A='X' (0xE7), then B=12345 (0xF1..0xF5). B2 is ignored.
          val expected = Array[Byte](
            0xE7.toByte,
            0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "write zero bytes when none of the REDEFINES alternatives are present and strict schema is disabled" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(Tuple1("X")).toDF("A")

          val path = new Path(tempDir, "writer_redefines_none_present")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithRedefines)
            .option("strict_schema", "false")
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Expected EBCDIC data: A='X', shared bytes left as zeroes
          val expected = Array[Byte](
            0xE7.toByte,
            0x00, 0x00, 0x00, 0x00, 0x00
          )

          assertArraysEqual(bytes, expected)
        }
      }

      "fail with a clear message when none of the REDEFINES alternatives are present and strict schema is enabled" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val df = List(Tuple1("X")).toDF("A")

          val path = new Path(tempDir, "writer_redefines_none_present_strict")

          val ex = intercept[IllegalArgumentException] {
            df.coalesce(1)
              .write
              .format("cobol")
              .mode(SaveMode.Overwrite)
              .option("copybook_contents", copybookContentsWithRedefines)
              .save(path.toString)
          }

          assert(ex.getMessage.contains("B"))
          assert(ex.getMessage.contains("B1"))
        }
      }

      "write the full width of the REDEFINES cluster (max alternative size) and zero-pad unused trailing bytes " +
        "when alternatives have different sizes" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          val copybookContentsDifferentSizes =
            """       01  RECORD.
                 05  A       PIC X(1).
                 05  B       PIC 9(5).
                 05  B1      PIC X(35)      REDEFINES B.
            """

          val df = List(("X", 12345, Option.empty[String])).toDF("A", "B", "B1")

          val path = new Path(tempDir, "writer_redefines_diff_sizes")

          df.coalesce(1)
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsDifferentSizes)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // The REDEFINES cluster always reserves the widest alternative's size (35 bytes for B1),
          // even when the narrower alternative (B, 5 bytes) is the one populated.
          // Expected EBCDIC data: A='X', B=12345 (DISPLAY digits), followed by 30 zero-bytes
          // (the unused tail of B1's 35-byte region, left as binary zeroes rather than spaces).
          val expected = Array[Byte](0xE7.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte) ++
            Array.fill[Byte](30)(0x00)

          assert(bytes.length == 36, s"Expected a 36-byte record (1 + max(5, 35)), got ${bytes.length}")
          assertArraysEqual(bytes, expected)
        }
      }

      "write REDEFINES groups that contain nested sub-fields, selecting the active alternative via a " +
        "record-type discriminator column" in {
        withTempDirectory("cobol_writer_redefines") { tempDir =>
          // REC-TYPE is a plain data column: it carries no special meaning to the writer (the active
          // alternative is still chosen purely by which group is non-null on a given row), but it follows
          // the common COBOL convention of tagging each record with a type code so that a reader can tell
          // which REDEFINES alternative was used to write a row without inspecting the group contents.
          val copybookContentsWithNestedRedefines =
            """       01  RECORD.
                 05  A          PIC X(1).
                 05  REC-TYPE   PIC X(1).
                 05  GRP-B.
                    10  B-NUM   PIC 9(5).
                    10  B-NAME  PIC X(5).
                 05  GRP-B1     REDEFINES GRP-B.
                    10  B1-CODE PIC X(3).
                    10  B1-AMT  PIC 9(7).
            """

          val exampleJsons = Seq(
            """{"A":"1","REC_TYPE":"B","GRP_B":{"B_NUM":12345,"B_NAME":"HELLO"}}""",
            """{"A":"2","REC_TYPE":"1","GRP_B1":{"B1_CODE":"XYZ","B1_AMT":9876543}}"""
          )

          val df = spark.read.json(exampleJsons.toDS())
            .select("A", "REC_TYPE", "GRP_B", "GRP_B1")

          val path = new Path(tempDir, "writer_redefines_nested_groups")

          df.coalesce(1)
            .orderBy("A")
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContentsWithNestedRedefines)
            .save(path.toString)

          val bytes = readPartFileBytes(path)

          // Row 1: A='1', REC-TYPE='B', GRP-B populated (B-NUM=12345, B-NAME="HELLO"), GRP-B1 absent.
          val row1 = Array[Byte](
            0xF1.toByte, 0xC2.toByte,
            0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte,
            0xC8.toByte, 0xC5.toByte, 0xD3.toByte, 0xD3.toByte, 0xD6.toByte
          )
          // Row 2: A='2', REC-TYPE='1', GRP-B1 populated (B1-CODE="XYZ", B1-AMT=9876543), GRP-B absent.
          val row2 = Array[Byte](
            0xF2.toByte, 0xF1.toByte,
            0xE7.toByte, 0xE8.toByte, 0xE9.toByte,
            0xF9.toByte, 0xF8.toByte, 0xF7.toByte, 0xF6.toByte, 0xF5.toByte, 0xF4.toByte, 0xF3.toByte
          )

          assertArraysEqual(bytes, row1 ++ row2)
        }
      }
    }
  }

  def readPartFileBytes(path: Path): Array[Byte] = {
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
    bytes
  }

  def causeChainMessages(t: Throwable): List[String] = {
    @tailrec
    def loop(current: Throwable, acc: List[String], seen: Set[Throwable]): List[String] = {
      if (current == null || seen.contains(current)) {
        acc
      } else {
        val message = Option(current.getMessage).getOrElse("")
        loop(current.getCause, acc :+ message, seen + current)
      }
    }
    loop(t, Nil, Set.empty)
  }


  "write data frames with COMP-1 and COMP-2 fields" should {
    val copybookWithComp12 =
      """       01  RECORD.
           05  A       PIC S9(1)      COMP.
           05  B       PIC 9(4)V9(2)  COMP-1.
           05  C       PIC 9(4)V9(4)  COMP-2.
    """

    val df = List[(Int, java.lang.Float, java.lang.Double)](
      (1, 100.5f, 100.5),
      (2, 800.4f, 800.4),
      (3, -22.33f, -22.33),
      (4, 0f, 0.0),
      (5, Float.PositiveInfinity, Double.PositiveInfinity),
      (6, Float.NegativeInfinity, Double.NegativeInfinity),
      (7, Float.NaN, Double.NaN),
      (8, null: java.lang.Float, null: java.lang.Double)
    ).toDF("A", "B", "C")

    "IEE754 Little-endian" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithComp12)
          .option("floating_point_format", "IEEE754_little_endian")
          .save(path.toString)

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
          0x00, 0x01,                                      // 1
          0x00, 0x00, 0xC9, 0x42,                          // 100.5f
          0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x59, 0x40,  // 100.5d

          0x00, 0x02,                                      // 2
          0x9A, 0x19, 0x48, 0x44,                          // 800.4f
          0x33, 0x33, 0x33, 0x33, 0x33, 0x03, 0x89, 0x40,  // 800.4fd

          0x00, 0x03,                                      // 3
          0xD7, 0xA3, 0xB2, 0xC1,                          // -22.33f
          0x14, 0xAE, 0x47, 0xE1, 0x7A, 0x54, 0x36, 0xC0,  // -22.33d

          0x00, 0x04,                                      // 4
          0x00, 0x00, 0x00, 0x00,                          // 0
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 0

          0x00, 0x05,                                      // 5
          0x00, 0x00, 0x80, 0x7F,                          // +inf
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x7F,  // +inf

          0x00, 0x06,                                      // 6
          0x00, 0x00, 0x80, 0xFF,                          // -inf
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xFF,  // -inf

          0x00, 0x07,                                      // 7
          0x00, 0x00, 0xC0, 0x7F,                          // NaN
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF8, 0x7F,  // NaN

          0x00, 0x08,                                      // 8
          0x00, 0x00, 0x00, 0x00,                          // null
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00   // null
        ).map(_.toByte)

//         val df2 = spark.read.format("cobol")
//           .option("copybook_contents", copybookWithComp12)
//           .option("floating_point_format", "IEEE754_little_endian")
//           .load(path.toString)
//         //println(SparkUtils.convertDataFrameToPrettyJSON(df2))
//        df2.show(false)

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("0x%02X," format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("0x%02X," format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "IEE754 Big-endian" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithComp12)
          .option("floating_point_format", "IEEE754")
          .save(path.toString)

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
          0x00, 0x01,                                      // 1
          0x42, 0xC9, 0x00, 0x00,                          // 100.5f
          0x40, 0x59, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00,  // 100.5d

          0x00, 0x02,                                      // 2
          0x44, 0x48, 0x19, 0x9A,                          // 800.4f
          0x40, 0x89, 0x03, 0x33, 0x33, 0x33, 0x33, 0x33,  // 800.4fd

          0x00, 0x03,                                      // 3
          0xC1, 0xB2, 0xA3, 0xD7,                          // -22.33f
          0xC0, 0x36, 0x54, 0x7A, 0xE1, 0x47, 0xAE, 0x14,  // -22.33d

          0x00, 0x04,                                      // 4
          0x00, 0x00, 0x00, 0x00,                          // 0
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 0

          0x00, 0x05,                                      // 5
          0x7F, 0x80, 0x00, 0x00,                          // +inf
          0x7F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // +inf

          0x00, 0x06,                                      // 6
          0xFF, 0x80, 0x00, 0x00,                          // -inf
          0xFF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // -inf

          0x00, 0x07,                                      // 7
          0x7F, 0xC0, 0x00, 0x00,                          // NaN
          0x7F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // NaN

          0x00, 0x08,                                      // 8
          0x00, 0x00, 0x00, 0x00,                          // null
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00   // null
        ).map(_.toByte)

//        val df2 = spark.read.format("cobol")
//          .option("copybook_contents", copybookWithComp12)
//          .option("floating_point_format", "IEEE754")
//          .load(path.toString)
//        //println(SparkUtils.convertDataFrameToPrettyJSON(df2))
//        df2.show(false)

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("0x%02X," format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("0x%02X," format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "IBM Little-endian" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithComp12)
          .option("floating_point_format", "IBM_little_endian")
          .save(path.toString)

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
          0x00, 0x01,                                      // 1
          0x00, 0x80, 0x64, 0x42,                          // 100.5f
          0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x64, 0x42,  // 100.5d

          0x00, 0x02,                                      // 2
          0x66, 0x06, 0x32, 0x43,                          // 800.4f
          0x66, 0x66, 0x66, 0x66, 0x66, 0x06, 0x32, 0x43,  // 800.4fd

          0x00, 0x03,                                      // 3
          0x7A, 0x54, 0x16, 0xC2,                          // -22.33f
          0x14, 0xAE, 0x47, 0xE1, 0x7A, 0x54, 0x16, 0xC2,  // -22.33d

          0x00, 0x04,                                      // 4
          0x00, 0x00, 0x00, 0x00,                          // 0
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 0

          0x00, 0x05,                                      // 5
          0xFF, 0xFF, 0xFF, 0x7F,                          // +inf
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F,  // +inf

          0x00, 0x06,                                      // 7
          0xFF, 0xFF, 0xFF, 0xFF,                          // -inf
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,  // -inf

          0x00, 0x07,                                      // 8
          0xFF, 0xFF, 0xFF, 0xFF,                          // NaN
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,  // NaN

          0x00, 0x08,                                      // 8
          0x00, 0x00, 0x00, 0x00,                          // null
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00   // null
        ).map(_.toByte)

//        val df2 = spark.read.format("cobol")
//          .option("copybook_contents", copybookWithComp12)
//          .option("floating_point_format", "IBM_little_endian")
//          .load(path.toString)
//        //println(SparkUtils.convertDataFrameToPrettyJSON(df2))
//        df2.show(false)

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("0x%02X," format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("0x%02X," format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "IBM Big-endian" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookWithComp12)
          .option("floating_point_format", "IBM")
          .save(path.toString)

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
          0x00, 0x01,                                      // 1
          0x42, 0x64, 0x80, 0x00,                          // 100.5f
          0x42, 0x64, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00,  // 100.5d

          0x00, 0x02,                                      // 2
          0x43, 0x32, 0x06, 0x66,                          // 800.4f
          0x43, 0x32, 0x06, 0x66, 0x66, 0x66, 0x66, 0x66,  // 800.4fd

          0x00, 0x03,                                      // 3
          0xC2, 0x16, 0x54, 0x7A,                          // -22.33f
          0xC2, 0x16, 0x54, 0x7A, 0xE1, 0x47, 0xAE, 0x14,  // -22.33d

          0x00, 0x04,                                      // 4
          0x00, 0x00, 0x00, 0x00,                          // 0
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 0

          0x00, 0x05,                                      // 5
          0x7F, 0xFF, 0xFF, 0xFF,                          // +inf
          0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,  // +inf

          0x00, 0x06,                                      // 6
          0xFF, 0xFF, 0xFF, 0xFF,                          // -inf
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,  // -inf

          0x00, 0x07,                                      // 7
          0xFF, 0xFF, 0xFF, 0xFF,                          // NaN
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,  // NaN

          0x00, 0x08,                                      // 8
          0x00, 0x00, 0x00, 0x00,                          // null
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00   // null
        ).map(_.toByte)

//        val df2 = spark.read.format("cobol")
//          .option("copybook_contents", copybookWithComp12)
//          .option("floating_point_format", "IBM")
//          .load(path.toString)
//        //println(SparkUtils.convertDataFrameToPrettyJSON(df2))
//       df2.show(false)

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("0x%02X," format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("0x%02X," format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }
  }

  def assertArraysEqual(actual: Array[Byte], expected: Array[Byte]): Assertion = {
    if (!actual.sameElements(expected)) {
      val actualHex = actual.map(b => f"0x$b%02X").mkString(", ")
      val expectedHex = expected.map(b => f"0x$b%02X").mkString(", ")
      fail(s"Actual:   $actualHex\nExpected: $expectedHex")
    } else {
      succeed
    }
  }
}
