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

import scala.util.control.NonFatal

object UsingUtils {
  /**
    * Executes the given action with a resource that implements the AutoCloseable interface, ensuring
    * proper closure of the resource. Any exception that occurs during the action or resource closure
    * is handled appropriately, with suppressed exceptions added where relevant. Null resources are not supported.
    *
    * @param resource a lazily evaluated resource that implements AutoCloseable
    * @param action   a function to be executed using the provided resource
    * @tparam T the type of the resource, which must extend AutoCloseable
    * @throws Throwable if either the action or resource closure fails. If both fail, the action's exception
    *                   is thrown with the closure's exception added as suppressed
    */
  def using[T <: AutoCloseable,U](resource: => T)(action: T => U): U = {
    var thrownException: Option[Throwable] = None
    var suppressedException: Option[Throwable] = None
    val openedResource = resource

    val result = try {
      Option(action(openedResource))
    } catch {
      case NonFatal(ex) =>
        thrownException = Option(ex)
        None
    } finally
      if (openedResource != null) {
        try
          openedResource.close()
        catch {
          case NonFatal(ex) => suppressedException = Option(ex)
        }
      }

    (thrownException, suppressedException) match {
      case (Some(thrown), Some(suppressed)) =>
        thrown.addSuppressed(suppressed)
        throw thrown
      case (Some(thrown), None)     => throw thrown
      case (None, Some(suppressed)) => throw suppressed
      case (None, None)             => result.getOrElse(throw new IllegalArgumentException("Action returned null"))
    }
  }
}
