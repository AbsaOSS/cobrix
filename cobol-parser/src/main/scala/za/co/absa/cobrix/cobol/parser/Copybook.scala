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
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.Constants

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


class Copybook(val ast: CopybookAST) extends Serializable {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getCobolSchema: CopybookAST = ast

  lazy val getRecordSize: Int = {
    ast.binaryProperties.offset + ast.binaryProperties.actualSize
  }

  def isRecordFixedSize: Boolean = true

  /**
    * Returns all segment redefines defined in an AST.
    */
  def getAllSegmentRedefines: List[CopybookAST] = CopybookParser.getAllSegmentRedefines(ast)

  /**
    * Returns a mapping from a segment redefine field name to its children.
    */
  def getParentChildrenSegmentMap: Map[String, Seq[Group]] = CopybookParser.getParentToChildrenMap(ast)

  /**
    * Returns a root segment AST stripped of all child segment ASTs.
    */
  def getRootSegmentAST: CopybookAST = CopybookParser.getRootSegmentAST(ast)

  /**
    * Returns a a list of values of segment ids for the root segment.
    */
  def getRootSegmentIds(segmentIdRedefineMap: Map[String, String], fieldParentMap: Map[String, String]): List[String] =
    CopybookParser.getRootSegmentIds(segmentIdRedefineMap, fieldParentMap)

