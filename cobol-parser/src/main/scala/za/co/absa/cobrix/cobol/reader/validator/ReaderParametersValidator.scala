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

import scala.util.Try

object ReaderParametersValidator {

  def getEitherFieldAndExpression(fieldOrExpressionOpt: Option[String], cobolSchema: Copybook): (Option[Primitive], Option[RecordLengthExpression]) = {
    fieldOrExpressionOpt match {
      case Some(fieldOrExpression) =>
        val canBeExpression = fieldOrExpression.exists(c => "+-*/".contains(c))

        if (canBeExpression && Try(cobolSchema.getFieldByName(fieldOrExpression)).isSuccess) {
          (getLengthField(fieldOrExpression, cobolSchema), None)
        } else {
          (None, getLengthFieldExpr(fieldOrExpression, cobolSchema))
        }
      case None =>
        (None, None)
    }

  }

  @throws(classOf[IllegalStateException])
  def getLengthField(recordLengthFieldName: String, cobolSchema: Copybook): Option[Primitive] = {
    val field = cobolSchema.getFieldByName(recordLengthFieldName)

    val astNode = field match {
      case s: Primitive =>
        if (!s.dataType.isInstanceOf[za.co.absa.cobrix.cobol.parser.ast.datatype.Integral]) {
          throw new IllegalStateException(s"The record length field $recordLengthFieldName must be an integral type.")
        }
        if (s.occurs.isDefined && s.occurs.get > 1) {
          throw new IllegalStateException(s"The record length field '$recordLengthFieldName' cannot be an array.")
        }
        s
      case _            =>
        throw new IllegalStateException(s"The record length field $recordLengthFieldName must have an primitive integral type.")
    }
    Some(astNode)
  }

  @throws(classOf[IllegalStateException])
  def getLengthFieldExpr(recordLengthFieldExpr: String, cobolSchema: Copybook): Option[RecordLengthExpression] = {
    val evaluator = new NumberExprEvaluator(recordLengthFieldExpr)
    val vars = evaluator.getVariables
    val fields = vars.map { field =>
      val primitive = getLengthField(field, cobolSchema)
        .getOrElse(throw new IllegalArgumentException(s"The record length expression '$recordLengthFieldExpr' contains an unknown field '$field'."))
      (field, primitive)
    }
    val requiredBytesToRead = if (fields.nonEmpty) {
      fields.map { case (_, field) =>
        field.binaryProperties.offset + field.binaryProperties.actualSize
      }.max
    } else {
      0
    }
    Some(RecordLengthExpression(recordLengthFieldExpr, evaluator, fields.toMap, requiredBytesToRead))
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
