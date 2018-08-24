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

package za.co.absa.cobrix.spark.cobol.source.base.impl

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.commons.lang3.NotImplementedException
import za.co.absa.cobrix.spark.cobol.reader.fixedlen.FixedLenReader
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

class DummyFixedLenReader(sparkSchema: StructType, cobolSchema: CobolSchema, data: List[Map[String, Option[String]]])(invokeOnTraverse: () => Unit) extends FixedLenReader with Serializable {
  def getCobolSchema: CobolSchema = cobolSchema

  def getSparkSchema: StructType = sparkSchema

  def getRowIteratorMap(binaryData: Array[Byte]): Iterator[Map[String, Option[String]]] = {
    throw new NotImplementedException("Deprecated API, will be removed in further versions.")
  }

  def getRowWithSchemaIterator(binaryData: Array[Byte]): Iterator[Row] = {
    throw new NotImplementedException("")
  }
  
  def getRowIterator(binaryData: Array[Byte]): Iterator[Row] = {
    
    invokeOnTraverse()
    
    val recordsValues = for (record <- data) yield {
      val values = for (field <- sparkSchema.fields) yield {
        record(field.name).get
      }
      Row.fromSeq(values)
    }        
    recordsValues.iterator
  }

  def getRowIteratorFlat(binaryData: Array[Byte]): Iterator[Row] = {
    throw new NotImplementedException("")
  }

  override def getRecordStartOffset: Int = 0

  override def getRecordEndOffset: Int = 0
}
