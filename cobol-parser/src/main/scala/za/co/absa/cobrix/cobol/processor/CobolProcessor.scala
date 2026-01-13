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

package za.co.absa.cobrix.cobol.processor

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.processor.impl.{CobolProcessorInPlace, CobolProcessorToRdw}
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParametersParser, Parameters, ReaderParameters}
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema
import za.co.absa.cobrix.cobol.reader.stream.{FSStream, SimpleStream}
import za.co.absa.cobrix.cobol.utils.UsingUtils

import java.io.{BufferedOutputStream, FileOutputStream, OutputStream}
import scala.collection.mutable


/**
  * A trait that defines a processor for raw COBOL data streams.
  * It provides a method to process a COBOL file or a stream, provided record processor.
  */
trait CobolProcessor {
  /**
    * Processes the input stream of COBOL records and writes the output to the specified output stream.
    *
    * @param inputStream        the input stream containing raw COBOL records.
    * @param outputStream       the output stream where processed records will be written.
    * @param rawRecordProcessor the processor that processes each raw record.
    * @return The number of records processed.
    */
  def process(inputStream: SimpleStream,
              outputStream: OutputStream)
             (rawRecordProcessor: RawRecordProcessor): Long

}

object CobolProcessor {
  class CobolProcessorBuilder {
    private val caseInsensitiveOptions = new mutable.HashMap[String, String]()
    private var copybookContentsOpt: Option[String] = None
    private var rawRecordProcessorOpt: Option[RawRecordProcessor] = None
    private var cobolProcessingStrategy: CobolProcessingStrategy = CobolProcessingStrategy.InPlace

    def build(): CobolProcessor = {
      if (copybookContentsOpt.isEmpty) {
        throw new IllegalArgumentException("Copybook contents must be provided.")
      }

      val readerParameters = getReaderParameters
      val cobolSchema = getCobolSchema(readerParameters)

      cobolProcessingStrategy match {
        case CobolProcessingStrategy.InPlace => new CobolProcessorInPlace(readerParameters, cobolSchema.copybook, copybookContentsOpt.get, caseInsensitiveOptions.toMap)
        case CobolProcessingStrategy.ToVariableLength => new CobolProcessorToRdw(readerParameters, cobolSchema.copybook, copybookContentsOpt.get, caseInsensitiveOptions.toMap)
      }
    }

    def load(path: String): CobolProcessorLoader = {
      val file = new java.io.File(path)
      if (!file.exists) {
        throw new IllegalArgumentException(s"Path $path does not exist.")
      }

      if (file.isDirectory) {
        throw new IllegalArgumentException(s"Path $path should be a file, not a directory.")
      }

      if (copybookContentsOpt.isEmpty) {
        throw new IllegalArgumentException("Copybook contents must be provided.")
      }

      if (rawRecordProcessorOpt.isEmpty) {
        throw new IllegalArgumentException("A RawRecordProcessor must be provided.")
      }

      if (rawRecordProcessorOpt.isEmpty) {
        throw new IllegalArgumentException("A RawRecordProcessor must be provided.")
      }

      val readerParameters = getReaderParameters
      val cobolSchema = getCobolSchema(readerParameters)

      new CobolProcessorLoader(path, copybookContentsOpt.get, cobolSchema.copybook, rawRecordProcessorOpt.get, readerParameters, cobolProcessingStrategy, caseInsensitiveOptions.toMap)
    }

    def withCopybookContents(copybookContents: String): CobolProcessorBuilder = {
      copybookContentsOpt = Option(copybookContents)
      this
    }

    def withRecordProcessor(processor: RawRecordProcessor): CobolProcessorBuilder = {
      rawRecordProcessorOpt = Option(processor)
      this
    }

    def withProcessingStrategy(strategy: CobolProcessingStrategy): CobolProcessorBuilder = {
      cobolProcessingStrategy = strategy
      this
    }

    /**
      * Adds a single option to the builder.
      *
      * @param key   the option key.
      * @param value the option value.
      * @return this builder instance for method chaining.
      */
    def option(key: String, value: String): CobolProcessorBuilder = {
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
    def options(options: Map[String, String]): CobolProcessorBuilder = {
      caseInsensitiveOptions ++= options.map(kv => (kv._1.toLowerCase(), kv._2))
      this
    }

    private[processor] def getCobolSchema(readerParameters: ReaderParameters): CobolSchema = {
      CobolSchema.fromReaderParameters(Seq(copybookContentsOpt.get), readerParameters)
    }

    private[processor] def getReaderParameters: ReaderParameters = {
      val cobolParameters = CobolParametersParser.parse(new Parameters(caseInsensitiveOptions.toMap))

      CobolParametersParser.getReaderProperties(cobolParameters, None)
    }

    private[processor] def getOptions: Map[String, String] = caseInsensitiveOptions.toMap
  }

  class CobolProcessorLoader(fileToProcess: String,
                             copybookContents: String,
                             copybook: Copybook,
                             rawRecordProcessor: RawRecordProcessor,
                             readerParameters: ReaderParameters,
                             cobolProcessingStrategy: CobolProcessingStrategy,
                             options: Map[String, String]) {
    def save(outputFile: String): Long = {
      val processor = cobolProcessingStrategy match {
        case CobolProcessingStrategy.InPlace => new CobolProcessorInPlace(readerParameters, copybook, copybookContents, options)
        case CobolProcessingStrategy.ToVariableLength => new CobolProcessorToRdw(readerParameters, copybook, copybookContents, options)
      }

      val recordCount = UsingUtils.using(new FSStream(fileToProcess)) { ifs =>
        UsingUtils.using(new BufferedOutputStream(new FileOutputStream(outputFile))) { ofs =>
          processor.process(ifs, ofs)(rawRecordProcessor)
        }
      }

      recordCount
    }
  }

  def builder: CobolProcessorBuilder = {
    new CobolProcessorBuilder
  }
}
