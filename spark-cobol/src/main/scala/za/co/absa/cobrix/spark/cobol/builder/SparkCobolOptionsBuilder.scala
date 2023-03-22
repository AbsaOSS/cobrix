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

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC}
import za.co.absa.cobrix.cobol.reader.extractors.record.RecordExtractors
import za.co.absa.cobrix.cobol.reader.parameters.{CobolParameters, ReaderParameters}
import za.co.absa.cobrix.spark.cobol.reader.RowHandler
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

import java.nio.charset.{Charset, StandardCharsets}
import scala.collection.mutable

class SparkCobolOptionsBuilder(copybookContent: String)(implicit spark: SparkSession) {
  private val caseInsensitiveOptions = new mutable.HashMap[String, String]()

  def option(key: String, value: String): SparkCobolOptionsBuilder = {
    caseInsensitiveOptions += (key.toLowerCase -> value)
    this
  }

  def options(options: Map[String, String]): SparkCobolOptionsBuilder = {
    caseInsensitiveOptions ++= options.map(kv => (kv._1.toLowerCase(), kv._2))
    this
  }

  def load(rdd: RDD[Array[Byte]]): DataFrame = {
    val params = RddReaderParams.forBinary(caseInsensitiveOptions.toMap)

    getDataFrame(rdd, params)
  }

  def loadText(rddText: RDD[String]): DataFrame = {
    val params = RddReaderParams.forText(caseInsensitiveOptions.toMap)

    getDataFrame(rddText.map(_.getBytes()), params)
  }

  private[cobol] def getDataFrame(rdd: RDD[Array[Byte]], readerParams: ReaderParameters): DataFrame = {
    val encoding = if (readerParams.isEbcdic) EBCDIC else ASCII
    val asciiCharset = if (readerParams.asciiCharset.isEmpty) StandardCharsets.UTF_8 else Charset.forName(readerParams.asciiCharset)
    val segmentRedefines = readerParams.multisegment.map(r => r.segmentIdRedefineMap.values.toList.distinct).getOrElse(Nil)

    val parsedCopybook = CopybookParser.parse(copyBookContents = copybookContent,
                                              dataEncoding = encoding,
                                              dropGroupFillers = readerParams.dropGroupFillers,
                                              dropValueFillers = readerParams.dropValueFillers,
                                              fillerNamingPolicy = readerParams.fillerNamingPolicy,
                                              segmentRedefines = segmentRedefines,
                                              fieldParentMap = readerParams.multisegment.map(_.fieldParentMap).getOrElse(Map.empty),
                                              stringTrimmingPolicy = readerParams.stringTrimmingPolicy,
                                              commentPolicy = readerParams.commentPolicy,
                                              strictSignOverpunch = readerParams.strictSignOverpunch,
                                              improvedNullDetection = readerParams.improvedNullDetection,
                                              ebcdicCodePage = getCodePage(readerParams.ebcdicCodePage, readerParams.ebcdicCodePageClass),
                                              asciiCharset = asciiCharset,
                                              isUtf16BigEndian = readerParams.isUtf16BigEndian,
                                              floatingPointFormat = readerParams.floatingPointFormat,
                                              nonTerminals = readerParams.nonTerminals,
                                              occursHandlers = readerParams.occursMappings,
                                              debugFieldsPolicy = readerParams.debugFieldsPolicy,
                                              fieldCodePageMap = readerParams.fieldCodePage
                                              )

    val cobolSchema = new CobolSchema(parsedCopybook,
                                      readerParams.schemaPolicy,
                                      inputFileNameField = "",
                                      generateRecordId = false,
                                      metadataPolicy = readerParams.metadataPolicy)
    val sparkSchema = cobolSchema.getSparkSchema

    val recordHandler = new RowHandler()

    val schemaRetentionPolicy = readerParams.schemaPolicy

    val rddRow = rdd
      .filter(array => array.nonEmpty)
      .map(array => {
        val record = RecordExtractors.extractRecord[GenericRow](parsedCopybook.ast,
                                                                array,
                                                                0,
                                                                schemaRetentionPolicy, handler = recordHandler)
        Row.fromSeq(record)
      })

    spark.createDataFrame(rddRow, sparkSchema)
  }

  private def getCodePage(codePageName: String, codePageClass: Option[String]): CodePage = {
    codePageClass match {
      case Some(c) => CodePage.getCodePageByClass(c)
      case None    => CodePage.getCodePageByName(codePageName)
    }
  }
}
