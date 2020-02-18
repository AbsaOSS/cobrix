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

package za.co.absa.cobrix.spark.cobol.source.integration

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.ASCII
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePageCommon
import za.co.absa.cobrix.cobol.parser.policies.{CommentPolicy, StringTrimmingPolicy}
import za.co.absa.cobrix.cobol.parser.recordextractors.VarOccursRecordExtractor
import za.co.absa.cobrix.cobol.parser.stream.{FSStream, SimpleStream}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.{FileUtils, RowExtractors}

import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer

class Test21VariableOccurs extends FunSuite with SparkTestBase {

  private val exampleName = "Test21"
  private val inputCopybookPath = "file://../data/test21_copybook.cob"
  private val inputDataPath = "../data/test21_data"

  private val expectedResultsPath = "../data/test21_expected/test21.txt"
  private val actualResultsPath = "../data/test21_expected/test21_actual.txt"
  private val expectedSchemaPath = "../data/test21_expected/test21_schema.json"
  private val actualSchemaPath = "../data/test21_expected/test21_schema_actual.json"

  test("Test VarLenReader properly splits a file into records") {
    val inputStream = new FSStream(s"$inputDataPath/data.dat")
    val copybookContents = Files.readAllLines(Paths.get("../data/test21_copybook.cob"), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val copybook = CopybookParser.parseTree(ASCII(), copybookContents, true, Nil, HashMap[String, String](), StringTrimmingPolicy.TrimBoth,
      CommentPolicy(), new CodePageCommon, StandardCharsets.US_ASCII, FloatingPointFormat.IBM, Nil)
    val recordExtractor = new VarOccursRecordExtractor(inputStream, copybook)

    val expectedRecords = ListBuffer(Array(48.toByte),
      Array(49.toByte, 48.toByte),
      Array(49.toByte, 50.toByte, 65.toByte, 66.toByte),
      Array(50.toByte, 49.toByte, 65.toByte, 50.toByte, 66.toByte, 67.toByte),
      Array(50.toByte, 49.toByte, 65.toByte, 49.toByte, 66.toByte))

    val records = new ListBuffer[Array[Byte]]
    while (recordExtractor.hasNext) {
      records += recordExtractor.next
    }

    assert(records.length == 5)
    val sameData = records.zip(expectedRecords).forall{ case (a1, a2) => a1.sameElements(a2)}
    assert(sameData)
  }

  test(s"Integration test on $exampleName data") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("encoding", "ascii")
      .option("variable_size_occurs", "true")
      .load(inputDataPath)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      FileUtils.writeStringToFile(actualSchema, actualSchemaPath)
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      FileUtils.writeStringsToFile(actual, actualResultsPath)
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }

}
