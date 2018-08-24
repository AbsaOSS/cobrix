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

package za.co.absa.cobrix.cobol.parser

import org.slf4j.LoggerFactory
import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}

class Copybook(val ast: CopybookAST) extends Serializable {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getCobolSchema: CopybookAST = ast

  // ToDo This should throw an exception for variable-size records
  lazy val getRecordSize: Int = {
    val last = ast.last
    val sizeInBits = last.binaryProperties.offset + last.binaryProperties.actualSize
    if (sizeInBits % 8 == 0) sizeInBits / 8 else (sizeInBits / 8) + 1
  }

  def isRecordFixedSize: Boolean = true


  /**
    * Get the AST object of a field by name.
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique '.' is not required.
    *
    * @param fieldName A field name
    * @return An AST object of the field. Throws an IllegalStateException if not found of found multiple.
    *
    */
  @throws(classOf[IllegalArgumentException])
  def getFieldByName(fieldName: String): Statement = {

    def getFieldByNameInGroup(group: Group, fieldName: String): Seq[Statement] = {
      val groupMatch = if (group.name.equalsIgnoreCase(fieldName)) Seq(group) else Seq()
      groupMatch ++ group.children.flatMap(child => {
        child match {
          case g: Group => getFieldByNameInGroup(g, fieldName)
          case st: Primitive => if (st.name.equalsIgnoreCase(fieldName)) Seq(st) else Seq()
        }
      })
    }

    def getFieldByUniqueName(schema: CopybookAST, fieldName: String): Seq[Statement] = {
      val transformedFieldName = CopybookParser.transformIdentifier(fieldName)
      schema.flatMap(grp => getFieldByNameInGroup(grp, transformedFieldName))
    }

    def getFieldByPathInGroup(group: Group, path: Array[String]): Seq[Statement] = {
      if (path.length == 0) {
        Seq()
      } else {
        group.children.flatMap(child => {
          child match {
            case g: Group =>
              if (g.name.equalsIgnoreCase(path.head))
                getFieldByPathInGroup(g, path.drop(1))
              else Seq()
            case st: Primitive =>
              if (st.name.equalsIgnoreCase(path.head))
                Seq(st)
              else Seq()
          }
        })
      }
    }

    def pathBeginsWithRoot(ast: CopybookAST, fieldPath: Array[String]): Boolean = {
      if (ast.lengthCompare(1) == 0 && fieldPath.length>0) {
        val rootFieldName = CopybookParser.transformIdentifier(fieldPath.head)
        ast.head.name.equalsIgnoreCase(rootFieldName)
      } else {
        false
      }
    }

    def getFielByPathName(ast: CopybookAST, fieldName: String): Seq[Statement] = {
      val origPath = fieldName.split('.').map(str => CopybookParser.transformIdentifier(str))
      val path = if (!pathBeginsWithRoot(ast, origPath)) {
        ast.head.name +: origPath
      } else {
        origPath
      }
      ast.flatMap(grp =>
        if (grp.name.equalsIgnoreCase(path.head))
          getFieldByPathInGroup(grp, path.drop(1))
        else
          Seq()
      )
    }

    val schema = getCobolSchema

    val foundFields = if (fieldName.contains('.')) {
      getFielByPathName(schema, fieldName)
    } else {
      getFieldByUniqueName(schema, fieldName)
    }

    if (foundFields.isEmpty) {
      throw new IllegalStateException(s"Field '$fieldName' is not found in the copybook.")
    } else if (foundFields.lengthCompare(1) == 0) {
      foundFields.head
    } else {
      throw new IllegalStateException(s"Multiple fields with name '$fieldName' found in the copybook. Please specify the exact field using '.' " +
        s"notation.")
    }
  }

  /**
    * Get value of a field of the copybook record by the AST object of the field
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique '.' is not required.
    *
    * @param field The AST object of the field
    * @param bytes Binary encoded data of the record
    * @param startOffset An offset to the beginning of the field in the data (in bytes).
    * @return The value of the field
    *
    */
  @throws(classOf[Exception])
  def extractPrimitiveField(field: Primitive, bytes: Array[Byte], startOffset: Int = 0): Any = {
    val bits = BitVector(bytes)
    field.decodeTypeValue( field.binaryProperties.offset + startOffset*8, bits)
  }

