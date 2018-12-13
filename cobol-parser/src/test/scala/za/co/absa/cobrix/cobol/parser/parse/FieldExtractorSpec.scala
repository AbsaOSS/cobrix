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

package za.co.absa.cobrix.cobol.parser.parse

import org.scalatest.FunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.ast.datatype.AlphaNumeric
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC

class FieldExtractorSpec extends FunSuite {

  val copyBookContents: String =
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
      |           05  DATA-STRUCT2.
      |               10  EXAMPLE-INT-FLD      PIC 9(07) COMP-3.
      |               10  EXAMPLE-STR-FLD      PIC X(08).
      |""".stripMargin

  test("Test AST objects extracted by names are correct") {
    val parsedSchema = CopybookParser.parseTree(copyBookContents)

    val field1 = parsedSchema.getFieldByName("BIN-INT")
    assert(field1.name.equalsIgnoreCase("bin_int"))

    val field2 = parsedSchema.getFieldByName("str_fld")
    assert(field2.name.equalsIgnoreCase("STR_FLD"))

    val exception1 = intercept[IllegalStateException] {
      parsedSchema.getFieldByName("EXAMPLE-INT-FLD")
    }
    assert(exception1.getMessage.contains("Multiple fields with name"))

    val exception2 = intercept[IllegalStateException] {
      parsedSchema.getFieldByName("FIELD-NOT-FOUND")
    }
    assert(exception2.getMessage.contains("is not found in the copybook"))

    val field3 = parsedSchema.getFieldByName("RECORD.STRUCT-FLD.STR-FLD")
    assert(field3.name.equalsIgnoreCase("STR_FLD"))

    val field4 = parsedSchema.getFieldByName("RECORD.DATA-STRUCT2.EXAMPLE-STR-FLD")
    assert(field4.name.equalsIgnoreCase("EXAMPLE_STR_FLD"))
    assert(field4.asInstanceOf[Primitive].dataType.asInstanceOf[AlphaNumeric].length == 8)

    // If a copybook has only 1 root group, the name of the group can be omitted in the path
    val field5 = parsedSchema.getFieldByName("DATA-STRUCT2.EXAMPLE-STR-FLD")
    assert(field4.name.equalsIgnoreCase("EXAMPLE_STR_FLD"))
    assert(field4.asInstanceOf[Primitive].dataType.asInstanceOf[AlphaNumeric].length == 8)
  }



}
