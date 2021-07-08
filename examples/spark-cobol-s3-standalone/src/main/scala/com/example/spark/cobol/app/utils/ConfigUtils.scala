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

import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object ConfigUtils {
  private val log = LoggerFactory.getLogger(this.getClass)

  def getOptListStrings(conf: Config, path: String): Seq[String] = {
    if (conf.hasPath(path)) {
      conf.getStringList(path).asScala.toList
    } else {
      Nil
    }
  }

  def getExtraOptions(conf: Config, prefix: String): Map[String, String] = {
    if (conf.hasPath(prefix)) {
      ConfigUtils.getFlatConfig(conf.getConfig(prefix))
        .map { case (k, v) => (k, v.toString) }
    } else {
      Map()
    }
  }

  def logExtraOptions(description: String, extraOptions: Map[String, String]): Unit = {
    if (extraOptions.nonEmpty) {
      val p = "\""
      log.info(description)
      extraOptions.foreach { case (key, value) => {
        log.info(s"$key = $p$value$p")
      }}
    }
  }

  def getFlatConfig(conf: Config): Map[String, AnyRef] = {
    conf.entrySet().asScala.map({ entry =>
      entry.getKey -> entry.getValue.unwrapped()
    }).toMap
  }

  def getRedactedValue(key: String, value: AnyRef, tokensToRedact: Set[String]): AnyRef = {
    val needRedact = tokensToRedact.exists(w => key.toLowerCase.contains(w.toLowerCase()))
    if (needRedact) {
      "[redacted]"
    } else {
      value
    }
  }
}
