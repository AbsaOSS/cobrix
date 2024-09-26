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

package za.co.absa.cobrix.cobol.reader.extractors.raw

import org.slf4j.LoggerFactory
import za.co.absa.cobrix.cobol.parser.ast.Primitive
import za.co.absa.cobrix.cobol.reader.iterator.RecordLengthExpression
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.validator.ReaderParametersValidator

class FixedWithRecordLengthExprRawRecordExtractor(ctx: RawRecordContext,
                                                  readerProperties: ReaderParameters) extends Serializable with RawRecordExtractor {
  private val log = LoggerFactory.getLogger(this.getClass)
  ctx.headerStream.close()

  final private val copyBookRecordSize = ctx.copybook.getRecordSize
  final private val (recordLengthField, lengthFieldExpr) = ReaderParametersValidator.getEitherFieldAndExpression(readerProperties.lengthFieldExpression, readerProperties.lengthFieldMap, ctx.copybook)

  final private val lengthField = recordLengthField.map(_.field)
  final private val lengthMap = recordLengthField.map(_.valueMap).getOrElse(Map.empty)
  final private val isLengthMapEmpty = lengthMap.isEmpty

  type RawRecord = (String, Array[Byte])

  private var cachedValue: Option[RawRecord] = _
  private var byteIndex = readerProperties.fileStartOffset

  final private val segmentIdField = ReaderParametersValidator.getSegmentIdField(readerProperties.multisegment, ctx.copybook)
  final private val recordLengthAdjustment = readerProperties.rdwAdjustment

  fetchNext()

  override def offset: Long = cachedValue match {
    case Some(v) => ctx.inputStream.offset - v._2.length
    case None => ctx.inputStream.offset
  }

  override def hasNext: Boolean = cachedValue.nonEmpty

  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    cachedValue match {
      case None => throw new NoSuchElementException
      case Some(value) =>
        fetchNext()
        value._2
    }
  }

  private def fetchNext(): Unit = {
    var recordFetched = false
    while (!recordFetched) {
      val binaryData = if (lengthField.nonEmpty) {
        fetchRecordUsingRecordLengthField()
      } else {
        fetchRecordUsingRecordLengthFieldExpression(lengthFieldExpr.get)
      }

      binaryData match {
        case None =>
          cachedValue = None
          recordFetched = true
        case Some(data) if data.length < readerProperties.minimumRecordLength || data.length > readerProperties.maximumRecordLength =>
          recordFetched = false
        case Some(data) =>
          val segmentId = getSegmentId(data)
          val segmentIdStr = segmentId.getOrElse("")

          cachedValue = Some(segmentIdStr, data)
          recordFetched = true
      }
    }
  }

  private def fetchRecordUsingRecordLengthField(): Option[Array[Byte]] = {
    if (lengthField.isEmpty) {
      throw new IllegalStateException(s"For variable length reader either RDW record headers or record length field should be provided.")
    }

    val lengthFieldBlock = lengthField.get.binaryProperties.offset + lengthField.get.binaryProperties.actualSize

    val binaryDataStart = ctx.inputStream.next(readerProperties.startOffset + lengthFieldBlock)

    byteIndex += readerProperties.startOffset + lengthFieldBlock

    if (binaryDataStart.length < readerProperties.startOffset + lengthFieldBlock) {
      return None
    }

    val recordLength = lengthField match {
      case Some(lengthAST) => getRecordLengthFromField(lengthAST, binaryDataStart)
      case None            => copyBookRecordSize
    }

    val restOfDataLength = recordLength - lengthFieldBlock + readerProperties.endOffset

    byteIndex += restOfDataLength

    if (restOfDataLength > 0) {
      Some(binaryDataStart ++ ctx.inputStream.next(restOfDataLength))
    } else {
      Some(binaryDataStart)
    }
  }

  final private def getRecordLengthFromField(lengthAST: Primitive, binaryDataStart: Array[Byte]): Int = {
    val length = if (isLengthMapEmpty) {
      ctx.copybook.extractPrimitiveField(lengthAST, binaryDataStart, readerProperties.startOffset) match {
        case i: Int    => i
        case l: Long   => l.toInt
        case s: String => s.toInt
        case null      => throw new IllegalStateException(s"Null encountered as a record length field (offset: $byteIndex, raw value: ${getBytesAsHexString(binaryDataStart)}).")
        case _         => throw new IllegalStateException(s"Record length value of the field ${lengthAST.name} must be an integral type.")
      }
    } else {
      ctx.copybook.extractPrimitiveField(lengthAST, binaryDataStart, readerProperties.startOffset) match {
        case i: Int    => getRecordLengthFromMapping(i.toString)
        case l: Long   => getRecordLengthFromMapping(l.toString)
        case s: String => getRecordLengthFromMapping(s)
        case null      => throw new IllegalStateException(s"Null encountered as a record length field (offset: $byteIndex, raw value: ${getBytesAsHexString(binaryDataStart)}).")
        case _         =>    throw new IllegalStateException(s"Record length value of the field ${lengthAST.name} must be an integral type.")
      }
    }
    length + recordLengthAdjustment
  }

  final private def getRecordLengthFromMapping(v: String): Int = {
    lengthMap.get(v) match {
      case Some(len) => len
      case None => throw new IllegalStateException(s"Record length value '$v' is not mapped to a record length.")
    }
  }

  final private def getBytesAsHexString(bytes: Array[Byte]): String = {
    bytes.map("%02X" format _).mkString
  }

  private def fetchRecordUsingRecordLengthFieldExpression(expr: RecordLengthExpression): Option[Array[Byte]] = {
    val lengthFieldBlock = expr.requiredBytesToread
    val evaluator = expr.evaluator

    val binaryDataStart = ctx.inputStream.next(readerProperties.startOffset + lengthFieldBlock)

    byteIndex += readerProperties.startOffset + lengthFieldBlock

    if (binaryDataStart.length < readerProperties.startOffset + lengthFieldBlock) {
      return None
    }

    expr.fields.foreach{
      case (name, field) =>
        val obj = ctx.copybook.extractPrimitiveField(field, binaryDataStart, readerProperties.startOffset)
        try {
          obj match {
            case i: Int    => evaluator.setValue(name, i)
            case l: Long   => evaluator.setValue(name, l.toInt)
            case s: String => evaluator.setValue(name, s.toInt)
            case _         => throw new IllegalStateException(s"Record length value of the field ${field.name} must be an integral type.")
          }
        } catch {
          case ex: NumberFormatException =>
            throw new IllegalStateException(s"Encountered an invalid value of the record length field. Cannot parse '$obj' as an integer in: ${field.name} = '$obj'.", ex)
        }
    }

    val recordLength = evaluator.eval()

    val restOfDataLength = recordLength - lengthFieldBlock + readerProperties.endOffset

    byteIndex += restOfDataLength

    if (restOfDataLength > 0) {
      Some(binaryDataStart ++ ctx.inputStream.next(restOfDataLength))
    } else {
      Some(binaryDataStart)
    }
  }

  private def getSegmentId(data: Array[Byte]): Option[String] = {
    segmentIdField.map(field => {
      val fieldValue = ctx.copybook.extractPrimitiveField(field, data, readerProperties.startOffset)
      if (fieldValue == null) {
        log.error(s"An unexpected null encountered for segment id at $byteIndex")
        ""
      } else {
        fieldValue.toString.trim
      }
    })
  }
}
