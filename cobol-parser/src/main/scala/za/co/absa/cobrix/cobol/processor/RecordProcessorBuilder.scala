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

import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat.FixedLength
import za.co.absa.cobrix.cobol.reader.VarLenNestedReader
import za.co.absa.cobrix.cobol.reader.extractors.raw.{FixedRecordLengthRawRecordExtractor, RawRecordContext, RawRecordExtractor}
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParametersParser, Parameters, ReaderParameters}
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import java.io.OutputStream
import scala.collection.mutable
import scala.reflect.ClassTag

object RecordProcessorBuilder {
  /**
    * Creates a new instance of the RecordProcessorBuilder with the given copybook content.
    * The instabce is used to create an COBOL data processor allowing applying changes to a mainrame file
    * without changing the original format.
    *
    * @param copybookContent the COBOL copybook content as a string.
    * @return a new RecordProcessorBuilder instance.
    */
  def copybookContents(copybookContent: String): RecordProcessorBuilder = {
    new RecordProcessorBuilder(copybookContent)
  }
}

class RecordProcessorBuilder(copybookContents: String) {
  private val caseInsensitiveOptions = new mutable.HashMap[String, String]()

  /**
    * Adds a single option to the builder.
    *
    * @param key   the option key.
    * @param value the option value.
    * @return this builder instance for method chaining.
    */
  def option(key: String, value: String): RecordProcessorBuilder = {
    caseInsensitiveOptions += (key.toLowerCase -> value)
    this
  }

  /**
    * Adds multiple options to the builder.
    *
    * @param options a map of option key-value pairs.
    * @return this builder instance for method chaining.
    */
  def options(options: Map[String, String]): RecordProcessorBuilder = {
    caseInsensitiveOptions ++= options.map(kv => (kv._1.toLowerCase(), kv._2))
    this
  }

  /**
    * Processes the input stream of COBOL records and writes the output to the specified output stream.
    *
    * @param inputStream        the input stream containing raw COBOL records.
    * @param outputStream       the output stream where processed records will be written.
    * @param rawRecordProcessor the processor that processes each raw record.
    */
  def process(inputStream: SimpleStream,
              outputStream: OutputStream)
             (rawRecordProcessor: RawRecordProcessor): Unit = {
    val readerParameters = getReaderParameters
    val cobolSchema = getCobolSchema(readerParameters)
    val recordExtractor = getRecordExtractor(readerParameters, inputStream)

    val dataStream = inputStream.copyStream()
    try {
      StreamProcessor.processStream(cobolSchema.copybook,
        caseInsensitiveOptions.toMap,
        dataStream,
        recordExtractor,
        rawRecordProcessor,
        outputStream)
    } finally {
      dataStream.close()
    }
  }

  private[processor] def getCobolSchema(readerParameters: ReaderParameters): CobolSchema = {
    CobolSchema.fromReaderParameters(Seq(copybookContents), readerParameters)
  }

  private[processor] def getReaderParameters: ReaderParameters = {
    val cobolParameters = CobolParametersParser.parse(new Parameters(caseInsensitiveOptions.toMap))

    CobolParametersParser.getReaderProperties(cobolParameters, None)
  }

  private[processor] def getRecordExtractor(readerParameters: ReaderParameters, inputStream: SimpleStream): RawRecordExtractor = {
    val dataStream = inputStream.copyStream()
    val headerStream = inputStream.copyStream()

    val reader = new VarLenNestedReader[Array[Any]](Seq(copybookContents), readerParameters, new ArrayOfAnyHandler)

    reader.recordExtractor(0, dataStream, headerStream) match {
      case Some(extractor) => extractor
      case None if readerParameters.recordFormat == FixedLength =>
        val dataStream = inputStream.copyStream()
        val headerStream = inputStream.copyStream()
        val ctx = RawRecordContext(0, dataStream, headerStream, getCobolSchema(readerParameters).copybook, null, null, "")
        new FixedRecordLengthRawRecordExtractor(ctx, readerParameters.recordLength)
      case None =>
        throw new IllegalArgumentException(s"Cannot create a record extractor for the given reader parameters. " +
            "Please check the copybook and the reader parameters."
        )
    }
  }

  private[processor] def getOptions: Map[String, String] = caseInsensitiveOptions.toMap
}
