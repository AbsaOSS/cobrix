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

object UsingUtils {
  /**
    * Executes the given action with a resource that implements the AutoCloseable interface, ensuring
    * proper closure of the resource. Any exception that occurs during the action or resource closure
    * is handled appropriately.
    *
    * This is for use in Scala before Scala 2.13 introduced `Using` in `scala.util.Using` and works
    * similarly to Java try-with-resources with the exception that it does not support multiple resources.
    * But `using()` can be nested.
    *
    * Example usage:
    * {{{
    *   val result = UsingUtils.using(new AutoCloseableResource()) { resource =>
    *     // Perform operations with the resource
    *     // Return value of any type, including Unit, and null, or throw an exception.
    *   }
    * }}}
    *
    * @param resource a resource that implements AutoCloseable.
    * @param action   an action to be executed using the provided resource.
    * @tparam T the type of the resource, which must extend AutoCloseable.
    * @throws Throwable if either the action or resource closure fails. If both fail, the action's exception
    *                   is thrown with the closure's exception added as suppressed.
    */
  def using[T <: AutoCloseable, U](resource: => T)(action: T => U): U = {
    var actionException: Throwable = null
    val openedResource = resource

    try {
      action(openedResource)
    } catch {
      case t: Throwable =>
        actionException = t
        throw t
    } finally
      if (openedResource != null) {
        try
          openedResource.close()
        catch {
          case closeException: Throwable =>
            if (actionException != null) {
              actionException.addSuppressed(closeException)
            } else {
              throw closeException
            }
        }
      }
  }

  /**
    * Implicits implementing resource management via for comprehension. Example usage:
    * {{{
    *   import za.co.absa.cobrix.cobol.utils.UsingUtils.Implicits._
    *
    *   for {
    *     res1 <- new AutoCloseableResource()
    *     res2 <- new AutoCloseableResource()
    *   } {
    *     // Perform operations with the resource
    *   }
    * }}}
    *
    * or
    *
    * {{{
    *   import za.co.absa.cobrix.cobol.utils.UsingUtils.Implicits._
    *
    *   val result = for {
    *     res1 <- new AutoCloseableResource()
    *     res2 <- new AutoCloseableResource()
    *   } yield {
    *     // Perform operations with the resource, and return a value
    *   }
    * }}}
    */
  object Implicits {
    implicit class ResourceWrapper[T <: AutoCloseable](private val resource: T) {
      def foreach(f: T => Unit): Unit = using(resource)(f)

      def map[U](body: T => U): U = using(resource)(body)

      def flatMap[U](body: T => U): U = using(resource)(body)

      def withFilter(p: T => Boolean): FilteredResourceWrapper[T] = new FilteredResourceWrapper[T](resource, p)
    }
  }

  final class FilteredResourceWrapper[T <: AutoCloseable](private val resource: T,
                                                          private val p: T => Boolean) {
    def foreach(f: T => Unit): Unit =
      using(resource) { r =>
        if (p(r)) f(r) else ()
      }

    def map[U](body: T => U): U =
      using(resource) { r =>
        if (p(r)) body(r) else throw new NoSuchElementException("withFilter predicate is false")
      }

    def flatMap[U](body: T => U): U =
      using(resource) { r =>
        if (p(r)) body(r) else throw new NoSuchElementException("withFilter predicate is false")
      }

    def withFilter(p2: T => Boolean): FilteredResourceWrapper[T] =
      new FilteredResourceWrapper[T](resource, r => p(r) && p2(r))
  }
}
