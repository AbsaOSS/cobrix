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
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.processor.{CobolProcessor, SerializableRawRecordProcessor}
import za.co.absa.cobrix.spark.cobol.source.SerializableConfiguration
import za.co.absa.cobrix.spark.cobol.source.streaming.FileStreamer
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

import java.io.BufferedOutputStream
import java.util.concurrent.{ExecutorService, Executors}
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

trait SparkCobolProcessor {
  /**
    * Runs raw record processing and returns the record count processed.
    *
    * @param listOfFiles A sequence of input file paths to process.
    * @param outputPath  The path where processed records will be written.
    * @return The number of records that were processed
    */
  def process(listOfFiles: Seq[String], outputPath: String): Long
}

object SparkCobolProcessor {
  private val log = LoggerFactory.getLogger(this.getClass)

  class SparkCobolProcessorBuilder(implicit spark: SparkSession) {
    private val caseInsensitiveOptions = new mutable.HashMap[String, String]()
    private var copybookContentsOpt: Option[String] = None
    private var rawRecordProcessorOpt: Option[SerializableRawRecordProcessor] = None
    private var numberOfThreads: Int = 1

    def load(path: String): SparkCobolProcessorLoader = {
      val filePaths = FileUtils
        .getFiles(path, spark.sparkContext.hadoopConfiguration)

      load(filePaths)
    }

    def load(filePaths: Seq[String]): SparkCobolProcessorLoader = {
      if (copybookContentsOpt.isEmpty) {
        throw new IllegalArgumentException("Copybook contents must be provided.")
      }

      if (rawRecordProcessorOpt.isEmpty) {
        throw new IllegalArgumentException("A RawRecordProcessor must be provided.")
      }

      if (numberOfThreads < 1) {
        throw new IllegalArgumentException("Number of threads must be at least 1.")
      }

      if (filePaths.isEmpty) {
        throw new IllegalArgumentException("At least one input file must be provided.")
      }

      new SparkCobolProcessorLoader(filePaths, copybookContentsOpt.get, rawRecordProcessorOpt.get, numberOfThreads, caseInsensitiveOptions.toMap)
    }

    def withCopybookContents(copybookContents: String): SparkCobolProcessorBuilder = {
      copybookContentsOpt = Option(copybookContents)
      this
    }

    def withRecordProcessor(processor: SerializableRawRecordProcessor): SparkCobolProcessorBuilder = {
      rawRecordProcessorOpt = Option(processor)
      this
    }

    def withMultithreaded(numberOfThreads: Int): SparkCobolProcessorBuilder = {
      if (numberOfThreads < 1) {
        throw new IllegalArgumentException("Number of threads must be at least 1.")
      }
      this.numberOfThreads = numberOfThreads
      this
    }

    /**
      * Adds a single option to the builder.
      *
      * @param key   the option key.
      * @param value the option value.
      * @return this builder instance for method chaining.
      */
    def option(key: String, value: String): SparkCobolProcessorBuilder = {
      require(key.trim.nonEmpty, "Option key must not be empty or whitespace-only")
      caseInsensitiveOptions += (key.trim.toLowerCase -> value)
      this
    }

    /**
      * Adds multiple options to the builder.
      *
      * @param options a map of option key-value pairs.
      * @return this builder instance for method chaining.
      */
    def options(options: Map[String, String]): SparkCobolProcessorBuilder = {
      caseInsensitiveOptions ++= options.map(kv => (kv._1.toLowerCase(), kv._2))
      this
    }
  }

  class SparkCobolProcessorLoader(filesToRead: Seq[String],
                                  copybookContents: String,
                                  rawRecordProcessor: SerializableRawRecordProcessor,
                                  numberOfThreads: Int,
                                  options: Map[String, String])
                                 (implicit spark: SparkSession) {
    def save(outputPath: String): Long = {
      val cobolProcessor = CobolProcessor.builder
        .withCopybookContents(copybookContents)
        .options(options)
        .build()

      val processor = new SparkCobolProcessor {
        private val sconf = new SerializableConfiguration(spark.sparkContext.hadoopConfiguration)

        override def process(listOfFiles: Seq[String], outputPath: String): Long = {
          getFileProcessorRdd(listOfFiles, outputPath, copybookContents, cobolProcessor, rawRecordProcessor, sconf, numberOfThreads)
            .reduce(_ + _)
        }
      }

      log.info(s"Writing to $outputPath...")
      processor.process(filesToRead, outputPath)
    }
  }

  def builder(implicit spark: SparkSession): SparkCobolProcessorBuilder = {
    new SparkCobolProcessorBuilder
  }

  private def getFileProcessorRdd(listOfFiles: Seq[String],
                                  outputPath: String,
                                  copybookContents: String,
                                  cobolProcessor: CobolProcessor,
                                  rawRecordProcessor: SerializableRawRecordProcessor,
                                  sconf: SerializableConfiguration,
                                  numberOfThreads: Int
                                 )(implicit spark: SparkSession): RDD[Long] = {
    val groupedFiles = listOfFiles.grouped(numberOfThreads).toSeq
    val rdd = spark.sparkContext.parallelize(groupedFiles)
    rdd.map(group => {
      processListOfFiles(group, outputPath, copybookContents, cobolProcessor, rawRecordProcessor, sconf, numberOfThreads)
    })
  }

  private def processListOfFiles(listOfFiles: Seq[String],
                                 outputPath: String,
                                 copybookContents: String,
                                 cobolProcessor: CobolProcessor,
                                 rawRecordProcessor: SerializableRawRecordProcessor,
                                 sconf: SerializableConfiguration,
                                 numberOfThreads: Int
                                ): Long = {
    val threadPool: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(threadPool)

    val futures = listOfFiles.map { inputFIle =>
      val fileName = new Path(inputFIle).getName
      val outputPathFileName = new Path(outputPath, fileName).toString

      log.info(s"Processing file: $inputFIle -> $outputPathFileName")

      Future {
        val hadoopConfig = sconf.value
        val inputFs = new Path(inputFIle).getFileSystem(hadoopConfig)
        val ifs = new FileStreamer(inputFIle, inputFs)
        val outputFile = new Path(outputPath, fileName)
        val outputFs = outputFile.getFileSystem(hadoopConfig)
        val ofs = new BufferedOutputStream(outputFs.create(outputFile, true))

        var originalException: Throwable = null

        val recordCount = try {
          cobolProcessor.process(ifs, ofs)(rawRecordProcessor)
        } catch {
          case ex: Throwable =>
            originalException = ex
            0L
        } finally {
          // Ugly code to ensure no exceptions escape unnoticed.
          try {
            ifs.close()
          } catch {
            case e: Throwable =>
              if (originalException != null) {
                originalException.addSuppressed(e)
              } else {
                originalException = e
              }
          }

          try {
            ofs.close()
          } catch {
            case e: Throwable =>
              if (originalException != null) {
                originalException.addSuppressed(e)
              } else {
                originalException = e
              }
          }
        }

        if (originalException != null) throw originalException

        log.info(s"Writing to $outputFile succeeded!")
        recordCount
      }
    }.toSeq

    val seq = Future.sequence(futures)

    val recordCuntProcessed = try {
      Await.result(seq, Duration.Inf).sum
    } finally {
      threadPool.shutdown()
    }

    recordCuntProcessed
  }
}
