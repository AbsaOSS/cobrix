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

package za.co.absa.cobrix.spark.cobol.utils.impl

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.{transform => sparkTransform}

object HofsWrapper {
  /**
    * Applies the function `f` to every element in the `array`. The method is an equivalent to the `map` function
    * from functional programming.
    *
    * (The idea comes from https://github.com/AbsaOSS/spark-hats/blob/v0.3.0/src/main/scala_2.12/za/co/absa/spark/hats/HofsWrapper.scala)
    *
    * @param array       A column of arrays
    * @param f           A function transforming individual elements of the array
    * @return A column of arrays with transformed elements
    */
  def transform(
                 array: Column,
                 f: Column => Column): Column = {
    sparkTransform(array, f)
  }
}
