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

package com.example.spark.cobol.app

import org.apache.spark.sql.SparkSession

/**
  * This is an example Spark/Cobol Application.
  *
  * To run this locally use `mvn test`
  * or change Scala and Spark dependencies from `provided` to `compile` in pom.xml
  *
  * To run this on cluster generate the uber jar by running
  * `mvn package` and use `spark-submit` on cluster.
  */
object SparkTypesApp {

  /**
    * <h2>This is an example Cobrix Spark Job.</h2>
    * The file is a fixed length raw records file.
    * It consists of over a hundred records of different data types. Most fields contain numbers.
    *
    * This is an example of a one of the simplest possible Spark jobs for reading mainframe data.
    *
    * To load a mainframe data file as a Spark Data Frame you need to:
    * <ul><li>Specify "cobol" as the input data format</li>
    * <li>Specify the path to the copybook. Use "file:///" if the copybook is located in local file system. Otherwise it will be loaded from HDFS</li>
    * <li>Specify HDFS path location of the input binary mainframe file.</li>
    * <li>[Optinally] Specify `schema_retention_polity=collapse_root` so that root element won't be added to the Spark schema</li></ul>
    *
    */
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Spark-Cobol Type Varieties Example")
      .getOrCreate()

    import spark.implicits._

    val df = spark
      .read
      .format("cobol")                                        // Alternatively can use "za.co.absa.cobrix.spark.cobol.source"
      .option("copybook", "file://../example_data/data_types.cpy")    // A copybook can be provided inline
      .option("schema_retention_policy", "collapse_root")             // Collapses the root group returning it's field on the top level of the schema
      .load("../example_data/data_types")                       // Location of data file(s)

    df.printSchema()

    // Showing the first ten records
    df.select($"ID", $"STRING_VAL", $"NUM_STR_INT01", $"NUM_STR_INT09", $"NUM_STR_INT12", $"NUM_STR_DEC06", $"FLOAT_01", $"DOUBLE_01")
      .show(10, truncate = false)

    // Showing the first record as json
    df.toJSON.take(1).foreach(println)
  }

}
