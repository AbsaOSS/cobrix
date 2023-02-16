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

package za.co.absa.cobrix.spark.cobol.source.integration

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.Cobrix
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

import java.nio.charset.Charset
import scala.collection.JavaConverters.mapAsScalaMapConverter

class Test34FieldCodePages extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private val copybook =
    """         01 RECORD.
        02 FIELD-1 PIC X(1).
        02 FIELD-2 PIC X(1).
        02 FIELD-3 PIC X(1).
        02 FIELD-4 PIC X(1).
    """

  "multi code page data" should {
    "work for EBCDIC RDDs" in {
      val expected = """[{"FIELD_1":"ï","FIELD_2":"ï","FIELD_3":"ž","FIELD_4":""}]"""
      val data = Array(Array(87, 87, 182, 182).map(_.toByte))

      val rdd = spark.sparkContext.parallelize(data)

      val df = Cobrix.fromRdd
        .copybookContents(copybook)
        .option("encoding", "ebcdic")
        .option("field_code_page:cp037", "FIELD-1,FIELD_2")
        .option("field_code_page:cp870", " FIELD-3 ")
        .load(rdd)

      val actual = df.toJSON.collect().mkString("[", ",", "]")

      assert(actual == expected)
    }
    "work for EBCDIC files" in {
      val expected = """[{"FIELD_1":"ï","FIELD_2":"ï","FIELD_3":"ž","FIELD_4":""}]"""
      val data = Array(Array(87, 87, 182, 182).map(_.toByte))

      withTempBinFile("multicodepage", ".dat", data.head) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ebcdic")
          .option("field_code_page:cp037", "FIELD-1,FIELD_2")
          .option("field_code_page:cp870", " FIELD-3 ")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "work for ASCII files" in {
      val expected = """[{"FIELD_1":"ї","FIELD_2":"ї","FIELD_3":"ä","FIELD_4":"ä"}]"""
      val data = Array(Array(0xBF, 0xBF, 0xE4, 0xE4).map(_.toByte))

      withTempBinFile("multicodepage", ".dat", data.head) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("encoding", "ascii")
          .option("field_code_page:cp1251", "FIELD-1,FIELD_2")
          .option("field_code_page:cp1252", " FIELD-3, FIELD_4 ")
          .load(fileName)

        val actual = df.toJSON.collect().mkString("[", ",", "]")
        assert(actual == expected)
      }
    }

    "fail for ASCII RDDs" in {
      val data = Array("ABCD")

      val rdd = spark.sparkContext.parallelize(data)

      val ex = intercept[IllegalArgumentException] {
        Cobrix.fromRdd
          .copybookContents(copybook)
          .option("field_code_page:cp037", "FIELD-1,FIELD_2")
          .option("field_code_page:cp870", " FIELD-3 ")
          .loadText(rdd)
      }

      assert(ex.getMessage.contains("Code page mapping using"))
    }
  }
}
