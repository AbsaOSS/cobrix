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

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.recordheader.{RecordHeaderDecoder, RecordHeaderDecoderBdw, RecordHeaderDecoderRdw, RecordHeaderParameters}
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

/**
  * @param startingRecordNumber A record number the input stream is pointing to (zero-based).
  * @param inputStream          An input stream pointing to the beginning of a file or a record in a file. The
  *                             record extractor should close the stream when the end of file is reached.
  * @param headerStream         A stream pointing to the beginning of the file, even if inputStream is pointing
  *                             to a record in the middle. The record extractor should close the stream when it
  *                             is no longer needed.
  * @param copybook             A copybook of the input stream.
  * @param additionalInfo       A string provided by a client for the raw record extractor.
  * @param options              All options passed to `spark-cobol`
  */
case class RawRecordContext(
                             startingRecordNumber: Long,
                             inputStream: SimpleStream,
                             headerStream: SimpleStream,
                             copybook: Copybook,
                             rdwDecoder: RecordHeaderDecoder,
                             bdwDecoder: RecordHeaderDecoder,
                             additionalInfo: String,
                             options: Map[String, String]
                           )

object RawRecordContext {
  class RawRecordContextBuilder(inputStream: SimpleStream,
                                headerStream: SimpleStream,
                                copybook: Copybook) {
    private var context = RawRecordContext(0L,
      inputStream,
      headerStream,
      copybook,
      new RecordHeaderDecoderRdw(RecordHeaderParameters(isBigEndian = true, 0)),
      new RecordHeaderDecoderBdw(RecordHeaderParameters(isBigEndian = true, 0)),
      "",
      Map.empty[String, String]
    )

    def withReaderParams(readerParameters: ReaderParameters): RawRecordContextBuilder = {
      val rdwParams = RecordHeaderParameters(readerParameters.isRdwBigEndian, readerParameters.rdwAdjustment)

      val rdwDecoder = new RecordHeaderDecoderRdw(rdwParams)

      val bdwOpt = readerParameters.bdw
      val bdwParamsOpt = bdwOpt.map(bdw => RecordHeaderParameters(bdw.isBigEndian, bdw.adjustment))
      val bdwDecoderOpt = bdwParamsOpt.map(bdwParams => new RecordHeaderDecoderBdw(bdwParams))

      withAdditionalInfo(readerParameters.reAdditionalInfo)
        .withRdwDecoder(rdwDecoder)
        .withBdwDecoder(bdwDecoderOpt.getOrElse(rdwDecoder))
        .withOptions(readerParameters.options)
    }

    def withStartingRecordNumber(startingRecordNumber: Long): RawRecordContextBuilder = {
      require(startingRecordNumber >= 0, s"startingRecordNumber must be >= 0, got: $startingRecordNumber")
      context = context.copy(startingRecordNumber = startingRecordNumber)
      this
    }

    def withRdwDecoder(rdwDecoder: RecordHeaderDecoder): RawRecordContextBuilder = {
      context = context.copy(rdwDecoder = rdwDecoder)
      this
    }

    def withBdwDecoder(bdwDecoder: RecordHeaderDecoder): RawRecordContextBuilder = {
      context = context.copy(bdwDecoder = bdwDecoder)
      this
    }

    def withAdditionalInfo(additionalInfo: String): RawRecordContextBuilder = {
      context = context.copy(additionalInfo = additionalInfo)
      this
    }

    def withOptions(options: Map[String, String]): RawRecordContextBuilder = {
      context = context.copy(options = options)
      this
    }

    def build(): RawRecordContext = {
      context
    }
  }

  /**
    * Creates a new instance of `RawRecordContextBuilder` with the specified input stream,
    * header stream, and copybook. The builder allows further customization of the
    * `RawRecordContext` before building it.
    *
    * The header stream should always point to the beginning of a file.
    * The input stream can point in the middle of the file when read not from the beginning.
    *
    * @param inputStream  the main data stream containing the record data.
    * @param headerStream the stream containing header information for the records.
    * @param copybook     the copybook defining the structure of the raw records.
    * @return a new instance of `RawRecordContextBuilder`.
    */
  def builder(startingRecordNumber: Long,
              inputStream: SimpleStream,
              headerStream: SimpleStream,
              copybook: Copybook): RawRecordContextBuilder = {
    val builder = new RawRecordContextBuilder(inputStream, headerStream, copybook)

    builder.withStartingRecordNumber(startingRecordNumber)
  }

  /**
    * Creates a new instance of `RawRecordContextBuilder` with the specified input stream,
    * presuming the file is going to be read from beginning.
    *
    * @param inputStream the main data stream containing the record data.
    * @param copybook    the copybook defining the structure of the raw records.
    * @return a new instance of `RawRecordContextBuilder`.
    */
  def builder(inputStream: SimpleStream,
              copybook: Copybook): RawRecordContextBuilder = new RawRecordContextBuilder(inputStream, inputStream.copyStream(), copybook)

}
