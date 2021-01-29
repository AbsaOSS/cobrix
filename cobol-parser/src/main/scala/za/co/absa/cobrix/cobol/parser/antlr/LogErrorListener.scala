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

package za.co.absa.cobrix.cobol.parser.antlr

import org.antlr.v4.runtime.{BaseErrorListener, RecognitionException, Recognizer}
import org.slf4j.Logger

/**
  * An ANTLR error listener that will log errors using the provided logger instead of
  * printing them to stderr directly.
  *
  * @param logger A logger corresponding to the parser that might generate errors.
  */
class LogErrorListener(logger: Logger) extends BaseErrorListener {
  override def syntaxError(recognizer: Recognizer[_, _], offendingSymbol: Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException): Unit = {
    val errMsg = s"Syntax error in the copybook: Line $line:$charPositionInLine $msg"
    logger.error(errMsg)
  }
}
