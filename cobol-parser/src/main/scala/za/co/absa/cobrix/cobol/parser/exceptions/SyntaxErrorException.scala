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

class SyntaxErrorException(val lineNumber: Int, val posOpt: Option[Int], val fieldOpt: Option[String], val msg: String)
  extends Exception(SyntaxErrorException.constructErrorMessage(lineNumber, posOpt, fieldOpt, msg)) {
}

object SyntaxErrorException {
  private def constructErrorMessage(lineNumber: Int, pos: Option[Int], fieldOpt: Option[String], msg: String): String = {
    val atLine = if (lineNumber > 0) {
      pos match {
        case Some(p) => s" at line $lineNumber:$p"
        case None => s" at line $lineNumber"
      }
    }
    else
      ""

    val atField = fieldOpt match {
      case Some(f) => s", field $f"
      case None => ""
    }

    s"Syntax error in the copybook$atLine$atField: $msg"
  }
}