  /**
    * Returns true if there at least 1 parent-child relationships defined in any of segment redefines.
    */
  lazy val isHierarchical: Boolean = getAllSegmentRedefines.exists(_.parentSegment.nonEmpty)

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
      schema.children.flatMap(grp => getFieldByNameInGroup(grp.asInstanceOf[Group], transformedFieldName))
    }

    def getFieldByPathInGroup(group: Group, path: Array[String]): Seq[Statement] = {
      if (path.length == 0) {
        throw new IllegalStateException(s"'$fieldName' is a GROUP and not a primitive field. Cannot extract it's value.")
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
      val rootFieldName = CopybookParser.transformIdentifier(fieldPath.head)
      ast.children.foldLeft(false)( (b: Boolean, grp: Statement) => {
        grp.name.equalsIgnoreCase(rootFieldName)
      } )
    }

    def getFielByPathName(ast: CopybookAST, fieldName: String): Seq[Statement] = {
      val origPath = fieldName.split('.').map(str => CopybookParser.transformIdentifier(str))
      val path = if (!pathBeginsWithRoot(ast, origPath)) {
        ast.children.head.name +: origPath
      } else {
        origPath
      }
      ast.children.flatMap(grp =>
        if (grp.name.equalsIgnoreCase(path.head))
          getFieldByPathInGroup(grp.asInstanceOf[Group], path.drop(1))
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
    val slicedBytes = bytes.slice(field.binaryProperties.offset + startOffset, field.binaryProperties.offset + startOffset + field.binaryProperties.actualSize)
    field.decodeTypeValue(0, slicedBytes)
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

    def generateGroupLayoutPositions(group: Group, path: String = "  "): String = {
      val fieldStrings = for (field <- group.children) yield {
        fieldCounter += 1
        val isRedefines = if (field.redefines.nonEmpty) "R" else ""
        val isRedefinedByStr = if (field.isRedefined) "r" else ""
        val isArray = if (field.occurs.nonEmpty) "[]" else ""

        field match {
          case grp: Group =>
            val modifiers = s"$isRedefinedByStr$isRedefines$isArray"
            val groupStr = generateGroupLayoutPositions(grp, path + "  ")
            val start = grp.binaryProperties.offset + 1
            val length = grp.binaryProperties.actualSize
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
            val start = s.binaryProperties.offset + 1
            val length = s.binaryProperties.actualSize
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

    val strings = for (grp <- ast.children) yield {
      val start = grp.binaryProperties.offset + 1
      val length = grp.binaryProperties.actualSize
      val end = start + length - 1
      val groupStr = generateGroupLayoutPositions(grp.asInstanceOf[Group])
      val namePart = alignLeft(s"${grp.name}", 55)
      val fieldStartPart = alignRight(s"$start", 7)
      val fieldEndPart = alignRight(s"$end", 7)
      val fieldLengthPart = alignRight(s"$length", 7)
      s"$namePart$fieldStartPart$fieldEndPart$fieldLengthPart\n$groupStr"
    }
    val header = "-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH\n\n"
    header + strings.mkString("\n")
  }

  def dropRoot(): Copybook = {
    if (ast.children.isEmpty)
      throw new RuntimeException("Cannot drop the root of an empty copybook.")
    if (ast.children.size > 1)
      throw new RuntimeException("Cannot drop the root of a copybook with more than one root segment.")
    if (ast.children.head.asInstanceOf[Group].children.exists(_.isInstanceOf[Primitive]))
      throw new RuntimeException("All elements of the root element must be record groups.")

    val newRoot = ast.children.head.asInstanceOf[Group].copy()(None)
    new Copybook(CopybookParser.calculateBinaryProperties(newRoot))
  }

  def restrictTo(fieldName: String): Copybook = {
    val stmt = getFieldByName(fieldName)
    if (stmt.isInstanceOf[Primitive])
      throw new RuntimeException("Can only restrict the copybook to a group element.")
    val newRoot = Group.root.copy(children = mutable.ArrayBuffer(stmt))(None)
    val schema = CopybookParser.calculateBinaryProperties(newRoot)
    new Copybook(schema)
  }

  /**
   * This method traverses the AST and invokes the provided function on each primitive field
   *
   * @param f A function to invoke on each primitive field.
   */
  def visitPrimitive(f: Primitive => Unit): Unit = {
    def visitGroup(grp: Group): Unit = {
      grp.children.foreach {
        case g: Group => visitGroup(g)
        case p: Primitive => f(p)
      }
    }
    visitGroup(ast)
  }
}


object Copybook {
  def merge(copybooks: Iterable[Copybook]): Copybook = {
    if (copybooks.isEmpty)
      throw new RuntimeException("Cannot merge an empty iterable of copybooks.")

    // make sure all segments are the same level
    val rootLevels: Set[Int] = copybooks.flatMap(cb => cb.ast.children.map({
      case x: Group => x.level
      case x: Primitive => x.level
      case _ => 0
    })).toSet[Int]
    if (rootLevels.size > 1)
      throw new RuntimeException("Cannot merge copybooks with differing root levels")
    val rootLevel = rootLevels.last

    // make sure segments have different names
    val rootNames: List[String] = copybooks.flatMap(cb => cb.ast.children.map({
      case x: Group => x.name
      case x: Primitive => x.name
    })).toList
    val namesSet = rootNames.toSet
    if (namesSet.size != rootNames.size)
      throw new RuntimeException("Cannot merge copybooks with repeated segment identifiers")

    // if there are more than one segment on any copybook, they must redefine the first one
    for(cb <- copybooks) {
      if(cb.ast.children.size > 1) {
        val head = cb.ast.children.head
        if( !head.isRedefined || cb.ast.children.tail.exists(x => !x.redefines.contains(head.name)))
          throw new RuntimeException("Copybook segments must redefine top segment.")
      }
    }

    val newRoot = Group.root.copy(children = new ArrayBuffer[Statement]())(None)

    val targetName = copybooks.head.ast.children.head.name

    // every segment should redefine the first one of the head copybook
    newRoot.children += (copybooks.head.ast.children.head match {
      case x: Group => x.copy(redefines = None, isRedefined = true)(Some(newRoot))
      case x: Primitive => x.copy(redefines = None, isRedefined = true)(Some(newRoot))
    })
    newRoot.children ++= copybooks.head.ast.children.tail.map({
      case x: Group => x.copy(redefines = Option(targetName), isRedefined = false)(Some(newRoot))
      case x: Primitive => x.copy(redefines = Option(targetName), isRedefined = false)(Some(newRoot))
    }).toBuffer[Statement]

    for(cb <- copybooks.tail) {
      newRoot.children ++= cb.ast.children.map({
        case x: Group => x.copy(redefines = Option(targetName), isRedefined = false)(Some(newRoot))
        case x: Primitive => x.copy(redefines = Option(targetName), isRedefined = false)(Some(newRoot))
      }).toBuffer[Statement]
    }

    // recompute sizes
    val schema = CopybookParser.calculateBinaryProperties(newRoot)

    new Copybook(schema)
  }
}