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

package za.co.absa.cobrix.cobol.reader.extractors.record

import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy.SchemaRetentionPolicy

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.reflect.ClassTag


object RecordExtractors {

  /**
    * This method extracts a record from the specified array of bytes. The copybook for the record needs to be already parsed.
    *
    * @param ast                    The parsed copybook.
    * @param data                   The data bits containing the record.
    * @param offsetBytes            The offset to the beginning of the record (in bits).
    * @param policy                 A schema retention policy to be applied to the extracted record.
    * @param variableLengthOccurs   If true, OCCURS DEPENDING ON data size will depend on the number of elements.
    * @param generateRecordId       If true, a record id field will be added as the first field of the record.
    * @param generateRecordBytes    If true, a record bytes field will be added at the beginning of each record.
    * @param segmentLevelIds        Segment ids to put to the extracted record if id generation it turned on.
    * @param fileId                 A file id to be put to the extractor record if generateRecordId == true.
    * @param recordId               The record id to be saved to the record id field.
    * @param activeSegmentRedefine  An active segment redefine (the one that will be parsed).
    *                               All other segment redefines will be skipped.
    * @param generateInputFileField if true, a field containing input file name will be generated
    * @param inputFileName          An input file name to put if its generation is needed
    * @return An Array[Any] object corresponding to the record schema.
    */
  @throws(classOf[IllegalStateException])
  def extractRecord[T: ClassTag](
    ast: CopybookAST,
    data: Array[Byte],
    offsetBytes: Int = 0,
    policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
    variableLengthOccurs: Boolean = false,
    generateRecordId: Boolean = false,
    generateRecordBytes: Boolean = false,
    segmentLevelIds: List[String] = Nil,
    fileId: Int = 0,
    recordId: Long = 0,
    activeSegmentRedefine: String = "",
    generateInputFileField: Boolean = false,
    inputFileName: String = "",
    handler: RecordHandler[T]
  ): Seq[Any] = {
    val dependFields = scala.collection.mutable.HashMap.empty[String, Either[Int, String]]

    val isAstFlat = ast.children.exists(_.isInstanceOf[Primitive])

    def extractArray(field: Statement, useOffset: Int): (Int, Array[Any]) = {
      val from = 0
      val arraySize = field.arrayMaxSize
      val actualSize = field.dependingOn match {
        case None => arraySize
        case Some(dependingOn) =>
          val dependValue: Int = dependFields.getOrElse(dependingOn, Left(arraySize)) match {
            case Left(n) => n
            case Right(s) => field.dependingOnHandlers.getOrElse(s, arraySize)
          }
          if (dependValue >= field.arrayMinSize && dependValue <= arraySize)
            dependValue
          else
            arraySize
      }

      var offset = useOffset
      val arr = field match {
        case grp: Group =>
          val groupValues = new Array[Any](actualSize - from)
          var i = from
          var j = 0
          while (i < actualSize) {
            val (size, value) = getGroupValues(offset, grp)
            offset += size
            groupValues(j) = value
            i += 1
            j += 1
          }
          groupValues
        case s: Primitive =>
          val values = new Array[Any](actualSize - from)
          var i = from
          var j = 0
          while (i < actualSize) {
            val value = s.decodeTypeValue(offset, data)
            offset += s.binaryProperties.dataSize
            values(j) = value
            i += 1
            j += 1
          }
          values
      }
      if (variableLengthOccurs) {
        (offset - useOffset, arr)
      } else {
        (field.binaryProperties.actualSize, arr)
      }
    }

    def extractValue(field: Statement, useOffset: Int): (Int, Any) = {
      field match {
        case grp: Group =>
          if (grp.isSegmentRedefine && grp.name.compareToIgnoreCase(activeSegmentRedefine) != 0) {
            (grp.binaryProperties.actualSize, null)
          } else {
            getGroupValues(useOffset, grp)
          }
        case st: Primitive =>
          val value = st.decodeTypeValue(useOffset, data)
          if (value != null && st.isDependee) {
            val intStringVal: Either[Int, String] = value match {
              case v: Int => Left(v)
              case v: Number => Left(v.intValue())
              case v: String => Right(v)
              case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
            }
            dependFields += st.name -> intStringVal
          }
          (st.binaryProperties.actualSize, value)
      }
    }

    def getGroupValues(offset: Int, group: Group): (Int, T) = {
      var bitOffset = offset

      val fields = new Array[Any](group.nonFillerSize)

      var j = 0
      var i = 0
      while (i < group.children.length) {
        val field = group.children(i)
        val fieldValue = if (field.isArray) {
          val (size, value) = extractArray(field, bitOffset)
          if (!field.isRedefined) {
            bitOffset += size
          }
          value
        } else {
          val (size, value) = extractValue(field, bitOffset)
          if (!field.isRedefined) {
            if (field.redefines.isDefined) {
              bitOffset += field.binaryProperties.actualSize
            } else {
              bitOffset += size
            }
          }
          value
        }
        if (!field.isFiller) {
          fields(j) = fieldValue
          j += 1
        }
        i += 1
      }
      (bitOffset - offset, handler.create(fields, group))
    }

    var nextOffset = offsetBytes

    val rootRecords: scala.collection.Seq[Statement] = if (isAstFlat) {
      Seq(ast)
    } else {
      ast.children
    }

    val records: ListBuffer[T] = ListBuffer.empty[T]

    for (record <- rootRecords) yield {
      val (size, values) = getGroupValues(nextOffset, record.asInstanceOf[Group])
      if (!record.isRedefined) {
        nextOffset += size
      }
      records += values
    }

    val effectiveSchemaRetentionPolicy = if (isAstFlat) {
      SchemaRetentionPolicy.CollapseRoot
    } else {
      policy
    }

    applyRecordPostProcessing(ast, records.toList, effectiveSchemaRetentionPolicy, generateRecordId, generateRecordBytes, segmentLevelIds, fileId, recordId, data.length, data, generateInputFileField, inputFileName, handler)
  }

