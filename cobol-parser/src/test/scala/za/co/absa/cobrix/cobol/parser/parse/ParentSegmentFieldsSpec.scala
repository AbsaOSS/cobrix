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

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.ast.Group

import scala.collection.immutable.HashMap

class ParentSegmentFieldsSpec extends WordSpec {

  "For a simple copybook" when {
    val copybook =
      """      01 RECORD.
        |        02 RECORD-1.
        |           03 FIELD1 PIC X(2).
        |        02 Z-RECORD.
        |           03 FIELD2 PIC X(2).
      """.stripMargin

    val segmentRedefines: Seq[String] = Nil
    val fieldParentMap = HashMap[String, String]()

    "CopybookParser.parseTree" should {
      "not throw if no segment redefines or parent fields are provided" in {
        CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)
      }
    }

    "CopybookParser.getParentToChildrenMap" should {
      "return an empty map" in {
        val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)
        val map = CopybookParser.getParentToChildrenMap(parsedCopybook.ast)

        assert(map.isEmpty)
      }
    }
  }

  "For a copybook with a single parent-child relationship" when {
    val copybook =
      """      01 RECORD.
        |        02 SEGMENT-A.
        |           03 FIELD1 PIC X(2).
        |        02 SEGMENT-B REDEFINES SEGMENT-A.
        |           03 FIELD2 PIC X(2).
        |        02 Z-RECORD.
        |           03 FIELD3 PIC X(2).
      """.stripMargin

    val segmentRedefines = "SEGMENT-A" :: "SEGMENT-B" :: Nil
    val fieldParentMap = HashMap[String, String]("SEGMENT-B" -> "SEGMENT-A")

    "CopybookParser.parseTree" should {
      "work with a simple 2 segments having a parent-child relationship" in {
        val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

        assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(0).asInstanceOf[Group].parentSegment.isEmpty)
        assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(1).asInstanceOf[Group].parentSegment.nonEmpty)
        assert(parsedCopybook.ast.children.head.asInstanceOf[Group].children(2).asInstanceOf[Group].parentSegment.isEmpty)
      }
    }

    "CopybookParser.getParentToChildrenMap" should {
      "return a single entity map" in {

        val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

        val map = CopybookParser.getParentToChildrenMap(parsedCopybook.ast)

        val grpA = map.keys.find(_ == "SEGMENT_A").get
        val childrenOfA = map(grpA)

        assert(childrenOfA.size == 1)
        assert(childrenOfA.head.name == "SEGMENT_B")
      }
    }
  }

  "For a more complicated hierarchical copybook" when {
    val copybook =
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

    val segmentRedefines = "SEGMENT-A" :: "SEGMENT-C" :: "SEGMENT-B" :: Nil
    val fieldParentMap = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "SEGMENT-A")

    "CopybookParser.getParentToChildrenMap" should {
      "return a proper parent-children map" in {

        val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

        val map = CopybookParser.getParentToChildrenMap(parsedCopybook.ast)

        val grpA = map.keys.find(_ == "SEGMENT_A").get
        val childrenOfA = map(grpA)
        val grpB = map.keys.find(_ == "SEGMENT_B").get
        val childrenOfB = map(grpB)
        val grpC = map.keys.find(_ == "SEGMENT_C").get
        val childrenOfC = map(grpC)

        assert(map.keys.size == 3)
        assert(childrenOfA.size == 2)
        assert(childrenOfA.head.name == "SEGMENT_B")
        assert(childrenOfA(1).name == "SEGMENT_C")
        assert(childrenOfB.isEmpty)
        assert(childrenOfC.isEmpty)
      }
    }

  }

  "CopybookParser.parseTree" should {
    "return a serializable copybook AST when redefines and parents are defined" in {
      val copybook =
        """      01 RECORD.
          |        02 SEGMENT-A.
          |           03 FIELD1 PIC X(2).
          |        02 SEGMENT-B REDEFINES SEGMENT-A.
          |           03 FIELD2 PIC X(2).
          |        02 SEGMENT-C REDEFINES SEGMENT-A.
          |           03 FIELD3 PIC X(2).
          |        02 Z-RECORD.
          |           03 FIELD4 PIC X(2).
        """.stripMargin

      val segmentRedefines = "SEGMENT-A" :: "SEGMENT-B" :: "SEGMENT-C" :: Nil
      val fieldParentMap = HashMap[String, String]("SEGMENT-B" -> "SEGMENT-A", "SEGMENT-C" -> "SEGMENT-B")

      val parsedCopybook = CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMap)

      val bos = new ByteArrayOutputStream
      val out = new ObjectOutputStream(bos)
      out.writeObject(parsedCopybook)
      out.flush()

      assert(bos.toByteArray.nonEmpty)
    }

    "validate correctness of the provided parent-child relationship" when {
      val copybook =
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

      val segmentRedefines = "SEGMENT-A" :: "SEGMENT-C" :: "SEGMENT-B" :: Nil

      "a correct mapping is specified, should no throw" in {
        val fieldParentMapOk = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "SEGMENT-A")

        CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapOk)
      }

      "a one of the mapped fields is not a segment redefine, should throw an exception" in {
        val fieldParentMapSegmentRedefine = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "SEGMENT-A", "SEGMENT-A" -> "SEGMENT-D")

        val ex = intercept[IllegalStateException] {
          CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapSegmentRedefine)
        }
        assert(ex.getMessage.contains("Field SEGMENT_D is specified to be the parent of SEGMENT_A, but SEGMENT_D is not a segment redefine."))
      }

      "there are too root segments, should throw an exception" in {
        val fieldParentMapTwoRoots = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A")

        val ex = intercept[IllegalStateException] {
          CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapTwoRoots)
        }
        assert(ex.getMessage.contains("Only one root segment is allowed. Found root segments: [ SEGMENT_A, SEGMENT_B ]"))
      }

      "a non-group field is specified, should throw an exception" in {
        val fieldParentMapNonSegment = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "RECORD-1")

        val ex = intercept[IllegalStateException] {
          CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapNonSegment)
        }
        assert(ex.getMessage.contains("Field RECORD_1 is specified to be the parent of SEGMENT_B, but RECORD_1 is not a segment redefine"))
      }

      "a field is specified that is a parent of itself, should throw an exception" in {
        val fieldParentMapSelfParent = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-C", "SEGMENT-B" -> "SEGMENT-B")

        val ex = intercept[IllegalStateException] {
          CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapSelfParent)
        }
        assert(ex.getMessage.contains("A segment SEGMENT_C cannot be a parent of itself"))
      }

      "a parent-child relationship forms a cycle, should throw an exception" in {
        val fieldParentMapCycle = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-B", "SEGMENT-B" -> "SEGMENT-C")

        val ex = intercept[IllegalStateException] {
          CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapCycle)
        }
        assert(ex.getMessage.contains("Segments parent-child relation form a cycle: SEGMENT_C, SEGMENT_B, SEGMENT_C"))
      }

      "a field is specified that does not exist" in {
        val fieldParentMapNotExist = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "SEGMENT-Z")

        val ex = intercept[IllegalStateException] {
          CopybookParser.parseTree(copybook, dropGroupFillers = false, segmentRedefines, fieldParentMapNotExist)
        }
        assert(ex.getMessage.contains("Field SEGMENT_Z is specified to be the parent of SEGMENT_B, but SEGMENT_Z is not a segment redefine"))
      }
    }

  }

}
