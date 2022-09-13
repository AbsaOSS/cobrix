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

/**
  * An interface for AST transformation. This allows decoupling and chaining of AST transformations based on the
  * requirements provided by the client of the parser.
  */
trait AstTransformer {
  /**
    * Transforms an AST in the way defined by the implementation.
    *
    * @param ast An input AST
    * @return The transformer AST
    */
  def transform(ast: CopybookAST): CopybookAST
}
