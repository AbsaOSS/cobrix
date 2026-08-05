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
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, GpgUtils}

import java.io.{IOException, InputStream}
import scala.util.Try

class BufferedFSDataInputStream(filePath: Path,
                                hadoopConfig: Configuration,
                                startOffset: Long,
                                bufferSizeInMegabytes: Int,
                                maximumBytes: Long,
                                gpgKeyringAsc: Option[String],
                                gpgPassphrase: Option[String]) {
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
  private var bytesRead = 0L

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
        lengthLeft -= bytesLeft
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
          if (bufferContainBytes > 0 && lengthLeft > 0) {
            val available = bufferContainBytes - bufferPos
            val bytesToCopy = Math.min(lengthLeft, available)
            System.arraycopy(buffer, bufferPos, b, offsetLeft, bytesToCopy)
            bufferPos += bytesToCopy
            offsetLeft += bytesToCopy
            lengthLeft -= bytesToCopy
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

    val baseStream = gpgKeyringAsc match {
      case Some(keyring) =>
        isCompressedStream = true
        val rawStream = fileSystem.open(filePath)
        try {
          GpgUtils.decryptStream(rawStream, keyring, gpgPassphrase.map(_.toCharArray).getOrElse(Array.emptyCharArray))
        } catch {
          case ex: Throwable =>
            // Close rawStream only if decryptStream() fails to return a decrypted stream. Ignore errors that might happen on close.
            Try {
              rawStream.close()
            }
            throw ex
        }
      case None =>
        val codec = FileUtils.getCompressionCodec(filePath, hadoopConfig)
        val fsIn: FSDataInputStream = fileSystem.open(filePath)

        if (codec != null) {
          isCompressedStream = true
          codec.createInputStream(fsIn)
        } else {
          // No compression detected
          fsIn
        }
    }

    if (startOffset > 0) {
      if (!isCompressedStream) {
        baseStream.asInstanceOf[FSDataInputStream].seek(startOffset)
      } else {
        var toSkip = startOffset
        while (toSkip > 0) {
          val skipped = baseStream.skip(toSkip)
          if (skipped <= 0) return baseStream
          toSkip -= skipped
        }
      }
    }
    baseStream
  }
}
