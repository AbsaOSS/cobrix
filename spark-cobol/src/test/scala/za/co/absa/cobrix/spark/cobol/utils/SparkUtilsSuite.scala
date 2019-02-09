/*
 * Copyright 2018-2019 ABSA Group Limited
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

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import org.apache.spark.sql.functions._

class SparkUtilsSuite extends FunSuite with SparkTestBase {
  import spark.implicits._

  val nestedSampleData: List[String] = """[{"id":1,"legs":[{"legid":100,"conditions":[{"checks":[{"checkNums":["1","2","3b","4","5c","6"]}],"amount":100}]}]}]""" ::
    """[{"id":2,"legs":[{"legid":200,"conditions":[{"checks":[{"checkNums":["3","4","5b","6","7c","8"]}],"amount":200}]}]}]""" ::
    """[{"id":3,"legs":[{"legid":300,"conditions":[{"checks":[{"checkNums":["6","7","8b","9","0c","1"]}],"amount":300}]}]}]""" ::
    """[{"id":4,"legs":[]}]""" ::
    """[{"id":5,"legs":null}]""" :: Nil

  test("Test schema flattening of multiple nested structure") {
    val df = spark.read.json(nestedSampleData.toDS)

    df.printSchema()

    val df2 = SparkUtils.flattenSchema(df, false)

    df2.printSchema()

    df2.show(false)
  }

}
