/*
 * Copyright 2018 Barclays Africa Group Limited
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

import com.typesafe.scalalogging.LazyLogging
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, CobolType, Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.encoding.{EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.NonFatal

/**
  * The object contains generic function for the Copybook parser
  */
object CopybookParser extends LazyLogging{
  type MutableCopybook = mutable.ArrayBuffer[Statement]
  type CopybookAST = Seq[Group]

  import za.co.absa.cobrix.cobol.parser.common.ReservedWords._

  case class StatementLine(lineNumber: Int, text: String)

  case class StatementTokens(lineNumber: Int, tokens: Array[String])

  case class CopybookLine(level: Int, name: String, lineNumber: Int, modifiers: Map[String, String])

  case class RecordBoundary(name: String, begin: Int, end: Int)

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseTree(copyBookContents: String): Copybook = {
    parseTree(EBCDIC(), copyBookContents)
  }

  /**
    * Tokenizes a Cobol Copybook contents and returns the AST.
    *
    * @param enc Encoding of the data file (either ASCII/EBCDIC). The encoding of the copybook is expected to be ASCII.
    * @return Seq[Group] where a group is a record inside the copybook
    */
  def parseTree(enc: Encoding, copyBookContents: String): Copybook = {

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

    def getUsageModifiers(modifiers: Map[String, String]): Map[String, String] = {
      getComactLevel(modifiers) match {
        case Some(value) => Map[String, String](COMP123 -> value.toString)
        case None => Map[String, String]()
      }
    }

    val tokens = tokenize(copyBookContents)
    val lexedLines = tokens.map(lineTokens => lex(lineTokens.tokens))

    val lines: Seq[CopybookLine] =
      tokens.zip(lexedLines)
        .map { case (lineTokens, modifiers) =>
          if (lineTokens.tokens.length < 2) {
            throw new SyntaxErrorException(lineTokens.lineNumber, "", s"Syntax error at '${lineTokens.tokens.mkString(" ").trim}'")
          }
          val nameWithoutColons = transformIdentifier(lineTokens.tokens(1))
          CopybookLine(lineTokens.tokens(0).toInt, nameWithoutColons, lineTokens.lineNumber, modifiers)
        }

    val breakpoints: Seq[RecordBoundary] = getBreakpoints(lines)

    // A forest can only have multiple items if there is a duplicate newElementLevel
    val forest: Seq[Seq[CopybookLine]] = breakpoints.map(p => lines.slice(p.begin, p.end))

    val schema: MutableCopybook = new MutableCopybook()
    forest.foreach { fields =>
      val root = Group(1,
        fields.head.name,
        fields.head.lineNumber,
        mutable.ArrayBuffer(),
        redefines = None)(None)
      val trees = fields
        .drop(1) // root already added so drop first line
        .foldLeft[Statement](root)((element, field) => {
        val keywords = field.modifiers.keys.toList
        val isLeaf = keywords.contains(PIC)
        val redefines = field.modifiers.get(REDEFINES)
        val occurs = field.modifiers.get(OCCURS).map(i => i.toInt)
        val to = field.modifiers.get(TO).map(i => i.toInt)
        val dependingOn = field.modifiers.get(DEPENDING)
        val attachLevel = getMatchingGroup(element, field.level)

        val newElement = if (isLeaf) {
          val dataType = typeAndLengthFromString(keywords, field.modifiers, attachLevel.groupUsage, field.lineNumber, field.name)(enc)
          Primitive(field.level, field.name, field.lineNumber, dataType, redefines, isRedefined = false, occurs, to, dependingOn)(None)
        }
        else {
          val groupUsage = getUsageModifiers(field.modifiers)
          Group(field.level, field.name, field.lineNumber, mutable.ArrayBuffer(), redefines, isRedefined = false, occurs, to, dependingOn, groupUsage)(None)
        }

        attachLevel.add(newElement)
      })
      schema += root
    }

    val newTrees = renameGroupFillers(markDependeeFields(calculateBinaryProperties(schema)))
    val ast: CopybookAST = newTrees.map(grp => grp.asInstanceOf[Group])
    new Copybook(ast)
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
  def calculateSchemaSizes(subSchema: MutableCopybook ): MutableCopybook  = {

    def calculateGroupSize(originalGroup: Group): Group = {
      val children: MutableCopybook = calculateSchemaSizes(originalGroup.children)
      val groupSize = (for (child <- children if child.redefines.isEmpty) yield child.binaryProperties.actualSize).sum
      val groupSizeAllOccurs = groupSize*originalGroup.arrayMaxSize
      val newBinProps = BinaryProperties(originalGroup.binaryProperties.offset, groupSize, groupSizeAllOccurs)
      originalGroup.withUpdatedChildren(children).withUpdatedBinaryProperties(newBinProps)
    }

    def calculatePrimitiveSize(originalPrimitive: Primitive): Primitive = {
      val size = originalPrimitive.getBinarySizeBits
      val sizeAllOccurs = size*originalPrimitive.arrayMaxSize
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
          newSchema(i-1) = newSchema(i-1).withUpdatedIsRedefined(newIsRedefined = true)
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
          val updatedBinProps = newSchema(i-j).binaryProperties.copy(actualSize = maxSize)
          val updatedChild = newSchema(i-j).withUpdatedBinaryProperties(updatedBinProps)
          newSchema(i-j) = updatedChild
        }
      }
    }
    newSchema
  }

  /**
    * Calculate binary offsets for a mutble Cobybook schema which is just an array of AST objects
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
    var redefinedOffset =  bitOffset
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
  def markDependeeFields(originalSchema: MutableCopybook): MutableCopybook = {
    val flatFields = new mutable.ArrayBuffer[Primitive]()
    val dependees = new mutable.HashSet[Primitive]()

    def addDependeeField(name: String): Unit = {
      val nameUpper = name.toUpperCase
      // Find all the fields that match DEPENDING ON name
      val foundFields = flatFields.filter( f => f.name.toUpperCase == nameUpper)
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
    * Rename group fillers so filed names in the scheme doesn't repeat
    * Also, remove all group fillers that doesn't have child nodes
    *
    * @param originalSchema An AST as a set of copybook records
    * @return The same AST with group fillers renamed
    */
  private def renameGroupFillers(originalSchema: MutableCopybook): MutableCopybook = {
    var lastFillerIndex = 0

    def renameSubGroupFillers(group: Group): Group = {
      val newChildren = renameFillers(group.children)
      var renamedGroup = if (group.name.toUpperCase == FILLER) {
        lastFillerIndex += 1
        group.copy(name = s"${FILLER}_$lastFillerIndex")(group.parent)
      } else group
      renamedGroup.copy(children = newChildren)(renamedGroup.parent)
    }

    def renameFillers(subSchema: MutableCopybook): MutableCopybook = {
      val newSchema = ArrayBuffer[Statement]()
      subSchema.foreach {
        case grp: Group =>
          val newGrp = renameSubGroupFillers(grp)
          if (newGrp.children.nonEmpty) {
            newSchema += newGrp
          }
        case st: Primitive => newSchema += st
      }
      newSchema
    }

    renameFillers(originalSchema)
  }

  /**
    * Get the type and length from a cobol data structure.
    *
    * @param keywords Keywords of a Copybook statement
    * @param modifiers Modifiers of a Copybook field
    * @param groupModifiers Modifiers of the group level
    * @param lineNumber Line number of the field definition
    * @param fieldName The name of the field
    * @return Cobol data type
    */
  def typeAndLengthFromString(
                               keywords: List[String],
                               modifiers: Map[String, String],
                               groupModifiers: Map[String, String],
                               lineNumber: Int,
                               fieldName: String
                             )(enc: Encoding): CobolType = {
    val compDefined = getComactLevel(modifiers)
    val compInherited = getComactLevel(groupModifiers)

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

    val pic = try {
      modifiers(PIC)
    }
    catch {
      case NonFatal(e) => throw new SyntaxErrorException(lineNumber, fieldName, "Primitive fields need to have a PIC modifier.")
    }

    val sync = keywords.contains(SYNC)
    pic match {
      case s if s.contains("X") || s.contains("A") =>
        AlphaNumeric(s.length, wordAlligned = if (sync) Some(position.Left) else None, Some(enc))
      case s if s.contains("V") || s.contains(".") =>
        CopybookParser.decimalLength(s) match {
          case (integralDigits, fractureDigits) =>
            //println(s"DECIMAL LENGTH for $s => ($integralDigits, $fractureDigits)")
            Decimal(
              fractureDigits,
              integralDigits + fractureDigits,
              s.contains("."),
              if (s.startsWith("S")) Some(position.Left) else None,
              if (sync) Some(position.Right) else None,
              comp,
              Some(enc))
        }
      case s if s.contains("9") =>
        Integral(
          precision = if (s.startsWith("S")) s.length-1 else s.length,
          signPosition = if (s.startsWith("S")) Some(position.Left) else None,
          wordAlligned = if (sync) Some(position.Right) else None,
          comp,
          Some(enc)
        )
    }
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

    linesIn.foreach( line => {
      if (line.text.contains('.')) {
        val parts = (line.text + " ").split('.')
        parts.dropRight(1).foreach( p => {
          linesOut += StatementLine(line.lineNumber, s"${partiallLine.trim} $p")
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
  def lex(tokens: Array[String]): Map[String, String] = {
    val keywordsWithModifiers = List(REDEFINES, OCCURS, TO, PIC)
    val keywordsWithoutModifiers = List(COMP, COMPUTATIONAL, BINARY)

    if (tokens.length < 3) {
      Map[String, String]()
    } else {
      var index = 2
      val mapAccumulator = mutable.Map[String, String]()
      while (index < tokens.length) {
        if (tokens(index) == PIC) {
          if (index >= tokens.length - 1) {
            throw new IllegalStateException("PIC should be followed by a pattern")
          }
          // Expand PIC, e.g. S9(5) -> S99999
          mapAccumulator += tokens(index) -> expandPic(tokens(index + 1))
          index += 1
        } else if (tokens(index) == REDEFINES) {
          // Expand REDEFINES, ensure current field redefines the consequent field
          if (index >= tokens.length - 1) {
            throw new IllegalStateException(s"Modifier ${tokens(index)} should be followed by a field name")
          }
          // Add modifiers with parameters
          mapAccumulator += tokens(index) -> transformIdentifier(tokens(index + 1))
          index += 1
        } else if (keywordsWithModifiers.contains(tokens(index))) {
          if (index >= tokens.length - 1) {
            throw new IllegalStateException(s"Modifier ${tokens(index)} should be followed by a parameter")
          }
          // Add modifiers with parameters
          mapAccumulator += tokens(index) -> tokens(index + 1)
          index += 1
        } else if (tokens(index).startsWith(COMP123) || tokens(index).startsWith(COMPUTATIONAL123)) {
          // Handle COMP-1 / COMP-2 / COMP-3
          mapAccumulator += COMP123 -> tokens(index).split('-')(1)
        } else if (tokens(index) == SYNC) {
          // Handle SYNC
          mapAccumulator += tokens(index) -> "Right"
        } else if (tokens(index) == DEPENDING) {
          // Handle DEPENDING ON
          if (index >= tokens.length - 2 || tokens(index+1) != ON) {
            throw new IllegalStateException(s"Modifier DEPENDING should be followed by ON FIELD")
          }
          mapAccumulator += tokens(index) -> transformIdentifier(tokens(index + 2))
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
              throw new IllegalStateException(s"Incorrect field size of $inputPIC. Supported size is in range from 1 to ${Constants.maxFieldLength}.")
            }
          }
        }
        else {
          repeatCount += c
        }
      }
    }
    outputCharacters.mkString
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
    val separator = if (str.contains('V')) 'V' else '.'
    val parts = str.split(separator)
    val nines1 = parts.head.count(_ == '9')
    val nines2 = if (parts.length > 1) parts.last.count(_ == '9') else 0
    (nines1, nines2)  }
}