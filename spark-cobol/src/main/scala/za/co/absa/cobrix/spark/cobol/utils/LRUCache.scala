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

import scala.collection.JavaConverters._

class LRUCache[K,V](maxSize: Int, loadFactor: Float = 0.75f) {
  private val cache = new java.util.LinkedHashMap[K, V](16, loadFactor, true) {
    override def removeEldestEntry(eldest: java.util.Map.Entry[K, V]): Boolean = size() > maxSize
  }

  def apply(key: K): V = synchronized {
    cache.get(key)
  }

  def containsKey(key: K): Boolean = synchronized {
    cache.containsKey(key)
  }

  def get(key: K): Option[V] = synchronized {
    Option(cache.get(key))
  }

  def getMap: Map[K, V] = synchronized {
    cache.asScala.toMap
  }

  def put(key: K, value: V): Unit = synchronized {
    cache.put(key, value)
  }

  def remove(key: K): Unit = synchronized {
    cache.remove(key)
  }
}
