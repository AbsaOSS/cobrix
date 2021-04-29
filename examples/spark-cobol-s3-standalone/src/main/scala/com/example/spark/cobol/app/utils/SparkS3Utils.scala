/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spark.cobol.app.utils

import com.example.spark.cobol.app.config.ConfigKeys._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object SparkS3Utils {
  private val log = LoggerFactory.getLogger(this.getClass)

  def enableSparkS3FromConfig(spark: SparkSession, conf: Config): SparkSession = {
    val redactTokens = ConfigUtils.getOptListStrings(conf, HADOOP_REDACT_TOKENS).toSet

    if (conf.hasPath(HADOOP_OPTION_PREFIX)) {
      val sc = spark.sparkContext
      val hadoopOptions = ConfigUtils.getExtraOptions(conf, HADOOP_OPTION_PREFIX)

      hadoopOptions.foreach { case (key, value) =>
        val redactedValue = ConfigUtils.getRedactedValue(key, value, redactTokens).toString
        log.info(s"Hadoop config: $key = $redactedValue")

        sc.hadoopConfiguration.set(key, value)
      }
    }
    spark
  }

}
