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

package za.co.absa.cobrix.spark.cobol.mocks

import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.AlphaNumeric
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.asttransform.AstTransformer
import za.co.absa.cobrix.cobol.parser.decoders.DecoderSelector
import za.co.absa.cobrix.cobol.parser.decoders.DecoderSelector.getStringDecoder
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC

import scala.collection.mutable.ArrayBuffer

class AstTransformerSpy extends AstTransformer {
  import AstTransformerSpy._

  override def transform(ast: CopybookAST): CopybookAST = {
    transformCaller += 1
    val root = ast.children.head.asInstanceOf[Group]
    val fields = root.children
    val newChildren: ArrayBuffer[Statement] = fields.map {
      case p: Primitive => p.copy(name = p.name + "_transformed")(p.parent)
      case g: Group     => g.copy(name = g.name + "_transformed")(g.parent)
    }

    val dataType = AlphaNumeric("X(1)", 1, None, None, None, None)
    val stringDecoder = DecoderSelector.getDecoder(dataType)
    val extraField = Primitive(3, "EXTRA", "EXTRA", 0, dataType, decode = stringDecoder, encode = None)(Some(root))
    newChildren.append(extraField)

    val newGroup = root.copy(children = newChildren)(root.parent)
    ast.children.update(0, newGroup)
    ast
  }
}

object AstTransformerSpy {
  @volatile var transformCaller = 0
}