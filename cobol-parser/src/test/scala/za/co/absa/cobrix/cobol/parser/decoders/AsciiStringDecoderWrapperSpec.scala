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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets

import org.scalatest.WordSpec

class AsciiStringDecoderWrapperSpec extends WordSpec {
  import StringDecoders._

  "AsciiStringDecoderWrapper" should {

    "be able to decode strings" in {
      val decoder = new AsciiStringDecoderWrapper(TrimNone, "UTF8")

      assert(decoder(" Hello ".getBytes(StandardCharsets.UTF_8)) == " Hello ")
    }

    "be able to decode UTF-8 strings" in {
      val str = "ěščřžýáíé"
      val decoder = new AsciiStringDecoderWrapper(TrimNone, "UTF8")

      assert(decoder(str.getBytes(StandardCharsets.UTF_8)) == str)
    }

    "be able to decode strings with special characters" in {
      val str = "\u0001\u0005A\u0008\u0010B\u0015\u001F"
      val decoder = new AsciiStringDecoderWrapper(TrimNone, "ASCII")

      assert(decoder(str.getBytes(StandardCharsets.UTF_8)) == "  A  B  ")
    }

    "support left trimming" in {
      val decoder = new AsciiStringDecoderWrapper(TrimLeft, "UTF8")

      assert(decoder(" Hello ".getBytes(StandardCharsets.UTF_8)) == "Hello ")
    }

    "support right trimming" in {
      val decoder = new AsciiStringDecoderWrapper(TrimRight, "UTF8")

      assert(decoder(" Hello ".getBytes(StandardCharsets.UTF_8)) == " Hello")
    }

    "support trimming on both sides" in {
      val decoder = new AsciiStringDecoderWrapper(TrimBoth, "UTF8")

      assert(decoder(" Hello ".getBytes(StandardCharsets.UTF_8)) == "Hello")
    }

    "be able to decode strings with trimming and special characters" in {
      val str = "\u0002\u0004A\u0007\u000FB\u0014\u001E"
      val decoder = new AsciiStringDecoderWrapper(TrimBoth, "ASCII")

      assert(decoder(str.getBytes(StandardCharsets.UTF_8)) == "A  B")
    }

    "be serializable and deserializable" in {
      val decoder = new AsciiStringDecoderWrapper(TrimBoth, "UTF8")

      val bos = new ByteArrayOutputStream
      val out = new ObjectOutputStream(bos)
      out.writeObject(decoder)
      out.flush()
      val serialized = bos.toByteArray

      assert(serialized.nonEmpty)

      val ois = new ObjectInputStream(new ByteArrayInputStream(serialized))
      val decoder2 = ois.readObject().asInstanceOf[AsciiStringDecoderWrapper]

      assert(decoder2(" Hello ".getBytes(StandardCharsets.UTF_8)) == "Hello")
    }
  }
}
