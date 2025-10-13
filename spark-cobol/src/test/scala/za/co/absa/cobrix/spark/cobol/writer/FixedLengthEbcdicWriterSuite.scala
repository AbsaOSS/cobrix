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
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

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

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContents)
          //.option("ebcdic_code_page", "cp1144")
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
