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

import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC, Encoding}
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.parameters.CobolParametersParser.{PARAM_ASCII_CHARSET, PARAM_ENCODING, PARAM_SOURCE_PATH, parse}
import za.co.absa.cobrix.spark.cobol.parameters.{CobolParametersParser, Parameters}

import java.nio.charset.{Charset, StandardCharsets}

case class RddReaderParams(
                          encoding: Encoding,
                          asciiCharset: Charset,
                          schemaRetentionPolicy: SchemaRetentionPolicy
                          )

object RddReaderParams {
  val keysIncompatibleWithBinRdd: Seq[String] = Seq(PARAM_SOURCE_PATH)
  val keysIncompatibleWithTextRdd: Seq[String] = Seq(PARAM_ENCODING, PARAM_ASCII_CHARSET)

  def forBinary(options: Map[String, String]): RddReaderParams = {
    val incompatible = findIncompatible(options, keysIncompatibleWithBinRdd)

    if (incompatible.nonEmpty) {
      throw new IllegalArgumentException(s"The following options(s) are incompatible with the RDD reader: ${incompatible.mkString(", ")}")
    }

    val cobolParams = CobolParametersParser.parse(new Parameters(options))

    val encoding = if (cobolParams.isEbcdic) EBCDIC else ASCII
    val asciiCharset = if (cobolParams.asciiCharset.isEmpty) StandardCharsets.UTF_8 else Charset.forName(cobolParams.asciiCharset)

    RddReaderParams(encoding, asciiCharset, cobolParams.schemaRetentionPolicy)
  }

  def forText(options: Map[String, String]): RddReaderParams = {
    val incompatible = findIncompatible(options, keysIncompatibleWithTextRdd)

    if (incompatible.nonEmpty) {
      throw new IllegalArgumentException(s"The following options(s) are incompatible with the text RDD reader: ${incompatible.mkString(", ")}")
    }
    val textOptions = options +
      (PARAM_ENCODING -> "ascii") +
      (PARAM_ASCII_CHARSET -> "utf8")

    forBinary(textOptions)
  }

  private def findIncompatible(options: Map[String, String], keys: Seq[String]): Iterable[String] = {
    options.keys
      .filter(k => keys.contains(k))
  }

}