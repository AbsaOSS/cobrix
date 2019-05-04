/*
 * Copyright 2018-2019 ABSA Group Limited
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
import za.co.absa.cobrix.cobol.parser.antlr.ANTLRParser
import za.co.absa.cobrix.cobol.parser.ast.datatype
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Decimal, Integral, Usage}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.{Constants, ReservedWords}
import za.co.absa.cobrix.cobol.parser.decoders.{DecoderSelector, StringTrimmingPolicy}
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}
import za.co.absa.cobrix.cobol.parser.encoding.{EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.validators.CobolValidators

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.NonFatal

/**
  * The object contains generic function for the Copybook parser
  */
object CopybookParser {
  private val logger = LoggerFactory.getLogger(this.getClass)

  type MutableCopybook = mutable.ArrayBuffer[Statement]
  type CopybookAST = Group

  import za.co.absa.cobrix.cobol.parser.common.ReservedWords._

  case class StatementLine(lineNumber: Int, text: String)

  case class StatementTokens(lineNumber: Int, tokens: Array[String])

  case class CopybookLine(level: Int, name: String, lineNumber: Int, modifiers: Map[String, String])

  case class RecordBoundary(name: String, begin: Int, end: Int)

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param copyBookContents A string containing all lines of a copybook
    * @param dropGroupFillers Drop groups marked as fillers from the output AST
    * @param segmentRedefines A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseTree(copyBookContents: String,
                dropGroupFillers: Boolean = false,
                segmentRedefines: Seq[String] = Nil,
                stringTrimmingPolicy: StringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
                ebcdicCodePage: CodePage = new CodePageCommon): Copybook = {
    parseTree(EBCDIC(), copyBookContents, dropGroupFillers, segmentRedefines, stringTrimmingPolicy, ebcdicCodePage)
  }

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param enc                  Encoding of the data file (either ASCII/EBCDIC). The encoding of the copybook is expected to be ASCII.
    * @param copyBookContents     A string containing all lines of a copybook
    * @param dropGroupFillers     Drop groups marked as fillers from the output AST
    * @param segmentRedefines     A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    *                             resolving segment redefines.
    * @param stringTrimmingPolicy Specifies if and how strings should be trimmed when parsed
    * @return Seq[Group] where a group is a record inside the copybook
    */
  @throws(classOf[SyntaxErrorException])
  def parseTree(enc: Encoding,
                copyBookContents: String,
                dropGroupFillers: Boolean,
                segmentRedefines: Seq[String],
                stringTrimmingPolicy: StringTrimmingPolicy,
                ebcdicCodePage: CodePage): Copybook = {

    // Get start line index and one past last like index for each record (aka newElementLevel 1 field)
    def getBreakpoints(lines: Seq[CopybookLine]) = {
      // miniumum level
      val minLevel = lines.map(line => line.level).min
      // create a tuple of index value and root names for all the 01 levels
      val recordStartLines: Seq[(String, Int)] = lines.zipWithIndex.collect {
        case (CopybookLine(`minLevel`, name: String, _, _), i: Int) => (name, i)
      }
      val recordChangeLines: Seq[Int] = recordStartLines.drop(1).map(_._2) :+ lines.length
      val recordBeginEnd: Seq[((String, Int), Int)] = recordStartLines.zip(recordChangeLines)
      val breakpoints: Seq[RecordBoundary] = recordBeginEnd.map {
        case ((recordName, startLine), endLine) => RecordBoundary(recordName, startLine, endLine)
      }
      breakpoints
    }

    def getMatchingGroup(element: Statement, newElementLevel: Int): Group = {
      newElementLevel match {
        case level if level < 1 =>
          throw new SyntaxErrorException(element.lineNumber, element.name, s"Couldn't find matching level.")
        case level if level > element.level =>
          element match {
            case g: Group => g
            case s: Primitive =>
              throw new SyntaxErrorException(s.lineNumber, s.name, s"The field is a leaf element and cannot contain nested fields.")
            case c: Statement =>
              throw new SyntaxErrorException(c.lineNumber, c.name, s"Unknown AST object.")
          }
        case level if level <= element.level =>
          getMatchingGroup(element.up().get, level)
      }
    }

    def getUsageModifiers(modifiers: Map[String, String]): Option[Usage] = {
      getComactLevel(modifiers) match {
        case Some(1) => Some(datatype.COMP1())
        case Some(2) => Some(datatype.COMP2())
        case Some(3) => Some(datatype.COMP3())
        case Some(4) => Some(datatype.COMP())
        case _ => None
      }
    }

    val tokens = tokenize(copyBookContents)
    val lexedLines = tokens.map(lineTokens => lex(lineTokens.lineNumber, lineTokens.tokens))

    val fields: Seq[CopybookLine] =
      tokens.zip(lexedLines)
        .map { case (lineTokens, modifiers) =>
          CreateCopybookLine(lineTokens, modifiers)
        }

    val root = Group.root.copy(children = mutable.ArrayBuffer())(None)
    fields.foldLeft[Statement](root)((element, field) => {
      val comp = getComactLevel(field.modifiers).getOrElse(-1)
      val keywords = field.modifiers.keys.toList
      val isLeaf = keywords.contains(PIC) || comp == 1 || comp == 2
      val redefines = field.modifiers.get(REDEFINES)
      val occurs = field.modifiers.get(OCCURS).map(i => i.toInt)
      val to = field.modifiers.get(TO).map(i => i.toInt)
      val dependingOn = field.modifiers.get(DEPENDING)
      val attachLevel = getMatchingGroup(element, field.level)
      val isFiller = field.name.trim.toUpperCase() == ReservedWords.FILLER

      val newElement = if (isLeaf) {
        val dataType = typeAndLengthFromString(keywords, field.modifiers, attachLevel.groupUsage, field.lineNumber, field.name)(enc)
        val decode = DecoderSelector.getDecoder(dataType, stringTrimmingPolicy, ebcdicCodePage)
        Primitive(field.level, field.name, field.lineNumber, dataType, redefines, isRedefined = false, occurs, to,
          dependingOn, isFiller = isFiller, decode = decode)(None)
      }
      else {
        val groupUsage = getUsageModifiers(field.modifiers)
        Group(field.level, field.name, field.lineNumber, mutable.ArrayBuffer(), redefines, isRedefined = false,
          isSegmentRedefine = false, occurs, to, dependingOn, isFiller = isFiller, groupUsage)(None)
      }

      attachLevel.add(newElement)
    })


    val schema: MutableCopybook = ArrayBuffer(root)
    val schemaANTLR = ArrayBuffer(ANTLRParser.parse(copyBookContents, enc, stringTrimmingPolicy, ebcdicCodePage))

    val newTrees = if (dropGroupFillers) {
      calculateNonFillerSizes(markSegmentRedefines(processGroupFillers(markDependeeFields(calculateBinaryProperties(schema))), segmentRedefines))
    } else {
      calculateNonFillerSizes(markSegmentRedefines(renameGroupFillers(markDependeeFields(calculateBinaryProperties(schema))), segmentRedefines))
    }

    new Copybook(newTrees.head.asInstanceOf[Group])
  }

