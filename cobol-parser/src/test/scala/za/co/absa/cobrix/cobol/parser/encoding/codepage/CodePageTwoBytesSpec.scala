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
    val bytes = Array[Byte](0x14, 0x10, 0x14, 0x11 ,0x14, 0x12)

    val codePage = CodePage.getCodePageByName("cp00300")

    assert(codePage.convert(bytes) =="抜抛抗")
  }

  test("Ensure 'cp00300' correctly decodes odd number of bytes") {
    val bytes = Array[Byte](0x14, 0x10, 0x14, 0x11, 0x14)

    val codePage = CodePage.getCodePageByName("cp00300")

    assert(codePage.convert(bytes) == "抜抛")
  }
}
