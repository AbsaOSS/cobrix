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

package za.co.absa.cobrix.spark.cobol.source.streaming

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.streaming.StreamingContext
import scala.collection.JavaConverters.asScalaBufferConverter
import za.co.absa.cobrix.spark.cobol.reader.NestedReader
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParameters
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.sql.Row
import za.co.absa.cobrix.spark.cobol.reader.Reader
import org.apache.spark.SparkConf
import za.co.absa.cobrix.spark.cobol.reader.ReaderFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.spark.cobol.reader.FlatReader

/**
 * Provides an integration point for adding streaming support to the Spark-Cobol library.
 * 
 * This version is experimental and does not yet provide support to schemas or structured streaming.
 */
object CobolStreamer {

  import CobolParameters._
  
  def getReader(implicit ssc: StreamingContext): Reader = {
    new FlatReader(loadCopybookFromHDFS(ssc.sparkContext.hadoopConfiguration, ssc.sparkContext.getConf.get(PARAM_COPYBOOK_PATH)))
  }
  
  implicit class Deserializer(@transient val ssc: StreamingContext) extends Serializable {

    CobolParameters.validateOrThrow(ssc.sparkContext.getConf, ssc.sparkContext.hadoopConfiguration)
    val reader = CobolStreamer.getReader(ssc)
    
    def cobolStream(): DStream[Row] = {
      ssc
        .binaryRecordsStream(ssc.sparkContext.getConf.get(PARAM_SOURCE_PATH), reader.getCobolSchema.getRecordSize)
        .flatMap(record => {          
          val it = reader.getRowIterator(record)
          for (parsedRecord <- it) yield {                        
            new GenericRowWithSchema(parsedRecord.toSeq.toArray, reader.getSparkSchema)
          }
        })
    }
  }

  private def loadCopybookFromHDFS(hadoopConfiguration: Configuration, copyBookHDFSPath: String): String = {
    val hdfs = FileSystem.get(hadoopConfiguration)
    val stream = hdfs.open(new Path(copyBookHDFSPath))
    try IOUtils.readLines(stream).asScala.mkString("\n") finally stream.close()
  }

}