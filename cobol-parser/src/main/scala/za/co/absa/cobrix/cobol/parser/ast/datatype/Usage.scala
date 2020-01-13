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

package za.co.absa.cobrix.cobol.parser.ast.datatype


sealed trait Usage

//case class COMP() extends Usage
//case class COMP0() extends Usage
case class COMP1() extends Usage {
  override def toString = "COMP-1"
}
case class COMP2() extends Usage {
  override def toString = "COMP-2"
}
case class COMP3() extends Usage {
  override def toString = "COMP-3"
}
case class COMP4() extends Usage {
  override def toString = "COMP-4"
}
case class COMP5() extends Usage {
  override def toString = "COMP-5"
}
case class COMP9() extends Usage {       // artificial little-endian binary
  override def toString = "COMP-9"
}
//case class DISPLAY() extends Usage {
//  override def toString = "DISPLAY"
//}
//case class BINARY() extends Usage

