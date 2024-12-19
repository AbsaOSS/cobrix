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

package za.co.absa.cobrix.spark.cobol.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions.{array, col, expr, max, struct}
import za.co.absa.cobrix.spark.cobol.utils.impl.HofsWrapper.transform
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.spark.cobol.parameters.MetadataFields.MAX_ELEMENTS

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
  * This object contains common Spark tools used for easier processing of dataframes originated from mainframes.
  */
object SparkUtils extends Logging {

  /**
    * Retrieves all executors available for the current job.
    */
  def currentActiveExecutors(sc: SparkContext): Seq[String] = {
    val allExecutors = sc.getExecutorMemoryStatus.map(_._1.split(":").head)
    val driverHost: String = sc.getConf.get("spark.driver.host", "localhost")

    logger.info(s"Going to filter driver from available executors: Driver host: $driverHost, Available executors: $allExecutors")

    allExecutors.filter(!_.equals(driverHost)).toList.distinct
  }

  /**
    * Returns true if Spark Data type is a primitive data type.
    *
    * @param dataType Stark data type
    * @return true if the data type is primitive.
    */
  def isPrimitive(dataType: DataType): Boolean = {
    dataType match {
      case _: ArrayType => false
      case _: StructType => false
      case _: MapType => false
      case _ => true
    }
  }

