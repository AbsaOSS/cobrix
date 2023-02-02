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

package za.co.absa.cobrix.spark.cobol

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase

class CobrixSpec extends AnyWordSpec with SparkTestBase {
  import spark.implicits._

  "fromRdd" should {
    "read an RDD of strings" in {
      val copyBook: String =
        """       01  RECORD.
          |         05  FIELD1                  PIC X(2).
          |         05  FIELD2                  PIC X(1).
          |""".stripMargin

      val rdd = spark.sparkContext.parallelize(Seq("ЇBC", "DEF", "GHI"))

      val df = Cobrix.fromRdd
        .copybookContents(copyBook)
        //.option("schema_retention_policy", "keep_original")
        .loadText(rdd)

      df.show()
    }

    "read an RDD of byte arrays" in {
      val copyBook: String =
        """       01  RECORD.
          |         05  FIELD1                  PIC X(2).
          |         05  FIELD2                  PIC X(1).
          |""".stripMargin

      val rdd = spark.sparkContext.parallelize(Seq("ЇBC", "DEF", "GHI")).map(_.getBytes)

      val df = Cobrix.fromRdd
        .copybookContents(copyBook)
        .option("encoding", "ebcdic")
        .load(rdd)

      df.show()
    }
  }
}
