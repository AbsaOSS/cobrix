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

package za.co.absa.cobrix.spark.cobol.reader.varlen

import org.apache.spark.sql.Row
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.reader.index.entry.SparseIndexEntry

import scala.collection.mutable.ArrayBuffer

/** The abstract class for Cobol data readers from various sequential sources (e.g. variable size EBCDIC records) */
abstract class VarLenReader extends Reader with Serializable {

  /** Returns true if index generation is requested */
  def isIndexGenerationNeeded: Boolean

  /** Returns true if RDW header of variable length files is big endian */
  def isRdwBigEndian: Boolean

  /**
    * Returns a file iterator between particular offsets. This is for faster traversal of big binary files
    *
    * @param binaryData          A stream positioned at the beginning of the intended file portion to read
    * @param startingFileOffset  An offset of the file where parsing should be started
    * @param fileNumber          A file number uniquely identified a particular file of the data set
    * @param startingRecordIndex A starting record index of the data
    * @return An iterator of Spark Row objects
    *
    */
  @throws(classOf[Exception]) def getRowIterator(binaryData: SimpleStream,
                                                 startingFileOffset: Long,
                                                 fileNumber: Int,
                                                 startingRecordIndex: Long): Iterator[Row]

  /**
    * Traverses the data sequentially as fast as possible to generate record index.
    * This index will be used to distribute workload of the conversion.
    *
    * @param binaryData A stream of input binary data
    * @param fileNumber A file number uniquely identified a particular file of the data set
    * @return An index of the file
    *
    */
  @throws(classOf[Exception]) def generateIndex(binaryData: SimpleStream,
                                                fileNumber: Int,
                                                isRdwBigEndian: Boolean): ArrayBuffer[SparseIndexEntry]
}