  private def CreateCopybookLine(lineTokens: StatementTokens, modifiers: Map[String, String]): CopybookLine = {
    if (lineTokens.tokens.length < 2) {
      throw new SyntaxErrorException(lineTokens.lineNumber, "", s"Syntax error at '${lineTokens.tokens.mkString(" ").trim}'")
    }
    val nameWithoutColons = transformIdentifier(lineTokens.tokens(1))
    val level = try {
      lineTokens.tokens(0).toInt
    } catch {
      case e: NumberFormatException =>
        throw new SyntaxErrorException(
          lineTokens.lineNumber,
          "",
          s"Unable to parse the value of LEVEL. Numeric value expected, but '${lineTokens.tokens(0)}' encountered")
    }
    CopybookLine(level, nameWithoutColons, lineTokens.lineNumber, modifiers)
  }

  /** Calculate binary properties based on the whole AST
    *
    * @param originalSchema An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  def calculateBinaryProperties(originalSchema: MutableCopybook): MutableCopybook = {
    val schema = calculateSchemaSizes(originalSchema)
    getSchemaWithOffsets(0, schema)
  }

  /**
    * Calculate binary properties for a mutble Cobybook schema which is just an array of AST objects
    *
    * @param subSchema An array of AST objects
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[SyntaxErrorException])
  def calculateSchemaSizes(subSchema: MutableCopybook): MutableCopybook = {

    def calculateGroupSize(originalGroup: Group): Group = {
      val children: MutableCopybook = calculateSchemaSizes(originalGroup.children)
      val groupSize = (for (child <- children if child.redefines.isEmpty) yield child.binaryProperties.actualSize).sum
      val groupSizeAllOccurs = groupSize * originalGroup.arrayMaxSize
      val newBinProps = BinaryProperties(originalGroup.binaryProperties.offset, groupSize, groupSizeAllOccurs)
      originalGroup.withUpdatedChildren(children).withUpdatedBinaryProperties(newBinProps)
    }

    def calculatePrimitiveSize(originalPrimitive: Primitive): Primitive = {
      val size = originalPrimitive.getBinarySizeBytes
      val sizeAllOccurs = size * originalPrimitive.arrayMaxSize
      val binProps = BinaryProperties(originalPrimitive.binaryProperties.offset, size, sizeAllOccurs)
      originalPrimitive.withUpdatedBinaryProperties(binProps)
    }

    val newSchema: MutableCopybook = new MutableCopybook()
    val redefinedSizes = new mutable.ArrayBuffer[Int]()
    val redefinedNames = new mutable.HashSet[String]()

    // Calculate sizes of all elements of the AST array
    for (i <- subSchema.indices) {
      val child = subSchema(i)

      child.redefines match {
        case None =>
          redefinedSizes.clear()
          redefinedNames.clear()
        case Some(redefines) =>
          if (i == 0) {
            throw new SyntaxErrorException(child.lineNumber, child.name, s"The first field of a group cannot use REDEFINES keyword.")
          }
          if (!redefinedNames.contains(redefines.toUpperCase)) {
            throw new SyntaxErrorException(child.lineNumber, child.name, s"The field ${child.name} redefines $redefines, which is not part if the redefined fields block.")
          }
          newSchema(i - 1) = newSchema(i - 1).withUpdatedIsRedefined(newIsRedefined = true)
      }

      val childWithSizes = child match {
        case group: Group => calculateGroupSize(group)
        case st: Primitive => calculatePrimitiveSize(st)
      }
      redefinedSizes += childWithSizes.binaryProperties.actualSize
      redefinedNames += childWithSizes.name.toUpperCase
      newSchema += childWithSizes
      if (child.redefines.nonEmpty) {
        // Calculate maximum redefine size
        val maxSize = redefinedSizes.max
        for (j <- redefinedSizes.indices) {
          val updatedBinProps = newSchema(i - j).binaryProperties.copy(actualSize = maxSize)
          val updatedChild = newSchema(i - j).withUpdatedBinaryProperties(updatedBinProps)
          newSchema(i - j) = updatedChild
        }
      }
    }
    newSchema
  }

  /**
    * Calculate binary offsets for a mutable Cobybook schema which is just an array of AST objects
    *
    * @param subSchema An array of AST objects
    * @return The same AST with all offsets set for every field
    */
  def getSchemaWithOffsets(bitOffset: Int, subSchema: MutableCopybook): MutableCopybook = {

    def getGroupWithOffsets(bitOffset: Int, group: Group): Group = {
      val newChildern = getSchemaWithOffsets(bitOffset, group.children)
      val binProps = BinaryProperties(bitOffset, group.binaryProperties.dataSize, group.binaryProperties.actualSize)
      group.withUpdatedChildren(newChildern).withUpdatedBinaryProperties(binProps)
    }

    var offset = bitOffset
    var redefinedOffset = bitOffset
    val newSchema = for (field <- subSchema) yield {
      val useOffset = if (field.redefines.isEmpty) {
        redefinedOffset = offset
        offset
      } else redefinedOffset
      val newField = field match {
        case grp: Group =>
          getGroupWithOffsets(useOffset, grp)
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
    newSchema
  }

  /**
    * Sets isDependee attribute for fields in the schema which are used by other fields in DEPENDING ON clause
    *
    * @param originalSchema An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[IllegalStateException])
  def markDependeeFields(originalSchema: MutableCopybook): MutableCopybook = {
    val flatFields = new mutable.ArrayBuffer[Primitive]()
    val dependees = new mutable.HashSet[Primitive]()

    def addDependeeField(name: String): Unit = {
      val nameUpper = name.toUpperCase
      // Find all the fields that match DEPENDING ON name
      val foundFields = flatFields.filter(f => f.name.toUpperCase == nameUpper)
      if (foundFields.isEmpty) {
        throw new IllegalStateException(s"Unable to find dependee field $nameUpper from DEPENDING ON clause.")
      }
      if (foundFields.length > 1) {
        logger.warn("Field $name used in DEPENDING ON clause has multiple instances.")
      }
      dependees ++= foundFields
    }

    def traverseDepends(subSchema: MutableCopybook): Unit = {
      for (field <- subSchema) {
        field.dependingOn.foreach(name => addDependeeField(name))
        field match {
          case grp: Group => traverseDepends(grp.children)
          case st: Primitive => flatFields += st
        }
      }
    }

    def markDependeesForGroup(group: Group): Group = {
      val newChildren = markDependees(group.children)
      var groupWithMarkedDependees = group.copy(children = newChildren)(group.parent)
      groupWithMarkedDependees
    }

    def markDependees(subSchema: MutableCopybook): MutableCopybook = {
      val newSchema = for (field <- subSchema) yield {
        val newField: Statement = field match {
          case grp: Group => markDependeesForGroup(grp)
          case primitive: Primitive =>
            val newPrimitive = if (dependees contains primitive) {
              primitive.dataType match {
                case _: Integral => true
                case dt => throw new IllegalStateException(s"Field ${primitive.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${dt.getClass}.")
              }
              primitive.withUpdatedIsDependee(newIsDependee = true)
            } else {
              primitive
            }
            newPrimitive
        }
        newField
      }
      newSchema
    }

    traverseDepends(originalSchema)
    markDependees(originalSchema)
  }

  /**
    * Sets isSegmentRedefine property of redefined groups so the row extractor be able to skip parsing segment groups
    * that do not belong to a particular segment id.
    *
    * * Each field should appear in the list only once
    * * Any such field should be a redefine or a redefined by.
    * * All segment fields should belong to the same redefine group. E.g. they should redefine each other,
    * * All segment fields should belong to the level 1 (one level down record root level)
    * * A segment redefine cannot be inside an array
    *
    * @param originalSchema   An AST as a set of copybook records
    * @param segmentRedefines The list of fields names that correspond to segment GROUPs.
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[IllegalStateException])
  def markSegmentRedefines(originalSchema: MutableCopybook, segmentRedefines: Seq[String]): MutableCopybook = {
    var foundRedefines = new mutable.HashSet[String]
    val transformedSegmentRedefines = segmentRedefines.map(transformIdentifier)
    val allowNonRedefines = segmentRedefines.lengthCompare(1) == 0
    var redefineGroupState = 0

    def ensureSegmentRedefinesAreIneGroup(currentField: String, isCurrentFieldASegmentRedefine: Boolean): Unit = {
      if (redefineGroupState == 0 && isCurrentFieldASegmentRedefine) {
        redefineGroupState = 1
      } else if (redefineGroupState == 1 && !isCurrentFieldASegmentRedefine) {
        redefineGroupState = 2
      } else if (redefineGroupState == 2 && isCurrentFieldASegmentRedefine) {
        throw new IllegalStateException(s"The '$currentField' field is specified to be a segment redefine. " +
          "However, it is not in the same group of REDEFINE fields")
      }
    }

    def isOneOfSegmentRedefines(g: Group): Boolean = {
      (allowNonRedefines || g.isRedefined || g.redefines.nonEmpty) &&
        transformedSegmentRedefines.contains(g.name)
    }

    def processGroupFields(group: Group): Group = {
      val newChildren: ArrayBuffer[Statement] = group.children.map {
        case p: Primitive =>
          ensureSegmentRedefinesAreIneGroup(p.name, isCurrentFieldASegmentRedefine = false)
          p
        case g: Group => {
          if (isOneOfSegmentRedefines(g)) {
            if (foundRedefines.contains(g.name)) {
              throw new IllegalStateException(s"Duplicate segment redefine field '${g.name}' found.")
            }
            ensureSegmentRedefinesAreIneGroup(g.name, isCurrentFieldASegmentRedefine = true)
            foundRedefines += g.name
            g.withUpdatedIsSegmentRedefine(true)
          } else {
            ensureSegmentRedefinesAreIneGroup(g.name, isCurrentFieldASegmentRedefine = false)
            g
          }
        }
      }
      group.copy(children = newChildren)(group.parent)
    }

    def processRootLevelFields(copybook: MutableCopybook): MutableCopybook = {
      copybook.map {
        case p: Primitive =>
          p
        case g: Group =>
          processGroupFields(g)
      }
    }

    def validateAllSegmentsFound(): Unit = {
      val notFound = transformedSegmentRedefines.filterNot(foundRedefines)
      if (notFound.nonEmpty) {
        val notFoundList = notFound.mkString(",")
        throw new IllegalStateException(s"The following segment redefines not found: [ $notFoundList ]. " +
          "Please check the fields exist and are redefines/redefined by.")
      }
    }

    if (segmentRedefines.isEmpty) {
      originalSchema
    } else {
      val newSchema = processRootLevelFields(originalSchema.head.asInstanceOf[Group].children)
      validateAllSegmentsFound()
      ArrayBuffer(originalSchema.head.asInstanceOf[Group].copy(children=newSchema)(None))
    }
  }

  /**
    * Rename group fillers so filed names in the scheme doesn't repeat
    * Also, remove all group fillers that doesn't have child nodes
    *
    * @param originalSchema An AST as a set of copybook records
    * @return The same AST with group fillers renamed
    */
  private def renameGroupFillers(originalSchema: MutableCopybook): MutableCopybook = {
    var lastFillerIndex = 0

    def renameSubGroupFillers(group: Group): Group = {
      val (newChildren, hasNonFillers) = renameFillers(group.children)
      var renamedGroup = if (hasNonFillers) {
        if (group.isFiller) {
          lastFillerIndex += 1
          group.copy(name = s"${
            FILLER
          }_$lastFillerIndex", children = newChildren, isFiller = false)(group.parent)
        } else {
          group.copy(children = newChildren)(group.parent)
        }
      } else {
        // All the children are fillers
        group.copy(children = newChildren, isFiller = true)(group.parent)
      }
      renamedGroup
    }

    def renameFillers(subSchema: MutableCopybook): (MutableCopybook, Boolean) = {
      val newSchema = ArrayBuffer[Statement]()
      var hasNonFillers = false
      subSchema.foreach {
        case grp: Group =>
          val newGrp = renameSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newSchema += newGrp
          }
          if (!grp.isFiller) hasNonFillers = true
        case st: Primitive =>
          newSchema += st
          if (!st.isFiller) hasNonFillers = true
      }
      (newSchema, hasNonFillers)
    }

    val (newSchema, hasNonFillers) = renameFillers(originalSchema)
    if (!hasNonFillers) {
      throw new IllegalStateException("The copybook is empty of consists only of FILLER fields.")
    }
    newSchema
  }

  /**
    * Process group fillers.
    * <ul>
    * <li>Make fillers each group that contains only filler fields.</li>
    * <li>Remove all groups that don't have child nodes.</li>
    * </ul>
    *
    * @param originalSchema An AST as a set of copybook records
    * @return The same AST with group fillers processed
    */
  private def processGroupFillers(originalSchema: MutableCopybook): MutableCopybook = {
    var lastFillerIndex = 0

    def processSubGroupFillers(group: Group): Group = {
      val (newChildren, hasNonFillers) = processFillers(group.children)
      if (hasNonFillers)
        group.copy(children = newChildren)(group.parent)
      else
        group.copy(children = newChildren, isFiller = true)(group.parent)
    }

    def processFillers(subSchema: MutableCopybook): (MutableCopybook, Boolean) = {
      val newSchema = ArrayBuffer[Statement]()
      var hasNonFillers = false
      subSchema.foreach {
        case grp: Group =>
          val newGrp = processSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newSchema += newGrp
          }
          if (!grp.isFiller) hasNonFillers = true
        case st: Primitive =>
          newSchema += st
          if (!st.isFiller) hasNonFillers = true
      }
      (newSchema, hasNonFillers)
    }

    val (newSchema, hasNonFillers) = processFillers(originalSchema)
    if (!hasNonFillers) {
      throw new IllegalStateException("The copybook is empty of consists only of FILLER fields.")
    }
    newSchema
  }

