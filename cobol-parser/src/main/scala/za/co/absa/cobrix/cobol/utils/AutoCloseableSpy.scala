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

package za.co.absa.cobrix.cobol.utils

class AutoCloseableSpy(failCreate: Boolean = false, failAction: Boolean = false, failClose: Boolean = false) extends AutoCloseable {
  var actionCallCount: Int = 0
  var closeCallCount: Int = 0

  if (failCreate) {
    throw new RuntimeException("Failed to create resource")
  }

  def dummyAction(): Unit = {
    actionCallCount += 1
    if (failAction) {
      throw new RuntimeException("Failed during action")
    }
  }

  override def close(): Unit = {
    closeCallCount += 1
    if (failClose) {
      throw new RuntimeException("Failed to close resource")
    }
  }
}
