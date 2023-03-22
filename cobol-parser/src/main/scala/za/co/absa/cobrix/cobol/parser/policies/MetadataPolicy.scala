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

trait MetadataPolicy

object MetadataPolicy  {
  case object NoMetadata extends MetadataPolicy
  case object Basic extends MetadataPolicy
  case object Extended extends MetadataPolicy

  def apply(policyStr: String): MetadataPolicy = {
    policyStr match {
      case "no_metadata" | "no" | "false" => NoMetadata
      case "basic" => Basic
      case "extended" => Extended
      case _ => throw new IllegalArgumentException(s"Unknown metadata policy: '$policyStr'. Can be one of: 'false', 'basic', 'extended'.")
    }
  }
}
