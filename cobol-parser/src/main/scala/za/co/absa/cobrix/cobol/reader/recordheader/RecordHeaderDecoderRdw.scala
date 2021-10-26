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
  * This class represent a header decoder for standard RDW headers
  * according to: https://www.ibm.com/docs/en/zos/2.3.0?topic=records-record-descriptor-word-rdw
  */
class RecordHeaderDecoderRdw(rdwParameters: RecordHeaderParameters) extends RecordHeaderDecoderCommon {
  final val RDW_HEADER_LENGTH = 4

  override def headerSize: Int = RDW_HEADER_LENGTH

  def headerName = "RDW"

  override def getRecordLength(header: Array[Byte], offset: Long): Int = {
    validateHeader(header, offset)

    val useSecondHalf = header(0) == 0 && header(0) == 0 && (header(2) != 0 || header(3) != 0)
    val recordLength = if (rdwParameters.isBigEndian) {
      if (useSecondHalf)
        (header(3) & 0xFF) + 256 * (header(2) & 0xFF) + rdwParameters.adjustment
      else
        (header(1) & 0xFF) + 256 * (header(0) & 0xFF) + rdwParameters.adjustment
    } else {
      if (useSecondHalf)
        (header(2) & 0xFF) + 256 * (header(3) & 0xFF) + rdwParameters.adjustment
      else
        (header(0) & 0xFF) + 256 * (header(1) & 0xFF) + rdwParameters.adjustment
    }

    validateRecordLength(header, offset, recordLength)
    recordLength
  }

  protected def validateRecordLength(header: Array[Byte], offset: Long, recordLength: Int): Unit = {
    if (recordLength < 0) {
      reportInvalidValue(header, offset, recordLength)
    }
    if (recordLength == 0) {
      reportZeroLength(header, offset)
    }
  }

}
