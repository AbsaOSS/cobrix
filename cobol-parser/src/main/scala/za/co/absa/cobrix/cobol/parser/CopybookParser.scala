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

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * The object contains generic function for the Copybook parser
  */
object CopybookParser {
  private val logger = LoggerFactory.getLogger(this.getClass)

  type MutableCopybook = mutable.ArrayBuffer[Statement]
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
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseTree(copyBookContents: String,
                dropGroupFillers: Boolean = false,
                segmentRedefines: Seq[String] = Nil,
                fieldParentMap: Map[String, String] = HashMap[String, String](),
                stringTrimmingPolicy: StringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
                commentPolicy: CommentPolicy = CommentPolicy(),
                ebcdicCodePage: CodePage = new CodePageCommon,
                floatingPointFormat: FloatingPointFormat = FloatingPointFormat.IBM,
                nonTerminals: Seq[String] = Nil): Copybook = {
    parseTree(EBCDIC(), copyBookContents, dropGroupFillers, segmentRedefines, fieldParentMap, stringTrimmingPolicy, commentPolicy, ebcdicCodePage, floatingPointFormat, nonTerminals)
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
                floatingPointFormat: FloatingPointFormat,
                nonTerminals: Seq[String]): Copybook = {

    val schemaANTLR: MutableCopybook = ArrayBuffer(ANTLRParser.parse(copyBookContents, enc, stringTrimmingPolicy, commentPolicy, ebcdicCodePage, floatingPointFormat))

    val nonTerms: Set[String] = (for (id <- nonTerminals)
      yield transformIdentifier(id)
    ).toSet

    val newTrees = if (dropGroupFillers) {
      calculateNonFillerSizes(setSegmentParents(markSegmentRedefines(processGroupFillers(markDependeeFields(
        addNonTerminals(calculateBinaryProperties(schemaANTLR), nonTerms, enc, stringTrimmingPolicy, ebcdicCodePage, floatingPointFormat)
      )), segmentRedefines), fieldParentMap))
    } else {
      calculateNonFillerSizes(setSegmentParents(markSegmentRedefines(renameGroupFillers(markDependeeFields(
        addNonTerminals(calculateBinaryProperties(schemaANTLR), nonTerms, enc, stringTrimmingPolicy, ebcdicCodePage, floatingPointFormat)
      )), segmentRedefines), fieldParentMap))
    }

    new Copybook(newTrees.head.asInstanceOf[Group])
  }

  private def addNonTerminals(copybook: MutableCopybook, nonTerminals: Set[String],
                              enc: Encoding,
                              stringTrimmingPolicy: StringTrimmingPolicy,
                              ebcdicCodePage: CodePage,
                              floatingPointFormat: FloatingPointFormat
                             ): MutableCopybook = {

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

    val newCopybook: MutableCopybook = new ArrayBuffer()
    for(stmt <- copybook) {
      stmt match {
        case s: Primitive => newCopybook.append(s)
        case g: Group => {
          if (nonTerminals contains g.name) {
            newCopybook.append(
              g.copy(
                children = addNonTerminals(g.children, nonTerminals, enc, stringTrimmingPolicy, ebcdicCodePage, floatingPointFormat),
                isRedefined = true
              )(g.parent)
            )
            val sz = g.binaryProperties.actualSize
            val dataType = AlphaNumeric(s"X($sz)", sz, enc = Some(enc))
            val decode = DecoderSelector.getDecoder(dataType, stringTrimmingPolicy, ebcdicCodePage, floatingPointFormat)
            val newName = getNonTerminalName(g.name, g.parent.get)
            newCopybook.append(
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
            newCopybook.append(
              g.copy(children = addNonTerminals(g.children, nonTerminals, enc, stringTrimmingPolicy, ebcdicCodePage, floatingPointFormat))(g.parent)
            )
        }
      }
    }
    newCopybook
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
  def setSegmentParents(originalSchema: MutableCopybook, fieldParentMap: Map[String,String]): MutableCopybook = {
    val rootSegments = ListBuffer[String]()
    val redefinedFields = getAllSegmentRedefines(originalSchema.head.asInstanceOf[Group])

    def getParentField(childName: String): Option[Group] = {
      fieldParentMap
        .get(childName)
        .map(field => {
          val parentOpt = redefinedFields.find(_.name == field)
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
        case group: Group =>
          if (group.isSegmentRedefine) {
            val newGroup = group.withUpdatedParentSegment(getParentField(group.name))
            if (group.parentSegment.isEmpty) {
              rootSegments += group.name
            }
            newGroup
          } else {
            if (fieldParentMap.contains(group.name)) {
              throw new IllegalStateException("Parent field is defined for a field that is not a segment redefine. " +
              s"Field: '${group.name}'. Please, check if the field is specified for any of 'redefine-segment-id-map' options.")
            }
            group
          }
      }
      group.copy(children = childrenWithSegmentRedefines)(group.parent)
    }

    def processRootLevelFields(copybook: MutableCopybook): MutableCopybook = {
      copybook.map {
        case p: Primitive =>
          p
        case g: Group =>
          processGroupFields(g)
      }
    }

    def validateRootSegments(): Unit = {
      if (rootSegments.size > 1) {
        val rootSegmentsStr = rootSegments.mkString(", ")
        throw new IllegalStateException(s"Only one root segment is allowed. Found root segments: [ $rootSegmentsStr ]. ")
      }
    }

    if (fieldParentMap.isEmpty) {
      originalSchema
    } else {

      val newSchema = processRootLevelFields(originalSchema.head.asInstanceOf[Group].children)
      ArrayBuffer(originalSchema.head.asInstanceOf[Group].copy(children = newSchema)(None))
    }
  }

  /**
    * Given an AST of a copybook returns the list of all segment redefine GROUPs
    *
    * @param schema An AST as a set of copybook records
    * @return A list of segment redefine GROUPs
    */
  private def getAllSegmentRedefines(schema: CopybookAST): List[Group] = {
    val redefinedFields = ListBuffer[Group]()

    def processGroupFields(group: Group): Unit = {
      group.children.foreach {
        case p: Primitive => // Nothing to do
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
            Constants.FILLER
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

  /** Transforms the Cobol identifiers to be useful in Spark context. Removes characters an identifier cannot contain. */
  def transformIdentifier(identifier: String): String = {
    identifier
      .replaceAll(":", "")
      .replaceAll("-", "_")
  }

}
