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

class VariableLengthEbcdicWriterSuite extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {

  import spark.implicits._

  private val copybookContents =
    """       01  RECORD.
           05  A       PIC X(1).
           05  B       PIC X(5).
    """

  "cobol writer" should {
    "write simple variable -record-length EBCDIC data files with big-endian RDWs" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContents)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
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
          0x00.toByte, 0x06.toByte, 0x00.toByte, 0x00.toByte, // RDW1
          0xC1.toByte, 0xC6.toByte, 0x89.toByte, 0x99.toByte, 0xa2.toByte, 0xa3.toByte, // A,First
          0x00.toByte, 0x06.toByte, 0x00.toByte, 0x00.toByte, // RDW2
          0xC2.toByte, 0xE2.toByte, 0x83.toByte, 0x95.toByte, 0x84.toByte, 0x40.toByte, // B,Scnd_
          0x00.toByte, 0x06.toByte, 0x00.toByte, 0x00.toByte, // RDW3
          0xC3.toByte, 0xD3.toByte, 0x81.toByte, 0xa2.toByte, 0xa3.toByte, 0x40.toByte  // C,Last_
        )

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "write simple variable -record-length EBCDIC data files with little-endian RDWs and RDW being part of record length" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer1")

        df.coalesce(1)
          .orderBy("A")
          .write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContents)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "false")
          .option("is_rdw_part_of_record_length", "true")
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
          0x0A.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // RDW1
          0xC1.toByte, 0xC6.toByte, 0x89.toByte, 0x99.toByte, 0xa2.toByte, 0xa3.toByte, // A,First
          0x0A.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // RDW2
          0xC2.toByte, 0xE2.toByte, 0x83.toByte, 0x95.toByte, 0x84.toByte, 0x40.toByte, // B,Scnd_
          0x0A.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // RDW3
          0xC3.toByte, 0xD3.toByte, 0x81.toByte, 0xa2.toByte, 0xa3.toByte, 0x40.toByte  // C,Last_
        )

        if (!bytes.sameElements(expected)) {
          println(s"Expected bytes: ${expected.map("%02X" format _).mkString(" ")}")
          println(s"Actual bytes:   ${bytes.map("%02X" format _).mkString(" ")}")

          assert(bytes.sameElements(expected), "Written data should match expected EBCDIC encoding")
        }
      }
    }

    "throw an exception on unexpected output record format" in {
      withTempDirectory("cobol_writer2") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer2")

        val exception = intercept[IllegalArgumentException] {
          df.coalesce(1)
            .orderBy("A")
            .write
            .format("cobol")
            .mode(SaveMode.Overwrite)
            .option("copybook_contents", copybookContents)
            .option("record_format", "FB")           // Not supported
            .option("variable_size_occurs", "true")  // Not supported
            .option("occurs_mappings", "{\"DETAIL1\":{\"A\":0,\"B\":1},\"DETAIL2\":{\"A\":1,\"B\":2}}")
            .option("file_start_offset", "2")        // Not supported
            .option("record_end_offset", "4")        // Not supported
            .option("segment_field", "A")
            .save(path.toString)
        }

        assert(exception.getMessage.contains("Writer validation issues: Only 'F' and 'V' values for 'record_format' are supported for writing, provided value: 'FB';"))
        assert(exception.getMessage.contains("OCCURS mapping option ('occurs_mappings') is not supported for writing"))
        assert(exception.getMessage.contains("'record_start_offset' and 'record_end_offset' are not supported for writing"))
        assert(exception.getMessage.contains("'file_start_offset' and 'file_end_offset' are not supported for writing"))
        assert(exception.getMessage.contains("Multi-segment options ('segment_field', 'segment_filter', etc) are not supported for writing"))
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
