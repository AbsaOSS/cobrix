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

import com.example.spark.cobol.app.utils.ConfigUtils._
import com.typesafe.config.ConfigException.WrongType
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpec

class ConfigUtilsSuite extends AnyWordSpec {
  private val testConfig = ConfigFactory.parseResources("test/config/testconfig.conf")

  "getOptListStrings()" should {
    "return a list if it is set" in {
      val list = getOptListStrings(testConfig, "mytest.list.str")

      assert(list.nonEmpty)
      assert(list == Seq("A", "B", "C"))
    }

    "return a list of strings even if elements are values" in {
      val list = getOptListStrings(testConfig, "mytest.array")

      assert(list.nonEmpty)
      assert(list == Seq("10", "5", "7", "4"))
    }

    "return an empty list if no such key" in {
      val list = getOptListStrings(testConfig, "mytest.dummy")

      assert(list.isEmpty)
    }

    "throw WrongType exception if a wrong type of value is set" in {
      val ex = intercept[WrongType] {
        getOptListStrings(testConfig, "mytest.password")
      }
      assert(ex.getMessage.contains("has type STRING rather than LIST"))
    }
  }

  "getExtraOptions()" should {
    "return a new config if the prefix path exists" in {
      val map = getExtraOptions(testConfig, "mytest.extra.options")

      assert(map.size == 2)
      assert(map("value1") == "value1")
      assert(map("value2") == "100")
    }

    "return an empty map if no such key" in {
      val map = getExtraOptions(testConfig, "mytest.extra.options.dummy")

      assert(map.isEmpty)
    }

    "return arrays as strings if extra options contain lists" in {
      val map = getExtraOptions(testConfig, "mytest.extra.options2")

      assert(map.size == 3)
      assert(map("value1") == "value1")
      assert(map("value2") == "100")
      assert(map("value3") == "[10, 5, 7, 4]")
    }

    "throw WrongType exception if the path is not a config" in {
      val ex = intercept[WrongType] {
        getExtraOptions(testConfig, "mytest.extra.options.value1")
      }
      assert(ex.getMessage.contains("has type STRING rather than OBJECT"))
    }
  }

  "getRedactedValue()" should {
    "redact keys containing the list of tokens" in {
      val tokens = Set("secret", "password", "session.token")

      assert(getRedactedValue("mytest.password", "pwd", tokens) == "[redacted]")
      assert(getRedactedValue("mytest.secret", "pwd", tokens) == "[redacted]")
      assert(getRedactedValue("mytest.session.token", "pwd", tokens) == "[redacted]")
      assert(getRedactedValue("mytest.session.name", "name", tokens) == "name")
    }
  }
}
