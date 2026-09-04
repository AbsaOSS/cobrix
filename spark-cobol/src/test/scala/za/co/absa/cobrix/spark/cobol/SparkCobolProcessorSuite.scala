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

package za.co.absa.cobrix.spark.cobol

import org.apache.hadoop.fs.Path
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.processor.{CobolProcessingStrategy, CobolProcessorContext, SerializableRawRecordProcessor}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.{BinaryFileFixture, TextComparisonFixture}
import za.co.absa.cobrix.spark.cobol.utils.SparkUtils

class SparkCobolProcessorSuite extends AnyWordSpec with SparkTestBase with BinaryFileFixture with TextComparisonFixture {
  private val copybook =
    """      01 RECORD.
      |         05  T     PIC X.
      |""".stripMargin

  private val rawRecordProcessor = new SerializableRawRecordProcessor {
    override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
      record.map(v => (v - 1).toByte)
    }
  }

  "SparkCobolProcessor" should {
    "fail to create when a copybook is not specified" in {
      val exception = intercept[IllegalArgumentException] {
        SparkCobolProcessor.builder.load(".")
      }

      assert(exception.getMessage.contains("Copybook contents must be provided."))
    }

    "fail to create when a record processor is not provided" in {
      val exception = intercept[IllegalArgumentException] {
        SparkCobolProcessor.builder
          .withCopybookContents(copybook).load(".")
          .save("ignored")
      }

      assert(exception.getMessage.contains("A RawRecordProcessor must be provided."))
    }

    "fail to create when the number of threads is less than 0" in {
      val exception = intercept[IllegalArgumentException] {
        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .withRecordProcessor(rawRecordProcessor)
          .withMultithreaded(0)
          .load("")
      }

      assert(exception.getMessage.contains("Number of threads must be at least 1."))
    }

    "fail when no files are provided" in {
      val exception = intercept[IllegalArgumentException] {
        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .withRecordProcessor(rawRecordProcessor)
          .load(Seq.empty)
      }

      assert(exception.getMessage.contains("At least one input file must be provided."))
    }

    "process files via an RDD" in {
      withTempDirectory("spark_cobol_processor") { tempDir =>
        val binData = Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte)

        val inputPath = new Path(tempDir, "input.dat").toString
        val outputPath = new Path(tempDir, "output").toString
        val outputFile = new Path(outputPath, "input.dat").toString

        writeBinaryFile(inputPath, binData)

        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .withRecordProcessor(new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.length == binData.length)
        assert(outputData.head == 0xF0.toByte)
        assert(outputData(1) == 0xF1.toByte)
        assert(outputData(2) == 0xF2.toByte)
        assert(outputData(3) == 0xF3.toByte)
      }
    }

    "convert input format into VRL+RDW" in {
      val expected = """{"T":"0"}{"T":"1"}{"T":"2"}{"T":"3"}"""
      withTempDirectory("spark_cobol_processor") { tempDir =>
        val binData = Array(0xF1, 0xF2, 0xF3, 0xF4).map(_.toByte)

        val inputPath = new Path(tempDir, "input.dat").toString
        val outputPath = new Path(tempDir, "output").toString
        val outputFile = new Path(outputPath, "input.dat").toString

        writeBinaryFile(inputPath, binData)

        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
          .withRecordProcessor(new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.sameElements(
          Array(0, 1, 0, 0, -16, 0, 1, 0, 0, -15, 0, 1, 0, 0, -14, 0, 1, 0, 0, -13).map(_.toByte)
        ))

        val actual = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("pedantic", "true")
          .load(outputFile)
          .toJSON
          .collect()
          .mkString

        assert(actual == expected)
      }
    }

    "convert from VRL+RDW into VRL+RDW simple" in {
      val expected = """{"T":"0"}{"T":"1"}{"T":"2"}{"T":"3"}"""
      withTempDirectory("spark_cobol_processor") { tempDir =>
        val binData = Array(
          0x00, 0x00, 0x02, 0x00, 0xF1,
          0x00, 0x00, 0x02, 0x00, 0xF2,
          0x00, 0x00, 0x02, 0x00, 0xF3,
          0x00, 0x00, 0x02, 0x00, 0xF4).map(_.toByte)

        val inputPath = new Path(tempDir, "input.dat").toString
        val outputPath = new Path(tempDir, "output").toString
        val outputFile = new Path(outputPath, "input.dat").toString

        writeBinaryFile(inputPath, binData)

        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .option("record_format", "V")
          .option("rdw_adjustment", "-1")
          .option("is_rdw_big_endian", "false")
          .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
          .withRecordProcessor(new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.sameElements(
          Array(0, 1, 0, 0, -16, 0, 1, 0, 0, -15, 0, 1, 0, 0, -14, 0, 1, 0, 0, -13).map(_.toByte)
        ))

        val actual = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("pedantic", "true")
          .load(outputFile)
          .toJSON
          .collect()
          .mkString

        assert(actual == expected)
      }
    }

    "convert from VRL+RDW into VRL+RDW with RDW part of record length" in {
      val expected = """{"T":"0"}{"T":"1"}{"T":"2"}{"T":"3"}"""
      withTempDirectory("spark_cobol_processor") { tempDir =>
        val binData = Array(
          0x00, 0x00, 0x05, 0x00, 0xF1,
          0x00, 0x00, 0x05, 0x00, 0xF2,
          0x00, 0x00, 0x05, 0x00, 0xF3,
          0x00, 0x00, 0x05, 0x00, 0xF4).map(_.toByte)

        val inputPath = new Path(tempDir, "input.dat").toString
        val outputPath = new Path(tempDir, "output").toString
        val outputFile = new Path(outputPath, "input.dat").toString

        writeBinaryFile(inputPath, binData)

        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .option("record_format", "V")
          .option("is_rdw_part_of_record_length", "true")
          .option("is_rdw_big_endian", "false")
          .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
          .withRecordProcessor(new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.sameElements(
          Array(0, 1, 0, 0, -16, 0, 1, 0, 0, -15, 0, 1, 0, 0, -14, 0, 1, 0, 0, -13).map(_.toByte)
        ))

        val actual = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("pedantic", "true")
          .load(outputFile)
          .toJSON
          .collect()
          .mkString

        assert(actual == expected)
      }
    }

    "support file_start_offset and file_end_offset with InPlace strategy" in {
      val expected = """{"T":"0"}{"T":"1"}{"T":"2"}{"T":"3"}"""
      withTempDirectory("spark_cobol_processor") { tempDir =>
        val binData = Array(0x07, 0x07, 0x07, 0xF1, 0xF2, 0xF3, 0xF4, 0x08, 0x08).map(_.toByte)

        val inputPath = new Path(tempDir, "input.dat").toString
        val outputPath = new Path(tempDir, "output").toString
        val outputFile = new Path(outputPath, "input.dat").toString

        writeBinaryFile(inputPath, binData)

        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .withProcessingStrategy(CobolProcessingStrategy.InPlace)
          .withRecordProcessor(new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .option("file_start_offset", "3")
          .option("file_end_offset", "2")
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.sameElements(
          Array(7, 7, 7, -16, -15, -14, -13, 8, 8).map(_.toByte)
        ))

        val actual = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "F")
          .option("file_start_offset", "3")
          .option("file_end_offset", "2")
          .option("pedantic", "true")
          .load(outputFile)
          .toJSON
          .collect()
          .mkString

        assert(actual == expected)
      }
    }

    "support file_start_offset and file_end_offset with ToVariableLength strategy" in {
      val expected = """{"T":"0"}{"T":"1"}{"T":"2"}{"T":"3"}"""
      withTempDirectory("spark_cobol_processor") { tempDir =>
        val binData = Array(0x07, 0x07, 0x07, 0xF1, 0xF2, 0xF3, 0xF4, 0x08, 0x08).map(_.toByte)

        val inputPath = new Path(tempDir, "input.dat").toString
        val outputPath = new Path(tempDir, "output").toString
        val outputFile = new Path(outputPath, "input.dat").toString

        writeBinaryFile(inputPath, binData)

        SparkCobolProcessor.builder
          .withCopybookContents(copybook)
          .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
          .withRecordProcessor(new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .option("file_start_offset", "3")
          .option("file_end_offset", "2")
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.sameElements(
          Array(0, 1, 0, 0, -16, 0, 1, 0, 0, -15, 0, 1, 0, 0, -14, 0, 1, 0, 0, -13).map(_.toByte)
        ))

        val actual = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .option("is_rdw_big_endian", "true")
          .option("pedantic", "true")
          .load(outputFile)
          .toJSON
          .collect()
          .mkString

        assert(actual == expected)
      }
    }
  }

  "support files with redefine rules" in {
    val copybookStr: String =
      """      01 RECORD.
        |        05   SEGMENT-ID   PIC X(1).
        |        05   SEG1.
        |           10 RT1      PIC X(1).
        |           10 V11      PIC X(3).
        |           10 V12      PIC 9(3) REDEFINES V11.
        |        05   SEG2 REDEFINES SEG1.
        |           10 RT2      PIC X(1).
        |           10 V21      PIC X(3).
        |           10 V22      PIC 9(3) REDEFINES V21.
        |        05   SEG3 REDEFINES SEG1.
        |           10 CNT      PIC 9(1).
        |           10 AR       PIC X(1) OCCURS 5 TIMES
        |                            DEPENDING ON CNT.
        |           10 RT3      PIC X(1).
        |           10 V31      PIC X(3).
        |           10 V32      PIC 9(3) REDEFINES V31.
        |""".stripMargin

    val binData: Array[Byte] = Array(
      0x00, 0x00, 0x05, 0x00, 0xF1, 0xF1, 0xC1, 0xC2, 0xC3,             // exampleRecordSeg1Type1
      0x00, 0x00, 0x05, 0x00, 0xF1, 0xF2, 0xF1, 0xF2, 0xF3,             // exampleRecordSeg1Type2
      0x00, 0x00, 0x05, 0x00, 0xF2, 0xF1, 0xC4, 0xC5, 0xC6,             // exampleRecordSeg2Type1
      0x00, 0x00, 0x05, 0x00, 0xF2, 0xF2, 0xF4, 0xF5, 0xF6,             // exampleRecordSeg2Type2
      0x00, 0x00, 0x07, 0x00, 0xF3, 0xF1, 0xC1, 0xF1, 0xC1, 0xC2, 0xC3, // exampleRecordSeg3Type1Shift
      0x00, 0x00, 0x07, 0x00, 0xF3, 0xF1, 0x81, 0xF2, 0xF5, 0xF6, 0xF7  // exampleRecordSeg3Type2Shift
    ).map(_.toByte)

    val expected =
    """[ {
      |  "SEGMENT_ID" : "1",
      |  "SEG1" : {
      |    "RT1" : "1",
      |    "V11" : "aBC"
      |  }
      |}, {
      |  "SEGMENT_ID" : "1",
      |  "SEG1" : {
      |    "RT1" : "2",
      |    "V12" : 124
      |  }
      |}, {
      |  "SEGMENT_ID" : "2",
      |  "SEG2" : {
      |    "RT2" : "1",
      |    "V21" : "dEF"
      |  }
      |}, {
      |  "SEGMENT_ID" : "2",
      |  "SEG2" : {
      |    "RT2" : "2",
      |    "V22" : 457
      |  }
      |}, {
      |  "SEGMENT_ID" : "3",
      |  "SEG3" : {
      |    "CNT" : 1,
      |    "AR" : [ "A" ],
      |    "RT3" : "1",
      |    "V31" : "ABC"
      |  }
      |}, {
      |  "SEGMENT_ID" : "3",
      |  "SEG3" : {
      |    "CNT" : 1,
      |    "AR" : [ "a" ],
      |    "RT3" : "2",
      |    "V32" : 567
      |  }
      |} ]
      |""".stripMargin
    withTempDirectory("spark_cobol_processor") { tempDir =>
      val inputPath = new Path(tempDir, "input.dat").toString
      val outputPath = new Path(tempDir, "output").toString
      val outputFile = new Path(outputPath, "input.dat").toString

      writeBinaryFile(inputPath, binData)

      val options = Map(
        "segment_field" -> "SEGMENT_ID",
        "variable_size_occurs" -> "true",
        "redefine_segment_id_map:1" -> "SEG1 => 1",
        "redefine_segment_id_map:2" -> "SEG2 => 2",
        "redefine_segment_id_map:3" -> "SEG3 => 3",
        "redefine-rule:1" -> "V11 => RT1 = '1'",
        "redefine-rule:2" -> "V12 => RT1 = '2'",
        "redefine-rule:3" -> "V21 => RT2 = '1'",
        "redefine-rule:4" -> "V22 => RT2 = '2'",
        "redefine-rule:5" -> "V31 => RT3 = '1'",
        "redefine-rule:6" -> "V32 => RT3 = '2'"
      )

      SparkCobolProcessor.builder
        .withCopybookContents(copybookStr)
        .option("record_format", "V")
        .option("is_rdw_big_endian", "false")
        .options(options)
        .withProcessingStrategy(CobolProcessingStrategy.ToVariableLength)
        .withRecordProcessor(new SerializableRawRecordProcessor {
          override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
            val segIdValue = ctx.copybook.getFieldValueByName("SEGMENT_ID", record).toString
            val vars = ctx.copybook.extractExpressionVariablesFromRecord(record, Option(segIdValue))

            segIdValue match {
              case "1" =>
                val isV11Enabled = ctx.copybook.isFieldEnabled(ctx.copybook.getFieldByName("SEG1.V11"), Some("1"), vars)
                val isV12Enabled = ctx.copybook.isFieldEnabled(ctx.copybook.getFieldByName("SEG1.V12"), Some("1"), vars)
                if (isV11Enabled) {
                  val v11 = ctx.copybook.getFieldValueByName("SEG1.V11", record).toString
                  val v11Updated = s"${v11.head.toLower}${v11(1)}${v11{2}}"
                  ctx.copybook.setFieldValueByName("SEG1.V11", record, v11Updated)
                }
                if (isV12Enabled) {
                  val v12 = ctx.copybook.getFieldValueByName("SEG1.V12", record).asInstanceOf[Int]
                  val v12Updated = v12 + 1
                  ctx.copybook.setFieldValueByName("SEG1.V12", record, v12Updated)
                }
              case "2" =>
                val isV21Enabled = ctx.copybook.isFieldEnabled(ctx.copybook.getFieldByName("SEG2.V21"), Some("2"), vars)
                val isV22Enabled = ctx.copybook.isFieldEnabled(ctx.copybook.getFieldByName("SEG2.V22"), Some("2"), vars)
                if (isV21Enabled) {
                  val v21 = ctx.copybook.getFieldValueByName("SEG2.V21", record).toString
                  val v21Updated = s"${v21.head.toLower}${v21(1)}${v21{2}}"
                  ctx.copybook.setFieldValueByName("SEG2.V21", record, v21Updated)
                }
                if (isV22Enabled) {
                  val v22 = ctx.copybook.getFieldValueByName("SEG2.V22", record).asInstanceOf[Int]
                  val v22Updated = v22 + 1
                  ctx.copybook.setFieldValueByName("SEG2.V22", record, v22Updated)
                }
              case "3" =>
                val isV31Enabled = ctx.copybook.isFieldEnabled(ctx.copybook.getFieldByName("SEG3.V31"), Some("3"), vars)
                val isV32Enabled = ctx.copybook.isFieldEnabled(ctx.copybook.getFieldByName("SEG3.V32"), Some("3"), vars)
                if ((isV31Enabled && isV32Enabled) || (!isV31Enabled && !isV32Enabled))
                  throw new IllegalArgumentException("Unexpected condition for redefine rules of segment 3. Only one should be valid")
              case _ => throw new IllegalArgumentException(s"Unexpected segment id: $segIdValue")
            }

            record
          }
        })
        .load(inputPath)
        .save(outputPath)

      val outputData = readBinaryFile(outputFile)

      assert(outputData.sameElements(
        Array(0, 5, 0, 0, -15, -15, -127, -62, -61, 0, 5, 0, 0, -15, -14, -15, -14, -12, 0, 5, 0, 0,
          -14, -15, -124, -59, -58, 0, 5, 0, 0, -14, -14, -12, -11, -9, 0, 7, 0, 0, -13, -15, -63,
          -15, -63, -62, -61, 0, 7, 0, 0, -13, -15, -127, -14, -11, -10, -9).map(_.toByte)
      ))

      val actualDf = spark.read
        .format("cobol")
        .option("copybook_contents", copybookStr)
        .option("record_format", "V")
        .option("is_rdw_big_endian", "true")
        .option("pedantic", "true")
        .options(options)
        .load(outputFile)

      val actual = SparkUtils.convertDataFrameToPrettyJSON(actualDf)

      compareText(actual, expected)
    }
  }


  "convert input format into an RDD without indexes" in {
    val expected = """-13, -14, -15"""
    withTempDirectory("spark_cobol_processor") { tempDir =>
      val binData = Array(0xF1, 0xF2, 0xF3, 0xF1).map(_.toByte)

      val inputPath = new Path(tempDir, "input.dat").toString
      val outputPath = new Path(tempDir, "output").toString

      writeBinaryFile(inputPath, binData)

      val rddBuilder = SparkCobolProcessor.builder
        .withCopybookContents(copybook)
        .option("enable_indexes", "false")
        .load(inputPath)

      val parsedCopybook = rddBuilder.getParsedCopybook
      val rdd = rddBuilder.toRDD

      val count = rdd.count()

      assert(count == 4)

      val actual = rdd
        .map(row => row.mkString)
        .distinct
        .sortBy(x => x)
        .collect().mkString(", ")

      assert(parsedCopybook.ast.children.length == 1)
      assert(actual == expected)
    }
  }

  "convert input format into an RDD with index" in {
    val expected = """-10, -11, -12, -13, -14, -15"""
    withTempDirectory("spark_cobol_processor") { tempDir =>
      val binData = Array(0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF1).map(_.toByte)

      val inputPath = new Path(tempDir, "input.dat").toString

      writeBinaryFile(inputPath, binData)

      val rdd = SparkCobolProcessor.builder
        .withCopybookContents(copybook)
        .option("enable_indexes", "true")
        .option("input_split_records", "2")
        .load(inputPath)
        .toRDD

      val count = rdd.count()

      assert(count == 7)

      val actual = rdd
        .map(row => row.mkString)
        .distinct
        .sortBy(x => x)
        .collect().mkString(", ")

      assert(actual == expected)
    }
  }
}
