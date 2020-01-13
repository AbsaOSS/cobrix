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

package za.co.absa.cobrix.spark.cobol.source.utils

import scala.util.Random
import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenReader

/**
 * Provides utilities methods and data for testing the 'source' package.
 */
object SourceTestUtils {
  
    val rand = new Random()
  
    val sampleCopybook: String =
      """       01  RECORD.
        |      ******************************************************************
        |      *             This is an example COBOL copybook
        |      ******************************************************************
        |           05  BIN-INT                  PIC S9(4)  COMP.
        |           05  STRUCT-FLD.
        |               10  STR-FLD
        |                   PIC X(10).
        |           05  DATA-STRUCT.
        |               10  EXAMPLE-INT-FLD      PIC 9(07) COMP-3.
        |               10  EXAMPLE-STR-FLD      PIC X(06).
        |""".stripMargin

    val parsedSampleCopybook: String = "StructType(StructField(RECORD,StructType(StructField(BIN_INT,IntegerType,true), StructField(STRUCT_FLD,StringType," +
      "true), StructField(DATA_STRUCT,StructType(StructField(EXAMPLE_INT_FLD,IntegerType,true), StructField(EXAMPLE_STR_FLD,StringType,true)),true))," +
      "true))"

    def getRandomEmptyDir = {
     val randomDir = new File("randomTestDir_"+Math.abs(rand.nextInt()))
     randomDir.mkdir()
     randomDir
    }
    
    def createFile(root: File, name: String, content: String): File = {    
      val destination = new File(root, name)
      write(destination, content)     
      destination
    }    
  
    def write(file: File, content: String) = {
      try {FileUtils.write(file, content)} catch {case ex: Exception => ex.printStackTrace()}
    }
    
    /**
     * Creates a random directory then creates the file inside that directory.
     */
    def createFileInRandomDirectory(name: String, content: String): File = {      
      createFile(getRandomEmptyDir, name, content)
    }
}