  /**
    * Get value of a field of the copybook record by name
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique '.' is not required.
    *
    * @param fieldName A field name
    * @param bytes Binary encoded data of the record
    * @param startOffset An offset where the record starts in the data (in bytes).
    * @return The value of the field
    *
    */
  @throws(classOf[IllegalStateException])
  @throws(classOf[Exception])
  def getFieldValueByName(fieldName: String, bytes: Array[Byte], startOffset: Int = 0): Any = {
    val ast = getFieldByName(fieldName)
    ast match {
      case s: Primitive => extractPrimitiveField(s, bytes, startOffset)
      case _ => throw new IllegalStateException(s"$fieldName is not a primitive field, cannot extract it's value.")
    }
  }

  /** This routine is used for testing by generating a layout position information to compare with mainframe output */
  def generateRecordLayoutPositions(): String = {
    var fieldCounter: Int = 0

    def alignLeft(str: String, width: Int): String = {
      if (str.length >= width) {
        str
      } else {
        str + " " * (width - str.length)
      }
    }

    def alignRight(str: String, width: Int): String = {
      if (str.length >= width) {
        str
      } else {
        " " * (width - str.length) + str
      }
    }

    def generateGroupLayutPositions(group: Group, path: String = "  "): String = {
      val fieldStrings = for (field <- group.children) yield {
        fieldCounter += 1
        val isRedefines = if (field.redefines.nonEmpty) "R" else ""
        val isRedefinedByStr = if (field.isRedefined) "r" else ""
        val isArray = if (field.occurs.nonEmpty) "[]" else ""

        field match {
          case grp: Group =>
            val modifiers = s"$isRedefinedByStr$isRedefines$isArray"
            val groupStr = generateGroupLayutPositions(grp, path + "  ")
            val start = grp.binaryProperties.offset / 8 + 1
            val length = grp.binaryProperties.actualSize / 8
            val end = start + length - 1
            val namePart = alignLeft(s"$path${grp.level} ${grp.name}", 39)
            val picturePart = alignLeft(modifiers, 11)
            val fieldCounterPart = alignRight(s"$fieldCounter", 5)
            val startPart = alignRight(s"$start", 7)
            val fieldEndPart = alignRight(s"$end", 7)
            val fieldLengthPart = alignRight(s"$length", 7)
            val groupDescription = s"$namePart$picturePart$fieldCounterPart$startPart$fieldEndPart$fieldLengthPart\n"
            groupDescription + groupStr
          case s: Primitive =>
            val isDependeeStr = if (s.isDependee) "D" else ""
            val modifiers = s"$isDependeeStr$isRedefinedByStr$isRedefines$isArray"
            val start = s.binaryProperties.offset / 8 + 1
            val length = s.binaryProperties.actualSize / 8
            val end = start + length - 1
            val namePart = alignLeft(s"$path${s.level} ${s.name}", 39)
            val picturePart = alignLeft(modifiers, 11)
            val fieldCounterPart = alignRight(s"$fieldCounter", 5)
            val startPart = alignRight(s"$start", 7)
            val fieldEndPart = alignRight(s"$end", 7)
            val fieldLengthPart = alignRight(s"$length", 7)
            s"$namePart$picturePart$fieldCounterPart$startPart$fieldEndPart$fieldLengthPart"
        }
      }
      fieldStrings.mkString("\n")
    }

    val strings = for (grp <- ast) yield {
      val start = grp.binaryProperties.offset / 8 + 1
      val length = grp.binaryProperties.actualSize / 8
      val end = start + length - 1
      val groupStr = generateGroupLayutPositions(grp)
      val namePart = alignLeft(s"${grp.name}", 55)
      val fieldStartPart = alignRight(s"$start", 7)
      val fieldEndPart = alignRight(s"$end", 7)
      val fieldLengthPart = alignRight(s"$length", 7)
      s"$namePart$fieldStartPart$fieldEndPart$fieldLengthPart\n$groupStr"
    }
    val header = "-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH\n\n"
    header + strings.mkString("\n")
  }

}
