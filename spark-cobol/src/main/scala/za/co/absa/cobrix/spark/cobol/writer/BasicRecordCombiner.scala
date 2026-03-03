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
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.datatype.{Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema

class BasicRecordCombiner extends RecordCombiner {

  import BasicRecordCombiner._

  override def combine(df: DataFrame, cobolSchema: CobolSchema, readerParameters: ReaderParameters): RDD[Array[Byte]] = {
    val ast = getAst(cobolSchema)
    val copybookFields = ast.children.filter {
      case f if f.redefines.nonEmpty => false
      case p: Primitive              => !p.isFiller
      case g: Group                  => !g.isFiller
      case _                         => true
    }

    validateSchema(df, copybookFields.toSeq)

    val cobolFields = copybookFields.map(_.asInstanceOf[Primitive])
    val sparkFields = df.schema.fields.map(_.name.toLowerCase)

    cobolFields.foreach(cobolField =>
      if (cobolField.encode.isEmpty) {
        val fieldDefinition = getFieldDefinition(cobolField)
        throw new IllegalArgumentException(s"Field '${cobolField.name}' does not have an encoding defined in the copybook. " +
          s"'PIC $fieldDefinition' is not yet supported.")
      }
    )

    val sparkFieldPositions = cobolFields.zipWithIndex.map { case (cobolField, idx) =>
      val fieldName = cobolField.name.toLowerCase
      val position = sparkFields.indexOf(fieldName)

      if (position < 0) {
        throw new IllegalArgumentException(s"Field '${cobolField.name}' from the copybook is not found in the DataFrame schema.")
      }

      (idx, position)
    }

    val hasRdw = readerParameters.recordFormat == RecordFormat.VariableLength
    val isRdwBigEndian = readerParameters.isRdwBigEndian
    val adjustment1 = if (readerParameters.isRdwPartRecLength) 4 else 0
    val adjustment2 = readerParameters.rdwAdjustment

    val size = if (hasRdw) {
      cobolSchema.getRecordSize + 4
    } else {
      cobolSchema.getRecordSize
    }

    val startOffset = if (hasRdw) 4 else 0

    val recordLengthLong = cobolSchema.getRecordSize.toLong + adjustment1.toLong + adjustment2.toLong
    if (recordLengthLong < 0) {
      throw new IllegalArgumentException(
        s"Invalid RDW length $recordLengthLong. Check 'is_rdw_part_of_record_length' and 'rdw_adjustment'."
      )
    }
    if (isRdwBigEndian && recordLengthLong > 0xFFFFL) {
      throw new IllegalArgumentException(
        s"RDW length $recordLengthLong exceeds 65535 and cannot be encoded in big-endian mode."
      )
    }
    val recordLength = recordLengthLong.toInt

    df.rdd.map { row =>
      val ar = new Array[Byte](size)

      if (hasRdw) {
        if (isRdwBigEndian) {
          ar(0) = ((recordLength >> 8) & 0xFF).toByte
          ar(1) = (recordLength & 0xFF).toByte
          // The last two bytes are reserved and defined by IBM as binary zeros on all platforms.
          ar(2) = 0
          ar(3) = 0
        } else {
          ar(0) = (recordLength & 0xFF).toByte
          ar(1) = ((recordLength >> 8) & 0xFF).toByte
          // This is non-standard. But so are little-endian RDW headers.
          // As an advantage, it has no effect for small records but adds support for big records (> 64KB).
          ar(2) = ((recordLength >> 16) & 0xFF).toByte
          ar(3) = ((recordLength >> 24) & 0xFF).toByte
        }
      }

      sparkFieldPositions.foreach { case (cobolIdx, sparkIdx) =>
        if (!row.isNullAt(sparkIdx)) {
          val fieldStr = row.get(sparkIdx)
          val cobolField = cobolFields(cobolIdx)
          Copybook.setPrimitiveField(cobolField, ar, fieldStr, startOffset)
        }
      }

      ar
    }
  }

  private def validateSchema(df: DataFrame, copybookFields: Seq[Statement]): Unit = {
    val dfFields = df.schema.fields.map(_.name.toLowerCase).toSet

    val notFoundFields = copybookFields.flatMap { field =>
      if (dfFields.contains(field.name.toLowerCase)) {
        None
      } else {
        Some(field.name)
      }
    }

    if (notFoundFields.nonEmpty) {
      throw new IllegalArgumentException(s"The following fields from the copybook are not found in the DataFrame: ${notFoundFields.mkString(", ")}")
    }

    val unsupportedDataTypeFields = copybookFields.filter { field =>
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

object BasicRecordCombiner {
  def getFieldDefinition(field: Primitive): String = {
    val pic = field.dataType.originalPic.getOrElse(field.dataType.pic)

    val usage = field.dataType match {
      case dt: Integral => dt.compact.map(_.toString).getOrElse("USAGE IS DISPLAY")
      case dt: Decimal  => dt.compact.map(_.toString).getOrElse("USAGE IS DISPLAY")
      case _            => ""
    }

    s"$pic $usage".trim
  }
}
