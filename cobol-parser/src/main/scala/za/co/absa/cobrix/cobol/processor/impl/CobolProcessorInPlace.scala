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

package za.co.absa.cobrix.cobol.processor.impl

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.processor.{CobolProcessor, RawRecordProcessor}
import za.co.absa.cobrix.cobol.reader.VarLenNestedReader
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import java.io.OutputStream

/**
  * Implementation of the CobolProcessor trait, responsible for processing COBOL data streams
  * by extracting records and applying a user-defined raw record processor. This processor
  * retains the original COBOL data format in the output.
  *
  * Please, do not use this class directly. Use `CobolProcessor.builder()` instead.
  *
  * @param readerParameters Configuration for record extraction and COBOL file parsing.
  * @param copybook         The copybook definition used for interpreting COBOL data structures.
  * @param copybookContents The raw textual representation of the copybook.
  * @param options          A map of processing options to customize the behavior of the processor (same as for `spark-cobol`).
  */
class CobolProcessorInPlace(readerParameters: ReaderParameters,
                            copybook: Copybook,
                            copybookContents: String,
                            options: Map[String, String]) extends CobolProcessorBase {
  override def process(inputStream: SimpleStream,
                       outputStream: OutputStream)
                      (rawRecordProcessor: RawRecordProcessor): Long = {
    val recordExtractor = CobolProcessorBase.getRecordExtractor(readerParameters, copybookContents, inputStream, None)

    val dataStream = inputStream.copyStream()
    try {
      StreamProcessor.processStreamInPlace(copybook,
        options,
        dataStream,
        recordExtractor,
        rawRecordProcessor,
        outputStream)
    } finally {
      dataStream.close()
    }
  }


}
