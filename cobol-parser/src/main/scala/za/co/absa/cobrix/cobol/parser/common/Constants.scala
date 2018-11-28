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

package za.co.absa.cobrix.cobol.parser.common

/** The object contains various constants related to the Cobol copybook parser */
object Constants {

  // The '-' character in different encodings
  val minusCharEBCIDIC: Byte = 96
  val minusCharASCII: Byte = 45
  val plusCharEBCIDIC: Byte = 78
  val plusCharASCII: Byte = 43

  // Max integer precision after the number is considered Int
  val maxShortPrecision = 4

  // Max integer precision after the number is considered Long
  val maxIntegerPrecision = 9

  // Max long precision after the number is considered BigInt
  val maxLongPrecision = 18

  // Max picture size of a Copybook field
  val maxFieldLength = 100000
  val maxXcomRecordSize: Int = 100*1024*1024

  // Max COMP/BINARY format precision
  val maxBinIntPrecision = 18

  // For Decimal types
  val maxDecimalPrecision = 38
  val maxDecimalScale = 18

  // The fields that can be automatically generated
  val segmentIdField = "Seg_Id"
  val fileIdField = "File_Id"
  val recordIdField = "Record_Id"

}
