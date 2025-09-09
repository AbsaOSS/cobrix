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

package za.co.absa.cobrix.cobol.testutils

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}

object ComparisonUtils {
  def assertArraysEqual(actual: Array[Byte], expected: Array[Byte]): Assertion = {
    if (!actual.sameElements(expected)) {
      val actualHex = actual.map(b => f"0x$b%02X").mkString(", ")
      val expectedHex = expected.map(b => f"0x$b%02X").mkString(", ")
      fail(s"Actual:   $actualHex\nExpected: $expectedHex")
    } else {
      succeed
    }
  }

}
