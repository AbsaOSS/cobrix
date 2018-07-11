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

package za.co.absa.cobrix.spark.cobol.source.flatreader

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.reader.FlatReader
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase

class FlatReader1Spec extends FunSuite with SparkTestBase {

  private val exampleName = "Test1_Flat"
  private val inputCopybookPath = "../data/test1_copybook.cob"
  private val inpudDataPath = "../data/test1_data/example.bin"

  private val expectedSchemaPath = "../data/test1_expected/test1_flat_schema.json"
  private val actualSchemaPath = "../data/test1_expected/test1_flat_schema_actual.json"
  private val expectedResultsPath = "../data/test1_expected/test1.csv"
  private val actualResultsPath = "../data/test1_expected/test1_actual.csv"

  test(s"Flat reader test on $exampleName data") {
    val hadoopConfiguration = spark.sparkContext.hadoopConfiguration

    val copyBookContents = Files.readAllLines(Paths.get(inputCopybookPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val reader = new FlatReader(copyBookContents)
    val cobolSchema = reader.getCobolSchema
    val sparkSchema = reader.getSparkSchema

    // This is to print the actual output
    //println(sparkSchema.json)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    if (sparkSchema.json != expectedSchema) {
      val writer = new PrintWriter(actualSchemaPath)
      try {
        writer.write(sparkSchema.json)
      } finally {
        writer.close()
      }
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val byteArray = Files.readAllBytes(Paths.get(inpudDataPath))
    val actual = reader.generateDebugCsv(byteArray)

    // This is to print the actual output
    //println(actual)

    val it = reader.getRowIterator(byteArray)
    assert (it.next.size == sparkSchema.fields.length)

    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")

    if (actual != expected) {
      val writer = new PrintWriter(actualResultsPath)
      try {
        writer.write(actual)
      } finally {
        writer.close()
      }
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to " +
        s"$actualResultsPath for details.")
    }
  }

}
