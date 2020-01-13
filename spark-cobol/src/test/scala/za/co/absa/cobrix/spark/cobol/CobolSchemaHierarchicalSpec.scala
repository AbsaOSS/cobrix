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

package za.co.absa.cobrix.spark.cobol

import org.scalatest.WordSpec
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.schema.{CobolSchema, SchemaRetentionPolicy}

import scala.collection.immutable.HashMap

class CobolSchemaHierarchicalSpec extends WordSpec {

  "createSparkSchema" should {
    "create a hierarchical schema if a simple parent-child relation is specified" in {
      val copybook =
        """      01 RECORD.
          |        02 SEGMENT-A.
          |           03 FIELD1 PIC X(2).
          |        02 SEGMENT-B REDEFINES SEGMENT-A.
          |           03 FIELD2 PIC X(2).
          |        02 Z-RECORD.
          |           03 FIELD3 PIC X(2).
        """.stripMargin
      val expectedSchema =
        """root
          | |-- SEGMENT_A: struct (nullable = true)
          | |    |-- FIELD1: string (nullable = true)
          | |    |-- SEGMENT_B: array (nullable = true)
          | |    |    |-- element: struct (containsNull = true)
          | |    |    |    |-- FIELD2: string (nullable = true)
          | |-- Z_RECORD: struct (nullable = true)
          | |    |-- FIELD3: string (nullable = true)
          |""".stripMargin.replace("\r\n", "\n")

      val segmentRedefines = "SEGMENT-A" :: "SEGMENT-B" :: Nil
      val fieldParentMap = HashMap[String, String]("SEGMENT-B" -> "SEGMENT-A")

      val cobolSchema = parseSchema(copybook, segmentRedefines, fieldParentMap)

      assert(cobolSchema.getSparkSchema.treeString == expectedSchema)
    }
  }

  "create a hierarchical schema for a complex parent-child relation" in {
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

    val segmentRedefines = "SEGMENT-A" :: "SEGMENT-B" :: "SEGMENT-C" :: "SEGMENT-D" :: Nil
    val fieldParentMap = HashMap[String, String]("SEGMENT-C" -> "SEGMENT-A", "SEGMENT-B" -> "SEGMENT-A", "SEGMENT-D" -> "SEGMENT-C")

    val expectedSchema =
      """root
        | |-- RECORD_1: struct (nullable = true)
        | |    |-- FIELD_1: string (nullable = true)
        | |-- SEGMENT_A: struct (nullable = true)
        | |    |-- FIELD_2: string (nullable = true)
        | |    |-- SEGMENT_B: array (nullable = true)
        | |    |    |-- element: struct (containsNull = true)
        | |    |    |    |-- FIELD_3: integer (nullable = true)
        | |    |-- SEGMENT_C: array (nullable = true)
        | |    |    |-- element: struct (containsNull = true)
        | |    |    |    |-- FIELD_4: integer (nullable = true)
        | |    |    |    |-- SEGMENT_D: array (nullable = true)
        | |    |    |    |    |-- element: struct (containsNull = true)
        | |    |    |    |    |    |-- FIELD_5: integer (nullable = true)
        | |-- Z_RECORD: struct (nullable = true)
        | |    |-- FIELD_6: string (nullable = true)
        |""".stripMargin.replace("\r\n", "\n")

    val cobolSchema = parseSchema(copybook, segmentRedefines, fieldParentMap)

    assert(cobolSchema.getSparkSchema.treeString == expectedSchema)
  }

  private def parseSchema(copybook: String, segmentRedefines: List[String], fieldParentMap: Map[String, String]): CobolSchema = {
    val parsedSchema = CopybookParser.parseTree(copybook, segmentRedefines = segmentRedefines, fieldParentMap = fieldParentMap)
    new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, "",false)
  }
}
