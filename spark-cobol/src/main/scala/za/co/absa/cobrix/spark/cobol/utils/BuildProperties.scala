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

package za.co.absa.cobrix.spark.cobol.utils

import java.util.Properties

object BuildProperties {
  private val properties = new Properties()
  private val buildVersionKey = "build.version"
  private val buildTimestampKey = "build.timestamp"

  /** Returns the version of the build. */
  lazy val buildVersion: String = properties.getProperty(buildVersionKey)

  /** Returns the version of the build. */
  lazy val buildTimestamp: String = properties.getProperty(buildTimestampKey)

  loadConfig()

  private def loadConfig(): Unit = {
    val is = getClass.getResourceAsStream("/cobrix_build.properties")
    try properties.load(is)
    finally if (is != null) is.close()
  }

}
