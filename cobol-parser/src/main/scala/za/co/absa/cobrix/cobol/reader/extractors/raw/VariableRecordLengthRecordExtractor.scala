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

package za.co.absa.cobrix.cobol.reader.extractors.raw

import za.co.absa.cobrix.cobol.parser.headerparsers.RecordHeaderParserRDW
import za.co.absa.cobrix.cobol.reader.recordheader.RecordHeaderDecoderRdw


/**
  * A [[RawRecordExtractor]] that reads variable-length records from an input stream
  * using RDW (Record Descriptor Word) headers.
  *
  * Each record is preceded by a 4-byte RDW header that encodes the length of the
  * following payload. Invalid or zero-length RDW headers are skipped until a valid
  * record is found or the stream is exhausted.
  *
  * The header stream is closed immediately on construction since this extractor only
  * needs to read forward through the input stream.
  *
  * @param ctx The raw record context providing the input stream, RDW decoder, and other
  *            reading parameters.
  */
class VariableRecordLengthRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private var currentRecord: Option[Array[Byte]] = None
  private var recordOffset: Long = ctx.inputStream.offset
  private val rdwParams = ctx.rdwDecoder.asInstanceOf[RecordHeaderDecoderRdw].rdwParameters
  private val rdwAdjustment = if (rdwParams.headersPartOfRecordLength) rdwParams.adjustment - 4 else rdwParams.adjustment

  /** Returns the byte offset of the next record in the input stream. */
  override def offset: Long = recordOffset

  /** Always returns `true`, since variable-length RDW records can always be split at record boundaries. */
  override def canSplitHere: Boolean = true

  /**
    * Returns `true` if there is at least one more record available in the input stream.
    *
    * If no record has been pre-fetched yet, this method attempts to read the next record
    * from the stream before returning.
    */
  override def hasNext: Boolean = {
    if (currentRecord.isEmpty) {
      readNextRecord()
    }
    currentRecord.nonEmpty
  }


  /**
    * Returns the next raw record as an array of bytes and advances the stream position.
    *
    * Updates [[recordOffset]] to reflect the position of the record that will be returned
    * by the subsequent call to `next()`.
    *
    * @throws NoSuchElementException if there are no more records in the stream.
    * @return The raw bytes of the next record (excluding the RDW header).
    */
  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }

    val record = currentRecord.get
    recordOffset += ctx.rdwDecoder.headerSize + record.length

    currentRecord = None
    record
  }

  /**
    * Reads the next record from the input stream by parsing the RDW header and extracting
    * the subsequent payload.
    *
    * Skips over RDW headers that are marked as invalid. Stops when a valid record is found
    * or the end of stream is reached.
    */
  private def readNextRecord(): Unit = {
    val rdwSize = ctx.rdwDecoder.headerSize

    var valid = false

    while (!valid && !ctx.inputStream.isEndOfStream) {
      val rdwOffset = ctx.inputStream.offset
      val rdw = ctx.inputStream.next(rdwSize)

      val m = RecordHeaderParserRDW.processRdwHeader(rdw, rdwOffset, rdwParams.isBigEndian, rdwAdjustment)
      valid = m.isValid

      if (m.recordLength > 0) {
        val payload = ctx.inputStream.next(m.recordLength)

        if (payload.length > 0) {
          currentRecord = Some(payload)
        }
      }
    }
  }
}
