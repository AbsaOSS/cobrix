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
import org.apache.spark.sql.types.{ArrayType, StructType}
import org.apache.spark.sql.{DataFrame, Row}
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.Copybook
import za.co.absa.cobrix.cobol.parser.ast.datatype.{Decimal, Integral}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema
import za.co.absa.cobrix.spark.cobol.writer.WriterAst._

import scala.collection.mutable

class NestedRecordCombiner extends RecordCombiner {

  import NestedRecordCombiner._

  /**
    * Converts Spark DataFrame to the RDD with data in mainframe format as arrays of bytes, each array being a record.
    *
    * @param df               The input DataFrame
    * @param cobolSchema      The output COBOL schema
    * @param readerParameters The reader properties which are actually writer properties parsed as spark-cobol options
    * @return The RDD of records in mainframe format
    */
  override def combine(df: DataFrame, cobolSchema: CobolSchema, readerParameters: ReaderParameters): RDD[Array[Byte]] = {
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
    if (!isRdwBigEndian && recordLengthLong > Int.MaxValue.toLong) {
      throw new IllegalArgumentException(
        s"RDW length $recordLengthLong exceeds ${Int.MaxValue} and cannot be encoded safely."
      )
    }
    processRDD(df.rdd, cobolSchema.copybook, df.schema, size, adjustment1 + adjustment2, startOffset, hasRdw, isRdwBigEndian, readerParameters.variableSizeOccurs, readerParameters.strictSchema)
  }
}

object NestedRecordCombiner {
  private val log = LoggerFactory.getLogger(this.getClass)

  /**
    * Generates a field definition string containing the PIC clause and USAGE clause for a primitive COBOL field.
    *
    * This method extracts the picture clause (PIC) and usage information from the field's data type
    * and combines them into a single definition string. For integral and decimal types with compact
    * encoding, it includes the USAGE clause; otherwise, the default DISPLAY usage is assumed or omitted.
    *
    * The purpose is to render COBOL field name and type in exceptions and log messages.
    *
    * @param field The primitive field whose definition string should be generated
    * @return A string containing the PIC clause and optional USAGE clause, with any trailing whitespace trimmed
    */
  def getFieldDefinition(field: Primitive): String = {
    val pic = field.dataType.originalPic.getOrElse(field.dataType.pic)

    val usage = field.dataType match {
      case dt: Integral => dt.compact.map(_.toString).getOrElse("USAGE IS DISPLAY")
      case dt: Decimal  => dt.compact.map(_.toString).getOrElse("USAGE IS DISPLAY")
      case _            => ""
    }

    s"$pic $usage".trim
  }

  /**
    * Constructs a writer AST (Abstract Syntax Tree) from a copybook and Spark schema for serialization purposes.
    *
    * This method creates a hierarchical structure of WriterAst nodes that maps the copybook structure to the
    * corresponding Spark schema. The resulting GroupField can be used to serialize Spark Rows into binary format
    * according to the copybook specification. The AST contains getter functions that extract values from Rows
    * and metadata needed to write those values to the correct byte positions in the output buffer.
    *
    * The purpose of WriterAst class hierarchy is to provide memory and CPU efficient way of creating binary
    * records from Spark dataframes. It links Cobol schema and Spark schema in a single tree.
    *
    * @param copybook     The copybook definition describing the binary record layout and field specifications
    * @param schema       The Spark StructType schema that corresponds to the structure of the data to be written
    * @param strictSchema If true, each field in the copybook must exist in the Spark schema.
    * @return A GroupField representing the root of the writer AST, containing all non-filler, non-redefines
    *         fields with their associated getter functions and position information for binary serialization
    */
  def constructWriterAst(copybook: Copybook, schema: StructType, strictSchema: Boolean): GroupField = {
    buildGroupField(getAst(copybook), schema, row => row, "", new mutable.HashMap[String, DependingOnField](), strictSchema)
  }

