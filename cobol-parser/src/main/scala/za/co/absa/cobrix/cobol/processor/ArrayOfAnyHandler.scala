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

package za.co.absa.cobrix.cobol.processor

import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordHandler

/**
  * A handler for processing COBOL records and mapping it to JVM data structures.
  *
  * This implementation uses an array to group data fields of struct fields.
  */
class ArrayOfAnyHandler extends RecordHandler[scala.Array[Any]] {
  override def create(values: Array[Any], group: Group): Array[Any] = values

  override def toSeq(record: Array[Any]): Seq[Any] = record.toSeq

  override def foreach(record: Array[Any])(f: Any => Unit): Unit = record.foreach(f)
}
