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

package za.co.absa.cobrix.cobol.reader.recordheader

/**
  * This class represent a header decoder for standard RDW and BDW headers
  */
class RecordHeaderDecoderRdwBdw(headerName: String,
                                rdwParameters: RecordHeaderParameters) extends RecordHeaderDecoder {
  def headerSize: Int = 4

  def getRecordLength(header: Array[Byte], offset: Long): Int = {
    if (header.length != headerSize) reportInvalidHeaderLength(header, offset)

    val recordLength = if (rdwParameters.isBigEndian) {
      if (header(2) != 0 || header(3) != 0) reportInvalidHeader(header, offset)
      (header(1) & 0xFF) + 256 * (header(0) & 0xFF) + rdwParameters.adjustment
    } else {
      if (header(0) != 0 || header(1) != 0) reportInvalidHeader(header, offset)
      (header(2) & 0xFF) + 256 * (header(3) & 0xFF) + rdwParameters.adjustment
    }

    if (recordLength < 0) {
      reportInvalidValue(header, offset, recordLength)
    }
    if (recordLength == 0) {
      reportZeroLength(header, offset)
    } else {
      recordLength
    }
  }

  private def reportInvalidHeaderLength(header: Array[Byte], offset: Long): Unit = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"The length of $headerName headers is unexpected. Expected: $headerSize, got ${header.length}. Header: $rdwHeaders, offset: $offset.")
  }

  private def reportInvalidHeader(header: Array[Byte], offset: Long): Unit = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers contain non-zero values where zeros are expected (check 'rdw_big_endian' / 'bdw_big_endian' flags. Header: $rdwHeaders, offset: $offset.")
  }

  private def reportInvalidValue(header: Array[Byte], offset: Long, value: Int): Unit = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers contain an invalid value ($value). Header: $rdwHeaders, offset: $offset.")
  }

  private def reportZeroLength(header: Array[Byte], offset: Long): Nothing = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers should never be zero ($rdwHeaders). Found zero size record at $offset.")
  }

  private def renderHeader(header: Array[Byte]): String = {
    header.map(_ & 0xFF).mkString(",")
  }
}
