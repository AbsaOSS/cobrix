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

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Group

import scala.collection.immutable.HashMap

class ParserUtilsSpec extends WordSpec {

  private val simpleCopybook =
    """      01 RECORD.
      |        02 RECORD-1.
      |           03 FIELD1 PIC X(2).
      |        02 Z-RECORD.
      |           03 FIELD2 PIC X(2).
    """.stripMargin

  private val hierarchicalCopybook =
    """      01 RECORD.
      |        02 RECORD-1.
      |           03 FIELD-1 PIC X(2).
      |        02 SEGMENT-A.
      |           03 FIELD-2 PIC X(2).
      |        02 SEGMENT-B REDEFINES SEGMENT-A.
      |           03 FIELD-3 PIC S9(6) COMP.
      |        02 SEGMENT-C REDEFINES SEGMENT-A.
      |           03 FIELD-4 PICTURE S9(6) COMP.
      |        02 SEGMENT-D REDEFINES SEGMENT-A.
      |           03 FIELD-5 PICTURE S9(6) COMP.
      |        02 Z-RECORD.
      |           03 FIELD-6 PIC X(2).
    """.stripMargin

  private val segmentRedefines = "SEGMENT-A" :: "SEGMENT-B" :: "SEGMENT-C" :: "SEGMENT-D" :: Nil
  private val fieldParentMap = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "SEGMENT-A", "SEGMENT-D" -> "SEGMENT-B")

  "CopybookParser.findCycleIntAMap" should {
    "return an empty list if the input map is empty" in {
      val m = HashMap[String, String]()
      assert(CopybookParser.findCycleIntAMap(m).isEmpty)
    }

    "return an empty list if there are no cycles in the map" in {
      val m = HashMap[String, String]("A" -> "B", "B" -> "C", "X" -> "A", "Y" -> "A")
      assert(CopybookParser.findCycleIntAMap(m).isEmpty)
    }

    "return a list of fields for a trivial self-reference cycle" in {
      val m = HashMap[String, String]("A" -> "B", "C" -> "C")
      assert(CopybookParser.findCycleIntAMap(m) == "C" :: "C" :: Nil)
    }

    "return a list of fields for a multiple fields cycle chain self-reference cycle" in {
      val m = HashMap[String, String]("A" -> "B", "B" -> "C", "C" -> "D", "D" -> "A")
      val cycle = CopybookParser.findCycleIntAMap(m)

      // Due the nature of HashMap the cycle elements can start from any cycle element
      assert(cycle.contains("A"))
      assert(cycle.contains("B"))
      assert(cycle.contains("C"))
      assert(cycle.contains("D"))
      assert(cycle.head == cycle.last)
    }

    "return a cycle part of a path that contains a cycle" in {
      val m = HashMap[String, String]("0" -> "A", "A" -> "B", "B" -> "C", "C1" -> "C", "C" -> "D", "D1" -> "E", "D" -> "B")
      val cycle = CopybookParser.findCycleIntAMap(m)

      // Due the nature of HashMap the cycle elements can start from any cycle element
      assert(cycle.contains("B"))
      assert(cycle.contains("C"))
      assert(cycle.contains("D"))
      assert(cycle.head == cycle.last)
    }
  }

  "CopybookParser.getAllSegmentRedefines" should {
    "return an empty list if no segment redefines are defined" in {
      val segmentRedefines: Seq[String] = Nil
      val fieldParentMap = HashMap[String, String]()

      val parsedCopybook = CopybookParser.parseTree(simpleCopybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

      val redefines = CopybookParser.getAllSegmentRedefines(parsedCopybook.ast)

      assert(redefines.isEmpty)
    }

    "return a list of segment redefines for a hierarchical copybook" in {
      val parsedCopybook = CopybookParser.parseTree(hierarchicalCopybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

      val redefines = CopybookParser.getAllSegmentRedefines(parsedCopybook.ast)

      assert(redefines.size == 4)
    }
  }

  "CopybookParser.getRootSegmentAST" should {
    "return the same AST if no parent segments are defined" in {
      val segmentRedefines: Seq[String] = Nil
      val fieldParentMap = HashMap[String, String]()

      val parsedCopybook = CopybookParser.parseTree(simpleCopybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

      val rootAst = CopybookParser.getRootSegmentAST(parsedCopybook.ast)

      assert(rootAst.children.nonEmpty)
      assert(rootAst.children.head.asInstanceOf[Group].children.nonEmpty)
      assert(rootAst.children.head.asInstanceOf[Group].children.size == 2)
    }

    "return an AST without parent segments for a hierarchical copybook" in {
      val parsedCopybook = CopybookParser.parseTree(hierarchicalCopybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

      val rootAst = CopybookParser.getRootSegmentAST(parsedCopybook.ast)

      assert(rootAst.children.nonEmpty)

      val record = rootAst.children.head.asInstanceOf[Group]
      assert(record.children.size == 3)

      val record1 = record.children.head.asInstanceOf[Group]
      assert(record1.children.size == 1)
      assert(record1.children.head.name == "FIELD_1")

      val segmentA = record.children(1).asInstanceOf[Group]
      assert(segmentA.children.size == 1)
      assert(segmentA.children.head.name == "FIELD_2")

      val zrecord = record.children(2).asInstanceOf[Group]
      assert(zrecord.children.size == 1)
      assert(zrecord.children.head.name == "FIELD_6")
    }
  }

  "CopybookParser.getRootSegmentIds" should {
    "return an empty string is neither map is defined" in {
      val rootSegmentIds = CopybookParser.getRootSegmentIds(HashMap[String, String](), HashMap[String, String]())
      assert(rootSegmentIds.isEmpty)
    }

    "return a root segment id for a simple 2 segment tree" in {
      val rootSegmentIds = CopybookParser.getRootSegmentIds(
        HashMap[String, String]("1" -> "A", "2" -> "B"),
        HashMap[String, String]("B" -> "A"))
      assert(rootSegmentIds.size == 1)
      assert(rootSegmentIds.head == "1")
    }

    "return a root segment ids for a multiple root forest" in {
      val rootSegmentIds = CopybookParser.getRootSegmentIds(
        HashMap[String, String]("1" -> "A", "2" -> "B", "3" -> "C", "4" -> "D"),
        HashMap[String, String]("B" -> "A", "D" -> "C"))
      assert(rootSegmentIds.size == 2)
      assert(rootSegmentIds.contains("1"))
      assert(rootSegmentIds.contains("3"))
    }

    "return a root segment ids for a root having multiple segment id values" in {
      val rootSegmentIds = CopybookParser.getRootSegmentIds(
        HashMap[String, String]("1" -> "A", "2" -> "B", "3" -> "C", "4" -> "A"),
        HashMap[String, String]("B" -> "A", "C" -> "D"))
      assert(rootSegmentIds.size == 2)
      assert(rootSegmentIds.contains("1"))
      assert(rootSegmentIds.contains("4"))
    }
  }

}
