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

import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast._
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException

import scala.collection.mutable

class BinaryPropertiesAdder extends AstTransformer {
  /**
    * Calculate binary properties based on the whole AST
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  override def transform(ast: CopybookAST): CopybookAST = {
    getSchemaWithOffsets(0, calculateSchemaSizes(ast))
  }

  /**
    * Calculate binary properties for a mutable Cobybook schema which is just an array of AST objects
    *
    * @param ast An array of AST objects
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[SyntaxErrorException])
  def calculateSchemaSizes(ast: CopybookAST): CopybookAST = {
    val newChildren: mutable.ArrayBuffer[Statement] = new mutable.ArrayBuffer[Statement]()
    val redefinedSizes = new mutable.ArrayBuffer[Int]()
    val redefinedNames = new mutable.HashSet[String]()

    // Calculate sizes of all elements of the AST array
    for ((child, i) <- ast.children.zipWithIndex) {
      child.redefines match {
        case None =>
          redefinedSizes.clear()
          redefinedNames.clear()
        case Some(redefines) =>
          if (i == 0) {
            throw new SyntaxErrorException(child.lineNumber, None, Option(child.name), s"The first field of a group cannot use REDEFINES keyword.")
          }
          if (!redefinedNames.contains(redefines.toUpperCase)) {
            throw new SyntaxErrorException(child.lineNumber, None, Option(child.name), s"The field ${child.name} redefines $redefines, which is not part if the redefined fields block.")
          }
          newChildren(i - 1) = newChildren(i - 1).withUpdatedIsRedefined(newIsRedefined = true)
      }

      val childWithSizes = child match {
        case group: Group => calculateSchemaSizes(group)
        case st: Primitive =>
          val size = st.getBinarySizeBytes
          val sizeAllOccurs = size * st.arrayMaxSize
          val binProps = BinaryProperties(st.binaryProperties.offset, size, sizeAllOccurs)
          st.withUpdatedBinaryProperties(binProps)
      }
      redefinedSizes += childWithSizes.binaryProperties.actualSize
      redefinedNames += childWithSizes.name.toUpperCase
      newChildren += childWithSizes
      if (child.redefines.nonEmpty) {
        // Calculate maximum redefine size
        val maxSize = redefinedSizes.max
        for (j <- redefinedSizes.indices) {
          val updatedBinProps = newChildren(i - j).binaryProperties.copy(actualSize = maxSize)
          val updatedChild = newChildren(i - j).withUpdatedBinaryProperties(updatedBinProps)
          newChildren(i - j) = updatedChild
        }
      }
    }

    val groupSize = (for (child <- newChildren if child.redefines.isEmpty) yield child.binaryProperties.actualSize).sum
    val groupSizeAllOccurs = groupSize * ast.arrayMaxSize
    val newBinProps = BinaryProperties(ast.binaryProperties.offset, groupSize, groupSizeAllOccurs)
    ast.withUpdatedChildren(newChildren).withUpdatedBinaryProperties(newBinProps)
  }

  /**
    * Calculate binary offsets for a mutable Cobybook schema which is just an array of AST objects
    *
    * @param ast An array of AST objects
    * @return The same AST with all offsets set for every field
    */
  def getSchemaWithOffsets(bitOffset: Int, ast: CopybookAST): CopybookAST = {
    var offset = bitOffset
    var redefinedOffset = bitOffset
    val newChildren = for (field <- ast.children) yield {
      val useOffset = if (field.redefines.isEmpty) {
        redefinedOffset = offset
        offset
      } else redefinedOffset
      val newField = field match {
        case grp: Group =>
          getSchemaWithOffsets(useOffset, grp)
        case st: Primitive =>
          val binProps = BinaryProperties(useOffset, st.binaryProperties.dataSize, st.binaryProperties.actualSize)
          st.withUpdatedBinaryProperties(binProps)
        case _ => field
      }
      if (field.redefines.isEmpty) {
        offset += newField.binaryProperties.actualSize
      }
      newField
    }
    val binProps = BinaryProperties(bitOffset, ast.binaryProperties.dataSize, ast.binaryProperties.actualSize)
    ast.withUpdatedChildren(newChildren).withUpdatedBinaryProperties(binProps)
  }
}

object BinaryPropertiesAdder {
  def apply(): BinaryPropertiesAdder = new BinaryPropertiesAdder()
}
