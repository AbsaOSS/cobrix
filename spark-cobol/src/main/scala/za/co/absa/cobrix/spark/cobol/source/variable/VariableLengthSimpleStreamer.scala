/*
 * Copyright 2018 Barclays Africa Group Limited
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

package za.co.absa.cobrix.spark.cobol.source.variable

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream

class VariableLengthSimpleStreamer(filePath: String, fileSystem: FileSystem) extends SimpleStream {

  private val logger = Logger.getLogger(VariableLengthSimpleStreamer.this.getClass)

  private val hdfsInputStream = fileSystem.open(getHDFSPath(filePath))
  private var offset = 0

  override def next(numberOfBytes: Int): Array[Byte] = {

    val buffer = new Array[Byte](numberOfBytes)

    val readBytes = hdfsInputStream.read(offset, buffer, 0, numberOfBytes)
    offset = offset + readBytes

    if (readBytes == numberOfBytes) {
      buffer
    }
    else {
      logger.warn(s"End of stream reached: Requested $numberOfBytes bytes, received $readBytes.")
      // resize buffer so that the consumer knows how many bytes are there
      val shrunkBuffer = new Array[Byte](readBytes)
      System.arraycopy(buffer, 0, shrunkBuffer, 0, readBytes)
      shrunkBuffer
    }
  }

  override def close(): Unit = hdfsInputStream.close()

  private def getHDFSPath(path: String): Path = {
    val hdfsPath = new Path(path)
    if (!fileSystem.exists(hdfsPath)) {
      throw new IllegalArgumentException(s"Inexistent file: $path")
    }
    hdfsPath
  }
}
