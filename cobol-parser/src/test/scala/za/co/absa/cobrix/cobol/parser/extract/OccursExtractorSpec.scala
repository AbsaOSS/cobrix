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

package za.co.absa.cobrix.cobol.parser.extract

import java.nio.charset.{Charset, StandardCharsets}

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Group, Primitive}
import za.co.absa.cobrix.cobol.parser.decoders.{DecoderSelector, FloatingPointFormat}
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC}
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.policies.{CommentPolicy, StringTrimmingPolicy}

import scala.collection.immutable.HashMap

class OccursExtractorSpec extends AnyFunSuite {

  val copyBookContents: String =
    """       01  RECORD.
      |           05  ID                        PIC 9(1).
      |           05  DATA.
      |               10  FIELD                 PIC X.
      |               10  DETAIL1       OCCURS 0 TO 2 TIMES
      |                                 DEPENDING ON FIELD.
      |                   15  VAL1       PIC X.
      |               10  DETAIL2       OCCURS 0 TO 2 TIMES
      |                                 DEPENDING ON FIELD.
      |                   15  VAL2       PIC X.
      |      |""".stripMargin

  test("Test OCCURS fails on non integral DEPENDING ON") {
    val exc = intercept[IllegalStateException] {
      CopybookParser.parseTree(copyBookContents)
    }

    assert(exc.getMessage.contains("Field FIELD is a DEPENDING ON field of an OCCURS, should be integral"))
  }


  test("Test OCCURS on non integral DEPENDING ON and handlers") {

    val occursMapping: Map[String, Map[String, Int]] = Map(
      "DETAIL1" -> Map(
        "A" -> 0,
        "B" -> 1
      ),
      "DETAIL2" -> Map(
        "A" -> 1,
        "B" -> 2
      )
    )

    CopybookParser.parseTree(copyBookContents, occursHandlers = occursMapping)
  }

}
