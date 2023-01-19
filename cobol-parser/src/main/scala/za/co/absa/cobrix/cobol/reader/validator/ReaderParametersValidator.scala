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

package za.co.absa.cobrix.cobol.reader.validator

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.parser.expression.NumberExprEvaluator
import za.co.absa.cobrix.cobol.reader.iterator.RecordLengthExpression
import za.co.absa.cobrix.cobol.reader.parameters.MultisegmentParameters

object ReaderParametersValidator {

  @throws(classOf[IllegalStateException])
  def getLengthField(recordLengthFieldName: Option[String], cobolSchema: Copybook): Option[Primitive] = {
    recordLengthFieldName match {
      case Some(fieldName) if !fieldName.contains('@') =>
        val field = cobolSchema.getFieldByName(fieldName)
        val astNode = field match {
          case s: Primitive =>
            if (!s.dataType.isInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral]) {
              throw new IllegalStateException(s"The record length field $fieldName must be an integral type.")
            }
            if (s.occurs.isDefined && s.occurs.get > 1) {
              throw new IllegalStateException(s"The record length field '$fieldName' cannot be an array.")
            }
            s
          case _ =>
            throw new IllegalStateException(s"The record length field $fieldName must have an primitive integral type.")
        }
        Some(astNode)
      case _ => None
    }
  }

  @throws(classOf[IllegalStateException])
  def getLengthFieldExpr(recordLengthFieldExpr: Option[String], cobolSchema: Copybook): RecordLengthExpression = {
    recordLengthFieldExpr match {
      case Some(expr) if expr.contains('@') =>
        val evaluator = new NumberExprEvaluator(expr)
        val vars = evaluator.getVariables
        val fields = vars.map { field =>
          val primitive = getLengthField(Option(field), cobolSchema)
            .getOrElse(throw new IllegalArgumentException(s"The record length expression '$expr' contains an unknown field '$field'."))
          (field, primitive)
        }
        val requiredBytesToRead = if (fields.nonEmpty) {
          fields.map { case (_, field) =>
            field.binaryProperties.offset + field.binaryProperties.actualSize
          }.max
        } else {
          0
        }
        RecordLengthExpression(expr, evaluator, fields.toMap, requiredBytesToRead)
      case _ => RecordLengthExpression("", new NumberExprEvaluator(""), Map.empty, 0)
    }
  }

  @throws(classOf[IllegalStateException])
  def getSegmentIdField(multisegment: Option[MultisegmentParameters], cobolSchema: Copybook): Option[Primitive] = {
    multisegment.flatMap(params => {
      val field = cobolSchema.getFieldByName(params.segmentIdField)
      val astNode = field match {
        case s: Primitive =>
          if (s.occurs.isDefined && s.occurs.get > 1) {
            throw new IllegalStateException(s"The segment Id field '${params.segmentIdField}' cannot be an array.")
          }
          s
        case _ =>
          throw new IllegalStateException(s"The segment Id field ${params.segmentIdField} must have a primitive type.")
      }
      Some(astNode)
    })
  }

}