  /**
    * Given an instance of DataFrame returns a dataframe with flattened schema.
    * All nested structures are flattened and arrays are projected as columns.
    *
    * Note. The method checks the maximum size for each array and that could perform slowly,
    * especially on a vary big dataframes.
    *
    * @param df                 A dataframe
    * @param useShortFieldNames When flattening a schema each field name will contain full path. You can override this
    *                           behavior and use a short field names instead
    * @return A new dataframe with flat schema.
    */
  def flattenSchema(df: DataFrame, useShortFieldNames: Boolean = false): DataFrame = {
    val fields = new mutable.ListBuffer[Column]()
    val stringFields = new mutable.ListBuffer[String]()
    val usedNames = new mutable.HashSet[String]()

    def getNewFieldName(desiredName: String): String = {
      var name = desiredName
      var i = 1
      while (usedNames.contains(name)) {
        name = s"$desiredName$i"
        i += 1
      }
      usedNames.add(name)
      name
    }

    /**
      * Aggregating arrays of primitives by projecting it's columns
      *
      * @param path            path to an StructArray
      * @param fieldNamePrefix Prefix for the field name
      * @param structField     StructField
      * @param arrayType       ArrayType
      */
    def flattenStructArray(path: String, fieldNamePrefix: String, structField: StructField, arrayType: ArrayType): Unit = {
      val maxInd = getMaxArraySize(s"$path${structField.name}")
      var i = 0
      while (i < maxInd) {
        arrayType.elementType match {
          case st: StructType =>
            val newFieldNamePrefix = s"${fieldNamePrefix}${i}_"
            flattenGroup(s"$path`${structField.name}`[$i].", newFieldNamePrefix, st)
          case ar: ArrayType =>
            val newFieldNamePrefix = s"${fieldNamePrefix}${i}_"
            flattenArray(s"$path`${structField.name}`[$i].", newFieldNamePrefix, structField, ar)
          // AtomicType is protected on package 'sql' level so have to enumerate all subtypes :(
          case _ =>
            val newFieldNamePrefix = s"${fieldNamePrefix}${i}"
            val newFieldName = getNewFieldName(s"$newFieldNamePrefix")
            fields += expr(s"$path`${structField.name}`[$i]").as(newFieldName, structField.metadata)
            stringFields += s"""expr("$path`${structField.name}`[$i] AS `$newFieldName`")"""
        }
        i += 1
      }
    }

    def flattenNestedArrays(path: String, fieldNamePrefix: String, arrayType: ArrayType, metadata: Metadata): Unit = {
      val maxInd = getMaxArraySize(path)
      var i = 0
      while (i < maxInd) {
        arrayType.elementType match {
          case st: StructType =>
            val newFieldNamePrefix = s"${fieldNamePrefix}${i}_"
            flattenGroup(s"$path[$i]", newFieldNamePrefix, st)
          case ar: ArrayType =>
            val newFieldNamePrefix = s"${fieldNamePrefix}${i}_"
            flattenNestedArrays(s"$path[$i]", newFieldNamePrefix, ar, metadata)
          // AtomicType is protected on package 'sql' level so have to enumerate all subtypes :(
          case _ =>
            val newFieldNamePrefix = s"${fieldNamePrefix}${i}"
            val newFieldName = getNewFieldName(s"$newFieldNamePrefix")
            fields += expr(s"$path[$i]").as(newFieldName, metadata)
            stringFields += s"""expr("$path`[$i] AS `$newFieldName`")"""
        }
        i += 1
      }
    }

    def getMaxArraySize(path: String): Int = {
      getField(path, df.schema) match {
        case Some(field) if field.metadata.contains(MAX_ELEMENTS) =>
          field.metadata.getLong(MAX_ELEMENTS).toInt
        case _ =>
          val collected = df.agg(max(expr(s"size($path)"))).collect()(0)(0)
          if (collected != null) {
            // can be null for empty dataframe
            collected.toString.toInt
          } else {
            1
          }
      }
    }

    def flattenArray(path: String, fieldNamePrefix: String, structField: StructField, arrayType: ArrayType): Unit = {
      arrayType.elementType match {
        case _: ArrayType =>
          flattenNestedArrays(s"$path${structField.name}", fieldNamePrefix, arrayType, structField.metadata)
        case _ =>
          flattenStructArray(path, fieldNamePrefix, structField, arrayType)
      }
    }

    def flattenGroup(path: String, fieldNamePrefix: String, structField: StructType): Unit = {
      structField.foreach(field => {
        val newFieldNamePrefix = if (useShortFieldNames) {
          s"${field.name}_"
        } else {
          s"$fieldNamePrefix${field.name}_"
        }
        field.dataType match {
          case st: StructType =>
            flattenGroup(s"$path`${field.name}`.", newFieldNamePrefix, st)
          case arr: ArrayType =>
            flattenArray(path, newFieldNamePrefix, field, arr)
          case _ =>
            val newFieldName = getNewFieldName(s"$fieldNamePrefix${field.name}")
            fields += expr(s"$path`${field.name}`").as(newFieldName, field.metadata)
            if (path.contains('['))
              stringFields += s"""expr("$path`${field.name}` AS `$newFieldName`")"""
            else
              stringFields += s"""col("$path`${field.name}`").as("$newFieldName")"""
        }
      })
    }

    flattenGroup("", "", df.schema)
    logger.info(stringFields.mkString("Flattening code: \n.select(\n", ",\n", "\n)"))
    df.select(fields.toSeq: _*)
  }

  /**
    * Removes all struct nesting when possible for a given schema.
    */
  def unstructSchema(schema: StructType, useShortFieldNames: Boolean = false): StructType = {
    def mapFieldShort(field: StructField): Array[StructField] = {
      field.dataType match {
        case st: StructType =>
          st.fields flatMap mapFieldShort
        case _ =>
          Array(field)
      }
    }

    def mapFieldLong(field: StructField, path: String): Array[StructField] = {
      field.dataType match {
        case st: StructType =>
          st.fields.flatMap(f => mapFieldLong(f, s"$path${field.name}_"))
        case _ =>
          Array(field.copy(name = s"$path${field.name}"))
      }
    }

    val fields = if (useShortFieldNames)
      schema.fields flatMap mapFieldShort
    else
      schema.fields.flatMap(f => mapFieldLong(f, ""))

    StructType(fields)
  }

