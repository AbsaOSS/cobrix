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
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.Constants
import org.apache.hadoop.fs.ContentSummary

/**
  * This class provides methods for streaming bytes from an HDFS file.
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

  // Use a buffer to read the data from HDFS in big chunks
  private var buferredStream = new BufferedFSDataInputStream(getHDFSPath(filePath), fileSystem, startOffset, Constants.defaultStreamBufferInMB, maximumBytes)

  private val fileSize = getHDFSFileSize(getHDFSPath(filePath))

  override def inputFileName: String = filePath

  override def size: Long = if (maximumBytes > 0) Math.min(fileSize, maximumBytes + startOffset) else fileSize

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
    if ((maximumBytes > 0 && byteIndex - startOffset >= maximumBytes) || buferredStream.isClosed) {
      close()
      new Array[Byte](0)
    } else {

      val buffer = new Array[Byte](numberOfBytes)

      var readBytes = buferredStream.readFully(buffer, 0, numberOfBytes)

      if (readBytes > 0) {
        byteIndex = byteIndex + readBytes
      }

      if (readBytes == numberOfBytes) {
        buffer
      }
      else {
        logger.warn(s"End of stream reached: Requested $numberOfBytes bytes, received $readBytes.")
        // resize buffer so that the consumer knows how many bytes are there
        close()
        if (readBytes > 0) {
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
    if (!buferredStream.isClosed) {
      buferredStream.close()
      buferredStream = null
    }
  }

  /**
    * Gets an HDFS [[Path]] to the file.
    *
    * Throws IllegalArgumentException in case the file does not exist.
    */
  private def getHDFSPath(path: String) = {

    if (fileSystem == null) {
      throw new IllegalArgumentException("Null FileSystem instance.")
    }

    if (path == null) {
      throw new IllegalArgumentException("Null input file.")
    }

    val hdfsPath = new Path(path)
    if (!fileSystem.exists(hdfsPath)) {
      throw new IllegalArgumentException(s"Inexistent file: $path")
    }
    hdfsPath
  }

  private def getHDFSFileSize(hdfsPath: Path): Long = {
    val cSummary: ContentSummary = fileSystem.getContentSummary(hdfsPath)
    cSummary.getLength
  }
}