  /**
    * For each group calculates the number of non-filler items
    *
    * @param originalSchema An AST as a set of copybook records
    * @return The same AST with non-filler size set for each group
    */
  private def calculateNonFillerSizes(originalSchema: MutableCopybook): MutableCopybook = {
    var lastFillerIndex = 0

    def calcSubGroupNonFillers(group: Group): Group = {
      val newChildren = calcNonFillers(group.children)
      var i = 0
      var nonFillers = 0
      while (i < group.children.length) {
        if (!group.children(i).isFiller)
          nonFillers += 1
        i += 1
      }
      group.copy(nonFillerSize = nonFillers, children = newChildren)(group.parent)
    }

    def calcNonFillers(subSchema: MutableCopybook): MutableCopybook = {
      val newSchema = ArrayBuffer[Statement]()
      subSchema.foreach {
        case grp: Group =>
          val newGrp = calcSubGroupNonFillers(grp)
          if (newGrp.children.nonEmpty) {
            newSchema += newGrp
          }
        case st: Primitive => newSchema += st
      }
      newSchema
    }

    calcNonFillers(originalSchema)
  }

  /**
    * Get the type and length from a cobol data structure.
    *
    * @param keywords       Keywords of a Copybook statement
    * @param modifiers      Modifiers of a Copybook field
    * @param groupModifiers Modifiers of the group level
    * @param lineNumber     Line number of the field definition
    * @param fieldName      The name of the field
    * @return Cobol data type
    */
  @throws(classOf[SyntaxErrorException])
  def typeAndLengthFromString(
                               keywords: List[String],
                               modifiers: Map[String, String],
                               groupModifiers: Option[Usage],
                               lineNumber: Int,
                               fieldName: String
                             )(enc: Encoding): CobolType = {
    val compDefined = getComactLevel(modifiers)
    val compInherited: Option[Int] = groupModifiers match{
      case Some(datatype.COMP1()) => Some(1)
      case Some(datatype.COMP2()) => Some(2)
      case Some(datatype.COMP3()) => Some(3)
      case Some(datatype.COMP()) => Some(4)
      case _ => None
    }

    val comp = (compDefined, compInherited) match {
      case (None, None) => None
      case (Some(x), None) => Some(x)
      case (None, Some(y)) => Some(y)
      case (Some(x), Some(y)) =>
        if (x != y) {
          throw new SyntaxErrorException(lineNumber, fieldName, s"Field USAGE (COMP-$x) doesn't match group's USAGE (COMP-$y).")
        }
        Some(x)
    }

    val computation = comp.getOrElse(-1)
    val pic = if (computation == 1 || computation == 2) {
      // Floating point numbers (COMP-1, COMP2) do not need PIC, so just replacing it with a dummy PIC that doesn't affect the actual format
      "9(16)V9(16)"
    } else {
      try {
        modifiers(PIC)
      }
      catch {
        case NonFatal(e) => throw new SyntaxErrorException(lineNumber, fieldName, "Primitive fields need to have a PIC modifier.")
      }
    }

    val picOrigin = modifiers.getOrElse("PIC_ORIGIN", pic)

    // Trailing sign is supported implicitly by smart uncompressed number converters
    val isSignSeparate = modifiers.contains(SIGN_SEP) || pic.contains('+') || pic.contains('-')

    CobolValidators.validatePic(lineNumber, fieldName, picOrigin)

    val sync = keywords.contains(SYNC)
    val dataType = pic match {
      case s if s.contains('X') || s.contains('A') =>
        AlphaNumeric(picOrigin, s.length, wordAligned = if (sync) Some(position.Left) else None, Some(enc))
      case s if s.contains('V') || s.contains(',') =>
        CopybookParser.decimalLength(s) match {
          case (integralDigits, fractureDigits) =>
            //println(s"DECIMAL LENGTH for $s => ($integralDigits, $fractureDigits)")
            Decimal(
              picOrigin,
              fractureDigits,
              integralDigits + fractureDigits,
              s.contains(','),
              if (s.startsWith("S")) Some(position.Left) else None,
              isSignSeparate = isSignSeparate,
              if (sync) Some(position.Right) else None,
              comp,
              Some(enc))
        }
      case s if s.contains("9") =>
        Integral(
          picOrigin,
          precision = s.count(_ == '9'),
          signPosition = if (s.startsWith("S")) Some(position.Left) else None,
          isSignSeparate = isSignSeparate,
          wordAlligned = if (sync) Some(position.Right) else None,
          comp,
          Some(enc)
        )
    }
    CobolValidators.validateDataType(lineNumber, fieldName, dataType)
    dataType
  }