  /**
    * Removes all struct nesting when possible for a given dataframe.
    *
    * Similar to `flattenSchema()`, but does not flatten arrays.
    */
  def unstructDataFrame(df: DataFrame, useShortFieldNames: Boolean = false): DataFrame = {
    def mapFieldShort(column: Column, field: StructField): Array[Column] = {
      field.dataType match {
        case st: StructType =>
          st.fields.flatMap(f => mapFieldShort(column.getField(f.name), f))
        case _ =>
          Array(column.as(field.name, field.metadata))
      }
    }

    def mapFieldLong(column: Column, field: StructField, path: String): Array[Column] = {
      field.dataType match {
        case st: StructType =>
          st.fields.flatMap(f => mapFieldLong(column.getField(f.name), f, s"$path${field.name}_"))
        case _ =>
          Array(column.as(s"$path${field.name}", field.metadata))
      }
    }

    val columns = if (useShortFieldNames)
      df.schema.fields.flatMap(f => mapFieldShort(col(f.name), f))
    else
      df.schema.fields.flatMap(f => mapFieldLong(col(f.name), f, ""))
    df.select(columns: _*)
  }

  /**
    * Copies metadata from one schema to another as long as names and data types are the same.
    *
    * @param schemaFrom      Schema to copy metadata from.
    * @param schemaTo        Schema to copy metadata to.
    * @param overwrite       If true, the metadata of schemaTo is not retained
    * @param sourcePreferred If true, schemaFrom metadata is used on conflicts, schemaTo otherwise.
    * @param copyDataType    If true, data type is copied as well. This is limited to primitive data types.
    * @return Same schema as schemaTo with metadata from schemaFrom.
    */
  def copyMetadata(schemaFrom: StructType,
                   schemaTo: StructType,
                   overwrite: Boolean = false,
                   sourcePreferred: Boolean = false,
                   copyDataType: Boolean = false): StructType = {
    def joinMetadata(from: Metadata, to: Metadata): Metadata = {
      val newMetadataMerged = new MetadataBuilder

      if (sourcePreferred) {
        newMetadataMerged.withMetadata(to)
        newMetadataMerged.withMetadata(from)
      } else {
        newMetadataMerged.withMetadata(from)
        newMetadataMerged.withMetadata(to)
      }

      newMetadataMerged.build()
    }

    @tailrec
    def processArray(ar: ArrayType, fieldFrom: StructField, fieldTo: StructField): ArrayType = {
      ar.elementType match {
        case st: StructType if fieldFrom.dataType.isInstanceOf[ArrayType] && fieldFrom.dataType.asInstanceOf[ArrayType].elementType.isInstanceOf[StructType] =>
          val innerStructFrom = fieldFrom.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType]
          val newDataType = StructType(copyMetadata(innerStructFrom, st, overwrite, sourcePreferred, copyDataType).fields)
          ArrayType(newDataType, ar.containsNull)
        case at: ArrayType =>
          processArray(at, fieldFrom, fieldTo)
        case p =>
          if (copyDataType && fieldFrom.dataType.isInstanceOf[ArrayType] && isPrimitive(fieldFrom.dataType.asInstanceOf[ArrayType].elementType)) {
            ArrayType(fieldFrom.dataType.asInstanceOf[ArrayType].elementType, ar.containsNull)
          } else {
            ArrayType(p, ar.containsNull)
          }
      }
    }

    val fieldsMap = schemaFrom.fields.map(f => (f.name, f)).toMap

