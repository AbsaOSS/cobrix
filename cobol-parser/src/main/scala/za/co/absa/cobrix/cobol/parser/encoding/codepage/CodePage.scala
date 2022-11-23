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

import za.co.absa.cobrix.cobol.internal.Logging

/**
  * A trait for generalizing EBCDIC to ASCII conversion tables for different EBCDIC code pages.
  */
abstract class CodePage extends Serializable {
  /**
    * A short name is used to distinguish between different code pages, so it must be unique
    */
  def codePageShortName: String

  /**
    * Each class inherited from CodePage should provide its own conversion table
    */
  protected def ebcdicToAsciiMapping: Array[Char]

  /**
    * Gets a mapping table for EBCDIC to ASCII conversions. Uses underlying protected abstract method to get
    * the actual table. Checks that the size of the mapping arrays is exactly 256 elements.
    *
    * An EBCDIC to ASCII conversion table is represented as an array of characters.
    * For each EBCDIC character encoded as an index of the array there is a UNICODE symbol represented as `Char`.
    *
    * @return An EBCDIC to ASCII conversion table as an array of chars
    */
  @throws(classOf[IllegalArgumentException])
  final def getEbcdicToAsciiMapping: Array[Char] = {
    val ConversionTableElements = 256
    val table = ebcdicToAsciiMapping
    if (table.length != ConversionTableElements) {
      throw new IllegalArgumentException(
        s"An EBCDIC to ASCII conversion table should have exactly $ConversionTableElements elements. It has ${table.length} elements.")
    }
    table
  }
}

object CodePage extends Logging{

  def getCodePageByName(codePageName: String): CodePage = {
    codePageName match {
      case "common"          => new CodePageCommon
      case "common_extended" => new CodePageCommonExt
      case "cp037"           => new CodePage037
      case "cp037_extended"  => new CodePage037Ext
      case "cp875"           => new CodePage875
      case "cp1047"          => new CodePage1047
      case codePage => throw new IllegalArgumentException(s"The code page '$codePage' is not one of the builtin EBCDIC code pages.")
    }
  }

  def getCodePageByClass(codePageClass: String): CodePage = {
    logger.info(s"Instantiating code page class: $codePageClass")
    Class.forName(codePageClass,
                  true,
                  Thread.currentThread().getContextClassLoader)
      .newInstance()
      .asInstanceOf[CodePage]
  }


}
