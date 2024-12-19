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
    * A short name is used to distinguish between different code pages, so it must be unique.
    */
  def codePageShortName: String

  /**
    * Converts an array of bytes to string according to the rules of the code page.
    */
  def convert(bytes: Array[Byte]): String
}

object CodePage extends Logging {

  /**
    * Code page names from: https://www.ibm.com/docs/en/zos-connect/zosconnect/3.0?topic=properties-coded-character-set-identifiers
    */
  def getCodePageByName(codePageName: String): CodePage = {
    codePageName match {
      case "common"          => new CodePageCommon
      case "common_extended" => new CodePageCommonExt
      case "cp037"           => new CodePage037
      case "cp037_extended"  => new CodePage037Ext
      case "cp00300"         => new CodePage300 // This is the same as cp300
      case "cp273"           => new CodePage273
      case "cp275"           => new CodePage275
      case "cp277"           => new CodePage277
      case "cp278"           => new CodePage278
      case "cp280"           => new CodePage280
      case "cp284"           => new CodePage284
      case "cp285"           => new CodePage285
      case "cp297"           => new CodePage297
      case "cp300"           => new CodePage300
      case "cp500"           => new CodePage500
      case "cp838"           => new CodePage838
      case "cp870"           => new CodePage870
      case "cp875"           => new CodePage875
      case "cp1025"          => new CodePage1025
      case "cp1047"          => new CodePage1047
      case "cp1140"          => new CodePage1140
      case "cp1141"          => new CodePage1141
      case "cp1145"          => new CodePage1145
      case "cp1146"          => new CodePage1146
      case "cp1148"          => new CodePage1148
      case "cp1364"          => new CodePage1364
      case "cp1388"          => new CodePage1388
      case codePage          => throw new IllegalArgumentException(s"The code page '$codePage' is not one of the builtin EBCDIC code pages.")
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
