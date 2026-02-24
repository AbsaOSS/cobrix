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

package za.co.absa.cobrix.cobol.utils

object StringUtils {
  // Characters used for HEX conversion
  private final val HEX_ARRAY = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

  /**
    * Converts an array of bytes into a hexadecimal string representation.
    *
    * The main goal is the high CPU and memory efficiency of this method.
    *
    * @param a the input array of bytes to be converted. If the input is null, the method returns null.
    * @return a string representing the hexadecimal equivalent of the input byte array, or null if the input is null.
    */
  def convertArrayToHex(a: Array[Byte]): String = {
    if (a == null) return null
    val hexArray = HEX_ARRAY
    val hexChars = new Array[Char](a.length * 2)
    var i = 0
    while (i < a.length) {
      val v = a(i) & 0xFF
      hexChars(i * 2) = hexArray(v >>> 4)
      hexChars(i * 2 + 1) = hexArray(v & 0x0F)
      i += 1
    }
    new String(hexChars)
  }
}