  /**
    * Tokenizes a copybook to lift the relevant information
    */
  def tokenize(cpyBook: String): Array[StatementTokens] = {
    val tokens = cpyBook
      // split by line breaks
      .split("\\r?\\n")
      .map(
        line =>
          line
            // ignore all columns after 72th one and
            // first 6 columns (historically for line numbers)
            .slice(6, 72)
            // remove unnecessary white space
            .replaceAll("\\s\\s+", " ")
            // the '.' sybmol inside a PIC meand an explicit decimal point
            // but also '.' is a stetement separator.
            // So here we replace '.' inside a PIC by ',' and thread ',' as an explicit decimal point instead
            .replaceAll("\\.9", ",9")
            .trim()
      )
      .zipWithIndex
      .map(v => StatementLine(v._2 + 1, v._1))
      // ignore commented lines
      .filterNot(l => l.text.startsWith("*"))

    val tokensSplit = ResplitByStatementSeparator(tokens)

    tokensSplit
      .map(l => l.copy(text = l.text.replaceAll("^\\s+", "")))
      // filter out aliases and enumerations
      .filterNot(l => l.text.startsWith("66") || l.text.startsWith("77") || l.text.startsWith("88") || l.text.trim.isEmpty)
      .map(l => StatementTokens(l.lineNumber, l.text.trim().split("\\s+")))
      .toArray
  }

