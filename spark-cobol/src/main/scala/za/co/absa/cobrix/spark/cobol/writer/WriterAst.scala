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

object WriterAst {
  type PrimitiveGetter = Row => Any
  type GroupGetter = Row => Row
  type ArrayGetter = Row => mutable.WrappedArray[AnyRef]

  case class Filler(fillerSize: Int) extends WriterAst
  case class PrimitiveField(cobolField: Primitive, getter: PrimitiveGetter) extends WriterAst
  case class GroupField(children: Seq[WriterAst], cobolField: Group, getter: GroupGetter) extends WriterAst
  case class PrimitiveArray(cobolField: Primitive, arrayGetter: ArrayGetter) extends WriterAst
  case class GroupArray(groupField: GroupField, cobolField: Group, arrayGetter: ArrayGetter) extends WriterAst
}
