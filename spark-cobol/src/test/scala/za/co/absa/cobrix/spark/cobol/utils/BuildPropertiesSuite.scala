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

import org.scalatest.wordspec.AnyWordSpec

class BuildPropertiesSuite extends AnyWordSpec {
  "BuildProperties" should {
    "return non-empty build version" in {
      assert(BuildProperties.buildVersion.nonEmpty)
    }

    "return non-empty build timestamp" in {
      assert(BuildProperties.buildTimestamp.nonEmpty)
    }

    "return full version containing build version and timestamp for SNAPSHOT versions" in {
      val fullVersion = BuildProperties.getFullVersion
      assert(fullVersion.contains(BuildProperties.buildVersion))
      if (BuildProperties.buildVersion.contains("SNAPSHOT")) {
        assert(fullVersion.contains(BuildProperties.buildTimestamp))
      }
    }
  }
}
