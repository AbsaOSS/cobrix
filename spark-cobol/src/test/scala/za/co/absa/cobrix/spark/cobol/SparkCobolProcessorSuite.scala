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
          .withRecordProcessor (new SerializableRawRecordProcessor {
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

    "convert input format into VRL+DRW" in {
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
          .withRecordProcessor (new SerializableRawRecordProcessor {
            override def processRecord(record: Array[Byte], ctx: CobolProcessorContext): Array[Byte] = {
              record.map(v => (v - 1).toByte)
            }
          })
          .load(inputPath)
          .save(outputPath)

        val outputData = readBinaryFile(outputFile)

        assert(outputData.sameElements(
          Array(0, 0, 1, 0, -16, 0, 0, 1, 0, -15, 0, 0, 1, 0, -14, 0, 0, 1, 0, -13).map(_.toByte)
        ))

        val actual = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "V")
          .load(outputFile)
          .toJSON
          .collect()
          .mkString

        assert(actual == expected)
      }
    }
  }
}
