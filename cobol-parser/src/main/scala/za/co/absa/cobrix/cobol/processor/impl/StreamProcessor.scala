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
import za.co.absa.cobrix.cobol.processor.{CobolProcessorContext, RawRecordProcessor}
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import java.io.OutputStream

object StreamProcessor {
  /**
    * Processes a stream of COBOL raw records and writes it back in the same format as the input data.
    *
    * @param copybook        the COBOL copybook that describes the schema of the records.
    * @param options         arbitrary options used for splitting input data into records (same as 'spark-cobol' options).
    *                        Keys are lower-cased for case-insensitive handling. Can contain custom options as well.
    * @param inputStream     the input stream containing the raw COBOL records.
    * @param recordExtractor the extractor that extracts raw records from the input stream.
    * @param recordProcessor the per-record processing logic implementation.
    * @param outputStream    the output stream where the processed records will be written.
    * @return The number of records processed.
    */
  def processStreamInPlace(copybook: Copybook,
                           options: Map[String, String],
                           inputStream: SimpleStream,
                           recordExtractor: RawRecordExtractor,
                           recordProcessor: RawRecordProcessor,
                           outputStream: OutputStream): Long = {
    var recordCount = 0L
    while (recordExtractor.hasNext) {
      recordCount += 1
      val record = recordExtractor.next()
      val recordSize = record.length

      val ctx = CobolProcessorContext(copybook, options, recordExtractor.offset)

      val updatedRecord = recordProcessor.processRecord(record, ctx)

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
    recordCount
  }

  /**
    * Processes a stream of COBOL raw records and writes it back as a variable length format with big-endian RDW headers.
    *
    * @param copybook        the COBOL copybook that describes the schema of the records.
    * @param options         arbitrary options used for splitting input data into records (same as 'spark-cobol' options).
    *                        Keys are lower-cased for case-insensitive handling. Can contain custom options as well.
    * @param recordExtractor the extractor that extracts raw records from the input stream.
    * @param recordProcessor the per-record processing logic implementation.
    * @param outputStream    the output stream where the processed records will be written.
    * @return The number of records processed.
    */
  def processStreamToRdw(copybook: Copybook,
                         options: Map[String, String],
                         recordExtractor: RawRecordExtractor,
                         recordProcessor: RawRecordProcessor,
                         outputStream: OutputStream): Long = {
    var recordCount = 0L

    while (recordExtractor.hasNext) {
      recordCount += 1
      val record = recordExtractor.next()
      val recordSize = record.length

      val ctx = CobolProcessorContext(copybook, options, recordExtractor.offset)

      val updatedRecord = recordProcessor.processRecord(record, ctx)

      val rdw = Array[Byte](((updatedRecord.length >> 8) & 0xFF).toByte, ((updatedRecord.length) & 0xFF).toByte, 0, 0)

      outputStream.write(rdw)
      outputStream.write(updatedRecord)
    }

    recordCount
  }

}
