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

package com.example.spark.cobol.app

import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.headerparsers.{RecordHeaderParser, RecordMetadata}

/**
  * This is a parser for records that contain 5 byte custom RDW headers for custom record parser integration spec.
  */
class CustomRecordHeadersParser extends Serializable with RecordHeaderParser {

  /** RDW header is a 5 byte header */
  override def getHeaderLength: Int = 5

  override def isHeaderDefinedInCopybook: Boolean = false

  /**
    * Given a raw values of a record header returns metadata sufficient to parse the record.
    *
    * @param header    A record header as an array of bytes
    * @param fileOffset    An offset from the beginning of the underlying file
    * @param fileSize      A size of the underlying file
    * @param recordNum A sequential record number
    * @return A parsed record metadata
    */
  override def getRecordMetadata(header: Array[Byte], fileOffset: Long, fileSize: Long, recordNum: Long): RecordMetadata = {
    val rdwHeaderBlock = getHeaderLength
    if (header.length < rdwHeaderBlock) {
      RecordMetadata(-1, isValid = false)
    }
    else {
      val isValid = header(0) == 1
      val recordLength = (header(3) & 0xFF) + 256 * (header(4) & 0xFF)

      if (recordLength > 0) {
        if (recordLength > Constants.maxRdWRecordSize) {
          val rdwHeaders = header.map(_ & 0xFF).mkString(",")
          throw new IllegalStateException(s"Custom RDW headers too big (length = $recordLength > ${Constants.maxRdWRecordSize}). Headers = $rdwHeaders at $fileOffset.")
        }
        RecordMetadata(recordLength, isValid)
      } else {
        val rdwHeaders = header.map(_ & 0xFF).mkString(",")
        throw new IllegalStateException(s"Custom RDW headers should never be zero ($rdwHeaders). Found zero size record at $fileOffset.")
      }
    }
  }

}