  /**
    * This re-splits lines separated by a new line character to lines separated by '.' (COBOL statement separator)
    */
  private def ResplitByStatementSeparator(linesIn: Seq[StatementLine]): Seq[StatementLine] = {
    val linesOut = new ListBuffer[StatementLine]()
    var partiallLine: String = ""
    var lastLineNumber = 0

    linesIn.foreach(line => {
      if (line.text.contains('.')) {
        val parts = (line.text + " ").split('.')
        parts.dropRight(1).foreach(p => {
          linesOut += StatementLine(line.lineNumber, s"${
            partiallLine.trim
          } $p")
          partiallLine = ""
        })
        partiallLine = parts.last
      } else {
        partiallLine += " " + line.text
      }
      lastLineNumber = line.lineNumber
    })
    if (partiallLine.trim.nonEmpty) {
      linesOut += StatementLine(lastLineNumber, partiallLine)
    }
    linesOut
  }

  /** Returns compact level of a binary field */
  private def getComactLevel(modifiers: Map[String, String]): Option[Int] = {
    val keywords = modifiers.keys.toList
    val comp: Option[Int] =
      if (keywords.contains(COMP123))
        Some(modifiers.getOrElse(COMP123, "1").toInt)
      else {
        if (keywords.contains(COMP) || keywords.contains(COMPUTATIONAL) || keywords.contains(BINARY))
          Some(4)
        else
          None
      }
    comp
  }

