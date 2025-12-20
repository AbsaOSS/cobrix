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

package za.co.absa.cobrix.spark.cobol.source.utils

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.utils.LRUCache

class LRUCacheSuite extends AnyWordSpec {
  "LRUCache" should {
    "remember items put there" in {
      val cache = new LRUCache[Int, String](3)
      cache.put(1, "one")
      cache.put(2, "two")
      cache.put(3, "three")
      assert(cache.get(1).contains("one"))
      assert(cache.get(2).contains("two"))
      assert(cache.get(3).contains("three"))
    }

    "forget old items" in {
      val cache = new LRUCache[Int, String](3)
      cache.put(1, "one")
      cache.put(2, "two")
      cache.put(3, "three")
      cache.put(4, "four")
      assert(cache.get(1).isEmpty)
      assert(cache(1) == null)
      assert(cache.get(2).contains("two"))
      assert(cache(2) == "two")
      assert(cache.get(3).contains("three"))
      assert(cache.get(4).contains("four"))
    }

    "remember frequently used items" in {
      val cache = new LRUCache[Int, String](3)
      cache.put(1, "one")
      cache.put(2, "two")
      cache.put(3, "three")
      cache.get(1)
      cache.get(3)
      cache.put(4, "four")

      assert(cache.get(1).contains("one"))
      assert(cache.get(2).isEmpty)
      assert(cache.get(3).contains("three"))
      assert(cache.get(4).contains("four"))
    }

    "allow invalidating of values" in {
      val cache = new LRUCache[Int, String](3)
      cache.put(1, "one")
      cache.put(2, "two")
      cache.put(3, "three")
      cache.remove(3)
      cache.remove(4)
      cache.get(1)
      cache.get(3)
      cache.put(4, "four")

      assert(cache.containsKey(1))
      assert(!cache.containsKey(8))
      assert(cache(1) == "one")
      assert(cache(2) == "two")
      assert(cache(3) == null)
      assert(cache(4) == "four")
    }

    "return the cache as a map" in {
      val cache = new LRUCache[Int, String](3)
      cache.put(1, "one")
      cache.put(2, "two")
      cache.put(3, "three")

      assert(cache.getMap.size == 3)
    }
  }
}
