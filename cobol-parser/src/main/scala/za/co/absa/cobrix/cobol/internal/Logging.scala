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

package za.co.absa.cobrix.cobol.internal

import org.slf4j.{Logger, LoggerFactory}

/**
 * Utility trait for classes that want to log data. Creates a transient SLF4J logger for the class lazily
 * so that objects with Logging can be serialized and used on another machine
 */
private[cobrix] trait Logging {

  @transient private var log_ : Logger = null // scalastyle:ignore

  protected def logName: String = {
    this.getClass.getName.stripSuffix("$")
  }

  protected def logger: Logger = {
    if (log_ == null) { // scalastyle:ignore
      log_ = LoggerFactory.getLogger(logName)
    }
    log_
  }

}