  /** Lex the parsed tokens
    *
    * @param tokens Tokens to lex
    * @return lexed properties
    */
  @throws(classOf[SyntaxErrorException])
  def lex(lineNumber: Int, tokens: Array[String]): Map[String, String] = {
    val keywordsWithModifiers = List(REDEFINES, OCCURS, TO, PIC)
    val keywordsWithoutModifiers = List(COMP, COMPUTATIONAL, BINARY)

    if (tokens.length < 3) {
      Map[String, String]()
    } else {
      var index = 2
      val mapAccumulator = mutable.Map[String, String]()
      while (index < tokens.length) {
        if (tokens(index) == PIC || tokens(index) == PICTURE) {
          if (index >= tokens.length - 1) {
            throw new SyntaxErrorException(lineNumber, "", "PIC should be followed by a pattern")
          }
          val pic = fixPic(tokens(index + 1))
          mapAccumulator += "PIC_ORIGIN" -> pic
          // Expand PIC, e.g. S9(5) -> S99999
          mapAccumulator += PIC -> expandPic(pic)
          index += 1
        } else if (tokens(index) == REDEFINES) {
          // Expand REDEFINES, ensure current field redefines the consequent field
          if (index >= tokens.length - 1) {
            throw new SyntaxErrorException(lineNumber, "", s"Modifier ${
              tokens(index)
            } should be followed by a field name")
          }
          // Add modifiers with parameters
          mapAccumulator += tokens(index) -> transformIdentifier(tokens(index + 1))
          index += 1
        } else if (keywordsWithModifiers.contains(tokens(index))) {
          if (index >= tokens.length - 1) {
            throw new SyntaxErrorException(lineNumber, "", s"Modifier ${
              tokens(index)
            } should be followed by a parameter")
          }
          // Add modifiers with parameters
          mapAccumulator += tokens(index) -> tokens(index + 1)
          index += 1
        } else if (tokens(index).startsWith(COMP123) || tokens(index).startsWith(COMPUTATIONAL123)) {
          // Handle COMP-1 / COMP-2 / COMP-3
          mapAccumulator += COMP123 -> tokens(index).split('-')(1)
        } else if (tokens(index) == SYNC) {
          // Handle SYNC
          mapAccumulator += tokens(index) -> RIGHT
        } else if (tokens(index) == DEPENDING) {
          // Handle DEPENDING ON
          if (index >= tokens.length - 2 || tokens(index + 1) != ON) {
            throw new SyntaxErrorException(lineNumber, "", s"Modifier DEPENDING should be followed by ON FIELD")
          }
          mapAccumulator += tokens(index) -> transformIdentifier(tokens(index + 2))
          index += 2
        } else if (tokens(index) == INDEXED) {
          // Handle INDEXED BY
          if (index >= tokens.length - 2 || tokens(index + 1) != BY) {
            throw new SyntaxErrorException(lineNumber, "", s"Modifier INDEXED should be followed by BY FIELD")
          }
          mapAccumulator += tokens(index) -> transformIdentifier(tokens(index + 2))
          index += 2
        } else if (tokens(index) == SIGN) {
          // SIGN [IS] {LEADING|TRAILING} [SEPARATE] [CHARACTER]
          val except = new SyntaxErrorException(lineNumber, "", s"Modifier SIGN should be followed by [IS] {LEADING|TRAILING} [SEPARATE] [CHARACTER]")
          if (index >= tokens.length - 1) {
            throw except
          }
          if (tokens(index + 1) == IS) {
            index += 1
          }
          if (index >= tokens.length - 1) {
            throw except
          }
          if (tokens(index + 1) != LEADING && tokens(index + 1) != TRAILING) {
            throw new SyntaxErrorException(lineNumber, "", s"Modifier SIGN should be followed by either LEADING or TRAILING")
          }
          if (tokens(index + 1) == LEADING) {
            mapAccumulator += SIGN -> LEFT
          } else {
            mapAccumulator += SIGN -> RIGHT
          }
          if (index < tokens.length - 2 && tokens(index + 2) == SEPARATE) {
            mapAccumulator += SIGN_SEP -> "true"
            index += 1
          }
          if (index < tokens.length - 2 && tokens(index + 2) == CHARACTER) {
            index += 1
          }
        } else if (keywordsWithoutModifiers.contains(tokens(index))) {
          // Handle parameterless modifiers (COMP)
          mapAccumulator += tokens(index) -> ""
        }
        index += 1
      }
      mapAccumulator.toMap
    }

  }

