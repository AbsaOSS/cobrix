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

package za.co.absa.cobrix.cobol.parser.encoding.codepage

import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Failure, Success, Try}

class CodePageTwoBytesSpec extends AnyFunSuite {
  test("Ensure codepage 'cp00300' gives the associated CodePage") {
    val codePage = CodePage.getCodePageByName("cp00300")
    assert(codePage.codePageShortName == "cp00300")
  }

  test("Ensure 'cp00300' decodes strings as expected") {
    val bytes = Array[Byte](0x0E, 0x46, 0xAF.toByte, 0x46, 0x7A, 0x46, 0x7C)

    val codePage = CodePage.getCodePageByName("cp00300")

    assert(codePage.convert(bytes) =="不丸由")
  }

  test("Ensure 'cp00300' correctly decodes odd number of bytes") {
    val bytes = Array[Byte](0x0E, 0x46, 0x7A, 0x46, 0x7C, 0x14)

    val codePage = CodePage.getCodePageByName("cp00300")

    assert(codePage.convert(bytes) == "丸由")
  }

  test("Ensure 'cp00300' correctly decodes single bytes sequence") {
    val bytes = Array[Byte](0x62, 0x63, 0x64, 0x65)

    val codePage = CodePage.getCodePageByName("cp00300")

    assert(codePage.convert(bytes) == "abcd")
  }

  test("Ensure 'cp00300' correctly decodes single + multiple byte  sequence") {
    val bytes = Array[Byte](0x62, 0x63, 0x64, 0x65, 0x0E, 0x46, 0xAF.toByte, 0x46, 0x7C, 0x0F, 0x62, 0x63, 0x64, 0x65)

    val codePage = CodePage.getCodePageByName("cp00300")

    assert(codePage.convert(bytes) == "abcd不由abcd")
  }
}