  /**
    * Processes an RDD of Spark Rows and converts them into an RDD of byte arrays according to the copybook specification.
    *
    * The resulting RDD can then be written to storage as files in mainframe format (usually, EBCDIC).
    *
    * Each Row is transformed into a fixed or variable-length byte array representation based on the copybook layout.
    *
    * Variable-record-length records supported are ones that have RDW headers (big-endian or little-endian).
    * For variable-length records with OCCURS DEPENDING ON, the output may be trimmed to the actual bytes written.
    *
    * @param rdd                The input RDD containing Spark Rows to be converted to binary format
    * @param copybook           The copybook definition that describes the binary record layout and field specifications
    * @param schema             The Spark StructType schema that corresponds to the structure of the input Rows
    * @param recordSize         The maximum size in bytes allocated for each output record
    * @param recordLengthAdj    An adjustment value added to the bytes written when computing the RDW length field
    * @param startOffset        The byte offset at which data writing should begin, typically 0 for fixed-length or 4 for RDW records
    * @param hasRdw             A flag indicating whether to prepend a Record Descriptor Word header to each output record
    * @param isRdwBigEndian     A flag indicating the byte order for the RDW header, true for big-endian, false for little-endian
    * @param variableSizeOccurs A flag indicating whether OCCURS DEPENDING ON fields should use actual element counts rather than maximum sizes
    * @param strictSchema       If true, each field in the copybook must exist in the Spark schema.
    * @return An RDD of byte arrays, where each array represents one record in binary format according to the copybook specification
    */
  private[cobrix] def processRDD(rdd: RDD[Row],
                                 copybook: Copybook,
                                 schema: StructType,
                                 recordSize: Int,
                                 recordLengthAdj: Int,
                                 startOffset: Int,
                                 hasRdw: Boolean,
                                 isRdwBigEndian: Boolean,
                                 variableSizeOccurs: Boolean,
                                 strictSchema: Boolean): RDD[Array[Byte]] = {
    val writerAst = constructWriterAst(copybook, schema, strictSchema)

    rdd.mapPartitions { rows =>
      rows.map { row =>
        val ar = new Array[Byte](recordSize)

        val bytesWritten = writeToBytes(writerAst, row, ar, startOffset, variableSizeOccurs)

        if (hasRdw) {
          val recordLengthToWriteToRDW = bytesWritten + recordLengthAdj

          if (isRdwBigEndian) {
            ar(0) = ((recordLengthToWriteToRDW >> 8) & 0xFF).toByte
            ar(1) = (recordLengthToWriteToRDW & 0xFF).toByte
            // The last two bytes are reserved and defined by IBM as binary zeros on all platforms.
            ar(2) = 0
            ar(3) = 0
          } else {
            ar(0) = (recordLengthToWriteToRDW & 0xFF).toByte
            ar(1) = ((recordLengthToWriteToRDW >> 8) & 0xFF).toByte
            // This is non-standard. But so are little-endian RDW headers.
            // As an advantage, it has no effect for small records but adds support for big records (> 64KB).
            ar(2) = ((recordLengthToWriteToRDW >> 16) & 0xFF).toByte
            ar(3) = ((recordLengthToWriteToRDW >> 24) & 0xFF).toByte
          }
        }

        if (!variableSizeOccurs || recordSize == bytesWritten + startOffset) {
          ar
        } else {
          java.util.Arrays.copyOf(ar, bytesWritten + startOffset)
        }
      }
    }
  }

  /**
    * Retrieves the appropriate AST (Abstract Syntax Tree) group from a copybook.
    * If the root AST has exactly one child and that child is a Group, returns that child.
    * Otherwise, returns the root AST itself. This normalization ensures consistent handling
    * of copybook structures regardless of whether they have a single top-level group or multiple elements.
    *
    * @param copybook The copybook object containing the AST structure to extract from
    * @return The normalized Group representing the copybook structure, either the single child group or the root AST
    */
  def getAst(copybook: Copybook): Group = {
    val rootAst = copybook.ast

    if (rootAst.children.length == 1 && rootAst.children.head.isInstanceOf[Group]) {
      rootAst.children.head.asInstanceOf[Group]
    } else {
      rootAst
    }
  }

