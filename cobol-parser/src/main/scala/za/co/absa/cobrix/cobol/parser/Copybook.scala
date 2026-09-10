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

import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, COMP3, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.asttransform.{BinaryPropertiesAdder, ParentGroupSetter}
import za.co.absa.cobrix.cobol.parser.policies.VariableSizeOccursPolicy
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordExtractors
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordExtractors.canExtract
import za.co.absa.cobrix.cobol.reader.parameters.WriterParameters

import java.util.concurrent.ConcurrentHashMap
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


class Copybook(val ast: CopybookAST) extends Logging with Serializable {
  import Copybook._

  private val cachePrimitives = new ConcurrentHashMap[String, Primitive]()
  private val cacheStatements = new ConcurrentHashMap[String, Statement]()
  private[cobrix] var variableSizeOccursPolicy: VariableSizeOccursPolicy = VariableSizeOccursPolicy.MaxSize

  val isFlatCopybook: Boolean = ast.children.exists(f => f.isInstanceOf[Primitive])

  lazy val getRecordSize: Int = {
    ast.binaryProperties.offset + ast.binaryProperties.actualSize
  }

  /**
    * Returns true if there is at least 1 parent-child relationship defined in any of segment redefines.
    */
  lazy val isHierarchical: Boolean = getAllSegmentRedefines.exists(_.parentSegment.nonEmpty)

  def getCobolSchema: CopybookAST = ast

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

  /** Returns the top-level records of the copybook.  */
  def getRootRecords: scala.collection.Seq[Statement] = {
    if (isFlatCopybook) {
      scala.collection.Seq(ast)
    } else {
      ast.children
    }
  }

  /**
    * Indicates whether the copybook contains any REDEFINE rules that need to be evaluated at parsing time.
    *
    * When this flag is false, no expression variables need to be extracted from records and all fields
    * can be treated as enabled, which allows skipping the rule evaluation logic entirely.
    */
  lazy val hasRedefineRules: Boolean = {
    def hasAnyRules(group: Group): Boolean = {
      group.children.exists {
        case g: Group => g.ruleExpression.nonEmpty || hasAnyRules(g)
        case p: Primitive => p.isUsedInRules || p.ruleExpression.nonEmpty
      }
    }
    hasAnyRules(ast)
  }

  /**
    * A list of primitive fields of the copybook that are referenced by REDEFINE rules
    * (conditional expressions that determine which redefined group applies to a record).
    *
    * The value is computed on first access and cached afterwards.
    */
  lazy val fieldsUsedInRedefineRules: Seq[Primitive] = {
    val primitives = new ListBuffer[Primitive]

    def processGroup(group: Group): Unit = {
      group.children.foreach {
        case g: Group     => processGroup(g)
        case p: Primitive => if (p.isUsedInRules) primitives += p
      }
    }

    if (!hasRedefineRules) {
      Seq.empty
    } else {
      processGroup(ast)
      primitives.toList
    }
  }

