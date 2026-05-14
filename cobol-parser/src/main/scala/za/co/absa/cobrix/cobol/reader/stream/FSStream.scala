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

package za.co.absa.cobrix.cobol.reader.stream

import java.io.{BufferedInputStream, File, FileInputStream, FileNotFoundException, IOException}

class FSStream (fileName: String, fileStartOffset: Long = 0L, fileEndOffset: Long = 0L) extends SimpleStream {
  val bytesStream = new BufferedInputStream(new FileInputStream(fileName))
  private var isClosed = false

  private val fileSize: Long = new File(fileName).length()
  private val effectiveSize: Long = math.max(0L, fileSize - fileStartOffset - fileEndOffset)
  private var byteIndex = 0L
  private var skipped: Boolean = false

  override def size: Long = effectiveSize

  override def totalSize: Long = effectiveSize

  override def offset: Long = byteIndex

  override def inputFileName: String = fileName

  /**
    * Reads and returns the bytes at the beginning of the file that precede the effective stream content.
    *
    * This method attempts to read the leading bytes (defined by the file start offset) that appear
    * before the effective content of the stream. It only performs the read on the first invocation
    * and if the file start offset is greater than zero. Subsequent calls will return an empty array
    * since the skipped flag is set after the first successful read.
    *
    * @return an array of bytes containing the skipped start bytes, or an empty array if the bytes
    *         have already been read, the file start offset is zero or negative, or no bytes could be read.
    */
  def getSkippedStartBytes: Array[Byte] = {
    if (fileStartOffset > Int.MaxValue)
      throw new IllegalArgumentException(s"fileStartOffset ($fileStartOffset) exceeds maximum supported value (${Int.MaxValue})")
    val fileStartOffsetInt = fileStartOffset.toInt
    if (skipped || fileStartOffsetInt <= 0)
      Array.empty[Byte]
    else {
      skipped = true
      val b = new Array[Byte](fileStartOffsetInt)
      val actual = bytesStream.read(b, 0, fileStartOffsetInt)
      if (actual <= 0) {
        Array.empty[Byte]
      } else {
        b.take(actual)
      }
    }
  }

  /**
    * Reads and returns the bytes at the end of the file that follow the effective stream content.
    *
    * This method attempts to read the trailing bytes (defined by the file end offset) that appear
    * after the effective content of the stream. It only performs the read when the byte index has
    * reached or exceeded the effective size and the stream has not yet been closed. If the file end
    * offset is zero or negative, the stream is closed and an empty array is returned.
    *
    * @return an array of bytes containing the skipped end bytes, or an empty array if the stream
    *         has not yet reached the end of the effective content, the stream is already closed,
    *         the file end offset is zero or negative, or no bytes could be read.
    */
  def getSkippedEndBytes: Array[Byte] = {
    if (fileEndOffset > Int.MaxValue)
      throw new IllegalArgumentException(s"fileEndOffset ($fileEndOffset) exceeds maximum supported value (${Int.MaxValue})")
    val fileEndOffsetInt = fileEndOffset.toInt
    if (byteIndex >= effectiveSize && !isClosed) {
      if (fileEndOffsetInt > 0) {
        val b = new Array[Byte](fileEndOffsetInt)
        val actual = bytesStream.read(b, 0, fileEndOffsetInt)
        if (actual <= 0) {
          Array.empty[Byte]
        } else {
          b.take(actual)
        }
      } else {
        close()
        Array.empty[Byte]
      }
    } else {
      Array.empty[Byte]
    }
  }


  @throws(classOf[IOException])
  override def next(numberOfBytes: Int): Array[Byte] = {
    if (numberOfBytes <= 0) throw new IllegalArgumentException("Value of numberOfBytes should be greater than zero.")

    if (!skipped && fileStartOffset > 0) {
      // Skip the start offset if specified
      skipFully(fileStartOffset)
    }

    // Check if we've reached the effective end of the stream
    if (byteIndex >= effectiveSize) {
      if (fileEndOffset <= 0)
        close()
      return new Array[Byte](0)
    }

    // Calculate how many bytes we can actually read without exceeding effectiveSize
    val bytesToRead = Math.min(numberOfBytes, (effectiveSize - byteIndex).toInt)

    val b = new Array[Byte](bytesToRead)
    val actual = bytesStream.read(b, 0, bytesToRead)
    if (actual <= 0) {
      close()
      new Array[Byte](0)
    } else {
      byteIndex += actual
      b.take(actual)
    }
  }

  @throws(classOf[IOException])
  override def close(): Unit = {
    if (!isClosed) {
      bytesStream.close()
      isClosed = true
    }
  }

  @throws(classOf[FileNotFoundException])
  override def copyStream(): SimpleStream = {
    new FSStream(fileName, fileStartOffset, fileEndOffset)
  }

  private def skipFully(bytesToSkip: Long): Unit = {
    skipped = true
    var remaining = math.min(bytesToSkip, fileSize)
    while (remaining > 0) {
      val skipped = bytesStream.skip(remaining)
      if (skipped <= 0) {
        throw new IOException(s"Unable to skip $bytesToSkip bytes in $fileName.")
      }
      remaining -= skipped
    }
  }
}
