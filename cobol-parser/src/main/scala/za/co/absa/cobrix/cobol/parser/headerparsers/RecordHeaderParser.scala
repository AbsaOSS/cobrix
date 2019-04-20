/*
 * Copyright 2018-2019 ABSA Group Limited
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

package za.co.absa.cobrix.cobol.parser.headerparsers

/**
  * This trait represents a contract for record header parsers. Usually each record of a multi-segment mainframe file
  * has a 4 byte record descriptor word (RDW) header specifying length of each record. If this RDW header is no-standard
  * a custom header parser can be provided to Cobrix to extract such records.
  *
  * To this you need to create a parser class that extends this trait and implement the header parser logic.
  *
  * A record header parser for RDW is implemented in `RecordHeaderParserRDW`. This is a good starting point
  * for implementing your own record header parser.
  */
trait RecordHeaderParser {

  /**
    * Returns the length of the header sufficient to determine record's validity and size. Usually this is
    * a constant number for a given record header flavor.
    */
  def getHeaderLength: Int

  /**
    * If true the record parser will expect record headers to be defined in the copybook itself.
    * If false the headers are not expected to be defined in the copybook and the number of bytes
    * returned by `getHeaderLength` will be truncated before record bytes are passed to the parser.
    */
  def isHeaderDefinedInCopybook: Boolean

  /**
    * Given a raw values of a record header returns metadata sufficient to parse the record.
    *
    * @param header A record header as an array of bytes
    * @return A parsed record metadata
    */
  def getRecordMetadata(header: Array[Byte], byteIndex: Long): RecordMetadata
}
