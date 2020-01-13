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

package za.co.absa.cobrix.cobol.parser.headerparsers

import za.co.absa.cobrix.cobol.parser.common.Constants

/**
  * This is a parser for records that contain 4 byte RDW headers.
  */
class RecordHeaderParserRDW(isBigEndian: Boolean,
                            fileHeaderBytes: Int,
                            fileFooterBytes: Int,
                            rdwAdjustment: Int) extends Serializable with RecordHeaderParser {

  /** RDW header is a 4 byte header */
  override def getHeaderLength: Int = 4

  /** RDW headers are added by mainframe transferring tools (such as XCOM), so they are never defined in copybooks. */
  override def isHeaderDefinedInCopybook: Boolean = false

  /**
    * Given a raw values of a record header returns metadata sufficient to parse the record.
    *
    * @param header     A record header as an array of bytes
    * @param fileOffset An offset from the beginning of the underlying file
    * @param fileSize   A size of the underlying file
    * @param recordNum  A sequential record number
    * @return A parsed record metadata
    */
  override def getRecordMetadata(header: Array[Byte], fileOffset: Long, fileSize: Long, recordNum: Long): RecordMetadata = {
    if (fileHeaderBytes > getHeaderLength && fileOffset == getHeaderLength) {
      RecordMetadata(fileHeaderBytes - getHeaderLength, isValid = false)
    } else if (fileSize > 0L && fileFooterBytes > 0 && fileSize - fileOffset <= fileFooterBytes) {
      RecordMetadata((fileSize - fileOffset).toInt, isValid = false)
    } else {
      processRdwHeader(header, fileOffset)
    }
  }

  /**
    * Parses an RDW header.
    *
    * @param header A record header as an array of bytes
    * @param offset An offset from the beginning of the underlying file
    *
    * @return A parsed record metadata
    */
  private def processRdwHeader(header: Array[Byte], offset: Long): RecordMetadata = {
    val rdwHeaderBlock = getHeaderLength
    if (header.length < rdwHeaderBlock) {
      RecordMetadata(-1, isValid = false)
    }
    else {
      val recordLength = if (isBigEndian) {
        (header(1) & 0xFF) + 256 * (header(0) & 0xFF) + rdwAdjustment
      } else {
        (header(2) & 0xFF) + 256 * (header(3) & 0xFF) + rdwAdjustment
      }

      if (recordLength > 0) {
        if (recordLength > Constants.maxRdWRecordSize) {
          val rdwHeaders = header.map(_ & 0xFF).mkString(",")
          throw new IllegalStateException(s"RDW headers too big (length = $recordLength > ${Constants.maxRdWRecordSize}). Headers = $rdwHeaders at $offset.")
        }
        RecordMetadata(recordLength, isValid = true)
      } else {
        val rdwHeaders = header.map(_ & 0xFF).mkString(",")
        throw new IllegalStateException(s"RDW headers should never be zero ($rdwHeaders). Found zero size record at $offset.")
      }
    }
  }

}
