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
import za.co.absa.cobrix.cobol.parser.decoders.StringDecoders
import za.co.absa.cobrix.cobol.parser.decoders.StringDecoders.KeepAll
import za.co.absa.cobrix.cobol.parser.encoding._
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy
import za.co.absa.cobrix.cobol.parser.policies.DebugFieldsPolicy.DebugFieldsPolicy

import scala.collection.mutable.ArrayBuffer

/**
  * @param debugFieldsPolicy Specifies if debugging fields need to be added and what should they contain (false, hex, raw).
  */
class DebugFieldsAdder(debugFieldsPolicy: DebugFieldsPolicy) extends AstTransformer {
  /**
    * Add debugging fields if debug mode is enabled.
    *
    * @param ast               An AST as a set of copybook records
    * @return The same AST with debugging fields added
    */
  final override def transform(ast: CopybookAST): CopybookAST = {
    def getDebugField(field: Primitive): Primitive = {
      val debugEncoding = debugFieldsPolicy match {
        case DebugFieldsPolicy.HexValue => HEX
        case DebugFieldsPolicy.RawValue => RAW
        case DebugFieldsPolicy.StringValue => ASCII
        case _ => throw new IllegalStateException(s"Unexpected debug fields policy: $debugFieldsPolicy.")
      }

      val debugDecoder = debugFieldsPolicy match {
        case DebugFieldsPolicy.HexValue => StringDecoders.decodeHex _
        case DebugFieldsPolicy.RawValue => StringDecoders.decodeRaw _
        case DebugFieldsPolicy.StringValue => (a: Array[Byte]) => new String(a)
        case _ => throw new IllegalStateException(s"Unexpected debug fields policy: $debugFieldsPolicy.")
      }

      val size = debugFieldsPolicy match {
        case DebugFieldsPolicy.HexValue => field.binaryProperties.dataSize * 2
        case DebugFieldsPolicy.RawValue => field.binaryProperties.dataSize
        case DebugFieldsPolicy.StringValue => field.binaryProperties.dataSize
        case _ => throw new IllegalStateException(s"Unexpected debug fields policy: $debugFieldsPolicy.")
      }

      val debugFieldName = field.name + "_debug"
      val debugDataType = AlphaNumeric(s"X($size)", size, None, Some(debugEncoding), None)

      val debugField = field.copy(name = debugFieldName,
        dataType = debugDataType,
        redefines = Some(field.name),
        isDependee = false,
        decode = debugDecoder)(parent = field.parent)

      debugField
    }

    def processGroup(group: Group): Group = {
      val newChildren = ArrayBuffer[Statement]()
      group.children.foreach {
        case grp: Group =>
          val newGrp = processGroup(grp)
          newChildren += newGrp
        case st: Primitive =>
          newChildren += st.withUpdatedIsRedefined(newIsRedefined = true)
          newChildren += getDebugField(st)
      }
      group.withUpdatedChildren(newChildren)
    }

    if (debugFieldsPolicy != DebugFieldsPolicy.NoDebug) {
      processGroup(ast)
    } else {
      ast
    }
  }
}

object DebugFieldsAdder {
  def apply(debugFieldsPolicy: DebugFieldsPolicy): DebugFieldsAdder = new DebugFieldsAdder(debugFieldsPolicy)
}
