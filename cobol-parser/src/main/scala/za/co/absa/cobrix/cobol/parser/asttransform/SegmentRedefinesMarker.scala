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

import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * @param segmentRedefines The list of fields names that correspond to segment GROUPs.
  */
class SegmentRedefinesMarker(segmentRedefines: Seq[String]) extends AstTransformer {
  /**
    * Sets isSegmentRedefine property of redefined groups so the row extractor be able to skip parsing segment groups
    * that do not belong to a particular segment id.
    *
    * * Each field should appear in the list only once
    * * Any such field should be a redefine or a redefined by.
    * * All segment fields should belong to the same redefine group. E.g. they should redefine each other,
    * * All segment fields should belong to the level 1 (one level down record root level)
    * * A segment redefine cannot be inside an array
    *
    * @param ast              An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    val foundRedefines = new mutable.HashSet[String]
    val transformedSegmentRedefines = segmentRedefines.map(CopybookParser.transformIdentifier)
    val allowNonRedefines = segmentRedefines.lengthCompare(1) == 0
    var redefineGroupState = 0

    def ensureSegmentRedefinesAreIneGroup(currentField: String, isCurrentFieldASegmentRedefine: Boolean): Unit = {
      if (redefineGroupState == 0 && isCurrentFieldASegmentRedefine) {
        redefineGroupState = 1
      } else if (redefineGroupState == 1 && !isCurrentFieldASegmentRedefine) {
        redefineGroupState = 2
      } else if (redefineGroupState == 2 && isCurrentFieldASegmentRedefine) {
        throw new IllegalStateException(s"The '$currentField' field is specified to be a segment redefine. " +
          "However, it is not in the same group of REDEFINE fields")
      }
    }

    def isOneOfSegmentRedefines(g: Group): Boolean = {
      (allowNonRedefines || g.isRedefined || g.redefines.nonEmpty) &&
        transformedSegmentRedefines.contains(g.name)
    }

    def processGroupFields(group: Group): Group = {
      val childrenWithSegmentRedefines: ArrayBuffer[Statement] = group.children.map {
        case p: Primitive =>
          ensureSegmentRedefinesAreIneGroup(p.name, isCurrentFieldASegmentRedefine = false)
          p
        case g: Group =>
          if (isOneOfSegmentRedefines(g)) {
            if (foundRedefines.contains(g.name)) {
              throw new IllegalStateException(s"Duplicate segment redefine field '${g.name}' found.")
            }

            if (redefineGroupState == 1 && g.redefines.isEmpty)
              throw new IllegalStateException(s"The segment redefine field '${g.name}' is not a REDEFINE or redefined by another field.")

            ensureSegmentRedefinesAreIneGroup(g.name, isCurrentFieldASegmentRedefine = true)
            foundRedefines += g.name
            g.withUpdatedIsSegmentRedefine(true)
          } else {
            // Allow redefines in between segment redefines.
            val fieldMightBeRedefine = if (redefineGroupState == 1 && g.redefines.nonEmpty)
              true
            else
              false
            ensureSegmentRedefinesAreIneGroup(g.name, isCurrentFieldASegmentRedefine = fieldMightBeRedefine)
            // Check nested fields recursively only if segment redefines hasn't been found so far.
            if (redefineGroupState == 0) {
              processGroupFields(g)
            } else {
              g
            }
          }
      }
      group.copy(children = childrenWithSegmentRedefines)(group.parent)
    }

    def processRootLevelFields(group: CopybookAST): CopybookAST = {
      group.withUpdatedChildren(
        group.children.map {
          case p: Primitive =>
            p
          case g: Group =>
            processGroupFields(g)
        }
      )
    }

    def validateAllSegmentsFound(): Unit = {
      val notFound = transformedSegmentRedefines.filterNot(foundRedefines)
      if (notFound.nonEmpty) {
        val notFoundList = notFound.mkString(",")
        throw new IllegalStateException(s"The following segment redefines not found: [ $notFoundList ]. " +
          "Please check the fields exist and are redefines/redefined by.")
      }
    }

    if (segmentRedefines.isEmpty) {
      ast
    } else {
      val isFlatAst = ast.children.exists(_.isInstanceOf[Primitive])
      val newSchema = if (isFlatAst) {
        processGroupFields(ast)
      } else {
        processRootLevelFields(ast)
      }
      validateAllSegmentsFound()
      ast.withUpdatedChildren(newSchema.children)
    }
  }
}

object SegmentRedefinesMarker {
  def apply(segmentRedefines: Seq[String]): SegmentRedefinesMarker = new SegmentRedefinesMarker(segmentRedefines)
}