  /**
    * Traverses the copybook AST over the given record and collects the values of all fields that
    * participate in expression evaluation, such as fields referenced by conditional expressions
    * ("is used in rules") and fields used as `DEPENDING ON` counters of variable size arrays.
    *
    * Fields are decoded lazily: groups and primitives that cannot be extracted for the given record,
    * as well as segment redefines that do not match the provided segment id, are skipped while the
    * offset is advanced according to their binary size.
    *
    * @param recordBytes              The raw bytes of the record to extract the variables from.
    * @param segmentIdValue           The value of the segment id of the record, if the copybook contains
    *                                 segment redefines. Redefined groups that do not allow this segment id
    *                                 are not decoded.
    * @param startOffset              The offset, in bits, at which the root record starts inside the given bytes.
    * @return A mutable map from field names to their decoded values that can be used as the variable
    *         context for expression evaluation.
    */
  def extractExpressionVariablesFromRecord(recordBytes: Array[Byte],
                                           segmentIdValue: Option[String] = None,
                                           startOffset: Int = 0): mutable.HashMap[String, Any] = {
    if (!hasRedefineRules) return mutable.HashMap.empty[String, Any]
    if (variableSizeOccursPolicy == VariableSizeOccursPolicy.MaxSize) {
      val variables = new mutable.HashMap[String, Any]()

      segmentIdValue match {
        case Some(segId) =>
          fieldsUsedInRedefineRules.foreach { f =>
            if (isPartOfSegment(f, segId)) {
              val value = Copybook.extractPrimitiveField(f, recordBytes, startOffset)
              variables += (f.name -> value)
            }
          }
        case None        =>
          fieldsUsedInRedefineRules.foreach { f =>
            val value = Copybook.extractPrimitiveField(f, recordBytes, startOffset)
            variables += (f.name -> value)
          }
      }
      return variables
    }

    val dependFields = scala.collection.mutable.HashMap.empty[String, Either[Int, String]]
    val variables = new mutable.HashMap[String, Any]()

    def skipArray(field: Statement): Int = {
      val arraySize = field.arrayMaxSize
      val actualSize = field.dependingOn match {
        case None => arraySize
        case Some(dependingOn) =>
          val dependValue: Int = dependFields.getOrElse(dependingOn, Left(arraySize)) match {
            case Left(n) => n
            case Right(s) => field.dependingOnHandlers.getOrElse(s, arraySize)
          }
          if (dependValue >= field.arrayMinSize && dependValue <= arraySize)
            dependValue
          else
            arraySize
      }

      variableSizeOccursPolicy match {
        case VariableSizeOccursPolicy.MaxSize =>
          field.binaryProperties.actualSize
        case VariableSizeOccursPolicy.ShiftRecord =>
          (field.binaryProperties.actualSize / arraySize) * actualSize
        case VariableSizeOccursPolicy.PadRecord =>
          (field.binaryProperties.actualSize / arraySize) * actualSize
      }
    }

    def processValue(field: Statement, offset: Int): Int = {
      field match {
        case grp: Group =>
          if (grp.isSegmentRedefine && segmentIdValue.nonEmpty && !grp.segmentRedefineAllowedValues.contains(segmentIdValue.get)) {
            grp.binaryProperties.actualSize
          } else {
            val extract = canExtract(grp, variables)
            if (extract) {
              processGroup(grp, offset)
            } else {
              grp.binaryProperties.actualSize
            }
          }
        case st: Primitive =>
          val extract = canExtract(st, variables)
          if (extract && (st.isUsedInRules || st.isDependee)) {
            val value = st.decodeTypeValue(offset, recordBytes)
            if (st.isUsedInRules) {
              variables += st.name -> value
            }
            if (value != null && st.isDependee) {
              val intStringVal: Either[Int, String] = value match {
                case v: Int    => Left(v)
                case v: Number => Left(v.intValue())
                case v: String => Right(v)
                case v         => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral or 'occurs_mapping' should be defined, found ${v.getClass}.")
              }
              dependFields += st.name -> intStringVal
            }
            st.binaryProperties.actualSize
          } else {
            st.binaryProperties.actualSize
          }
      }
    }

    def processGroup(group: Group, offset: Int): Int = {
      var bitOffset = offset
      var j = 0
      var i = 0
      while (i < group.children.length) {
        val field = group.children(i)
        if (field.isArray) {
          val size = skipArray(field)
          if (!field.isRedefined) {
            bitOffset += size
          }
        } else {
          val size = processValue(field, bitOffset)
          if (!field.isRedefined) {
            if (field.redefines.isDefined) {
              bitOffset += field.binaryProperties.actualSize
            } else {
              bitOffset += size
            }
          }
        }
        if (!field.isFiller) {
          j += 1
        }
        i += 1
      }
      bitOffset - offset
    }

    processGroup(ast, startOffset)
    variables
  }

