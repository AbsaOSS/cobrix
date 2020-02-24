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

package za.co.absa.cobrix.spark.cobol.source.regression

import org.apache.spark.sql.functions.col
import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test08InputFileName extends FunSuite with SparkTestBase with BinaryFileFixture {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 A     PIC X(1).
                03 B     PIC X(2).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    // File offset start
    0x00, 0x00, 0x00, 0x00,
    // Records
    0xF0.toByte, 0xF1.toByte, 0xF2.toByte,
    0xF3.toByte, 0xF4.toByte, 0xF5.toByte,
    0xF6.toByte, 0xF7.toByte, 0xF8.toByte,
    // File offset end
    0x00, 0x00, 0x00, 0x00, 0x00
  )

  test("Test input data has the input file column and file offsets") {
    withTempBinFile("bin_file", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("with_input_file_name_col", "file")
        .option("file_start_offset", "4")
        .option("file_end_offset", "5")
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)
        .filter(col("file").contains("bin_file"))

      assert(df.count == 3)
    }
  }

  test("Test Cobrix throws an exceptions when conditions for 'schema_retention_policy' are not met ") {
    intercept[IllegalArgumentException] {
      spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("with_input_file_name_col", "file")
        .option("schema_retention_policy", "collapse_root")
        .load("dummy.dat")
    }
  }

}
