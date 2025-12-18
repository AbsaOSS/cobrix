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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, Path}
import org.apache.hadoop.io.compress.CompressionCodecFactory

import java.io.{IOException, InputStream}

class BufferedFSDataInputStream(filePath: Path, hadoopConfig: Configuration, startOffset: Long, bufferSizeInMegabytes: Int, maximumBytes: Long ) {
  val bytesInMegabyte: Int = 1048576
  private var isCompressedStream = false

  if (bufferSizeInMegabytes <=0 || bufferSizeInMegabytes > 1000) {
    throw new IllegalArgumentException(s"Invalid buffer size $bufferSizeInMegabytes MB.")
  }

  private var in: InputStream = openStream()

  private val bufferSizeInBytes = bufferSizeInMegabytes * bytesInMegabyte
  private var isStreamClosed = in == null

  private val buffer = new Array[Byte](bufferSizeInBytes)
  private var bufferPos = 0
  private var bufferContainBytes = 0
  private var bytesRead = 0

  @throws[IOException]
  def close(): Unit = {
    if (!isStreamClosed) {
      in.close()
      in = null
      isStreamClosed = true
    }
  }

  def isClosed: Boolean = isStreamClosed && bufferPos >= bufferContainBytes

  def isCompressed: Boolean = isCompressedStream

  def readFully(b: Array[Byte], off: Int, len: Int): Int =
  {
    if (isClosed) {
      -1
    } else if (bufferPos + len < bufferContainBytes) {
      System.arraycopy(buffer, bufferPos, b, off, len)
      bufferPos += len
      len
    } else {
      var offsetLeft = off
      var lengthLeft = len
      if (bufferPos < bufferContainBytes) {
        val bytesLeft = bufferContainBytes - bufferPos
        System.arraycopy(buffer, bufferPos, b, off, bytesLeft)
        lengthLeft -= bufferContainBytes - bufferPos
        offsetLeft += bytesLeft
      }
      bufferPos = 0
      bufferContainBytes = if ( (maximumBytes>0 && bytesRead >= maximumBytes) || isStreamClosed) {
        close()
        0
      } else {
        val toRead = if (maximumBytes > 0) Math.min(bufferSizeInBytes, maximumBytes - bytesRead) else bufferSizeInBytes
        readFullyHelper(buffer, 0, toRead.toInt)
      }
      bytesRead += bufferContainBytes
      if (bufferContainBytes > 0) {
        if (bufferPos + lengthLeft < bufferContainBytes) {
          System.arraycopy(buffer, bufferPos, b, offsetLeft, lengthLeft)
          bufferPos += lengthLeft
          offsetLeft += lengthLeft
          lengthLeft = 0
        } else {
          if (bufferContainBytes > 0) {
            System.arraycopy(buffer, bufferPos, b, offsetLeft, lengthLeft)
            bufferPos += bufferContainBytes
            offsetLeft += bufferContainBytes
            lengthLeft -= bufferContainBytes
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

  private def openStream(): InputStream = {
    val fileSystem = filePath.getFileSystem(hadoopConfig)
    val fsIn: FSDataInputStream = fileSystem.open(filePath)

    if (startOffset > 0) {
      fsIn.seek(startOffset)
    }

    val factory = new CompressionCodecFactory(hadoopConfig)
    val codec = factory.getCodec(filePath)

    if (codec != null) {
      isCompressedStream = true
      codec.createInputStream(fsIn)
    } else {
      // No compression detected
      fsIn
    }
  }
}
