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

import za.co.absa.cobrix.cobol.parser.common.BinaryUtils

object StringDecoders {

  /** A decoder for any EBCDIC string fields (alphabetical or any char)
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeEbcdicString(bytes: Array[Byte]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(BinaryUtils.ebcdic2ascii((bytes(i) + 256) % 256))
      i = i + 1
    }
    buf.toString.trim
  }

  /** A decoder for any ASCII string fields (alphabetical or any char)
    *
    * @param bytes A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def decodeAsciiString(bytes: Array[Byte]): String = {
    var i = 0
    val buf = new StringBuffer(bytes.length)
    while (i < bytes.length) {
      buf.append(bytes(i).toChar)
      i = i + 1
    }
    buf.toString.trim
  }

}
