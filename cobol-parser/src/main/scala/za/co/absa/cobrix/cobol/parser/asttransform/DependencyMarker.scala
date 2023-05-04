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

package za.co.absa.cobrix.cobol.parser.asttransform

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.Integral
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DependencyMarker(
                       occursHandlers: Map[String, Map[String, Int]]
                     ) extends AstTransformer {
  private val log = LoggerFactory.getLogger(this.getClass)

  /**
    * Sets isDependee attribute for fields in the schema which are used by other fields in DEPENDING ON clause
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    val flatFields = new mutable.ArrayBuffer[Primitive]()
    val dependees = new mutable.HashMap[Primitive, ListBuffer[Statement]]()

    def addDependeeField(statement: Statement, name: String): Statement = {
      val nameUpper = name.toUpperCase
      // Find all the fields that match DEPENDING ON name
      val foundFields = flatFields.filter(f => f.name.toUpperCase == nameUpper)
      if (foundFields.isEmpty) {
        throw new IllegalStateException(s"Unable to find dependee field $nameUpper from DEPENDING ON clause.")
      }
      if (foundFields.length > 1) {
        log.warn("Field $name used in DEPENDING ON clause has multiple instances.")
      }

      val updatedStatement = occursHandlers.get(statement.name) match {
        case Some(mapping) => statement.withUpdatedDependingOnHandlers(mapping)
        case _ => statement
      }

      dependees.get(foundFields.head) match {
        case Some(list) => list += updatedStatement
        case _ => dependees += (foundFields.head -> ListBuffer(updatedStatement))
      }
      updatedStatement
    }

    def traverseDepends(group: CopybookAST): CopybookAST = {

      group.withUpdatedChildren(
        for (field <- group.children) yield {
          val updatedField = field.dependingOn match {
            case Some(depName) => addDependeeField(field, depName)
            case None => field
          }
          val stmt: Statement = updatedField match {
            case grp: Group => traverseDepends(grp)
            case st: Primitive => {
              flatFields += st
              st
            }
          }
          stmt
        }
      )
    }

    def markDependeesForGroup(group: Group, occursHandlers: Map[String, Map[String, Int]]): Group = {
      val newChildren = markDependees(group, occursHandlers)
      var groupWithMarkedDependees = group.copy(children = newChildren.children)(group.parent)
      groupWithMarkedDependees
    }

    def markDependees(group: CopybookAST, occursHandlers: Map[String, Map[String, Int]]): CopybookAST = {
      val newChildren = for (field <- group.children) yield {
        val newField: Statement = field match {
          case grp: Group => markDependeesForGroup(grp, occursHandlers)
          case primitive: Primitive =>
            val newPrimitive = if (dependees contains primitive) {
              primitive.dataType match {
                case _: Integral => true
                case dt =>
                  for (stmt <- dependees(primitive)) {
                    if (stmt.dependingOnHandlers.isEmpty)
                      throw new IllegalStateException(s"Field ${primitive.name} is a DEPENDING ON field of an OCCURS, should be integral or 'occurs_mapping' should be defined, found ${dt.getClass}.")
                  }
              }
              primitive.withUpdatedIsDependee(newIsDependee = true)
            } else {
              primitive
            }
            newPrimitive
        }
        newField
      }
      group.copy(children = newChildren)(group.parent)
    }

    markDependees(
      traverseDepends(ast),
      occursHandlers
    )
  }
}

object DependencyMarker {
  def apply(occursHandlers: Map[String, Map[String, Int]]): DependencyMarker = new DependencyMarker(occursHandlers)
}
