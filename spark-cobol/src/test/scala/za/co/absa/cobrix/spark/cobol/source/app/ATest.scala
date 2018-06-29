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

package za.co.absa.cobrix.spark.cobol.source.app

import scala.collection._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.hadoop.conf.Configuration

object ATest {
  def main(args: Array[String]): Unit = {

        val config = new SparkConf().setAppName("testing provider").setMaster("local")
    
        val sc = new SparkContext(config)
        val sqlContext = new SQLContext(sc)
    
        val df = sqlContext
                  .read
                  .format("za.co.absa.spark.cobol.source")
                  .option("copybook", "data/test1_copybook.cob")
                  .load("data/test1_data")
    
        df.printSchema()
    
        df.show()

  }
  
  
}