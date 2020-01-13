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

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.{FixedLenNestedReader, FixedLenReader}
import za.co.absa.cobrix.spark.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersValidator

import scala.collection.JavaConverters.asScalaBufferConverter

/**
 * Provides an integration point for adding streaming support to the Spark-Cobol library.
 * 
 * This version is experimental and does not yet provide support to schemas or structured streaming.
 */
object CobolStreamer {
  
  def getReader(implicit ssc: StreamingContext): FixedLenReader = {
    val copybooks = Seq(loadCopybookFromHDFS(ssc.sparkContext.hadoopConfiguration, ssc.sparkContext.getConf.get(PARAM_COPYBOOK_PATH)))
    new FixedLenNestedReader(copybooks,
      isEbcdic = true,
      CodePage.getCodePageByName("common"),
      floatingPointFormat = FloatingPointFormat.IBM,
      schemaRetentionPolicy = SchemaRetentionPolicy.CollapseRoot,
      stringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
      dropGroupFillers = true,
      nonTerminals = Seq(),
      readerProperties = ReaderParameters()
    )
  }
  
  implicit class Deserializer(@transient val ssc: StreamingContext) extends Serializable {

    CobolParametersValidator.validateOrThrow(ssc.sparkContext.getConf, ssc.sparkContext.hadoopConfiguration)
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