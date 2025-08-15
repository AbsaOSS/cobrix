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

package za.co.absa.cobrix.spark.cobol.writer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema

class BasicRecordCombiner extends RecordCombiner {
  override def combine(df: DataFrame, cobolSchema: CobolSchema, readerParameters: ReaderParameters): RDD[Array[Byte]] = {
    val ast = getAst(cobolSchema)
    validateSchema(df, ast)

    val cobolFields = ast.children.map(_.asInstanceOf[Primitive])
    val sparkFields = df.schema.fields.map(_.name.toLowerCase)

    cobolFields.foreach(cobolField =>
      if (cobolField.encode.isEmpty) {
        throw new IllegalArgumentException(s"Field '${cobolField.name}' does not have an encoding defined in the copybook. 'PIC ${cobolField.dataType.originalPic}' is not yet supported.")
      }
    )

    val sparkFieldPositions = cobolFields.map { cobolField =>
      val fieldName = cobolField.name.toLowerCase
      val position = sparkFields.indexOf(fieldName)

      if (position < 0) {
        throw new IllegalArgumentException(s"Field '${cobolField.name}' from the copybook is not found in the DataFrame schema.")
      }

      position
    }

    val size = cobolSchema.getRecordSize

    df.rdd.map { row =>
      val ar = new Array[Byte](size)

      sparkFieldPositions.foreach { index =>
        val fieldStr = row.get(index)
        val cobolField = cobolFields(index)
        cobolSchema.copybook.setPrimitiveField(cobolField, ar, fieldStr, 0)
      }

      ar
    }
  }

  private def validateSchema(df: DataFrame, ast: Group): Unit = {
    val dfFields = df.schema.fields.map(_.name.toLowerCase).toSet

    val notFoundFields = ast.children.flatMap { field =>
      if (dfFields.contains(field.name.toLowerCase)) {
        None
      } else {
        Some(field.name)
      }
    }

    if (notFoundFields.nonEmpty) {
      throw new IllegalArgumentException(s"The following fields from the copybook are not found in the DataFrame: ${notFoundFields.mkString(", ")}")
    }

    val unsupportedDataTypeFields = ast.children.filter { field =>
      field.isInstanceOf[Group] ||
        (field.isInstanceOf[Primitive] && field.asInstanceOf[Primitive].occurs.isDefined) ||
        field.redefines.nonEmpty
    }

    if (unsupportedDataTypeFields.nonEmpty) {
      throw new IllegalArgumentException(s"The following fields from the copybook are not supported by the 'spark-cobol' at the moment: " +
        s"${unsupportedDataTypeFields.map(_.name).mkString(", ")}. Only primitive fields without redefines and occurs are supported.")
    }
  }

  private def getAst(cobolSchema: CobolSchema): Group = {
    val rootAst = cobolSchema.copybook.ast

    if (rootAst.children.length == 1 && rootAst.children.head.isInstanceOf[Group]) {
      rootAst.children.head.asInstanceOf[Group]
    } else {
      rootAst
    }
  }
}
