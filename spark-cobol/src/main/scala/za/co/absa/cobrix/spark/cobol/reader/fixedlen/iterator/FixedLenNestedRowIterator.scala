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
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.validator.ReaderParametersValidator
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.utils.RowExtractors

import scala.collection.immutable.HashMap

/**
  * This is an iterator traversing contents of Cobol binary data
  *
  * @param binaryData  A binary data to traverse
  * @param cobolSchema A Cobol schema obtained by parsing a copybook
  */
class FixedLenNestedRowIterator(val binaryData: Array[Byte],
                                val cobolSchema: CobolSchema,
                                readerProperties: ReaderParameters,
                                policy: SchemaRetentionPolicy,
                                startOffset: Int,
                                endOffset: Int) extends Iterator[Row] {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val recordSize = cobolSchema.getRecordSize
  private var byteIndex = startOffset

  private val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, cobolSchema.copybook)
  private val segmentRedefineMap = readerProperties.multisegment.map(_.segmentIdRedefineMap).getOrElse(HashMap[String, String]())
  private val segmentRedefineAvailable = segmentRedefineMap.nonEmpty

  override def hasNext: Boolean = byteIndex + recordSize <= binaryData.length

  @throws(classOf[IllegalStateException])
  override def next(): Row = {
    if (!hasNext) {
      throw new NoSuchElementException()
    }

    val offset = byteIndex

    val activeSegmentRedefine: String = if (segmentRedefineAvailable) {
      val segmentId = getSegmentId(binaryData, offset)
      val segmentIdStr = segmentId.getOrElse("")
      segmentRedefineMap.getOrElse(segmentIdStr, "")
    } else {
      ""
    }

    val records = RowExtractors.extractRecord(cobolSchema.getCobolSchema.ast,
      binaryData,
      offset,
      policy,
      activeSegmentRedefine = activeSegmentRedefine
    )

    // Advance byte index to the next record
    val lastRecord = cobolSchema.getCobolSchema.ast.children.last
    val lastRecordActualSize = lastRecord.binaryProperties.offset + lastRecord.binaryProperties.actualSize
    byteIndex += lastRecordActualSize + endOffset

    records
  }

  private def getSegmentId(data: Array[Byte], offset: Int): Option[String] = {
    segmentIdField.map(field => {
      val fieldValue = cobolSchema.copybook.extractPrimitiveField(field, data, offset)
      if (fieldValue == null) {
        logger.error(s"An unexpected null encountered for segment id at $byteIndex")
        ""
      } else {
        fieldValue.toString.trim
      }
    })
  }

}
