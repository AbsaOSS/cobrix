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

class AstTransformerSpy extends AstTransformer {
  @volatile var transformCaller = 0

  override def transform(ast: CopybookAST): CopybookAST = {
    transformCaller += 1
    val fields = ast.children.head.asInstanceOf[Group].children
    val newChildren: ArrayBuffer[Statement] = fields.map {
      case p: Primitive => p.copy(name = p.name + "_transformed")(p.parent)
      case g: Group     => g.copy(name = g.name + "_transformed")(g.parent)
    }

    val originalGroup = ast.children.head.asInstanceOf[Group]
    val newGroup = originalGroup.copy(children = newChildren)(originalGroup.parent)
    ast.children.update(0, newGroup)
    ast
  }
}
