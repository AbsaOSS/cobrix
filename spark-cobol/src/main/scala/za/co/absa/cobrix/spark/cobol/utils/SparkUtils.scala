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

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions.{concat_ws, expr, max}
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}

import scala.collection.mutable

/**
  * This object contains common Spark tools used for easier processing of dataframes originated from mainframes.
  */
object SparkUtils {

  /**
    * Given an instance of [[DataFrame]] returns a dataframe with flttened schema.
    * All nested structures are flattened and arrays are projected as columns.
    *
    * Note. The method checks the maximum size for each array and that could perform slowly,
    * especially on a vary big dataframes.
    *
    * @param df A dataframe
    * @return A new dataframe with flat schema.
    */
  def flattenSchema(df: DataFrame): DataFrame = {
    val fields = new mutable.ListBuffer[Column]()
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

    def flattenStructArray(path: String, structField: StructField, arrayType: ArrayType): Unit = {
      val maxInd = df.agg(max(expr(s"size($path${structField.name})"))).collect()(0)(0).toString.toInt
      var i = 0
      while (i < maxInd) {
        arrayType.elementType match {
          case st: StructType =>
            flattenGroup(s"$path${structField.name}[$i].", st)
          case _ =>
        }
        i += 1
      }
    }

    def flattenArray(path: String, structField: StructField, arrayType: ArrayType): Unit = {
      arrayType.elementType match {
        case st: StructType =>
          flattenStructArray(path, structField, arrayType)
        case fld =>
          fields += concat_ws(" ", expr(s"$path${structField.name}")).as(getNewFieldName(s"${structField.name}"))
      }
    }

    def flattenGroup(path: String, structField: StructType): Unit = {
      structField.foreach(field => {
        field.dataType match {
          case st: StructType =>
            flattenGroup(s"$path${field.name}.", st)
          case arr: ArrayType =>
            flattenArray(path, field, arr)
          case fld =>
            fields += expr(s"$path${field.name}").as(getNewFieldName(field.name)).cast(StringType)
        }
      })
    }

    flattenGroup("", df.schema)
    df.select(fields: _*)
  }


  /**
    * Given an instance of [[DataFrame]] returns a dataframe where all promitive fields are converted to String
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
          fields += expr(s"$path${structField.name}")
        case fld =>
          fields += expr(s"$path${structField.name}").cast(ArrayType(StringType))
      }
    }

    def convertToStrings(path: String, structField: StructType): Unit = {
      structField.foreach(field => {
        field.dataType match {
          case st: StructType =>
            convertToStrings(s"$path${field.name}.", st)
          case arr: ArrayType =>
            convertArrayToStrings(path, field, arr)
          case fld =>
            fields += expr(s"$path${field.name}").cast(StringType)
        }
      })
    }

    convertToStrings("", df.schema)
    df.select(fields: _*)
  }
}
