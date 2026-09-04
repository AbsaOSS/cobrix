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

package za.co.absa.cobrix.cobol.parser

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.expression.ExpressionEvaluator
import za.co.absa.cobrix.cobol.parser.policies.VariableSizeOccursPolicy

class CopybookSuite extends AnyWordSpec {
  val copybookStr: String =
    """      01 RECORD.
      |        05   SEGMENT-ID   PIC X(1).
      |        05   SEG1.
      |           10 RT1      PIC X(1).
      |           10 V11      PIC X(3).
      |           10 V12      PIC 9(9) REDEFINES V11.
      |        05   SEG2 REDEFINES SEG1.
      |           10 RT2      PIC X(1).
      |           10 V21      PIC X(3).
      |           10 V22      PIC 9(9) REDEFINES V21.
      |        05   SEG3 REDEFINES SEG1.
      |           10 CNT      PIC 9(1).
      |           10 AR       PIC X(1) OCCURS 5 TIMES
      |                            DEPENDING ON CNT.
      |           10 RT3      PIC X(1).
      |           10 V31      PIC X(3).
      |           10 V32      PIC 9(9) REDEFINES V31.
      |""".stripMargin

  val exampleRecordSeg1Type1: Array[Byte] = Array(0xF1, 0xF1, 0xC1, 0xC2, 0xC3).map(_.toByte)
  val exampleRecordSeg1Type2: Array[Byte] = Array(0xF1, 0xF2, 0xF1, 0xF2, 0xF3).map(_.toByte)
  val exampleRecordSeg2Type1: Array[Byte] = Array(0xF2, 0xF1, 0xC4, 0xC5, 0xC6).map(_.toByte)
  val exampleRecordSeg2Type2: Array[Byte] = Array(0xF2, 0xF2, 0xF4, 0xF5, 0xF6).map(_.toByte)
  val exampleRecordSeg3Type1Shift: Array[Byte] = Array(0xF3, 0xF1, 0xC1, 0xF1, 0xC1, 0xC2, 0xC3).map(_.toByte)
  val exampleRecordSeg3Type2Shift: Array[Byte] = Array(0xF3, 0xF1, 0x81, 0xF2, 0xF5, 0xF6, 0xF7).map(_.toByte)
  val exampleRecordSeg3Type1Max: Array[Byte] = Array(0xF3, 0xF2, 0xC1, 0xC2, 0x00, 0x00, 0x00, 0xF1, 0xC1, 0xC2, 0xC3).map(_.toByte)
  val exampleRecordSeg3Type2Max: Array[Byte] = Array(0xF3, 0xF2, 0x81, 0x82, 0x00, 0x00, 0x00, 0xF2, 0xF5, 0xF6, 0xF7).map(_.toByte)

  val segmentIdRedefineMap: Map[String, String] = Map (
    "1" -> "SEG1",
    "2" -> "SEG2",
    "3" -> "SEG3",
    "4" -> "SEG3"
  )

  val redefineRuleExpressions: Map[String, ExpressionEvaluator] = Map[String, ExpressionEvaluator] (
    "V11" -> new ExpressionEvaluator("RT1 = '1'"),
    "V12" -> new ExpressionEvaluator("RT1 = '2'"),
    "V21" -> new ExpressionEvaluator("RT2 = '1'"),
    "V22" -> new ExpressionEvaluator("RT2 = '2'"),
    "V31" -> new ExpressionEvaluator("RT3 = '1'"),
    "V32" -> new ExpressionEvaluator("RT3 = '2'")
  )

  val copybook: Copybook = CopybookParser.parse(copybookStr,
    segmentIdRedefineMap = segmentIdRedefineMap,
    redefineRuleExpressions = redefineRuleExpressions)

  "hasRedefineRules" should {
    "return false if there are no redefine rules" in {
      val copybookWithoutRedefineRules: Copybook = CopybookParser.parse(copybookStr)
      assert(!copybookWithoutRedefineRules.hasRedefineRules)
    }

    "return true if at least one rule is defined" in {
      assert(copybook.hasRedefineRules)
    }
  }

  "extractExpressionVariablesFromRecord" should {
    "extract variables properly for the segment 1, record type = 1" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg1Type1, Some("1"))

