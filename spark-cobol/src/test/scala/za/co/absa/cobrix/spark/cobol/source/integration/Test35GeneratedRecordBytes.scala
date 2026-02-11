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

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser.PARAM_GENERATE_RECORD_BYTES
import za.co.absa.cobrix.spark.cobol.Cobrix
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test35GeneratedRecordBytes extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private val copybook =
    """         01 RECORD.
        02 FIELD-1 PIC X(2).
    """

  "record bytes generation" should {
    "work for EBCDIC fixed length RDDs" in {
      val expected = """[{"Record_Bytes":"8fI=","FIELD_1":"12"},{"Record_Bytes":"8/Q=","FIELD_1":"34"}]"""
      val data = Array(Array(0xF1, 0xF2).map(_.toByte), Array(0xF3, 0xF4).map(_.toByte))

      val rdd = spark.sparkContext.parallelize(data)

      val df = Cobrix.fromRdd
        .copybookContents(copybook)
        .option("encoding", "ebcdic")
        .option("generate_record_bytes", "true")
        .load(rdd)

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(actual == expected)
    }

    "work for fixed length EBCDIC files" in {
      val expected = """[{"Record_Bytes":"8fI=","FIELD_1":"12"},{"Record_Bytes":"8/Q=","FIELD_1":"34"}]"""
      val data = Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte)

      withTempBinFile("recbytesgen", ".dat", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ebcdic")
          .option("generate_record_bytes", "true")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for variable length EBCDIC files" in {
      val expected = """[{"Record_Bytes":"8fI=","FIELD_1":"12"},{"Record_Bytes":"8/Q=","FIELD_1":"34"}]"""
      val data = Array(0x00, 0x02, 0x00, 0x00, 0xF1, 0xF2, 0x00, 0x02, 0x00, 0x00, 0xF3, 0xF4).map(_.toByte)

      withTempBinFile("recbytesgen", ".dat", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("encoding", "ebcdic")
          .option("generate_record_bytes", "true")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for ASCII files" in {
      val expected = """[{"Record_Bytes":"MTI=","FIELD_1":"12"},{"Record_Bytes":"MzQ=","FIELD_1":"34"}]"""
      val data = Array(Array(0x31, 0x32, 0x33, 0x34).map(_.toByte))

      withTempBinFile("recbytesgen", ".dat", data.head) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ascii")
          .option("generate_record_bytes", "true")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "fail for hierarchical files" in {
      val data = Array(Array(0x31, 0x32, 0x33, 0x34).map(_.toByte))

      withTempBinFile("recbytesgen", ".dat", data.head) { fileName =>
        val ex = intercept[IllegalArgumentException] {
          spark.read
            .format("cobol")
            .option("copybook_contents", copybook)
            .option("encoding", "ascii")
            .option("segment_field", "FIELD_1")
            .option("redefine_segment_id_map:1", "FIELD_2 => C")
            .option("redefine-segment-id-map:2", "FIELD_2 => P")
            .option("segment-children:1", "FIELD_2 => FIELD_1")
            .option("generate_record_bytes", "true")
            .load(fileName)
        }

        assert(ex.getMessage.contains(s"Option '$PARAM_GENERATE_RECORD_BYTES=true' cannot be used with 'segment-children:*'"))
      }
    }
  }

  "record bytes generation with keep_original schema policy" should {
    "work for EBCDIC fixed length RDDs" in {
      val expected = """[{"Record_Bytes":"8fI=","RECORD":{"FIELD_1":"12"}},{"Record_Bytes":"8/Q=","RECORD":{"FIELD_1":"34"}}]"""
      val data = Array(Array(0xF1, 0xF2).map(_.toByte), Array(0xF3, 0xF4).map(_.toByte))

      val rdd = spark.sparkContext.parallelize(data)

      val df = Cobrix.fromRdd
        .copybookContents(copybook)
        .option("encoding", "ebcdic")
        .option("generate_record_bytes", "true")
        .option("schema_retention_policy", "keep_original")
        .load(rdd)

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(actual == expected)
    }

    "work for fixed length EBCDIC files" in {
      val expected = """[{"Record_Bytes":"8fI=","RECORD":{"FIELD_1":"12"}},{"Record_Bytes":"8/Q=","RECORD":{"FIELD_1":"34"}}]"""
      val data = Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte)

      withTempBinFile("recbytesgen", ".dat", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ebcdic")
          .option("generate_record_bytes", "true")
          .option("schema_retention_policy", "keep_original")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for variable length EBCDIC files" in {
      val expected = """[{"Record_Bytes":"8fI=","RECORD":{"FIELD_1":"12"}},{"Record_Bytes":"8/Q=","RECORD":{"FIELD_1":"34"}}]"""
      val data = Array(0x00, 0x02, 0x00, 0x00, 0xF1, 0xF2, 0x00, 0x02, 0x00, 0x00, 0xF3, 0xF4).map(_.toByte)

      withTempBinFile("recbytesgen", ".dat", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("encoding", "ebcdic")
          .option("generate_record_bytes", "true")
          .option("schema_retention_policy", "keep_original")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for ASCII files" in {
      val expected = """[{"Record_Bytes":"MTI=","RECORD":{"FIELD_1":"12"}},{"Record_Bytes":"MzQ=","RECORD":{"FIELD_1":"34"}}]"""
      val data = Array(Array(0x31, 0x32, 0x33, 0x34).map(_.toByte))

      withTempBinFile("recbytesgen", ".dat", data.head) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ascii")
          .option("generate_record_bytes", "true")
          .option("schema_retention_policy", "keep_original")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }
  }

  "record id and bytes generation" should {
    "work for fixed length EBCDIC files" in {
      val expected = """[{"File_Id":0,"Record_Id":0,"Record_Byte_Length":2,"Record_Bytes":"8fI=","FIELD_1":"12"},{"File_Id":0,"Record_Id":1,"Record_Byte_Length":2,"Record_Bytes":"8/Q=","FIELD_1":"34"}]"""
      val data = Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte)

      withTempBinFile("recbytesgen", ".dat", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ebcdic")
          .option("generate_record_id", "true")
          .option("generate_record_bytes", "true")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for variable length EBCDIC files" in {
      val expected = """[{"File_Id":0,"Record_Id":0,"Record_Byte_Length":2,"Record_Bytes":"8fI=","FIELD_1":"12"},{"File_Id":0,"Record_Id":1,"Record_Byte_Length":2,"Record_Bytes":"8/Q=","FIELD_1":"34"}]"""
      val data = Array(0x00, 0x02, 0x00, 0x00, 0xF1, 0xF2, 0x00, 0x02, 0x00, 0x00, 0xF3, 0xF4).map(_.toByte)

      withTempBinFile("recbytesgen", ".dat", data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("encoding", "ebcdic")
          .option("generate_record_id", "true")
          .option("generate_record_bytes", "true")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for ASCII files" in {
      val expected = """[{"File_Id":0,"Record_Id":0,"Record_Byte_Length":2,"Record_Bytes":"MTI=","FIELD_1":"12"},{"File_Id":0,"Record_Id":1,"Record_Byte_Length":2,"Record_Bytes":"MzQ=","FIELD_1":"34"}]"""
      val data = Array(Array(0x31, 0x32, 0x33, 0x34).map(_.toByte))

      withTempBinFile("recbytesgen", ".dat", data.head) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ascii")
          .option("generate_record_id", "true")
          .option("generate_record_bytes", "true")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }
  }
}
