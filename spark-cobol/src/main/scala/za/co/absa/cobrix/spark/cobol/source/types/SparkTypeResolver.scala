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

package za.co.absa.cobrix.spark.cobol.source.types

import org.apache.spark.sql.types.ByteType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.FloatType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.ShortType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.BooleanType
import org.apache.spark.sql.types.StringType

/**
 * Provides construction of Spark types (i.e. StructFields) from Scala types.
 */
object SparkTypeResolver {

  def toSparkType(name: String, value: Any, nullable: Boolean): StructField = {
    
    require(name != null && !name.isEmpty(), "Null or empty field name.")
    require(value != null, "Null field value.")    
    
    value match {
      case _: Double  => new StructField(name, DoubleType, nullable)
      case _: Float   => new StructField(name, FloatType, nullable)
      case _: Long    => new StructField(name, LongType, nullable)
      case _: Int     => new StructField(name, IntegerType, nullable)
      case _: Short   => new StructField(name, ShortType, nullable)
      case _: Byte    => new StructField(name, ByteType, nullable)
      case _: Boolean => new StructField(name, BooleanType, nullable)
      case _: Char    => new StructField(name, StringType, nullable)
      case _: String  => new StructField(name, StringType, nullable)
      case _          => throw new IllegalArgumentException("Unknown type: " + value.getClass().getCanonicalName)
    }
  }
}