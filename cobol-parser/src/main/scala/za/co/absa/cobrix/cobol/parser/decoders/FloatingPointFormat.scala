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

package za.co.absa.cobrix.cobol.parser.decoders

object FloatingPointFormat extends Enumeration {
  type FloatingPointFormat = Value

  val IBM, IBM_LE, IEEE754, IEEE754_LE = Value

  def withNameOpt(s: String): Option[Value] = {
    val exactNames = values.find(_.toString == s)
    if (exactNames.isEmpty) {
      val sLowerCase = s.toLowerCase()
      if (sLowerCase == "ibm") {
        Some(IBM)
      } else if (sLowerCase == "ibm_little_endian") {
        Some(IBM_LE)
      } else if (sLowerCase == "ieee754") {
        Some(IEEE754)
      } else if (sLowerCase == "ieee754_little_endian") {
        Some(IEEE754_LE)
      } else {
        None
      }
    } else {
      exactNames
    }
  }

}
