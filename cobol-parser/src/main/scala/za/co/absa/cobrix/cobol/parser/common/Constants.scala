/*
 * Copyright 2018-2019 ABSA Group Limited
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
  val dotCharEBCIDIC: Byte = 75
  val spaceCharEBCIDIC: Byte = 64
  val plusCharEBCIDIC: Byte = 78
  val plusCharASCII: Byte = 43

  // Min/max integer precision after the number is considered Int
  val minShortPrecision = 1
  val maxShortPrecision = 4
  val binaryShortSizeBytes = 2

  // Min/max integer precision after the number is considered Long
  val minIntegerPrecision = 5
  val maxIntegerPrecision = 9
  val binaryIntSizeBytes = 4

  // Min/max long precision after the number is considered BigInt
  val minLongPrecision = 10
  val maxLongPrecision = 18
  val binaryLongSizeBytes = 8


  // Max picture size of a Copybook field
  val maxFieldLength = 100000
  val maxRdWRecordSize: Int = 100*1024*1024

  // Max COMP/BINARY format precision
  val maxBinIntPrecision = 38

  // For Decimal types
  val maxDecimalPrecision = 38
  val maxDecimalScale = 18

  val floatSize = 4
  val doubleSize = 8

  // COMPRESSION values
  val compBinary1 = 0 // COMP
  val compBinary2 = 4 // COMP-4
  val compFloat = 1
  val compDouble = 2
  val compBCD = 3
  val compBinaryBinCutoff= 5
  val compBinaryLittleEndian= 9

  // The fields that can be automatically generated
  val segmentIdField = "Seg_Id"
  val fileIdField = "File_Id"
  val recordIdField = "Record_Id"

  // Types of built-in record header parsers
  val RhXcom = "xcom"
  val RhRdw = "rdw"
  val RhRdwBigEndian = "rdw_big_endian"
  val RhRdwLittleEndian = "rdw_little_endian"
}
