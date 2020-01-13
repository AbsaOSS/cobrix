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

object StringTools {

  /**
    * Trims a string from the left side
    * (The implementation is based on java.lang.String.trim())
    *
    * @param s A string
    * @return The trimmed string
    */
  def trimLeft(s: String): String = {
    val len = s.length
    var st = 0
    val v = s.toCharArray

    while ( {
      (st < len) && (v(st) <= ' ')
    }) st += 1

    if ((st > 0) || (len < s.length))
      s.substring(st, len)
    else s
  }

  /**
    * Trims a string from the left side
    * (The implementation is based on java.lang.String.trim())
    *
    * @param s A string
    * @return The trimmed string
    */
  def trimRight(s: String): String = {
    var len = s.length
    val st = 0
    val v = s.toCharArray

    while ( {
      (st < len) && (v(len - 1) <= ' ')
    }) len -= 1

    if ((st > 0) || (len < s.length))
      s.substring(st, len)
    else s
  }
}
