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

package za.co.absa.cobrix.spark.cobol.source.parameters

import za.co.absa.cobrix.spark.cobol.reader.Constants
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.SchemaRetentionPolicy.SchemaRetentionPolicy

/**
  * This class provides methods for parsing the parameters set as Spark options.
  */
object CobolParametersParser {

  val SHORT_NAME                     = "cobol"
  val PARAM_COPYBOOK_PATH            = "copybook"
  val PARAM_COPYBOOK_CONTENTS        = "copybook_contents"
  val PARAM_SOURCE_PATH              = "path"
  val PARAM_RECORD_LENGTH            = "record_length_field"
  val PARAM_RECORD_START_OFFSET      = "record_start_offset"
  val PARAM_RECORD_END_OFFSET        = "record_end_offset"
  val PARAM_GENERATE_RECORD_ID       = "generate_record_id"
  val PARAM_SCHEMA_RETENTION_POLICY  = "schema_retention_policy"

  def parse(params: Map[String,String]): CobolParameters = {

    val policyName = params.getOrElse(PARAM_SCHEMA_RETENTION_POLICY, "keep_original")
    val policy = SchemaRetentionPolicy.withNameOpt(policyName)

    if (policy.isEmpty) {
      throw new IllegalArgumentException(s"Invalid value '$policyName' for '$PARAM_SCHEMA_RETENTION_POLICY' option.")
    }

    CobolParameters(
      getParameter(PARAM_COPYBOOK_PATH, params),
      getParameter(PARAM_COPYBOOK_CONTENTS, params),
      getParameter(PARAM_SOURCE_PATH, params),
      params.getOrElse(PARAM_RECORD_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_RECORD_END_OFFSET, "0").toInt,
      parseVariableLengthParameters(params),
      params.getOrElse(PARAM_GENERATE_RECORD_ID, "false").toBoolean,
      policy.get
    )
  }

  private def parseVariableLengthParameters(params: Map[String,String]): Option[VariableLengthParameters] = {

    if (params.contains(PARAM_RECORD_LENGTH)) {
      Some(VariableLengthParameters
      (
        params(PARAM_RECORD_LENGTH)
      ))
    }
    else {
      None
    }
  }

  private def getParameter(key: String, params: Map[String,String]): Option[String] = {
    if (params.contains(key)) {
      Some(params(key))
    }
    else {
      None
    }
  }
}