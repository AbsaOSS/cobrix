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

package za.co.absa.cobrix.spark.cobol.reader.validator

import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.spark.cobol.reader.parameters.MultisegmentParameters

object ReaderParametersValidator {

  @throws(classOf[IllegalStateException])
  def getLengthField(recordLengthFieldName: Option[String], cobolSchema: Copybook): Option[Primitive] = {
    recordLengthFieldName.flatMap(fieldName => {
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
    })
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
