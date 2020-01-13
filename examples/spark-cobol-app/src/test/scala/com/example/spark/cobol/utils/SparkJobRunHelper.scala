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

package com.example.spark.cobol.utils

import org.scalatest.FunSuiteLike
import scala.reflect.ClassTag
import scala.reflect.runtime.universe

trait SparkJobRunHelper {
  this: FunSuiteLike =>

  private def runSparkJob[T](implicit ct: ClassTag[T]): Unit = {
    type MainClass = {def main(args: Array[String]): Unit}

    val jobClass = ct.runtimeClass
    val jobClassSymbol = universe runtimeMirror jobClass.getClassLoader classSymbol jobClass

    val jobInstance =
      if (jobClassSymbol.isModuleClass)
        jobClass.getField("MODULE$").get(jobClass)
      else
        jobClass.newInstance()

    jobInstance.asInstanceOf[MainClass].main(Array.empty)
  }

  def runSparkJobAsTest[T](implicit ct: ClassTag[T]): Unit = {
    val simpleName = ct.runtimeClass.getSimpleName
    test(simpleName)(runSparkJob[T](ct))
  }

}
