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

package za.co.absa.cobrix.cobol.parser.antlr

import scala.collection.mutable.ListBuffer

/**
  * A tiny stack implementation (since mutable.Stack is deprecated (since 2.12.0)
  */
object Stack {
  def apply[A](l: A*): Stack[A] = {
    new Stack[A](l:_*)
  }
}
class Stack[A](l: A*) extends Seq[A] {
  var lst: ListBuffer[A] = ListBuffer[A](l:_*)

  def top: A = lst.head

  def push(a: A): Unit = a +=: lst

  def pop: A = {
    val a = lst.head
    lst = lst.tail
    a
  }

  override def length: Int = lst.length

  override def apply(idx: Int): A = lst(idx)

  override def iterator: Iterator[A] = lst.iterator
}
