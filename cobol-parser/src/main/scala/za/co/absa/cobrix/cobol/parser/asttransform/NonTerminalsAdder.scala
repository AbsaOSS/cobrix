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
import za.co.absa.cobrix.cobol.parser.ast.datatype.AlphaNumeric
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.decoders.{DecoderSelector, EncoderSelector}
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy

import java.nio.charset.Charset
import scala.collection.mutable.ArrayBuffer

class NonTerminalsAdder(
                         nonTerminals: Set[String],
                         enc: Encoding,
                         stringTrimmingPolicy: StringTrimmingPolicy,
                         ebcdicCodePage: CodePage,
                         asciiCharset: Charset,
                         isUtf16BigEndian: Boolean,
                         floatingPointFormat: FloatingPointFormat,
                         strictSignOverpunch: Boolean,
                         improvedNullDetection: Boolean
                       ) extends AstTransformer {
  /**
    * Adds virtual virtual primitive fields for GROUPS that can be parsed as concatenation of their children.
    *
    * @param ast An AST as a set of copybook records
    * @return The same AST with non-terminals added
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    def getNonTerminalName(name: String, parent: Group): String = {
      val existingNames = parent.children.map {
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
    for (stmt <- ast.children) {
      stmt match {
        case s: Primitive => newChildren.append(s)
        case g: Group =>
          if (nonTerminals contains g.name) {
            newChildren.append(
              transform(g).copy(isRedefined = true)(g.parent)
            )
            val sz = g.binaryProperties.actualSize
            val dataType = AlphaNumeric(s"X($sz)", sz, enc = Some(enc))
            val decode = DecoderSelector.getDecoder(dataType, stringTrimmingPolicy, isDisplayAlwaysString = false, ebcdicCodePage, asciiCharset, isUtf16BigEndian, floatingPointFormat, strictSignOverpunch, improvedNullDetection)
            val encode = EncoderSelector.getEncoder(dataType, ebcdicCodePage, asciiCharset)
            val newName = getNonTerminalName(g.name, g.parent.get)
            newChildren.append(
              Primitive(
                g.level, newName, "", g.lineNumber,
                dataType,
                redefines = Some(g.name),
                decode = decode,
                encode = encode,
                binaryProperties = g.binaryProperties
              )(g.parent)
            )
          }
          else
            newChildren.append(
              transform(g)
            )
      }
    }
    ast.copy(children = newChildren)(ast.parent)
  }
}

object NonTerminalsAdder {
  def apply(
             nonTerminals: Set[String],
             enc: Encoding,
             stringTrimmingPolicy: StringTrimmingPolicy,
             ebcdicCodePage: CodePage,
             asciiCharset: Charset,
             isUtf16BigEndian: Boolean,
             floatingPointFormat: FloatingPointFormat,
             strictSignOverpunch: Boolean,
             improvedNullDetection: Boolean
           ): NonTerminalsAdder = {
    new NonTerminalsAdder(
      nonTerminals,
      enc,
      stringTrimmingPolicy,
      ebcdicCodePage,
      asciiCharset,
      isUtf16BigEndian,
      floatingPointFormat,
      strictSignOverpunch,
      improvedNullDetection)
  }
}