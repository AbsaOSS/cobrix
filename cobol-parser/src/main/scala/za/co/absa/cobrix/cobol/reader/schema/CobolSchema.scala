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

package za.co.absa.cobrix.cobol.reader.schema

import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.parser.encoding.{ASCII, EBCDIC}
import za.co.absa.cobrix.cobol.parser.policies.MetadataPolicy
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy

import java.nio.charset.{Charset, StandardCharsets}
import scala.collection.immutable.HashMap


/**
  * This class provides a view on a COBOL schema from the perspective of Spark. When provided with a parsed copybook the class
  * provides the corresponding Spark schema and also other properties for the Spark data source.
  *
  * @param copybook                A parsed copybook.
  * @param policy                  Specifies a policy to transform the input schema. The default policy is to keep the schema exactly as it is in the copybook.
  * @param generateRecordId        If true, a record id field will be prepended to the beginning of the schema.
  * @param generateRecordBytes     If true, a record bytes field will be appended to the beginning of the schema.
  * @param inputFileNameField      If non-empty, a source file name will be prepended to the beginning of the schema.
  * @param generateSegIdFieldsCnt  A number of segment ID levels to generate
  * @param segmentIdProvidedPrefix A prefix for each segment id levels to make segment ids globally unique (by default the current timestamp will be used)
  * @param metadataPolicy          Specifies a policy to generate metadata fields.
  */
class CobolSchema(val copybook: Copybook,
                  val policy: SchemaRetentionPolicy,
                  val inputFileNameField: String,
                  val generateRecordId: Boolean,
                  val generateRecordBytes: Boolean,
                  val generateSegIdFieldsCnt: Int = 0,
                  val segmentIdProvidedPrefix: String = "",
                  val metadataPolicy: MetadataPolicy = MetadataPolicy.Basic) extends Serializable {

  val segmentIdPrefix: String = if (segmentIdProvidedPrefix.isEmpty) getDefaultSegmentIdPrefix else segmentIdProvidedPrefix

  def getCobolSchema: Copybook = copybook

  lazy val getRecordSize: Int = copybook.getRecordSize

  def isRecordFixedSize: Boolean = copybook.isRecordFixedSize

  private def getDefaultSegmentIdPrefix: String = {
    val timestampFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val now = ZonedDateTime.now()
    timestampFormat.format(now)
  }
}

object CobolSchema {
  def fromReaderParameters(copyBookContents: Seq[String], readerParameters: ReaderParameters): CobolSchema = {
    if (copyBookContents.isEmpty) {
      throw new IllegalArgumentException("At least one copybook must be specified.")
    }

    val encoding = if (readerParameters.isEbcdic) EBCDIC else ASCII
    val segmentRedefines = readerParameters.multisegment.map(r => r.segmentIdRedefineMap.values.toList.distinct).getOrElse(Nil)
    val fieldParentMap = readerParameters.multisegment.map(r => r.fieldParentMap).getOrElse(HashMap[String, String]())
    val codePage = getCodePage(readerParameters.ebcdicCodePage, readerParameters.ebcdicCodePageClass)
    val asciiCharset = readerParameters.asciiCharset match {
      case Some(asciiCharset) => Charset.forName(asciiCharset)
      case None               => StandardCharsets.UTF_8
    }

    val schema = if (copyBookContents.size == 1)
      CopybookParser.parseTree(encoding,
        copyBookContents.head,
        readerParameters.dropGroupFillers,
        readerParameters.dropValueFillers,
        readerParameters.fillerNamingPolicy,
        segmentRedefines,
        fieldParentMap,
        readerParameters.stringTrimmingPolicy,
        readerParameters.commentPolicy,
        readerParameters.strictSignOverpunch,
        readerParameters.improvedNullDetection,
        readerParameters.decodeBinaryAsHex,
        codePage,
        asciiCharset,
        readerParameters.isUtf16BigEndian,
        readerParameters.floatingPointFormat,
        readerParameters.nonTerminals,
        readerParameters.occursMappings,
        readerParameters.debugFieldsPolicy,
        readerParameters.fieldCodePage)
    else
      Copybook.merge(copyBookContents.map(cpb =>
        CopybookParser.parseTree(encoding,
          cpb,
          readerParameters.dropGroupFillers,
          readerParameters.dropValueFillers,
          readerParameters.fillerNamingPolicy,
          segmentRedefines,
          fieldParentMap,
          readerParameters.stringTrimmingPolicy,
          readerParameters.commentPolicy,
          readerParameters.strictSignOverpunch,
          readerParameters.improvedNullDetection,
          readerParameters.decodeBinaryAsHex,
          codePage,
          asciiCharset,
          readerParameters.isUtf16BigEndian,
          readerParameters.floatingPointFormat,
          nonTerminals = readerParameters.nonTerminals,
          readerParameters.occursMappings,
          readerParameters.debugFieldsPolicy,
          readerParameters.fieldCodePage)
      ))
    val segIdFieldCount = readerParameters.multisegment.map(p => p.segmentLevelIds.size).getOrElse(0)
    val segmentIdPrefix = readerParameters.multisegment.map(p => p.segmentIdPrefix).getOrElse("")
    new CobolSchema(schema, readerParameters.schemaPolicy, readerParameters.inputFileNameColumn, readerParameters.generateRecordId, readerParameters.generateRecordBytes, segIdFieldCount, segmentIdPrefix, readerParameters.metadataPolicy)
  }

  def getCodePage(codePageName: String, codePageClass: Option[String]): CodePage = {
    codePageClass match {
      case Some(c) => CodePage.getCodePageByClass(c)
      case None => CodePage.getCodePageByName(codePageName)
    }
  }
}