  /**
    * Recursively walks the copybook group and the Spark StructType in lockstep, producing
    * [[WriterAst]] nodes whose getters extract the correct value from a [[org.apache.spark.sql.Row]].
    *
    * @param group        A copybook Group node whose children will be processed.
    * @param schema       The Spark StructType that corresponds to `group`.
    * @param getter       A function that, given the "outer" Row, returns the Row that belongs to this group.
    * @param path         The path to the field
    * @param dependeeMap  A map of field names to their corresponding DependingOnField specs, used to resolve dependencies for OCCURS DEPENDING ON fields.
    * @param strictSchema If true, each field in the copybook must exist in the Spark schema.
    * @return A [[GroupField]] covering all non-filler, non-redefines children found in both
    *         the copybook and the Spark schema.
    */
  private def buildGroupField(group: Group, schema: StructType, getter: GroupGetter, path: String, dependeeMap: mutable.HashMap[String, DependingOnField], strictSchema: Boolean): GroupField = {
    val children = group.children.withFilter { stmt =>
      stmt.redefines.isEmpty
    }.map {
      case s if s.isFiller => Filler(s.binaryProperties.actualSize)
      case p: Primitive    => buildPrimitiveNode(p, schema, path, dependeeMap, strictSchema)
      case g: Group        => buildGroupNode(g, schema, path, dependeeMap, strictSchema)
    }
    GroupField(children.toSeq, group, getter)
  }

  /**
    * Builds a [[WriterAst]] node for a primitive copybook field, using the field's index in the
    * supplied Spark schema to create a getter function.
    *
    * Returns a filler when the field is absent from the schema (e.g. filtered out during reading).
    */
  private def buildPrimitiveNode(p: Primitive, schema: StructType, path: String, dependeeMap: mutable.HashMap[String, DependingOnField], strictSchema: Boolean): WriterAst = {
    def addDependee(): DependingOnField = {
      val spec = DependingOnField(p, p.binaryProperties.offset)
      val uppercaseName = p.name.toUpperCase()
      if (dependeeMap.contains(uppercaseName)) {
        throw new IllegalArgumentException(s"Duplicate field name '${p.name}' found in copybook. " +
          s"Field names must be unique (case-insensitive) when OCCURS DEPENDING ON is used. " +
          s"Already found a dependee field with the same name at line ${dependeeMap(uppercaseName).cobolField.lineNumber}, " +
          s"current field line number: ${p.lineNumber}.")
      }
      dependeeMap += (p.name.toUpperCase -> spec)
      spec
    }

    val fieldName = p.name
    val fieldIndexOpt = schema.fields.zipWithIndex.find { case (field, _) =>
      field.name.equalsIgnoreCase(fieldName)
    }.map(_._2)

    fieldIndexOpt.map { idx =>
      if (p.encode.isEmpty) {
        val fieldDefinition = getFieldDefinition(p)
        throw new IllegalArgumentException(s"Field '${p.name}' does not have an encoding defined in the copybook. " +
          s"'PIC $fieldDefinition' is not yet supported.")
      }
      if (p.occurs.isDefined) {
        // Array of primitives
        val dependingOnField = p.dependingOn.map { dependingOn =>
          dependeeMap.getOrElse(dependingOn.toUpperCase, throw new IllegalStateException(
            s"Array field '${p.name}' depends on '$dependingOn' which is not found among previously processed fields."
          ))
        }
        PrimitiveArray(p, row => row.getAs[mutable.WrappedArray[AnyRef]](idx), dependingOnField)
      } else {
        if (p.isDependee) {
          PrimitiveDependeeField(addDependee())
        } else {
          PrimitiveField(p, row => row.get(idx))
        }
      }
    }.getOrElse {
      // Dependee fields need not be defined in Spark schema.
      if (p.isDependee) {
        PrimitiveDependeeField(addDependee())
      } else {
        if (strictSchema)
          throw new IllegalArgumentException(s"Field '$path${p.name}' is not found in Spark schema.")
        else
          log.warn(s"Field '$path${p.name}' is not found in Spark schema. Will be replaced by filler.")
        Filler(p.binaryProperties.actualSize)
      }
    }
  }

