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

package za.co.absa.cobrix.spark.cobol.reader.fixedlen.iterator

import org.apache.spark.sql.Row
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.ReservedWords
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.{CobolSchema, SchemaRetentionPolicy}
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * This is an iterator traversing contents of Cobol binary data
  *
  * @param binaryData  A binary data to traverse
  * @param cobolSchema A Cobol schema obtained by parsing a copybook
  */
class FixedLenNestedRowIterator(val binaryData: Array[Byte],
                                val cobolSchema: CobolSchema,
                                policy: SchemaRetentionPolicy,
                                startOffset: Int,
                                endOffset: Int) extends Iterator[Row] {
  private val dataBits: BitVector = BitVector(binaryData)
  private val recordSize = cobolSchema.getRecordSize
  private var bitIndex = startOffset.toLong * 8

  override def hasNext: Boolean = bitIndex + recordSize <= dataBits.size

  @throws(classOf[IllegalStateException])
  override def next(): Row = {
    if (!hasNext) {
      throw new NoSuchElementException()
    }

    var offset = bitIndex
    val records = RowExtractors.extractRecord(cobolSchema.getCobolSchema.ast, dataBits, offset, generateRecordId = false, policy)

    // Advance bit index to the next record
    val lastRecord = cobolSchema.getCobolSchema.ast.last
    val lastRecordActualSize = lastRecord.binaryProperties.offset + lastRecord.binaryProperties.actualSize
    bitIndex += lastRecordActualSize + endOffset * 8

    records
  }

}
