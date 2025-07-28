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
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import java.io.OutputStream

object StreamProcessor {
  /**
    * Processes a stream of COBOL raw records and writes it back in the same format as the input data.
    *
    * @param copybook the COBOL copybook that describes the schema of the records.
    * @param options arbitrary options used for splitting input data into records. Same as options to 'spark-cobol'. Can contain custom options as well.
    * @param inputStream the input stream containing the raw COBOL records.
    * @param recordExtractor the extractor that extracts raw records from the input stream.
    * @param recordProcessor the per-record processing logic implementation.
    * @param outputStream the output stream where the processed records will be written.
    */
  def processStream(copybook: Copybook,
                    options: Map[String, String],
                    inputStream: SimpleStream,
                    recordExtractor: RawRecordExtractor,
                    recordProcessor: RawRecordProcessor,
                    outputStream: OutputStream): Unit = {
    var i = 0
    while (recordExtractor.hasNext) {
      i += 1
      val record = recordExtractor.next()
      val recordSize = record.length

      val updatedRecord = recordProcessor.processRecord(copybook, options, record, recordExtractor.offset)

      val headerSize = recordExtractor.offset - recordSize - inputStream.offset
      if (headerSize > 0) {
        val header = inputStream.next(headerSize.toInt)
        outputStream.write(header)
      }
      inputStream.next(recordSize)
      outputStream.write(updatedRecord)
    }

    val footerSize = inputStream.size - inputStream.offset
    if (footerSize > 0) {
      val footer = inputStream.next(footerSize.toInt)
      outputStream.write(footer)
    }
  }
}