  /**
    * Builds a [[WriterAst]] node for a group copybook field.  For groups with OCCURS the getter
    * extracts an array; for plain groups it extracts the nested Row.  In both cases the children
    * are built by recursing into the nested Spark StructType.
    *
    * Returns a filler when the field is absent from the schema.
    */
  private def buildGroupNode(g: Group, schema: StructType, path: String, dependeeMap: mutable.HashMap[String, DependingOnField], strictSchema: Boolean): WriterAst = {
    val fieldName = g.name
    val fieldIndexOpt = schema.fields.zipWithIndex.find { case (field, _) =>
      field.name.equalsIgnoreCase(fieldName)
    }.map(_._2)

    fieldIndexOpt.map { idx =>
      if (g.occurs.isDefined) {
        // Array of structs – the element type must be a StructType
        schema(idx).dataType match {
          case ArrayType(elementType: StructType, _) =>
            val dependingOnField = g.dependingOn.map { dependingOn =>
              dependeeMap.getOrElse(dependingOn.toUpperCase, throw new IllegalStateException(
                s"Array group '${g.name}' depends on '$dependingOn' which is not found among previously processed fields."
              ))
            }
            val childAst = buildGroupField(g, elementType, row => row, s"$path${g.name}.", dependeeMap, strictSchema)
            GroupArray(childAst, g, row => row.getAs[mutable.WrappedArray[AnyRef]](idx), dependingOnField)
          case other                                 =>
            throw new IllegalArgumentException(
              s"Expected ArrayType(StructType) for group field '${g.name}' with OCCURS, but got $other")
        }
      } else {
        // Nested struct
        schema(idx).dataType match {
          case nestedSchema: StructType =>
            val childGetter: GroupGetter = row => row.getAs[Row](idx)
            val childAst = buildGroupField(g, nestedSchema, childGetter, s"$path${g.name}.", dependeeMap, strictSchema)
            GroupField(childAst.children, g, childGetter)
          case other                    =>
            throw new IllegalArgumentException(
              s"Expected StructType for group field '${g.name}', but got $other")
        }
      }
    }.getOrElse {
      if (strictSchema)
        throw new IllegalArgumentException(s"Field '$path${g.name}' is not found in Spark schema.")
      else
        log.warn(s"Field '$path${g.name}' is not found in Spark schema. Will be replaced by filler.")
      Filler(g.binaryProperties.actualSize)
    }
  }

