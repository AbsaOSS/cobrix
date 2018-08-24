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

package za.co.absa.cobrix.spark.cobol.examples

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._
object StreamingExample {

  private final val PARAM_COPYBOOK = "-Dcopybook"
  private final val PARAM_DATA = "-Ddata"
  private final val PARAM_PARQUET_DESTINATION = "-DparquetDestination"

  def main(args: Array[String]): Unit = {
     
    if (args.length < 2) {
      println("Informed parameters: " + args.mkString)
      println(s"Usage parameters: $PARAM_COPYBOOK=path_to_copybook $PARAM_DATA=path_to_binary_data_dir [optional: -DparquetDestination]")
      System.exit(1)
    }
    
    val paramMap = parseArguments(args)        
    
    val spark = SparkSession
      .builder()
      .appName("CobolParser")
      .master("local[2]")
      .config("duration", 2)
      .config(PARAM_COPYBOOK_PATH, paramMap(PARAM_COPYBOOK))
      .config(PARAM_SOURCE_PATH, paramMap(PARAM_DATA))
      .getOrCreate()          
      
    // user is responsible for managing the streaming context
    val streamingContext = new StreamingContext(spark.sparkContext, Seconds(3))         
    
    // imports the Cobol deserializer for streams
    import za.co.absa.cobrix.spark.cobol.source.streaming.CobolStreamer._
    
    // gets a Cobol stream
    val result = streamingContext.cobolStream()    
        
    val reader = getReader(streamingContext)
    
    val filtered = result.filter(row => row.getAs[Integer]("RECORD.COMPANY-ID-NUM") % 2 == 0)
    
    // perform queries here        
    val pairs = filtered.map(row => {
      for (field <- reader.getSparkSchema.fields) yield (field.name, row.getAs[Any](field.name))
    })
    
    pairs.foreachRDD(rdd => {
      rdd.foreach(array => {
        println("*** RECORD ***")
        array.foreach(pair => println(s"${pair._1} = ${pair._2}"))
      })
    })
    
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def parseArguments(args: Array[String]): Map[String, String] = {
    args.map(param => {
      val tokens = param.split("=")
      (tokens(0), tokens(1))
    })
      .toMap
  }
}  
