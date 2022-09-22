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

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.Integral
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * @param dropGroupFillers specifies if we actually neeed to drop group FILLERs
  * @param dropValueFillers is there intention to drop primitive fields fillers
  * @return The same AST with group fillers processed
  */
class GroupFillersRemover(
                      dropGroupFillers: Boolean,
                      dropValueFillers: Boolean
                    ) extends AstTransformer {
  private val log = LoggerFactory.getLogger(this.getClass)

  /**
    * Process group fillers.
    * <ul>
    * <li>Make fillers each group that contains only filler fields.</li>
    * <li>Remove all groups that don't have child nodes.</li>
    * </ul>
    *
    * @param ast              An AST as a set of copybook records
    * @return The same AST with group fillers processed
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    def processSubGroupFillers(group: Group): Group = {
      val (newChildren, hasNonFillers) = processFillers(group)
      if (hasNonFillers)
        group.copy(children = newChildren.children)(group.parent)
      else
        group.copy(children = newChildren.children, isFiller = true)(group.parent)
    }

    def processFillers(group: CopybookAST): (CopybookAST, Boolean) = {
      val newChildren = ArrayBuffer[Statement]()
      var hasNonFillers = false
      group.children.foreach {
        case grp: Group =>
          val newGrp = processSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
          if (!grp.isFiller) hasNonFillers = true
        case st: Primitive =>
          newChildren += st
          if (!st.isFiller || !dropValueFillers) hasNonFillers = true
      }
      (group.withUpdatedChildren(newChildren), hasNonFillers)
    }

    if (dropGroupFillers) {
      val (newSchema, hasNonFillers) = processFillers(ast)
      if (!hasNonFillers) {
        throw new IllegalStateException("The copybook is empty of consists only of FILLER fields.")
      }
      newSchema
    } else {
      ast
    }
  }
}

object GroupFillersRemover {
  def apply(dropGroupFillers: Boolean, dropValueFillers: Boolean): GroupFillersRemover = new GroupFillersRemover(dropGroupFillers, dropValueFillers)
}
