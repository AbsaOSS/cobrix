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

package za.co.absa.cobrix.cobol.parser.common

/**
  * The object contains literals for Cobol reserved words
  */
object ReservedWords {
  val COMP = "COMP"
  val COMPUTATIONAL = "COMPUTATIONAL"
  val BINARY = "BINARY"
  val COMP123 = "COMP-"
  val COMPUTATIONAL123 = "COMPUTATIONAL-"
  val DEPENDING = "DEPENDING"
  val FILLER = "FILLER"
  val OCCURS = "OCCURS"
  val ON = "ON"
  val PIC = "PIC"
  val REDEFINES = "REDEFINES"
  val SYNC = "SYNC"
  val TO = "TO"

  // SIGN [IS] {LEADING|TRAILING} [SEPARATE] [CHARACTER]
  val SIGN = "SIGN"
  val SIGN_SEP = "SIGN_SEP"
  val IS = "IS"
  val LEADING = "LEADING"
  val TRAILING = "TRAILING"
  val SEPARATE = "SEPARATE"
  val CHARACTER = "CHARACTER"
}
