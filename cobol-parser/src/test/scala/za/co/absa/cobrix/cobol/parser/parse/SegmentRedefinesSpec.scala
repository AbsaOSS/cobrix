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
import za.co.absa.cobrix.cobol.parser.ast.Group

class SegmentRedefinesSpec extends FunSuite {

  test ("Test segment redefines should not throw if no segment redefines ar provided") {
    val copybook =
      """      01 RECORD.
        |        02 A-RECORD.
        |           03 FIELD0 PIC X(2).
        |        02 Z-RECORD.
        |           03 FIELD5 PIC X(2).
      """.stripMargin

    val segmentRedefines: Seq[String] = Nil

    CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines)
  }

  test ("Test segment redefines should worked if only one segment is specified") {
    val copybook =
      """      01 RECORD.
        |        02 SEGMENT-A.
        |           03 FIELD0 PIC X(2).
        |        02 Z-RECORD.
        |           03 FIELD5 PIC X(2).
      """.stripMargin

    val segmentRedefines = "SEGMENT-A" :: Nil

    val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines)

    assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(0).asInstanceOf[Group].isSegmentRedefine)
    assert(!parsedCopybook.ast.children.head.asInstanceOf[Group].children(1).asInstanceOf[Group].isSegmentRedefine)
  }

  test ("Test segment redefines mark all redefines at root level") {
    val copybook =
      """      01 RECORD.
        |        02 A-RECORD.
        |           03 FIELD0 PIC X(2).
        |        02 SEGMENT-A.
        |           03 FIELD1 PIC X(2).
        |        02 SEGMENT-B REDEFINES SEGMENT-A.
        |           03 FIELD3 PIC S9(6)usage COMP.
        |        02 SEGMENT-C REDEFINES SEGMENT-A.
        |           03 FIELD4 PICTURE S9(6)USAGE COMP.
        |        02 Z-RECORD.
        |           03 FIELD5 PIC X(2).
      """.stripMargin

    val segmentRedefinesOk = "SEGMENT-A" :: "SEGMENT-C" :: "SEGMENT-B" :: Nil
    val segmentRedefinesMissing = "SEGMENT-A" :: "SEGMENT-C" :: "SEGMENT-B" :: "SEGMENT-D" :: Nil

    val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefinesOk)

    // If a segment redefine is missing in the copybook an exception should be raised
    val exception1 = intercept[IllegalStateException] {
      CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefinesMissing)
    }
    assert(exception1.getMessage.contains("The following segment redefines not found: [ SEGMENT_D ]"))

    // Segment redefines should be correctly redefined
    assert(!parsedCopybook.ast.children.head.asInstanceOf[Group].children(0).asInstanceOf[Group].isSegmentRedefine)
    assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(1).asInstanceOf[Group].isSegmentRedefine)
    assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(2).asInstanceOf[Group].isSegmentRedefine)
    assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(3).asInstanceOf[Group].isSegmentRedefine)
    assert(!parsedCopybook.ast.children.head.asInstanceOf[Group].children(4).asInstanceOf[Group].isSegmentRedefine)
  }

  test ("Test segment redefines should be in the same redefined group") {
    val copybook =
      """      01 RECORD.
        |        02 A-RECORD.
        |           03 FIELD0 PIC X(2).
        |        02 SEGMENT-A.
        |           03 FIELD1 PIC X(2).
        |        02 SEGMENT-B REDEFINES SEGMENT-A.
        |           03 FIELD1 PIC X(2).
        |        02 B-RECORD.
        |           03 FIELD3 PIC S9(6)usage COMP.
        |        02 SEGMENT-C.
        |           03 FIELD4 PICTURE S9(6)USAGE COMP.
        |        02 SEGMENT-D REDEFINES SEGMENT-C.
        |           03 FIELD4 PICTURE S9(6)USAGE COMP.
        |        02 Z-RECORD.
        |           03 FIELD5 PIC X(2).
      """.stripMargin

    val segmentRedefines = "SEGMENT-A" :: "SEGMENT-B" :: "SEGMENT-C" :: "SEGMENT-D" :: Nil

    val exception1 = intercept[IllegalStateException] {
      CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines)
    }
    assert(exception1.getMessage.contains("The 'SEGMENT_C' field is specified to be a segment redefine."))
  }


}
