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

import org.apache.spark.sql.{SQLContext, SaveMode, SparkSession}
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object SampleApp {

  private final val PARAM_COPYBOOK = "-Dcopybook"
  private final val PARAM_DATA = "-Ddata"
  private final val PARAM_PARQUET_DESTINATION = "-DparquetDestination"
  
  def main(args: Array[String]): Unit = {
    
    if (args.length < 2) {      
      println("Informed parameters: "+args.mkString)
      println(s"Usage parameters: $PARAM_COPYBOOK=path_to_copybook $PARAM_DATA=path_to_binary_data_dir [optional: -DparquetDestination]")
      System.exit(1)
    }
    
    val paramMap = parseArguments(args)
    
    val config = new SparkConf().setAppName("CobolParser")    
    val sqlContext = SparkSession.builder().config(config).getOrCreate()

    val df = sqlContext
      .read
      .format("za.co.absa.spark.cobol.source")      
      .option("copybook", paramMap(PARAM_COPYBOOK))
      .load(paramMap(PARAM_DATA))

    df.printSchema()

    if (paramMap.contains(PARAM_PARQUET_DESTINATION)) {
      println(s"Writing from ${paramMap(PARAM_DATA)} to ${paramMap(PARAM_PARQUET_DESTINATION)}.")
      df.write.mode(SaveMode.Overwrite).parquet(paramMap(PARAM_PARQUET_DESTINATION))
    }
    else {
      println(s"Reading from ${paramMap(PARAM_DATA)} and applying filter.")
      df.filter("RECORD.COMPANY-ID-NUM % 2 = 0").take(10).foreach(v => println(v))
    }
  }

  private def parseArguments(args: Array[String]): Map[String,String] = {    
    args.map(param => {
      val tokens = param.split("=" )
      (tokens(0), tokens(1))
    })
    .toMap    
  }
}