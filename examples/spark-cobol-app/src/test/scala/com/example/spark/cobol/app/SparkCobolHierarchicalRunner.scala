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

package com.example.spark.cobol.app

import com.example.spark.cobol.utils.SparkLocalMaster
import com.example.spark.cobol.utils.SparkJobRunHelper
import org.scalatest.funsuite.AnyFunSuite

class SparkCobolHierarchicalRunner extends AnyFunSuite
  with SparkJobRunHelper
  with SparkLocalMaster {
      runSparkJobAsTest[SparkCobolHierarchical.type]
  }

