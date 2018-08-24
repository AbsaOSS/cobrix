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

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class FileStreamerSpec extends FlatSpec with BeforeAndAfter with Matchers {

  private val TEMP_DIR = new File("tmp-"+System.currentTimeMillis())
  private var streamer: FileStreamer = _

  before {
    FileUtils.forceMkdir(TEMP_DIR)
  }

  after {
    if (streamer != null) {
      streamer.close()
    }
    FileUtils.deleteDirectory(TEMP_DIR)
  }

  behavior of classOf[FileStreamer].getName

  it should "throw if file path is null" in {
    val caught = intercept[IllegalArgumentException] {
      new FileStreamer(null, FileSystem.get(new Configuration()))
    }
    assert(caught.getMessage.toLowerCase.contains("null"))
  }

  it should "throw if FileSystem is null" in {
    val caught = intercept[IllegalArgumentException] {
      new FileStreamer(createTempFile(2).getAbsolutePath, null)
    }
    assert(caught.getMessage.toLowerCase.contains("null"))
  }

  it should "throw if file does not exist" in {
    val caught = intercept[IllegalArgumentException] {
      new FileStreamer(new File(TEMP_DIR, "inexistent").getAbsolutePath, FileSystem.get(new Configuration()))
    }
    assert(caught.getMessage.toLowerCase.contains("inexistent"))
  }

  it should "return array of same length than expected number of bytes if enough data" in {
    val batchLength = 8
    val iterations = 10
    val tmpFile = createTempFile(iterations * batchLength)

    streamer = new FileStreamer(tmpFile.getAbsolutePath, FileSystem.get(new Configuration()))
    for (i <- 0 until iterations) {
      assert(streamer.next(batchLength).length == batchLength)
    }
  }

  it should "return array of length equals to available bytes if less than expected number" in {
    val batchLength = 8
    val iterations = 10
    val extraBytes = 1
    val tmpFile = createTempFile(iterations * batchLength + extraBytes)

    streamer = new FileStreamer(tmpFile.getAbsolutePath, FileSystem.get(new Configuration()))
    streamer.next(iterations * batchLength ) // consumes all but the extra bytes
    assert(streamer.next(batchLength).length == extraBytes)
  }

  it should "return empty array if end of file was reached" in {
    val totalBytes = 10
    val tmpFile = createTempFile(totalBytes)
    streamer = new FileStreamer(tmpFile.getAbsolutePath, FileSystem.get(new Configuration()))
    streamer.next(totalBytes) // consumes all bytes
    assert(streamer.next(totalBytes).length == 0)
  }

  it should "return empty array if file is empty" in {
    val totalBytes = 0
    val tmpFile = createTempFile(totalBytes)
    streamer = new FileStreamer(tmpFile.getAbsolutePath, FileSystem.get(new Configuration()))
    assert(streamer.next(totalBytes).length == totalBytes)
  }

  private def createTempFile(numberOfBytes: Int): File = {
    val tmp = new File(TEMP_DIR, "tmp")
    val bytes = for (i <- 0 until numberOfBytes) yield i.toByte
    FileUtils.writeByteArrayToFile(tmp, bytes.toArray)
    tmp.deleteOnExit()
    tmp
  }
}
