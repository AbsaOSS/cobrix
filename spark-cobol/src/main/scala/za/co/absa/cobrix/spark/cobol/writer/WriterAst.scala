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

import org.apache.spark.sql.Row
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}

import scala.collection.mutable

sealed trait WriterAst

/**
  * Represents an abstract syntax tree (AST) for writing COBOL data structures from Spark rows.
  *
  * This object provides type aliases and case classes that define the structure for converting
  * Spark Row data into COBOL format. The AST models the hierarchical structure of COBOL records,
  * including primitive fields, groups, arrays, and fillers.
  *
  * Type aliases define getter functions for extracting data from Spark rows:
  * - PrimitiveGetter extracts a single value from a row
  * - GroupGetter extracts a nested row structure from a row
  * - ArrayGetter extracts an array of values from a row
  *
  * Case classes represent different node types in the writer AST:
  * - Filler represents unused space in COBOL records with a specified size
  * - PrimitiveField represents a single COBOL primitive field with its getter function
  * - PrimitiveDependeeField represents a primitive field that other fields depend on for their occurrence count
  * - GroupField represents a COBOL group containing child fields with its getter function
  * - PrimitiveArray represents an array of primitive values with optional depending-on semantics
  * - GroupArray represents an array of group structures with optional depending-on semantics
  *
  * The depending-on fields support COBOL's OCCURS DEPENDING ON clause, where the actual number
  * of array elements is determined by the value of another field at runtime.
  */
object WriterAst {
  type PrimitiveGetter = Row => Any
  type GroupGetter = Row => Row
  type ArrayGetter = Row => mutable.WrappedArray[AnyRef]

  case class Filler(fillerSize: Int) extends WriterAst
  case class PrimitiveField(cobolField: Primitive, getter: PrimitiveGetter) extends WriterAst
  case class PrimitiveDependeeField(spec: DependingOnField) extends WriterAst
  case class GroupField(children: Seq[WriterAst], cobolField: Group, getter: GroupGetter) extends WriterAst
  case class PrimitiveArray(cobolField: Primitive, arrayGetter: ArrayGetter, dependingOn: Option[DependingOnField]) extends WriterAst
  case class GroupArray(groupField: GroupField, cobolField: Group, arrayGetter: ArrayGetter, dependingOn: Option[DependingOnField]) extends WriterAst
}
