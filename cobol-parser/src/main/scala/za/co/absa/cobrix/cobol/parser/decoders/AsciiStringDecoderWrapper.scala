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

import java.nio.charset.{Charset, StandardCharsets}

import scala.collection.mutable.ArrayBuffer

/**
  * This class is an ASCII string decoder wrapper to workaround Charset not being serializable.
  * An instance of this class can be casted to (Array[Byte] => Any) - a function for decoding
  * an array of bytes into an arbitrary type. For this decoder the output type is always string.
  *
  * @param trimmingType     Specifies if and how the soutput string should be trimmed
  * @param asciiCharsetName A charset name of input strings
  * @return A string representation of the binary data
  */
class AsciiStringDecoderWrapper(trimmingType: Int, asciiCharsetName: String) extends Serializable with (Array[Byte] => Any) {
  import StringDecoders._

  lazy val charset: Charset = Charset.forName(asciiCharsetName)

  /**
    * A decoder for any ASCII string fields (alphabetical or any char)
    *
    * @param bytes        A byte array that represents the binary data
    * @return A string representation of the binary data
    */
  def apply(bytes: Array[Byte]): String = {
    var i = 0

    // Filter out all special characters
    val buf = new ArrayBuffer[Byte](bytes.length)
    while (i < bytes.length) {
      if (bytes(i) >= 0 && bytes(i) < 32 /* Special characters are masked */ )
        buf.append(32)
      else
        buf.append(bytes(i))
      i = i + 1
    }

    val str = new String(buf.toArray, charset)

    if (trimmingType == TrimNone) {
      str
    } else if (trimmingType == TrimLeft) {
      StringTools.trimLeft(str)
    } else if (trimmingType == TrimRight) {
      StringTools.trimRight(str)
    } else {
      str.trim
    }
  }
}


