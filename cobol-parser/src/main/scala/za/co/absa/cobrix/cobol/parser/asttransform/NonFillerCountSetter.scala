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

import scala.collection.mutable.ArrayBuffer

class NonFillerCountSetter extends AstTransformer {

  /**
    * For each group calculates the number of non-filler items.
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with non-filler count set for each group
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    def calcGroupNonFillers(group: Group): Group = {
      val newChildren = calcNonFillerChildren(group)
      var i = 0
      var nonFillers = 0
      while (i < group.children.length) {
        if (!group.children(i).isFiller && !group.children(i).isChildSegment)
          nonFillers += 1
        i += 1
      }
      group.copy(nonFillerSize = nonFillers, children = newChildren.children)(group.parent)
    }

    def calcNonFillerChildren(group: CopybookAST): CopybookAST = {
      val newChildren = ArrayBuffer[Statement]()
      group.children.foreach {
        case grp: Group =>
          val newGrp = calcGroupNonFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
        case st: Primitive => newChildren += st
      }
      group.withUpdatedChildren(newChildren)
    }

    calcGroupNonFillers(ast)
  }
}

object NonFillerCountSetter {
  def apply(): NonFillerCountSetter = new NonFillerCountSetter
}
