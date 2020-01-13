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

import scala.collection.mutable

/**
  * Wraps parameters provided by spark.read.option(...) in order to track which parameters are
  * actually used/recognized.
  *
  * @param params Parameters provided by spark.read.option(...)
  */
class Parameters(params: Map[String, String]) {
  private val usedKeys = new mutable.HashSet[String]

  /**
    * Gets a value from the underlying parameters map tracking its usage.
    *
    * @param key A key to the parameters map
    * @return A value associated with the key
    */
  def apply(key: String): String = {
    usedKeys += key
    params(key)
  }

  /**
    * Gets a value from the underlying parameters map tracking its usage.
    *
    * @param key A key to the parameters map
    * @return An option boxed value associated with the key
    */
  def get(key: String): Option[String] = {
    usedKeys += key
    params.get(key)
  }

  /**
    * Marks that a key is actually used by 'spark-cobol'. The method is used when options are
    * processed by enumerating the underlying map.
    *
    * @param key A key to the parameters map
    */
  def markUsed(key: String): Unit = usedKeys += key

  /**
    * Returns true if a key is present in the underlying map tracking its usage.
    *
    * @param key A key to the parameters map
    * @return True if the key is present in the map
    */
  def contains(key: String): Boolean = {
    usedKeys += key
    params.contains(key)
  }

  /**
    * Gets a value from the underlying parameters map tracking its usage.
    *
    * @param key     A key to the parameters map
    * @param default A default value that will be returned if the map does not contain the key.
    * @return An value associated with the key or the default value if the key is not present in the map
    */
  def getOrElse(key: String, default: => String): String = {
    usedKeys += key
    params.getOrElse(key, default)
  }

  /**
    * Returns the underlying map. This is used for getting spark-cobol options by enumerating the map itself.
    *
    * @return The underlying parameters map
    */
  def getMap: Map[String, String] = params

  /**
    * Returns the underlying map. This is used for getting spark-cobol options by enumerating the map itself.
    *
    * @return True is the provided key was used for determining how a COBOL data need to be processed.
    */
  def isKeyUsed(key: String): Boolean = {
    usedKeys.contains(key)
  }
}
