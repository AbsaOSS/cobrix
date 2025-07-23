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

package za.co.absa.cobrix.spark.cobol.builder

import za.co.absa.cobrix.cobol.reader.parameters.{CobolParameters, CobolParametersParser, Parameters, ReaderParameters}
import za.co.absa.cobrix.cobol.reader.parameters.CobolParametersParser._

object RddReaderParams {
  def forBinary(options: Map[String, String]): ReaderParameters = {
    val incompatible = findIncompatible(options, keysIncompatibleWithBinRdd)

    if (incompatible.nonEmpty) {
      throw new IllegalArgumentException(s"The following options(s) are incompatible with the RDD reader: ${incompatible.mkString(", ")}")
    }

    val cobolParams: CobolParameters = CobolParametersParser.parse(new Parameters(options))

    getReaderProperties(cobolParams, None)
  }

  def forText(options: Map[String, String]): ReaderParameters = {
    val incompatible = findIncompatible(options, keysIncompatibleWithTextRdd)

    if (incompatible.nonEmpty) {
      throw new IllegalArgumentException(s"The following options(s) are incompatible with the text RDD reader: ${incompatible.mkString(", ")}")
    }
    val textOptions = options +
      (PARAM_ENCODING -> "ascii") +
      (PARAM_ASCII_CHARSET -> "utf8")

    if (options.keys.exists(_.startsWith(PARAM_FIELD_CODE_PAGE_PREFIX))) {
      throw new IllegalArgumentException(s"Code page mapping using '$PARAM_FIELD_CODE_PAGE_PREFIX*' is not supported with text RDD reader. Use forBinary()")
    }

    forBinary(textOptions)
  }

  private def findIncompatible(options: Map[String, String], keys: Seq[String]): Iterable[String] = {
    options.keys
      .filter(k => keys.contains(k))
  }

  private val keysIncompatibleWithBinRdd: Seq[String] = Seq(
    PARAM_COPYBOOK_PATH,
    PARAM_MULTI_COPYBOOK_PATH,
    PARAM_COPYBOOK_CONTENTS,
    PARAM_SOURCE_PATH,
    PARAM_SOURCE_PATHS,
    PARAM_SOURCE_PATHS_LEGACY,
    PARAM_FILE_START_OFFSET,
    PARAM_FILE_END_OFFSET,
    PARAM_GENERATE_RECORD_ID,
    PARAM_RECORD_FORMAT,
    PARAM_RECORD_LENGTH,
    PARAM_IS_XCOM,
    PARAM_IS_RECORD_SEQUENCE,
    PARAM_IS_RDW_BIG_ENDIAN,
    PARAM_IS_BDW_BIG_ENDIAN,
    PARAM_IS_RDW_PART_REC_LENGTH,
    PARAM_RDW_ADJUSTMENT,
    PARAM_BDW_ADJUSTMENT,
    PARAM_BLOCK_LENGTH,
    PARAM_RECORDS_PER_BLOCK,
    PARAM_SEGMENT_ID_PREFIX,
    PARAM_RECORD_HEADER_PARSER,
    PARAM_RECORD_EXTRACTOR,
    PARAM_RHP_ADDITIONAL_INFO,
    PARAM_RE_ADDITIONAL_INFO,
    PARAM_INPUT_FILE_COLUMN,
    PARAM_ENABLE_INDEXES,
    PARAM_INPUT_SPLIT_RECORDS,
    PARAM_INPUT_SPLIT_SIZE_MB,
    PARAM_OPTIMIZE_ALLOCATION,
    PARAM_IMPROVE_LOCALITY,
    PARAM_DEBUG_IGNORE_FILE_SIZE
    )

  private val keysIncompatibleWithTextRdd: Seq[String] = Seq(
    PARAM_ENCODING,
    PARAM_ASCII_CHARSET
    )
}