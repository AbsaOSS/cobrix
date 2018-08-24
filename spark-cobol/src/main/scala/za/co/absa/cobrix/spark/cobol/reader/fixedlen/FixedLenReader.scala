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

package za.co.absa.cobrix.spark.cobol.reader.fixedlen

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.spark.cobol.reader.Reader
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

/** The abstract class for Cobol block (fixed length records) data readers from various sources */
abstract class FixedLenReader extends Reader with Serializable {
  @throws(classOf[Exception]) def getRowIterator(binaryData: Array[Byte]): Iterator[Row]
}
