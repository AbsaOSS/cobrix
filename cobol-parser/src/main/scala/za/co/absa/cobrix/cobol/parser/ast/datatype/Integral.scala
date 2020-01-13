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

package za.co.absa.cobrix.cobol.parser.ast.datatype

import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.position.Position

/** The Integer data type
  *
  * @param precision    A precision that is the number of digits in a number
  * @param signPosition A position of a sign in the numeric representation
  * @param wordAlligned An alignment type
  * @param compact      A type of binary number representation format
  * @param enc          An encoding
  */
case class Integral(
                    pic: String,
                    precision: Int,
                    signPosition: Option[Position] = None,
                    isSignSeparate: Boolean = false,
                    wordAlligned: Option[Position] = None,
                    compact: Option[Usage] = None,
                    enc: Option[Encoding] = None,
                    originalPic: Option[String] = None
                  )
  extends CobolType
