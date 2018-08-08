/*
 * Copyright 2018 Barclays Africa Group Limited
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

package za.co.absa.cobrix.spark.cobol.reader.fixedlen

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.iterator.{FixedLenFlatRowIterator, FixedLenMapIterator}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

/** The Cobol data reader that provides output using flattened schema */
class FixedLenFlatReader(val copyBookContents: String) extends FixedLenReader with Serializable {
  private val cobolSchema: CobolSchema = loadCopyBook(copyBookContents)

  override def getCobolSchema: CobolSchema = cobolSchema
  override def getSparkSchema: StructType = cobolSchema.getSparkFlatSchema

  override def getRowIterator(binaryData: Array[Byte]): Iterator[Row] = {
    checkBinaryDataValidity(binaryData)
    new FixedLenFlatRowIterator(binaryData, cobolSchema)
  }

  def getRowIteratorMap(binaryData: Array[Byte]): Iterator[Map[Field, Option[Value]]] = {
    checkBinaryDataValidity(binaryData)
    new FixedLenMapIterator(binaryData, cobolSchema)
  }

  def generateDebugCsv(binaryData: Array[Byte]): String = {
    val it = getRowIteratorMap(binaryData).asInstanceOf[FixedLenMapIterator]
    val headers = it.generateDebugCSVHeaders
    val rows = for(row <- it) yield it.generateDebugCSVCurrentRow
    headers + "\n" + rows.mkString("\n")
  }

  private def checkBinaryDataValidity(binaryData: Array[Byte]): Unit = {
    if (binaryData.length < cobolSchema.getRecordSize) {
      throw new IllegalArgumentException (s"Binary record too small. Expected binary record size = ${cobolSchema.getRecordSize}, got ${binaryData.length} ")
    }
    if (binaryData.length % cobolSchema.getRecordSize > 0) {
      throw new IllegalArgumentException (s"Binary record size ${cobolSchema.getRecordSize} does not divide data size ${binaryData.length}.")
    }
  }

  private def loadCopyBook(copyBookContents: String): CobolSchema = {
    val schema = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    new CobolSchema(schema)
  }
}
