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

import java.nio.file.Paths

import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class Test11NoCopybookErrMsg extends FunSuite with SparkTestBase with BinaryFileFixture {

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """       01  R.
                03 A     PIC X(1).
                03 B     PIC X(2).
    """

  val binFileContents: Array[Byte] = Array[Byte](
    0xF0.toByte, 0xF1.toByte, 0xF2.toByte
  )

  test("Test a file loads normally when all mandatory parameters are provided") {
    withTempBinFile("bin_file2", ".dat", binFileContents) { tmpFileName =>
      val df = spark
        .read
        .format("cobol")
        .option("copybook_contents", copybook)
        .option("schema_retention_policy", "collapse_root")
        .load(tmpFileName)

      assert(df.count == 1)
    }
  }

  test("Test the error message logged when no copybook is provided") {
    val ex = intercept[IllegalStateException] {
      spark
        .read
        .format("cobol")
        .load("dummy")
    }

    assert(ex.getMessage.contains("COPYBOOK is not provided"))
  }

  test("Test the error message when copybook and copybook_contents are provided") {
    val ex = intercept[IllegalStateException] {
      spark
        .read
        .option("copybook", "dummy")
        .option("copybook_contents", copybook)
        .format("cobol")
        .load("dummy")
    }

    assert(ex.getMessage.contains("'copybook' and 'copybook_contents' options cannot be specified"))
  }

  test("Test the error message when copybooks and copybook_contents are provided") {
    val ex = intercept[IllegalStateException] {
      spark
        .read
        .option("copybooks", "dummy, dummy")
        .option("copybook_contents", copybook)
        .format("cobol")
        .load("dummy")
    }

    assert(ex.getMessage.contains("'copybooks' and 'copybook_contents' options cannot be specified"))
  }

  test("Test the error message when copybook and copybooks are provided") {
    val ex = intercept[IllegalStateException] {
      spark
        .read
        .option("copybook", "dummy, dummy")
        .option("copybooks", "dummy")
        .format("cobol")
        .load("dummy")
    }

    assert(ex.getMessage.contains("'copybook' and 'copybooks' options cannot be specified"))
  }

  test("Test the error message logged when copybook path is not a file") {
    withTempBinFile("bin_file4", ".dat", binFileContents) { tmpFileName =>
      val ex = intercept[IllegalArgumentException] {
        val path = Paths.get(tmpFileName)
        val parentPath = path.getParent.toString
        spark
          .read
          .format("cobol")
          .option("copybook", parentPath)
          .load(tmpFileName)
      }

      assert(ex.getMessage.contains("is not a file"))
    }
  }

  test("Test the error message logged when copybook local path is not a file") {
    withTempBinFile("bin_file4", ".dat", binFileContents) { tmpFileName =>
      val ex = intercept[IllegalArgumentException] {
        val path = Paths.get(tmpFileName)
        val parentPath = s"file://${path.getParent.toString}"
        spark
          .read
          .format("cobol")
          .option("copybook", parentPath)
          .load(tmpFileName)
      }

      assert(ex.getMessage.contains("is not a file"))
    }
  }

}
