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

package za.co.absa.cobrix.spark.cobol

import org.apache.spark.sql.SparkSession
import za.co.absa.cobrix.spark.cobol.builder.{SparkCobolBuilder, ValidatorBuilder}

object Cobrix {
  /**
    * Creates a builder for converting an RDD of COBOL records into a Spark DataFrame.
    *
    * The returned builder is used to specify the copybook that describes the record layout,
    * either by providing its contents directly or by giving a path to a copybook file.
    * Further parsing options can then be configured before the conversion is performed.
    *
    * @return a new [[SparkCobolBuilder]] instance for specifying the copybook and conversion options
    */
  def fromRdd(implicit spark: SparkSession): SparkCobolBuilder = {
    new SparkCobolBuilder()
  }

  /**
    * Creates a builder for validating spark-cobol options against a set of data files.
    *
    * The returned builder is used to specify reader options, either one at a time or as a map.
    * Option keys are treated case-insensitively. The data files can then be validated against
    * these options without fully loading them into a DataFrame. File paths passed for validation
    * may contain wildcards.
    *
    * @return a new [[ValidatorBuilder]] instance for specifying options and validating data files
    */
  def validateOptions(implicit spark: SparkSession): ValidatorBuilder = {
    new ValidatorBuilder()
  }
}
