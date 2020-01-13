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

package za.co.absa.cobrix.spark.cobol.source

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenReader
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema
import za.co.absa.cobrix.spark.cobol.source.base.SparkCobolTestBase
import za.co.absa.cobrix.spark.cobol.source.base.impl.{DummyCobolSchema, DummyFixedLenReader}
import za.co.absa.cobrix.spark.cobol.source.parameters.LocalityParameters
import za.co.absa.cobrix.spark.cobol.source.utils.SourceTestUtils.{createFileInRandomDirectory, sampleCopybook}

class CobolRelationSpec extends SparkCobolTestBase with Serializable {

  private val copybookFileName: String = "testCopybookFile.cob"
  private var copybookFile: File = _
  private var sparkSchema: StructType = _
  private var testData: List[Map[String, Option[String]]] = _
  private var cobolSchema: CobolSchema = _
  private var oneRowRDD: RDD[Array[Byte]] = _

  private val localityParams = new LocalityParameters(false, false)

  before {
    copybookFile = createFileInRandomDirectory(copybookFileName, sampleCopybook)
    sparkSchema = createSparkSchema(createTestSchemaMap())
    testData = createTestData()
    cobolSchema = new DummyCobolSchema(sparkSchema)
    oneRowRDD = sqlContext.sparkContext.parallelize(List(Array[Byte]()))
  }

  after {
    // BE CAREFUL when changing this line, DO NOT point at the wrong directory
    println("Removing test dir: "+copybookFile.getParentFile.getAbsolutePath)
    FileUtils.deleteDirectory(copybookFile.getParentFile)
  }

  behavior of "CobolRelation"

  it should "return an RDD[Row] if data are correct" in {
    val testReader: FixedLenReader = new DummyFixedLenReader(sparkSchema, cobolSchema, testData)(() => Unit)
    val relation = new CobolRelation(copybookFile.getParentFile.getAbsolutePath,
      testReader,
      localityParams = localityParams,
      debugIgnoreFileSize = false)(sqlContext)
    val cobolData: RDD[Row] = relation.parseRecords(testReader, oneRowRDD)

    val cobolDataFrame = sqlContext.createDataFrame(cobolData, sparkSchema)
    cobolDataFrame.collect().foreach(row => {
      for (map <- testData) {
        val keys = map.keys.toList
        if (map(keys.head) == row.getAs(keys.head)) {
          for (i <- 1 until keys.length) {
            val fromMap = map(keys(i)).toString
            val fromRow = row.getAs(keys(i)).toString
            assert(fromMap == fromRow)
          }
        }
      }
    })
  }

  it should "manage exceptions from Reader" in {
    val exceptionMessage = "exception expected message"
    val testReader: FixedLenReader = new DummyFixedLenReader(sparkSchema, cobolSchema, testData)(() => throw new Exception(exceptionMessage))
    val relation = new CobolRelation(copybookFile.getParentFile.getAbsolutePath,
      testReader,
      localityParams = localityParams,
      debugIgnoreFileSize = false)(sqlContext)

    val caught = intercept[Exception] {
      relation.parseRecords(testReader, oneRowRDD).collect()
    }
    assert(caught.getMessage.contains(exceptionMessage))
  }

  it should "manage records with missing fields" in {
    val absentField = "absentField"
    val modifiedSparkSchema = sparkSchema.add(StructField(absentField, StringType, false))
    val testReader: FixedLenReader = new DummyFixedLenReader(modifiedSparkSchema, cobolSchema, testData)(() => Unit)
    val relation = new CobolRelation(copybookFile.getParentFile.getAbsolutePath,
      testReader,
      localityParams = localityParams,
      debugIgnoreFileSize = false)(sqlContext)
    
    val caught = intercept[SparkException] {
      relation.parseRecords(testReader, oneRowRDD).collect()
    }    
    
    assert(caught.getMessage.contains("key not found: absentField"))
  }

  private def createTestData(): List[Map[String, Option[String]]] = {
    List(
      Map("name" -> Some("Young Guy"), "id" -> Some("1"), "age" -> Some("20")),
      Map("name" -> Some("Adult Guy"), "id" -> Some("2"), "age" -> Some("30")),
      Map("name" -> Some("Senior Guy"), "id" -> Some("3"), "age" -> Some("40")),
      Map("name" -> Some("Very Senior Guy"), "id" -> Some("4"), "age" -> Some("50")))
  }

  private def createTestSchemaMap(): Map[String, Any] = {
    Map("name" -> "", "id" -> "1l", "age" -> "1")
  }
}