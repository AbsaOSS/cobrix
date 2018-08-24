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

package za.co.absa.cobrix.cobol.parser.exceptions

class SyntaxErrorException(val lineNumber: Int, val field: String, val msg: String)
  extends Exception(SyntaxErrorException.constructErrorMessage(lineNumber, field, msg)) {
}

object SyntaxErrorException {
  private def constructErrorMessage(lineNumber: Int, field: String, msg: String): String = {
    val atLine = if (lineNumber > 0) s" at line $lineNumber"
    val atField = if (field.nonEmpty) s", field $field" else ""

    s"Syntax error in the copybook$atLine$atField: $msg"
  }
}