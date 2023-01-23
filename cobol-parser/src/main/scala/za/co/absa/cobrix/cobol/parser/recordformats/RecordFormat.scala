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

package za.co.absa.cobrix.cobol.parser.recordformats

sealed trait RecordFormat

object RecordFormat {
  case object FixedLength extends RecordFormat
  case object FixedBlock extends RecordFormat
  case object VariableLength extends RecordFormat
  case object VariableBlock extends RecordFormat
  case object AsciiText extends RecordFormat
  case object CobrixAsciiText extends RecordFormat

  def withNameOpt(s: String): Option[RecordFormat] = {
    s match {
      case "F" => Some(FixedLength)
      case "FB" => Some(FixedBlock)
      case "V" => Some(VariableLength)
      case "VB" => Some(VariableBlock)
      case "D" => Some(AsciiText)         // Uses Spark facilities to process ASCII files in parallel
      case "D2" => Some(CobrixAsciiText)  // Similar to 'D', but uses the custom Cobrix ASCII parser
      case "T" => Some(AsciiText)         // Same as 'D' - Cobrix extension
      case _ => None
    }
  }
}
