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
  private val effectiveSize: Long = fileSize - fileStartOffset - fileEndOffset
  private var byteIndex = 0L

  // Skip the start offset if specified
  if (fileStartOffset > 0) {
    bytesStream.skip(fileStartOffset)
  }

  override def size: Long = effectiveSize

  override def totalSize: Long = effectiveSize

  override def offset: Long = byteIndex

  override def inputFileName: String = fileName

  @throws(classOf[IOException])
  override def next(numberOfBytes: Int): Array[Byte] = {
    if (numberOfBytes <= 0) throw new IllegalArgumentException("Value of numberOfBytes should be greater than zero.")

    // Check if we've reached the effective end of the stream
    if (byteIndex >= effectiveSize) {
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
}
