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

import org.slf4j.{Logger, LoggerFactory}

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
    * Each class inherited from CodePage should provide its own conversion table
    */
  protected def asciiToAsciiMapping: Array[Byte] = {
    Array[Byte](
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, //   0 -  15
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, //  16 -  31
      0x40.toByte, 0x4F.toByte, 0x7F.toByte, 0x7B.toByte, 0x5B.toByte, 0x6C.toByte, 0x50.toByte, 0x7D.toByte, 0x4D.toByte, 0x5D.toByte, 0x5C.toByte, 0x4E.toByte, 0x6B.toByte, 0x60.toByte, 0x4B.toByte, 0x61.toByte, //  32 -  47
      0xF0.toByte, 0xF1.toByte, 0xF2.toByte, 0xF3.toByte, 0xF4.toByte, 0xF5.toByte, 0xF6.toByte, 0xF7.toByte, 0xF8.toByte, 0xF9.toByte, 0x7A.toByte, 0x5E.toByte, 0x4C.toByte, 0x7E.toByte, 0x6E.toByte, 0x6F.toByte, //  48 -  63
      0x7C.toByte, 0xC1.toByte, 0xC2.toByte, 0xC3.toByte, 0xC4.toByte, 0xC5.toByte, 0xC6.toByte, 0xC7.toByte, 0xC8.toByte, 0xC9.toByte, 0xD1.toByte, 0xD2.toByte, 0xD3.toByte, 0xD4.toByte, 0xD5.toByte, 0xD6.toByte, //  64 -  79
      0xD7.toByte, 0xD8.toByte, 0xD9.toByte, 0xE2.toByte, 0xE3.toByte, 0xE4.toByte, 0xE5.toByte, 0xE6.toByte, 0xE7.toByte, 0xE8.toByte, 0xE9.toByte, 0x4A.toByte, 0xE0.toByte, 0x5A.toByte, 0x5F.toByte, 0x6D.toByte, //  80 -  95
      0x79.toByte, 0x81.toByte, 0x82.toByte, 0x83.toByte, 0x84.toByte, 0x85.toByte, 0x86.toByte, 0x87.toByte, 0x88.toByte, 0x89.toByte, 0x91.toByte, 0x92.toByte, 0x93.toByte, 0x94.toByte, 0x95.toByte, 0x96.toByte, //  96 - 111
      0x97.toByte, 0x98.toByte, 0x99.toByte, 0xA2.toByte, 0xA3.toByte, 0xA4.toByte, 0xA5.toByte, 0xA6.toByte, 0xA7.toByte, 0xA8.toByte, 0xA9.toByte, 0xC0.toByte, 0x6A.toByte, 0xD0.toByte, 0xA1.toByte, 0x00.toByte, // 112 - 127
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 128 - 143
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 144 - 159
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 160 - 175
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 176 - 191
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 192 - 207
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 208 - 223
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, // 224 - 239
      0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte, 0x00.toByte  // 240 - 255
    )
  }

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

object CodePage {
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getCodePageByName(codePageName: String): CodePage = {
    codePageName match {
      case "common"          => new CodePageCommon
      case "common_extended" => new CodePageCommonExt
      case "cp037"           => new CodePage037
      case "cp875"           => new CodePage875
      case "cp037_extended"  => new CodePage037Ext
      case codePage => throw new IllegalArgumentException(s"The code page '$codePage' is not one of the builtin EBCDIC code pages.")
    }
  }

  def getCodePageByClass(codePageClass: String): CodePage = {
    log.info(s"Instantiating code page class: $codePageClass")
    Class.forName(codePageClass,
                  true,
                  Thread.currentThread().getContextClassLoader)
      .newInstance()
      .asInstanceOf[CodePage]
  }


}
