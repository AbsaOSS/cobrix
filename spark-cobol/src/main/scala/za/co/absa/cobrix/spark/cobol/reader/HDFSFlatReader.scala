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

package za.co.absa.cobrix.spark.cobol.reader

import scala.collection.JavaConverters.asScalaBufferConverter
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.reader.iterator.{BinaryDataFlatRowIterator, BinaryDataMapIterator}
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

/** The Cobol data reader from HDFS filesystem that flattens the original schema and prvides flattened schema */
class HDFSFlatReader (hadoopConfiguration: Configuration, val copyBookFile: String) extends Reader with Serializable {
  private val cobolSchema: CobolSchema = loadCopyBook(hadoopConfiguration, new Path(copyBookFile))

  override def getCobolSchema: CobolSchema = cobolSchema
  override def getSparkSchema: StructType = cobolSchema.getSparkFlatSchema

  override def getRowIterator(binaryData: Array[Byte]): Iterator[Row] = {
    checkBinaryDataValidity(binaryData)
    new BinaryDataFlatRowIterator(binaryData, cobolSchema)
  }

  def getRowIteratorMap(binaryData: Array[Byte]): Iterator[Map[Field, Option[Value]]] = {
    checkBinaryDataValidity(binaryData)
    new BinaryDataMapIterator(binaryData, cobolSchema)
  }

  def generateDebugCsv(binaryData: Array[Byte]): String = {
    val it = getRowIteratorMap(binaryData).asInstanceOf[BinaryDataMapIterator]
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

  private def loadCopyBook(hadoopConfiguration: Configuration, copyBookFile: Path): CobolSchema = {
    val hdfs = FileSystem.get(hadoopConfiguration)
    val stream = hdfs.open(copyBookFile)
    val copyBookContents = try IOUtils.readLines(stream).asScala.mkString("\n") finally stream.close()
    val schema = CopybookParser.parseTree(EBCDIC(), copyBookContents)
    new CobolSchema(schema)
  }
}
