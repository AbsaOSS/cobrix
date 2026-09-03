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
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}

/**
  * An AST transformer that restores the parent references of every element of a copybook AST.
  *
  * The AST is traversed recursively from the root group down to the leaves, and each group and primitive
  * field is recreated so that its `parent` points to the newly created copy of its enclosing group. The root
  * of the AST is left without a parent.
  *
  * This transformation is required because AST elements are immutable case classes: whenever a copybook AST is
  * rebuilt or modified by other transformations, the parent links of the affected elements may point to stale
  * copies of their parents. Applying this transformer as the final step ensures that the parent links are
  * consistent with the actual hierarchy of the resulting AST.
  *
  * The structure, ordering and all other properties of the fields remain unchanged.
  */
class ParentGroupSetter extends AstTransformer {
  final override def transform(ast: CopybookAST): CopybookAST = {
    def processGroup(group: Group, parent: Option[Group]): Unit = {
      var i = 0
      group.parent = parent
      while (i < group.children.length) {
        group.children(i) match {
          case g: Group     => processGroup(g, Some(group))
          case p: Primitive => p.parent = Some(group)
        }
        i += 1
      }
    }

    processGroup(ast, None)
    ast
  }
}


object ParentGroupSetter {
  def apply(): ParentGroupSetter = new ParentGroupSetter()
}
