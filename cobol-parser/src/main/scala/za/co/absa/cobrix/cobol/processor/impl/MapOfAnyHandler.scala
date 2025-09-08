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

package za.co.absa.cobrix.cobol.processor.impl

import za.co.absa.cobrix.cobol.parser.ast.Group
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordHandler

/**
  * A handler for processing COBOL records and mapping it to JVM data structures.
  *
  * This implementation uses a map from a string field name to value to represent struct fields from data records.
  */
class MapOfAnyHandler extends RecordHandler[Map[String, Any]] {
  override def create(values: Array[Any], group: Group): Map[String, Any] = {
    (group.children zip values).map(t => t._1.name -> (t._2 match {
      case s: Array[Any] => s.toSeq
      case s => s
    })).toMap
  }

  override def toSeq(record: Map[String, Any]): Seq[Any] = {
    record.values.toSeq
  }

  override def foreach(record: Map[String, Any])(f: Any => Unit): Unit = record.values.foreach(f)
}
