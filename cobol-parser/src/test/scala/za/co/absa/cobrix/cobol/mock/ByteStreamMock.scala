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

package za.co.absa.cobrix.cobol.mock

import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

class ByteStreamMock(bytes: Array[Byte]) extends SimpleStream{

  var position = 0
  val sz: Int = bytes.length

  override def inputFileName: String = "dummy"

  override def size: Long = sz

  override def totalSize: Long = sz

  override def offset: Long = position

  override def next(numberOfBytes: Int): Array[Byte] = {
    if (position + numberOfBytes < sz) {
      val s = bytes.slice(position, position + numberOfBytes)
      position += numberOfBytes
      s
    } else {
      if (position < sz) {
        val s = bytes.slice(position, sz)
        position = sz
        s
      } else {
        new Array[Byte](0)
      }
    }
  }

  override def close(): Unit = position = sz
}