  /**
    * Expands a PIC by replacing parenthesis with a number by the actual symbols.
    * For example: 99(3).9(2) -> 9999.99
    *
    * @param inputPIC An input PIC specification, e.g. "9(5)V9(2)"
    * @return The expanded version of a PIC value, e.g. "99999V99"
    */
  @throws(classOf[IllegalStateException])
  def expandPic(inputPIC: String): String = {
    val outputCharacters = new ArrayBuffer[Char]()
    val repeatCount = new ArrayBuffer[Char]()
    var parsingCount = false
    var lastCharacter = ' '
    for (c <- inputPIC) {
      if (!parsingCount) {
        if (c == '(') {
          parsingCount = true
        }
        else {
          outputCharacters += c
          lastCharacter = c
        }
      } else {
        if (c == ')') {
          parsingCount = false
          val num = repeatCount.mkString("").toInt - 1
          repeatCount.clear()
          if (num > 0 && num <= Constants.maxFieldLength) {
            for (i <- Range(0, num)) {
              outputCharacters += lastCharacter
            }
          } else {
            // Do nothing if num == 0
            if (num < 0 || num > Constants.maxFieldLength) {
              throw new IllegalStateException(s"Incorrect field size of $inputPIC. Supported size is in range from 1 to ${
                Constants.maxFieldLength
              }.")
            }
          }
        }
        else {
          repeatCount += c
        }
      }
    }
    val pic = outputCharacters.mkString
    // 'Z' has the same meaning as '9' from Spark data types perspective
    pic.replace('Z', '9')
  }

