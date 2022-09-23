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

import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.Constants

import scala.collection.mutable.ArrayBuffer

/**
  * @param dropGroupFillers specifies if group FILLERs are going to be dropped so they don't need renaming
  * @param dropValueFillers specifies if value FILLERs are going to be dropped so they don't need renaming
  * @return The same AST with group fillers processed
  */
class GroupFillersRenamer(
                      dropGroupFillers: Boolean,
                      dropValueFillers: Boolean
                    ) extends AstTransformer {
  /**
    * Rename group fillers so filed names in the scheme doesn't repeat
    * Also, remove all group fillers that doesn't have child nodes
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with group fillers renamed
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    var lastFillerIndex = 0
    var lastFillerPrimitiveIndex = 0

    def processPrimitive(st: Primitive): Primitive = {
      if (dropValueFillers || !st.isFiller) {
        st
      } else {
        lastFillerPrimitiveIndex += 1
        val newName = s"${Constants.FILLER}_P$lastFillerPrimitiveIndex"
        st.copy(name = newName, isFiller = false)(st.parent)
      }
    }

    def renameSubGroupFillers(group: Group): Group = {
      val (newChildren, hasNonFillers) = renameFillers(group)
      val renamedGroup = if (hasNonFillers) {
        if (group.isFiller && !dropGroupFillers) {
          lastFillerIndex += 1
          group.copy(name = s"${
            Constants.FILLER
          }_$lastFillerIndex", children = newChildren.children, isFiller = false)(group.parent)
        } else {
          group.withUpdatedChildren(newChildren.children)
        }
      } else {
        // All the children are fillers
        group.copy(children = newChildren.children, isFiller = true)(group.parent)
      }
      renamedGroup
    }

    def renameFillers(group: Group): (Group, Boolean) = {
      val newChildren = ArrayBuffer[Statement]()
      var hasNonFillers = false
      group.children.foreach {
        case grp: Group =>
          val newGrp = renameSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
          if (!newGrp.isFiller) hasNonFillers = true
        case st: Primitive =>
          val newSt = processPrimitive(st)
          newChildren += newSt
          if (!newSt.isFiller) hasNonFillers = true
      }
      (group.withUpdatedChildren(newChildren), hasNonFillers)
    }

    val (newSchema, hasNonFillers) = renameFillers(ast)
    if (!hasNonFillers) {
      throw new IllegalStateException("The copybook is empty since it consists only of FILLER fields.")
    }
    newSchema
  }
}

object GroupFillersRenamer {
  def apply(dropGroupFillers: Boolean, dropValueFillers: Boolean): GroupFillersRenamer = new GroupFillersRenamer(dropGroupFillers, dropValueFillers)
}
