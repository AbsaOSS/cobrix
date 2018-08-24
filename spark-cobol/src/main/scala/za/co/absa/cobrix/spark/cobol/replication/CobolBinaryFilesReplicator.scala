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

package za.co.absa.cobrix.spark.cobol.replication

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.apache.hadoop.conf.Configuration

import za.co.absa.cobrix.spark.cobol.replication.files.destination.generation.HDFSFileWriter
import za.co.absa.cobrix.spark.cobol.replication.files.destination.identification.IncrementalFileIdProvider
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.log4j.PropertyConfigurator

object CobolBinaryFilesReplicator {

  private val PARAM_SOURCE_DIR = "-DsourceDir"
  private val PARAM_DESTINATION_DIR = "-DdestinationDir"
  private val PARAM_MAX_SPACE_BYTES = "-DmaxSpaceBytes"
  private val PARAM_MAX_PARALLEL_REPLICATORS = "-DmaxParallelReplicators"
  private val PARAM_INITIAL_FILE_ID = "-DinitialFileId"

  def main(args: Array[String]): Unit = {

    if (args.length != 5) {
      println(wrongParametersHelperMessage())
      System.exit(1)
    }
    
    PropertyConfigurator.configure("log4j.properties")
    
    val params = parseArguments(args)
    
    val destDir = getDestinationDirectory(params)
    val maxAllowedSpaceBytes = getMaxAllowedSpaceBytes(params)
    val idProvider = new IncrementalFileIdProvider(getInitialDestinationFileId(params))
    val maxParallelReplicators = getMaxParallelReplicators(params)
    val files = new File(getFilesSourceDirectory(params)).listFiles()
    val conf = getConfiguration()
    
    val numWorkers = if (maxParallelReplicators < files.length) maxParallelReplicators else files.length

    println(s"[Cobol Binary Files Replicator] Destination directory: $destDir")
    println(s"[Cobol Binary Files Replicator] Maximum space allowed: $maxAllowedSpaceBytes")
    println(s"[Cobol Binary Files Replicator] Number of workers: $numWorkers")    
    
    val pool: ExecutorService = Executors.newFixedThreadPool(numWorkers)
    for (w <- 1 to numWorkers) {
      pool.execute(new HDFSFileWriter(destDir, files, maxAllowedSpaceBytes / numWorkers, conf, idProvider))
    }
    pool.shutdown()
  }  
  
  private def parseArguments(args: Array[String]): Map[String, String] = {
    args.map(param => {
      val tokens = param.split("=")
      (tokens(0), tokens(1))
    })
      .toMap
  }
  
  private def getDestinationDirectory(params: Map[String,String]): String = {
    params.getOrElse(PARAM_DESTINATION_DIR, throw new Exception(s"Missing parameter: $PARAM_DESTINATION_DIR"))
  }
  
  private def getMaxAllowedSpaceBytes(params: Map[String,String]): Long = {
    params.getOrElse(PARAM_MAX_SPACE_BYTES, throw new Exception(s"Missing parameter: $PARAM_MAX_SPACE_BYTES")).toLong
  }
  
  private def getInitialDestinationFileId(params: Map[String,String]): Int = {
    params.getOrElse(PARAM_INITIAL_FILE_ID, throw new Exception(s"Missing parameter: $PARAM_INITIAL_FILE_ID")).toInt
  }
  
  private def getMaxParallelReplicators(params: Map[String,String]): Int = {
    params.getOrElse(PARAM_MAX_PARALLEL_REPLICATORS, throw new Exception(s"Missing parameter: $PARAM_MAX_PARALLEL_REPLICATORS")).toInt
  }
  
  private def getFilesSourceDirectory(params: Map[String,String]): String = {
    params.getOrElse(PARAM_SOURCE_DIR, throw new Exception(s"Missing parameter: PARAM_SOURCE_DIR"))
  }
  
  private def wrongParametersHelperMessage() = {
    s"Usage parameters: $PARAM_SOURCE_DIR=dir_containing_files_to_replicate $PARAM_DESTINATION_DIR=hdfs_destination_dir $PARAM_MAX_SPACE_BYTES=maximum_allowed_space_in_bytes $PARAM_MAX_PARALLEL_REPLICATORS=maximum_number_of_replication_threads $PARAM_INITIAL_FILE_ID=first_replicated_file_id"
  }
  
  private def getConfiguration(): Configuration = {
    new Configuration()
  }
  
  private def showConf(conf: Configuration) = {
    val it = conf.iterator();
    while (it.hasNext()) {
      println(it.next())
    }
  }
}