      assert(vars.size == 1)
      assert(vars.contains("RT1"))
      assert(vars("RT1") == "1")
    }

    "extract variables properly for the segment 1, record type = 2" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg1Type2, Some("1"))

      assert(vars.size == 1)
      assert(vars.contains("RT1"))
      assert(vars("RT1") == "2")
    }

    "extract variables properly for the segment 2, record type = 1" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg2Type1, Some("2"))

      assert(vars.size == 1)
      assert(vars.contains("RT2"))
      assert(vars("RT2") == "1")
    }

    "extract variables properly for the segment 2, record type = 2" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg2Type2, Some("2"))

      assert(vars.size == 1)
      assert(vars.contains("RT2"))
      assert(vars("RT2") == "2")
    }

    "extract variables properly for the segment 3, record type = 1, max_size" in {
      copybook.setVariableSizeOccursPolicy(VariableSizeOccursPolicy.MaxSize)
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg3Type1Max, Some("3"))

      assert(vars.size == 1)
      assert(vars.contains("RT3"))
      assert(vars("RT3") == "1")
    }

    "extract variables properly for the segment 3, record type = 2, max_size" in {
      copybook.setVariableSizeOccursPolicy(VariableSizeOccursPolicy.MaxSize)
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg3Type2Max, Some("3"))

      assert(vars.size == 1)
      assert(vars.contains("RT3"))
      assert(vars("RT3") == "2")
    }

    "extract variables properly for the segment 3, record type = 1, shifted" in {
      copybook.setVariableSizeOccursPolicy(VariableSizeOccursPolicy.PadRecord)
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg3Type1Shift, Some("3"))

      assert(vars.size == 1)
      assert(vars.contains("RT3"))
      assert(vars("RT3") == "1")
    }

    "extract variables properly for the segment 3, record type = 2, shifted" in {
      copybook.setVariableSizeOccursPolicy(VariableSizeOccursPolicy.ShiftRecord)
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg3Type2Shift, Some("3"))

      assert(vars.size == 1)
      assert(vars.contains("RT3"))
      assert(vars("RT3") == "2")
    }
  }

  "isFieldEnabled" should {
    "work for segment 1 record type 1" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg1Type1, Some("1"))
      val isField1Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V11"), Some("1"), vars)
      val isField2Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V12"), Some("1"), vars)
      val isField3Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V21"), Some("1"), vars)
      val isField4Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V22"), Some("1"), vars)

      assert(isField1Enabled)
      assert(!isField2Enabled)
      assert(!isField3Enabled)
      assert(!isField4Enabled)
    }

    "work for segment 1 record type 2" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg1Type2, Some("1"))
      val isField1Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V11"), Some("1"), vars)
      val isField2Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V12"), Some("1"), vars)
      val isField3Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V21"), Some("1"), vars)
      val isField4Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V22"), Some("1"), vars)

      assert(!isField1Enabled)
      assert(isField2Enabled)
      assert(!isField3Enabled)
      assert(!isField4Enabled)
    }

    "work for segment 2 record type 1" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg2Type1, Some("2"))

      val isField1Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V11"), Some("2"), vars)
      val isField2Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V12"), Some("2"), vars)
      val isField3Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V21"), Some("2"), vars)
      val isField4Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V22"), Some("2"), vars)

      assert(!isField1Enabled)
      assert(!isField2Enabled)
      assert(isField3Enabled)
      assert(!isField4Enabled)
    }

    "work for segment 2 record type 2" in {
      val vars = copybook.extractExpressionVariablesFromRecord(exampleRecordSeg2Type2, Some("2"))

      val isField1Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V11"), Some("2"), vars)
      val isField2Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG1.V12"), Some("2"), vars)
      val isField3Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V21"), Some("2"), vars)
      val isField4Enabled = copybook.isFieldEnabled(copybook.getFieldByName("SEG2.V22"), Some("2"), vars)

      assert(!isField1Enabled)
      assert(!isField2Enabled)
      assert(!isField3Enabled)
      assert(isField4Enabled)
    }

  }

  "isPartOfSegment" should {
    "work for segment 1" in {
      //assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG1.RT1"), "1"))
      //assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG1.V11"), "1"))
      //assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG1.V12"), "1"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG2.RT2"), "1"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG2.V21"), "1"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG2.V22"), "1"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG3.RT3"), "1"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG3.V31"), "1"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG3.V32"), "1"))
    }
    "work for segment 2" in {
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG1.RT1"), "2"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG1.V11"), "2"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG1.V12"), "2"))
      assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG2.RT2"), "2"))
      assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG2.V21"), "2"))
      assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG2.V22"), "2"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG3.RT3"), "2"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG3.V31"), "2"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG3.V32"), "2"))
    }
    "work for segment 3" in {
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG1.RT1"), "3"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG1.V11"), "3"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG1.V12"), "3"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG2.RT2"), "3"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG2.V21"), "3"))
      assert(!copybook.isPartOfSegment(copybook.getFieldByName("SEG2.V22"), "3"))
      assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG3.RT3"), "3"))
      assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG3.V31"), "3"))
      assert(copybook.isPartOfSegment(copybook.getFieldByName("SEG3.V32"), "3"))
    }
  }

}
