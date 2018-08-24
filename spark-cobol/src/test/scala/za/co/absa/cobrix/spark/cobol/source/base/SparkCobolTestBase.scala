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

package za.co.absa.cobrix.spark.cobol.source.base

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructType
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}
import za.co.absa.cobrix.spark.cobol.source.types.SparkTypeResolver

/**
 * This class should be extended by Specs that need a SQLContext instance.
 * The SQLContext instance is only created once, thus, if new instantiations are needed, the subclasses must perform them.
 */
class SparkCobolTestBase extends FlatSpec with BeforeAndAfter with Matchers with Serializable with SparkTestBase {

  @transient protected var sqlContext: SQLContext = spark.sqlContext

  protected def createSparkSchema(types: Map[String, Any]): StructType = {
    createSparkSchema(types.map(t => (t._1, t._2, false)).toList)
  }

  protected def createSparkSchema(types: List[(String, Any, Boolean)]) = {
    val fields = types.map(tuple => SparkTypeResolver.toSparkType(tuple._1, tuple._2, tuple._3))
    new StructType(fields.toArray)
  }
}