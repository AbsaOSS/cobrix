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

class FakeCodePage extends CodePage {
  /**
   * A short name is used to distinguish between different code pages, so it must be unique
   */
  override def codePageShortName: String = "fake_code_page"

  /**
   * Each class inherited from CodePage should provide its own conversion table
   */
  override protected def ebcdicToAsciiMapping: Array[Char] = Array[Char]('s', 'd', 'w')
}