  /**
    * Determines whether a given field should be processed for the current record.
    *
    * A field is considered enabled when both of the following conditions hold:
    * it belongs to the segment identified by the provided segment id value (when a segment id value
    * is available; if no segment id value is provided, the segment check is skipped and the field is
    * treated as belonging to the current segment), and it can be extracted according to the values of
    * expression variables (defined by REDEFINE rules) collected from the record.
    *
    * Use `extractExpressionVariablesFromRecord` to obtain the values of expression variables for the current record.
    *
    * @param field             A field (AST statement) of the copybook to check.
    * @param segmentIdValueOpt An optional value of the segment id of the current record.
    * @param recordVariables   A map of expression variable names to their values extracted from the current record.
    * @return true if the field is part of the current segment and can be extracted from the record.
    */
  def isFieldEnabled(field: Statement, segmentIdValueOpt: Option[String], recordVariables: mutable.HashMap[String, Any]): Boolean = {
    val isCorrectSegment = segmentIdValueOpt match {
      case Some(segmentIdValue) =>
        isPartOfSegment(field, segmentIdValue)
      case None => true
    }

    if (isCorrectSegment) {
      RecordExtractors.canExtract(field, recordVariables)
    } else {
      false
    }
  }

  /**
    * Determines whether a given field belongs to a segment identified by the specified segment id value.
    *
    * The method walks up the AST from the field's parent looking for the closest enclosing group that is
    * a segment redefine. If such a group is found, the field is considered part of the segment only when
    * the group's allowed segment id values contain the given segment id value. If the field is not
    * located inside any segment redefine (or has no parent at all), it is considered to be part of
    * every segment.
    *
    * @param field          A field (AST statement) of the copybook to check.
    * @param segmentIdValue A value of the segment id of the current record.
    * @return true if the field belongs to the segment corresponding to the given segment id value.
    */
  def isPartOfSegment(field: Statement, segmentIdValue: String): Boolean = {
    @tailrec
    def getSegmentRedefineGroup(g: Group): Option[Group] = {
      if (g.isSegmentRedefine) {
        Some(g)
      } else{
        g.parent match {
          case Some(parent) => getSegmentRedefineGroup(parent)
          case None => None
        }
      }
    }

    val startGroup = field match {
      case group: Group => Some(group)
      case _            => field.parent
    }

    startGroup match {
      case Some(p) =>
        getSegmentRedefineGroup(p) match {
          case Some(segmentRedefine) =>
            segmentRedefine.segmentRedefineAllowedValues.contains(segmentIdValue)
          case None =>
            true
        }
      case None => true
    }
  }

  /**
    * Get value of a field of the copybook record by name
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique '.' is not required.
    *
    * @param fieldName   A field name
    * @param recordBytes Binary encoded data of the record
    * @param startOffset An offset where the record starts in the data (in bytes).
    * @return The value of the field
    */
  def getFieldValueByName(fieldName: String, recordBytes: Array[Byte], startOffset: Int = 0): Any = {
    val primitive = getPrimitiveFieldByName(fieldName)

    getPrimitiveField(primitive, recordBytes, startOffset)
  }

  /**
    * Sets the value of a copybook record field specified by name.
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique, '.' is not required.
    *
    * This method modifies the record in place and does not return a value.
    *
    * @param fieldName   A field name
    * @param recordBytes Binary encoded data of the record
    * @param value       The value to set
    * @param startOffset An offset where the record starts in the data (in bytes)
    */
  def setFieldValueByName(fieldName: String, recordBytes: Array[Byte], value: Any, startOffset: Int = 0): Unit = {
    val primitive = getPrimitiveFieldByName(fieldName)

    setPrimitiveField(primitive, recordBytes, value, startOffset)
  }

