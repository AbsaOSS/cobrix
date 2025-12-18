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
import org.apache.hadoop.fs.{ContentSummary, Path}
import org.apache.hadoop.io.compress.CompressionCodecFactory
import org.apache.log4j.Logger
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

import java.io.IOException

/**
  * This class provides methods for streaming bytes from a Hadoop file.
  *
  * It is stateful, which means that it stores the offset until which the file has been consumed.
  *
  * Instances of this class are not reusable, i.e., once the file is fully read, it can neither be reopened nor can another
  * file be consumed.
  *
  * @param filePath   String containing the fully qualified path to the file.
  * @param hadoopConfig Hadoop configuration.
  * @note This class is not thread-safe and should only be accessed from a single thread
  */
class FileStreamer(filePath: String, hadoopConfig: Configuration, startOffset: Long = 0L, maximumBytes: Long = 0L) extends SimpleStream {

  private val logger = Logger.getLogger(FileStreamer.this.getClass)

  private val hadoopPath = new Path(filePath)
  private var byteIndex = startOffset

  // This ensures that the file is never opened if the stream is never used. This serves two purposes:
  // - Safety: ensures that unused streams are closed.
  // - Performance: prevents time being spent on opening unused files.
  // Note: Since we are working with a network file system, opening a file is a very expensive operation.
  private var wasOpened = false
  private var bufferedStream: BufferedFSDataInputStream = _

  private lazy val isCompressedStream = {
    val factory = new CompressionCodecFactory(hadoopConfig)
    val codec = factory.getCodec(hadoopPath)

    codec != null
  }

  private lazy val fileSize = getHadoopFileSize(hadoopPath)

  override def inputFileName: String = filePath

  override def size: Long = if (maximumBytes > 0) Math.min(fileSize, maximumBytes + startOffset) else fileSize

  override def totalSize: Long = fileSize

  override def offset: Long = byteIndex

  override def isCompressed: Boolean = isCompressedStream

  override def isEndOfStream: Boolean = if (isCompressed) {
    wasOpened && (bufferedStream == null || bufferedStream.isClosed)
  } else {
    offset >= size
  }

  /**
    * Retrieves a given number of bytes from the file stream.
    *
    * One of three situations is possible:
    *
    * 1. There's enough data to be read, thus, the resulting array's length will be exactly `numberOfBytes`.
    * 2. There's not enough data but at least some bytes are available, thus, the resulting array's length will be less than requested.
    * 3. The end of the file was already reached or the stream is closed, in which case the resulting array will be empty.
    *
    * @param numberOfBytes The number of bytes to read from the stream
    * @return An array containing the requested bytes, or fewer bytes if end of stream is reached, or empty array if no more data
    */
  @throws[IOException]
  override def next(numberOfBytes: Int): Array[Byte] = {
    ensureOpened()
    val actualBytesToRead = if (maximumBytes > 0) {
      Math.min(maximumBytes - byteIndex + startOffset, numberOfBytes).toInt
    } else {
      numberOfBytes
    }

    if (numberOfBytes <= 0) {
      new Array[Byte](0)
    } else if (actualBytesToRead <=0 || bufferedStream == null || bufferedStream.isClosed) {
      logger.info(s"End of stream reached: Requested $numberOfBytes bytes, reached offset $byteIndex.")
      close()
      new Array[Byte](0)
    } else {
      val buffer = new Array[Byte](actualBytesToRead)

      val readBytes = bufferedStream.readFully(buffer, 0, actualBytesToRead)

      if (readBytes > 0) {
        byteIndex = byteIndex + readBytes
      }

      if (readBytes == numberOfBytes) {
        buffer
      } else {
        logger.info(s"End of stream reached: Requested $numberOfBytes bytes, received $readBytes.")
        close()
        if (readBytes == actualBytesToRead) {
          buffer
        } else if (readBytes > 0) {
          val shrunkBuffer = new Array[Byte](readBytes)
          System.arraycopy(buffer, 0, shrunkBuffer, 0, readBytes)
          shrunkBuffer
        } else {
          new Array[Byte](0)
        }
      }
    }
  }

  @throws[IOException]
  override def close(): Unit = {
    wasOpened = true
    if (bufferedStream != null && !bufferedStream.isClosed) {
      bufferedStream.close()
      bufferedStream = null
    }
  }

  override def copyStream(): SimpleStream = {
    new FileStreamer(filePath, hadoopConfig, startOffset, maximumBytes)
  }

  @throws[IOException]
  private def ensureOpened(): Unit = {
    if (!wasOpened) {
      bufferedStream = new BufferedFSDataInputStream(new Path(filePath), hadoopConfig, startOffset, Constants.defaultStreamBufferInMB, maximumBytes)
      wasOpened = true
    }
  }

  private def getHadoopFileSize(hadoopPath: Path): Long = {
    val fileSystem = hadoopPath.getFileSystem(hadoopConfig)
    val cSummary: ContentSummary = fileSystem.getContentSummary(hadoopPath)
    cSummary.getLength
  }
}
