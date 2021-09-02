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

abstract class RecordHeaderDecoderCommon extends RecordHeaderDecoder {
  def headerName: String

  protected def validateHeader(header: Array[Byte], offset: Long): Unit = {
    if (header.length != headerSize) reportInvalidHeaderLength(header, offset)
  }

  protected def reportInvalidHeaderLength(header: Array[Byte], offset: Long): Unit = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"The length of $headerName headers is unexpected. Expected: $headerSize, got ${header.length}. Header: $rdwHeaders, offset: $offset.")
  }

  protected def reportInvalidHeaderZeros(header: Array[Byte], offset: Long): Unit = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers contain non-zero values where zeros are expected (check 'rdw_big_endian' flag. Header: $rdwHeaders, offset: $offset.")
  }

  protected def reportInvalidValue(header: Array[Byte], offset: Long, value: Int): Unit = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers contain an invalid value ($value). Header: $rdwHeaders, offset: $offset.")
  }

  protected def reportZeroLength(header: Array[Byte], offset: Long): Nothing = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers should never be zero ($rdwHeaders). Found zero size record at $offset.")
  }

  protected def reportMinimumLength(header: Array[Byte], offset: Long, length: Int, minLength: Int): Nothing = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"$headerName headers should have a length of at least $minLength bytes. Got $length bytes. Header: $rdwHeaders, offset: $offset.")
  }

  protected def reportTooLargeBlockLength(header: Array[Byte], offset: Long, blockLength: Int): Nothing = {
    val rdwHeaders = renderHeader(header)
    throw new IllegalStateException(s"The length of $headerName block is too big. Got $blockLength. Header: $rdwHeaders, offset: $offset.")
  }

  protected def renderHeader(header: Array[Byte]): String = {
    header.map(_ & 0xFF).mkString(",")
  }
}
