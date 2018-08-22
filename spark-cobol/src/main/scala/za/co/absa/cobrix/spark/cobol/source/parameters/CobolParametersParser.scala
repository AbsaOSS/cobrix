/*
 * Copyright 2018 Barclays Africa Group Limited
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
  val PARAM_RECORD_ID_FILE_INCREMENT = "record_id_file_increment"

  def parse(params: Map[String,String]): CobolParameters = {

    new CobolParameters(
      getParameter(PARAM_COPYBOOK_PATH, params),
      getParameter(PARAM_COPYBOOK_CONTENTS, params),
      getParameter(PARAM_SOURCE_PATH, params),
      params.getOrElse(PARAM_RECORD_START_OFFSET, "0").toInt,
      params.getOrElse(PARAM_RECORD_END_OFFSET, "0").toInt,
      parseVariableLengthParameters(params),
      params.getOrElse(PARAM_GENERATE_RECORD_ID, "false").toBoolean,
      params.getOrElse(PARAM_RECORD_ID_FILE_INCREMENT, Constants.defaultFileRecordIdIncrement.toString).toLong
    )
  }

  private def parseVariableLengthParameters(params: Map[String,String]): Option[VariableLengthParameters] = {

    if (params.contains(PARAM_RECORD_LENGTH)) {
      Some(new VariableLengthParameters
      (
        params.get(PARAM_RECORD_LENGTH).get
      ))
    }
    else {
      None
    }
  }

  private def getParameter(key: String, params: Map[String,String]): Option[String] = {
    if (params.contains(key)) {
      Some(params.get(key).get)
    }
    else {
      None
    }
  }
}