    val newFields: Array[StructField] = schemaTo.fields.map { fieldTo =>
      fieldsMap.get(fieldTo.name) match {
        case Some(fieldFrom) =>
          val newMetadata = if (overwrite) {
            fieldFrom.metadata
          } else {
            joinMetadata(fieldFrom.metadata, fieldTo.metadata)
          }

          fieldTo.dataType match {
            case st: StructType if fieldFrom.dataType.isInstanceOf[StructType] =>
              val newDataType = StructType(copyMetadata(fieldFrom.dataType.asInstanceOf[StructType], st, overwrite, sourcePreferred, copyDataType).fields)
              fieldTo.copy(dataType = newDataType, metadata = newMetadata)
            case at: ArrayType =>
              val newType = processArray(at, fieldFrom, fieldTo)
              fieldTo.copy(dataType = newType, metadata = newMetadata)
            case _ =>
              if (copyDataType && isPrimitive(fieldFrom.dataType)) {
                fieldTo.copy(dataType = fieldFrom.dataType, metadata = newMetadata)
              } else {
                fieldTo.copy(metadata = newMetadata)
              }
          }
        case None =>
          fieldTo
      }
    }

    StructType(newFields)
  }

  /**
    * Allows mapping every primitive field in a dataframe with a Spark expression.
    *
    * The metadata of the original schema is retained.
    *
    * @param df The dataframe to map.
    * @param f  The function to apply to each primitive field.
    * @return The new dataframe with the mapping applied.
    */
  def mapPrimitives(df: DataFrame)(f: (StructField, Column) => Column): DataFrame = {
    def mapField(column: Column, field: StructField): Column = {
      field.dataType match {
        case st: StructType =>
          val columns = st.fields.map(f => mapField(column.getField(f.name), f))
          struct(columns: _*).as(field.name, field.metadata)
        case ar: ArrayType =>
          mapArray(ar, column, field.name).as(field.name, field.metadata)
        case _ =>
          f(field, column).as(field.name, field.metadata)
      }
    }

    def mapArray(arr: ArrayType, column: Column, columnName: String): Column = {
      arr.elementType match {
        case st: StructType =>
          transform(column, c => {
            val columns = st.fields.map(f => mapField(c.getField(f.name), f))
            struct(columns: _*)
          })
        case ar: ArrayType =>
          array(mapArray(ar, column, columnName))
        case p =>
          array(f(StructField(columnName, p), column))
      }
    }

    val columns = df.schema.fields.map(f => mapField(col(f.name), f))
    val newDf = df.select(columns: _*)
    val newSchema = copyMetadata(df.schema, newDf.schema)

    df.sparkSession.createDataFrame(newDf.rdd, newSchema)
  }

  def covertIntegralToDecimal(df: DataFrame): DataFrame = {
    mapPrimitives(df) { (field, c) =>
      val metadata = field.metadata
      if (metadata.contains("precision") && (field.dataType == LongType || field.dataType == IntegerType || field.dataType == ShortType)) {
        val precision = metadata.getLong("precision").toInt
        c.cast(DecimalType(precision, 0)).as(field.name)
      } else {
        c
      }
    }
  }

  /**
    * Given an instance of DataFrame returns a dataframe where all primitive fields are converted to String
    *
    * @param df A dataframe
    * @return A new dataframe with all primitive fields as Strings.
    */
  def convertDataframeFieldsToStrings(df: DataFrame): DataFrame = {
    val fields = new mutable.ListBuffer[Column]()

    def convertArrayToStrings(path: String, structField: StructField, arrayType: ArrayType): Unit = {
      arrayType.elementType match {
        case st: StructType =>
          // ToDo convert array's inner struct fields to Strings.
          // Possibly Spark 2.4 array transform API could be used for that.
          fields += expr(s"$path`${structField.name}`")
        case fld =>
          fields += expr(s"$path`${structField.name}`").cast(ArrayType(StringType))
      }
    }

    def convertToStrings(path: String, structField: StructType): Unit = {
      structField.foreach(field => {
        field.dataType match {
          case st: StructType =>
            convertToStrings(s"$path`${field.name}`.", st)
          case arr: ArrayType =>
            convertArrayToStrings(path, field, arr)
          case fld =>
            fields += expr(s"$path`${field.name}`").cast(StringType)
        }
      })
    }

    convertToStrings("", df.schema)
    df.select(fields.toSeq: _*)
  }


  def convertDataFrameToPrettyJSON(df: DataFrame, takeN: Int = 0): String = {
    val collected = if (takeN <= 0) {
      df.toJSON.collect().mkString("\n")
    } else {
      df.toJSON.take(takeN).mkString("\n")
    }

    val json = "[" + "}\n".r.replaceAllIn(collected, "},\n") + "]"

    prettyJSON(json)
  }

  def prettyJSON(jsonIn: String): String = {
    val mapper = new ObjectMapper()

    val jsonUnindented = mapper.readValue(jsonIn, classOf[Any])
    val indented = mapper.writerWithDefaultPrettyPrinter.writeValueAsString(jsonUnindented)
    indented.replace("\r\n", "\n")
  }

  /**
    * Get a Spark field from a text path and a given schema
    * (originally implemented here: https://github.com/AbsaOSS/enceladus/blob/665b34fa1c04fe255729e4b6706cf9ea33227b3e/utils/src/main/scala/za/co/absa/enceladus/utils/schema/SchemaUtils.scala#L45)
    *
    * @param path   The dot-separated path to the field
    * @param schema The schema which should contain the specified path
    * @return Some(the requested field) or None if the field does not exist
    */
  def getField(path: String, schema: StructType): Option[StructField] = {
    @tailrec
    def goThroughArrayDataType(dataType: DataType): DataType = {
      dataType match {
        case ArrayType(dt, _) => goThroughArrayDataType(dt)
        case result => result
      }
    }

    @tailrec
    def examineStructField(names: List[String], structField: StructField): Option[StructField] = {
      if (names.isEmpty) {
        Option(structField)
      } else {
        structField.dataType match {
          case struct: StructType => examineStructField(names.tail, struct(names.head))
          case ArrayType(el: DataType, _) =>
            goThroughArrayDataType(el) match {
              case struct: StructType => examineStructField(names.tail, struct(names.head))
              case _ => None
            }
          case _ => None
        }
      }
    }

    val pathTokens = splitFieldPath(path)
    Try {
      examineStructField(pathTokens.tail, schema(pathTokens.head))
    }.getOrElse(None)
  }

  def getDefaultHdfsBlockSize(spark: SparkSession, pathOpt: Option[String]): Option[Int] = {
    val conf = spark.sparkContext.hadoopConfiguration

    val fileSystem  =pathOpt match {
      case Some(pathStr) => new Path(pathStr).getFileSystem(conf)
      case None => FileSystem.get(conf)
    }

    val hdfsBlockSize = HDFSUtils.getHDFSDefaultBlockSizeMB(fileSystem)
    hdfsBlockSize match {
      case None => logger.info(s"Unable to get default block size for '${fileSystem.getScheme}://.")
      case Some(size) => logger.info(s"Default block size for '${fileSystem.getScheme}://' is $size MB.")
    }

    hdfsBlockSize
  }

  private def splitFieldPath(path: String): List[String] = {
    var state = 0

    var currentField = new StringBuilder()
    val fields = new ListBuffer[String]()

    var i = 0
    while (i < path.length) {
      val c = path(i)

      state match {
        case 0 =>
          // The character might be part of the path
          if (c == '.') {
            fields.append(currentField.toString())
            currentField = new StringBuilder()
          } else if (c == '`') {
            state = 1
          } else if (c == '[') {
            state = 2
          } else {
            currentField.append(c)
          }
        case 1 =>
          // The character is part of the backquoted field name
          if (c == '`') {
            state = 0
          } else {
            currentField.append(c)
          }
        case 2 =>
          // The character is an index (that should be ignored)
          if (c == ']') {
            state = 0
          }
      }
      i += 1
    }
    if (currentField.nonEmpty) {
      fields.append(currentField.toString())
    }
    fields.toList
  }


}
