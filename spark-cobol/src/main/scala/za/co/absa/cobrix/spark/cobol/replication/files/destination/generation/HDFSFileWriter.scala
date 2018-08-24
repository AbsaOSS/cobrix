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

package za.co.absa.cobrix.spark.cobol.replication.files.destination.generation

import za.co.absa.cobrix.spark.cobol.replication.files.destination.identification.FileIdProvider
import java.util.concurrent.atomic.AtomicInteger
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import java.nio.file.Files
import java.io.File
import org.apache.hadoop.fs.Path
import java.io.OutputStream

object HDFSFileWriter {
  private val workerIdGenerator = new AtomicInteger(0)
}

/**
 * Writes files from an array, picked in a Round-Robin fashion, into HDFS, up to a maximum occupied space.
 */
class HDFSFileWriter(
  destDir:      String,
  files:        Array[File],
  maxSizeBytes: Long,
  hdfsConf:     Configuration,
  idProvider:   FileIdProvider) extends Runnable {

  var workerId = HDFSFileWriter.workerIdGenerator.incrementAndGet()

  implicit val hdfs = FileSystem.get(hdfsConf)

  private var totalBytesWritten = 0l
  private var filePointer = 0
  private var pointer = 0

  def run(): Unit = {

    while (totalBytesWritten < maxSizeBytes) {
      val nextFilePath = getNextFilePath()
      val lastWritten = write(files(pointer), nextFilePath)
      if (lastWritten == 0) {
        return
      }
      updateBytesWritten(lastWritten)
      updateFilePointer()
      println(s"[Worker $workerId]: wrote file: $nextFilePath (total bytes = $totalBytesWritten)")
    }
  }

  private def getNextFilePath() = {
    destDir + "/part" + idProvider.getNextId() + ".bin"
  }

  private def updateBytesWritten(lastWritten: Long) = {
    totalBytesWritten = totalBytesWritten + lastWritten
  }

  private def updateFilePointer() = {
    pointer = pointer + 1
    if (pointer == files.length) {
      pointer = 0
    }
  }

  private def write(file: File, destName: String)(implicit hdfs: FileSystem): Long = {
    try {
      val outstream = hdfs.create(new Path(destName), true)            
      outstream.write(toBytes(file))
      outstream.flush()
      outstream.close()
      file.length()
    } catch {
      case e: Exception =>
        println(s"Error writing ${file.getAbsolutePath} to $destName: ${e.getMessage()}")
        e.printStackTrace()
        0
    }
  }

  private def toBytes(file: File) = {
    Files.readAllBytes(file.toPath())
  }
}