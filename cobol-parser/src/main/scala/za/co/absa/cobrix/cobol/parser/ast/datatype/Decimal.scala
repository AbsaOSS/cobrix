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

/** The Decimal data type
  *
  * @param scale        A scale that is the number of fracture decimal digits in a number
  * @param precision    A precision that is the number of digits in a number
  * @param scaleFactor  A number of digits to shift the decimal points to
  * @param signPosition A position of a sign in the numeric representation
  * @param wordAlligned An alignment type
  * @param compact      A type of binary number representation format
  * @param enc          An encoding
  */
case class Decimal(
                    pic: String,
                    scale: Int,
                    precision: Int,
                    scaleFactor: Int,
                    explicitDecimal: Boolean = false,
                    signPosition: Option[Position] = None,
                    isSignSeparate: Boolean = false,
                    wordAlligned: Option[Position] = None,
                    compact: Option[Usage] = None,
                    enc: Option[Encoding] = None,
                    originalPic: Option[String] = None
                  )
  extends CobolType {

  /** Returns the precision of the actual number after scale factor is applied */
  def getEffectivePrecision: Int = precision + Math.abs(scaleFactor)

  /** Returns the scale of the actual number after scale factor is applied */
  def getEffectiveScale: Int = {
    if (scaleFactor > 0)
      // If scale factor is positive a 10^scaleFactor multiplier is used so the data type is effectively a big integer
      0
    else if (scaleFactor < 0)
      // If scale factor is negative the number looks like 0.aaaBBB where 'aaa' zeros according to the scale and 'BBB' are digits of an encoded number.
      getEffectivePrecision
    else
      // If scale factor is not specified, numbers are interpreted normally
      scale
  }

}
