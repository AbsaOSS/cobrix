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

package za.co.absa.cobrix.cobol.parser.asttransform

import za.co.absa.cobrix.cobol.parser.CopybookParser.{CopybookAST, getAllSegmentRedefines}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * @param fieldParentMap A mapping between field names and their parents
  */
class SegmentParentsSetter(fieldParentMap: Map[String, String]) extends AstTransformer {
  /**
    * Sets parent groups for child segment redefines.
    * This relies on segment id to redefines map. The assumptions are
    *
    * * Only one segment redefine field has empty parent - the root segment.
    * * All other segment redefines should have a parent segment.
    * * isSegmentRedefine should be already set for all segment redefines.
    * * A parent of a segment redefine should be a segment redefine as well
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with segment parents set
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    val rootSegments = ListBuffer[String]()
    val redefinedFields = getAllSegmentRedefines(ast)

    def getParentField(childName: String): Option[Group] = {
      fieldParentMap
        .get(childName)
        .map(field => {
          val parentOpt = redefinedFields.find(f => f.name == field)
          parentOpt match {
            case Some(group) => group
            case _ => throw new IllegalStateException(s"Field $field is specified to be the parent of $childName, " +
              s"but $field is not a segment redefine. Please, check if the field is specified for any of 'redefine-segment-id-map' options.")
          }
        })
    }

    def processGroupFields(group: Group): Group = {
      val childrenWithSegmentRedefines: ArrayBuffer[Statement] = group.children.map {
        case p: Primitive =>
          p
        case g: Group =>
          if (g.isSegmentRedefine) {
            val newGroup = g.withUpdatedParentSegment(getParentField(g.name))
            if (newGroup.parentSegment.isEmpty) {
              rootSegments += g.name
            }
            newGroup
          } else {
            if (fieldParentMap.contains(g.name)) {
              throw new IllegalStateException("Parent field is defined for a field that is not a segment redefine. " +
                s"Field: '${g.name}'. Please, check if the field is specified for any of 'redefine-segment-id-map' options.")
            }
            processGroupFields(g)
          }
      }
      group.copy(children = childrenWithSegmentRedefines)(group.parent)
    }

    def validateRootSegments(): Unit = {
      if (rootSegments.size > 1) {
        val rootSegmentsStr = rootSegments.mkString(", ")
        throw new IllegalStateException(s"Only one root segment is allowed. Found root segments: [ $rootSegmentsStr ]. ")
      }
      if (rootSegments.isEmpty) {
        throw new IllegalStateException(s"No root segment found in the segment parent-child map.")
      }
    }

    if (fieldParentMap.isEmpty) {
      ast
    } else {
      val newSchema = processGroupFields(ast)
      validateRootSegments()
      newSchema
    }
  }
}

object SegmentParentsSetter {
  def apply(fieldParentMap: Map[String, String]): SegmentParentsSetter = new SegmentParentsSetter(fieldParentMap)
}