  /**
    * This method extracts a hierarchical record from the specified raw bytes.
    * The copybook for the record needs to be already parsed.
    *
    * This extractor expects multiple segments to be provided as a list of a segmentId-data pair.
    * Raw data for each segment should be provided as array of bytes.
    *
    * This method reconstructs hierarchical record structure by putting all provided segments in their
    * corresponding places in the hierarchy.
    *
    * @param ast                  The parsed copybook
    * @param segmentsData         The data bits containing the record
    * @param segmentRedefines     A list of segment redefine GROUPs
    * @param segmentIdRedefineMap A mapping from segment ids to segment redefine groups
    * @param parentChildMap       A mapping from a segment field name to its parents
    * @param offsetBytes          The offset to the beginning of the record (in bits)
    * @param policy               A schema retention policy to be applied to the extracted record
    * @param variableLengthOccurs If true, OCCURS DEPENDING ON data size will depend on the number of elements
    * @param generateRecordId     If true a record id field will be added as the first field of the record.
    * @param fileId               A file id to be put to the extractor record if generateRecordId == true
    * @param recordId             The record id to be saved to the record id field
    * @param generateInputFileField if true, a field containing input file name will be generated
    * @param inputFileName          An input file name to put if its generation is needed
    * @return An Array[Any] object corresponding to the hierarchical record schema
    */
  @throws(classOf[IllegalStateException])
  def extractHierarchicalRecord[T: ClassTag](
      ast: CopybookAST,
      segmentsData: ArrayBuffer[(String, Array[Byte])],
      segmentRedefines: Array[Group],
      segmentIdRedefineMap: Map[String, Group],
      parentChildMap: Map[String, Seq[Group]],
      offsetBytes: Int = 0,
      policy: SchemaRetentionPolicy = SchemaRetentionPolicy.KeepOriginal,
      variableLengthOccurs: Boolean = false,
      generateRecordId: Boolean = false,
      fileId: Int = 0,
      recordId: Long = 0,
      generateInputFileField: Boolean = false,
      inputFileName: String = "",
      handler: RecordHandler[T]
  ): Seq[Any] = {
    val isAstFlat = ast.children.exists(_.isInstanceOf[Primitive])

    val dependFields = scala.collection.mutable.HashMap.empty[String, Either[Int, String]]

    def extractArray(field: Statement, useOffset: Int, data: Array[Byte], currentIndex: Int, parentSegmentIds: List[String]): (Int, Array[Any]) = {
      val from = 0
      val arraySize = field.arrayMaxSize
      val actualSize = field.dependingOn match {
        case None => arraySize
        case Some(dependingOn) =>
          val dependValue: Int = dependFields.getOrElse(dependingOn, Left(arraySize)) match {
            case Left(n) => n
            case Right(s) => field.dependingOnHandlers.getOrElse(s, arraySize)
          }
          if (dependValue >= field.arrayMinSize && dependValue <= arraySize)
            dependValue
          else
            arraySize
      }

      var offset = useOffset
      val arr = field match {
        case grp: Group =>
          val groupValues = new Array[Any](actualSize - from)
          var i = from
          var j = 0
          while (i < actualSize) {
            val (size, value) = getGroupValues(offset, grp, data, currentIndex, parentSegmentIds)
            offset += size
            groupValues(j) = value
            i += 1
            j += 1
          }
          groupValues
        case s: Primitive =>
          val values = new Array[Any](actualSize - from)
          var i = from
          var j = 0
          while (i < actualSize) {
            val value = s.decodeTypeValue(offset, data)
            offset += s.binaryProperties.dataSize
            values(j) = value
            i += 1
            j += 1
          }
          values
      }
      if (variableLengthOccurs) {
        (offset - useOffset, arr)
      } else {
        (field.binaryProperties.actualSize, arr)
      }
    }

    def extractValue(field: Statement, useOffset: Int, data: Array[Byte], currentIndex: Int, parentSegmentIds: List[String]): (Int, Any) = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp, data, currentIndex, parentSegmentIds)
        case st: Primitive =>
          val value = st.decodeTypeValue(useOffset, data)
          if (value != null && st.isDependee) {
            val intStringVal: Either[Int, String] = value match {
              case v: Int => Left(v)
              case v: Number => Left(v.intValue())
              case v: String => Right(v)
              case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
            }
            dependFields += st.name -> intStringVal
          }
          (st.binaryProperties.actualSize, value)
      }
    }

    def extractChildren(field: Group, currentIndex: Int, parentSegmentIds: List[String]): Any = {
      val children = new ArrayBuffer[T]()

      val segmentCount = segmentsData.size

      var i = currentIndex
      var break = false

      while (i < segmentCount && !break) {
        segmentsData(i) match {
          case (segmentId, segmentData) =>
            if (segmentIdRedefineMap.get(segmentId).map(_.name).getOrElse("") == field.name) {
              val (_, child) = getGroupValues(field.binaryProperties.offset, field, segmentData, i, segmentId :: parentSegmentIds)
              children += child
            } else {
              if (parentSegmentIds.contains(segmentId)) {
                break = true
              }
            }
        }
        i = i + 1
      }

      children.toArray
    }

    def getGroupValues(offset: Int, group: Group, data: Array[Byte], currentIndex: Int, parentSegmentIds: List[String]): (Int, T) = {
      var bitOffset = offset

      val childrenNum = if (group.isSegmentRedefine) {
        parentChildMap(group.name).size
      } else {
        0
      }

      val fields = new Array[Any](group.nonFillerSize + childrenNum)

      var j = 0
      var i = 0
      while (i < group.children.length) {
        val field = group.children(i)
        val fieldValue = if (field.isArray) {
          val (size, value) = extractArray(field, bitOffset, data, currentIndex, parentSegmentIds)
          if (!field.isRedefined) {
            bitOffset += size
          }
          value
        } else {
          val (size, value) = extractValue(field, bitOffset, data, currentIndex, parentSegmentIds)
          if (!field.isRedefined) {
            if (field.redefines.isDefined) {
              bitOffset += field.binaryProperties.actualSize
            } else {
              bitOffset += size
            }
          }
          value
        }
        if (!field.isFiller && !field.isChildSegment) {
          fields(j) = fieldValue
          j += 1
        }
        i += 1
      }

      // Add children
      if (group.isSegmentRedefine) {
        parentChildMap.get(group.name).foreach(children => {
          children.foreach(child => {
            fields(j) = extractChildren(child, currentIndex + 1, parentSegmentIds)
            j += 1
          })
        })
      }

      (bitOffset - offset, handler.create(fields, group))
    }

    var nextOffset = offsetBytes

    val rootRecords: scala.collection.Seq[Statement] = if (isAstFlat) {
      Seq(ast)
    } else {
      ast.children
    }

    val records: ListBuffer[T] = ListBuffer.empty[T]

    rootRecords.collect { case grp: Group if grp.parentSegment.isEmpty =>
      val (size, values) = getGroupValues(nextOffset, grp, segmentsData(0)._2, 0, segmentsData(0)._1 :: Nil)
      nextOffset += size
      records += values
    }

    val recordLength = segmentsData.map(_._2.length).sum

    val effectiveSchemaRetentionPolicy = if (isAstFlat) {
      SchemaRetentionPolicy.CollapseRoot
    } else {
      policy
    }

    applyRecordPostProcessing(ast, records.toList, effectiveSchemaRetentionPolicy, generateRecordId, generateRecordBytes = false, Nil, fileId, recordId, recordLength, Array.empty[Byte], generateInputFileField = generateInputFileField, inputFileName, handler)
  }

  /**
    * <p>This method applies additional postprocessing to the schema obtained from a copybook to make it easier to use as a Spark Schema.</p>
    *
    * <p>The following transofmations will currently be applied:
    * <ul>
    * <li>If `generateRecordId == true` the record id field will be prepended to the row.</li>
    * <li>If `generateInputFileField == true` the input file name be prepended to the row right after record ids.</li>
    * <li>If the schema has only one root StructType element, the element will be expanded. The resulting schema will contain only the children fields of
    * the element.</li>
    * </ul>
    * Combinations of the listed transformations are supported.
    * </p>
    *
    * @param ast                    The parsed copybook
    * @param records                The array of [[T]] object for each Group of the copybook
    * @param generateRecordId       If true a record id field will be added as the first field of the record.
    * @param generateRecordBytes    If true a record bytes field will be added at the beginning of the record.
    * @param fileId                 The file id to be saved to the file id field
    * @param recordId               The record id to be saved to the record id field
    * @param recordByteLength       The length of the record
    * @param generateInputFileField if true, a field containing input file name will be generated
    * @param inputFileName          An input file name to put if its generation is needed
    * @return A [[T]] object corresponding to the record schema
    */
  private def applyRecordPostProcessing[T](
    ast: CopybookAST,
    records: List[T],
    policy: SchemaRetentionPolicy,
    generateRecordId: Boolean,
    generateRecordBytes: Boolean,
    segmentLevelIds: List[String],
    fileId: Int,
    recordId: Long,
    recordByteLength: Int,
    recordBytes: Array[Byte],
    generateInputFileField: Boolean,
    inputFileName: String,
    handler: RecordHandler[T]
  ): Seq[Any] = {
    val generatedFields = new ListBuffer[Any]

    if (generateRecordId) {
      generatedFields.append(fileId, recordId, recordByteLength)
    }

    if (generateRecordBytes) {
      generatedFields.append(recordBytes)
    }

    if (generateInputFileField) {
      generatedFields.append(inputFileName)
    }

    segmentLevelIds.foreach(generatedFields.append(_))

    policy match {
      case SchemaRetentionPolicy.CollapseRoot =>
        // If the policy for schema retention is root collapsing, expand root fields
        generatedFields ++ records.flatMap(record => handler.toSeq(record))
      case SchemaRetentionPolicy.KeepOriginal =>
        // Return rows as the original sequence of groups
        generatedFields ++ records
    }
  }
}
