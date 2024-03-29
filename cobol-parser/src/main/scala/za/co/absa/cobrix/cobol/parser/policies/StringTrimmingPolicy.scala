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

package za.co.absa.cobrix.cobol.parser.policies

object StringTrimmingPolicy extends Enumeration {
  type StringTrimmingPolicy = Value

  val TrimNone, TrimLeft, TrimRight, TrimBoth, KeepAll = Value

  def withNameOpt(s: String): Option[Value] = {
    val exactNames = values.find(_.toString == s)
    if (exactNames.isEmpty) {
      val sLowerCase = s.toLowerCase()
      if (sLowerCase == "none") {
        Some(TrimNone)
      } else if (sLowerCase == "left") {
        Some(TrimLeft)
      } else if (sLowerCase == "right") {
        Some(TrimRight)
      } else if (sLowerCase == "both") {
        Some(TrimBoth)
      } else if (sLowerCase == "keep_all") {
        Some(KeepAll)
      } else {
        None
      }
    } else {
      exactNames
    }
  }

}
