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

package za.co.absa.cobrix.cobol.parser.validators

import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException

object CobolValidators {

  def validatePic(lineNumber: Int, fieldName: String, pic: String): Unit = {
    val displayPic = pic.replaceAll("\\,", ".")


  }

  def validateDataType(lineNumber: Int, fieldName: String, dt: CobolType): Unit = {
    dt match {
      case a: AlphaNumeric => // no vaidation needed for a string
      case i: Integral => validateIntegralType(lineNumber, fieldName, i)
      case d: Decimal => validateDecimalType(lineNumber, fieldName, d)
    }
  }

  def validateIntegralType(lineNumber: Int, fieldName: String, dt: Integral): Unit = {
    if (dt.isSignSeparate && dt.compact.isDefined) {
      throw new SyntaxErrorException(lineNumber, fieldName, s"SIGN SEPARATE clause is not supported for COMP-${dt.compact.get}. It is only supported for DISPLAY formatted fields.")
    }
    for (bin <- dt.compact) {
      if (dt.precision > Constants.maxBinIntPrecision) {
        throw new SyntaxErrorException(lineNumber, fieldName,
          s"BINARY-encoded integers with precision bigger than ${Constants.maxBinIntPrecision} are not supported.")
      }
    }
  }

  def validateDecimalType(lineNumber: Int, fieldName: String, dt: Decimal): Unit = {
    val displayPic = dt.pic.replaceAll("\\,", ".")
    if (dt.explicitDecimal && dt.compact.isDefined) {
      throw new SyntaxErrorException(lineNumber, fieldName,
        s"Explicit decimal point in 'PIC $displayPic' is not supported for COMP-${dt.compact.get}. It is only supported for DISPLAY formatted fields.")
    }
    if (dt.isSignSeparate && dt.compact.isDefined) {
      throw new SyntaxErrorException(lineNumber, fieldName, s"SIGN SEPARATE clause is not supported for COMP-${dt.compact.get}. It is only supported for DISPLAY formatted fields.")
    }
    if (dt.precision - dt.scale > Constants.maxDecimalPrecision) {
      throw new SyntaxErrorException(lineNumber, fieldName,
        s"Decimal numbers with precision bigger than ${Constants.maxDecimalPrecision} are not supported.")
    }
    if (dt.scale > Constants.maxDecimalScale) {
      throw new SyntaxErrorException(lineNumber, fieldName,
        s"Decimal numbers with scale bigger than ${Constants.maxDecimalScale} are not supported.")
    }
  }

}
