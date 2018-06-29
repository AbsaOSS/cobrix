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
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Statement}
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST

class Copybook(val ast: CopybookAST) extends Serializable with LazyLogging {

  def getCobolSchema: CopybookAST = ast

  // ToDo This should throw an exception for variable-size records
  lazy val getRecordSize: Int = {
    val last = ast.last
    val sizeInBits = last.binaryProperties.offset + last.binaryProperties.actualSize
    if (sizeInBits % 8 == 0) sizeInBits / 8 else (sizeInBits / 8) + 1
  }

  def isRecordFixedSize: Boolean = true

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
          case s: Statement =>
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
