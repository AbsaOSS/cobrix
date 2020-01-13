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

import org.apache.spark.sql.types.StructType
import za.co.absa.cobrix.spark.cobol.schema.{CobolSchema, SchemaRetentionPolicy}
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Group

import scala.collection.Seq

class DummyCobolSchema(val sparkSchema: StructType) extends CobolSchema(new Copybook(Group.root), SchemaRetentionPolicy.KeepOriginal, "", false) with Serializable {
  override def getSparkSchema: StructType = sparkSchema

  override lazy val getRecordSize: Int = 40
}
