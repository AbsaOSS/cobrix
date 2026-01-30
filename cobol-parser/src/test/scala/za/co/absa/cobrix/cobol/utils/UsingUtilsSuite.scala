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

import org.scalatest.wordspec.AnyWordSpec

class UsingUtilsSuite extends AnyWordSpec {
  "using with a single resource" should {
    "properly close the resource" in {
      var resource: AutoCloseableSpy = null

      UsingUtils.using(new AutoCloseableSpy()) { res =>
        resource = res
        res.dummyAction()
      }

      assert(resource.actionCallCount == 1)
      assert(resource.closeCallCount == 1)
    }

    "close resource even if exception occurs" in {
      var resource: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        UsingUtils.using(new AutoCloseableSpy(failAction = true)) { res =>
          resource = res
          res.dummyAction()
        }
      } catch {
        case ex: Throwable =>
          assert(ex.getMessage.contains("Failed during action"))
          exceptionThrown = true
      }

      assert(exceptionThrown)
      assert(resource.actionCallCount == 1)
      assert(resource.closeCallCount == 1)
    }

    "handle a null resource" in {
      var resourceWasNull = false

      UsingUtils.using(null: AutoCloseableSpy) { res =>
        resourceWasNull = res == null
      }

      assert(resourceWasNull)
    }

    "handle a null resource and action throw" in {
      var exceptionThrown = false
      var resourceWasNull = false

      try {
        UsingUtils.using(null: AutoCloseableSpy) { res =>
          resourceWasNull = res == null
          throw new RuntimeException("Action throws")
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Action throws"))
      }

      assert(exceptionThrown)
      assert(resourceWasNull)
    }

    "handle a null return value" in {
      var resourceWasNull = false

      val result = UsingUtils.using(null: AutoCloseableSpy) { res =>
        resourceWasNull = res == null
        null: String
      }

      assert(resourceWasNull)
      assert(result == null)
    }

    "handle exceptions when a resource is created" in {
      var exceptionThrown = false
      var resource: AutoCloseableSpy = null

      try {
        UsingUtils.using(new AutoCloseableSpy(failCreate = true)) { res =>
          resource = res
          res.dummyAction()
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Failed to create resource"))
      }

      assert(exceptionThrown)
      assert(resource == null)
    }

    "handle exceptions when a resource is closed" in {
      var resource: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        UsingUtils.using(new AutoCloseableSpy(failClose = true)) { res =>
          resource = res
          res.dummyAction()
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Failed to close resource"))
      }

      assert(exceptionThrown)
      assert(resource.actionCallCount == 1)
      assert(resource.closeCallCount == 1)
    }

    "handle exceptions on both action and close" in {
      var resource: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        UsingUtils.using(new AutoCloseableSpy(failClose = true)) { res =>
          resource = res
          res.dummyAction()
          throw new RuntimeException("Failed during action")
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Failed during action"))
          val suppressed = ex.getSuppressed
          assert(suppressed.length == 1)
          assert(suppressed(0).getMessage.contains("Failed to close resource"))
      }

      assert(exceptionThrown)
      assert(resource.actionCallCount == 1)
      assert(resource.closeCallCount == 1)
    }
  }

  "using with two resources" should {
    "properly close both resources" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null

      val result = UsingUtils.using(new AutoCloseableSpy()) { res1 =>
        resource1 = res1
        UsingUtils.using(new AutoCloseableSpy()) { res2 =>
          resource2 = res2
          res1.dummyAction()
          res2.dummyAction()
          100
        }
      }

      assert(result == 100)
      assert(resource1.actionCallCount == 1)
      assert(resource1.closeCallCount == 1)
      assert(resource2.actionCallCount == 1)
      assert(resource2.closeCallCount == 1)
    }

    "properly close both resources when an inner one throws an exception during action and close" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        UsingUtils.using(new AutoCloseableSpy()) { res1 =>
          resource1 = res1
          UsingUtils.using(new AutoCloseableSpy(failAction = true, failClose = true)) { res2 =>
            resource2 = res2
            res1.dummyAction()
            res2.dummyAction()
          }
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Failed during action"))
          val suppressed = ex.getSuppressed
          assert(suppressed.length == 1)
          assert(suppressed(0).getMessage.contains("Failed to close resource"))
      }

      assert(exceptionThrown)
      assert(resource1.actionCallCount == 1)
      assert(resource1.closeCallCount == 1)
      assert(resource2.actionCallCount == 1)
      assert(resource2.closeCallCount == 1)
    }

    "properly close both resources when an outer one throws an exception during action and close" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        UsingUtils.using(new AutoCloseableSpy(failAction = true, failClose = true)) { res1 =>
          resource1 = res1
          UsingUtils.using(new AutoCloseableSpy()) { res2 =>
            resource2 = res2
            res1.dummyAction()
            res2.dummyAction()
          }
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Failed during action"))
          val suppressed = ex.getSuppressed
          assert(suppressed.length == 1)
          assert(suppressed(0).getMessage.contains("Failed to close resource"))
      }

      assert(exceptionThrown)
      assert(resource1.actionCallCount == 1)
      assert(resource1.closeCallCount == 1)
      assert(resource2.actionCallCount == 0)
      assert(resource2.closeCallCount == 1)
    }

    "properly close the outer resource when the inner one fails on create" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        UsingUtils.using(new AutoCloseableSpy()) { res1 =>
          resource1 = res1
          UsingUtils.using(new AutoCloseableSpy(failCreate = true)) { res2 =>
            resource2 = res2
            res1.dummyAction()
            res2.dummyAction()
          }
        }
      } catch {
        case ex: Throwable =>
          exceptionThrown = true
          assert(ex.getMessage.contains("Failed to create resource"))
      }

      assert(exceptionThrown)
      assert(resource1.actionCallCount == 0)
      assert(resource1.closeCallCount == 1)
      assert(resource2 == null)
    }
  }
}
