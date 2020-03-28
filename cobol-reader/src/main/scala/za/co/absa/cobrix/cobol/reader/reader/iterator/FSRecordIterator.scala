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

package za.co.absa.cobrix.cobol.parser.reader.iterator

import java.nio.file.{Files, Paths}
import java.io.FileInputStream

import za.co.absa.cobrix.cobol.parser.common.DataExtractors
import za.co.absa.cobrix.cobol.parser.Copybook


@throws(classOf[IllegalArgumentException])
class FSRecordIterator (cobolSchema: Copybook, binaryFilePath: String) extends Iterator[Seq[Any]] {
  private val binaryFileSize = Files.size(Paths.get(binaryFilePath))
  private val recordSize = cobolSchema.getRecordSize
  private var byteIndex = 0L
  private var inputStream = new FileInputStream(binaryFilePath)

  checkBinaryFileValidity()

  override def hasNext: Boolean = byteIndex + recordSize <= binaryFileSize

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalStateException])
  override def next(): Seq[Any] = {
    if (!hasNext) {
      throw new NoSuchElementException()
    }

    val bytes = new Array[Byte](recordSize)
    inputStream.read(bytes)

    val records = DataExtractors.extractValues(cobolSchema.getCobolSchema, bytes)

    // Advance byte index to the next record
    val lastRecord = cobolSchema.getCobolSchema.children.last
    val lastRecordActualSize = lastRecord.binaryProperties.offset + lastRecord.binaryProperties.actualSize
    byteIndex += lastRecordActualSize

    records
  }

  @throws(classOf[IllegalArgumentException])
  private def checkBinaryFileValidity(): Unit = {
    if (recordSize <= 0) {
      throw new IllegalArgumentException (s"Incorrect binary record size $recordSize. The size should be greater than zero.")
    }
    if (binaryFileSize < recordSize) {
      throw new IllegalArgumentException (s"Binary file is too small. Expected binary record size = $recordSize, got $binaryFileSize.")
    }
    if (binaryFileSize % recordSize > 0) {
      throw new IllegalArgumentException (s"Binary record size $recordSize does not divide data size $binaryFileSize.")
    }
  }

}