  /**
    * Recursively walks `ast` and writes every primitive value from `row` into `ar`.
    *
    * For plain (non-array) fields the `configuredStartOffset` is forwarded directly to
    * [[Copybook.setPrimitiveField]], which adds it to `field.binaryProperties.offset`.
    *
    * For array fields (both primitive and group-of-primitives) each element is written
    * using the `fieldStartOffsetOverride` parameter so the exact byte position can be
    * supplied.  The row array may contain fewer elements than the copybook allows — any
    * missing tail elements are silently skipped, leaving those bytes as zeroes.
    *
    * @param ast                  The [[WriterAst]] node to process.
    * @param row                  The Spark [[Row]] from which values are read.
    * @param ar                   The target byte array (record buffer).
    * @param currentOffset        RDW prefix length (0 for fixed-length records, 4 for variable).
    * @param variableLengthOccurs A flag indicating whether size of OCCURS DEPENDING ON should match the number of elements
    *                             and not always fixed.
    * @throws IllegalArgumentException if a field value cannot be encoded according to the copybook definition.
    */
  private def writeToBytes(ast: WriterAst, row: Row, ar: Array[Byte], currentOffset: Int, variableLengthOccurs: Boolean): Int = {
    ast match {
      // ── Filler  ──────────────────────────────────────────────────────────────
      case Filler(size) => size

      // ── Plain primitive ──────────────────────────────────────────────────────
      case PrimitiveField(cobolField, getter) =>
        val value = getter(row)
        if (value != null) {
          Copybook.setPrimitiveField(cobolField, ar, value, 0, currentOffset)
        }
        cobolField.binaryProperties.actualSize

      // ── Primitive which has an OCCURS DEPENDS ON ─────────────────────────────
      case PrimitiveDependeeField(spec) =>
        // NOTE: baseOffset is mutated here for each row. This is safe because rows
        // are processed sequentially within mapPartitions, and the offset is always
        // updated before being read in subsequent array-element writes.
        spec.baseOffset = currentOffset
        spec.cobolField.binaryProperties.actualSize

      // ── Plain nested group ───────────────────────────────────────────────────
      case GroupField(children, cobolField, getter) =>
        val nestedRow = getter(row)
        if (nestedRow != null) {
          var writtenBytes = 0
          children.foreach { child =>
            val written = writeToBytes(child, nestedRow, ar, currentOffset + writtenBytes, variableLengthOccurs)
            writtenBytes += written
          }
          writtenBytes
        } else {
          cobolField.binaryProperties.actualSize
        }

      // ── Array of primitives  (OCCURS on a primitive field) ───────────────────
      case PrimitiveArray(cobolField, arrayGetter, dependingOn) =>
        val arr = arrayGetter(row)
        if (arr != null) {
          val maxElements = cobolField.arrayMaxSize // copybook upper bound
          val elementSize = cobolField.binaryProperties.dataSize
          val baseOffset = currentOffset
          val elementsToWrite = math.min(arr.length, maxElements)

          var i = 0
          while (i < elementsToWrite) {
            val value = arr(i)
            if (value != null) {
              val elementOffset = baseOffset + i * elementSize
              // fieldStartOffsetOverride is the absolute position; pass it so
              // setPrimitiveField does not add binaryProperties.offset on top again.
              Copybook.setPrimitiveField(cobolField, ar, value, fieldStartOffsetOverride = elementOffset)
            }
            i += 1
          }
          dependingOn.foreach(spec =>
            Copybook.setPrimitiveField(spec.cobolField, ar, elementsToWrite, fieldStartOffsetOverride = spec.baseOffset)
          )
          if (variableLengthOccurs) {
            // For variable-length OCCURS, the actual size is determined by the number of elements written.
            elementSize * elementsToWrite
          } else {
            cobolField.binaryProperties.actualSize
          }
        } else {
          dependingOn.foreach(spec =>
            Copybook.setPrimitiveField(spec.cobolField, ar, 0, fieldStartOffsetOverride = spec.baseOffset)
          )
          if (variableLengthOccurs) 0 else cobolField.binaryProperties.actualSize
        }

      // ── Array of groups  (OCCURS on a group field) ───────────────────────────
      case GroupArray(groupField: GroupField, cobolField, arrayGetter, dependingOn) =>
        val arr = arrayGetter(row)
        if (arr != null) {
          val maxElements = cobolField.arrayMaxSize // copybook upper bound
          val elementSize = cobolField.binaryProperties.dataSize
          val baseOffset = currentOffset
          val elementsToWrite = math.min(arr.length, maxElements)

          var i = 0
          while (i < elementsToWrite) {
            val elementRow = arr(i).asInstanceOf[Row]
            if (elementRow != null) {
              // Build an adjusted element offset so that each child's base offset
              // (which is relative to the group's base) lands at the correct position in ar.
              val elementStartOffset = baseOffset + i * elementSize
              writeToBytes(groupField, elementRow, ar, elementStartOffset, variableLengthOccurs)
            }
            i += 1
          }
          dependingOn.foreach(spec =>
            Copybook.setPrimitiveField(spec.cobolField, ar, elementsToWrite, fieldStartOffsetOverride = spec.baseOffset)
          )
          if (variableLengthOccurs) {
            // For variable-length OCCURS, the actual size is determined by the number of elements written.
            elementSize * elementsToWrite
          } else {
            cobolField.binaryProperties.actualSize
          }
        } else {
          dependingOn.foreach(spec =>
            Copybook.setPrimitiveField(spec.cobolField, ar, 0, fieldStartOffsetOverride = spec.baseOffset)
          )
          if (variableLengthOccurs) 0 else cobolField.binaryProperties.actualSize
        }
    }
  }
}
