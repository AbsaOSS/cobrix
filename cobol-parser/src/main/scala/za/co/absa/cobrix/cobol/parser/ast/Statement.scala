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

package za.co.absa.cobrix.cobol.parser.ast

/** Trait for Cobol copybook AST element (a statement). */
trait Statement {
  val camelCased: String = {
    camelCase(name)
  }

  /** Returns the level of the AST element */
  def level: Int

  /** Returns the name of the AST element */
  def name: String

  /** Returns the line number in the copybook where the ast object is defined */
  def lineNumber: Int

  /** Returns the parent element of the ASt element */
  def parent: Option[Group]

  /** Returns a fields name that this AST element redefines (REDEFINES property) */
  def redefines: Option[String]

  /** Returns true is the AST element is an array (has OCCURS) */
  def isArray: Boolean = occurs.nonEmpty

  /** Returns OCCURS property of the AST element if present */
  def occurs: Option[Int]

  /** Returns TO property of the AST element if present */
  def to: Option[Int]

  /** Returns the minimum OCCURS (array) size of the field. Returns 1 for non-OCCURS (non-array) fields. */
  @throws(classOf[IllegalThreadStateException])
  def arrayMinSize: Int = {
    (occurs, to) match {
      case (None, None) => 1
      case (Some(n), None) => 1
      case (None, Some(_)) => throw new IllegalThreadStateException(s"Field properties 'OCCURS' and 'TO' are incorrectly specified for '$name'")
      case (Some(n), Some(_)) => n
    }
  }

  /** Returns the maximum OCCURS (array) size of the field. Returns 1 for non-OCCURS (non-array) fields. */
  @throws(classOf[IllegalThreadStateException])
  def arrayMaxSize: Int = {
    (occurs, to) match {
      case (None, None) => 1
      case (Some(n), None) => n
      case (None, Some(_)) => throw new IllegalThreadStateException(s"Field properties 'OCCURS' and 'TO' are incorrectly specified for '$name'")
      case (Some(_), Some(m)) => m
    }
  }

  /** Returns a field name this fields depends on. This is used for OCCURS (arrays). */
  def dependingOn: Option[String]

  /** Returns true if this field is redefined by some other field */
  def isRedefined: Boolean

  /** Returns true if the field is a filler */
  def isFiller: Boolean

  /** Returns true if the field is a child segment */
  def isChildSegment: Boolean

  /** A binary properties of a field */
  val binaryProperties: BinaryProperties

  /** Returns a parent of the current node **/
  def up(): Option[Statement] = this.parent

  /** Returns a string representation of the AST element */
  override def toString: String = {
    s"${" " * 2 * level}$camelCased ${camelCase(redefines.getOrElse(""))}"
  }

  /** Returns this the name of this fields as a camel cased string */
  def camelCase(s: String): String = {
    s.replace(".", "")
      .split("-")
      .map(c => c.toLowerCase.capitalize)
      .mkString
  }

  /** Returns the original AST element with updated binary properties */
  def withUpdatedBinaryProperties(newBinaryProperties: BinaryProperties): Statement

  /** Returns the original AST element with updated `isRedefined` flag */
  def withUpdatedIsRedefined(newIsRedefined: Boolean): Statement

}
