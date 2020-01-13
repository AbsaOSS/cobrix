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

import java.nio.charset.{Charset, StandardCharsets}

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.antlr.ANTLRParser
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.{DecoderSelector, FloatingPointFormat}
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}
import za.co.absa.cobrix.cobol.parser.encoding.{EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.policies.{CommentPolicy, StringTrimmingPolicy}

import scala.annotation.tailrec
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * The object contains generic function for the Copybook parser
  */
object CopybookParser {
  private val logger = LoggerFactory.getLogger(this.getClass)

  type CopybookAST = Group

  case class StatementLine(lineNumber: Int, text: String)

  case class StatementTokens(lineNumber: Int, tokens: Array[String])

  case class CopybookLine(level: Int, name: String, lineNumber: Int, modifiers: Map[String, String])

  case class RecordBoundary(name: String, begin: Int, end: Int)

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param copyBookContents     A string containing all lines of a copybook
    * @param dropGroupFillers     Drop groups marked as fillers from the output AST
    * @param segmentRedefines     A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    * @param fieldParentMap       A segment fields parent mapping
    * @param stringTrimmingPolicy Specifies if and how strings should be trimmed when parsed
    * @param commentPolicy        Specifies a policy for comments truncation inside a copybook
    * @param ebcdicCodePage       A code page for EBCDIC encoded data
    * @param asciiCharset         A charset for ASCII encoded data
    * @param floatingPointFormat  A format of floating-point numbers (IBM/IEEE754)
    * @param nonTerminals         A list of non-terminals that should be extracted as strings
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseTree(copyBookContents: String,
                dropGroupFillers: Boolean = false,
                segmentRedefines: Seq[String] = Nil,
                fieldParentMap: Map[String, String] = HashMap[String, String](),
                stringTrimmingPolicy: StringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
                commentPolicy: CommentPolicy = CommentPolicy(),
                ebcdicCodePage: CodePage = new CodePageCommon,
                asciiCharset: Charset = StandardCharsets.US_ASCII,
                floatingPointFormat: FloatingPointFormat = FloatingPointFormat.IBM,
                nonTerminals: Seq[String] = Nil): Copybook = {
    parseTree(EBCDIC(),
      copyBookContents,
      dropGroupFillers,
      segmentRedefines,
      fieldParentMap,
      stringTrimmingPolicy,
      commentPolicy,
      ebcdicCodePage,
      asciiCharset,
      floatingPointFormat,
      nonTerminals)
  }

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param enc                  Encoding of the data file (either ASCII/EBCDIC). The encoding of the copybook is expected to be ASCII.
    * @param copyBookContents     A string containing all lines of a copybook
    * @param dropGroupFillers     Drop groups marked as fillers from the output AST
    * @param segmentRedefines     A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    *                             resolving segment redefines.
    * @param fieldParentMap       A segment fields parent mapping
    * @param stringTrimmingPolicy Specifies if and how strings should be trimmed when parsed
    * @param commentPolicy        Specifies a policy for comments truncation inside a copybook
    * @param ebcdicCodePage       A code page for EBCDIC encoded data
    * @param asciiCharset         A charset for ASCII encoded data
    * @param floatingPointFormat  A format of floating-point numbers (IBM/IEEE754)
    * @param nonTerminals         A list of non-terminals that should be extracted as strings
    * @return Seq[Group] where a group is a record inside the copybook
    */
  @throws(classOf[SyntaxErrorException])
  def parseTree(enc: Encoding,
                copyBookContents: String,
                dropGroupFillers: Boolean,
                segmentRedefines: Seq[String],
                fieldParentMap: Map[String,String],
                stringTrimmingPolicy: StringTrimmingPolicy,
                commentPolicy: CommentPolicy,
                ebcdicCodePage: CodePage,
                asciiCharset: Charset,
                floatingPointFormat: FloatingPointFormat,
                nonTerminals: Seq[String]): Copybook = {

    val schemaANTLR: CopybookAST = ANTLRParser.parse(copyBookContents, enc, stringTrimmingPolicy, commentPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)

    val nonTerms: Set[String] = (for (id <- nonTerminals)
      yield transformIdentifier(id)
    ).toSet
    
    val correctedFieldParentMap = transformIdentifierMap(fieldParentMap)
    validateFieldParentMap(correctedFieldParentMap)

    new Copybook(
      if (dropGroupFillers) {
        calculateNonFillerSizes(setSegmentParents(markSegmentRedefines(processGroupFillers(markDependeeFields(
          addNonTerminals(calculateBinaryProperties(schemaANTLR), nonTerms, enc, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)
        )), segmentRedefines), correctedFieldParentMap))
      } else {
        calculateNonFillerSizes(setSegmentParents(markSegmentRedefines(renameGroupFillers(markDependeeFields(
          addNonTerminals(calculateBinaryProperties(schemaANTLR), nonTerms, enc, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)
        )), segmentRedefines), correctedFieldParentMap))
      }
    )
  }

  private def addNonTerminals(copybook: CopybookAST, nonTerminals: Set[String],
                              enc: Encoding,
                              stringTrimmingPolicy: StringTrimmingPolicy,
                              ebcdicCodePage: CodePage,
                              asciiCharset: Charset,
                              floatingPointFormat: FloatingPointFormat
                             ): CopybookAST = {

    def getNonTerminalName(name: String, parent: Group): String = {
      val existingNames = parent.children.map{
        case x: Primitive => x.name
        case x: Group => x.name
      }

      var modifier = 0
      var wantedName = name + Constants.nonTerminalsPostfix
      while (existingNames contains wantedName) {
        modifier += 1
        wantedName = name + Constants.nonTerminalsPostfix + modifier.toString
      }
      wantedName
    }

    val newChildren: ArrayBuffer[Statement] = new ArrayBuffer[Statement]()
    for(stmt <- copybook.children) {
      stmt match {
        case s: Primitive => newChildren.append(s)
        case g: Group => {
          if (nonTerminals contains g.name) {
            newChildren.append(
              addNonTerminals(g, nonTerminals, enc, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat).copy(isRedefined = true)(g.parent)
            )
            val sz = g.binaryProperties.actualSize
            val dataType = AlphaNumeric(s"X($sz)", sz, enc = Some(enc))
            val decode = DecoderSelector.getDecoder(dataType, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)
            val newName = getNonTerminalName(g.name, g.parent.get)
            newChildren.append(
              Primitive(
                g.level, newName, g.lineNumber,
                dataType,
                redefines = Some(g.name),
                decode = decode,
                binaryProperties = g.binaryProperties
              )(g.parent)
            )
          }
          else
            newChildren.append(
              addNonTerminals(g, nonTerminals, enc, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)
            )
        }
      }
    }
    copybook.copy(children = newChildren)(copybook.parent)
  }

  /** Calculate binary properties based on the whole AST
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  def calculateBinaryProperties(ast: CopybookAST): CopybookAST = {
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
            throw new SyntaxErrorException(child.lineNumber, child.name, s"The first field of a group cannot use REDEFINES keyword.")
          }
          if (!redefinedNames.contains(redefines.toUpperCase)) {
            throw new SyntaxErrorException(child.lineNumber, child.name, s"The field ${child.name} redefines $redefines, which is not part if the redefined fields block.")
          }
          newChildren(i - 1) = newChildren(i - 1).withUpdatedIsRedefined(newIsRedefined = true)
      }

      val childWithSizes = child match {
        case group: Group => calculateSchemaSizes(group)
        case st: Primitive => {
          val size = st.getBinarySizeBytes
          val sizeAllOccurs = size * st.arrayMaxSize
          val binProps = BinaryProperties(st.binaryProperties.offset, size, sizeAllOccurs)
          st.withUpdatedBinaryProperties(binProps)
        }
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

  /**
    * Sets isDependee attribute for fields in the schema which are used by other fields in DEPENDING ON clause
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[IllegalStateException])
  def markDependeeFields(ast: CopybookAST): CopybookAST = {
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

    def traverseDepends(group: CopybookAST): Unit = {
      for (field <- group.children) {
        field.dependingOn.foreach(name => addDependeeField(name))
        field match {
          case grp: Group => traverseDepends(grp)
          case st: Primitive => flatFields += st
        }
      }
    }

    def markDependeesForGroup(group: Group): Group = {
      val newChildren = markDependees(group)
      var groupWithMarkedDependees = group.copy(children = newChildren.children)(group.parent)
      groupWithMarkedDependees
    }

    def markDependees(group: CopybookAST): CopybookAST = {
      val newChildren = for (field <- group.children) yield {
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
      group.copy(children = newChildren)(group.parent)
    }

    traverseDepends(ast)
    markDependees(ast)
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
    * @param ast   An AST as a set of copybook records
    * @param segmentRedefines The list of fields names that correspond to segment GROUPs.
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[IllegalStateException])
  def markSegmentRedefines(ast: CopybookAST, segmentRedefines: Seq[String]): CopybookAST = {
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
      val childrenWithSegmentRedefines: ArrayBuffer[Statement] = group.children.map {
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
      group.copy(children = childrenWithSegmentRedefines)(group.parent)
    }

    def processRootLevelFields(group: CopybookAST): CopybookAST = {
      group.withUpdatedChildren(
        group.children.map {
          case p: Primitive =>
            p
          case g: Group =>
            processGroupFields(g)
        }
      )
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
      ast
    } else {
      val newSchema = processRootLevelFields(ast)
      validateAllSegmentsFound()
      ast.withUpdatedChildren(newSchema.children)
    }
  }

  /**
    * Sets parent groups for child segment redefines.
    * This relies on segment id to redefines map. The assumptions are
    *
    * * Only one segment redefine field has empty parent - the root segment.
    * * All other segment redefines should have a parent segment.
    * * isSegmentRedefine should be already set for all segment redefines.
    * * A parent of a segment redefine should be a segment redefine as well
    *
    * @param originalSchema   An AST as a set of copybook records
    * @param fieldParentMap A mapping between field names and their parents
    * @return The same AST with binary properties set for every field
    */
  @throws(classOf[IllegalStateException])
  def setSegmentParents(originalSchema: CopybookAST, fieldParentMap: Map[String,String]): CopybookAST = {
    val rootSegments = ListBuffer[String]()
    val redefinedFields = getAllSegmentRedefines(originalSchema)

    def getParentField(childName: String): Option[Group] = {
      fieldParentMap
        .get(childName)
        .map(field => {
          val parentOpt = redefinedFields.find(f => f.name == field)
          parentOpt match {
            case Some(group) => group
            case _ => throw new IllegalStateException(s"Field $field is specified to be the parent of $childName, " +
              s"but $field is not a segment redefine. Please, check if the field is specified for any of 'redefine-segment-id-map' options.")
          }
        })
    }

    def processGroupFields(group: Group): Group = {
      val childrenWithSegmentRedefines: ArrayBuffer[Statement] = group.children.map {
        case p: Primitive =>
          p
        case g: Group =>
          if (g.isSegmentRedefine) {
            val newGroup = g.withUpdatedParentSegment(getParentField(g.name))
            if (newGroup.parentSegment.isEmpty) {
              rootSegments += g.name
            }
            newGroup
          } else {
            if (fieldParentMap.contains(g.name)) {
              throw new IllegalStateException("Parent field is defined for a field that is not a segment redefine. " +
              s"Field: '${g.name}'. Please, check if the field is specified for any of 'redefine-segment-id-map' options.")
            }
            processGroupFields(g)
          }
      }
      group.copy(children = childrenWithSegmentRedefines)(group.parent)
    }

    def validateRootSegments(): Unit = {
      if (rootSegments.size > 1) {
        val rootSegmentsStr = rootSegments.mkString(", ")
        throw new IllegalStateException(s"Only one root segment is allowed. Found root segments: [ $rootSegmentsStr ]. ")
      }
      if (rootSegments.isEmpty) {
        throw new IllegalStateException(s"No root segment found in the segment parent-child map.")
      }
    }

    if (fieldParentMap.isEmpty) {
      originalSchema
    } else {
      val newSchema = processGroupFields(originalSchema)
      validateRootSegments()
      newSchema
    }
  }

  /**
    * Given an AST of a copybook returns the list of all segment redefine GROUPs
    *
    * @param schema An AST as a set of copybook records
    * @return A list of segment redefine GROUPs
    */
  def getAllSegmentRedefines(schema: CopybookAST): List[Group] = {
    val redefinedFields = ListBuffer[Group]()

    def processGroupFields(group: Group): Unit = {
      group.children.foreach {
        case _: Primitive => // Nothing to do
        case g: Group =>
          if (g.isSegmentRedefine) {
            redefinedFields += g
          }
          processGroupFields(g)
      }
    }

    processGroupFields(schema)
    redefinedFields.toList
  }

  /**
    * Given an AST of a copybook returns a map from segment redefines to their children
    *
    * @param schema An AST as a set of copybook records
    * @return A map from segment redefines to their children
    */
  def getParentToChildrenMap(schema: CopybookAST): Map[String, Seq[Group]] = {
    val redefineParents = mutable.ListBuffer[(Group, Option[Group])]()

    def generateListOfParents(group: Group): Unit = {
      group.children.foreach {
        case _: Primitive => // Nothing to do
        case g: Group =>
          if (g.isSegmentRedefine) {
            redefineParents.append((g, g.parentSegment))
          }
          generateListOfParents(g)
      }
    }

    generateListOfParents(schema)

    val redefines = redefineParents.map(_._1)
    redefines.map(parent => {
      val children = redefines.flatMap(child =>
        if (child.parentSegment.nonEmpty && child.parentSegment.get.name == parent.name) {
          List[Group](child)
        } else {
          List[Group]()
        }
      )
      (parent.name, children)
    }).toMap
  }

  /**
    * Given an AST of a copybook returns a new AST that does not contain child segments
    *
    * @param schema An AST as a set of copybook records
    * @return A list of segment redefine GROUPs
    */
  def getRootSegmentAST(schema: CopybookAST): CopybookAST = {
    val newChildren: ArrayBuffer[Statement] = schema.children.collect {
      case p: Primitive =>
        p
      case g: Group if g.parentSegment.isEmpty =>
        getRootSegmentAST(g)
    }
    schema.withUpdatedChildren(newChildren)
  }

  /**
    * Returns a a list of values of segment ids for the root segment.
    */
  def getRootSegmentIds(segmentIdRedefineMap: Map[String, String],
                        fieldParentMap: Map[String, String]): List[String] = {

    val rootSegmentFields = getRootSegmentFields(fieldParentMap)

    segmentIdRedefineMap.toList.collect {
      case (segmentId, redefine) if rootSegmentFields.contains(redefine) => segmentId
    }
  }

  /**
    * From a mapping from fields to their parents returns roots field - the ones that does not have a parent.
    */
  private def getRootSegmentFields(fieldParentMap: Map[String, String]): List[String] = {
    fieldParentMap
      .values
      .toSet
      .diff(fieldParentMap.keys.toSet)
      .toList
  }


  /**
    * Rename group fillers so filed names in the scheme doesn't repeat
    * Also, remove all group fillers that doesn't have child nodes
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with group fillers renamed
    */
  private def renameGroupFillers(ast: CopybookAST): CopybookAST = {
    var lastFillerIndex = 0

    def renameSubGroupFillers(group: Group): Group = {
      val (newChildren, hasNonFillers) = renameFillers(group)
      val renamedGroup = if (hasNonFillers) {
        if (group.isFiller) {
          lastFillerIndex += 1
          group.copy(name = s"${
            Constants.FILLER
          }_$lastFillerIndex", children = newChildren.children, isFiller = false)(group.parent)
        } else {
          group.withUpdatedChildren(newChildren.children)
        }
      } else {
        // All the children are fillers
        group.copy(children = newChildren.children, isFiller = true)(group.parent)
      }
      renamedGroup
    }

    def renameFillers(group: CopybookAST): (CopybookAST, Boolean) = {
      val newChildren = ArrayBuffer[Statement]()
      var hasNonFillers = false
      group.children.foreach {
        case grp: Group =>
          val newGrp = renameSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
          if (!grp.isFiller) hasNonFillers = true
        case st: Primitive =>
          newChildren += st
          if (!st.isFiller) hasNonFillers = true
      }
      (group.withUpdatedChildren(newChildren), hasNonFillers)
    }

    val (newSchema, hasNonFillers) = renameFillers(ast)
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
    * @param ast An AST as a set of copybook records
    * @return The same AST with group fillers processed
    */
  private def processGroupFillers(ast: CopybookAST): CopybookAST = {

    def processSubGroupFillers(group: Group): Group = {
      val (newChildren, hasNonFillers) = processFillers(group)
      if (hasNonFillers)
        group.copy(children = newChildren.children)(group.parent)
      else
        group.copy(children = newChildren.children, isFiller = true)(group.parent)
    }

    def processFillers(group: CopybookAST): (CopybookAST, Boolean) = {
      val newChildren = ArrayBuffer[Statement]()
      var hasNonFillers = false
      group.children.foreach {
        case grp: Group =>
          val newGrp = processSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
          if (!grp.isFiller) hasNonFillers = true
        case st: Primitive =>
          newChildren += st
          if (!st.isFiller) hasNonFillers = true
      }
      (group.withUpdatedChildren(newChildren), hasNonFillers)
    }

    val (newSchema, hasNonFillers) = processFillers(ast)
    if (!hasNonFillers) {
      throw new IllegalStateException("The copybook is empty of consists only of FILLER fields.")
    }
    newSchema
  }

  /**
    * For each group calculates the number of non-filler items
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with non-filler size set for each group
    */
  private def calculateNonFillerSizes(ast: CopybookAST): CopybookAST = {
    var lastFillerIndex = 0

    def calcSubGroupNonFillers(group: Group): Group = {
      val newChildren = calcNonFillers(group)
      var i = 0
      var nonFillers = 0
      while (i < group.children.length) {
        if (!group.children(i).isFiller && !group.children(i).isChildSegment)
          nonFillers += 1
        i += 1
      }
      group.copy(nonFillerSize = nonFillers, children = newChildren.children)(group.parent)
    }

    def calcNonFillers(group: CopybookAST): CopybookAST = {
      val newChildren = ArrayBuffer[Statement]()
      group.children.foreach {
        case grp: Group =>
          val newGrp = calcSubGroupNonFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
        case st: Primitive => newChildren += st
      }
      group.withUpdatedChildren(newChildren)
    }

    calcNonFillers(ast)
  }

  /** Transforms the Cobol identifiers to be useful in Spark context. Removes characters an identifier cannot contain. */
  def transformIdentifier(identifier: String): String = {
    identifier
      .replaceAll(":", "")
      .replaceAll("-", "_")
  }

  /** Transforms all identifiers in a map to be useful in Spark context. Removes characters an identifier cannot contain. */
  def transformIdentifierMap(identifierMap: Map[String,String]): Map[String,String] = {
    identifierMap.map {
      case (k, v) =>
        val newKey = transformIdentifier(k)
        val newValue = transformIdentifier(v)
        (newKey, newValue)
    }
  }

  /**
    * Finds a cycle in a parent-child relation map.
    *
    * @param m A mapping from field name to its parent field name.
    * @return A list of fields in a cycle if there is one, an empty list otherwise
    */
  def findCycleIntAMap(m: Map[String, String]): List[String] = {
    @tailrec
    def findCycleHelper(field: String, fieldsInPath: List[String]): List[String] = {
      val i = fieldsInPath.indexOf(field)
      if (i >= 0) {
        fieldsInPath.take(i + 1).reverse :+ field
      } else {
        m.get(field) match {
          case Some(parent) =>
            val path = field :: fieldsInPath
            findCycleHelper(parent, path)
          case None => Nil
        }
      }
    }

    m.view
      .map({ case (k, _) =>
        findCycleHelper(k, Nil) })
      .find(_.nonEmpty)
      .getOrElse(List[String]())
  }

  /** Transforms all identifiers in a map to be useful in Spark context. Removes characters an identifier cannot contain. */
  private def validateFieldParentMap(identifierMap: Map[String, String]): Unit = {
    identifierMap.foreach {
      case (k, v) =>
        if (k.equalsIgnoreCase(v)) {
          throw new IllegalStateException(s"A segment $k cannot be a parent of itself.")
        }
    }

    val cycle = findCycleIntAMap(identifierMap)
    if (cycle.nonEmpty) {
      val listStr = cycle.mkString(", ")
      throw new IllegalStateException(s"Segments parent-child relation form a cycle: $listStr.")
    }
  }

}
