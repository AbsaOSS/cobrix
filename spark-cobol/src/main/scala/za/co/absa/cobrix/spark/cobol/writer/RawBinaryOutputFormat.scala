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

package za.co.absa.cobrix.spark.cobol.writer

import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce._
import org.apache.hadoop.io.{BytesWritable, NullWritable}
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.getOutputPath

import java.io.DataOutputStream
import java.util.UUID

/**
  * A custom implementation of `FileOutputFormat` that outputs raw binary data for fixed record length
  * outputs or for variable record length outputs when record size headers are already embedded into
  * each record array of bytes.
  *
  * The `RawBinaryOutputFormat` class is designed to write binary data into output files
  * without adding any additional structure or metadata. Each record is directly written
  * as a stream of bytes to the output.
  *
  * This output format only handles records that are represented as `BytesWritable` and ignores the key.
  *
  * - The key type for the output is `NullWritable` because the key is not used.
  * - The value type for the output is `BytesWritable`, which represents the binary data to be written.
  */

class RawBinaryOutputFormat extends FileOutputFormat[NullWritable, BytesWritable] {
  private val uniqueUuid = UUID.randomUUID().toString

  override def checkOutputSpecs(job: JobContext): Unit = {
    val outDir = getOutputPath(job)
    if (outDir == null) throw new IllegalStateException("Output directory not set.")
  }

  override def getDefaultWorkFile(context: TaskAttemptContext, extension: String): Path = {
    val conf = context.getConfiguration
    val uniqueWriteJobId = conf.get("spark.sql.sources.writeJobUUID")
    val idFilePart = if (uniqueWriteJobId == null) uniqueUuid else uniqueWriteJobId
    val taskAttemptID = context.getTaskAttemptID
    val taskId = f"${taskAttemptID.getTaskID.getId}%05d"
    val attemptId = f"c${taskAttemptID.getId}%03d"

    val filename = s"part-$taskId-$idFilePart-$attemptId$extension"
    val outputPath = getOutputPath(context)
    new Path(outputPath, filename)
  }

  override def getRecordWriter(context: TaskAttemptContext): RecordWriter[NullWritable, BytesWritable] = {
    val extension = context.getConfiguration.get("cobol.writer.output.extension", ".dat")
    val path: Path = getDefaultWorkFile(context, extension)
    val fs = path.getFileSystem(context.getConfiguration)
    val out: DataOutputStream = fs.create(path, false)

    new RecordWriter[NullWritable, BytesWritable] {
      override def write(key: NullWritable, value: BytesWritable): Unit = {
        if (value != null) {
          out.write(value.getBytes, 0, value.getLength) // No separator
        }
      }
      override def close(context: TaskAttemptContext): Unit = {
        out.close()
      }
    }
  }
}

