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
import org.apache.log4j.Logger
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream
import org.apache.hadoop.fs.ContentSummary
import za.co.absa.cobrix.cobol.reader.common.Constants

/**
  * This class provides methods for streaming bytes from an Hadoop file.
  *
  * It is stateful, which means that it stores the offset until which the file has been consumed.
  *
  * Instances of this class are not reusable, i.e. once the file is fully read it can neither be reopened nor can other
  * file be consumed.
  *
  * @param filePath   String contained the fully qualified path to the file.
  * @param fileSystem Underlying FileSystem point of access.
  * @throws IllegalArgumentException in case the file is not found in the underlying file system.
  */
class FileStreamer(filePath: String, fileSystem: FileSystem, startOffset: Long = 0L, maximumBytes: Long = 0L) extends SimpleStream {

  private val logger = Logger.getLogger(FileStreamer.this.getClass)

  private var byteIndex = startOffset

  // Use a buffer to read the data from Hadoop in big chunks
  private var bufferedStream = new BufferedFSDataInputStream(getHadoopPath(filePath), fileSystem, startOffset, Constants.defaultStreamBufferInMB, maximumBytes)

  private val fileSize = getHadoopFileSize(getHadoopPath(filePath))

  override def inputFileName: String = filePath

  override def size: Long = if (maximumBytes > 0) Math.min(fileSize, maximumBytes + startOffset) else fileSize

  override def totalSize: Long = fileSize

  override def offset: Long = byteIndex

  /**
    * Retrieves a given number of bytes.
    *
    * One of three situations is possible:
    *
    * 1. There's enough data to be read, thus, the resulting array's length will be exactly ''numberOfBytes''.
    * 2. There's not enough data but at least some, thus, the resulting array's length will be the number of available bytes.
    * 3. The end of the file was already reached, in which case the resulting array will be empty.
    *
    * @param numberOfBytes
    * @return
    */
  override def next(numberOfBytes: Int): Array[Byte] = {
    val actualBytesToRead = if (maximumBytes > 0) {
      Math.min(maximumBytes - byteIndex + startOffset, numberOfBytes).toInt
    } else {
      numberOfBytes
    }

    if (numberOfBytes <= 0) {
      new Array[Byte](0)
    } else if (actualBytesToRead <=0 || bufferedStream == null || bufferedStream.isClosed) {
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
        logger.warn(s"End of stream reached: Requested $numberOfBytes bytes, received $readBytes.")
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

  override def close(): Unit = {
    if (bufferedStream != null && !bufferedStream.isClosed) {
      bufferedStream.close()
      bufferedStream = null
    }
  }

  override def copyStream(): SimpleStream = {
    new FileStreamer(filePath, fileSystem, startOffset, maximumBytes)
  }

  /**
    * Gets a Hadoop [[Path]] (HDFS, S3, DBFS, etc) to the file.
    *
    * Throws IllegalArgumentException in case the file does not exist.
    */
  private def getHadoopPath(path: String) = {
    val hadoopPath = new Path(path)
    if (!fileSystem.exists(hadoopPath)) {
      throw new IllegalArgumentException(s"File does not exist: $path")
    }
    hadoopPath
  }

  private def getHadoopFileSize(hadoopPath: Path): Long = {
    val cSummary: ContentSummary = fileSystem.getContentSummary(hadoopPath)
    cSummary.getLength
  }
}
