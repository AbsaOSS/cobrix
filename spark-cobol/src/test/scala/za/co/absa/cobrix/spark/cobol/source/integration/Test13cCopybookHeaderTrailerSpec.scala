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

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test13cCopybookHeaderTrailerSpec extends AnyFunSuite with SparkTestBase with BinaryFileFixture {

  // Copybook with header record (10 bytes), data record (10 bytes), and trailer record (8 bytes)
  private val copybookContents =
    """       01  HEADER-REC.
      |           05  HDR-TAG        PIC X(4).
      |           05  HDR-DATE       PIC X(6).
      |       01  DATA-REC.
      |           05  NAME           PIC X(6).
      |           05  AMOUNT         PIC 9(4).
      |       01  TRAILER-REC.
      |           05  TRL-TAG        PIC X(4).
      |           05  TRL-COUNT      PIC 9(4).
      |""".stripMargin

  // Header: "HDR 240101" (10 bytes)
  // Data record 1: "ALICE 0100" (10 bytes)
  // Data record 2: "BOB   0200" (10 bytes)
  // Trailer: "TRL 0002" (8 bytes)
  private val testData: Array[Byte] = Array[Byte](
    // Header record: HDR 240101 (10 bytes)
    0x48, 0x44, 0x52, 0x20, 0x32, 0x34, 0x30, 0x31, 0x30, 0x31,
    // Data record 1: ALICE 0100 (10 bytes)
    0x41, 0x4C, 0x49, 0x43, 0x45, 0x20, 0x30, 0x31, 0x30, 0x30,
    // Data record 2: BOB   0200 (10 bytes)
    0x42, 0x4F, 0x42, 0x20, 0x20, 0x20, 0x30, 0x32, 0x30, 0x30,
    // Trailer record: TRL 0002 (8 bytes)
    0x54, 0x52, 0x4C, 0x20, 0x30, 0x30, 0x30, 0x32
  )

  test("Test reading with record_header_name and record_trailer_name") {
    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookContents)
        .option("encoding", "ascii")
        .option("record_header_name", "HEADER-REC")
        .option("record_trailer_name", "TRAILER-REC")
        .option("schema_retention_policy", "collapse_root")
        .load(tempFile)

      // Schema should only contain data record fields, not header/trailer fields
      val fieldNames = df.schema.fieldNames
      assert(fieldNames.contains("NAME"), s"Schema should contain NAME field but has: ${fieldNames.mkString(", ")}")
      assert(fieldNames.contains("AMOUNT"), s"Schema should contain AMOUNT field but has: ${fieldNames.mkString(", ")}")
      assert(!fieldNames.contains("HDR_TAG"), s"Schema should NOT contain HDR_TAG but has: ${fieldNames.mkString(", ")}")
      assert(!fieldNames.contains("TRL_TAG"), s"Schema should NOT contain TRL_TAG but has: ${fieldNames.mkString(", ")}")

      // Data should contain 2 records
      assert(df.count() == 2)

      val data = df.orderBy("NAME").collect()
      assert(data(0).getString(0).trim == "ALICE")
      assert(data(0).getInt(1) == 100)
      assert(data(1).getString(0).trim == "BOB")
      assert(data(1).getInt(1) == 200)
    }
  }

  test("Test that record_header_name produces same results as manual file_start_offset with data-only copybook") {
    // Copybook with only the data record (no header/trailer definitions)
    val dataOnlyCopybook =
      """       01  DATA-REC.
        |           05  NAME           PIC X(6).
        |           05  AMOUNT         PIC 9(4).
        |""".stripMargin

    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val dfWithHeaderName = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookContents)
        .option("encoding", "ascii")
        .option("record_header_name", "HEADER-REC")
        .option("record_trailer_name", "TRAILER-REC")
        .option("schema_retention_policy", "collapse_root")
        .load(tempFile)

      val dfWithOffsets = spark
        .read
        .format("cobol")
        .option("copybook_contents", dataOnlyCopybook)
        .option("encoding", "ascii")
        .option("file_start_offset", "10")
        .option("file_end_offset", "8")
        .option("schema_retention_policy", "collapse_root")
        .load(tempFile)

      val resultHeaderName = dfWithHeaderName.orderBy("NAME").collect().map(r => (r.getString(0).trim, r.getInt(1)))
      val resultOffsets = dfWithOffsets.orderBy("NAME").collect().map(r => (r.getString(0).trim, r.getInt(1)))

      assert(resultHeaderName.sameElements(resultOffsets),
        s"Results should match. Header name: ${resultHeaderName.mkString(",")}. Offsets: ${resultOffsets.mkString(",")}")
    }
  }

  test("Test that file_header_field produces same results as manual file_start_offset with data-only copybook") {
    // Copybook with only the data record (no header/trailer definitions)
    val dataOnlyCopybook =
      """       01  DATA-REC.
        |           05  NAME           PIC X(6).
        |           05  AMOUNT         PIC 9(4).
        |""".stripMargin

    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val dfWithHeaderName = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookContents)
        .option("encoding", "ascii")
        .option("file_header_field", "HEADER-REC")
        .option("file_trailer_field", "TRAILER-REC")
        .option("schema_retention_policy", "collapse_root")
        .load(tempFile)

      val dfWithOffsets = spark
        .read
        .format("cobol")
        .option("copybook_contents", dataOnlyCopybook)
        .option("encoding", "ascii")
        .option("file_start_offset", "10")
        .option("file_end_offset", "8")
        .option("schema_retention_policy", "collapse_root")
        .load(tempFile)

      val resultHeaderName = dfWithHeaderName.orderBy("NAME").collect().map(r => (r.getString(0).trim, r.getInt(1)))
      val resultOffsets = dfWithOffsets.orderBy("NAME").collect().map(r => (r.getString(0).trim, r.getInt(1)))

      assert(resultHeaderName.sameElements(resultOffsets),
        s"Results should match. Header name: ${resultHeaderName.mkString(",")}. Offsets: ${resultOffsets.mkString(",")}")
    }
  }

  test("Test only record_header_name without trailer") {
    // When only header is excluded, each data record includes DATA-REC (10 bytes) + TRAILER-REC (8 bytes) = 18 bytes
    val testDataNoTrailer: Array[Byte] = Array[Byte](
      // Header record: HDR 240101 (10 bytes)
      0x48, 0x44, 0x52, 0x20, 0x32, 0x34, 0x30, 0x31, 0x30, 0x31,
      // Data record 1: ALICE 0100 (10 bytes DATA-REC) + TRL 0001 (8 bytes TRAILER-REC) = 18 bytes
      0x41, 0x4C, 0x49, 0x43, 0x45, 0x20, 0x30, 0x31, 0x30, 0x30,
      0x54, 0x52, 0x4C, 0x20, 0x30, 0x30, 0x30, 0x31,
      // Data record 2: BOB   0200 (10 bytes DATA-REC) + TRL 0002 (8 bytes TRAILER-REC) = 18 bytes
      0x42, 0x4F, 0x42, 0x20, 0x20, 0x20, 0x30, 0x32, 0x30, 0x30,
      0x54, 0x52, 0x4C, 0x20, 0x30, 0x30, 0x30, 0x32
    )

    withTempBinFile("test13c", ".dat", testDataNoTrailer) { tempFile =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybookContents)
        .option("encoding", "ascii")
        .option("record_header_name", "HEADER-REC")
        .option("schema_retention_policy", "collapse_root")
        .load(tempFile)

      assert(df.count() == 2)

      // Schema should include both DATA-REC and TRAILER-REC fields (only header is excluded)
      val fieldNames = df.schema.fieldNames
      assert(fieldNames.contains("NAME"))
      assert(fieldNames.contains("AMOUNT"))
      assert(fieldNames.contains("TRL_TAG"))
      assert(fieldNames.contains("TRL_COUNT"))
      assert(!fieldNames.contains("HDR_TAG"))
    }
  }

  test("Test error when record_header_name is used with file_start_offset") {
    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookContents)
          .option("encoding", "ascii")
          .option("record_header_name", "HEADER-REC")
          .option("file_start_offset", "10")
          .option("schema_retention_policy", "collapse_root")
          .load(tempFile)
          .count()
      }
      assert(ex.getMessage.contains("record_header_name"))
      assert(ex.getMessage.contains("file_start_offset"))
    }
  }

  test("Test error when record_trailer_name is used with file_end_offset") {
    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookContents)
          .option("encoding", "ascii")
          .option("record_trailer_name", "TRAILER-REC")
          .option("file_end_offset", "8")
          .option("schema_retention_policy", "collapse_root")
          .load(tempFile)
          .count()
      }
      assert(ex.getMessage.contains("record_trailer_name"))
      assert(ex.getMessage.contains("file_end_offset"))
    }
  }

  test("Test error when record_header_name references a non-existent record") {
    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookContents)
          .option("encoding", "ascii")
          .option("record_header_name", "NON-EXISTENT")
          .option("schema_retention_policy", "collapse_root")
          .load(tempFile)
          .count()
      }
      assert(ex.getMessage.contains("NON-EXISTENT"))
      assert(ex.getMessage.contains("not found"))
    }
  }

  test("Test error when record_header_name and record_trailer_name are the same") {
    withTempBinFile("test13c", ".dat", testData) { tempFile =>
      val ex = intercept[IllegalArgumentException] {
        spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookContents)
          .option("encoding", "ascii")
          .option("record_header_name", "HEADER-REC")
          .option("record_trailer_name", "HEADER-REC")
          .option("schema_retention_policy", "collapse_root")
          .load(tempFile)
          .count()
      }
      assert(ex.getMessage.contains("cannot refer to the same record"))
    }
  }
}
