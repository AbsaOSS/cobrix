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

package za.co.absa.cobrix.spark.cobol.source.parameters

import java.io.FileNotFoundException
import java.security.InvalidParameterException
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf

object CobolParameters {
    
  val SHORT_NAME = "cobol"
  val PARAM_COPYBOOK_PATH = "copybook"
  val PARAM_SOURCE_PATH = "path"  
  
  def validateOrThrow(sparkConf: SparkConf, hadoopConf: Configuration): Unit = {
    val parameters = Map[String,String](PARAM_COPYBOOK_PATH -> sparkConf.get(PARAM_COPYBOOK_PATH), PARAM_SOURCE_PATH -> sparkConf.get(PARAM_SOURCE_PATH))
    validateOrThrow(parameters,hadoopConf)
  }
  
  def validateOrThrow(parameters: Map[String, String], hadoopConf: Configuration): Unit = {
    parameters.getOrElse(PARAM_COPYBOOK_PATH, throw new IllegalStateException(s"Cannot define path to Copybook file: missing parameter: '$PARAM_COPYBOOK_PATH'"))
    parameters.getOrElse(PARAM_SOURCE_PATH, throw new IllegalStateException(s"Cannot define path to source files: missing parameter: '$PARAM_SOURCE_PATH'"))            
    
    val hdfs = FileSystem.get(hadoopConf)
        
    if (!hdfs.exists(new Path(parameters(PARAM_COPYBOOK_PATH)))) {
      throw new FileNotFoundException(s"Copybook not found at ${parameters(PARAM_COPYBOOK_PATH)}")
    }
    if (!hdfs.exists(new Path(parameters(PARAM_SOURCE_PATH)))) {
      throw new FileNotFoundException(s"Data source not found at ${parameters(PARAM_SOURCE_PATH)}")
    }           
    if (!hdfs.isFile(new Path(parameters(PARAM_COPYBOOK_PATH)))) {
      throw new InvalidParameterException(s"Value does not point at a valid Copybook file: ${parameters(PARAM_COPYBOOK_PATH)}")      
    }       
  }  
}