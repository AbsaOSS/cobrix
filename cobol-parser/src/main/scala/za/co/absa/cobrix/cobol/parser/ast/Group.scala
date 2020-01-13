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

import za.co.absa.cobrix.cobol.parser.ast.datatype.Usage

import scala.collection.mutable

/** An abstraction for the non-leaves in the Cobol copybook
  *
  * @param level             A level for the statement
  * @param name              An identifier
  * @param lineNumber        An line number in the copybook
  * @param children          Child entities
  * @param redefines         A name of a field which is redefined by this one
  * @param isRedefined       Is the field redefined by an other field
  * @param isSegmentRedefine Is the field corresponds to one of the segments (it should be a redefine)
  * @param parentSegment     Specifies a parent segment for a segment redefine in hierarchical files
  * @param occurs            The number of elements in an fixed size array / minimum items in variable-sized array
  * @param to                The maximum number of items in a variable size array
  * @param dependingOn       A field which specifies size of the array in a record
  * @param isFiller          Is the group a filler (unnamed block of data)
  * @param groupUsage        A USAGE to be inherited by all the fields in the group
  * @param nonFillerSize     The number of non-filler children in the group
  * @param binaryProperties  Pre-calculated offsets and sizes of thebinary data of the group
  * @param parent            A parent node
  */
case class Group(
                  level: Int,
                  name: String,
                  lineNumber: Int,
                  children: mutable.ArrayBuffer[Statement] = mutable.ArrayBuffer(),
                  redefines: Option[String] = None,
                  isRedefined: Boolean = false,
                  isSegmentRedefine: Boolean = false,
                  parentSegment: Option[Group] = None,
                  occurs: Option[Int] = None,
                  to: Option[Int] = None,
                  dependingOn: Option[String] = None,
                  isFiller: Boolean = false,
                  groupUsage: Option[Usage] = None,
                  nonFillerSize: Int = 0,
                  binaryProperties: BinaryProperties = BinaryProperties(0, 0, 0)
                )
                (val parent: Option[Group] = None)
  extends Statement {

  /** This method is used to add a [[za.co.absa.cobrix.cobol.parser.ast.Statement]] object as a child of
    * another [[za.co.absa.cobrix.cobol.parser.ast.Statement]] object
    *
    * @param tree A tree to add this item to
    * @tparam T Either Group or Primitive
    * @return the new tree
    */
  @throws(classOf[IllegalThreadStateException])
  def add[T <: Statement](tree: T): Statement = {
    val child = tree match {
      case grp: Group => grp.copy()(Some(this))
      case st: Primitive => st.copy()(Some(this))
      case _ => throw new IllegalStateException("Unknown AST object encountered while parsing a Cobol copybook")
    }
    children += child
    child
  }

  /** Returns true if the field is a child segment */
  def isChildSegment: Boolean = parentSegment.nonEmpty

  /** Returns the original Group with updated children */
  def withUpdatedChildren(newChildren: mutable.ArrayBuffer[Statement]): Group = {
    copy(children = newChildren)(parent)
  }

  /** Returns the original Group with updated binary properties */
  def withUpdatedBinaryProperties(newBinaryProperties: BinaryProperties): Group = {
    copy(binaryProperties = newBinaryProperties)(parent)
  }

  /** Returns the original Group with updated `isRedefined` flag */
  def withUpdatedIsRedefined(newIsRedefined: Boolean): Group = {
    copy(isRedefined = newIsRedefined)(parent)
  }

  /** Returns the original AST element with updated `isSegmentRedefine` flag */
  def withUpdatedIsSegmentRedefine(newIsSegmentRedefine: Boolean): Group = {
    copy(isSegmentRedefine = newIsSegmentRedefine)(parent)
  }

  /** Returns the original AST element with updated `isSegmentRedefine` flag */
  def withUpdatedParentSegment(newParentSegmentOpt: Option[Group]): Group = {
    copy(parentSegment = newParentSegmentOpt)(parent)
  }

}

object Group {
  val root: Group = Group(level=0, name="_ROOT_", lineNumber = -1)()
}
