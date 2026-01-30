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
  import UsingUtils.Implicits._

  "using with a single resource" should {
    "properly close the resource" in {
      var resource: AutoCloseableSpy = null

      for (res <- new AutoCloseableSpy()) {
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

    "work with for comprehension" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null

      val result = for {
        res1 <- new AutoCloseableSpy()
        res2 <- new AutoCloseableSpy()
      } yield {
        resource1 = res1
        resource2 = res2
        res1.dummyAction()
        res2.dummyAction()
        100
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

    "properly close both resources when an inner one throws an exception during action and close (for comprehension)" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        for {
          res1 <- new AutoCloseableSpy()
          res2 <- new AutoCloseableSpy(failAction = true, failClose = true)
        } {
          resource1 = res1
          resource2 = res2
          res1.dummyAction()
          res2.dummyAction()
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

    "properly close both resources when an outer one throws an exception during action and close (for comprehension)" in {
      var resource1: AutoCloseableSpy = null
      var resource2: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        for {
          res1 <- new AutoCloseableSpy(failAction = true, failClose = true)
          res2 <- new AutoCloseableSpy()
        } {
          resource1 = res1
          resource2 = res2
          res1.dummyAction()
          res2.dummyAction()
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

    "properly close the outer resource when the inner one fails on create (for comprehension)" in {
      val resource1: AutoCloseableSpy = new AutoCloseableSpy()
      var resource2: AutoCloseableSpy = null
      var exceptionThrown = false

      try {
        resource1
          .flatMap(res1 =>
            new AutoCloseableSpy(failCreate = true)
              .map(res2 => {
                resource2 = res2
                res1.dummyAction()
                res2.dummyAction()
              })
          )
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

  "withFilter (for-comprehension guards)" should {
    "skip the body when the guard is false (foreach form) and still close the resource" in {
      val res = new AutoCloseableSpy()

      var bodyRan = false
      for {
        r <- res if false
      } {
        bodyRan = true
        r.dummyAction()
      }

      assert(!bodyRan)
      assert(res.actionCallCount == 0)
      assert(res.closeCallCount == 1)
    }

    "run the body when the guard is true (foreach form) and close the resource" in {
      val res = new AutoCloseableSpy()

      var bodyRan = false
      for {
        r <- res if true
      } {
        bodyRan = true
        r.dummyAction()
      }

      assert(bodyRan)
      assert(res.actionCallCount == 1)
      assert(res.closeCallCount == 1)
    }

    "close both resources when an inner guard is false (nested generators)" in {
      val r1 = new AutoCloseableSpy()
      val r2 = new AutoCloseableSpy()

      var bodyRan = false
      for {
        a <- r1
        b <- r2 if false
      } {
        bodyRan = true
        a.dummyAction()
        b.dummyAction()
      }

      assert(!bodyRan)
      assert(r1.actionCallCount == 0)
      assert(r2.actionCallCount == 0)
      assert(r2.closeCallCount == 1)
      assert(r1.closeCallCount == 1)
    }

    "compose multiple guards correctly (both must be true)" in {
      val res = new AutoCloseableSpy()

      var bodyRan = false
      for {
        r <- res if true if false
      } {
        bodyRan = true
        r.dummyAction()
      }

      assert(!bodyRan)
      assert(res.actionCallCount == 0)
      assert(res.closeCallCount == 1)
    }

    "not evaluate the body when the first guard is false (side-effect check)" in {
      val res = new AutoCloseableSpy()

      var sideEffect = 0
      for {
        r <- res if false if { sideEffect += 1; true }
      } {
        r.dummyAction()
      }

      assert(sideEffect == 0)
      assert(res.actionCallCount == 0)
      assert(res.closeCallCount == 1)
    }

    "throw on yield when the guard is false (map path) and still close the resource" in {
      val res = new AutoCloseableSpy()

      var thrown = false
      try {
        val _ = for {
          r <- res if false
        } yield {
          r.dummyAction()
          1
        }
      } catch {
        case _: NoSuchElementException => thrown = true
      }

      assert(thrown)
      assert(res.actionCallCount == 0)
      assert(res.closeCallCount == 1)
    }

    "throw on a guarded middle generator in a yield (flatMap->map path) and close all opened resources" in {
      val r1 = new AutoCloseableSpy()
      val r2 = new AutoCloseableSpy()

      var thrown = false
      try {
        val _ = for {
          a <- r1
          b <- r2 if false
        } yield {
          a.dummyAction()
          b.dummyAction()
          1
        }
      } catch {
        case _: NoSuchElementException => thrown = true
      }

      assert(thrown)
      assert(r1.actionCallCount == 0)
      assert(r2.actionCallCount == 0)
      assert(r2.closeCallCount == 1)
      assert(r1.closeCallCount == 1)
    }
  }
}
