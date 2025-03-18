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

package za.co.absa.cobrix.cobol.reader.extractors.raw

/**
 * This trait represents a contract for extracting raw records from a stream of bytes.
 * A raw record is an array of bytes.
 *
 * Record extractors are used for in situations where the size of records in a file is not fixed and cannot be
 * determined neither from the copybook nor from record headers.
 */
trait RawRecordExtractor extends Iterator[Array[Byte]] {
  /**
    * Returns the byte offset of the next record.
    *
    * The offset should point to the absolute beginning of the record, e.g. including headers,
    * so that if a record extractor is started from this offset it would be able to extract the record
    * by invoking .next().
    *
    * IMPORTANT. The offset points to the next record to be fetched by .next(). If this invariant is not held,
    * the reader might get inconsistent record ids, or can fail in certain circumstances.
    *
    * If you want to prefetch a record, use this pattern:
    * {{{
    * class ExampleRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
    *
    *   private var currentOffset = ctx.inputStream.offset
    *
    *   var record: Option[Array[Byte]] = fetchRecord()
    *
    *   override def offset: Long = currentOffset
    *
    *   override def hasNext: Boolean = record.isDefined
    *
    *   override def next(): Array[Byte] = {
    *     if (record.isEmpty)
    *         throw new NoSuchElementException("next on empty iterator")
    *
    *     currentOffset = ctx.inputStream.offset
    *     val result = record.get
    *     record = fetchRecord()
    *     result
    *   }
    *
    *   def fetchRecord(): Option[Array[Byte]] = {
    *     // fetch the record from the stream if any
    *   }
    * }
    * }}}
    */
  def offset: Long

  /**
    * Returns true if the input stream can be split at the given location.
    *
    * Usually every file can be split by the location at the beginning of each record.
    * However, block variable length files (record_format = VB) can only be split by blocks
    */
  def canSplitHere: Boolean = true

  /**
    * Clients of 'spark-cobol' can pass additional information to custom record header parsers using
    *
    * ```
    * .option("re_additional_info", "...anything as a string...")
    * ```
    *
    * If a client provides any additional info the method will be executed just after constructing
    * the record header parser.
    *
    * Built-in record header parsers ignore the additional info. This info string is intended for
    * custom record header parsers.
    *
    * @param additionalInfo A string provided by a client for the record header parser.
    */
  def onReceiveAdditionalInfo(additionalInfo: String): Unit = { }
}
