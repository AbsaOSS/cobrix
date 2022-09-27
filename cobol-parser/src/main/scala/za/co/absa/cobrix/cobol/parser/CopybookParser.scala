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
import za.co.absa.cobrix.cobol.parser.antlr.ANTLRParser
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.asttransform._
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.codepage.{CodePage, CodePageCommon}
import za.co.absa.cobrix.cobol.parser.encoding.{EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy.DebugFieldsPolicy
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.policies.{CommentPolicy, DebugFieldsPolicy, StringTrimmingPolicy}

import java.nio.charset.{Charset, StandardCharsets}
import scala.annotation.tailrec
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * The object contains generic function for the Copybook parser
  */
object CopybookParser extends Logging {

  type CopybookAST = Group

  case class StatementLine(lineNumber: Int, text: String)

  case class StatementTokens(lineNumber: Int, tokens: Array[String])

  case class CopybookLine(level: Int, name: String, lineNumber: Int, modifiers: Map[String, String])

  case class RecordBoundary(name: String, begin: Int, end: Int)


  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * This method accepts arguments that affect only structure of the output AST.
    *
    * @param copyBookContents   A string containing all lines of a copybook
    * @param dropGroupFillers   Drop GROUPs marked as fillers from the output AST
    *                           (the name of this parameter is retained for compatibility, fields won't be actually removed from
    *                           the AST unless dropFillersFromAst is set to true).
    *
    *                           When dropGroupFillers is set to true, FILLER fields will retain their names,
    *                                and 'isFiller() = true' for FILLER GROUPs.
    *
    *                           When dropGroupFillers is set to false, FILLER fields will be renamed to 'FILLER_1, FILLER_2, ...'
    *                                to retain uniqueness of names in the output schema.
    *
    * @param dropValueFillers   Drop primitive fields marked as fillers from the output AST
    *                           (the name of this parameter is retained for compatibility, fields won't be actually removed from
    *                           the AST unless dropFillersFromAst is set to true).
    *
    *                           When dropValueFillers is set to true, FILLER fields will retain their names,
    *                                and 'isFiller() = true' for FILLER primitive fields.
    *
    *                           When dropValueFillers is set to false, FILLER fields will be renamed to 'FILLER_P1, FILLER_P2, ...'
    *                                to retain uniqueness of names in the output schema.
    *
    * @param commentPolicy      Specifies a policy for comments truncation inside a copybook
    * @param dropFillersFromAst If true, fillers are going to be dropped from AST according to dropGroupFillers and dropValueFillers.
    *                           If false, fillers will remain in the AST, but still can be recognizable by 'isFiller()' method.
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseSimple(copyBookContents: String,
                  dropGroupFillers: Boolean = false,
                  dropValueFillers: Boolean = true,
                  commentPolicy: CommentPolicy = CommentPolicy(),
                  dropFillersFromAst: Boolean = false
                 ): Copybook = {
    val copybook = parse(copyBookContents = copyBookContents,
      dropGroupFillers = dropGroupFillers,
      dropValueFillers = dropValueFillers,
      commentPolicy = commentPolicy)

    if (dropFillersFromAst && (dropGroupFillers || dropValueFillers)) {
      copybook.dropFillers(dropGroupFillers, dropValueFillers)
    } else {
      copybook
    }
  }

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param dataEncoding          Encoding of the data file (either ASCII/EBCDIC). The encoding of the copybook is expected to be ASCII.
    * @param copyBookContents      A string containing all lines of a copybook
    * @param dropGroupFillers      Drop groups marked as fillers from the output AST
    * @param dropValueFillers      Drop primitive fields marked as fillers from the output AST
    * @param segmentRedefines      A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    *                              resolving segment redefines.
    * @param fieldParentMap        A segment fields parent mapping
    * @param stringTrimmingPolicy  Specifies if and how strings should be trimmed when parsed
    * @param strictSignOverpunch   If true sign overpunching is not allowed for unsigned numbers
    * @param improvedNullDetection If true, string values that contain only zero bytes (0x0) will be considered null.
    * @param commentPolicy         Specifies a policy for comments truncation inside a copybook
    * @param ebcdicCodePage        A code page for EBCDIC encoded data
    * @param asciiCharset          A charset for ASCII encoded data
    * @param isUtf16BigEndian      If true UTF-16 strings are considered big-endian.
    * @param floatingPointFormat   A format of floating-point numbers (IBM/IEEE754)
    * @param nonTerminals          A list of non-terminals that should be extracted as strings
    * @param debugFieldsPolicy     Specifies if debugging fields need to be added and what should they contain (false, hex, raw).
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parse(copyBookContents: String,
            dataEncoding: Encoding = EBCDIC,
            dropGroupFillers: Boolean = false,
            dropValueFillers: Boolean = true,
            segmentRedefines: Seq[String] = Nil,
            fieldParentMap: Map[String, String] = HashMap[String, String](),
            stringTrimmingPolicy: StringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
            commentPolicy: CommentPolicy = CommentPolicy(),
            strictSignOverpunch: Boolean = true,
            improvedNullDetection: Boolean = false,
            ebcdicCodePage: CodePage = new CodePageCommon,
            asciiCharset: Charset = StandardCharsets.US_ASCII,
            isUtf16BigEndian: Boolean = true,
            floatingPointFormat: FloatingPointFormat = FloatingPointFormat.IBM,
            nonTerminals: Seq[String] = Nil,
            occursHandlers: Map[String, Map[String, Int]] = Map(),
            debugFieldsPolicy: DebugFieldsPolicy = DebugFieldsPolicy.NoDebug): Copybook = {
    parseTree(dataEncoding,
      copyBookContents,
      dropGroupFillers,
      dropValueFillers,
      segmentRedefines,
      fieldParentMap,
      stringTrimmingPolicy,
      commentPolicy,
      strictSignOverpunch,
      improvedNullDetection,
      ebcdicCodePage,
      asciiCharset,
      isUtf16BigEndian,
      floatingPointFormat,
      nonTerminals,
      occursHandlers,
      debugFieldsPolicy)
  }

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param copyBookContents      A string containing all lines of a copybook
    * @param dropGroupFillers      Drop groups marked as fillers from the output AST
    * @param dropValueFillers      Drop primitive fields marked as fillers from the output AST
    * @param segmentRedefines      A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    * @param fieldParentMap        A segment fields parent mapping
    * @param stringTrimmingPolicy  Specifies if and how strings should be trimmed when parsed
    * @param commentPolicy         Specifies a policy for comments truncation inside a copybook
    * @param strictSignOverpunch   If true sign overpunching is not allowed for unsigned numbers
    * @param improvedNullDetection If true, string values that contain only zero bytes (0x0) will be considered null.
    * @param ebcdicCodePage        A code page for EBCDIC encoded data
    * @param asciiCharset          A charset for ASCII encoded data
    * @param isUtf16BigEndian      If true UTF-16 strings are considered big-endian.
    * @param floatingPointFormat   A format of floating-point numbers (IBM/IEEE754)
    * @param nonTerminals          A list of non-terminals that should be extracted as strings
    * @param debugFieldsPolicy     Specifies if debugging fields need to be added and what should they contain (false, hex, raw).
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseTree(copyBookContents: String,
                dropGroupFillers: Boolean = false,
                dropValueFillers: Boolean = true,
                segmentRedefines: Seq[String] = Nil,
                fieldParentMap: Map[String, String] = HashMap[String, String](),
                stringTrimmingPolicy: StringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
                commentPolicy: CommentPolicy = CommentPolicy(),
                strictSignOverpunch: Boolean = true,
                improvedNullDetection: Boolean = false,
                ebcdicCodePage: CodePage = new CodePageCommon,
                asciiCharset: Charset = StandardCharsets.US_ASCII,
                isUtf16BigEndian: Boolean = true,
                floatingPointFormat: FloatingPointFormat = FloatingPointFormat.IBM,
                nonTerminals: Seq[String] = Nil,
                occursHandlers: Map[String, Map[String, Int]] = Map(),
                debugFieldsPolicy: DebugFieldsPolicy = DebugFieldsPolicy.NoDebug): Copybook = {
    parseTree(EBCDIC,
      copyBookContents,
      dropGroupFillers,
      dropValueFillers,
      segmentRedefines,
      fieldParentMap,
      stringTrimmingPolicy,
      commentPolicy,
      strictSignOverpunch,
      improvedNullDetection,
      ebcdicCodePage,
      asciiCharset,
      isUtf16BigEndian,
      floatingPointFormat,
      nonTerminals,
      occursHandlers,
      debugFieldsPolicy)
  }

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param enc                   Encoding of the data file (either ASCII/EBCDIC). The encoding of the copybook is expected to be ASCII.
    * @param copyBookContents      A string containing all lines of a copybook
    * @param dropGroupFillers      Drop groups marked as fillers from the output AST
    * @param dropValueFillers      Drop primitive fields marked as fillers from the output AST
    * @param segmentRedefines      A list of redefined fields that correspond to various segments. This needs to be specified for automatically
    *                              resolving segment redefines.
    * @param fieldParentMap        A segment fields parent mapping
    * @param stringTrimmingPolicy  Specifies if and how strings should be trimmed when parsed
    * @param commentPolicy         Specifies a policy for comments truncation inside a copybook
    * @param improvedNullDetection If true, string values that contain only zero bytes (0x0) will be considered null.
    * @param ebcdicCodePage        A code page for EBCDIC encoded data
    * @param asciiCharset          A charset for ASCII encoded data
    * @param isUtf16BigEndian      If true UTF-16 strings are considered big-endian.
    * @param floatingPointFormat   A format of floating-point numbers (IBM/IEEE754)
    * @param nonTerminals          A list of non-terminals that should be extracted as strings
    * @param debugFieldsPolicy     Specifies if debugging fields need to be added and what should they contain (false, hex, raw).
    * @return Seq[Group] where a group is a record inside the copybook
    */
  @throws(classOf[SyntaxErrorException])
  def parseTree(enc: Encoding,
                copyBookContents: String,
                dropGroupFillers: Boolean,
                dropValueFillers: Boolean,
                segmentRedefines: Seq[String],
                fieldParentMap: Map[String, String],
                stringTrimmingPolicy: StringTrimmingPolicy,
                commentPolicy: CommentPolicy,
                strictSignOverpunch: Boolean,
                improvedNullDetection: Boolean,
                ebcdicCodePage: CodePage,
                asciiCharset: Charset,
                isUtf16BigEndian: Boolean,
                floatingPointFormat: FloatingPointFormat,
                nonTerminals: Seq[String],
                occursHandlers: Map[String, Map[String, Int]],
                debugFieldsPolicy: DebugFieldsPolicy): Copybook = {

    val schemaANTLR: CopybookAST = ANTLRParser.parse(copyBookContents, enc, stringTrimmingPolicy, commentPolicy, strictSignOverpunch, improvedNullDetection, ebcdicCodePage, asciiCharset, isUtf16BigEndian, floatingPointFormat)

    val nonTerms: Set[String] = (for (id <- nonTerminals)
      yield transformIdentifier(id)
      ).toSet

    val correctedFieldParentMap = transformIdentifierMap(fieldParentMap)
    validateFieldParentMap(correctedFieldParentMap)

    val transformers = Seq(
      // Calculate sized of fields and their positions from the beginning of a record
      BinaryPropertiesAdder(),
      // Adds virtual primitive fields for GROUPs that can be parsed as concatenation of their children.
      NonTerminalsAdder(nonTerms, enc, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, isUtf16BigEndian, floatingPointFormat, strictSignOverpunch, improvedNullDetection),
      // Sets isDependee attribute for fields in the schema which are used by other fields in DEPENDING ON clause
      DependencyMarker(occursHandlers),
      // Drops group FILLERs if necessary
      GroupFillersRemover(dropGroupFillers, dropValueFillers),
      // Renames FILLERs that will be kept in the ast
      GroupFillersRenamer(dropGroupFillers, dropValueFillers),
      // Sets isSegmentRedefine property of redefined groups
      SegmentRedefinesMarker(segmentRedefines),
      // Sets parent groups for child segment redefines.
      SegmentParentsSetter(correctedFieldParentMap),
      // Add debugging fields if debug mode is enabled.
      DebugFieldsAdder(debugFieldsPolicy)
    )

    val transformedAst = transformers.foldLeft(schemaANTLR) { (ast, transformer) =>
      transformer.transform(ast)
    }

    new Copybook(
      calculateNonFillerSizes(
        transformedAst
      )
    )
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
      (parent.name, children.toList)
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
    * For each group calculates the number of non-filler items
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with non-filler size set for each group
    */
  private def calculateNonFillerSizes(ast: CopybookAST): CopybookAST = {
    def calcGroupNonFillers(group: Group): Group = {
      val newChildren = calcNonFillerChildren(group)
      var i = 0
      var nonFillers = 0
      while (i < group.children.length) {
        if (!group.children(i).isFiller && !group.children(i).isChildSegment)
          nonFillers += 1
        i += 1
      }
      group.copy(nonFillerSize = nonFillers, children = newChildren.children)(group.parent)
    }

    def calcNonFillerChildren(group: CopybookAST): CopybookAST = {
      val newChildren = ArrayBuffer[Statement]()
      group.children.foreach {
        case grp: Group =>
          val newGrp = calcGroupNonFillers(grp)
          if (newGrp.children.nonEmpty) {
            newChildren += newGrp
          }
        case st: Primitive => newChildren += st
      }
      group.withUpdatedChildren(newChildren)
    }

    calcGroupNonFillers(ast)
  }

  /** Transforms the Cobol identifiers to be useful in Spark context. Removes characters an identifier cannot contain. */
  def transformIdentifier(identifier: String): String = {
    identifier
      .replaceAll(":", "")
      .replaceAll("-", "_")
  }

  /** Transforms all identifiers in a map to be useful in Spark context. Removes characters an identifier cannot contain. */
  def transformIdentifierMap(identifierMap: Map[String, String]): Map[String, String] = {
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
  def findCycleInAMap(m: Map[String, String]): List[String] = {
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
        findCycleHelper(k, Nil)
      })
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

    val cycle = findCycleInAMap(identifierMap)
    if (cycle.nonEmpty) {
      val listStr = cycle.mkString(", ")
      throw new IllegalStateException(s"Segments parent-child relation form a cycle: $listStr.")
    }
  }

}
