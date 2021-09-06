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
class RecordHeaderDecoderBdw(bdwParameters: RecordHeaderParameters) extends RecordHeaderDecoderCommon {
  final val MAX_BDW_BLOCK_SIZE = 256*1024*1024
  final val BDW_HEADER_LENGTH = 4

  override def headerSize: Int = BDW_HEADER_LENGTH

  def headerName = "BDW"

  override def getRecordLength(header: Array[Byte], offset: Long): Int = {
    validateHeader(header, offset)

    val recordLength = if (bdwParameters.isBigEndian) {
      if (isExtendedBigEndianBdw(header)) {
        parseExtendedBigEndianBdw(header)
      } else {
        parseNormalBigEndianBdw(header, offset)
      }
    } else {
      if (isExtendedLittleEndianBdw(header)) {
        parseExtendedLittleEndianBdw(header)
      } else {
        parseNormalLittleEndianBdw(header, offset)
      }
    }

    validateBlockLength(header, offset, recordLength)
    recordLength
  }

  protected final def isExtendedBigEndianBdw(header: Array[Byte]): Boolean = {
    (header(0) & 0x80) > 0
  }

  protected final def isExtendedLittleEndianBdw(header: Array[Byte]): Boolean = {
    (header(3) & 0x80) > 0
  }

  protected final def parseNormalBigEndianBdw(header: Array[Byte], offset: Long): Int = {
    if (header(2) != 0 || header(3) != 0) reportInvalidHeaderZeros(header, offset)
    (header(1) & 0xFF) + 256 * (header(0) & 0x7F) + bdwParameters.adjustment
  }

  protected final def parseExtendedBigEndianBdw(header: Array[Byte]): Int = {
    (header(3) & 0xFF) + 256 * (header(2) & 0xFF) + 65536 * (header(1) & 0xFF) + 16777216 * (header(0) & 0x7F) + bdwParameters.adjustment
  }

  protected final def parseNormalLittleEndianBdw(header: Array[Byte], offset: Long): Int = {
    if (header(0) != 0 || header(1) != 0) reportInvalidHeaderZeros(header, offset)
    (header(2) & 0xFF) + 256 * (header(3) & 0x7F) + bdwParameters.adjustment
  }

  protected final def parseExtendedLittleEndianBdw(header: Array[Byte]): Int = {
    (header(0) & 0xFF) + 256 * (header(1) & 0xFF) + 65536 * (header(2) & 0xFF) + 16777216 * (header(3) & 0x7F) + bdwParameters.adjustment
  }

  protected def validateBlockLength(header: Array[Byte], offset: Long, blockLength: Int): Unit = {
    if (blockLength < 0) {
      reportInvalidValue(header, offset, blockLength)
    }
    if (blockLength < BDW_HEADER_LENGTH) {
      reportMinimumLength(header, offset, blockLength, BDW_HEADER_LENGTH)
    }
    if (blockLength > MAX_BDW_BLOCK_SIZE) {
      reportTooLargeBlockLength(header, offset, blockLength)
    }
  }

}
