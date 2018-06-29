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

package za.co.absa.cobrix.spark.cobol

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

class CobolSchemaSpec extends FunSuite {

  val copyBookContents: String =
    """       01  RECORD.
      |      ******************************************************************
      |      *             This is an example COBOL copybook
      |      ******************************************************************
      |           05  BIN-INT                  PIC S9(4)  COMP.
      |           05  STRUCT-FLD
      |               10  STR-FLD
      |                   PIC X(10).
      |           05  DATA-STRUCT.
      |               10  EXAMPLE-INT-FLD      PIC 9(07) COMP-3.
      |               10  EXAMPLE-STR-FLD      PIC X(06).
      |""".stripMargin

  val expectedSchema: String = "StructType(StructField(RECORD,StructType(StructField(BIN_INT,IntegerType,true), StructField(STRUCT_FLD,StringType," +
    "true), StructField(DATA_STRUCT,StructType(StructField(EXAMPLE_INT_FLD,IntegerType,true), StructField(EXAMPLE_STR_FLD,StringType,true)),true))," +
    "true))"

  test("Test simple Spark schema devivation from a Copybook") {
    val parsedSchema = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    val cobolSchema = new CobolSchema(parsedSchema)
    val actualSchemaJson = cobolSchema.getSparkSchema.toString()

    assert(actualSchemaJson == expectedSchema)
  }

}
