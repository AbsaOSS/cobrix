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

import za.co.absa.cobrix.cobol.parser.common.Constants

/**
  * This is a parser for records that contain 4 byte RDW headers.
  */
class RecordHeaderParserRDW(isBigEndian: Boolean) extends Serializable with RecordHeaderParser {

  /** RDW header is a 4 byte header */
  override def getHeaderLength: Int = 4

  /** RDW headers are added by mainframe transferring tools (such as XCOM), so they are never defined in copybooks. */
  override def isHeaderDefinedInCopybook: Boolean = false

  /**
    * Given a raw values of a record header returns metadata sufficient to parse the record.
    *
    * @param header    A record header as an array of bytes
    * @param byteIndex The index of the header in a mainframe file. This is provided for error messages generation
    *                  purposes
    * @return A parsed record metadata
    */
  override def getRecordMetadata(header: Array[Byte], byteIndex: Long = 0L): RecordMetadata = {
    val rdwHeaderBlock = getHeaderLength
    if (header.length < rdwHeaderBlock) {
      RecordMetadata(-1, isValid = false)
    }
    else {
      val recordLength = if (isBigEndian) {
        (header(1) & 0xFF) + 256 * (header(0) & 0xFF)
      } else {
        (header(2) & 0xFF) + 256 * (header(3) & 0xFF)
      }

      if (recordLength > 0) {
        if (recordLength > Constants.maxRdWRecordSize) {
          val rdwHeaders = header.map(_ & 0xFF).mkString(",")
          throw new IllegalStateException(s"RDW headers too big (length = $recordLength > ${Constants.maxRdWRecordSize}). Headers = $rdwHeaders at $byteIndex.")
        }
        RecordMetadata(recordLength, isValid = true)
      } else {
        val rdwHeaders = header.map(_ & 0xFF).mkString(",")
        throw new IllegalStateException(s"RDW headers should never be zero ($rdwHeaders). Found zero size record at $byteIndex.")
      }
    }
  }

}
