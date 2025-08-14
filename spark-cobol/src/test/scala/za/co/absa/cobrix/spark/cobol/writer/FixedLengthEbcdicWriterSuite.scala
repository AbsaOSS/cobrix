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

package za.co.absa.cobrix.spark.cobol.writer

import org.apache.hadoop.fs.Path
import org.apache.spark.sql.SaveMode
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

class FixedLengthEbcdicWriterSuite extends AnyWordSpec with SparkTestBase with BinaryFileFixture {
  import spark.implicits._

  private val copybookContents =
    """       01  RECORD.
           05  A       PIC X(1).
           05  B       PIC X(5).
    """

  "cobol writer" should {
    "write simple fixed-record-length EBCDIC data files" in {
      withTempDirectory("cobol_writer1") { tempDir =>
        val df = List(("A", "First"), ("B", "Scnd"), ("C", "Last")).toDF("A", "B")

        val path = new Path(tempDir, "writer1")
        
        df.write
          .format("cobol")
          .mode(SaveMode.Overwrite)
          .option("copybook_contents", copybookContents)
          .save(path.toString)

        val fs = path.getFileSystem(spark.sparkContext.hadoopConfiguration)

        assert(fs.exists(path), "Output directory should exist")
        val files = fs.listStatus(path)
          .filter(_.getPath.getName.startsWith("part-"))
        assert(files.nonEmpty, "Output directory should contain part files")
      }
    }
  }

}
