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
import za.co.absa.cobrix.cobol.reader.recordheader.RecordHeaderDecoder
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

/**
  * @param startingRecordNumber A record number the input stream is pointing to (zero-based).
  * @param dataStream           An input stream pointing to the beginning of a file or a record in a file. The
  *                             record extractor should close the stream when the end of file is reached.
  * @param headerStream         A stream pointing to the beginning of the file, even if inputStream is pointing
  *                             to a record in the middle. The record extractor should close the stream when it
  *                             is no longer needed.
  * @param copybook             A copybook of the input stream.
  * @param additionalInfo       A string provided by a client for the raw record extractor.
  */
case class RawRecordContext(
                             startingRecordNumber: Long,
                             dataStream: SimpleStream,
                             headerStream: SimpleStream,
                             copybook: Copybook,
                             rdwDecoder: RecordHeaderDecoder,
                             bdwDecoder: RecordHeaderDecoder,
                             additionalInfo: String
                           )