  /**
    * Get the AST object of a field by name.
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique, '.' is not required.
    *
    * @param fieldName A field name
    * @return An AST object of the field. Throws an IllegalStateException if not found of found multiple.
    *
    */
  def getFieldByName(fieldName: String): Statement = {

    def getFieldByNameInGroup(group: Group, fieldName: String): Seq[Statement] = {
      val groupMatch = if (group.name.equalsIgnoreCase(fieldName)) Seq(group) else Seq()
      groupMatch ++ group.children.flatMap {
        case g: Group      => getFieldByNameInGroup(g, fieldName)
        case st: Primitive => if (st.name.equalsIgnoreCase(fieldName)) Seq(st) else Seq()
      }
    }

    def getFieldByUniqueName(schema: CopybookAST, fieldName: String): Seq[Statement] = {
      val transformedFieldName = CopybookParser.transformIdentifier(fieldName)
      getFieldByNameInGroup(schema, transformedFieldName)
    }

    def getFieldByPathInGroup(group: Group, path: Array[String]): scala.collection.Seq[Statement] = {
      if (path.length == 0) {
        throw new IllegalStateException(s"'$fieldName' is a GROUP and not a primitive field. Cannot extract its value.")
      } else {
        group.children.flatMap {
          case g: Group      =>
            if (g.name.equalsIgnoreCase(path.head))
              getFieldByPathInGroup(g, path.drop(1))
            else scala.collection.Seq.empty[Statement]
          case st: Primitive =>
            if (st.name.equalsIgnoreCase(path.head))
              Seq(st)
            else scala.collection.Seq.empty[Statement]
        }
      }
    }

    def pathBeginsWithRoot(ast: CopybookAST, fieldPath: Array[String]): Boolean = {
      val rootFieldName = CopybookParser.transformIdentifier(fieldPath.head)
      ast.children.foldLeft(false)( (b: Boolean, grp: Statement) => {
        grp.name.equalsIgnoreCase(rootFieldName)
      } )
    }

    def getFieldByPathName(ast: CopybookAST, fieldName: String): scala.collection.Seq[Statement] = {
      val origPath = fieldName.split('.').map(str => CopybookParser.transformIdentifier(str))
      val rootRecords = getRootRecords
      val path = if (!pathBeginsWithRoot(ast, origPath)) {
        rootRecords.head.name +: origPath
      } else {
        origPath
      }
      rootRecords.flatMap(grp =>
        if (grp.name.equalsIgnoreCase(path.head))
          getFieldByPathInGroup(grp.asInstanceOf[Group], path.drop(1))
        else
          scala.collection.Seq()
      )
    }

    val cachedStatement = cacheStatements.get(fieldName)

    if (cachedStatement == null) {
      val schema = getCobolSchema

      val foundFields = if (fieldName.contains('.')) {
        getFieldByPathName(schema, fieldName)
      } else {
        getFieldByUniqueName(schema, fieldName)
      }

      if (foundFields.isEmpty) {
        throw new IllegalStateException(s"Field '$fieldName' is not found in the copybook.")
      } else if (foundFields.lengthCompare(1) == 0) {
        val result = foundFields.head
        cacheStatements.put(fieldName, result)
        result
      } else {
        throw new IllegalStateException(s"Multiple fields with name '$fieldName' found in the copybook. Please specify the exact field using '.' " +
          s"notation.")
      }
    } else {
      cachedStatement
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

    def generateGroupLayoutPositions(group: Group, path: String = ""): String = {
      val fieldStrings = for (field <- group.children) yield {
        fieldCounter += 1
        val isRedefines = if (field.redefines.nonEmpty) "R" else ""
        val isRedefinedByStr = if (field.isRedefined) "r" else ""
        val isArray = if (field.occurs.nonEmpty) "[]" else ""

        field match {
          case grp: Group =>
            val fieldCounterPart = alignRight(s"$fieldCounter", 5)
            val modifiers = s"$isRedefinedByStr$isRedefines$isArray"
            val groupStr = generateGroupLayoutPositions(grp, path + "  ")
            val start = grp.binaryProperties.offset + 1
            val length = grp.binaryProperties.actualSize
            val end = start + length - 1
            val namePart = alignLeft(s"$path${grp.level} ${grp.name}", 39)
            val picturePart = alignLeft(modifiers, 11)
            val startPart = alignRight(s"$start", 7)
            val fieldEndPart = alignRight(s"$end", 7)
            val fieldLengthPart = alignRight(s"$length", 7)
            val groupDescription = s"$namePart$picturePart$fieldCounterPart$startPart$fieldEndPart$fieldLengthPart\n"
            groupDescription + groupStr
          case s: Primitive =>
            val fieldCounterPart = alignRight(s"$fieldCounter", 5)
            val isDependeeStr = if (s.isDependee) "D" else ""
            val modifiers = s"$isDependeeStr$isRedefinedByStr$isRedefines$isArray"
            val start = s.binaryProperties.offset + 1
            val length = s.binaryProperties.actualSize
            val end = start + length - 1
            val namePart = alignLeft(s"$path${s.level} ${s.name}", 39)
            val picturePart = alignLeft(modifiers, 11)
            val startPart = alignRight(s"$start", 7)
            val fieldEndPart = alignRight(s"$end", 7)
            val fieldLengthPart = alignRight(s"$length", 7)
            s"$namePart$picturePart$fieldCounterPart$startPart$fieldEndPart$fieldLengthPart"
        }
      }
      fieldStrings.mkString("\n")
    }

    val layout = generateGroupLayoutPositions(ast)
    val header = "-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH\n\n"
    header + layout
  }

  def dropRoot(): Copybook = {
    if (ast.children.isEmpty)
      throw new RuntimeException("Cannot drop the root of an empty copybook.")
    if (ast.children.size > 1)
      throw new RuntimeException("Cannot drop the root of a copybook with more than one root segment.")
    if (ast.children.head.asInstanceOf[Group].children.exists(_.isInstanceOf[Primitive]))
      throw new RuntimeException("All elements of the root element must be record groups.")

    val newRoot = ast.children.head.asInstanceOf[Group].copy()(None)
    val cpy = new Copybook(BinaryPropertiesAdder().transform(newRoot))
    cpy.setVariableSizeOccursPolicy(variableSizeOccursPolicy)
    cpy
  }

  def dropFillers(dropGroupFillers: Boolean, dropValueFillers: Boolean): Copybook = {
    def dropFillersAst(group: Group): Option[Group] = {
      if (dropGroupFillers && group.isFiller) {
        None
      } else {
        val newChildren: ArrayBuffer[Statement] = group.children.flatMap {
          case g: Group => dropFillersAst(g)
          case p: Primitive =>
            if (dropValueFillers && p.isFiller) {
              None
            } else {
              Some(p)
            }
        }
        if (newChildren.isEmpty) {
          None
        } else {
          Some(group.withUpdatedChildren(newChildren))
        }
      }
    }

    dropFillersAst(ast) match {
      case Some(newAst) =>
        val cpy = new Copybook(newAst)
        cpy.setVariableSizeOccursPolicy(variableSizeOccursPolicy)
        cpy
      case None => throw new IllegalArgumentException("Removing of fillers made the copybook empty.")
    }
  }

  def restrictTo(fieldName: String): Copybook = {
    val stmt = getFieldByName(fieldName)
    if (stmt.isInstanceOf[Primitive])
      throw new RuntimeException("Can only restrict the copybook to a group element.")
    val newRoot = Group.root.copy(children = mutable.ArrayBuffer(stmt))(None)
    val schema = new BinaryPropertiesAdder().transform(newRoot)
    val cpy = new Copybook(schema)
    cpy.setVariableSizeOccursPolicy(variableSizeOccursPolicy)
    cpy
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

  private[cobrix] def setVariableSizeOccursPolicy(variableSizeOccursPolicy: VariableSizeOccursPolicy): Unit = {
    this.variableSizeOccursPolicy = variableSizeOccursPolicy
  }

  private def getPrimitiveFieldByName(fieldName: String): Primitive = {
    val cachedPrimitive = cachePrimitives.get(fieldName)

    if (cachedPrimitive == null) {
      val ast = getFieldByName(fieldName)
      ast match {
        case s: Primitive =>
          cachePrimitives.put(fieldName, s)
          s
        case _ => throw new IllegalStateException(s"$fieldName is not a primitive field, cannot extract its value.")
      }
    } else {
      cachedPrimitive
    }
  }
}

object Copybook {
  def merge(copybooks: Seq[Copybook]): Copybook = {
    if (copybooks.isEmpty)
      throw new RuntimeException("Cannot merge an empty iterable of copybooks.")

    if (copybooks.size == 1) {
      return copybooks.head
    }

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
    val schema1 = BinaryPropertiesAdder().transform(newRoot)
    val schema = ParentGroupSetter().transform(schema1)

    val occursPolicies = copybooks.map(_.variableSizeOccursPolicy).distinct
    if (occursPolicies.size > 1) {
      throw new IllegalArgumentException(
        "Cannot merge copybooks with different variable-size OCCURS policies."
      )
    }

    val cpy = new Copybook(schema)
    cpy.setVariableSizeOccursPolicy(occursPolicies.head)
    cpy
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
  def getPrimitiveField(field: Primitive, bytes: Array[Byte], startOffset: Int = 0): Any = {
    val slicedBytes = bytes.slice(field.binaryProperties.offset + startOffset, field.binaryProperties.offset + startOffset + field.binaryProperties.actualSize)
    field.decodeTypeValue(0, slicedBytes)
  }

  /** Same as getPrimitiveField(). The original method is left for backwards compatibility. */
  def extractPrimitiveField(field: Primitive, bytes: Array[Byte], startOffset: Int = 0): Any = {
    getPrimitiveField(field, bytes, startOffset)
  }

  /**
    * Set value of a field of the copybook record by the AST object of the field
    *
    * Nested field names can contain '.' to identify the exact field.
    * If the field name is unique '.' is not required.
    *
    * @param field                    The AST object of the field
    * @param recordBytes              Binary encoded data of the record
    * @param configuredStartOffset    An offset to the beginning of the field in the data (in bytes).
    * @param fieldStartOffsetOverride If this offset is 0 or negative use the field offset is defined by the copybook.
    *                                 Otherwise, use the specified offset
    * @return The value of the field
    *
    */
  def setPrimitiveField(field: Primitive, recordBytes: Array[Byte], value: Any, configuredStartOffset: Int = 0, fieldStartOffsetOverride: Int = 0): Unit = {
    field.encode match {
      case Some(encode) =>
        val fieldBytes = encode(value)

        val startByte = if (fieldStartOffsetOverride > 0)
          fieldStartOffsetOverride
        else
          field.binaryProperties.offset + configuredStartOffset
        val endByte = if (fieldStartOffsetOverride > 0)
          fieldStartOffsetOverride + field.binaryProperties.actualSize
        else
          field.binaryProperties.offset + configuredStartOffset + field.binaryProperties.actualSize

        if (startByte < 0 || endByte > recordBytes.length) {
          throw new IllegalArgumentException(s"Cannot set value for field '${field.name}' because the field is out of bounds of the record.")
        }
        if (fieldBytes.length != field.binaryProperties.dataSize) {
          throw new IllegalArgumentException(s"Cannot set value for field '${field.name}' because the encoded value has a different size than the field size.")
        }

        System.arraycopy(fieldBytes, 0, recordBytes, startByte, fieldBytes.length)
      case None =>
        throw new IllegalStateException(s"Cannot set value for field '${field.name}' because it does not have an encoder defined.")
    }
  }

  /**
    * Sets the value of a primitive field in the copybook record, with null-aware handling.
    *
    * When the value is non-null, delegates to setPrimitiveField to encode the value directly.
    * When the value is null and the output encoding is EBCDIC, applies special null handling
    * based on the field's data type and the writer parameters configuration:
    * - AlphaNumeric fields are filled with EBCDIC space bytes (0x40) if nullStringsAsSpaces is enabled.
    * - Display numeric (Integral or Decimal without compact encoding) fields are filled with EBCDIC zero bytes (0xF0) if nullDisplayNumbersAsZeros is enabled.
    * - COMP-3 encoded numeric fields are set to zero if nullComp3NumbersAsZeros is enabled.
    *
    * For non-EBCDIC output or when no matching null-handling option is configured, null values
    * result in no modification to the record bytes.
    *
    * @param field                    The AST object of the primitive field to set.
    * @param writerParameters         The writer configuration parameters controlling null handling behavior.
    * @param recordBytes              The mutable byte array representing the binary encoded record data.
    * @param value                    The value to set for the field, or null to trigger null-aware handling.
    * @param configuredStartOffset    An offset to the beginning of the field in the data (in bytes).
    * @param fieldStartOffsetOverride If this offset is 0 or negative, the field offset defined by the copybook is used.
    *                                 Otherwise, the specified offset is used.
    * @return Unit
    */
  def setPrimitiveFieldNullAware(field: Primitive,
                                writerParameters: WriterParameters,
                                recordBytes: Array[Byte],
                                value: Any,
                                configuredStartOffset: Int = 0,
                                fieldStartOffsetOverride: Int = 0): Unit = {
    if (value != null) {
      setPrimitiveField(field, recordBytes, value, configuredStartOffset, fieldStartOffsetOverride)
    } else {
      // Special handling of nulls is supported only in EBCDIC output for now.
      if (writerParameters.isEbcdic) {
        val (offset, fieldLength) = getFieldPositionAndSize(field, configuredStartOffset, fieldStartOffsetOverride)

        field.dataType match {
          case _: AlphaNumeric if writerParameters.nullStringsAsSpaces =>
            // For string fields, nulls are treated as spaces if the option is set. Since the recordBytes array is initialized with zeroes,
            // we need to explicitly set space bytes for the field's length.
            java.util.Arrays.fill(recordBytes, offset, offset + fieldLength, 0x40.toByte)
          case i: Integral if writerParameters.nullDisplayNumbersAsZeros && i.compact.isEmpty =>
            java.util.Arrays.fill(recordBytes, offset, offset + fieldLength, 0xF0.toByte)
          case d: Decimal if writerParameters.nullDisplayNumbersAsZeros && d.compact.isEmpty =>
            java.util.Arrays.fill(recordBytes, offset, offset + fieldLength, 0xF0.toByte)
          case i: Integral if writerParameters.nullComp3NumbersAsZeros && i.compact.exists(_.isInstanceOf[COMP3]) =>
            Copybook.setPrimitiveField(field, recordBytes, 0, configuredStartOffset, fieldStartOffsetOverride)
          case d: Decimal if writerParameters.nullComp3NumbersAsZeros && d.compact.exists(_.isInstanceOf[COMP3]) =>
            Copybook.setPrimitiveField(field, recordBytes, new java.math.BigDecimal(0), configuredStartOffset, fieldStartOffsetOverride)
          case _ => // Nothing to do
        }
      }
    }
  }

  /**
    * Calculates the position (offset) and size of a field within binary record data.
    *
    * The field's offset is determined by either the fieldStartOffsetOverride (if non-zero)
    * or by adding the configuredStartOffset to the field's binary properties offset.
    * The field's size is taken directly from the field's binary properties data size.
    *
    * @param field                    The AST object of the field for which to determine position and size.
    * @param configuredStartOffset    An offset to the beginning of the field in the data (in bytes).
    * @param fieldStartOffsetOverride If this offset is non-zero, it is used as the field offset directly.
    *                                 Otherwise, the field offset is computed from configuredStartOffset
    *                                 plus the field's defined binary properties offset.
    * @return A tuple where the first element is the field's offset (position) in bytes and
    *         the second element is the field's size in bytes.
    */
  def getFieldPositionAndSize(field: Statement, configuredStartOffset: Int = 0, fieldStartOffsetOverride: Int = 0): (Int, Int) = {
    val fieldLength = field.binaryProperties.dataSize
    val offset = if (fieldStartOffsetOverride > 0) fieldStartOffsetOverride else configuredStartOffset + field.binaryProperties.offset
    (offset, fieldLength)
  }
}
