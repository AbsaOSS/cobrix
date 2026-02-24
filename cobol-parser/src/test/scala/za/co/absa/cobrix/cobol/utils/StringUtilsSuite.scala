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

import org.scalatest.wordspec.AnyWordSpec

class StringUtilsSuite extends AnyWordSpec {
  "StringUtils" should {
    "convert byte array to hex string correctly" in {
      val byteArray = Array[Byte](0x00, 0x01, 0x02, 0x0A, 0x0F, 0x10, 0x1F, 0x7F, 0x80.toByte, 0xFF.toByte)
      val expectedHexString = "0001020A0F101F7F80FF"
      assert(StringUtils.convertArrayToHex(byteArray) == expectedHexString)
    }

    "return empty string when input is an empty array" in {
      assert(StringUtils.convertArrayToHex(Array.empty) == "")
    }

    "return null when input is null" in {
      assert(StringUtils.convertArrayToHex(null) == null)
    }
  }

}
