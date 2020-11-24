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
  def offset: Long
}
