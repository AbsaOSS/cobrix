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
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class FixedLengthEbcdicWriterSuite extends AnyWordSpec with SparkTestBase with BinaryFileFixture {

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
          0xC3.toByte, 0xD3.toByte, 0x81.toByte, 0xa2.toByte, 0xa3.toByte, 0x40.toByte // C,Last_
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
          0xC3.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte // C,Last_
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


    "write should fail with save mode append and the path exists" in {
      withTempDirectory("cobol_writer3") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer2")

        df.write
          .format("cobol")
          .mode(SaveMode.Append)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        assertThrows[IllegalArgumentException] {
          df.write
            .format("cobol")
            .mode(SaveMode.Append)
            .option("copybook_contents", copybookContents)
            .save(path.toString)
        }
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

}
