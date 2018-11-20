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

package za.co.absa.cobrix.spark.cobol.source.streaming

import org.apache.hadoop.fs.{FSDataInputStream, FileSystem, Path}

class BufferedFSDataInputStream(filePath: Path, fileSystem: FileSystem, startOffset: Long, bufferSizeInMegabytes: Int, maximumBytes: Long ) {
  val bytesInMegabyte: Int = 1048576

  if (bufferSizeInMegabytes <=0 || bufferSizeInMegabytes > 1000) {
    throw new IllegalArgumentException(s"Invalid buffer size $bufferSizeInMegabytes MB.")
  }

  var in: FSDataInputStream = fileSystem.open(filePath)
  if (startOffset > 0) {
    in.seek(startOffset)
  }

  private val bufferSizeInBytes = bufferSizeInMegabytes * bytesInMegabyte
  private var isStreamClosed = in == null

  private val buffer = new Array[Byte](bufferSizeInBytes)
  private var bufferPos = 0
  private var bufferConitainBytes = 0
  private var bytesRead = 0

  def close(): Unit = {
    if (!isStreamClosed) {
      in.close()
      in = null
      isStreamClosed = true
    }
  }

  def isClosed: Boolean = isStreamClosed && bufferPos >= bufferConitainBytes

  def readFully(b: Array[Byte], off: Int, len: Int): Int =
  {
    if (isClosed) {
      -1
    } else if (bufferPos + len < bufferConitainBytes) {
      System.arraycopy(buffer, bufferPos, b, off, len)
      bufferPos += len
      len
    } else {
      var offsetLeft = off
      var lengthLeft = len
      if (bufferPos < bufferConitainBytes) {
        val bytesLeft = bufferConitainBytes - bufferPos
        System.arraycopy(buffer, bufferPos, b, off, bytesLeft)
        lengthLeft -= bufferConitainBytes - bufferPos
        offsetLeft += bytesLeft
      }
      bufferPos = 0
      bufferConitainBytes = if ( (maximumBytes>0 && bytesRead >= maximumBytes) || isStreamClosed) {
        close
        0
      } else {
        val toRead = if (maximumBytes >0) Math.min(bufferSizeInBytes, maximumBytes - bytesRead) else bufferSizeInBytes
        readFullyHelper(buffer, 0, bufferSizeInBytes)
      }
      bytesRead += bufferConitainBytes
      if (bufferConitainBytes > 0) {
        if (bufferPos + lengthLeft < bufferConitainBytes) {
          System.arraycopy(buffer, bufferPos, b, offsetLeft, lengthLeft)
          bufferPos += lengthLeft
          offsetLeft += lengthLeft
          lengthLeft = 0
        } else {
          if (bufferConitainBytes > 0) {
            System.arraycopy(buffer, bufferPos, b, offsetLeft, lengthLeft)
            bufferPos += bufferConitainBytes
            offsetLeft += bufferConitainBytes
            lengthLeft -= bufferConitainBytes
          }
        }
      }
      len - lengthLeft
    }
  }

  /** This is the fastest way to read the data from hdfs stream without doing seeks. */
  private def readFullyHelper(b: Array[Byte], off: Int, len: Int): Int = {
    if (len <= 0) {
      len
    } else {
      var n = 0
      var count = 0
      while (n < len && count >= 0) {
        count = in.read(b, off + n, len - n)
        if (count >= 0) {
          n += count
        } else {
          close()
        }
      }
      n
    }
  }

}