  /**
    * Fix PIC according to the actual copybooks being encountered.
    *
    * <ul><li>For '`02 FIELD PIC 9(5)USAGE COMP.`' The picture is '`9(5)USAGE`', but should be '`9(5)`'</li></ul>
    *
    * @param inputPIC An input PIC specification, e.g. "9(5)V9(2)"
    * @return The fixed PIC specification
    */
  def fixPic(inputPIC: String): String = {
    // Fix 'PIC 9(5)USAGE' pics
    if (inputPIC.contains('U')) {
      inputPIC.split('U').head
    } else if (inputPIC.contains('u')) {
      inputPIC.split('u').head
    } else {
      inputPIC
    }
  }

  /** Transforms the Cobol identifiers to be useful in Spark context. Removes characters an identifier cannot contain. */
  def transformIdentifier(identifier: String): String = {
    identifier
      .replaceAll(":", "")
      .replaceAll("-", "_")
  }

  /**
    * Get number of decimal digits given a PIC of a numeric field
    *
    * @param s A PIC string
    * @return A pair specifying the number of digits before and after decimal separator
    */
  def decimalLength(s: String): (Int, Int) = {
    var str = expandPic(s)
    val separator = if (str.contains('V')) 'V' else if (str.contains(',')) ',' else '.'
    val parts = str.split(separator)
    val nines1 = parts.head.count(_ == '9')
    val nines2 = if (parts.length > 1) parts.last.count(_ == '9') else 0
    (nines1, nines2)
  }
}