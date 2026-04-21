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

package za.co.absa.cobrix.cobol.parser.policies

trait VariableSizeOccursPolicy

object VariableSizeOccursPolicy  {
  case object MaxSize extends VariableSizeOccursPolicy
  case object ShiftRecord extends VariableSizeOccursPolicy
  case object PadRecord extends VariableSizeOccursPolicy

  def apply(policyStr: String): VariableSizeOccursPolicy = {
    policyStr.toLowerCase match {
      case "false" | "max_size" => MaxSize
      case "true" | "shift_record" => ShiftRecord
      case "pad_record" => PadRecord
      case _ => throw new IllegalArgumentException(s"Unknown variable size OCCURS policy: '$policyStr'. Can be one of: 'false' (aka 'max_size') , 'true' (aka 'shift_record'), 'pad_record'.")
    }
  }
}
