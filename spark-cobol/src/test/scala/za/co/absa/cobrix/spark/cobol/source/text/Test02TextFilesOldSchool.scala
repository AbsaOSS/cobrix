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

package za.co.absa.cobrix.spark.cobol.source.text

import java.nio.charset.StandardCharsets

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.ASCII
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordExtractors
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.reader.RowHandler
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test02TextFilesOldSchool extends FunSuite with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  RECORD.
           05  A1       PIC X(1).
           05  A2       PIC X(5).
           05  A3       PIC X(10).
    """

  private val textFileContent: String =
    Seq("1Tes  0123456789",
        "2 est2 SomeText ",
        "3None Data+3    ",
        "4 on      Data 4").mkString("\n")

  test("Test a the 'old school' way of handling ASCII text files") {
    withTempTextFile("text_ascii", ".txt", StandardCharsets.UTF_8, textFileContent) { tmpFileName =>

      val parsedCopybook = CopybookParser.parse(copybook, dataEnncoding = ASCII, stringTrimmingPolicy = StringTrimmingPolicy.TrimNone)
      val cobolSchema = new CobolSchema(parsedCopybook, SchemaRetentionPolicy.CollapseRoot, "", false)
      val sparkSchema = cobolSchema.getSparkSchema

      val rddText = spark.sparkContext
        .textFile(tmpFileName)

      val recordHandler = new RowHandler()

      val rddRow = rddText
        .filter(str => str.length > 0)
        .map(str => {
          val record = RecordExtractors.extractRecord[GenericRow](parsedCopybook.ast,
            str.getBytes(),
            0,
            SchemaRetentionPolicy.CollapseRoot, handler = recordHandler)
          Row.fromSeq(record)
        })

      val df = spark.createDataFrame(rddRow, sparkSchema)

      val expected = """[{"A1":"1","A2":"Tes  ","A3":"0123456789"},{"A1":"2","A2":" est2","A3":" SomeText "},{"A1":"3","A2":"None ","A3":"Data+3    "},{"A1":"4","A2":" on  ","A3":"    Data 4"}]"""

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assertEqualsMultiline(actual, expected)
    }
  }

}
