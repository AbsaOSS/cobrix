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

package za.co.absa.cobrix.spark.cobol.utils

import org.scalatest.wordspec.AnyWordSpec

import java.io.ByteArrayInputStream
import scala.io.Source

class GpgUtilsSuite extends AnyWordSpec {

  import za.co.absa.cobrix.cobol.utils.UsingUtils.Implicits._

  "decryptStream" should {
    "decrypt an input stream" in {
      val gpgPrivateKey = ResourceUtils.readResourceAsString("/test/test_gpg_key.asc")

      val decryptedText = for {
        iss <- getClass.getResourceAsStream("/test/test_gpg_file.gpg")
        oss <- GpgUtils.decryptStream(iss, gpgPrivateKey, Array.empty[Char])
      } yield {
        Source.fromInputStream(oss).mkString
      }

      assert(decryptedText.trim == "This is a test")
    }

    "fail if the stream is not GPG-encrypted" in {
      val gpgPrivateKey = ResourceUtils.readResourceAsString("/test/test_gpg_key.asc")

      val ex = intercept[RuntimeException] {
        for {
          iss <- new ByteArrayInputStream(Array[Byte](0, 0, 0, 0))
          oss <- GpgUtils.decryptStream(iss, gpgPrivateKey, Array.empty[Char])
        } yield {
          Source.fromInputStream(oss).mkString
        }
      }

      assert(ex.getMessage.contains("The input stream does not contain PGP encrypted data."))
    }
  }

}
